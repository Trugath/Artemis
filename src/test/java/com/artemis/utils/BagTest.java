package com.artemis.utils;

import com.google.common.collect.testing.SetTestSuiteBuilder;
import com.google.common.collect.testing.TestStringSetGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Use guava to test the Bag implementation (as a Set)
 */
public class BagTest {

    @Test
    public void runTestSuite() {
        new JUnitCore()
            .run(SetTestSuiteBuilder.using(new TestStringSetGenerator() {
                        @Override
                        protected Set<String> create(String[] elements) {
                            Bag<String> result = new Bag<>(0);
                            Collections.addAll(result, elements);
                            return result;
                        }
                    })
                            .named("BagTest")
                            .withFeatures(CollectionSize.ANY,
                                    CollectionFeature.ALLOWS_NULL_QUERIES,
                                    CollectionFeature.SUPPORTS_ADD,
                                    CollectionFeature.SUPPORTS_REMOVE,
                                    CollectionFeature.SUPPORTS_ITERATOR_REMOVE,
                                    CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION)
                            .createTestSuite()
            );
    }

    @Test
    public void testRemoveLast() {
        Bag<String> bag = new Bag<>();
        bag.add("a");
        bag.add("b");
        bag.add("c");
        assertEquals(bag.removeLast(), "c");
        bag.add("d");
        assertEquals(bag.removeLast(), "d");
        assertEquals(bag.removeLast(), "b");
        assertEquals(bag.removeLast(), "a");
        assertEquals(bag.removeLast(), null);
    }

    @Test
    public void testEnsureCapacity() {
        Bag<String> bag = new Bag<String>(0);
        assertEquals(bag.getCapacity(), 0);
        bag.ensureCapacity(16);
        assertTrue(bag.getCapacity() >= 16);
    }

    @Test
    public void testSet() {
        Bag<String> bag = new Bag<String>(0);
        assertEquals(bag.getCapacity(), 0);
        bag.set(64, "a");
        assertTrue(bag.getCapacity() >= 64);
    }

    @Test
    public void testGrow() {
        Bag<String> bag = new Bag<String>(0);
        assertEquals(bag.getCapacity(), 0);
        for(int i = 0; i < 64; ++i) {
            bag.add("" + (byte)(i + 64));
        }
        assertTrue(bag.getCapacity() >= 64);
    }

    // https://code.google.com/p/artemis-framework/issues/detail?id=6
    @Test
    public void issueSix() {
        Bag<String> b = new Bag<String>();
        b.set(5, "a");
        b.remove(5);
        assertTrue(b.isEmpty());
    }

    @Test
    public void issueSeven() {
        // Fill a bag with 10 elements
        Bag<String> bag = new Bag<String>(0);
        for(int i = 0; i < 64; ++i) {
            bag.add("" + (byte)(i + 64));
        }

        // Call bag.set(4, newitem);
        bag.set(4, "newItem");

        // Check bag.size();
        assertTrue(bag.size() >= 64);
    }
}
