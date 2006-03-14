package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ScrollableTextArea;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.data.viewer.TablePresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class DataSortFilterPanel extends JPanel {

    private TextArea rowFilter;

    private TextArea sortOrder;

    private MessagePanel messagePanel;

    private EmfDataset dataset;

    private JPanel actionPanel;

    public DataSortFilterPanel(MessagePanel messagePanel, EmfDataset dataset) {
        this.messagePanel = messagePanel;
        this.dataset = dataset;

        super.setLayout(new BorderLayout(5, 5));
        super.add(sortFilterPanel(), BorderLayout.CENTER);
        super.add(controlPanel(), BorderLayout.EAST);
        super.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    private JPanel sortFilterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1, 5, 5));

        panel.add(sortOrderPanel());
        panel.add(rowFilterPanel());

        return panel;
    }

    private JPanel controlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        actionPanel = new JPanel(new GridLayout(3, 1));
        panel.add(actionPanel);

        return panel;
    }

    private JPanel sortOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(new Label("Sort Order "), BorderLayout.WEST);
        sortOrder = new TextArea("sortOrder", dataset.getDatasetType().getDefaultSortOrder(), 25, 2);
        sortOrder.setToolTipText(sortOrder.getText());
        panel.add(ScrollableTextArea.createWithVerticalScrollBar(sortOrder), BorderLayout.CENTER);

        return panel;
    }

    private JPanel rowFilterPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(new Label("Row Filter  "), BorderLayout.WEST);
        rowFilter = new TextArea("rowFilter", "", 25, 2);
        rowFilter.setToolTipText(rowFilter.getText());
        panel.add(ScrollableTextArea.createWithVerticalScrollBar(rowFilter), BorderLayout.CENTER);

        return panel;
    }

    public void init(final TablePresenter presenter) {
        Button apply = new Button("Apply", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doApplyConstraints(presenter);
            }
        });
        apply.setToolTipText("Apply the Row Filter & Sort Order constraints to the table");
        actionPanel.add(new JLabel(""));
        actionPanel.add(apply);
        actionPanel.add(new JLabel(""));
    }

    private void doApplyConstraints(final TablePresenter presenter) {
        try {
            String rowFilterValue = rowFilter.getText().trim();
            String sortOrderValue = sortOrder.getText().trim();
            presenter.doApplyConstraints(rowFilterValue, sortOrderValue);

            if (rowFilterValue.length() == 0)
                rowFilterValue = "No filter";
            String sortMessage = sortOrderValue;
            if (sortMessage.length() == 0)
                sortMessage = "No sort";

            messagePanel.setMessage("Applied Sort '" + sortMessage + "' and Filter '" + rowFilterValue + "'");
        } catch (EmfException ex) {
            messagePanel.setError(ex.getMessage());
        }
    }

}
