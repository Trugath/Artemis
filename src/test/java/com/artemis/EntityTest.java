package com.artemis;

import com.artemis.managers.UuidEntityManager;
import com.artemis.utils.Bag;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.BitSet;
import java.util.UUID;

import static org.junit.Assert.*;

public class EntityTest {

    World world;
    Entity entity;

    @Before
    public void setup() {
        world = new World();
        entity = world.createEntity();
        world.addEntity(entity);
        world.process();
    }

    @After
    public void teardown() {
        world.deleteEntity(entity);
        world = null;
    }

    @Test
    public void testGetId() throws Exception {
        assertEquals(entity.getId(), 0);
    }

    @Test
    public void testGetComponentBits() throws Exception {
        BitSet bitSet = entity.getComponentBits();
        assertTrue(bitSet != null);
        assertTrue(bitSet.isEmpty());
    }

    @Test
    public void testGetSystemBits() throws Exception {
        BitSet bitSet = entity.getSystemBits();
        assertTrue(bitSet != null);
        assertTrue(bitSet.isEmpty());
    }

    @Test
    public void testReset() throws Exception {
        world.setManager(new UuidEntityManager());
        UUID uuid = entity.getUuid();
        entity.reset();
        assertNotEquals(entity.getUuid(), uuid);
    }

    @Test
    public void testToString() throws Exception {
        assertEquals(entity.toString(), "Entity[0]");
    }

    @Test
    public void testAddRemoveComponent() throws Exception {
        Component cmp = new Component() {};
        ComponentType type = ComponentType.getTypeFor(cmp.getClass());

        // add and remove by reference
        entity.addComponent(cmp);
        assertEquals(entity.getComponent(cmp.getClass()), cmp);
        entity.removeComponent(cmp);
        assertEquals(entity.getComponent(cmp.getClass()), null);

        // add and remove by type
        entity.addComponent(cmp, type);
        assertEquals(entity.getComponent(type), cmp);
        entity.removeComponent(type);
        assertEquals(entity.getComponent(type), null);

        // remove by class
        entity.addComponent(cmp);
        assertEquals(entity.getComponent(cmp.getClass()), cmp);
        entity.removeComponent(cmp.getClass());
        assertEquals(entity.getComponent(cmp.getClass()), null);
    }

    @Test
    public void testIsActive() throws Exception {
        assertTrue(entity.isActive());
        world.disable(entity);
        world.process();
        assertTrue(entity.isActive()); // disable does not stop an entity being active. you have to remove it
        world.deleteEntity(entity);
        world.process();
        assertFalse(entity.isActive());
    }

    @Test
    public void testIsEnabled() throws Exception {
        assertTrue(entity.isEnabled());
        world.disable(entity);
        world.process();
        assertFalse(entity.isEnabled());
    }

    @Test
    public void testGetComponents() throws Exception {
        Component cmp = new Component() {};

        Bag<Component> bag = entity.getComponents(new Bag<Component>());
        assertTrue(bag.isEmpty());
        entity.addComponent(cmp);
        bag = entity.getComponents(new Bag<Component>());
        assertFalse(bag.isEmpty());
    }

    @Test
    public void testAddToWorld() throws Exception {
        // remove using world
        world.deleteEntity(entity);
        world.process();

        // add using addToWorld
        assertFalse(entity.isActive());
        entity.addToWorld();
        world.process();
        assertTrue(entity.isActive());
    }

   class ChangeTriggerManager extends Manager {
       boolean triggered = false;
       @Override
        protected void initialize() { }
        @Override
        public void changed(Entity e) { triggered = true; }
    }

    @Test
    public void testChangedInWorld() throws Exception {
        ChangeTriggerManager manager = new ChangeTriggerManager();
        world.setManager(manager);
        world.initialize();

        assertFalse(manager.triggered);
        entity.changedInWorld();
        assertFalse(manager.triggered);
        world.process();
        assertTrue(manager.triggered);
    }

    @Test
    public void testDeleteFromWorld() throws Exception {
        entity.deleteFromWorld();
        world.process();
        assertFalse(entity.isActive());
    }

    @Test
    public void testDisableEnable() throws Exception {
        assertTrue(entity.isEnabled());
        entity.disable();
        world.process();
        assertFalse(entity.isEnabled());
        entity.enable();
        world.process();
        assertTrue(entity.isEnabled());
    }

    @Test
    public void testGetWorld() throws Exception {
        assertEquals(entity.getWorld(), world);
    }
}