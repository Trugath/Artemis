package com.artemis.systems;

import com.artemis.*;
import com.artemis.annotations.Mapper;
import org.junit.Test;

import static org.junit.Assert.*;

public class DelayedEntityProcessingSystemTest {
    class DelayComponentOne extends Component {
        double delay = 0.0;
    }

    class DelayComponentTwo extends Component {
        double delay = 1.0;
    }

    @Test
    public void dummySystemTest() throws Exception {
        DelayedEntityProcessingSystem system = new DelayedEntityProcessingSystem(Aspect.getEmpty()) {
            @Override
            protected double getRemainingDelay(Entity e) {
                return 0;
            }
            @Override
            protected void processDelta(Entity e, double accumulatedDelta) {
            }
            @Override
            protected void processExpired(Entity e) {
            }
        };
        system.process();
        system.added(null);
        system.changed(null);
        system.deleted(null);
        system.disabled(null);
        system.enabled(null);
        assertTrue(system.getActives().isEmpty());
        assertEquals(system.getRemainingTimeUntilProcessing(), 0.0, 0.01);
    }

    @Test
    public void singleAspectEmptySystemTest() throws Exception {
        DelayedEntityProcessingSystem system = new DelayedEntityProcessingSystem(Aspect.getAspectForAll(DelayComponentOne.class)) {
            @Override
            protected double getRemainingDelay(Entity e) {
                return 0;
            }
            @Override
            protected void processDelta(Entity e, double accumulatedDelta) {
            }
            @Override
            protected void processExpired(Entity e) {
            }
        };

        system.process();
        system.added(null);
        system.changed(null);
        system.deleted(null);
        system.disabled(null);
        system.enabled(null);
        assertTrue(system.getActives().isEmpty());
        assertEquals(system.getRemainingTimeUntilProcessing(), 0.0, 0.01);
    }

    @Test
    public void singleAspectSystemTest() throws Exception {

        World world = new World();
        DelayedEntityProcessingSystem system = new DelayedEntityProcessingSystem(Aspect.getAspectForOne(DelayComponentOne.class, DelayComponentTwo.class)) {
            @Mapper ComponentMapper<DelayComponentOne> oneMapper;
            @Mapper ComponentMapper<DelayComponentTwo> twoMapper;

            @Override
            protected double getRemainingDelay(Entity e) {
                double result = Double.MAX_VALUE;
                if(oneMapper.has(e))
                    result = Math.min(result, oneMapper.get(e).delay);
                if(twoMapper.has(e))
                    result = Math.min(result, twoMapper.get(e).delay);
                return result;
            }

            @Override
            protected void processDelta(Entity e, double accumulatedDelta) {
                if(oneMapper.has(e))
                    oneMapper.get(e).delay -= accumulatedDelta;
                if(twoMapper.has(e))
                    twoMapper.get(e).delay -= accumulatedDelta;
            }

            @Override
            protected void processExpired(Entity e) {
                double result = Double.MAX_VALUE;
                if(oneMapper.has(e) && oneMapper.get(e).delay <= 0.0)
                    result = Math.min(result, oneMapper.get(e).delay += 2.0);
                if(twoMapper.has(e) && twoMapper.get(e).delay <= 0.0)
                    result = Math.min(result, twoMapper.get(e).delay += 2.0);
                offerDelay(result);
            }
        };
        world.setSystem(system);
        world.initialize();

        ComponentMapper<DelayComponentOne> oneMapper = ComponentMapper.getFor(DelayComponentOne.class, world);
        ComponentMapper<DelayComponentTwo> twoMapper = ComponentMapper.getFor(DelayComponentTwo.class, world);

        // entity not captured by the system
        Entity e1 = world.createEntity();
        world.addEntity(e1);

        // entity captured by the system
        Entity e2 = world.createEntity();
        e2.addComponent(new DelayComponentOne());
        world.addEntity(e2);

        // entity captured by the system
        Entity e3 = world.createEntity();
        e3.addComponent(new DelayComponentTwo());
        world.addEntity(e3);

        // entity captured by the system
        Entity e4 = world.createEntity();
        e4.addComponent(new DelayComponentOne());
        e4.addComponent(new DelayComponentTwo());
        world.addEntity(e4);

        world.process();

        assertFalse(system.getActives().contains(e1));
        assertTrue(system.getActives().contains(e2));
        assertTrue(system.getActives().contains(e3));
        assertTrue(system.getActives().contains(e4));

        for( Entity e : system.getActives() ) {
            if(oneMapper.has(e))assertEquals(2.0, oneMapper.get(e).delay, 0.01);
            if(twoMapper.has(e))assertEquals(1.0, twoMapper.get(e).delay, 0.01);
        }

        assertEquals(system.getRemainingTimeUntilProcessing(), 1.0, 0.01);

        world.setDelta(0.5);
        world.process();

        for( Entity e : system.getActives() ) {
            if(oneMapper.has(e))assertEquals(2.0, oneMapper.get(e).delay, 0.01);
            if(twoMapper.has(e))assertEquals(1.0, twoMapper.get(e).delay, 0.01);
        }

        assertEquals(system.getRemainingTimeUntilProcessing(), 0.5, 0.01);

        world.setDelta(0.5);
        world.process();

        for( Entity e : system.getActives() ) {
            if(oneMapper.has(e))assertEquals(1.0, oneMapper.get(e).delay, 0.01);
            if(twoMapper.has(e))assertEquals(2.0, twoMapper.get(e).delay, 0.01);
        }

        assertEquals(system.getRemainingTimeUntilProcessing(), 1.0, 0.01);

        world.setDelta(2.5);
        world.process();

        for( Entity e : system.getActives() ) {
            if(oneMapper.has(e))assertEquals(0.5, oneMapper.get(e).delay, 0.01);
            if(twoMapper.has(e))assertEquals(1.5, twoMapper.get(e).delay, 0.01);
        }

        assertEquals(system.getRemainingTimeUntilProcessing(), 0.5, 0.01);

        world.deleteEntity(e1);
        world.deleteEntity(e2);
        world.deleteEntity(e3);
        world.deleteEntity(e4);
        world.process();

        assertFalse(system.getActives().contains(e1));
        assertFalse(system.getActives().contains(e2));
        assertFalse(system.getActives().contains(e3));
        assertFalse(system.getActives().contains(e4));
    }
}