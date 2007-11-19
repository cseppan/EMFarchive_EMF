package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.EmfImageTool;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.LightControlMeasure;
import gov.epa.emissions.framework.ui.DoubleTextField;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.NumberFieldVerifier;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.emissions.framework.ui.TrackableSortFilterSelectModel;
import gov.epa.emissions.framework.ui.YesNoDialog;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class ControlMeasureSelectionDialog extends JDialog implements ControlMeasureSelectionView {

    private TrackableSortFilterSelectModel selectModel;

    private EmfConsole parent;

    private ControlMeasureSelectionPresenter presenter;
    
    private SingleLineMessagePanel messagePanel;
    
    private ManageChangeables changeables;  
    
    private DoubleTextField rule, rPenetration, rEffective;
    private NumberFieldVerifier verifier;

    public ControlMeasureSelectionDialog(EmfConsole parent, ManageChangeables changeables) {
        super(parent);
        super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
        this.verifier= new NumberFieldVerifier("Measure properties: ");
        this.parent = parent;
        this.changeables = changeables;
    }

    public void display(ControlMeasureTableData tableData) {
        EmfTableModel tableModel = new EmfTableModel(tableData);
        selectModel = new TrackableSortFilterSelectModel(tableModel);
        changeables.addChangeable(selectModel);
        SortFilterSelectionPanel panel = new SortFilterSelectionPanel(parent, selectModel);
        
        messagePanel = new SingleLineMessagePanel();
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(5, 10));
        contentPane.add(messagePanel, BorderLayout.PAGE_START);
        contentPane.add(panel, BorderLayout.CENTER);
        contentPane.add(createLowerSection(), BorderLayout.SOUTH);

        setTitle("Select Control Measures");
        this.pack();
        this.setSize(700,600);
        this.setLocation(ScreenUtils.getPointToCenter(parent));
        this.setVisible(true);
    }
   
    private JPanel createLowerSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createPropertySection(), BorderLayout.CENTER);
        panel.add(buttonPanel(), BorderLayout.SOUTH);
        return panel;  
    }
    
    private JPanel createPropertySection() {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Measure Properties"));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Set Order:", ruleField(), panel);
        layoutGenerator.addLabelWidgetPair("Set RP %:", rPField(), panel);
        layoutGenerator.addLabelWidgetPair("Set RE %:", rEField(), panel);
        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 1, 6, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }
    private DoubleTextField ruleField() {
        rule = new DoubleTextField("Set Order", 1, 100, 10);
        rule.setValue(1);
        return rule;
    }
    
    private DoubleTextField rPField() {
        rPenetration = new DoubleTextField("Set RP %", 1, 100, 10);
        rPenetration.setValue(100);
        return rPenetration;
    }
    
    private DoubleTextField rEField() {
        rEffective = new DoubleTextField("Set RE %", 1, 100, 10);
        rEffective.setValue(100);
        return rEffective;
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
                setVisible(false);
                dispose();
            }

        };
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    add();
                } catch (EmfException e1) {
                    // NOTE Auto-generated catch block
                    messagePanel.setError(e1.getMessage());
                }           
            }
        };
    }

    private void add() throws EmfException {
        messagePanel.clear();
        List selected = selectModel.selected();
        
        // prevent from closing window without selecting items
        if (selected.size()==0){
            String message = "Would you like to close without selecting any measures?";
            YesNoDialog dialog = new YesNoDialog(this, "No measures selected", message);
            if (dialog.confirm()){
                setVisible(false);
                dispose();
            }
            return; 
        }
        LightControlMeasure[] cms = (LightControlMeasure[]) selected.toArray(new LightControlMeasure[0]);
        presenter.doAdd(cms, checkNumber(rule), checkNumber(rPenetration), checkNumber(rEffective));
        setVisible(false);
        dispose();
    }

    private double checkNumber(DoubleTextField value) throws EmfException{
        if (value.getText().trim().length() == 0){
            throw new EmfException(value.getName()+":  Enter a number between 1 and 100");
        }
        double value1 = verifier.parseDouble(value.getText());

        // make sure the number makes sense...
        if (value1 < 1 || value1 > 100) {
            throw new EmfException(value.getName()+":  Enter a number between 1 and 100");
        }
        return value1;

    }

    public void observe(ControlMeasureSelectionPresenter presenter) {
        this.presenter = presenter;
    }

}
