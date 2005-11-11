package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.services.Page;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.ScrollableTable;
import gov.epa.emissions.framework.ui.TableData;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class PageViewPanel extends JPanel implements PageView {

    private EmfTableModel tableModel;

    private InternalSource source;

    public PageViewPanel(InternalSource source) {
        super(new BorderLayout());
        this.source = source;
    }

    public void display(Page page) {
        // TODO: refresh table w/ new data?
        super.add(doLayout(page), BorderLayout.CENTER);
    }

    private JPanel doLayout(Page page) {
        JPanel container = new JPanel(new BorderLayout());

        container.add(table(new PageData(source, page)), BorderLayout.CENTER);
        container.add(paginationPanel(), BorderLayout.PAGE_END);

        return container;
    }

    private JScrollPane table(TableData tableData) {
        tableModel = new EmfTableModel(tableData);
        return new ScrollableTable(tableModel);
    }

    private JPanel paginationPanel() {
        return new JPanel();
    }

}
