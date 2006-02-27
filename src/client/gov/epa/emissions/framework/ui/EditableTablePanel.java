package gov.epa.emissions.framework.ui;

import gov.epa.emissions.commons.gui.EditableTable;
import gov.epa.emissions.commons.gui.Editor;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.Label;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class EditableTablePanel extends JPanel implements Editor {

    protected EmfTableModel tableModel;

    protected EditableTable table;

    protected ManageChangeables changeablesList;

    public EditableTablePanel(String label, SelectableEmfTableData tableData, ManageChangeables changeablesList) {
        this.changeablesList = changeablesList;
        super.setLayout(new BorderLayout());
        super.add(doLayout(label, tableData), BorderLayout.CENTER);
    }

    private JPanel doLayout(String label, SelectableEmfTableData tableData) {
        JPanel container = new JPanel(new BorderLayout());

        container.add(labelPanel(label), BorderLayout.PAGE_START);
        container.add(table(tableData), BorderLayout.CENTER);
        container.add(buttonsPanel(tableData), BorderLayout.PAGE_END);

        return container;
    }

    private JPanel labelPanel(String name) {
        JPanel panel = new JPanel(new BorderLayout());

        Label label = new Label(name);
        panel.add(label, BorderLayout.WEST);

        return panel;
    }

    protected JScrollPane table(TableData tableData) {
        tableModel = new EmfTableModel(tableData);
        table = new EditableTable(tableModel);
        changeablesList.addChangeable(table);

        return new JScrollPane(table);
    }

    private void setColumnWidths(TableColumnModel model) {
        TableColumn col = model.getColumn(0);
        col.setMaxWidth(250);
    }

    private JPanel buttonsPanel(final SelectableEmfTableData tableData) {
        JPanel container = new JPanel();

        JButton add = new JButton("Add");
        add.setMargin(new Insets(2, 2, 2, 2));
        add.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                tableData.addBlankRow();
                refresh();
            }
        });
        container.add(add);

        JButton remove = new JButton("Remove");
        remove.setMargin(new Insets(2, 2, 2, 2));
        remove.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                tableData.removeSelected();
                refresh();
            }
        });
        container.add(remove);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(container, BorderLayout.WEST);

        return panel;
    }

    private void refresh() {
        tableModel.refresh();
        super.revalidate();
    }

    public void setColumnEditor(TableCellEditor editor, int columnIndex, String toolTip) {
        TableColumnModel colModel = table.getColumnModel();
        TableColumn col = colModel.getColumn(columnIndex);
        col.setCellEditor(editor);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setToolTipText(toolTip);
        col.setCellRenderer(renderer);
    }

    public void invalidate() {
        setColumnWidths(table.getColumnModel());
        super.invalidate();
    }

    public void commit() {
        table.commit();
    }

    public void addListener(KeyListener keyListener) {
        table.addKeyListener(keyListener);
    }

}
