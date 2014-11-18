package com.artemis.managers;

import com.artemis.Entity;
import com.artemis.World;
import com.artemis.utils.ImmutableBag;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class PlayerManagerTest {
    @Test
    public void testEmpty() throws Exception {
        PlayerManager pm = new PlayerManager();
        pm.setPlayer(null, null);
        pm.setPlayer(null, "");
        assertTrue(pm.getEntitiesOfPlayer(null).isEmpty());
        pm.removeFromPlayer(null);
        assertTrue(pm.getPlayer(null) == null);
        pm.deleted(null);
    }

    @Test
    public void testUnknownPlayer() throws Exception {
        PlayerManager pm = new PlayerManager();
        assertTrue(pm.getEntitiesOfPlayer("a").isEmpty());
    }

    @Test
    public void testUnknownEntity() throws Exception {
        World world = new World();
        PlayerManager pm = new PlayerManager();
        world.setManager(pm);
        world.initialize();

        pm.removeFromPlayer(world.createEntity());
        assertTrue(pm.getPlayer(world.createEntity()) == null);
    }

    @Test
    public void testSinglePlayer() throws Exception {
        World world = new World();
        PlayerManager pm = new PlayerManager();
        world.setManager(pm);
        world.initialize();

        Entity e1 = world.createEntity();
        world.addEntity(e1);

        Entity e2 = world.createEntity();
        world.addEntity(e2);

        world.process();

        pm.setPlayer(e1, "a");
        ImmutableBag<Entity> ent = pm.getEntitiesOfPlayer("a");
        assertTrue(ent.size() == 1);
        assertTrue(ent.contains(e1));
        assert(pm.getPlayer(e1).equals("a"));

        pm.setPlayer(e2, "a");
        ent = pm.getEntitiesOfPlayer("a");
        assertTrue(ent.size() == 2);
        assertTrue(ent.contains(e1));
        assertTrue(ent.contains(e2));
        assert(pm.getPlayer(e2).equals("a"));

        pm.removeFromPlayer(e2);
        pm.removeFromPlayer(e1);
        ent = pm.getEntitiesOfPlayer("a");
        assertTrue(ent.isEmpty());
        assertTrue(!ent.contains(e1));
        assert(pm.getPlayer(e1) == null);
    }
}