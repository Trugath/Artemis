package com.artemis;

import com.artemis.annotations.Wire;
import com.artemis.managers.UuidEntityManager;
import com.artemis.utils.Bag;
import com.artemis.utils.ImmutableBag;
import com.artemis.utils.reflect.ClassReflection;
import com.artemis.utils.reflect.Constructor;
import com.artemis.utils.reflect.Field;
import com.artemis.utils.reflect.ReflectionException;

import java.util.*;


/**
 * The primary instance for the framework.
 * <p>
 * It contains all the managers. You must use this to create, delete and
 * retrieve entities. It is also important to set the delta each game loop
 * iteration, and initialize before game loop.
 * </p>
 *
 * @author Arni Arent
 */
public class World {

	/**
	 * Manages all entities for the world.
	 */
	private final EntityManager em;
	/**
	 * Manages all component-entity associations for the world.
	 */
	private final ComponentManager cm;
	/**
	 * The time passed since the last update.
	 */
	public double delta;

	final WildBag<Entity> added;
	final WildBag<Entity> changed;
	final WildBag<Entity> disabled;
	final WildBag<Entity> enabled;
	final WildBag<Entity> deleted;

	/**
	 * Runs actions on systems and managers when entities get added.
	 */
	private final AddedPerformer addedPerformer;
	/**
	 * Runs actions on systems and managers when entities are changed.
	 */
	private final ChangedPerformer changedPerformer;
	/**
	 * Runs actions on systems and managers when entities are deleted.
	 */
	private final DeletedPerformer deletedPerformer;

	/**
	 * Contains all managers and managers classes mapped.
	 */
	private final Map<Class<? extends Manager>, Manager> managers;
	/**
	 * Contains all managers unordered.
	 */
	private final WildBag<Manager> managersBag;
	/**
	 * Contains all systems and systems classes mapped.
	 */
	private final Map<Class<?>, EntitySystem> systems;
	/**
	 * Contains all systems unordered.
	 */
	private final WildBag<EntitySystem> systemsBag;
	/**
	 * Contains all uninitialized systems. *
	 */
	private final Bag<EntitySystem> systemsToInit;
	
	private boolean registerUuids;
	private ArtemisInjector injector;
	
	int rebuiltIndices;
	private int maxRebuiltIndicesPerTick;

	final EntityEditPool editPool = new EntityEditPool(this);
	
	private boolean initialized;
	
	/**
	 * Creates a new world.
	 * <p>
	 * An EntityManager and ComponentManager are created and added upon
	 * creation.
	 * </p>
	 */
	public World() {
		this(new WorldConfiguration().maxRebuiltIndicesPerTick(64));
	}

	/**
	 * Creates a new world.
	 * <p>
	 * An EntityManager and ComponentManager are created and added upon
	 * creation.
	 * </p>
	 */
	public World(WorldConfiguration configuration) {
		managers = new IdentityHashMap<Class<? extends Manager>, Manager>();
		managersBag = new WildBag<Manager>();

		systems = new IdentityHashMap<Class<?>, EntitySystem>();
		systemsBag = new WildBag<EntitySystem>();
		systemsToInit = new Bag<EntitySystem>();

		added = new WildBag<Entity>();
		changed = new WildBag<Entity>();
		deleted = new WildBag<Entity>();
		enabled = new WildBag<Entity>();
		disabled = new WildBag<Entity>();

		addedPerformer = new AddedPerformer();
		changedPerformer = new ChangedPerformer();
		deletedPerformer = new DeletedPerformer();

		cm = new ComponentManager(configuration.expectedEntityCount());
		setManager(cm);

		em = new EntityManager(configuration.expectedEntityCount());
		setManager(em);
		
		maxRebuiltIndicesPerTick = configuration.maxRebuiltIndicesPerTick();
		injector = new ArtemisInjector(this, configuration);
	}
	
