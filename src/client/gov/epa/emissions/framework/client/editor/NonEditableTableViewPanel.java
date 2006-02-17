package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.ScrollableTable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

public class NonEditableTableViewPanel extends JPanel implements NonEditablePageManagerView {

    private EmfTableModel tableModel;

    private InternalSource source;

    private JPanel pageContainer;

    private PaginationPanel paginationPanel;

    private ScrollableTable table;

    private DataSortFilterPanel sortFilterPanel;
    
    private EmfDataset dataset;

    public NonEditableTableViewPanel(InternalSource source, MessagePanel messagePanel, EmfDataset dataset) {
        super(new BorderLayout());
        this.source = source;
        this.dataset = dataset;
        doLayout(messagePanel);
    }

    private void setBorder() {
        Border outer = BorderFactory.createEmptyBorder(10, 5, 10, 5);
        Border inner = BorderFactory.createLineBorder(Color.GRAY);
        CompoundBorder border = BorderFactory.createCompoundBorder(outer, inner);

        super.setBorder(border);
    }

    private void doLayout(MessagePanel messagePanel) {
        super.add(topPanel(messagePanel), BorderLayout.PAGE_START);

        pageContainer = new JPanel(new BorderLayout());
        super.add(pageContainer, BorderLayout.CENTER);
        setBorder();
    }

    private JPanel topPanel(MessagePanel messagePanel) {
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(sortFilterPanel(messagePanel), BorderLayout.LINE_START);

        paginationPanel = new PaginationPanel(messagePanel);
        panel.add(paginationPanel, BorderLayout.LINE_END);

        return panel;
    }

    private DataSortFilterPanel sortFilterPanel(MessagePanel messagePanel) {
        sortFilterPanel = new DataSortFilterPanel(messagePanel, null, dataset);
        return sortFilterPanel;
    }

    public void observe(TablePresenter presenter) {
        paginationPanel.init(presenter);
        sortFilterPanel.init(presenter);
    }

    public void display(Page page) {
        pageContainer.removeAll();

        paginationPanel.updateStatus(page);
        pageContainer.add(table(page), BorderLayout.CENTER);
    }

    private ScrollableTable table(Page page) {
        tableModel = new EmfTableModel(new ViewablePage(page, cols()));
        table = new ScrollableTable(tableModel);
        return table;
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
