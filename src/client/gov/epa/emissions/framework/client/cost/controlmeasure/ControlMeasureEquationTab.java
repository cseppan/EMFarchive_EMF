package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.EditableTable;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureEquation;
import gov.epa.emissions.framework.services.cost.EquationType;
import gov.epa.emissions.framework.ui.EditableEmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

//import java.util.ArrayList;
//import java.util.List;

public class ControlMeasureEquationTab extends JPanel implements ControlMeasureTabView {

    private MessagePanel messagePanel;

//    private ControlMeasure measure;
    private ControlMeasurePresenter controlMeasurePresenter;
    private EmfConsole parent;
    private EmfSession session;
    
    private ControlMeasure measure;
    private Button addButton;
    private Button removeButton;
    private JPanel mainPanel;
    
    
    private EditableEmfTableModel tableModel;
    private CMEquationsTableData tableData;
    private ManageChangeables changeables;
    
    public ControlMeasureEquationTab(ControlMeasure measure, EmfSession session, ManageChangeables changeables,
            MessagePanel messagePanel, EmfConsole parent,
            ControlMeasurePresenter controlMeasurePresenter){
        
        this.mainPanel = new JPanel(new BorderLayout());
        this.parent = parent;
       
        this.session = session; 
        this.messagePanel = messagePanel;
        this.measure = measure;
        this.changeables = changeables;
        this.controlMeasurePresenter = controlMeasurePresenter;
     
//       mainPanel = new JPanel(new BorderLayout());
        doLayout(measure);
        
        super.setName("CMEquation tab");
        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
    }
 

    private void doLayout(ControlMeasure measure){
        mainPanel.setBorder(BorderFactory.createTitledBorder("Equation"));
        mainPanel.add(createTable(measure.getEquations()), BorderLayout.CENTER);
        this.add(mainPanel, BorderLayout.CENTER);
        this.add(buttonPanel(), BorderLayout.SOUTH);
    }

    private void updateMainPanel(ControlMeasureEquation[] equations){
        mainPanel.removeAll();  
        mainPanel.add(createTable(equations), BorderLayout.CENTER);
        mainPanel.validate();

    }
    
    private JScrollPane createTable(ControlMeasureEquation[] cmEquations) {
        
        tableData = new CMEquationsTableData(cmEquations);
        tableModel = new EditableEmfTableModel(tableData);

        EditableTable table = new EditableTable(tableModel);
//        table.setColWidthsBasedOnColNames();
        changeables.addChangeable(table);
 //       table.disableScrolling();
 //       table.setRowHeight(20);
        return new JScrollPane(table);
    }
 
    
    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        addButton = new AddButton(addAction());
        panel.add(addButton);
        removeButton = new RemoveButton(removeAction());
        panel.add(removeButton);

        JPanel container = new JPanel(new BorderLayout());
        container.add(panel, BorderLayout.LINE_START);

        return container;
    }

    private Action addAction() {
        final Component dialogParent  = this;
        return new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                if(tableData.rows().size()>0)  
                    messagePanel.setError("Please remove old equation type to add new one" );
                else {
                    EquationTypeSelectionView view = new EquationTypeSelectionDialog(parent, dialogParent, changeables);
                    EquationTypeSelectionPresenter presenter = new EquationTypeSelectionPresenter(view, session);
                    try {
                        presenter.display();
                        //get Equation Type...
                        EquationType equationType = presenter.getEquationType();
    
                        if (equationType!=null){
                            int n = equationType.getEquationTypeVariables().length;
                            ControlMeasureEquation[] equations;
                            //if equation type have variables...
                            if (n > 0 ) {
                                equations = new ControlMeasureEquation[n];
                                for(int i = 0; i < n; i++){                              
                                    ControlMeasureEquation equation = new ControlMeasureEquation(equationType, equationType.getEquationTypeVariables()[i], 
                                            0.0);
                                    equations[i] = equation;   
                                }
                            //if equation type doesn't have variables...
                            } else {
                                equations = new ControlMeasureEquation[1];
                                ControlMeasureEquation equation = new ControlMeasureEquation(equationType);
                                equations[0] = equation;   
                            }
                            updateMainPanel(equations);
                            tableModel.refresh(tableData);
                        }
                        
                        //messagePanel.setError(equationType.getName());
                        //wrap it with ControlMeasureEquationType...
    //                    cmEquationTypes
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                        messagePanel.setError("Could not return equation type: " + e.getMessage());
                    }
                }
            }
        };
        
    }
    

