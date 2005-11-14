package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.services.Page;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.IconButton;
import gov.epa.emissions.framework.ui.ImageResources;
import gov.epa.emissions.framework.ui.ScrollableTable;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

public class PageViewPanel extends JPanel implements PageView {

    private EmfTableModel tableModel;

    private InternalSource source;

    private JPanel layout;

    private PageViewPresenter presenter;

    private MessagePanel messagePanel;

    private TextField pageInput;

    public PageViewPanel(InternalSource source, MessagePanel messagePanel) {
        super(new BorderLayout());
        this.source = source;
        this.messagePanel = messagePanel;

        layout = new JPanel(new BorderLayout());
        super.add(layout, BorderLayout.CENTER);

        layout.add(paginationPanel(), BorderLayout.PAGE_START);
    }

    public void observe(PageViewPresenter presenter) {
        this.presenter = presenter;
    }

    public void display(Page page) {
        // TODO: refresh table w/ new data? or remove and add back the table?

        tableModel = new EmfTableModel(new PageData(source, page));
        JScrollPane table = new ScrollableTable(tableModel);
        layout.add(table, BorderLayout.CENTER);
    }

    private JPanel paginationPanel() {
        JPanel container = new JPanel(new BorderLayout());

        JPanel panel = new JPanel();
        JLabel current = new JLabel("Current: ");
        panel.add(current);

        JLabel total = new JLabel("Total Records: ");
        panel.add(total);

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        addButtons(toolbar);
        panel.add(toolbar);

        container.add(panel, BorderLayout.LINE_END);

        return container;
    }

    private void addButtons(JToolBar toolBar) {
        ImageResources res = new ImageResources();

        toolBar.add(firstButton(res));
        toolBar.add(prevButton(res));
        pageInput = new TextField("page", 7);
        toolBar.add(pageInput);
        toolBar.add(nextButton(res));
        toolBar.add(lastButton(res));
    }

    private IconButton lastButton(ImageResources res) {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    presenter.doDisplayLast();
                } catch (EmfException e) {
                    messagePanel.setError("Could not display Last Page. Reason: " + e.getMessage());
                }

            }
        };
        return new IconButton("Last", "Go to Last Page", res.last("Go to Last Page"), action);
    }

    private IconButton nextButton(ImageResources res) {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    presenter.doDisplayNext();
                } catch (EmfException e) {
                    messagePanel.setError("Could not display Next Page. Reason: " + e.getMessage());
                }
            }
        };
        return new IconButton("Next", "Go to Next Page", res.next("Go to Next Page"), action);
    }

    private IconButton prevButton(ImageResources res) {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    presenter.doDisplayPrevious();
                } catch (EmfException e) {
                    messagePanel.setError("Could not display Previous Page. Reason: " + e.getMessage());
                }
            }
        };
        return new IconButton("Prev", "Go to Previous Page", res.prev("Go to Previous Page"), action);
    }

    private IconButton firstButton(ImageResources res) {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    presenter.doDisplayFirst();
                } catch (EmfException e) {
                    messagePanel.setError("Could not display First Page. Reason: " + e.getMessage());
                }
            }
        };
        return new IconButton("First", "Go to First Page", res.first("Go to First Page"), action);
    }

}
