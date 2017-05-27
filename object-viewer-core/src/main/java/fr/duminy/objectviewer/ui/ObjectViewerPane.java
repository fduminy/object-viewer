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

import com.google.common.base.Function;
import com.google.common.collect.Range;
import org.oxbow.swingbits.table.filter.TableRowFilterSupport;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import static com.google.common.base.Joiner.on;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Range.closed;
import static java.awt.BorderLayout.CENTER;
import static java.awt.Cursor.*;
import static java.lang.String.format;
import static java.util.UUID.fromString;
import static javax.swing.text.html.HTML.Attribute.HREF;
import static javax.swing.text.html.HTML.Tag.A;

/**
 * @author Fabien DUMINY
 */
public class ObjectViewerPane<T> extends JPanel {
    private final Class<T> baseClass;
    private final Map<UUID, Component> uuidToComponent = new HashMap<UUID, Component>();

    private JTabbedPane tabbedPane;

    public ObjectViewerPane(Class<T> baseClass, List<T> values) {
        this.baseClass = baseClass;
        JComponent contentPane = getTabContent(values);
        tabbedPane = new JTabbedPane();
        tabbedPane.add(contentPane.getName(), contentPane);
        setLayout(new BorderLayout());
        add(tabbedPane, CENTER);
    }

    @SuppressWarnings("unchecked")
    private JComponent getTabContent(List<T> values) {
        JComponent component = buildTableModel(values);
        component.setName(baseClass.getSimpleName());
        return component;
    }

    @SuppressWarnings("unchecked")
    private JComponent buildTableModel(final List<T> values) {
        TableModel tableModel = new BeanTableModel(baseClass, values);
        final JTable table = new JTable(tableModel);

        final JEditorPane renderer = new JEditorPane();
        renderer.setEditable(false);
        renderer.setOpaque(false);
        renderer.setBorder(BorderFactory.createEmptyBorder());
        EditorKit editorKit = JEditorPane.createEditorKitForContentType("text/html");
        renderer.setEditorKit(editorKit);
        final JComponent oldRenderer = (JComponent) table.getDefaultRenderer(String.class);
        TableCellRenderer tableCellRenderer = new TableCellRenderer() {
            private final Function<Object, String> idToHyperlink = new Function<Object, String>() {
                @Nullable @Override public String apply(@Nullable Object input) {
                    String result = "";
                    if (input != null) {
                        result = format("<a href=\"uuid=%s\">%s</a>", input, input);
                    }
                    return result;
                }
            };

            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus,
                                                           int row, int column) {
                if (hasFocus) {
                    renderer.requestFocus();
                } else {
                    renderer.transferFocus();
                }
                //                renderer.setSelected(isSelected);

                String text = renderIdHyperlink(value);
                renderer.setText(text);
                return renderer;
            }

            private String renderIdHyperlink(@Nullable Object value) {
                String result;
                if ((value != null) && Iterable.class.isAssignableFrom(value.getClass())) {
                    result = on(',').join(transform((Iterable) value, idToHyperlink));
                } else {
                    result = idToHyperlink.apply(value);
                }
                return "<html>" + result + "</html>";
            }
        };
        table.setDefaultRenderer(UUID.class, tableCellRenderer);
        table.setDefaultRenderer(Iterable.class, tableCellRenderer);
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                handleMouseEvent(e, new MouseCallBack() {
                    @Override public void mouseOnHyperlink(String uuid) {
                        showObjectTab(uuid);
                    }
                });
            }

            @Override public void mouseEntered(MouseEvent e) {
                handleMouseMove(e);
            }

            @Override public void mouseExited(MouseEvent e) {
                handleMouseMove(e);
            }

            @Override public void mouseMoved(MouseEvent e) {
                handleMouseMove(e);
            }

            private void handleMouseMove(MouseEvent e) {
                handleMouseEvent(e, new MouseCallBack() {
                    @Override public void mouseOnHyperlink(String uuid) {
                        table.setCursor(getPredefinedCursor(HAND_CURSOR));
                    }
                });
            }

            private void handleMouseEvent(MouseEvent e, MouseCallBack callBack) {
                boolean onALink = false;

                TableColumnModel columnModel = table.getColumnModel();
                int column = columnModel.getColumnIndexAtX(e.getX());
                int row = table.rowAtPoint(e.getPoint());
                if ((column >= 0) && (row >= 0)) {
                    Rectangle rectangle = table.getCellRect(row, column, true);
                    renderer.setSize(rectangle.getSize());
                    Object link = renderer.getAccessibleContext().getAccessibleComponent();
                    JEditorPane.AccessibleJTextComponent l = (JEditorPane.AccessibleJTextComponent) link;

                    Point pt = new Point(e.getPoint());
                    pt.translate((int) -rectangle.getX(), (int) -rectangle.getY());
                    int index = l.getIndexAtPoint(pt);

                    HTMLDocument doc = (HTMLDocument) renderer.getDocument();
                    HTMLDocument.Iterator elements = doc.getIterator(A);
                    Map<Range<Integer>, String> indexes = new HashMap<Range<Integer>, String>();
                    int ind = 0;
                    boolean first = true;
                    for (; elements.isValid(); elements.next()) {
                        if (!first) {
                            ind++;
                        }
                        first = false;

                        String o = (String) elements.getAttributes().getAttribute(HREF);
                        o = o.split("=")[1];
                        Range<Integer> range = closed(ind, ind + o.length() - 1);
                        indexes.put(range, o);
                        ind += o.length();
                    }

                    for (Entry<Range<Integer>, String> entry : indexes.entrySet()) {
                        if (entry.getKey().contains(index)) {
                            onALink = true;
                            callBack.mouseOnHyperlink(entry.getValue());
                            break;
                        }
                    }
                }

                if (!onALink) {
                    table.setCursor(getPredefinedCursor(DEFAULT_CURSOR));
                }
            }
        });

        return new JScrollPane(TableRowFilterSupport.forTable(table).apply());
    }

    private void showObjectTab(String uuid) {
        UUID uuidObject = fromString(uuid);
        Component component = uuidToComponent.get(uuidObject);
        if (component == null) {
            component = new JPanel();
            component.setName(uuid);
            uuidToComponent.put(uuidObject, component);
            tabbedPane.add(component.getName(), component);
        }

        tabbedPane.setSelectedComponent(component);
    }

    private class MouseCallBack {
        void mouseOnHyperlink(String uuid) {
        }
    }
}
