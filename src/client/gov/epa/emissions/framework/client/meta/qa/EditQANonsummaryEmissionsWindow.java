package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.client.DisposableInteralFrame;
//import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
//import gov.epa.emissions.commons.data.DatasetType;
//import gov.epa.emissions.commons.gui.Button;
//import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
//import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionDialog;
//import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionPresenter;
//import gov.epa.emissions.framework.ui.ListWidget;
//import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

//import java.awt.BorderLayout;
import java.awt.Dimension;
//import java.awt.Insets;
import java.awt.event.ActionEvent;
//import java.util.ArrayList;
//import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
//import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

public class EditQANonsummaryEmissionsWindow extends DisposableInteralFrame implements EditQANonsummaryEmissionsView {

private AddRemoveDatasetWidget datasetWidget;
    
    private EmfConsole parentConsole;
    
    private JPanel layout;
    
    private EditQANonsummaryEmissionsPresenter presenter1;
    
    private EmfSession session;
    
    private EmfDataset [] datasets; 
    
    private SingleLineMessagePanel messagePanel;
        
public EditQANonsummaryEmissionsWindow(DesktopManager desktopManager, EmfSession session, EmfDataset [] datasets,  SingleLineMessagePanel messagePanel) {
        
        super("Emissions Inventories Editor", new Dimension(600, 300), desktopManager);

        this.session = session;
        this.datasets = datasets;
        this.messagePanel = messagePanel;
        this.getContentPane().add(createLayout());
        
    }

public EditQANonsummaryEmissionsWindow(DesktopManager desktopManager, EmfSession session) {
    
    super("Emissions Inventories Editor", new Dimension(600, 300), desktopManager);

    this.session = session;
    this.datasets = null;
    this.getContentPane().add(createLayout());
    
}

    public void display(EmfDataset dataset, QAStep qaStep) {
        super.setTitle("EditQANonsummaryEmissionsEditor: " + dataset.getName() + "_" + qaStep.getId() );
        super.display();
    }

    public void observe(EditQANonsummaryEmissionsPresenter presenter1) {
        this.presenter1 = presenter1;
    }
    
    // A JList with Add and Remove buttons for the Emission Inventories.
    // A Text Field for Adding the Inventory Table with a Select button
    // OK and Cancel buttons.
    
    public JPanel createLayout(){
        
        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        JPanel content = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
       
        layoutGenerator.addLabelWidgetPair("Emission inventories:", emisinv(), content);
        layoutGenerator.makeCompactGrid(content, 1, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad*/
        layout.add(content);
        layout.add(buttonPanel());
        
        return layout;
    }
    
    private JPanel emisinv() {
        datasetWidget = new AddRemoveDatasetWidget(this, parentConsole, session);
        datasetWidget.setPreferredSize(new Dimension(350,250));
        if(!(datasets==null) && (datasets.length > 0))
            datasetWidget.setDatasetsFromStepWindow(datasets);
        return datasetWidget;
    }
    
    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        panel.add(new OKButton(okAction()));
        panel.add(new CancelButton(cancelAction()));
        return panel;
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
                EmfDataset [] newDatasets = new EmfDataset [datasetWidget.getDatasets().length];
                for (int k =0; k < datasetWidget.getDatasets().length; k++) {
                    newDatasets[k] = (EmfDataset) datasetWidget.getDatasets()[k];
                }
                // Get the dataset type for the first dataset. Set firstElementDatasetType to that value.
                 
                DatasetType firstElementDatasetType = newDatasets[0].getDatasetType();
                //System.out.println("Dataset 1 type: " + newDatasets[0]);
                // Go through the rest of the datasets.  If the dataset type for any of them is not
                // the same as in firstElementContains, throw an exception.
                   for (int j = 1; j < newDatasets.length; j++) {
                         //System.out.println("Dataset " + (j + 1) + " type: " + newDatasets[j]);
                         if (!(newDatasets[j].getDatasetType().equals(firstElementDatasetType)))
                             messagePanel.setError("There is a mismatch of inventory dataset types.");
                  }
                
                presenter1.updateDatasets(datasetWidget.getDatasets());
                dispose();
                disposeView();
            }
        };
    }
}
