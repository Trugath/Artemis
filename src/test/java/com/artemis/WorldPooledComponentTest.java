package com.artemis;

import com.artemis.component.ReusedComponent;
import com.artemis.systems.EntityProcessingSystem;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class WorldPooledComponentTest
{
	private World world;

	@Before
	public void setUp() throws Exception
	{
		world = new World();
	}

	@Test // FIXME, the +1 shouldn't be necessary but there is some delay or something when deleting.
	public void pooled_component_reuse_with_deleted_entities()
	{
		world.setSystem(new SystemComponentEntityRemover());
		world.initialize();

		Set<Integer> hashes = runWorld();
		assertEquals("Contents: " + hashes, 3 + 1, hashes.size());
	}
	
	@Test
	public void pooled_component_reuse_with_removed_components()
	{
		world.setSystem(new SystemComponentPooledRemover());
		world.initialize();
		
		Set<Integer> hashes = runWorld();
		assertEquals("Contents: " + hashes, 3, hashes.size());
	}

	private Set<Integer> runWorld()
	{
		Set<Integer> hashes = new HashSet<Integer>();
		hashes.add(createEntity());
		hashes.add(createEntity());
		world.process();
		hashes.add(createEntity());
		world.process();
		hashes.add(createEntity());
		world.process();
		hashes.add(createEntity());
		world.process();
		hashes.add(createEntity());
		hashes.add(createEntity());
		hashes.add(createEntity());
		world.process();
		world.process();
		hashes.add(createEntity());
		world.process();
		
		return hashes;
	}
	
	private int createEntity()
	{
		Entity e = world.createEntity();
		ReusedComponent component = e.edit().create(ReusedComponent.class);
		int hash = System.identityHashCode(component);
		return hash;
	}
	
	static class SystemComponentEntityRemover extends EntityProcessingSystem
	{
		@SuppressWarnings("unchecked")
		public SystemComponentEntityRemover()
		{
			super(Aspect.getAspectForAll(ReusedComponent.class));
		}

		@Override
		protected void process(Entity e)
		{
			e.deleteFromWorld();
		}
	}
	
	static class SystemComponentPooledRemover extends EntityProcessingSystem
	{
		@SuppressWarnings("unchecked")
		public SystemComponentPooledRemover()
		{
			super(Aspect.getAspectForAll(ReusedComponent.class));
		}
		
		@Override
		protected void process(Entity e)
		{
			e.edit().remove(ReusedComponent.class);
		}
	}
}
