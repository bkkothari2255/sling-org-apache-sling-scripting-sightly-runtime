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

import java.time.Instant;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.sling.scripting.sightly.Record;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbstractRuntimeObjectModelTest {

    private AbstractRuntimeObjectModel runtimeObjectModel = new AbstractRuntimeObjectModel() {};

    @Test
    void testResolveProperty() {
        assertNull(runtimeObjectModel.resolveProperty(null, null));
        assertNull(runtimeObjectModel.resolveProperty(this, null));
        assertNull(runtimeObjectModel.resolveProperty(this, ""));
        assertEquals(0, runtimeObjectModel.resolveProperty(Collections.emptyList(), "size"));
        assertNull(runtimeObjectModel.resolveProperty(null, null));
        int[] ints = new int[] {1, 2, 3};
        assertEquals(ints.length, runtimeObjectModel.resolveProperty(ints, "length"));
        Integer[] testArray = new Integer[] {1, 2, 3};
        assertEquals(testArray.length, runtimeObjectModel.resolveProperty(testArray, "length"));
        assertEquals(2, runtimeObjectModel.resolveProperty(testArray, 1));
        assertNull(runtimeObjectModel.resolveProperty(testArray, 3));
        assertNull(runtimeObjectModel.resolveProperty(testArray, -1));
        List<Integer> testList = Arrays.asList(testArray);
        assertEquals(2, runtimeObjectModel.resolveProperty(testList, 1));
        assertNull(runtimeObjectModel.resolveProperty(testList, 3));
        assertNull(runtimeObjectModel.resolveProperty(testList, -1));
        Map<String, Integer> map = new HashMap<>();
        map.put("one", 1);
        map.put("two", 2);
        assertEquals(1, runtimeObjectModel.resolveProperty(map, "one"));
        assertNull(runtimeObjectModel.resolveProperty(map, null));
        assertNull(runtimeObjectModel.resolveProperty(map, ""));
        Map<Integer, String> stringMap = new HashMap<>();
        stringMap.put(1, "one");
        stringMap.put(2, "two");
        assertEquals("one", runtimeObjectModel.resolveProperty(stringMap, 1));
        assertEquals("two", runtimeObjectModel.resolveProperty(stringMap, 2));
        Map<String, String> strings = new HashMap<>();
        strings.put("a", "one");
        strings.put("b", "two");
        Record<String> records = new Record<String>() {
            @Override
            public String getProperty(String name) {
                return strings.get(name);
            }

            @Override
            public Set<String> getPropertyNames() {
                return strings.keySet();
            }
        };
        assertEquals("one", runtimeObjectModel.resolveProperty(records, "a"));
    }

    @Test
    void testToDate() {
        assertNull(runtimeObjectModel.toDate(null));
        Date testDate = new Date();
        assertEquals(testDate, runtimeObjectModel.toDate(testDate));
        Calendar testCalendar = Calendar.getInstance();
        assertEquals(testCalendar.getTime(), runtimeObjectModel.toDate(testCalendar));
        Instant testInstant = Instant.now();
        assertEquals(Date.from(testInstant), runtimeObjectModel.toDate(testInstant));
    }

    @Test
    void testToInstant() {
        assertNull(runtimeObjectModel.toInstant(null));
        Date testDate = new Date();
        assertEquals(testDate, Date.from(runtimeObjectModel.toInstant(testDate)));
        Calendar testCalendar = Calendar.getInstance();
        assertEquals(testCalendar.getTime(), Date.from(runtimeObjectModel.toInstant(testCalendar)));
        Instant testInstant = Instant.now();
        assertEquals(testInstant, runtimeObjectModel.toInstant(testInstant));
    }

    @Test
    void testGetPropertyNullChecks() {
        assertNull(runtimeObjectModel.getProperty(null, null));
        assertNull(runtimeObjectModel.getProperty(this, null));
        assertNull(runtimeObjectModel.getProperty(this, ""));
    }

    @Test
    void testIsDate() {
        assertFalse(runtimeObjectModel.isDate(null));
        assertTrue(runtimeObjectModel.isDate(new Date()));
        assertTrue(runtimeObjectModel.isDate(Calendar.getInstance()));
        assertTrue(runtimeObjectModel.isDate(Instant.now()));
    }

    @Test
    void testIsNumber() {
        assertFalse(runtimeObjectModel.isNumber(null));
        assertFalse(runtimeObjectModel.isNumber(""));
        assertTrue(runtimeObjectModel.isNumber(0));
        assertTrue(runtimeObjectModel.isNumber(0.5));
        assertTrue(runtimeObjectModel.isNumber("0"));
        assertTrue(runtimeObjectModel.isNumber("0.5"));
    }

    @Test
    void testToCollection() {
        assertTrue(runtimeObjectModel.toCollection(null).isEmpty());
        final Map<String, String> properties = new HashMap<>();
        properties.put("a", "1");
        properties.put("b", "2");
        Record<String> record1 = new Record<String>() {

            @Override
            public String getProperty(String name) {
                return properties.get(name);
            }

            @Override
            public Set<String> getPropertyNames() {
                return properties.keySet();
            }
        };
        Collection<Object> testCollection = runtimeObjectModel.toCollection(record1);
        assertEquals(2, testCollection.size());
        assertTrue(testCollection.contains("a"));
        assertTrue(testCollection.contains("b"));
    }

    @Test
    void testToMap() {
        final Map<String, String> properties = new HashMap<>();
        properties.put("a", "1");
        properties.put("b", "2");
        assertEquals(properties, runtimeObjectModel.toMap(properties));
        Record<String> records = new Record<String>() {
            @Override
            public String getProperty(String name) {
                return properties.get(name);
            }

            @Override
            public Set<String> getPropertyNames() {
                return properties.keySet();
            }
        };
        assertEquals(properties, runtimeObjectModel.toMap(records));
        assertTrue(runtimeObjectModel.toMap(null).isEmpty());
    }
}
