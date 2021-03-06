package com.artemis.managers;

import com.artemis.Entity;
import com.artemis.Manager;
import com.artemis.utils.Bag;
import com.artemis.utils.ImmutableBag;

import java.util.HashMap;
import java.util.Map;


/**
 * You may sometimes want to specify to which player an entity belongs to.
 * <p>
 * An entity can only belong to a single player at a time.
 *
 * @author Arni Arent
 */
public class PlayerManager extends Manager {
    private final Map<Entity, String> playerByEntity;
    private final Map<String, Bag<Entity>> entitiesByPlayer;

    public PlayerManager() {
        playerByEntity = new HashMap<>();
        entitiesByPlayer = new HashMap<>();
    }

    public void setPlayer(Entity e, String player) {
        if (player == null || e == null)
            return;

        playerByEntity.put(e, player);
        Bag<Entity> entities = entitiesByPlayer.get(player);
        if (entities == null) {
            entities = new Bag<>();
            entitiesByPlayer.put(player, entities);
        }
        entities.add(e);
    }

    public ImmutableBag<Entity> getEntitiesOfPlayer(String player) {
        Bag<Entity> entities = entitiesByPlayer.get(player);
        if (entities == null) {
            entities = new Bag<>();
        }
        return entities;
    }

    public void removeFromPlayer(Entity e) {
        if (e == null)
            return;

        String player = playerByEntity.get(e);
        if (player != null) {
            Bag<Entity> entities = entitiesByPlayer.get(player);
            entities.remove(e);
            playerByEntity.remove(e);
        }
    }

    public String getPlayer(Entity e) {
        return playerByEntity.get(e);
    }

    @Override
    public void deleted(Entity e) {
        removeFromPlayer(e);
    }

}
