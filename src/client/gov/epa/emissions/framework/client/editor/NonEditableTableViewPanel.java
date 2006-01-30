package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.gui.ScrollableTextArea;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.ScrollableTable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;

public class NonEditableTableViewPanel extends JPanel implements NonEditableTableView {

    private EmfTableModel tableModel;

    private InternalSource source;

    private JPanel pageContainer;

    private PaginationPanel paginationPanel;

    private ScrollableTable table;

    public NonEditableTableViewPanel(InternalSource source, MessagePanel messagePanel) {
        super(new BorderLayout());
        this.source = source;

        doLayout(messagePanel);
    }

    private void setBorder() {
        javax.swing.border.Border outer = BorderFactory.createEmptyBorder(10, 5, 10, 5);
        javax.swing.border.Border inner = BorderFactory.createLineBorder(Color.GRAY);
        CompoundBorder border = BorderFactory.createCompoundBorder(outer, inner);

        super.setBorder(border);
    }

    private void doLayout(MessagePanel messagePanel) {
        paginationPanel = new PaginationPanel(messagePanel);
        super.add(paginationPanel, BorderLayout.PAGE_START);

        pageContainer = new JPanel(new BorderLayout());
        super.add(pageContainer, BorderLayout.CENTER);

        add(notesPanel(), BorderLayout.PAGE_END);
        setBorder();
    }

    private JPanel notesPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel notesLabelPanel = new JPanel(new BorderLayout());
        notesLabelPanel.add(new JLabel("Notes"), BorderLayout.LINE_START);
        panel.add(notesLabelPanel);

        TextArea notes = new TextArea("Notes", "Notes will be coming soon...");
        notes.setEditable(false);
        panel.add(new ScrollableTextArea(notes));

        return panel;
    }

    public void observe(TablePresenter presenter) {
        paginationPanel.init(presenter);
    }

    public void display(Page page) {
        // TODO: refresh table w/ new data? or remove and add back the table?
        pageContainer.removeAll();

        paginationPanel.updateStatus(page);

        tableModel = new EmfTableModel(new ViewablePage(page, cols()));
        table = new ScrollableTable(tableModel);
        pageContainer.add(table, BorderLayout.CENTER);
    }

    // Filter out the first four (version-specific cols)
    private String[] cols() {
        // TODO: should these cols come from the Page/Service?
        List cols = new ArrayList();

        String[] allCols = source.getCols();
        for (int i = 4; i < allCols.length; i++)
            cols.add(allCols[i]);

        return (String[]) cols.toArray(new String[0]);
    }

    public void scrollToPageEnd() {
        table.selectLastRow();
    }

}
