/**
 * ObjectViewer is a tool allowing to search and view elements in a graph of objects.
 *
 * Copyright (C) 2016-2016 Fabien DUMINY (fabien [dot] duminy [at] webmails [dot] com)
 *
 * ObjectViewer is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * ObjectViewer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 */
package fr.duminy.objectviewer.demo;

import fr.duminy.objectviewer.api.ObjectLoader;
import fr.duminy.objectviewer.api.ObjectLoaderService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

/**
 * @author Fabien DUMINY
 */
@ObjectLoader
public class DemoDataProvider implements ObjectLoaderService<DemoDataProvider.TestObject> {
    public Iterable<TestObject> loadObjects() {
        return Arrays.<TestObject>asList(
            new TestObject("111-111-111-111-111", Arrays.asList("333-333-333-333-333", "444-444-444-444-444"),
                           "value1"),
            new TestObject("222-222-222-222-222", Arrays.asList("555-555-555-555-555"), "value2"));
    }

    public Class<TestObject> getBaseClass() {
        return TestObject.class;
    }

    public static class TestObject {
        private final UUID uuid;
        private final Collection<UUID> uuids;
        private final String field1;

        public TestObject(String uuid, Iterable<String> uuids, String field1) {
            this.uuid = UUID.fromString(uuid);
            this.uuids = new ArrayList<UUID>();
            for (String id : uuids) {
                this.uuids.add(UUID.fromString(id));
            }
            this.field1 = field1;
        }

        public UUID getUuid() {
            return uuid;
        }

        public String getField1() {
            return field1;
        }

        public Iterable<UUID> getUuids() {
            return uuids;
        }
    }
}