	/**
	 * Makes sure all managers systems are initialized in the order they were
	 * added.
	 */
	public void initialize() {
		initialized = true;
		injector.update();
		for (int i = 0; i < managersBag.size(); i++) {
			Manager manager = managersBag.get(i);
			injector.inject(manager);
			manager.initialize();
		}

		initializeSystems();
	}

	/**
	 * Inject dependencies on object.
	 *
	 * Immediately perform dependency injection on the target.
	 * {@link com.artemis.annotations.Wire} annotation is required on the target
	 * or fields.
	 *
	 * If you want to specify nonstandard dependencies to inject, use
	 * {@link com.artemis.WorldConfiguration#register(String, Object)} instead.
	 *
	 * @see com.artemis.annotations.Wire for more details about dependency injection.
	 * @param target Object to inject into.
	 */
	public void inject(Object target) {
		assertInitialized();
		injector.inject(target);
	}

	private void assertInitialized() {
		if (!initialized)
			throw new MundaneWireException("World#initialize() has not yet been called.");
	}

	/**
	 * Disposes all managers and systems. Only necessary if either need to free
	 * managed resources upon bringing the world to an end.
	 *
	 * @throws ArtemisMultiException if any managers or systems throws an exception.
	 */
	public void dispose() {
		List<Throwable> exceptions = new ArrayList<Throwable>();

		for (Manager manager : managersBag) {
			try {
				manager.dispose();
			} catch (Exception e) {
				exceptions.add(e);
			}
		}

		for (EntitySystem system : systemsBag) {
			try {
				system.dispose();
			} catch (Exception e) {
				exceptions.add(e);
			}
		}

		if (exceptions.size() > 0)
			throw new ArtemisMultiException(exceptions);
	}
	
