package com.artemis.managers;

import com.artemis.Entity;
import com.artemis.Manager;
import com.artemis.utils.Bag;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * If you need to tag any entity, use this. A typical usage would be to tag
 * entities such as "PLAYER", "BOSS" or something that is very unique.
 * 
 * @author Arni Arent
 *
 */
public class TagManager extends Manager {
	private final Map<String, Entity> entitiesByTag;
	private final Map<Entity, Bag<String>> tagsByEntity;

	public TagManager() {
		entitiesByTag = new HashMap<>();
		tagsByEntity = new HashMap<>();
	}

	public void register(String tag, Entity e) {
		if(tag == null || e == null)
			return;

		entitiesByTag.put(tag, e);
		if(!tagsByEntity.containsKey(e))
			tagsByEntity.put(e, new Bag<String>());
		tagsByEntity.get(e).add(tag);
	}

	public void unregister(String tag) {
		Entity e = entitiesByTag.remove(tag);
		Bag<String> bag = tagsByEntity.get(e);
		bag.remove(tag);
		if(bag.isEmpty())
			tagsByEntity.remove(e);
	}

	public boolean isRegistered(String tag) {
		return entitiesByTag.containsKey(tag);
	}

	public Entity getEntity(String tag) {
		return entitiesByTag.get(tag);
	}
	
	public Collection<String> getRegisteredTags() {
		return entitiesByTag.keySet();
	}
	
	@Override
	public void deleted(Entity e) {
		if(tagsByEntity.containsKey(e)) {
			for (String removedTag : tagsByEntity.remove(e)) {
				entitiesByTag.remove(removedTag);
			}
		}
	}

	@Override
	protected void initialize() {
	}

}
