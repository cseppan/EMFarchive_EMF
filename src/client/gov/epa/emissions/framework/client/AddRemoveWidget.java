package gov.epa.emissions.framework.client;

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

public class AddRemoveWidget extends JPanel {

    private ListWidget objectsList;

    private Object[] allObjects;

    private EmfConsole parentConsole;
    
    private Button addButton;
    private Button removeButton;

    public AddRemoveWidget(Object[] allObjects, ManageChangeables changeables, EmfConsole parentConsole) {
        this.allObjects = allObjects;
        this.parentConsole = parentConsole;
        setupLayout(changeables);

    }

    public void setObjects(Object[] objects) {
        for (int i = 0; i < objects.length; i++) {
            objectsList.addElement(objects[i]);
        }
    }

    public Object[] getObjects() {
        return Arrays.asList(objectsList.getAllElements()).toArray(new Object[0]);
    }

    private void setupLayout(ManageChangeables changeables) {
        this.objectsList = new ListWidget(new Object[0]);
        changeables.addChangeable(objectsList);
        
        JScrollPane pane = new JScrollPane(objectsList);
        JPanel buttonPanel = addRemoveButtonPanel();

        this.setLayout(new BorderLayout(1, 1));
        this.add(pane);
        this.add(buttonPanel, BorderLayout.SOUTH);
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
                removeObjects();
            }
        };
    }

    private Action addAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                addObjects();
            }
        };
    }

    private void addObjects() {
        ObjectChooser objectSelector = new ObjectChooser("Jobs", allObjects, objectsList, parentConsole);
        objectSelector.display();
    }

    private void removeObjects() {
        Object[] removeValues = objectsList.getSelectedValues();
        objectsList.removeElements(removeValues);

    }

    public void viewOnly() {
        addButton.setVisible(false);
        removeButton.setVisible(false);  
    }
}
