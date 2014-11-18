package com.artemis.managers;

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
        assertFalse(gm.inInGroup(null, null));
        gm.deleted(null);
    }
}