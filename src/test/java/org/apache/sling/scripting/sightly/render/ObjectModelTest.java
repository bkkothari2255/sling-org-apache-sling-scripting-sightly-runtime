/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.scripting.sightly.render;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.sling.scripting.sightly.render.testobjects.Person;
import org.apache.sling.scripting.sightly.render.testobjects.TestEnum;
import org.apache.sling.scripting.sightly.render.testobjects.TestEnum2;
import org.apache.sling.scripting.sightly.render.testobjects.internal.AdultFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ObjectModelTest {

    private static final String COMMA_DELIMITED_LIST = "1,2,3";
    private static final String TEST_URI = "http://localhost/test";
    private static final String STRING = "string";
    private static final String INT = "int";
    private static final String INTEGER = "integer";

    private static final Integer[] TEST_ARRAY = new Integer[] {1, 2, 3};
    private static final int[] TEST_PRIMITIVE_ARRAY = new int[] {1, 2, 3};

    private static final List<Integer> TEST_LIST = Collections.unmodifiableList(Arrays.asList(TEST_ARRAY));

    @Test
    void testToBoolean() {
        Map<String, String> populatedMap = new HashMap<>();
        populatedMap.put("one", "entry");

        Object[] falsyInputs = {
            null,
            0,
            "",
            false,
            Boolean.FALSE,
            new int[0],
            new Integer[] {},
            Collections.emptyList(),
            Collections.emptyMap(),
            new HashMap<>(),
            Collections.emptyList().iterator(),
            new Bag<>(new Integer[] {}),
            Optional.empty(),
            Optional.of(""),
            Optional.of(false),
            Optional.ofNullable(null)
        };

        Object[] truthyInputs = {
            123456,
            "FalSe",
            "false",
            "FALSE",
            "true",
            "TRUE",
            "TrUE",
            TEST_ARRAY,
            TEST_PRIMITIVE_ARRAY,
            TEST_LIST,
            TEST_LIST.iterator(),
            new Bag<>(TEST_ARRAY),
            new Date(),
            new Object(),
            populatedMap,
            Optional.of(true),
            Optional.of("pass"),
            Optional.of(1)
        };

        for (Object input : falsyInputs) {
            assertFalse(ObjectModel.toBoolean(input), "Should be false for: " + input);
        }
        for (Object input : truthyInputs) {
            assertTrue(ObjectModel.toBoolean(input), "Should be true for: " + input);
        }
    }

    @Test
    void testToNumber() {
        assertEquals(1, ObjectModel.toNumber(1));
        assertEquals(1, ObjectModel.toNumber("1"));
        assertNull(ObjectModel.toNumber(null));
        assertNull(ObjectModel.toNumber("1-2"));

        assertNull(ObjectModel.toNumber(Optional.empty()));
        assertNull(ObjectModel.toNumber(Optional.of(false)));
        assertNull(ObjectModel.toNumber(Optional.ofNullable(null)));
        assertNull(ObjectModel.toNumber(Optional.of(true)));
        assertNull(ObjectModel.toNumber(Optional.of("pass")));
        assertEquals(1, ObjectModel.toNumber(Optional.of(1)));
        assertEquals(1, ObjectModel.toNumber(Optional.of("1")));
    }

    @Test
    void testToString() throws URISyntaxException {
        assertEquals("", ObjectModel.toString(null));
        assertEquals("1", ObjectModel.toString("1"));
        assertEquals("1", ObjectModel.toString(1));
        assertEquals("CONSTANT", ObjectModel.toString(TestEnum.CONSTANT));
        assertEquals(COMMA_DELIMITED_LIST, ObjectModel.toString(TEST_LIST));
        assertEquals(COMMA_DELIMITED_LIST, ObjectModel.toString(TEST_ARRAY));
        assertEquals(COMMA_DELIMITED_LIST, ObjectModel.toString(TEST_PRIMITIVE_ARRAY));
        assertEquals(TEST_URI, ObjectModel.toString(new URI(TEST_URI)));

        assertEquals("", ObjectModel.toString(Optional.empty()));
        assertEquals("false", ObjectModel.toString(Optional.of(false)));
        assertEquals("", ObjectModel.toString(Optional.ofNullable(null)));
        assertEquals("true", ObjectModel.toString(Optional.of(true)));
        assertEquals("pass", ObjectModel.toString(Optional.of("pass")));
        assertEquals("1", ObjectModel.toString(Optional.of(1)));
        assertEquals("1", ObjectModel.toString(Optional.of("1")));
    }

    @Test
    void testToCollection() {
        assertTrue(ObjectModel.toCollection(null).isEmpty());
        StringBuilder sb = new StringBuilder();
        assertEquals(Collections.singletonList(sb), ObjectModel.toCollection(sb));
        Map<String, Integer> map = new HashMap<>();
        map.put("one", 1);
        map.put("two", 2);
        assertEquals(TEST_LIST, ObjectModel.toCollection(TEST_ARRAY));
        assertEquals(TEST_LIST, ObjectModel.toCollection(TEST_PRIMITIVE_ARRAY));
        assertEquals(TEST_LIST, ObjectModel.toCollection(TEST_LIST));
        Collection<Object> mapCollection = ObjectModel.toCollection(map);
        assertTrue(mapCollection.containsAll(map.keySet()) && mapCollection.size() == map.size());
        ArrayList<Integer> arrayList = new ArrayList<>(TEST_LIST);
        assertEquals(TEST_LIST, ObjectModel.toCollection(arrayList));
        assertEquals(TEST_LIST, ObjectModel.toCollection(TEST_LIST.iterator()));
        assertEquals(TEST_LIST, ObjectModel.toCollection(new Bag<>(TEST_ARRAY)));
        String stringObject = "test";
        Integer numberObject = 1;
        Collection<Object> stringCollection = ObjectModel.toCollection(stringObject);
        assertTrue(stringCollection.size() == 1 && stringCollection.contains(stringObject));
        Collection<Object> numberCollection = ObjectModel.toCollection(numberObject);
        assertTrue(numberCollection.size() == 1 && numberCollection.contains(numberObject));

        List<Object> emptyList = Collections.emptyList();
        assertEquals(emptyList, ObjectModel.toCollection(Optional.empty()));
        assertEquals(emptyList, ObjectModel.toCollection(Optional.of(Arrays.asList())));
        List<Integer> list = Arrays.asList(1, 2, 3);
        assertEquals(list, ObjectModel.toCollection(Optional.of(list)));
    }

    @Test
    void testCollectionToString() {
        assertEquals("", ObjectModel.collectionToString(null));
        assertEquals(COMMA_DELIMITED_LIST, ObjectModel.collectionToString(TEST_LIST));
    }

    @Test
    void testFromIterator() {
        assertTrue(ObjectModel.fromIterator(null).isEmpty());
        assertEquals(TEST_LIST, ObjectModel.fromIterator((Iterator) TEST_LIST.iterator()));
    }

    @Test
    void testResolveProperty() {
        Person johnDoe = AdultFactory.createAdult("John", "Doe");
        OptionalTest optionalTest = new OptionalTest();

        Map<String, Integer> map = new HashMap<>();
        map.put("one", 1);
        map.put("two", 2);

        Map<Integer, String> stringMap = new HashMap<>();
        stringMap.put(1, "one");
        stringMap.put(2, "two");

        assertAll(
                "Null and Boundary Checks",
                () -> assertNull(ObjectModel.resolveProperty(null, 0)),
                () -> assertNull(ObjectModel.resolveProperty(this, null)),
                () -> assertNull(ObjectModel.resolveProperty(null, null)),
                () -> assertNull(ObjectModel.resolveProperty(TEST_ARRAY, 3)),
                () -> assertNull(ObjectModel.resolveProperty(TEST_ARRAY, -1)),
                () -> assertNull(ObjectModel.resolveProperty(TEST_LIST, 3)),
                () -> assertNull(ObjectModel.resolveProperty(TEST_LIST, -1)));

        assertAll(
                "Indexed Access",
                () -> assertEquals(2, ObjectModel.resolveProperty(TEST_ARRAY, 1)),
                () -> assertEquals(2, ObjectModel.resolveProperty(TEST_LIST, 1)),
                () -> assertEquals(
                        3,
                        ObjectModel.resolveProperty(TEST_ARRAY, "length"),
                        "Expected to be able to access an array's length property."));

        assertAll(
                "Map Access",
                () -> assertEquals(1, ObjectModel.resolveProperty(map, "one")),
                () -> assertNull(ObjectModel.resolveProperty(map, null)),
                () -> assertNull(ObjectModel.resolveProperty(map, "")),
                () -> assertEquals("one", ObjectModel.resolveProperty(stringMap, 1)),
                () -> assertEquals("two", ObjectModel.resolveProperty(stringMap, 2)));

        assertAll(
                "POJO Properties",
                () -> assertEquals(
                        1L,
                        ObjectModel.resolveProperty(johnDoe, "CONSTANT"),
                        "Expected to be able to access public static final constants."),
                () -> assertNull(
                        ObjectModel.resolveProperty(johnDoe, "TODAY"),
                        "Did not expect to be able to access public fields from package protected classes."),
                () -> assertNotNull(
                        ObjectModel.resolveProperty(johnDoe, "lastName"),
                        "Expected not null result for invocation of interface method on implementation class."));

        assertAll(
                "Negative POJO checks",
                () -> assertNull(
                        ObjectModel.resolveProperty(johnDoe, "fullName"),
                        "Expected null result for public method available on implementation but not exposed by interface."),
                () -> assertNull(
                        ObjectModel.resolveProperty(johnDoe, "nomethod"),
                        "Expected null result for inexistent method."));

        assertEquals(0, ObjectModel.resolveProperty(Collections.emptyList(), "size"));

        assertAll(
                "Optional Support",
                () -> assertEquals(Optional.of(STRING), ObjectModel.resolveProperty(optionalTest, STRING)),
                () -> assertEquals(Optional.of(1), ObjectModel.resolveProperty(optionalTest, INT)),
                () -> assertEquals(Optional.of(1), ObjectModel.resolveProperty(Optional.of(optionalTest), INT)),
                () -> assertEquals(Optional.of(Integer.valueOf(1)), ObjectModel.resolveProperty(optionalTest, INTEGER)),
                () -> assertEquals(
                        Optional.of(Integer.valueOf(1)),
                        ObjectModel.resolveProperty(Optional.of(optionalTest), INTEGER)),
                () -> assertNull(ObjectModel.resolveProperty(Optional.empty(), INTEGER)));
    }

    /**
     * Verify that values of an enumeration can be resolved
     * by their name
     */
    @Test
    void testResolvePropertyFromEnum() {
        assertEquals(TestEnum2.ONE, ObjectModel.resolveProperty(TestEnum2.class, "ONE"));
        assertEquals(TestEnum2.TWO, ObjectModel.resolveProperty(TestEnum2.class, "TWO"));
        assertNull(ObjectModel.resolveProperty(TestEnum.class, "INVALID"));

        assertEquals(
                TestEnum2.STR_CONSTANT,
                ObjectModel.resolveProperty(TestEnum2.class, "STR_CONSTANT"),
                "Expected to be able to access public static final constants.");
    }

    /**
     * Verify that values of an static method of an enumeration can be invoked
     */
    @Test
    void testResolveMethodFromEnum() {
        Object value = ObjectModel.resolveProperty(TestEnum2.class, "values");
        assertNotNull(value);
        assertTrue(value.getClass().isArray());
        assertEquals(TestEnum2.class, value.getClass().getComponentType());
        assertArrayEquals(TestEnum2.values(), (TestEnum2[]) value);

        assertEquals("value from static method", ObjectModel.resolveProperty(TestEnum2.class, "someStaticMethod1"));
    }

    @Test
    void testGetIndex() {
        assertNull(ObjectModel.getIndex(null, 0));
        assertEquals(2, ObjectModel.getIndex(TEST_ARRAY, 1));
        assertNull(ObjectModel.getIndex(TEST_ARRAY, 3));
        assertNull(ObjectModel.getIndex(TEST_ARRAY, -1));
        assertEquals(2, ObjectModel.getIndex(TEST_LIST, 1));
        assertNull(ObjectModel.getIndex(TEST_LIST, 3));
        assertNull(ObjectModel.getIndex(TEST_LIST, -1));
        Map<Integer, String> stringMap = new HashMap<>();
        stringMap.put(1, "one");
        stringMap.put(2, "two");
        assertNull(ObjectModel.getIndex(stringMap, 1));
        assertNull(ObjectModel.getIndex(stringMap, 2));
    }

    /**
     * Verify that values of an enumeration can be resolved
     * by their ordinal value
     */
    @Test
    void testGetIndexFromEnum() {
        assertEquals(TestEnum2.ONE, ObjectModel.getIndex(TestEnum2.class, 0));
        Object two = ObjectModel.getIndex(TestEnum2.class, 1);
        assertEquals(TestEnum2.TWO, two);
        assertEquals(TestEnum2.TWO.ordinal(), ((Enum<?>) two).ordinal());
        assertNull(ObjectModel.getIndex(TestEnum.class, 100));
    }

    @Test
    void testClassBasedMethodsForNulls() {
        assertNull(ObjectModel.getField(null, null));
        assertNull(ObjectModel.getField("", null));
        assertNull(ObjectModel.getField(this, ""));
        assertNull(ObjectModel.findBeanMethod(null, null));
        assertNull(ObjectModel.findBeanMethod(this.getClass(), null));
        assertNull(ObjectModel.findBeanMethod(this.getClass(), ""));
        assertNull(ObjectModel.invokeBeanMethod(null, null));
        assertNull(ObjectModel.invokeBeanMethod(this, null));
        assertNull(ObjectModel.invokeBeanMethod(this, ""));
    }

    private class Bag<T> implements Iterable<T> {

        private T[] backingArray;

        public Bag(T[] array) {
            this.backingArray = array;
        }

        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>() {

                int index = 0;

                @Override
                public boolean hasNext() {
                    return index < backingArray.length;
                }

                @Override
                public T next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return backingArray[index++];
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    public class OptionalTest {
        public Optional<String> getEmpty() {
            return Optional.empty();
        }

        public Optional<String> getString() {
            return Optional.of(STRING);
        }

        public Optional<Integer> getInt() {
            return Optional.of(1);
        }

        public Optional<Integer> getInteger() {
            return Optional.of(Integer.valueOf(1));
        }
    }
}
