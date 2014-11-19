package com.artemis.systems;

import com.artemis.*;
import com.artemis.annotations.Mapper;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EntityProcessingSystemTest {

    class RemovableComponent extends Component {

    }

    class IssueTenEntityProcessingSystem extends EntityProcessingSystem {

        public boolean insertedTriggered = false;
        public boolean removedTriggered = false;
        private @Mapper ComponentMapper<RemovableComponent> cmpMapper;

        private RemovableComponent cmp;

        public IssueTenEntityProcessingSystem() {
            super(Aspect.getAspectForOne(RemovableComponent.class));
        }

        @Override
        protected void process(Entity e) {

        }

        protected void inserted(Entity e) {
            insertedTriggered = true;
            assertTrue(cmpMapper.has(e));
            cmp = cmpMapper.get(e);
        }

        protected void removed(Entity e) {
            removedTriggered = true;
            // assertTrue(cmpMapper.has(e)); <-- User wants to be able to get the component from the removed entity
        }
    }


    @Test
    public void issueTen() throws Exception {

        World world = new World();
        IssueTenEntityProcessingSystem system = new IssueTenEntityProcessingSystem();
        world.setSystem(system);
        world.initialize();
        world.process();

        Entity e = world.createEntity();
        world.addEntity(e);
        world.process();

        assertFalse(system.insertedTriggered);
        assertFalse(system.removedTriggered);

        e.addComponent(new RemovableComponent());
        e.changedInWorld();
        world.process();

        assertTrue(system.insertedTriggered);
        assertFalse(system.removedTriggered);

        e.removeComponent(RemovableComponent.class);
        e.changedInWorld();
        world.process();

        assertTrue(system.removedTriggered);
    }
}