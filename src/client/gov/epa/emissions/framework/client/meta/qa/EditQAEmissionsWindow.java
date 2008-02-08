package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionDialog;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionPresenter;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.ListWidget;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

public class EditQAEmissionsWindow extends DisposableInteralFrame implements EditQAEmissionsView {
    
    private AddRemoveDatasetWidget datasetWidget;
    
    private EmfConsole parentConsole;
    
    private JPanel layout;
    
    private EditQAEmissionsPresenter presenter1;
    
    private ListWidget invTable;
    
    private EmfSession session;
    
    private SingleLineMessagePanel messagePanel;
    
    private Button addButton;
    
    private EmfDataset[] inventories;
    
    private EmfDataset[] invTables;
        
    public EditQAEmissionsWindow(DesktopManager desktopManager, EmfSession session, EmfDataset[] inventories, EmfDataset [] invTables) {
        
        super("Emissions Inventories Editor", new Dimension(600, 300), desktopManager);

        this.session = session;
        this.inventories = inventories;
        this.invTables = invTables;
        this.getContentPane().add(createLayout());
        
    }

    public void display(EmfDataset dataset, QAStep qaStep) {
        super.setTitle("Setup "+qaStep.getName()+": " + dataset.getName() + "_" + qaStep.getId() );
        super.display();
    }

    public void observe(EditQAEmissionsPresenter presenter1) {
        this.presenter1 = presenter1;
    }
    
    // A JList with Add and Remove buttons for the Emission Inventories.
    // A Text Field for Adding the Inventory Table with a Select button
    // OK and Cancel buttons.

    private JPanel createLayout() {
        
        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        JPanel content = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
       
        layoutGenerator.addLabelWidgetPair("Emission inventories:", emisinv(), content);
        layoutGenerator.addLabelWidgetPair("Inventory table:", invTablePanel(), content);
        layoutGenerator.makeCompactGrid(content, 2, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad*/
        layout.add(content);
        layout.add(buttonPanel());
        
        return layout;
    }
    
    private JPanel emisinv() {
        datasetWidget = new AddRemoveDatasetWidget(this, parentConsole, session);
        datasetWidget.setPreferredSize(new Dimension(350,250));
        if(inventories != null && inventories.length > 0)
            datasetWidget.setDatasetsFromStepWindow(inventories);
        return datasetWidget;
    }
    
    private JPanel invTablePanel() {
        
        invTable = new ListWidget(new EmfDataset[0]);
        if(!(invTables==null) && (invTables.length > 0))
            setDatasetsFromStepWindow(invTables);
        
        JScrollPane pane = new JScrollPane(invTable);
        pane.setPreferredSize(new Dimension(350, 25));
        invTable.setToolTipText("The inventory table dataset.  Press select button to choose from a list.");
       
        addButton = new AddButton("Select", addAction());
        addButton.setMargin(new Insets(1, 2, 1, 2));
        
        JPanel invPanel = new JPanel(new BorderLayout(5,0));
        
        invPanel.add(pane, BorderLayout.LINE_START);
        invPanel.add(addButton);
        return invPanel;
    }
    
    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        panel.add(new OKButton(okAction()));
        panel.add(new CancelButton(cancelAction()));
        return panel;
    }
    
    private Action addAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doAddWindow();
            }
        };
    }
  

    private Action cancelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                dispose();
                disposeView();
            }

        };
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                
                //System.out.println(invTable.getDataset());
                presenter1.updateInventories(datasetWidget.getDatasets(), getInvTableDatasets());
                dispose();
                disposeView();
            }
        };
    }
    
    private void doAddWindow() {
        List<DatasetType> datasetTypeList = new ArrayList<DatasetType>();
        try {
            DatasetType[] allDatasetTypes = session.dataCommonsService().getDatasetTypes();
            for (int i = 0; i < allDatasetTypes.length; i++) {
             // Only get the dataset type INVTABLE
                if (allDatasetTypes[i].getName().equals("Inventory Table Data (INVTABLE)"))
                    datasetTypeList.add(allDatasetTypes[i]);
            }
            DatasetType[] datasetTypes = datasetTypeList.toArray(new DatasetType[0]);
            InputDatasetSelectionDialog view = new InputDatasetSelectionDialog (parentConsole, this);
            InputDatasetSelectionPresenter presenter = new InputDatasetSelectionPresenter(view, session, datasetTypeList.toArray(new DatasetType[0]));
            if (datasetTypes.length == 1)
                presenter.display(datasetTypes[0]);
            else
                presenter.display(null);
            setDatasets(presenter.getDatasets());
        } catch (Exception e) {
             messagePanel.setError(e.getMessage());
        }
    }
    
    private void setDatasetsFromStepWindow(EmfDataset [] datasets){
        invTable.removeAll();
        for (int i = 0; i < datasets.length; i++) {
            //System.out.println(" Inv dataset is: " + datasets[i]);
            invTable.addElement(datasets[i]);
        }
    }
    
    private void setDatasets(EmfDataset [] datasets) {
        for (int i = 0; i < datasets.length; i++) {
           //System.out.println(" Inv dataset is: " + datasets[i]);
           invTable.addElement(datasets[i]);
        }
        
    }
    
   private Object[] getInvTableDatasets() {
        return invTable.getAllElements();
   }
   
}
