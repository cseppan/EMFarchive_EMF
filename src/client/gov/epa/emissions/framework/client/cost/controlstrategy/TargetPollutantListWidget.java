package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.ui.ListWidget;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class TargetPollutantListWidget extends JPanel {

    private ListWidget pollutantsList;

    private Pollutant[] allPollutants;

    private EmfConsole parentConsole;
    
    private Button addButton;
    private Button removeButton;

    public TargetPollutantListWidget(Pollutant[] allPollutants, ManageChangeables changeables, EmfConsole parentConsole) {
        this.allPollutants = allPollutants;
        this.parentConsole = parentConsole;
        setupLayout(changeables);

    }

    public void setPollutants(Pollutant[] pollutants) {
        if (pollutants != null && pollutants.length > 0) {
            for (int i = 0; i < pollutants.length; i++) {
                pollutantsList.addElement(pollutants[i]);
            }
        }
    }

    public Pollutant[] getPollutants() {
        return Arrays.asList(pollutantsList.getAllElements()).toArray(new Pollutant[0]);
    }

    private void setupLayout(ManageChangeables changeables) {
        this.pollutantsList = new ListWidget(new Pollutant[0]);
        changeables.addChangeable(pollutantsList);
        
        JScrollPane pane = new JScrollPane(pollutantsList);
        JPanel buttonPanel = addRemoveButtonPanel();

        this.setLayout(new BorderLayout(1, 1));
        this.add(pane, BorderLayout.WEST);
        this.add(buttonPanel, BorderLayout.EAST);
    }

    private JPanel addRemoveButtonPanel() {
        JPanel panel = new JPanel(new BorderLayout(1, 1));
        addButton = new AddButton("Add", addAction());
        removeButton = new RemoveButton("Remove", removeAction());
        addButton.setMargin(new Insets(1, 2, 1, 2));      
        removeButton.setMargin(new Insets(1, 2, 1, 2));
        panel.add(addButton, BorderLayout.NORTH);
        panel.add(removeButton, BorderLayout.SOUTH);

        return panel;
    }

    private Action removeAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                removePollutants();
            }
        };
    }

    private Action addAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                addMonths();
            }
        };
    }

    private void addMonths() {
        TargetPollutantChooser pollutantSelector = new TargetPollutantChooser(allPollutants, pollutantsList, parentConsole);
        pollutantSelector.display();
    }

    private void removePollutants() {
        pollutantsList.removeElements(pollutantsList.getSelectedValues());
    }

    public void viewOnly() {
        addButton.setVisible(false);
        removeButton.setVisible(false);  
    }
}
