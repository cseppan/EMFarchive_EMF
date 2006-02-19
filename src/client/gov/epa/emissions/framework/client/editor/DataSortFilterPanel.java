package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableTextArea;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class DataSortFilterPanel extends JPanel {

    private TextArea rowFilter;

    private TextArea sortOrder;

    private MessagePanel messagePanel;

    private ManageChangeables listOfChangeables;

    private EmfDataset dataset;

    private JPanel actionPanel;

    public DataSortFilterPanel(MessagePanel messagePanel, ManageChangeables listOfChangeables, EmfDataset dataset) {
        this.listOfChangeables = listOfChangeables;
        this.messagePanel = messagePanel;
        this.dataset = dataset;

        super.add(sortFilterPanel());
        super.add(controlPanel());
    }

    private JPanel sortFilterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(sortOrderPanel());
        panel.add(rowFilterPanel());

        return panel;
    }

    private JPanel controlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        actionPanel = new JPanel(new BorderLayout());
        panel.add(actionPanel);

        return panel;
    }

    private JPanel sortOrderPanel() {
        JPanel panel = new JPanel();

        panel.add(new Label("Sort Order"));
        sortOrder = new TextArea("sortOrder", dataset.getDatasetType().getDefaultSortOrder(), 30, 2);
        sortOrder.setToolTipText(sortOrder.getText());
        if (listOfChangeables != null) {
            listOfChangeables.addChangeable(sortOrder);
            sortOrder.addTextListener();
        }
        panel.add(ScrollableTextArea.createWithVerticalScrollBar(sortOrder));

        return panel;
    }

    private JPanel rowFilterPanel() {
        JPanel panel = new JPanel();

        panel.add(new Label("Row Filter"));
        rowFilter = new TextArea("rowFilter", "", 30, 2);
        rowFilter.setToolTipText(rowFilter.getText());
        if (listOfChangeables != null) {
            listOfChangeables.addChangeable(rowFilter);
            rowFilter.addTextListener();
        }
        panel.add(ScrollableTextArea.createWithVerticalScrollBar(rowFilter));

        return panel;
    }

    public void init(final TablePresenter presenter) {
        Button apply = new Button("Apply", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    presenter.doApplyConstraints(rowFilter.getText(), sortOrder.getText());
                    messagePanel.setMessage("Constraints applied - Row Filter(" + rowFilter.getText()
                            + "}, Sort Order(" + sortOrder.getText() + ")");
                } catch (EmfException ex) {
                    messagePanel.setError(ex.getMessage());
                }
            }
        });
        apply.setToolTipText("Apply the Row Filter & Sort Order constraints to the table");
        actionPanel.add(apply, BorderLayout.LINE_END);
    }

}
