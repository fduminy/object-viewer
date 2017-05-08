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
package fr.duminy.objectviewer.spi;

import fr.duminy.objectviewer.api.ObjectLoader;
import fr.duminy.objectviewer.api.ObjectLoaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import static com.google.common.io.Closeables.closeQuietly;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

/**
 * @author Fabien DUMINY
 */
@ObjectLoader
public class SerializableProvider implements ObjectLoaderService<Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SerializableProvider.class);

    @SuppressWarnings("unchecked")
    @Override
    public Iterable<Object> loadObjects() {
        JFileChooser jfc = new JFileChooser();
        if (jfc.showDialog(null, "Load") == JFileChooser.APPROVE_OPTION) {
            File file = jfc.getSelectedFile();
            ObjectInputStream input = null;
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                input = new ObjectInputStream(fis);
                Object object = input.readObject();
                return (object instanceof Iterable) ? (Iterable<Object>) object : singleton(object);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            } catch (ClassNotFoundException e) {
                LOGGER.error(e.getMessage(), e);
            } finally {
                closeQuietly(input);
                closeQuietly(fis);
            }
        }
        return emptySet();
    }

    @Override
    public Class<Object> getBaseClass() {
        return Object.class;
    }
}
