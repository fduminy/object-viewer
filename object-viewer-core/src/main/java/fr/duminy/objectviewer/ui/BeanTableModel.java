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
package fr.duminy.objectviewer.ui;

import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.table.AbstractTableModel;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.lang.reflect.Modifier.PUBLIC;
import static org.reflections.ReflectionUtils.*;

/**
 * @author Fabien DUMINY
 */
class BeanTableModel<T> extends AbstractTableModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(BeanTableModel.class);
    private static final Comparator<Method> METHOD_COMPARATOR = new Comparator<Method>() {
        @Override
        public int compare(Method o1, Method o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

    private final transient Class<?>[] columnClasses;
    private final transient String[] columnNames;
    private final transient Object[][] values;
    private final transient int columnCount;
    private final transient int rowCount;

    @SuppressWarnings("unchecked") BeanTableModel(Class<T> baseClass, List<T> beans) {
        SortedSet<Method> getters = new TreeSet<Method>(METHOD_COMPARATOR);
        getters.addAll(getAllMethods(baseClass, withModifier(PUBLIC), withPrefix("get"), withParametersCount(0)));

        rowCount = beans.size();
        columnCount = getters.size();
        columnClasses = new Class<?>[columnCount];
        columnNames = new String[columnCount];
        values = new Object[columnCount][];
        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
            Method method = Iterables.get(getters, columnIndex);
            columnClasses[columnIndex] = method.getReturnType();
            columnNames[columnIndex] = method.getName().substring(3);

            Object[] columnValues = new Object[rowCount];
            values[columnIndex] = columnValues;
            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                try {
                    columnValues[rowIndex] = method.invoke(beans.get(rowIndex));
                } catch (IllegalAccessException e) {
                    LOGGER.error(e.getMessage(), e);
                } catch (InvocationTargetException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public int getRowCount() {
        return rowCount;
    }

    @Override
    public int getColumnCount() {
        return columnCount;
    }

    @Override public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1;
    }

    @Override public Class<?> getColumnClass(int columnIndex) {
        return columnClasses[columnIndex];
    }

    @Override public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return values[columnIndex][rowIndex];
    }
}
