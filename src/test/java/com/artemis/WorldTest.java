package com.artemis;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class WorldTest {

    @Test
    public void testEmptyWorld() throws Exception {
        World world = new World();
        world.initialize();

        // should have an entity manager and a component manager by default
        EntityManager em = world.getEntityManager();
        assertTrue(em != null && world.getManager(EntityManager.class) == em);
        ComponentManager cm = world.getComponentManager();
        assertTrue(cm != null && world.getManager(ComponentManager.class) == cm);

        // delta should work sanely
        double delta = Math.random();
        world.setDelta(delta);
        assertTrue(world.getDelta() == delta);
    }

    @Test
    public void blankEntityTests() throws Exception {
        World world = new World();
        world.initialize();

        EntityManager em = world.getEntityManager();

        // able to create a blank entity
        Entity e = world.createEntity();
        assertTrue(e != null);

        assertTrue(world.getEntity(e.getId()) == null);
        assertTrue(!em.isActive(e.getId()));
        world.process();
        assertTrue(world.getEntity(e.getId()) == e);
        assertTrue(em.isActive(e.getId()));

        // entity changed message
        world.process();

        // delete entity
        e.deleteFromWorld();
        assertTrue(world.getEntity(e.getId()) == e);
        e.deleteFromWorld();
        assertTrue(world.getEntity(e.getId()) == e);
        world.process();
        assertTrue(world.getEntity(e.getId()) == null);
    }
}