package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class EditQAEmissionsColumnBasedWindow extends EditQAEmissionsWindow implements EditQAEmissionsView {
    
    private ComboBox emissionTypes;
    
    private String emissionType; 
    
    public EditQAEmissionsColumnBasedWindow(DesktopManager desktopManager, String program, EmfSession session, 
            EmfDataset[] inventories, EmfDataset [] invTables, String summaryType, String emissionType) {
        super(desktopManager, program, session, inventories, invTables, summaryType);
        this.emissionType = emissionType; 
        this.getContentPane().add(createLayout());
        
    }


    public void display(EmfDataset dataset, QAStep qaStep) {
        super.setTitle("Setup "+qaStep.getName()+": " + dataset.getName() + "_" + qaStep.getId() );
        super.display();
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
        emissionTypeCombo();
        layoutGenerator.addLabelWidgetPair("Emission Type:", emissionTypes, content);
        
        summaryTypeCombo();
        layoutGenerator.addLabelWidgetPair("Summary Type:", summaryTypes, content);
        layoutGenerator.makeCompactGrid(content, 4, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad*/
        layout.add(content);
        layout.add(buttonPanel());
        
        return layout;
    }
    
    
    private void emissionTypeCombo() {
        String [] values= new String[]{"Annual emissions", "Average day emissions"};
        emissionTypes = new ComboBox("Not Selected", values);
        emissionTypes.setPreferredSize(new Dimension(350, 25));
        if(!(emissionType==null) && (emissionType.trim().length()>0)){
            if (emissionType.trim().startsWith("Average"))
            emissionTypes.setSelectedItem("Average day emissions");
            if (emissionType.trim().startsWith("Annual"))
                emissionTypes.setSelectedItem("Annual emissions");
        }
        emissionTypes.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                emissionTypes.getSelectedItem();
            }
        });
    }

    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        panel.add(new OKButton(okAction()));
        panel.add(new CancelButton(cancelAction()));
        return panel;
    }
    

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                
                //System.out.println(invTable.getDataset());
                presenter1.updateInventories(datasetWidget.getDatasets(), getInvTableDatasets(), getSummaryType(), getEmissionType() );
                dispose();
                disposeView();
            }
        };
    }
    
    
    
   private String getEmissionType(){
       if (emissionTypes.getSelectedItem()==null)
           return ""; 
       return emissionTypes.getSelectedItem().toString();
   }
   
}
