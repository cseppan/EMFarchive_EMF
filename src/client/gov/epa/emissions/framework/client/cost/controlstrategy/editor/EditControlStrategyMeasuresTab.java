package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.ui.ListWidget;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class EditControlStrategyMeasuresTab extends JPanel implements EditControlStrategyTabView {

    private ListWidget classesList;
    
    private String[] classes = { "Known", "Emerging", "Hypothetical", "Obselete" };
    
    public EditControlStrategyMeasuresTab(ControlStrategy controlStrategy, ManageChangeables changeablesList,
            SingleLineMessagePanel messagePanel, EmfConsole parentConsole, EmfSession session) throws EmfException {
        setupLayout(changeablesList);
        
        if (false)
            throw new EmfException("Under construction...");
    }

    private void setupLayout(ManageChangeables changeables) {
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(50,50,0,300));
        this.add(createClassesPanel(changeables), BorderLayout.NORTH);
    }
    
    private JPanel createClassesPanel(ManageChangeables changeables) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JLabel classToInclude = new JLabel("Classes to Include:");
        this.classesList = new ListWidget(classes);
        changeables.addChangeable(classesList);
        JScrollPane pane = new JScrollPane(classesList);
        pane.setPreferredSize(new Dimension(20, 80));
        panel.add(classToInclude, BorderLayout.NORTH);
        panel.add(pane, BorderLayout.CENTER);
        
        return panel;
    }

    public void save(ControlStrategy controlStrategy) throws EmfException {
        // NOTE Auto-generated method stub
        if (false)
            throw new EmfException("Cannot save measures filter fields.");
    }

    public void refresh(ControlStrategyResult controlStrategyResult) {
        // NOTE Auto-generated method stub

    }

}
