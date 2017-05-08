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

import fr.duminy.objectviewer.api.ObjectLoader;
import fr.duminy.objectviewer.api.ObjectLoaderService;
import net.java.sezpoz.Index;
import org.oxbow.swingbits.table.filter.TableRowFilterSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;

import static com.google.common.base.Strings.nullToEmpty;

/**
 * @author Fabien DUMINY
 */
public class ObjectViewer {
    public static final String LOADER_PROPERTY = "loader";

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectViewer.class);

    public static void main(String[] args) throws InstantiationException {
        JComponent contentPane = getTabContent();
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add(contentPane.getName(), contentPane);

        JFrame frame = new JFrame("Object Viewer");
        frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        frame.setContentPane(tabbedPane);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static <T> JComponent getTabContent() throws InstantiationException {
        String loaderClass = nullToEmpty(System.getProperty(LOADER_PROPERTY)).trim();
        ObjectLoaderService<T> service = null;
        if (loaderClass != null) {
            try {
                service = ObjectLoaderService.class.cast(Class.forName(loaderClass).newInstance());
            } catch (IllegalAccessException e) {
                LOGGER.error(e.getMessage(), e);
            } catch (ClassNotFoundException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        if (service == null) {
            service = Index.load(ObjectLoader.class, ObjectLoaderService.class).iterator()
                           .next().instance();
        }

        final Class<T> baseClass = service.getBaseClass();
        final List<T> values = new ArrayList<T>();
        for (T item : service.loadObjects()) {
            values.add(item);
        }
        JComponent component = buildTableModel(values, baseClass);
        component.setName(baseClass.getSimpleName());
        return component;
    }

    @SuppressWarnings("unchecked")
    private static <T> JComponent buildTableModel(final List<T> values, final Class<T> baseClass)
        throws InstantiationException {
        TableModel tableModel = new BeanTableModel(baseClass, values);
        final JTable table = new JTable(tableModel);
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                TableColumnModel columnModel = table.getColumnModel();
                int column = columnModel.getColumnIndexAtX(e.getX());
                int row = table.rowAtPoint(e.getPoint());
                if ((column >= 0) && (row >= 0)) {
                    Object value = table.getValueAt(row, column);
                    LOGGER.info("value={}", value);
                }
            }
        });

        final JEditorPane renderer = new JEditorPane();
        renderer.setEditable(true);
        renderer.setOpaque(false);
        renderer.setBorder(BorderFactory.createEmptyBorder());
        renderer.setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));
        renderer.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    StringTokenizer st = new StringTokenizer(e.getDescription(), " ");
                    if (st.hasMoreTokens()) {
                        JOptionPane.showMessageDialog(null, "clicked on " + st.nextToken());
                    }
                }
            }
        });
        final JComponent oldRenderer = (JComponent) table.getDefaultRenderer(String.class);
        TableCellRenderer tableCellRenderer = new TableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus,
                                                           int row, int column) {
                if (hasFocus) {
                    renderer.requestFocus();
                } else {
                    renderer.transferFocus();
                }
                //                renderer.setSelected(isSelected);
                String text = String
                    .format("<html><a href=\"uuid=%s\">%s</a></html>", value, value);
                renderer.setText(text);
                return renderer;
            }
        };
        table.setDefaultRenderer(UUID.class, tableCellRenderer);
        table.setDefaultRenderer(Iterable.class, tableCellRenderer);
        return new JScrollPane(TableRowFilterSupport.forTable(table).apply());
    }

}
