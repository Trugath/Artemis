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

        /**
         * This is called in two situations, once when the complete entity with components is being deleted
         * and once when the the final component is removed that this manager is interested in
         *
         * Without keeping all components around after they are removed the user is not able to consistently
         * query for the component that they want
         *
         * An alternative for the user would be to add an onAdded and onRemoved method into the abstract
         * base class for Component.
         *
         */
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