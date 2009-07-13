package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.data.GeoRegion;
import gov.epa.emissions.framework.ui.ListWidget;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class AddRemoveRegionsWidget extends JPanel {
    private ListWidget regionsList;
    private GeoRegion[] allRegions;
    private EmfConsole parentConsole;
    private Button addButton;
    private Button removeButton;

    public AddRemoveRegionsWidget(GeoRegion[] allGrids, ManageChangeables changeables, EmfConsole parentConsole) {
        this.allRegions = allGrids;
        this.parentConsole = parentConsole;
        setupLayout(changeables);

    }

    public AddRemoveRegionsWidget(GeoRegion[] allSectors) {
        setupLayout();

    }
    public void setRegions(GeoRegion[] grids) {
        regionsList.removeAllElements();   
        Arrays.sort(grids);
        for (int i = 0; i < grids.length; i++) {
            regionsList.addElement(grids[i]);
        }
    }
    
    public GeoRegion[] getRegions() {
        return Arrays.asList(regionsList.getAllElements()).toArray(new GeoRegion[0]);
    }

    private void setupLayout(ManageChangeables changeables) {
        this.regionsList = new ListWidget(new GeoRegion[0]);
        changeables.addChangeable(regionsList);
        
        JScrollPane pane = new JScrollPane(regionsList);
        JPanel buttonPanel = addRemoveButtonPanel();

        this.setLayout(new BorderLayout(1, 1));
        this.add(pane);
        this.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupLayout() {
        this.regionsList = new ListWidget(new GeoRegion[0]);
        
        JScrollPane pane = new JScrollPane(regionsList);

        this.setLayout(new BorderLayout(1, 1));
        this.add(pane);
    }


    private JPanel addRemoveButtonPanel() {
        JPanel panel = new JPanel();
        addButton = new AddButton("Add", addAction());
        removeButton = new RemoveButton("Remove", removeAction());
        addButton.setMargin(new Insets(1, 2, 1, 2));      
        removeButton.setMargin(new Insets(1, 2, 1, 2));
        panel.add(addButton);
        panel.add(removeButton);

        return panel;
    }

    private Action removeAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                removeGrids();
            }
        };
    }

    private Action addAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                addGrids();
            }
        };
    }

    private void addGrids() {
        RegionChooser gridSelector = new RegionChooser(allRegions, regionsList, parentConsole);
        gridSelector.display();
    }

    private void removeGrids() {
        Object[] removeValues = regionsList.getSelectedValues();
        regionsList.removeElements(removeValues);

    }

    public void viewOnly() {
        addButton.setVisible(false);
        removeButton.setVisible(false);  
    }
    
    public void addGrid(GeoRegion grid){
        if (!regionsList.contains(grid))
            regionsList.addElement(grid);
        sort(); 
    }
    
    private void sort() {
        GeoRegion[] grids = Arrays.asList(regionsList.getAllElements()).toArray(new GeoRegion[0]);
        
        if (grids == null || grids.length == 0)
            return;
        
        Arrays.sort(grids);
        regionsList.removeAllElements();
        
        for (int i = 0; i < grids.length; i++) {
            regionsList.addElement(grids[i]);
        }
    }
   

}
