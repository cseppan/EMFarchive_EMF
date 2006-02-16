package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.ScrollableTable;
import gov.epa.emissions.framework.ui.TableData;

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

    public EditablePagePanel(EditablePage page, MessagePanel messagePanel, ManageChangeables listOfChangeables) {
        this.listOfChangeables = listOfChangeables;
        super.setLayout(new BorderLayout());
        super.add(doLayout(page), BorderLayout.CENTER);

        this.messagePanel = messagePanel;
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

    private JScrollPane table(TableData tableData) {
        tableModel = new EmfTableModel(tableData);
        listOfChangeables.addChangeable(tableModel);
        tableModel.addDataChangeListener();
        table = new ScrollableTable(tableModel);
        return table;
    }

    private JPanel buttonsPanel(final EditablePage tableData) {
        JPanel container = new JPanel();

        Button add = new Button("Add Row", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doAdd(tableData);
            }
        });
        container.add(add);
        add.setToolTipText("Add a row to this table");

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

    protected void clearMessages() {
        messagePanel.clear();
        refresh();
    }

    private void refresh() {
        tableModel.refresh();
        super.revalidate();
    }

    private void doAdd(final EditablePage tableData) {
        clearMessages();
        tableData.addBlankRow();
        refresh();
        scrollToPageEnd();
    }

    private void doRemove(final EditablePage tableData) {
        clearMessages();
        tableData.removeSelected();
        refresh();
    }

    public void scrollToPageEnd() {
        table.moveToBottom();
    }

}
