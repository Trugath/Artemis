package com.artemis.managers;

import com.artemis.Entity;
import com.artemis.World;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GroupManagerTest {

    @Test
    public void testEmpty() throws Exception {
        GroupManager gm = new GroupManager();
        gm.initialize();
        gm.add(null, null);
        gm.remove(null, null);
        gm.removeFromAllGroups(null);
        assertTrue(gm.getEntities(null).isEmpty());
        assertTrue(gm.getGroups(null).isEmpty());
        assertFalse(gm.isInAnyGroup(null));
        assertFalse(gm.isInGroup(null, null));
        gm.deleted(null);
    }

    @Test
    public void testUnknownGroup() throws Exception {
        GroupManager gm = new GroupManager();
        assertTrue(gm.getEntities("a").isEmpty());
    }

    @Test
    public void testBasicMembership() throws Exception {
        World world = new World();
        world.setManager(new GroupManager());
        world.initialize();

        GroupManager gm = world.getManager(GroupManager.class);

        Entity e = world.createEntity();
        e.addToWorld();
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