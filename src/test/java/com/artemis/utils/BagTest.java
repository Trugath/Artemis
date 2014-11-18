package com.artemis.utils;

import com.google.common.collect.testing.SetTestSuiteBuilder;
import com.google.common.collect.testing.TestStringSetGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import junit.framework.TestSuite;

import java.util.Collections;
import java.util.Set;

/**
 * Use guava to test the Bag implementation (as a Set)
 */
public class BagTest {
    public static TestSuite suite() {
        return SetTestSuiteBuilder.using(new TestStringSetGenerator() {
            @Override
            protected Set<String> create(String[] elements) {
                Bag<String> result = new Bag<>();
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
                .createTestSuite();
    }
}
