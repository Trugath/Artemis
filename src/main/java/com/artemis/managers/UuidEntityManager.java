package com.artemis.managers;

import com.artemis.Entity;
import com.artemis.Manager;
import com.artemis.utils.Bag;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UuidEntityManager extends Manager {
	private final Map<UUID, Entity> uuidToEntity;
	private final Bag<UUID> entityToUuid;

	public UuidEntityManager() {
		this.uuidToEntity = new HashMap<>();
		this.entityToUuid = new Bag<>();
	}

	@Override
	public void added(Entity e) {
		setUuid(e, e.getUuid());
	}

	@Override
	public void deleted(Entity e) {
		UUID uuid = entityToUuid.get(e.getId());
		if (uuid == null)
			return;
		
		uuidToEntity.remove(uuid);
		entityToUuid.set(e.getId(), null);
	}
	
	public void updatedUuid(Entity e, UUID newUuid) {
		UUID oldUuid = entityToUuid.get(e.getId());
		if (oldUuid != null)
			uuidToEntity.remove(oldUuid);
		
		setUuid(e, newUuid);
	}
	
	public Entity getEntity(UUID uuid) {
		return uuidToEntity.get(uuid);
	}

	public UUID getUuid(Entity e) {
		UUID uuid = entityToUuid.get(e.getId());
		if (uuid == null) {
			uuid = UUID.randomUUID();
			setUuid(e, uuid);
		}
		
		return uuid;
	}
	
	public void setUuid(Entity e, UUID newUuid) {
		UUID oldUuid = entityToUuid.get(e.getId());
		if (oldUuid != null)
			uuidToEntity.remove(oldUuid);
		
		uuidToEntity.put(newUuid, e);
		entityToUuid.set(e.getId(), newUuid);
	}
}
