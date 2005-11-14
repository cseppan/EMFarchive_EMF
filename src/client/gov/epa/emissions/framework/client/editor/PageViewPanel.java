package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.services.Page;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.IconButton;
import gov.epa.emissions.framework.ui.ImageResources;
import gov.epa.emissions.framework.ui.ScrollableTable;
import gov.epa.emissions.framework.ui.TableData;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

public class PageViewPanel extends JPanel implements PageView {

    private EmfTableModel tableModel;

    private InternalSource source;

    public PageViewPanel(InternalSource source) {
        super(new BorderLayout());
        this.source = source;
    }

    public void display(Page page) {
        // TODO: refresh table w/ new data?

        JScrollPane table = table(new PageData(source, page));
        super.add(paginationPanel(), BorderLayout.PAGE_START);
        super.add(table, BorderLayout.CENTER);
    }

    private JScrollPane table(TableData tableData) {
        tableModel = new EmfTableModel(tableData);
        return new ScrollableTable(tableModel);
    }

    private JPanel paginationPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JToolBar toolbar = new JToolBar("Still draggable");
        toolbar.setFloatable(false);
        addButtons(toolbar);
        panel.add(toolbar, BorderLayout.LINE_END);

        return panel;
    }

    protected void addButtons(JToolBar toolBar) {
        ImageResources res = new ImageResources();

        toolBar.add(new IconButton("Prev", "Go to Previous Page", res.prev("Go to Previous Page")));
        toolBar.add(new IconButton("Next", "Go to Next Page", res.next("Go to Next Page")));
        toolBar.add(new IconButton("First", "Go to First Page", res.first("Go to First Page")));
        toolBar.add(new IconButton("Last", "Go to Last Page", res.last("Go to Last Page")));
    }

}
