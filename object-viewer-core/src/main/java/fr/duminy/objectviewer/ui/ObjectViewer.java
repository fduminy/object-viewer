/**
 * ObjectViewer is a tool allowing to search and view elements in a graph of objects.
 *
 * Copyright (C) 2016-2017 Fabien DUMINY (fabien [dot] duminy [at] webmails [dot] com)
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
package fr.duminy.objectviewer.ui;

import fr.duminy.objectviewer.api.ObjectLoader;
import fr.duminy.objectviewer.api.ObjectLoaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import static com.google.common.base.Strings.nullToEmpty;
import static java.lang.System.getProperty;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;
import static net.java.sezpoz.Index.load;

/**
 * @author Fabien DUMINY
 */
public class ObjectViewer {
    public static final String LOADER_PROPERTY = "loader";

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectViewer.class);

    public static void main(String[] args) throws InstantiationException {
        JFrame frame = new JFrame("Object Viewer");
        frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        frame.setContentPane(buildObjectViewerPane());
        frame.setVisible(true);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    @SuppressWarnings("unchecked")
    private static <T> ObjectViewerPane<T> buildObjectViewerPane() throws InstantiationException {
        String loaderClass = nullToEmpty(getProperty(LOADER_PROPERTY)).trim();
        ObjectLoaderService<T> service = null;
        try {
            service = ObjectLoaderService.class.cast(Class.forName(loaderClass).newInstance());
        } catch (IllegalAccessException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        }

        if (service == null) {
            service = load(ObjectLoader.class, ObjectLoaderService.class).iterator().next().instance();
        }

        final Class<T> baseClass = service.getBaseClass();
        final java.util.List<T> values = new ArrayList<T>();
        for (T item : service.loadObjects()) {
            values.add(item);
        }
        return new ObjectViewerPane(baseClass, values);
    }
}
