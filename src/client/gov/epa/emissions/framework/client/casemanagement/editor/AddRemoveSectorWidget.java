package gov.epa.emissions.framework.client.casemanagement.editor;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.ui.ListWidget;

public class AddRemoveSectorWidget extends JPanel {

    private ListWidget sectorsList;

    private Sector[] allSectors;

    private EmfConsole parentConsole;

    public AddRemoveSectorWidget(Sector[] allSectors, ManageChangeables changeables, EmfConsole parentConsole) {
        this.allSectors = allSectors;
        this.parentConsole = parentConsole;
        setupLayout(changeables);

    }

    public void setSectors(Sector[] sectors) {
        for (int i = 0; i < sectors.length; i++) {
            sectorsList.addElement(sectors[i]);
        }
    }

    public Sector[] getSectors() {
        return (Sector[]) Arrays.asList(sectorsList.getAllElements()).toArray(new Sector[0]);
    }

    private void setupLayout(ManageChangeables changeables) {
        this.sectorsList = new ListWidget(new Sector[0]);
        changeables.addChangeable(sectorsList);
        
        JScrollPane pane = new JScrollPane(sectorsList);
        JPanel buttonPanel = addRemoveButtonPanel();

        this.setLayout(new BorderLayout(5, 5));
        this.add(pane);
        this.add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel addRemoveButtonPanel() {
        JPanel panel = new JPanel();
        Button addButton = new AddButton("Add", addAction());
        Button removeButton = new RemoveButton("Remove", removeAction());

        panel.add(addButton);
        panel.add(removeButton);

        return panel;
    }

    private Action removeAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                removeSectors();
            }
        };
    }

    private Action addAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                addSectors();
            }
        };
    }

    private void addSectors() {
        SectorChooser sectorSelector = new SectorChooser(allSectors, sectorsList, parentConsole);
        sectorSelector.display();
    }

    private void removeSectors() {
        Object[] removeValues = sectorsList.getSelectedValues();
        sectorsList.removeElements(removeValues);

    }

}
