package com.artemis;

import com.artemis.annotations.Mapper;
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
		entity.addComponent(new ComponentX());
		entity.addComponent(new ComponentY());
		
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

	private static class MappedSystem extends EntityProcessingSystem {
		@Mapper private ComponentMapper<ComponentX> x;
		@Mapper private ComponentMapper<ComponentY> y;
		
		@SuppressWarnings("unchecked")
		public MappedSystem() {
			super(Aspect.getAspectForAll(ComponentX.class, ComponentY.class));
		}

		@Override
		protected void process(Entity e) {}
		
	}

	private static class MappedManager extends Manager {
		@Mapper private ComponentMapper<ComponentX> x;
		@Mapper private ComponentMapper<ComponentY> y;
		
		@Override
		protected void initialize() {}
	}
}
