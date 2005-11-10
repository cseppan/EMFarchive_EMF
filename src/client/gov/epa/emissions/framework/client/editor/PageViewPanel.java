package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.services.Page;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.TableData;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class PageViewPanel extends JPanel implements PageView {

    private EmfTableModel tableModel;

    private JTable table;

    private InternalSource source;

    public PageViewPanel(InternalSource source) {
        this.source = source;
    }

    public void display(Page page) {
        // TODO: refresh table w/ new data?
        super.add(doLayout(page));
    }

    private JPanel doLayout(Page page) {
        JPanel container = new JPanel(new BorderLayout());

        container.add(table(new PageData(source, page)), BorderLayout.CENTER);
        container.add(paginationPanel(), BorderLayout.PAGE_END);

        return container;
    }

    private JScrollPane table(TableData tableData) {
        tableModel = new EmfTableModel(tableData);

        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.setPreferredScrollableViewportSize(new Dimension(500, 200));

        return new JScrollPane(table);
    }

    private JPanel paginationPanel() {
        return new JPanel();
    }

}
