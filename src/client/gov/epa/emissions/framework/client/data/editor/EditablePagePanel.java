package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.ui.EditableEmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.ScrollableTable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

public class EditablePagePanel extends JPanel {

    private EditableEmfTableModel tableModel;

    private ScrollableTable table;

    private MessagePanel messagePanel;

    private ManageChangeables listOfChangeables;

    private DataEditorTable editableTable;

    public EditablePagePanel(EditablePage page, MessagePanel messagePanel, ManageChangeables listOfChangeables) {
        this.listOfChangeables = listOfChangeables;
        this.messagePanel = messagePanel;
        super.setLayout(new BorderLayout());
        super.add(doLayout(page), BorderLayout.CENTER);
    }

    private JPanel doLayout(EditablePage tableData) {
        JPanel container = new JPanel(new BorderLayout());
        JToolBar toolBar = toolBar(tableData);
        container.add(toolBar, BorderLayout.PAGE_START);
        container.add(table(tableData), BorderLayout.CENTER);

        setBorder();
        return container;
    }

    private JToolBar toolBar(EditablePage tableData) {
        JToolBar toolbar = new JToolBar();
        String insertAbove = "/toolbarButtonGraphics/table/RowInsertBefore" + 24 + ".gif";
        ImageIcon iconAbove = createImageIcon(insertAbove);
        String nameAbove = "Insert Above";
        JButton buttonAbove = toolbar.add(insertRowAction(tableData, true, nameAbove, iconAbove));
        buttonAbove.setToolTipText(nameAbove);

        String insertBelow = "/toolbarButtonGraphics/table/RowInsertAfter" + 24 + ".gif";
        ImageIcon iconBelow = createImageIcon(insertBelow);
        String nameBelow = "Insert Below";
        JButton buttonBelow = toolbar.add(insertRowAction(tableData, false, nameBelow, iconBelow));
        buttonBelow.setToolTipText(nameBelow);

        String delete = "/toolbarButtonGraphics/table/RowDelete" + 24 + ".gif";
        ImageIcon iconDelete = createImageIcon(delete);
        String nameDelete = "Delete";
        JButton buttonDelete = toolbar.add(deleteAction(tableData, nameDelete, iconDelete));
        buttonDelete.setToolTipText(nameDelete);
        return toolbar;
    }

    private void setBorder() {
        Border outer = BorderFactory.createEmptyBorder(10, 5, 10, 5);
        Border inner = BorderFactory.createLineBorder(Color.GRAY);
        CompoundBorder border = BorderFactory.createCompoundBorder(outer, inner);

        super.setBorder(border);
    }

    private JScrollPane table(EditablePage tableData) {
        tableModel = new EditableEmfTableModel(tableData);
        editableTable = new DataEditorTable(tableModel, tableData.getTableMetadata(), messagePanel);
        listOfChangeables.addChangeable(editableTable);

        table = new ScrollableTable(editableTable);
        return table;
    }

    private AbstractAction deleteAction(final EditablePage tableData, String nameDelete, ImageIcon iconDelete) {
        return new AbstractAction(nameDelete, iconDelete) {
            public void actionPerformed(ActionEvent e) {
                doRemove(tableData);
            }
        };
    }

    private Action insertRowAction(final EditablePage tableData, final boolean above, String name, ImageIcon icon) {
        return new AbstractAction(name, icon) {
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

    private void doAdd(final EditablePage tableData, DataEditorTable editableTable, boolean above) {
        int selectedRow = editableTable.getSelectedRow();
        messagePanel.clear();
        if (selectedRow != -1) {
            int insertRowNo = (above) ? selectedRow : selectedRow + 1;
            tableData.addBlankRow(selectedRow);
            refresh();
            editableTable.setRowSelectionInterval(insertRowNo, insertRowNo);
        } else {
            messagePanel.setError("Please highlight a row before insert");
        }
    }

    private void doRemove(final EditablePage tableData) {
        clearMessagesWithTableRefresh();
        if (tableData.getSelected().length == 0) {
            messagePanel.setError("Please select one more rows for removing data");
        } else {
            tableData.removeSelected();
            refresh();
        }
    }

    public void scrollToPageEnd() {
        table.moveToBottom();
    }

    protected ImageIcon createImageIcon(String path) {
        URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        }
        messagePanel.setError("Could not find file: " + path);
        return null;
    }
}
