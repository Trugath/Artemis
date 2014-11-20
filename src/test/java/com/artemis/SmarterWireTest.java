package com.artemis;

import com.artemis.annotations.Wire;
import com.artemis.component.ComponentX;
import com.artemis.component.ComponentY;
import com.artemis.systems.VoidEntitySystem;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SmarterWireTest {
	
	private World world;
	private EntityFactory entityFactory;
	private TiledMapSystem tiledMapSystem;
	

	@Before
	public void init() {
		world = new World();
		entityFactory = world.setManager(new EntityFactory());
		tiledMapSystem = world.setSystem(new TiledMapSystem());
		
		world.initialize();
		
		world.process();
	}
	
	@Test
	public void ensure_mappers_are_injected() {
		assertNotNull(entityFactory.x);
		assertNotNull(entityFactory.y);
	}
	
	@Test
	public void abstract_managers_are_resolved() { 
		assertNotNull(tiledMapSystem.factory);
		assertTrue(tiledMapSystem.factory.getClass() == EntityFactory.class);
	}
	
	abstract class AbstractEntityFactory extends Manager {
		protected ComponentMapper<ComponentX> x;
	}
	
	@Wire(injectInherited=true)
	class EntityFactory extends AbstractEntityFactory {
		private ComponentMapper<ComponentY> y;
	}
	
	@Wire
	class TiledMapSystem extends VoidEntitySystem {
		// no concrete factory available in library, game should provide.
		protected AbstractEntityFactory factory;

		@Override
		protected void processSystem() {
			
		} 
	}
}
