package com.artemis.systems;

import com.artemis.Aspect;
import com.artemis.Entity;
import com.artemis.World;
import com.artemis.component.ComponentX;
import com.artemis.utils.ImmutableBag;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("static-method")
public class IntervalEntitySystemTest {
	
	private static final float ACC = 0.0001f;

	@Test
	public void test_interval_delta() {
		
		World world = new World();
		IntervalSystem es = world.setSystem(new IntervalSystem());
		world.initialize();
		
		world.setDelta(1.1);
		world.process();
		assertEquals(1.1, es.getIntervalDelta(), ACC);
		
		world.setDelta(0.95);
		world.process();
		assertEquals(0.95, es.getIntervalDelta(), ACC);
	}

	private static class IntervalSystem extends IntervalEntitySystem {

		@SuppressWarnings("unchecked")
		public IntervalSystem() {
			super(Aspect.getAspectForAll(ComponentX.class), 1);
		}

		@Override
		protected void processEntities(ImmutableBag<Entity> entities) {
			
		}
	}
}
