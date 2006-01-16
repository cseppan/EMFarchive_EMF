package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ScrollableTextArea;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.ScrollableTable;
import gov.epa.emissions.framework.ui.SelectableEmfTableData;
import gov.epa.emissions.framework.ui.TableData;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.CompoundBorder;

public class EditableTableDataPanel extends JPanel {

    private EmfTableModel tableModel;

    private ScrollableTable table;

    private MessagePanel messagePanel;

    public EditableTableDataPanel(SelectableEmfTableData tableData, MessagePanel messagePanel) {
        super.setLayout(new BorderLayout());
        super.add(doLayout(tableData), BorderLayout.CENTER);

        this.messagePanel = messagePanel;
    }

    private JPanel doLayout(SelectableEmfTableData tableData) {
        JPanel container = new JPanel(new BorderLayout());

        container.add(table(tableData), BorderLayout.CENTER);
        container.add(bottomPanel(tableData), BorderLayout.PAGE_END);

        setBorder();
        return container;
    }

    private void setBorder() {
        javax.swing.border.Border outer = BorderFactory.createEmptyBorder(10, 5, 10, 5);
        javax.swing.border.Border inner = BorderFactory.createLineBorder(Color.GRAY);
        CompoundBorder border = BorderFactory.createCompoundBorder(outer, inner);

        super.setBorder(border);
    }

    private JPanel bottomPanel(SelectableEmfTableData tableData) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel buttonsPanel = buttonsPanel(tableData);
        panel.add(buttonsPanel);

        panel.add(notesPanel());

        TextArea notes = new TextArea("Notes", "");
        panel.add(new ScrollableTextArea(notes));

        return panel;
    }

    private JPanel notesPanel() {
        JPanel container = new JPanel(new BorderLayout());

        JPanel labelsPanel = new JPanel();
        labelsPanel.add(new JLabel("Notes"));
        
        JLabel comingSoon = new JLabel("(Coming Soon)");
        comingSoon.setForeground(Color.BLUE);
        labelsPanel.add(comingSoon);
        
        container.add(labelsPanel, BorderLayout.LINE_START);
        
        return container;
    }

    private JScrollPane table(TableData tableData) {
        tableModel = new EmfTableModel(tableData);
        table = new ScrollableTable(tableModel);
        return table;
    }

    private JPanel buttonsPanel(final SelectableEmfTableData tableData) {
        JPanel container = new JPanel();

        Button add = new Button("Add", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doAdd(tableData);
            }
        });
        container.add(add);

        Button remove = new Button("Remove", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doRemove(tableData);
            }
        });
        container.add(remove);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(container, BorderLayout.LINE_END);

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

    private void doAdd(final SelectableEmfTableData tableData) {
        clearMessages();
        
        tableData.addBlankRow();
        refresh();

        table.moveToBottom();
        refresh();
    }

    private void doRemove(final SelectableEmfTableData tableData) {
        clearMessages();
        tableData.removeSelected();
        refresh();
    }

}
