package com.artemis;

import com.artemis.annotations.Mapper;
import com.artemis.managers.UuidEntityManager;
import com.artemis.utils.Bag;
import com.artemis.utils.ImmutableBag;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The primary instance for the framework. It contains all the managers.
 * 
 * You must use this to create, delete and retrieve entities.
 * 
 * It is also important to set the delta each game loop iteration, and initialize before game loop.
 * 
 * @author Arni Arent
 * 
 */
public class World {
	private final EntityManager em;
	private final ComponentManager cm;

	private double delta;
	private final Bag<Entity> added;
	private final Bag<Entity> changed;
	private final Bag<Entity> deleted;
	private final Bag<Entity> enable;
	private final Bag<Entity> disable;

	private final Map<Class<? extends Manager>, Manager> managers;
	private final Bag<Manager> managersBag;
	
	private final Map<Class<?>, EntitySystem> systems;
	private final Bag<EntitySystem> systemsBag;

	private boolean hasUuidManager = false;
	boolean hasUuidManager() { return hasUuidManager; }

	public World() {
		managers = new HashMap<>();
		managersBag = new Bag<>();
		
		systems = new HashMap<>();
		systemsBag = new Bag<>();

		added = new Bag<>();
		changed = new Bag<>();
		deleted = new Bag<>();
		enable = new Bag<>();
		disable = new Bag<>();

		cm = new ComponentManager();
		setManager(cm);
		
		em = new EntityManager();
		setManager(em);
	}

	
	/**
	 * Makes sure all managers systems are initialized in the order they were added.
	 */
	public void initialize() {
		for (Manager m : managersBag) {
			ComponentMapperInitHelper.config(m, this);
			m.initialize();
		}
		
		for (EntitySystem s : systemsBag) {
			ComponentMapperInitHelper.config(s, this);
			s.initialize();
		}
	}
	
	
	/**
	 * Returns a manager that takes care of all the entities in the world.
	 * entities of this world.
	 * 
	 * @return entity manager.
	 */
	public EntityManager getEntityManager() {
		return em;
	}
	
	/**
	 * Returns a manager that takes care of all the components in the world.
	 * 
	 * @return component manager.
	 */
	public ComponentManager getComponentManager() {
		return cm;
	}
	
	
	

	/**
	 * Add a manager into this world. It can be retrieved later.
	 * World will notify this manager of changes to entity.
	 * 
	 * @param manager to be added
	 */
	public <T extends Manager> T setManager(T manager) {
		managers.put(manager.getClass(), manager);
		managersBag.add(manager);
		manager.setWorld(this);
		if(manager instanceof UuidEntityManager)
			hasUuidManager = true;
		return manager;
	}

	/**
	 * Returns a manager of the specified type.
	 * 
	 * @param <T>
	 * @param managerType
	 *            class type of the manager
	 * @return the manager
	 */
	public <T extends Manager> T getManager(Class<T> managerType) {
		return managerType.cast(managers.get(managerType));
	}
	
	/**
	 * Deletes the manager from this world.
	 * @param manager to delete.
	 */
	public void deleteManager(Manager manager) {
		managers.remove(manager.getClass());
		managersBag.remove(manager);
		if(manager instanceof UuidEntityManager)
			hasUuidManager = false;
	}

	
	
	
	/**
	 * Time since last game loop.
	 * 
	 * @return delta time since last game loop.
	 */
	public double getDelta() {
		return delta;
	}

	/**
	 * You must specify the delta for the game here.
	 * 
	 * @param delta time since last game loop.
	 */
	public void setDelta(double delta) {
		this.delta = delta;
	}
	


	/**
	 * Adds a entity to this world.
	 * 
	 * @param e entity
	 */
	public void addEntity(Entity e) {
		added.add(e);
	}
	
	/**
	 * Ensure all systems are notified of changes to this entity.
	 * If you're adding a component to an entity after it's been
	 * added to the world, then you need to invoke this method.
	 * 
	 * @param e entity
	 */
	public void changedEntity(Entity e) {
		changed.add(e);
	}
	
	/**
	 * Delete the entity from the world.
	 * 
	 * @param e entity
	 */
	public void deleteEntity(Entity e) {
		if (!deleted.contains(e)) {
			deleted.add(e);
		}
	}

	/**
	 * (Re)enable the entity in the world, after it having being disabled.
	 * Won't do anything unless it was already disabled.
	 */
	public void enable(Entity e) {
		enable.add(e);
	}

	/**
	 * Disable the entity from being processed. Won't delete it, it will
	 * continue to exist but won't get processed.
	 */
	public void disable(Entity e) {
		disable.add(e);
	}


	/**
	 * Create and return a new or reused entity instance.
	 * Will NOT add the entity to the world, use World.addEntity(Entity) for that.
	 * 
	 * @return entity
	 */
	public Entity createEntity() {
		Entity e = em.createEntityInstance();
		UuidEntityManager manager = getManager(UuidEntityManager.class);
		if(manager != null)
			manager.setUuid(e, e.getUuid());
		return e;
	}

