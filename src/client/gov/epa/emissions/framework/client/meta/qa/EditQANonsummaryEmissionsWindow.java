package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class EditQANonsummaryEmissionsWindow extends DisposableInteralFrame implements EditQANonsummaryEmissionsView {

    private AddRemoveDatasetWidget datasetWidget;

    private EmfConsole parentConsole;
    
    private SingleLineMessagePanel messagePanel;

    private JPanel layout;

    private EditQANonsummaryEmissionsPresenter presenter1;

    private EmfSession session;

    private EmfDataset [] datasets; 
    
    private String program;
        
public EditQANonsummaryEmissionsWindow(DesktopManager desktopManager, String program, EmfSession session, EmfDataset [] datasets) {
        
        super("Emissions Inventories Editor", new Dimension(600, 300), desktopManager);
        this.program = program; 
        this.session = session;
        this.datasets = datasets;
        this.getContentPane().add(createLayout());
        
    }

//public EditQANonsummaryEmissionsWindow(DesktopManager desktopManager, EmfSession session) {
//    
//    super("Set Inventories", new Dimension(600, 300), desktopManager);
//
//    this.session = session;
//    this.datasets = null;
//    this.getContentPane().add(createLayout());
//    
//}

    public void display(EmfDataset dataset, QAStep qaStep) {
        super.setTitle("Set Inventories: " + qaStep.getName()+ "_" + qaStep.getId()+" ("+dataset.getName()+")");
        super.display();
    }

    public void observe(EditQANonsummaryEmissionsPresenter presenter1) {
        this.presenter1 = presenter1;
    }
    
    // A JList with Add and Remove buttons for the Emission Inventories.
    // A Text Field for Adding the Inventory Table with a Select button
    // OK and Cancel buttons.
    
    private JPanel createLayout(){
        
        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        JPanel content = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
       
        layoutGenerator.addLabelWidgetPair("Emission inventories:", emisinv(), content);
        layoutGenerator.makeCompactGrid(content, 1, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad*/
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel);
        layout.add(content);
        layout.add(buttonPanel());
        
        return layout;
    }
    
    private JPanel emisinv() {
        datasetWidget = new AddRemoveDatasetWidget(this, program, parentConsole, session);
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
                if (datasetWidget.getDatasets().length == 0){
                    messagePanel.setError("Please select inventories");
                    return; 
                }
                presenter1.updateInventories(datasetWidget.getDatasets());
                dispose();
                disposeView();
            }
    };
    }
}
