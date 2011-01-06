package gov.epa.emissions.framework.client.data.dataset;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.BorderlessButton;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.CSInventoryEditDialog;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionDialog;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.data.DatasetVersion;
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
import javax.swing.ListSelectionModel;

public class AddRemoveDatasetVersionWidget extends JPanel {

    private EmfConsole parentConsole;
    
    private EmfSession session;
    
//    private SingleLineMessagePanel messagePanel;
    
    private ListWidget datasetVersionsList;
    
    private Button addButton;
    
    private Button removeButton;
    
    private Button setVersionButton;
    
    private boolean selectSingle;

    public AddRemoveDatasetVersionWidget(boolean selectSingle, ManageChangeables changeables, EmfConsole parentConsole, EmfSession session) {
        this.selectSingle = selectSingle;
        this.parentConsole = parentConsole;
        this.session = session;
        setupLayout(changeables);

    }
    
    // called when adding datasets
    public void setDatasetVersions(DatasetVersion[] datasetVersions) {
        for (int i = 0; i < datasetVersions.length; i++) {
            if (!isDuplicateDataset(datasetVersionsList.getModel(),datasetVersions[i]))
               datasetVersionsList.addElement(datasetVersions[i]); 
            else {
                datasetVersionsList.removeElements(new DatasetVersion[] { datasetVersions[i] }); 
                datasetVersionsList.addElement(datasetVersions[i]);
                datasetVersionsList.setSelectedIndex(datasetVersionsList.getLastVisibleIndex());
            }
        }
    }

    private boolean isDuplicateDataset(javax.swing.ListModel model, DatasetVersion datasetVersion) {
        for (int i = 0; i < model.getSize(); i++)
           if (model.getElementAt(i).equals(datasetVersion))
              return true;
        return false;
    }
    
    public void setDatasetVersionsFromStepWindow(DatasetVersion[] datasetVersions) {
        datasetVersionsList.removeElements(datasetVersionsList.getAllElements());
        for (int i = 0; i < datasetVersions.length; i++) {
            datasetVersionsList.addElement(datasetVersions[i]);
        }
    }
    
   public Object[] getDatasetVersions() {
        return datasetVersionsList.getAllElements();
        
    }
   
   private DatasetType getDatasetType(int index) {
       Object[] types = getDatasetVersions();
       if (types == null )
           return null; 
       return (types.length == 0) ? null : ((DatasetVersion)types[index]).getDataset().getDatasetType();
   }
   
    private void setupLayout(ManageChangeables changeables) {
        
        this.datasetVersionsList = new ListWidget(new DatasetVersion[0]);
        this.datasetVersionsList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        changeables.addChangeable(datasetVersionsList);
        
        JScrollPane pane = new JScrollPane(datasetVersionsList);
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
        setVersionButton = new BorderlessButton("Set Version", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                    setVersionAction();
            }
        });

        removeButton = new RemoveButton("Remove", removeAction());
        addButton.setMargin(new Insets(1, 2, 1, 2));      
        removeButton.setMargin(new Insets(1, 2, 1, 2));
        panel.add(addButton);
        panel.add(setVersionButton);
        panel.add(removeButton);

        return panel;
    }

    private Action removeAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
            
                datasetVersionsList.removeSelectedElements();
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

    private void setVersionAction() {
//        messagePanel.clear();
        //get a single selected item
        DatasetVersion selected = (DatasetVersion) datasetVersionsList.getSelectedValue();
        if (selected == null) {
//            messagePanel.setMessage("Please select dataset to set its version.");
            return;
        }

        //Show select version dialog
        SetDatasetVersionDialog dialog = new SetDatasetVersionDialog(parentConsole, selected.getDataset());
        SetDatasetVersionPresenter presenter = new SetDatasetVersionPresenter(this, dialog, session);
        presenter.display();
    }
    

    
    private void doAddWindow() {
        List<DatasetType> datasetTypeList = new ArrayList<DatasetType>();
//        boolean selectSingle = false; 
        try {
            // FIXME: really, we don't want to contact the server to get the dataset types - could be slow
            DatasetType[] allDatasetTypes = session.dataCommonsService().getDatasetTypes();
            
            // Make an object of the view and presenter of the dialog, and run the presenter's display ().
            // Set the list of datasets in the JList of this widget (which is part of the EditQAEmissionsWindow
            // to that of the datasets retrieved from the presenter.
            InputDatasetSelectionDialog view = new InputDatasetSelectionDialog (parentConsole);
            InputDatasetSelectionPresenter presenter = new InputDatasetSelectionPresenter(view, session, allDatasetTypes);
            presenter.display(getDatasetType(0), this.selectSingle);
            List<DatasetVersion> datasetVersions = new ArrayList<DatasetVersion>();
            for (EmfDataset dataset : presenter.getDatasets()) {
                datasetVersions.add(new DatasetVersion(dataset, getVersion(dataset.getId(), dataset.getDefaultVersion())));
            }
            setDatasetVersions(datasetVersions.toArray(new DatasetVersion[0]));

        } catch (Exception e) {
            e.printStackTrace();
 //            messagePanel.setError(e.getMessage());
        }
    }
    
    public Version getVersion(int datasetId, int version) throws EmfException {
        try {
            Version[] versions = session.dataEditorService().getVersions(datasetId);
            for (Version v : versions) {
                if (v.getVersion() == version)
                    return v;
            }
            return null;
        } finally {
            //
        }
    }

    public void viewOnly() {
        addButton.setVisible(false);
        removeButton.setVisible(false);  
    }

    public void setSelectionMode(int singleSelection) {
        datasetVersionsList.setSelectionMode(singleSelection);
    }
}