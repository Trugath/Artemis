package com.artemis.managers;

import com.artemis.Entity;
import com.artemis.World;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GroupManagerTest {

    World world;

    @Before
    public void setup() {
        world = new World();
        world.initialize();
    }

    @Test
    public void testEmpty() throws Exception {
        GroupManager gm = new GroupManager();
        gm.add(null, null);
        gm.remove(null, null);
        gm.removeFromAllGroups(null);
        assertTrue(gm.getEntities(null).isEmpty());
        assertTrue(gm.getGroups(null).isEmpty());
        assertFalse(gm.isInAnyGroup(null));
        assertFalse(gm.isInGroup(null, null));
    }

    @Test
    public void testUnknownGroupEntity() throws Exception {
        GroupManager gm = new GroupManager();
        gm.remove(world.createEntity(), "b");
        gm.removeFromAllGroups(world.createEntity());
        assertTrue(gm.getEntities("a").isEmpty());
    }

    @Test
    public void testBasicMembership() throws Exception {
        World world = new World();
        world.setManager(new GroupManager());
        world.initialize();

        GroupManager gm = world.getManager(GroupManager.class);

        Entity e = world.createEntity();
        world.process();

        assertFalse(gm.isInAnyGroup(e));
        assertFalse(gm.isInGroup(e, "a"));
        gm.add(e, "a");
        assertTrue(gm.isInAnyGroup(e));
        assertTrue(gm.isInGroup(e, "a"));
        gm.remove(e, "a");
        assertFalse(gm.isInAnyGroup(e));
        assertFalse(gm.isInGroup(e, "a"));
        gm.add(e, "a");
        assertTrue(gm.isInAnyGroup(e));
        assertTrue(gm.isInGroup(e, "a"));
        gm.removeFromAllGroups(e);
        assertFalse(gm.isInAnyGroup(e));
        assertFalse(gm.isInGroup(e, "a"));

    }
}