	/**
	 * Create and return a new or reused entity instance.
	 * Will NOT add the entity to the world, use World.addEntity(Entity) for that.
	 *
	 * @return entity
	 */
	public Entity createEntity(UUID uuid) {
		Entity e = em.createEntityInstance();
		e.setUuid(uuid);
		UuidEntityManager manager = getManager(UuidEntityManager.class);
		if(manager != null)
			manager.setUuid(e, uuid);
		return e;
	}


	/**
	 * Get a entity having the specified id.
	 * 
	 * @param entityId
	 * @return entity
	 */
	public Entity getEntity(int entityId) {
		return em.getEntity(entityId);
	}

	


	/**
	 * Gives you all the systems in this world for possible iteration.
	 * 
	 * @return all entity systems in world.
	 */
	public ImmutableBag<EntitySystem> getSystems() {
		return systemsBag;
	}

	/**
	 * Adds a system to this world that will be processed by World.process()
	 * 
	 * @param system the system to add.
	 * @return the added system.
	 */
	public <T extends EntitySystem> T setSystem(T system) {
		return setSystem(system, false);
	}

	/**
	 * Will add a system to this world.
	 *  
	 * @param system the system to add.
	 * @param passive wether or not this system will be processed by World.process()
	 * @return the added system.
	 */
	public <T extends EntitySystem> T setSystem(T system, boolean passive) {
		system.setWorld(this);
		system.setPassive(passive);
		
		systems.put(system.getClass(), system);
		systemsBag.add(system);
		
		return system;
	}
	
	/**
	 * Removed the specified system from the world.
	 * @param system to be deleted from world.
	 */
	public void deleteSystem(EntitySystem system) {
		systems.remove(system.getClass());
		systemsBag.remove(system);
	}

	private void notifySystems(Performer performer, Entity e) {
		for(EntitySystem system : systemsBag) {
			performer.perform(system, e);
		}
	}

	private void notifyManagers(Performer performer, Entity e) {
		for(Manager m : managersBag) {
			performer.perform(m, e);
		}
	}
	
	/**
	 * Retrieve a system for specified system type.
	 * 
	 * @param type type of system.
	 * @return instance of the system in this world.
	 */
	public <T extends EntitySystem> T getSystem(Class<T> type) {
		return type.cast(systems.get(type));
	}

	
	/**
	 * Performs an action on each entity.
	 * @param entities
	 * @param performer
	 */
	private void check(Bag<Entity> entities, Performer performer) {
		if (!entities.isEmpty()) {
			for (Entity e : entities) {
				notifyManagers(performer, e);
				notifySystems(performer, e);
			}
			entities.clear();
		}
	}

	
	/**
	 * Process all non-passive systems.
	 */
	public void process() {
		check(added, new Performer() {
			@Override
			public void perform(EntityObserver observer, Entity e) {
				observer.added(e);
			}
		});
		
		check(changed, new Performer() {
			@Override
			public void perform(EntityObserver observer, Entity e) {
				observer.changed(e);
			}
		});
		
		check(disable, new Performer() {
			@Override
			public void perform(EntityObserver observer, Entity e) {
				observer.disabled(e);
			}
		});
		
		check(enable, new Performer() {
			@Override
			public void perform(EntityObserver observer, Entity e) {
				observer.enabled(e);
			}
		});
		
		check(deleted, new Performer() {
			@Override
			public void perform(EntityObserver observer, Entity e) {
				observer.deleted(e);
			}
		});
		
		cm.clean();
		
		for(EntitySystem system : systemsBag) {
			if(!system.isPassive()) {
				system.process();
			}
		}
	}
	

	/**
	 * Retrieves a ComponentMapper instance for fast retrieval of components from entities.
	 * 
	 * @param type of component to get mapper for.
	 * @return mapper for specified component type.
	 */
	public <T extends Component> ComponentMapper<T> getMapper(Class<T> type) {
		return ComponentMapper.getFor(type, this);
	}

	/*
	 * Only used internally to maintain clean code.
	 */
	private interface Performer {
		void perform(EntityObserver observer, Entity e);
	}

	
	
	private static class ComponentMapperInitHelper {

		public static void config(Object target, World world) {
			try {
				configHelper (target, target.getClass (), world);
			} catch (Exception e) {
				throw new RuntimeException("Error while setting component mappers", e);
			}
		}

		private static void configHelper(Object target, Class<?> clazz, World world) {
			try {
				for (Field field : clazz.getDeclaredFields()) {
					Mapper annotation = field.getAnnotation(Mapper.class);
					if (annotation != null && Mapper.class.isAssignableFrom(Mapper.class)) {
						ParameterizedType genericType = (ParameterizedType) field.getGenericType();

						@SuppressWarnings("unchecked")
						Class<? extends Component> componentType = (Class<? extends Component>) genericType.getActualTypeArguments()[0];

						boolean accessible = field.isAccessible();
						field.setAccessible(true);
						if(field.get(target) == null)
							field.set(target, world.getMapper(componentType));
						field.setAccessible(accessible);
					}
				}
			} catch (Exception e) {
				throw new RuntimeException("Error while setting component mappers", e);
			}

			Class< ? > superClazz = clazz.getSuperclass ();
			if (superClazz != null)
				configHelper (target, superClazz, world);
		}
	}


}
