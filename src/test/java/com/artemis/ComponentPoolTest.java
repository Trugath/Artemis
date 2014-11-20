package com.artemis;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ComponentPoolTest
{
	@SuppressWarnings("static-method")
	@Test
	public void reuse_pooled_components() throws Exception
	{
		ComponentPool pool = new ComponentPool();
		SimplePooled c1 = pool.obtain(SimplePooled.class);
		SimplePooled c2 = pool.obtain(SimplePooled.class);
		pool.free(c1);
		SimplePooled c1b = pool.obtain(SimplePooled.class);
		
		assertTrue(c1 != c2);
		assertTrue(c1 == c1b);
	}
	
	public static final class SimplePooled extends PooledComponent {
		
		@Override
		public void reset() {}
	}
}
