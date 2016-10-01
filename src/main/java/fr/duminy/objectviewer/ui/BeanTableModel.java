/**
 * ObjectViewer is a tool allowing to search and view elements in a graph of objects.
 * <p>
 * Copyright (C) 2016-2016 Fabien DUMINY (fabien [dot] duminy [at] webmails [dot] com)
 * <p>
 * ObjectViewer is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * <p>
 * ObjectViewer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 */
package fr.duminy.objectviewer.ui;

import com.google.common.collect.Iterables;

import javax.swing.table.AbstractTableModel;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.reflections.ReflectionUtils.*;

/**
 * @author Fabien DUMINY
 */
class BeanTableModel<T> extends AbstractTableModel {
    private final SortedSet<Method> getters;
    private final List<T> values;

    public BeanTableModel(Class<T> objectsClass, List<T> values) {
        this.values = values;
        getters = new TreeSet<Method>(new Comparator<Method>() {

            public int compare(Method o1, Method o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        getters.addAll(
            getAllMethods(objectsClass, withModifier(Modifier.PUBLIC), withPrefix("get"), withParametersCount(0)));
    }

    public int getRowCount() {
        return values.size();
    }

    public int getColumnCount() {
        return getters.size();
    }

    @Override public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1;
    }

    @Override public Class<?> getColumnClass(int columnIndex) {
        return Iterables.get(getters, columnIndex).getReturnType();
    }

    @Override public String getColumnName(int column) {
        return Iterables.get(getters, column).getName().substring(3);
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        try {
            return Iterables.get(getters, columnIndex).invoke(values.get(rowIndex));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }
}
