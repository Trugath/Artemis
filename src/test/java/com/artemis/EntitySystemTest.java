package com.artemis;

import com.artemis.utils.ImmutableBag;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EntitySystemTest {

    class EmptyComponentOne extends Component {

    }

    class EmptyComponentTwo extends Component {

    }


    @Test
    public void dummySystemTest() throws Exception {
        EntitySystem system = new EntitySystem(Aspect.getEmpty()) {
            @Override
            protected void processEntities(ImmutableBag<Entity> entities) {}

            @Override
            protected boolean checkProcessing() { return true; }
        };

        system.process();
        system.added(null);
        system.changed(null);
        system.deleted(null);
        system.disabled(null);
        system.enabled(null);
        assertTrue(system.getActives().isEmpty());
    }

    @Test
    public void singleAspectEmptySystemTest() throws Exception {
        EntitySystem system = new EntitySystem(Aspect.getAspectForAll(EmptyComponentOne.class)) {
            @Override
            protected void processEntities(ImmutableBag<Entity> entities) {}

            @Override
            protected boolean checkProcessing() { return true; }
        };

        system.process();
        system.added(null);
        system.changed(null);
        system.deleted(null);
        system.disabled(null);
        system.enabled(null);
        assertTrue(system.getActives().isEmpty());
    }

    @Test
    public void singleAspectSystemTest() throws Exception {

        World world = new World();
        EntitySystem system = new EntitySystem(Aspect.getAspectForAll(EmptyComponentOne.class)) {
            @Override
            protected void processEntities(ImmutableBag<Entity> entities) {}

            @Override
            protected boolean checkProcessing() { return true; }
        };
        world.setSystem(system);
        world.initialize();

        // entity not captured by the system
        Entity e1 = world.createEntity();
        e1.addComponent(new EmptyComponentTwo());
        world.addEntity(e1);

        // entity captured by the system
        Entity e2 = world.createEntity();
        e2.addComponent(new EmptyComponentOne());
        world.addEntity(e2);

        world.process();

        assertFalse(system.getActives().contains(e1));
        assertTrue(system.getActives().contains(e2));

        world.deleteEntity(e1);
        world.deleteEntity(e2);
        world.process();

        assertFalse(system.getActives().contains(e1));
        assertFalse(system.getActives().contains(e2));
    }

    @Test
    public void twiAspectORSystemTest() throws Exception {

        World world = new World();
        EntitySystem system = new EntitySystem(Aspect.getAspectForOne(EmptyComponentOne.class, EmptyComponentTwo.class)) {
            @Override
            protected void processEntities(ImmutableBag<Entity> entities) {}

            @Override
            protected boolean checkProcessing() { return true; }
        };
        world.setSystem(system);
        world.initialize();

        // entity captured by the system
        Entity e1 = world.createEntity();
        e1.addComponent(new EmptyComponentOne());
        world.addEntity(e1);

        // entity captured by the system
        Entity e2 = world.createEntity();
        e2.addComponent(new EmptyComponentTwo());
        world.addEntity(e2);

        world.process();

        assertTrue(system.getActives().contains(e1));
        assertTrue(system.getActives().contains(e2));

        world.deleteEntity(e1);
        world.deleteEntity(e2);
        world.process();

        assertFalse(system.getActives().contains(e1));
        assertFalse(system.getActives().contains(e2));
    }

    @Test
    public void twiAspectANDSystemTest() throws Exception {

        World world = new World();
        EntitySystem system = new EntitySystem(Aspect.getAspectForAll(EmptyComponentOne.class, EmptyComponentTwo.class)) {
            @Override
            protected void processEntities(ImmutableBag<Entity> entities) {}

            @Override
            protected boolean checkProcessing() { return true; }
        };
        world.setSystem(system);
        world.initialize();

        // entity not captured by the system
        Entity e1 = world.createEntity();
        e1.addComponent(new EmptyComponentOne());
        world.addEntity(e1);

        // entity captured by the system
        Entity e2 = world.createEntity();
        e2.addComponent(new EmptyComponentOne());
        e2.addComponent(new EmptyComponentTwo());
        world.addEntity(e2);

        world.process();

        assertFalse(system.getActives().contains(e1));
        assertTrue(system.getActives().contains(e2));

        world.deleteEntity(e1);
        world.deleteEntity(e2);
        world.process();

        assertFalse(system.getActives().contains(e1));
        assertFalse(system.getActives().contains(e2));
    }
}