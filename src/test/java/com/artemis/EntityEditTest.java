package com.artemis;


import com.artemis.component.ComponentX;
import com.artemis.component.ComponentY;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EntityEditTest {
	
	@SuppressWarnings("static-method")
	@Test
	public void basic_entity_edit_test() {
		World world = new World();
		LeManager lm = world.setManager(new LeManager());
		world.initialize();
		
		Entity e = world.createEntity();
		world.process();
		
		assertEquals(1, lm.added);
		assertEquals(0, lm.changed);
		
		EntityEdit edit = e.edit();
		edit.create(ComponentX.class);
		edit.create(ComponentY.class);
		
		world.process();
		
		assertEquals(1, lm.added);
		assertEquals(1, lm.changed);
	}
	
	@Test
	public void test_composition_identity_simple_case() {
		World world = new World();
		world.initialize();
		
		Entity e = world.createEntity();
		world.process();
		assertEquals(1, e.getCompositionId());
	}
	
	@Test
	public void test_composition_identity() {
		World world = new World();
		world.initialize();

		Entity e = world.createEntity();
		assertEquals(1, e.getCompositionId());
	}
	
	private static class LeManager extends Manager {
		
		int added, changed;
		
		@Override
		public void changed(Entity e) {
			changed++;
		}
		
		@Override
		public void added(Entity e) {
			added++;
		}
	}
}
