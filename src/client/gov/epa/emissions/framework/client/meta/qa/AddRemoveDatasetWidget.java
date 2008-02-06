package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionDialog;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionPresenter;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.ListWidget;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class AddRemoveDatasetWidget extends JPanel {

    private EmfConsole parentConsole;
    
    private EmfSession session;
    
//    private SingleLineMessagePanel messagePanel;
    
    private ListWidget datasetsList;
    
    private Button addButton;
    
    private Button removeButton;
    
    private ManageChangeables changeables;

    public AddRemoveDatasetWidget(ManageChangeables changeables, EmfConsole parentConsole, EmfSession session) {
        this.parentConsole = parentConsole;
        this.changeables = changeables;
        this.session = session;
        setupLayout(changeables);

    }
    
    public void setDatasets(EmfDataset [] datasets) {
        for (int i = 0; i < datasets.length; i++) {
            datasetsList.addElement(datasets[i]);
        }
    }
    
    public void setDatasetsFromStepWindow(EmfDataset [] datasets) {
        datasetsList.removeElements(datasetsList.getAllElements());
        for (int i = 0; i < datasets.length; i++) {
            datasetsList.addElement(datasets[i]);
        }
    }
    
   public Object[] getDatasets() {
        return datasetsList.getAllElements();
        
    }
   
   private DatasetType getDatasetType(int index) {
       Object[] types = getDatasets();
       if (types == null )
           return null; 
       return (types.length == 0) ? null : ((EmfDataset)types[index]).getDatasetType();
   }
   
    private void setupLayout(ManageChangeables changeables) {
        
        this.datasetsList = new ListWidget(new EmfDataset[0]);
        changeables.addChangeable(datasetsList);
        
        JScrollPane pane = new JScrollPane(datasetsList);
        pane.setPreferredSize(new Dimension(500, 300));
        JPanel buttonPanel = addRemoveButtonPanel();
        //JLabel emisLabel = new JLabel("Emission Inventories");
        this.setLayout(new BorderLayout(1, 1));
        //this.add(emisLabel, BorderLayout.WEST);
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
            
                datasetsList.removeSelectedElements();
                //System.out.println(parentConsole);
            }
        };
    }

    private Action addAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doAddWindow();
            }
        };
    }

    private void doAddWindow() {
        List<DatasetType> datasetTypeList = new ArrayList<DatasetType>();
        try {
            DatasetType[] allDatasetTypes = session.dataCommonsService().getDatasetTypes();
            for (int i = 0; i < allDatasetTypes.length; i++) {
                if (
                        
                        //get all dataset types that start with ORL
                        allDatasetTypes[i].getName().startsWith("ORL")
                    )
                    {
                    datasetTypeList.add(allDatasetTypes[i]);
                }
            }
            
            // Make an object of the view and presenter of the dialog, and run the presenter's display ().
            // Set the list of datasets in the JList of this widget (which is part of the EditQAEmissionsWindow
            // to that of the datasets retrived from the presenter.
            InputDatasetSelectionDialog view = new InputDatasetSelectionDialog (parentConsole, changeables);
            InputDatasetSelectionPresenter presenter = new InputDatasetSelectionPresenter(view, session, datasetTypeList.toArray(new DatasetType[0]));
            presenter.display(getDatasetType(0));
            setDatasets(presenter.getDatasets());

        } catch (Exception e) {
            e.printStackTrace();
 //            messagePanel.setError(e.getMessage());
        }
    }
    
    public void viewOnly() {
        addButton.setVisible(false);
        removeButton.setVisible(false);  
    }
}
