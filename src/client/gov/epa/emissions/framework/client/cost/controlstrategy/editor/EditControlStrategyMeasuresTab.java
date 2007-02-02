package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasureClass;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.ui.ListWidget;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class EditControlStrategyMeasuresTab extends JPanel implements ControlStrategyMeasuresTabView {

    private ListWidget classesList;
    
    private ControlMeasureClass[] allClasses;
    private ControlMeasureClass[] classes;

    private EditControlStrategyMeasuresTabPresenter presenter;

    private ManageChangeables changeablesList;
    private ControlMeasureClass defaultClass = new ControlMeasureClass("All");
    
    public EditControlStrategyMeasuresTab(ControlStrategy controlStrategy, ManageChangeables changeablesList,
            SingleLineMessagePanel messagePanel, EmfConsole parentConsole, EmfSession session) {
        this.changeablesList = changeablesList;
    }

    public void display(ControlStrategy strategy) throws EmfException {
        this.allClasses = presenter.getAllClasses();
        this.classes = presenter.getControlMeasureClasses();
        setupLayout(changeablesList);
    }
    
    private void setupLayout(ManageChangeables changeables) {
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(50,50,0,300));
        this.add(createClassesPanel(changeables), BorderLayout.NORTH);
    }
    
    private JPanel createClassesPanel(ManageChangeables changeables) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JLabel classToInclude = new JLabel("Classes to Include:");

        List allClassesList = new ArrayList(Arrays.asList(allClasses));
        allClassesList.add(0, defaultClass);
        allClasses = (ControlMeasureClass[]) allClassesList.toArray(new ControlMeasureClass[0]);
        if (classes.length == 0) {
            List selClassesList = new ArrayList();
            selClassesList.add(defaultClass);
            classes = (ControlMeasureClass[]) selClassesList.toArray(new ControlMeasureClass[0]);
        }
        this.classesList = new ListWidget(allClasses, classes);
        changeables.addChangeable(classesList);
        JScrollPane pane = new JScrollPane(classesList);
        pane.setPreferredSize(new Dimension(20, 100));
        panel.add(classToInclude, BorderLayout.NORTH);
        panel.add(pane, BorderLayout.CENTER);

        return panel;
    }

    public void save(ControlStrategy controlStrategy) {
        controlStrategy.setControlMeasureClasses(getControlMeasureClasses());
    }

    private ControlMeasureClass[] getControlMeasureClasses() {
        ControlMeasureClass[] controlMeasureClasses = null;
        ControlMeasureClass[] selClasses = Arrays.asList(classesList.getSelectedValues()).toArray(new ControlMeasureClass[0]);

        //make sure we don't include the All class, its just for display purposes,
        //its not stored in the database
        if (selClasses.length != 0 
                && !(selClasses.length == 1 && selClasses[0].equals(defaultClass))) {
            List selClassesList = new ArrayList();
            for (int i = 0; i < selClasses.length; i++)
                if (!selClasses[i].equals(defaultClass)) 
                    selClassesList.add(selClasses[i]);

            controlMeasureClasses = (ControlMeasureClass[]) selClassesList.toArray(new ControlMeasureClass[0]);
        }
        return controlMeasureClasses;
    }


    public void refresh(ControlStrategyResult controlStrategyResult) {
        // NOTE Auto-generated method stub

    }

    public void observe(EditControlStrategyMeasuresTabPresenter presenter) {
        this.presenter = presenter;
    }

}
