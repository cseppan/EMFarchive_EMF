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
    
    public EditControlStrategyMeasuresTab(ControlStrategy controlStrategy, ManageChangeables changeablesList,
            SingleLineMessagePanel messagePanel, EmfConsole parentConsole, EmfSession session) {
        this.changeablesList = changeablesList;
    }

    public void display(ControlStrategy strategy) throws EmfException {
        this.allClasses = presenter.getAllClasses();
        this.classes = presenter.getControlMeasureClasses(strategy.getId());
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
        this.classesList = new ListWidget(allClasses, classes);//
        changeables.addChangeable(classesList);
        JScrollPane pane = new JScrollPane(classesList);
        pane.setPreferredSize(new Dimension(20, 80));
        panel.add(classToInclude, BorderLayout.NORTH);
        panel.add(pane, BorderLayout.CENTER);
        
        return panel;
    }

    public void save(ControlStrategy controlStrategy) {
        controlStrategy.setControlMeasureClasses(getControlMeasureClasses());
    }

    private ControlMeasureClass[] getControlMeasureClasses() {
        List selectedValues = Arrays.asList(classesList.getSelectedValues());
        return (ControlMeasureClass[]) selectedValues.toArray(new ControlMeasureClass[0]);
    }


    public void refresh(ControlStrategyResult controlStrategyResult) {
        // NOTE Auto-generated method stub

    }

    public void observe(EditControlStrategyMeasuresTabPresenter presenter) {
        this.presenter = presenter;
    }

}
