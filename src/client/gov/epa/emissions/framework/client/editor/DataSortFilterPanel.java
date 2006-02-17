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
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class DataSortFilterPanel extends JPanel {

    private TextArea rowFilter;

    private TextArea sortOrder;

    private MessagePanel messagePanel;
    
    private ManageChangeables listOfChangeables;
    
    private EmfDataset dataset;

    public DataSortFilterPanel(MessagePanel messagePanel, ManageChangeables listOfChangeables, EmfDataset dataset) {
        this.listOfChangeables = listOfChangeables;
        this.messagePanel = messagePanel;
        this.dataset = dataset;
        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        super.add(rowFilterPanel());
        super.add(sortOrderPanel());
    }

    private JPanel actionPanel(final TablePresenter presenter, final MessagePanel messagePanel) {
        JPanel panel = new JPanel(new BorderLayout());
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
        panel.add(apply, BorderLayout.LINE_END);

        return panel;
    }

    private JPanel sortOrderPanel() {
        JPanel panel = new JPanel();

        panel.add(new Label("Sort Order"));
        sortOrder = new TextArea("sortOrder", dataset.getDatasetType().getDefaultSortOrder(), 30, 2);
        sortOrder.setToolTipText(sortOrder.getText());
        if(listOfChangeables != null) {
            listOfChangeables.addChangeable(sortOrder);
            sortOrder.addTextListener();
        }
        panel.add(new ScrollableTextArea(sortOrder));

        return panel;
    }

    private JPanel rowFilterPanel() {
        JPanel panel = new JPanel();

        panel.add(new Label("Row Filter"));
        rowFilter = new TextArea("rowFilter", "", 30, 2);
        rowFilter.setToolTipText(rowFilter.getText());
        if(listOfChangeables != null) {
            listOfChangeables.addChangeable(rowFilter);
            rowFilter.addTextListener();
        }
        panel.add(new ScrollableTextArea(rowFilter));

        return panel;
    }

    public void init(TablePresenter presenter) {
        super.add(actionPanel(presenter, messagePanel));
        super.add(Box.createVerticalStrut(10));
    }

}
