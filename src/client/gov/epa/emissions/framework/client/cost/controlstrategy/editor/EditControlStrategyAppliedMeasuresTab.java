package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class EditControlStrategyAppliedMeasuresTab extends JPanel implements EditControlStrategyTabView {

    public EditControlStrategyAppliedMeasuresTab(ControlStrategy controlStrategy, ManageChangeables changeablesList,
            SingleLineMessagePanel messagePanel, EmfConsole parentConsole, EmfSession session) throws EmfException {
        setupLayout(changeablesList);

        if (false)
            throw new EmfException("Cannot save appliced measures filter fields.");
    }
    
    private void setupLayout(ManageChangeables changeables) {
        this.setLayout(new BorderLayout());
        this.add(createConstraintPanel(changeables), BorderLayout.CENTER);
    }

    private JPanel createConstraintPanel(ManageChangeables changeables) {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(30,50,250,200));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("             Constraints:", new JLabel(), panel);
        
        TextField emisReduction = new TextField("emission reduction", 10);
        changeables.addChangeable(emisReduction);
        layoutGenerator.addLabelWidgetPair("Emissions Reduction  >", emisReduction, panel);

        TextField contrlEff = new TextField("control efficiency", 10);
        changeables.addChangeable(contrlEff);
        layoutGenerator.addLabelWidgetPair("Control Efficiency        >", contrlEff, panel);

        TextField costPerTon = new TextField("cost per ton", 10);
        changeables.addChangeable(costPerTon);
        layoutGenerator.addLabelWidgetPair("Cost per Ton                 <", costPerTon, panel);

        TextField annCost = new TextField("annual cost", 10);
        changeables.addChangeable(annCost);
        layoutGenerator.addLabelWidgetPair("Annual Cost                  <", annCost, panel);

        layoutGenerator.makeCompactGrid(panel, 5, 2, // rows, cols
                10, 10, // initialX, initialY
                5, 5);// xPad, yPad

        return panel;
    }

    public void save(ControlStrategy controlStrategy) throws EmfException {
        // NOTE Auto-generated method stub
        if (false)
            throw new EmfException("Cannot save appliced measures filter fields.");
    }

    public void refresh(ControlStrategyResult controlStrategyResult) {
        // NOTE Auto-generated method stub

    }

}
