package com.artemis.managers;

import com.artemis.Entity;
import com.artemis.World;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class TagManagerTest {

    World world;
    TagManager tagManager;

    @Before
    public void setup() {
        world = new World();
        tagManager = new TagManager();
        world.setManager(tagManager);
        world.initialize();
    }

    @After
    public void tearDown() {
        tagManager = null;
        world = null;
    }

    @Test
    public void testRegister() throws Exception {

        // nothing should die
        tagManager.register(null, null);
        assertFalse(tagManager.isRegistered(null));
        tagManager.register("", null);
        assertFalse(tagManager.isRegistered(""));
        tagManager.register("a", null);
        assertFalse(tagManager.isRegistered("a"));

        Entity e = world.createEntity();
        tagManager.register(null, e); // does nothing
        assertFalse(tagManager.isRegistered(null));
        tagManager.register("a", e);
        assertTrue(tagManager.isRegistered("a"));
        assertTrue(tagManager.getRegisteredTags().contains("a"));
        assertEquals(e, tagManager.getEntity("a"));
    }

    @Test
    public void testUnregister() throws Exception {

        tagManager.unregister(null);

        Entity e = world.createEntity();
        tagManager.register("a", e);
        assertTrue(tagManager.isRegistered("a"));
        assertEquals(e, tagManager.getEntity("a"));
        tagManager.unregister("a");
        assertFalse(tagManager.isRegistered("a"));
        assertNotEquals(e, tagManager.getEntity("a"));
    }

    @Test
    public void testIsRegistered() throws Exception {

        assertFalse(tagManager.isRegistered(null));

        Entity e = world.createEntity();
        assertFalse(tagManager.isRegistered("a"));
        tagManager.register("a", e);
        assertTrue(tagManager.isRegistered("a"));
    }

    @Test
    public void testGetEntity() throws Exception {

        assertEquals(null, tagManager.getEntity(null));
        assertEquals(null, tagManager.getEntity(""));
        assertEquals(null, tagManager.getEntity("a"));

        Entity e = world.createEntity();
        assertFalse(tagManager.isRegistered("a"));
        assertNotEquals(e, tagManager.getEntity("a"));
        tagManager.register("a", e);
        assertEquals(e, tagManager.getEntity("a"));
    }

    @Test
    public void testGetRegisteredTags() throws Exception {
        assertTrue(tagManager.getRegisteredTags().isEmpty());
        tagManager.register("a", world.createEntity());
        assertTrue(tagManager.getRegisteredTags().contains("a"));
        assertTrue(tagManager.getRegisteredTags().containsAll(new ArrayList<String>(Arrays.asList("a"))));
        tagManager.register("b", world.createEntity());
        assertTrue(tagManager.getRegisteredTags().containsAll(new ArrayList<String>(Arrays.asList("a", "b"))));
        tagManager.register("c", world.createEntity());
        assertTrue(tagManager.getRegisteredTags().containsAll(new ArrayList<String>(Arrays.asList("a", "b", "c"))));
    }

    @Test
    public void oneTagsForEntityDeleted() throws Exception {
        Entity e = world.createEntity();
        world.process();

        tagManager.register("a", e);
        assertTrue(tagManager.isRegistered("a"));

        assertEquals(1, tagManager.getRegisteredTags().size());
        assertTrue(tagManager.getRegisteredTags().contains("a"));
        assertEquals(e, tagManager.getEntity("a"));

        e.deleteFromWorld();
        world.process();

        assertFalse(tagManager.isRegistered("a"));
        assertEquals(0, tagManager.getRegisteredTags().size());
        assertNotEquals(e, tagManager.getEntity("a"));
    }

    @Test
    public void twoTagsForEntityDeleted() throws Exception {
        Entity e = world.createEntity();
        world.process();

        tagManager.register("a", e);
        tagManager.register("b", e);

        assertTrue(tagManager.isRegistered("a"));
        assertTrue(tagManager.isRegistered("b"));

        assertEquals(2, tagManager.getRegisteredTags().size());
        assertTrue(tagManager.getRegisteredTags().containsAll(new ArrayList<String>(Arrays.asList("a", "b"))));

        assertEquals(e, tagManager.getEntity("a"));
        assertEquals(e, tagManager.getEntity("b"));

        e.deleteFromWorld();
        world.process();

        assertFalse(tagManager.isRegistered("a"));
        assertFalse(tagManager.isRegistered("b"));

        assertEquals(0, tagManager.getRegisteredTags().size());

        assertNotEquals(e, tagManager.getEntity("a"));
        assertNotEquals(e, tagManager.getEntity("b"));
    }

    @Test
    public void addRemoveExtraTag() throws Exception {
        Entity e = world.createEntity();
        world.process();

        tagManager.register("a", e);
        assertTrue(tagManager.isRegistered("a"));

        assertEquals(1, tagManager.getRegisteredTags().size());
        assertTrue(tagManager.getRegisteredTags().contains("a"));
        assertEquals(e, tagManager.getEntity("a"));

        tagManager.register("b", e);
        assertTrue(tagManager.isRegistered("a"));
        assertTrue(tagManager.isRegistered("b"));

        assertEquals(2, tagManager.getRegisteredTags().size());
        assertTrue(tagManager.getRegisteredTags().containsAll(new ArrayList<String>(Arrays.asList("a", "b"))));

        assertEquals(e, tagManager.getEntity("a"));
        assertEquals(e, tagManager.getEntity("b"));

        tagManager.unregister("b");
        assertTrue(tagManager.isRegistered("a"));
        assertFalse(tagManager.isRegistered("b"));

        assertEquals(1, tagManager.getRegisteredTags().size());
        assertTrue(tagManager.getRegisteredTags().contains("a"));
        assertEquals(e, tagManager.getEntity("a"));
        assertNotEquals(e, tagManager.getEntity("b"));

        e.deleteFromWorld();
        world.process();

        assertFalse(tagManager.isRegistered("a"));
        assertEquals(0, tagManager.getRegisteredTags().size());
        assertNotEquals(e, tagManager.getEntity("a"));
    }
}