package com.artemis;

import com.artemis.systems.EntityProcessingSystem;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

@SuppressWarnings("static-method")
public class Issue206SystemTest {

	@Test
	public void test_edited_bitset_sanity() {
		World world = new World();
		world.initialize();

		world.setSystem(new TestSystemAB());
		world.initialize();

		Entity e = world.createEntity();
		e.edit().create(CompA.class);
		e.edit().create(CompB.class);
		e.edit().create(TestComponentC.class);

		world.process();
		
		assertSame(e.edit(), e.edit());
		e.edit().remove(CompB.class);
		// nota bene: in 0.7.0 and 0.7.1, chaining edit() caused
		// the componentBits to reset
		e.edit().remove(TestComponentC.class);

		world.process();
		world.process();
	}

	public static class CompA extends Component {}
	public static class CompB extends Component {}
	public static class TestComponentC extends Component {}

	private static class TestSystemAB extends EntityProcessingSystem {
		@SuppressWarnings("unchecked")
		public TestSystemAB() {
			super(Aspect.getAspectForAll(CompA.class, CompB.class));
		}

		@Override
		protected void process(Entity e) {
			assertNotNull(e.getComponent(CompB.class));
		}
	}
}