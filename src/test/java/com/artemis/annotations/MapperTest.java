package com.artemis.annotations;

import com.artemis.*;
import com.artemis.component.ComponentX;
import com.artemis.component.ComponentY;
import com.artemis.systems.EntityProcessingSystem;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MapperTest {
	
	private World world;
	private MappedSystem mappedSystem;
	private MappedManager mappedManager;
	private Entity entity;

	@Before
	public void init() {
		world = new World();
		mappedSystem = world.setSystem(new MappedSystem());
		mappedManager = world.setManager(new MappedManager());
		
		world.initialize();
		
		entity = world.createEntity();
		entity
				.edit()
				.add(new ComponentX())
				.add(new ComponentY());

		world.process();
	}
	
	@Test
	public void systems_support_mapper_annotation() {
		assertNotNull(mappedSystem.x);
		assertNotNull(mappedSystem.y);
		
		assertEquals(ComponentX.class, mappedSystem.x.get(entity).getClass());
		assertEquals(ComponentY.class, mappedSystem.y.get(entity).getClass());
	}
	
	@Test
	public void managers_support_mapper_annotation() {
		assertNotNull(mappedManager.x);
		assertNotNull(mappedManager.y);
		
		assertEquals(ComponentX.class, mappedSystem.x.get(entity).getClass());
		assertEquals(ComponentY.class, mappedSystem.y.get(entity).getClass());
	}

	@Wire
	private static class MappedSystem extends EntityProcessingSystem {
		private ComponentMapper<ComponentX> x;
		private ComponentMapper<ComponentY> y;
		
		@SuppressWarnings("unchecked")
		public MappedSystem() {
			super(Aspect.getAspectForAll(ComponentX.class, ComponentY.class));
		}

		@Override
		protected void process(Entity e) {}
		
	}

	@Wire
	private static class MappedManager extends Manager {
		private ComponentMapper<ComponentX> x;
		private ComponentMapper<ComponentY> y;
		
		@Override
		protected void initialize() {}
	}
}