//    private void addEequation() {
//        EquationType[] equationTypes=getEquationTypes();
//        EquationTypeSelectionView view = new  EquationTypeSelectionView(new EquationType[0]);
//        SectorChooser sectorSelector = new SectorChooser(allSectors, sectorsList, parent);
//        sectorSelector.display();
//        
//    }    

    private Action removeAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
               
                try {
                    messagePanel.clear();
                    doRemove(); 
                    
                } catch (Exception e1) {
                    messagePanel.setError("Could not remove equation type");
                }
            

            }
        };
    }
    private void doRemove(){
 //       ControlMeasureEquation[] equations = new ControlMeasureEquation[]{};
        
        if (tableData.rows().size()==0)
            return; 
        String title = "Warning";
        String message = "Are you sure you want to remove equation type?";
        int selection = JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            if(tableData.rows().size()>0){
                ControlMeasureEquation[] cmEquations=new ControlMeasureEquation[]{};
                refresh(measure);
                updateMainPanel(cmEquations);
                tableModel.refresh(tableData);
                }
            }
    }
    
 
//    private void refreshTable() {
//
//        this.removeAll();
//        doLayout();
//        repaint();
//         
//
//    } 
//    here is an example of how to parse the new objects.
//
//    //get available equations...
//    EquationType[] equationTypes = session.controlMeasureService().getEquationTypes();
//    System.out.println(equationTypes.length);
//    for (int i = 0; i < equationTypes.length; i++) {
//        System.out.println(equationTypes[i].getName() + " " + equationTypes[i].getEquationTypeVariables().length);
//        for (int j = 0; j < equationTypes[i].getEquationTypeVariables().length; j++) {
//
//System.out.println(equationTypes[i].getEquationTypeVariables()[j].getName());
//        }
//    }
//
//    //get measure equations type information...
//    ControlMeasureEquationType[] controlMeasureEquationTypes = measure.getEquationTypes();
//    System.out.println("Number of Equations = " + controlMeasureEquationTypes.length);
//    for (int i = 0; i < controlMeasureEquationTypes.length; i++) {
//        System.out.println("Name = " + controlMeasureEquationTypes[i].getEquationType().getName() + ", Number of variables = " + controlMeasureEquationTypes[i].getControlMeasureEquationTypeVariables().length);
//        for (int j = 0; j < controlMeasureEquationTypes[i].getControlMeasureEquationTypeVariables().length; j++) {
//            System.out.println("Name of variable = " + controlMeasureEquationTypes[i].getControlMeasureEquationTypeVariables()[j].getEquationTypeVariable().getName());
//            System.out.println("Value of variable = " + controlMeasureEq                        for(int i=0; i<n; i++){
//    ControlMeasureEquationTypeVariable controlMeasureEquationTypeVariable = new ControlMeasureEquationTypeVariable(equationType.getEquationTypeVariables()[i], 0.0);
//    variables[i] = controlMeasureEquationTypeVariable;   
//}
//uationTypes[i].getControlMeasureEquationTypeVariables()[j].getValue());
//        } 

//    public void save(ControlMeasure measure) {
//        List sccsList = tableData.rows();
//        sccs = new Scc[sccsList.size()];
//        for (int i = 0; i < sccsList.size(); i++) {
//            ViewableRow row = (ViewableRow) sccsList.get(i);
//            Scc scc = (Scc) row.source();
//            sccs[i] = new Scc(scc.getCode(), "");
//        }
//    }

 
    public void modify() {
        controlMeasurePresenter.doModify();
    }

    public void viewOnly() {
        addButton.setVisible(false);
        removeButton.setVisible(false);
    }




    public void refresh(ControlMeasure measure) {
        
//        measure.setEquationTypes(cmEquationTypes);
       
    }




    public void save(ControlMeasure measure) {
        List<ControlMeasureEquation> equationList = new ArrayList<ControlMeasureEquation>();
        ControlMeasureEquation[] equations = tableData.sources();

        for (int i = 0; i < equations.length; i++) {
            ControlMeasureEquation equation = equations[i];
            equationList.add(equation);
        }
        measure.setEquations(equationList.toArray(new ControlMeasureEquation[0]));
    }

}


