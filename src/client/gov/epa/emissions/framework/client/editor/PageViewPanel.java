package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.ScrollableTable;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class PageViewPanel extends JPanel implements PageView {

    private EmfTableModel tableModel;

    private InternalSource source;

    private JPanel pageContainer;

    private PaginationPanel paginationPanel;

    public PageViewPanel(InternalSource source, MessagePanel messagePanel) {
        super(new BorderLayout());
        this.source = source;

        paginationPanel = new PaginationPanel(messagePanel);
        super.add(paginationPanel, BorderLayout.PAGE_START);

        pageContainer = new JPanel(new BorderLayout());
        super.add(pageContainer, BorderLayout.CENTER);
    }

    public void observe(PageViewPresenter presenter) {
        paginationPanel.init(presenter);
    }

    public void display(Page page) {
        // TODO: refresh table w/ new data? or remove and add back the table?
        pageContainer.removeAll();

        paginationPanel.updateStatus(page);

        tableModel = new EmfTableModel(new PageData(source, page));
        JScrollPane table = new ScrollableTable(tableModel);
        pageContainer.add(table, BorderLayout.CENTER);
    }

}
