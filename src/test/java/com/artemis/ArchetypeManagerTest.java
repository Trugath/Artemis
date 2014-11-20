package com.artemis;

import com.artemis.annotations.Wire;
import com.artemis.component.ComponentX;
import com.artemis.component.ComponentY;
import com.artemis.systems.EntityProcessingSystem;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ArchetypeManagerTest {
	private World world;
	private Es1 es1;
	private Es2 es2;
	private Archetype arch1;
	private Archetype arch2;
	private Archetype arch3;
	private EntityFactory factory;

	@Before
	public void init() {
		world = new World();
		factory = world.setManager(new EntityFactory());
		es1 = world.setSystem(new Es1());
		es2 = world.setSystem(new Es2());
		world.initialize();
	}
	
	@Test
	public void test_composition_id() throws Exception {
		assertEquals(1, arch1.compositionId);
		assertEquals(2, arch2.compositionId);
		assertEquals(3, arch3.compositionId);
	}
	
	@Test
	public void test_archetypes_component_classes() throws Exception {
		assertEquals(0, arch1.types.length);
		assertEquals(2, arch2.types.length);
		assertEquals(ComponentX.class, arch2.types[0].getType());
		assertEquals(ComponentY.class, arch2.types[1].getType());
		assertEquals(1, arch3.types.length);
		assertEquals(ComponentX.class, arch3.types[0].getType());
	}
	
	@Test
	public void test_adding_to_systems() {
		archetypeEntity(arch1, 2); // never inserted
		archetypeEntity(arch2, 4); // es1
		archetypeEntity(arch3, 8); // es1 + 2
		
		world.process();
		
		assertEquals(12, es1.getActives().size());
		assertEquals(8, es2.getActives().size());
		
		world.process();
	}

	private void archetypeEntity(Archetype arch, int s) {
		for (int i = 0; s > i; i++) {
			world.createEntity(arch);
		}
	}
	
	private class EntityFactory extends Manager {
		
		@Override
		protected void initialize() {
			arch1 = new ArchetypeBuilder()
				.build(world);
			arch2 = new ArchetypeBuilder()
				.add(ComponentX.class)
				.add(ComponentY.class)
				.build(world);
			arch3 = new ArchetypeBuilder()
				.add(ComponentX.class)
				.build(world);
		}
	}
	
	@Wire
	private static class Es1 extends EntityProcessingSystem {

		private ComponentMapper<ComponentX> componentXMapper;
		
		@SuppressWarnings("unchecked")
		public Es1() {
			super(Aspect.getAspectForAll(ComponentX.class));
		}

		@Override
		protected void process(Entity e) {
			assertNotNull(componentXMapper.get(e));
		}
	}
	
	@Wire
	private static class Es2 extends EntityProcessingSystem {
		
		private ComponentMapper<ComponentX> componentXMapper;
		
		@SuppressWarnings("unchecked")
		public Es2() {
			super(Aspect.getAspectForAll(ComponentX.class).exclude(ComponentY.class));
		}
		
		@Override
		protected void process(Entity e) {
			assertNotNull(componentXMapper.get(e));
		}
	}
}