	@SuppressWarnings("unchecked")
	<T extends EntityFactory<T>> T createFactory(Class<? extends T> factory) {
		if (!factory.isInterface())
			throw new MundaneWireException("Expected interface for type: " + factory);
		
		assertInitialized();
		
		String impl = factory.getName() + "Impl";
		try {
			Class<?> implClass = ClassReflection.forName(impl);
			Constructor constructor = ClassReflection.getConstructor(implClass, World.class);
			return (T) constructor.newInstance(this);
		} catch (ReflectionException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns a manager that takes care of all the entities in the world.
	 *
	 * @return entity manager
	 */
	public EntityManager getEntityManager() {
		return em;
	}

	/**
	 * Returns a manager that takes care of all the components in the world.
	 *
	 * @return component manager
	 */
	public ComponentManager getComponentManager() {
		return cm;
	}

	/**
	 * Add a manager into this world.
	 * <p>
	 * It can be retrieved later. World will notify this manager of changes to
	 * entity.
	 * </p>
	 *
	 * @param <T>	 class type of the manager
	 * @param manager manager to be added
	 * @return the manager
	 */
	public final <T extends Manager> T setManager(T manager) {
		managers.put(manager.getClass(), manager);
		managersBag.add(manager);
		manager.setWorld(this);
		
		if (manager instanceof UuidEntityManager)
			registerUuids = true;

		return manager;
	}

	/**
	 * Returns a manager of the specified type.
	 *
	 * @param <T>		 class type of the manager
	 * @param managerType class type of the manager
	 * @return the manager
	 */
	@SuppressWarnings("unchecked")
	public <T extends Manager> T getManager(Class<T> managerType) {
		return (T) managers.get(managerType);
	}
	
	/**
	 * @return all managers in this world
	 */
	public ImmutableBag<Manager> getManagers() {
		return managersBag;
	}

	/**
	 * Time since last game loop.
	 *
	 * @return delta time since last game loop
	 */
	public double getDelta() {
		return delta;
	}

	/**
	 * You must specify the delta for the game here.
	 *
	 * @param delta time since last game loop
	 */
	public void setDelta(double delta) {
		this.delta = delta;
	}

	boolean isRebuildingIndexAllowed() {
		return maxRebuiltIndicesPerTick > rebuiltIndices;
	}

	/**
	 * Create and return a new or reused entity instance. Entity is 
	 * automatically added to the world.
	 *
	 * @return entity
	 */
	public Entity createEntity() {
		Entity e = em.createEntityInstance();
		e.edit();
		return e;
	}
	
	/**
	 * Create and return an {@link Entity} wrapping a new or reused entity instance.
	 * Entity is automatically added to the world.
	 *
	 * @return entity
	 */
	public Entity createEntity(Archetype archetype) {
		Entity e = em.createEntityInstance(archetype);
		cm.addComponents(e, archetype);
		added.add(e);
		return e;
	}
	
	/**
	 * Create and return a new or reused entity instance.
	 * <p>
	 * The uuid parameter is ignored if {@link UuidEntityManager} hasn't been added to the
	 * world. 
	 * </p>
	 *
	 * @param uuid the UUID to give to the entity
	 * @return entity
	 */
	public Entity createEntity(UUID uuid) {
		Entity entity = em.createEntityInstance();
		entity.setUuid(uuid);
		entity.edit();
		return entity;
	}

	/**
	 * Get a entity having the specified id.
	 *
	 * @param entityId the entities id
	 * @return the specific entity
	 */
	public Entity getEntity(int entityId) {
		return em.getEntity(entityId);
	}

	/**
	 * Gives you all the systems in this world for possible iteration.
	 *
	 * @return all entity systems in world
	 */
	public ImmutableBag<EntitySystem> getSystems() {
		return systemsBag;
	}
	
	/**
	 * Adds a system to this world that will be processed by
	 * {@link #process()}.
	 *
	 * @param <T>	the system class type
	 * @param system the system to add
	 * @return the added system
	 */
	public <T extends EntitySystem> T setSystem(T system) {
		return setSystem(system, false);
	}

	/**
	 * Will add a system to this world.
	 *
	 * @param <T>	 the system class type
	 * @param system  the system to add
	 * @param passive whether or not this system will be processed by
	 *				{@link #process()}
	 * @return the added system
	 */
	public <T extends EntitySystem> T setSystem(T system, boolean passive) {
		system.setWorld(this);
		system.setPassive(passive);

		systems.put(system.getClass(), system);
		systemsBag.add(system);
		systemsToInit.add(system);

		return system;
	}

	/**
	 * Run performers on all systems.
	 *
	 * @param performer the performer to run
	 * @param entities the entity to pass as argument to the systems
	 */
	private void notifySystems(Performer performer, WildBag<Entity> entities) {
		Object[] data = systemsBag.getData();
		for (int i = 0, s = systemsBag.size(); s > i; i++) {
			performer.perform((EntitySystem) data[i], entities);
		}
	}

	/**
	 * Run performers on all managers.
	 *
	 * @param performer the performer to run
	 * @param entities the entity to pass as argument to the managers
	 */
	private void notifyManagers(Performer performer, WildBag<Entity> entities) {
		Object[] data = managersBag.getData();
		for (int i = 0, s = managersBag.size(); s > i; i++) {
			performer.perform((Manager) data[i], entities);
		}
	}

	/**
	 * Retrieve a system for specified system type.
	 *
	 * @param <T>  the class type of system
	 * @param type type of system
	 * @return instance of the system in this world
	 */
	@SuppressWarnings("unchecked")
	public <T extends EntitySystem> T getSystem(Class<T> type) {
		return (T) systems.get(type);
	}

	/**
	 * Performs an action on each entity.
	 *
	 * @param entityBag contains the entities upon which the action will be performed
	 * @param performer the performer that carries out the action
	 */
	private void check(WildBag<Entity> entityBag, Performer performer) {
		if (entityBag.size() == 0)
			return;
		
		notifyManagers(performer, entityBag);
		notifySystems(performer, entityBag);
		entityBag.setSize(0);
	}
	
	void processComponentIdentity(int id, BitSet componentBits) {
		Object[] data = systemsBag.getData();
		for (int i = 0, s = systemsBag.size(); s > i; i++) {
			((EntitySystem)data[i]).processComponentIdentity(id, componentBits);
		}
	}

	/**
	 * Process all non-passive systems.
	 */
	public void process() {
		rebuiltIndices = 0;
		
		updateEntityStates();

		em.clean();
		cm.clean();

		// Some systems may add other systems in their initialize() method.
		// Initialize those newly added systems right after setSystem() call.
		if (systemsToInit.size() > 0) {
			initializeSystems();
		}

		Object[] systemsData = systemsBag.getData();
		for (int i = 0, s = systemsBag.size(); s > i; i++) {
			updateEntityStates();
			
			EntitySystem system = (EntitySystem) systemsData[i];
			if (!system.isPassive()) {
				system.process();
			}
		}
	}

	private void updateEntityStates() {
		while (added.size() > 0 || changed.size() > 0) {
			check(added, addedPerformer);
			check(changed, changedPerformer);
		}
		
		while(editPool.processEntities()) {
			check(added, addedPerformer);
			check(changed, changedPerformer);
			check(deleted, deletedPerformer);
		}
	}

	private void initializeSystems() {
		for (int i = 0, s = systemsToInit.size(); i < s; i++) {
			EntitySystem es = systemsToInit.get(i);
			injector.inject(es);
			es.initialize();
		}
		systemsToInit.clear();
	}

	boolean hasUuidManager() {
		return registerUuids;
	}

	/**
	 * Retrieves a ComponentMapper instance for fast retrieval of components
	 * from entities.
	 *
	 * @param <T>  class type of the component
	 * @param type type of component to get mapper for
	 * @return mapper for specified component type
	 */
	public <T extends Component> ComponentMapper<T> getMapper(Class<T> type) {
		return BasicComponentMapper.getFor(type, this);
	}


	/**
	 * Runs {@link EntityObserver#deleted}.
	 */
	private static final class DeletedPerformer implements Performer {

		@Override
		public void perform(EntityObserver observer, WildBag<Entity> entities) {
			observer.deleted(entities);
		}
	}

	/**
	 * Runs {@link EntityObserver#changed}.
	 */
	private static final class ChangedPerformer implements Performer {
		
		@Override
		public void perform(EntityObserver observer, WildBag<Entity> entities) {
			observer.changed(entities);
		}
	}

	/**
	 * Runs {@link EntityObserver#added}.
	 */
	private static final class AddedPerformer implements Performer {
		
		@Override
		public void perform(EntityObserver observer, WildBag<Entity> entities) {
			observer.added(entities);
		}
	}

	/**
	 * Calls methods on observers.
	 * <p>
	 * Only used internally to maintain clean code.
	 * </p>
	 */
	private interface Performer {

		/**
		 * Call a method on the observer with the entity as argument.
		 *
		 * @param observer the observer with the method to calll
		 * @param entities	the entities to pass as argument
		 */
		void perform(EntityObserver observer, WildBag<Entity> entities);
	}

	/**
	 * Injects {@link ComponentMapper}, {@link EntitySystem} and {@link Manager} types into systems and
	 * managers.
	 */
	private static final class ArtemisInjector {
		private final World world;
		
		private final Map<Class<?>, Class<?>> systems;
		private final Map<Class<?>, Class<?>> managers;
		private final Map<String, Object> pojos;
		
		ArtemisInjector(World world, WorldConfiguration config) {
			this.world = world;
			
			systems = new IdentityHashMap<Class<?>, Class<?>>();
			managers = new IdentityHashMap<Class<?>, Class<?>>();
			pojos = new HashMap<String, Object>(config.injectables);
		}
		
		void update() {
			for (EntitySystem es : world.getSystems()) {
				Class<?> origin = es.getClass();
				Class<?> clazz = origin;
				do {
					systems.put(clazz, origin);
				} while ((clazz = clazz.getSuperclass()) != Object.class);
			}
			
			for (Manager manager : world.managersBag) {
				Class<?> origin = manager.getClass();
				Class<?> clazz = origin;
				do {
					managers.put(clazz, origin);
				} while ((clazz = clazz.getSuperclass()) != Object.class);
			}
			
		}

		public void inject(Object target) throws RuntimeException {
			try {
				Class<?> clazz = target.getClass();

				if (ClassReflection.isAnnotationPresent(clazz, Wire.class)) {
					Wire wire = ClassReflection.getDeclaredAnnotation(clazz, Wire.class).getAnnotation(Wire.class);
					if (wire != null) {
						injectValidFields(target, clazz, wire.failOnNull(), wire.injectInherited());
					}
				} else {
					injectAnnotatedFields(target, clazz);
				}
			} catch (ReflectionException e) {
				throw new MundaneWireException("Error while wiring", e);
			}
		}

		private void injectValidFields(Object target, Class<?> clazz, boolean failOnNull, boolean injectInherited)
				throws ReflectionException {

			Field[] declaredFields = ClassReflection.getDeclaredFields(clazz);
			for (int i = 0, s = declaredFields.length; s > i; i++) {
				injectField(target, declaredFields[i], failOnNull);
			}

			// should bail earlier, but it's just one more round.
			while (injectInherited && (clazz = clazz.getSuperclass()) != Object.class) {
				injectValidFields(target, clazz, failOnNull, injectInherited);
			}
		}

		private void injectAnnotatedFields(Object target, Class<?> clazz)
			throws ReflectionException {

			injectClass(target, clazz);
		}

		@SuppressWarnings("deprecation")
		private void injectClass(Object target, Class<?> clazz) throws ReflectionException {
			Field[] declaredFields = ClassReflection.getDeclaredFields(clazz);
			for (int i = 0, s = declaredFields.length; s > i; i++) {
				Field field = declaredFields[i];
				if (field.isAnnotationPresent(Wire.class)) {
					injectField(target, field, field.isAnnotationPresent(Wire.class));
				}
			}
		}

		@SuppressWarnings("unchecked")
		private void injectField(Object target, Field field, boolean failOnNotInjected)
			throws ReflectionException {

			field.setAccessible(true);

			Class<?> fieldType;
			try {
				fieldType = field.getType();
			} catch (RuntimeException ignore) {
				// Swallow exception caused by missing typedata on gwt platform.
				// @todo Workaround, awaiting junkdog-ification. Silently failing injections might be undesirable for users failing to add systems/components to gwt reflection inclusion config.
				return;
			}

			if (ClassReflection.isAssignableFrom(ComponentMapper.class, fieldType)) {
				ComponentMapper<?> mapper = world.getMapper(field.getElementType(0));
				if (failOnNotInjected && mapper == null) {
					throw new MundaneWireException("ComponentMapper not found for " + fieldType);
				}
				field.set(target, mapper);
			} else if (ClassReflection.isAssignableFrom(EntitySystem.class, fieldType)) {
				EntitySystem system = world.getSystem((Class<EntitySystem>)systems.get(fieldType));
				if (failOnNotInjected && system == null) {
					throw new MundaneWireException("EntitySystem not found for " + fieldType);
				}
				field.set(target, system);
			} else if (ClassReflection.isAssignableFrom(Manager.class, fieldType)) {
				Manager manager = world.getManager((Class<Manager>)managers.get(fieldType));
				if (failOnNotInjected && manager == null) {
					throw new MundaneWireException("Manager not found for " + fieldType);
				}
				field.set(target, manager);
			} else if (ClassReflection.isAssignableFrom(EntityFactory.class, fieldType)) {
				EntityFactory<?> factory = world.createFactory(field.getType());
				if (failOnNotInjected && factory == null) {
					throw new MundaneWireException("Factory not found for " + fieldType);
				}
				field.set(target, factory);
			} else if (field.isAnnotationPresent(Wire.class)) {
				final Wire wire = field.getDeclaredAnnotation(Wire.class).getAnnotation(Wire.class);
				String key = wire.name();
				if ("".equals(key))
					key = field.getType().getName();
				
				if (pojos.containsKey(key))
					field.set(target, pojos.get(key));
			}
		}
	}
}
