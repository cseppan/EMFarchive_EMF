package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.ScrollableTable;
import gov.epa.emissions.framework.ui.TableColumnWidth;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

public class EditablePagePanel extends JPanel {

    private EmfTableModel tableModel;

    private ScrollableTable table;

    private MessagePanel messagePanel;

    private ManageChangeables listOfChangeables;

    private DataEditableTable editableTable;

    public EditablePagePanel(EditablePage page, MessagePanel messagePanel, ManageChangeables listOfChangeables) {
        this.listOfChangeables = listOfChangeables;
        this.messagePanel = messagePanel;
        super.setLayout(new BorderLayout());
        super.add(doLayout(page), BorderLayout.CENTER);
    }

    private JPanel doLayout(EditablePage tableData) {
        JPanel container = new JPanel(new BorderLayout());

        container.add(table(tableData), BorderLayout.CENTER);
        container.add(bottomPanel(tableData), BorderLayout.PAGE_END);

        setBorder();
        return container;
    }

    private void setBorder() {
        Border outer = BorderFactory.createEmptyBorder(10, 5, 10, 5);
        Border inner = BorderFactory.createLineBorder(Color.GRAY);
        CompoundBorder border = BorderFactory.createCompoundBorder(outer, inner);

        super.setBorder(border);
    }

    private JPanel bottomPanel(EditablePage tableData) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel buttonsPanel = buttonsPanel(tableData);
        panel.add(buttonsPanel);

        return panel;
    }

    private JScrollPane table(EditablePage tableData) {
        tableModel = new EmfTableModel(tableData);
        editableTable = new DataEditableTable(tableModel, tableData.getTableMetadata(), messagePanel);
        listOfChangeables.addChangeable(editableTable);

        table = new ScrollableTable(editableTable);

        TableMetadata metadata = tableData.getTableMetadata();
        new TableColumnWidth(editableTable, metadata).columnWidths();
        return table;
    }

    private JPanel buttonsPanel(final EditablePage tableData) {
        JPanel container = new JPanel();

        Button insertAbove = new Button("Insert Row Above", insertRowAction(tableData, true));
        container.add(insertAbove);
        insertAbove.setToolTipText("insert a row above the selection to this table");

        Button insertBelow = new Button("Insert Row Below", insertRowAction(tableData, false));
        container.add(insertBelow);
        insertBelow.setToolTipText("insert a row above the selection to this table");

        Button remove = new Button("Remove Rows", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doRemove(tableData);
            }
        });
        container.add(remove);
        remove.setToolTipText("Remove the selected rows from this table");

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(container, BorderLayout.WEST);

        return panel;
    }

    private AbstractAction insertRowAction(final EditablePage tableData, final boolean above) {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doAdd(tableData, editableTable, above);
            }
        };
    }

    protected void clearMessagesWithTableRefresh() {
        messagePanel.clear();
        refresh();
    }

    private void refresh() {
        tableModel.refresh();
        super.revalidate();
    }

    private void doAdd(final EditablePage tableData, DataEditableTable editableTable, boolean above) {
        int selectedRow = editableTable.getSelectedRow();
        messagePanel.clear();
        if (selectedRow != -1) {
            int insertRowNo = tableData.addBlankRow(selectedRow, above);
            refresh();
            editableTable.setRowSelectionInterval(insertRowNo, insertRowNo);
        } else {
            messagePanel.setError("Please highlight a row before insert a row");
        }
    }

    private void doRemove(final EditablePage tableData) {
        clearMessagesWithTableRefresh();
        tableData.removeSelected();
        refresh();
    }

    public void scrollToPageEnd() {
        table.moveToBottom();
    }
}
