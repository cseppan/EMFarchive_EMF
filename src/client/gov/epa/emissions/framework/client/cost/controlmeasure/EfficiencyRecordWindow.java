package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.NumberFieldVerifier;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class EfficiencyRecordWindow extends DisposableInteralFrame implements EfficiencyRecordView {

    private EmfSession session;
    
    private MessagePanel messagePanel;

    private EfficiencyRecordPresenter presenter;

    private TextField efficiency;
    
    private ComboBox pollutant;
    
    private float efficiencyValue;

    private ManageChangeables changeablesList;

    private NumberFieldVerifier verifier;
    
    private boolean verified = false;
    
    private EfficiencyRecord record;
    
    private static int count = 0;
    
    protected Pollutant[] allPollutants;

    public EfficiencyRecordWindow(ManageChangeables changeablesList, DesktopManager desktopManager, EmfSession session) {
        super("Add Efficiency Record", new Dimension(400, 180), desktopManager);
        this.changeablesList = changeablesList;
        this.session = session;
        this.verifier = new NumberFieldVerifier("");
    }

    public void display(ControlMeasure measure, EfficiencyRecord record) {
        String name = measure.getName();
        if(name == null)
            name = "New Control Measure";
        super.setLabel(super.getTitle() + " " + ++count + " for Control Measure: " + name);
        JPanel layout = createLayout();
        super.getContentPane().add(layout);
        super.display();
        this.record = record;
        
        if(record != null)
            populateFields();
    }

    private JPanel createLayout() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);
        panel.add(recordPanel());
        panel.add(buttonsPanel());

        return panel;
    }

    private Component recordPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        try {
            allPollutants = session.dataCommonsService().getPollutants();
            pollutant = new ComboBox(allPollutants);
        } catch (EmfException e) {
            messagePanel.setError("Could not retrieve Pollutants");
        }
        // majorPollutant.setSelectedIndex(0);
        changeablesList.addChangeable(pollutant);
        layoutGenerator.addLabelWidgetPair("Pollutant:", pollutant, panel);

        efficiency = new TextField("", 10);
        efficiency.setName("Percent Reduction");
        changeablesList.addChangeable(efficiency);
        layoutGenerator.addLabelWidgetPair("Percent Reduction (%):", efficiency, panel);
        efficiency.setToolTipText("Enter the percent reduction as a percentage (e.g., 90%, or -10% for a disbenefit)");

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 2, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }
    
    private void populateFields() {
        pollutant.setSelectedItem(record.getPollutant());
        efficiency.setText(record.getEfficiency()+"");
    }

    protected void verifyInput() {
        try {
            efficiencyValue = verifier.parseFloat(efficiency);
            if (efficiencyValue > 100)
                throw new EmfException("Percent reduction must be less than or equal to 100.0");
            efficiencyValue = efficiencyValue / 100;
            verified = true;
        } catch (EmfException e) {
            verified = false;
            messagePanel.setError(e.getMessage());
        }
    }

    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();
        Button ok = new Button("OK", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doNew();
            }
        });
        getRootPane().setDefaultButton(ok);
        panel.add(ok);

        Button cancel = new Button("Cancel", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                disposeView();
            }
        });
        panel.add(cancel);

        return panel;
    }

    private void doNew() {
        verifyInput();
        if(verified == false)
            return;
        
        presenter.addNew(efficiencyRecord());
        disposeView();
    }
    
    public EfficiencyRecord efficiencyRecord() {
        EfficiencyRecord record = new EfficiencyRecord();
        
        return setRecord(record);
    }

    private EfficiencyRecord setRecord(EfficiencyRecord record) {
        record.setPollutant((Pollutant) pollutant.getSelectedItem());
        record.setEfficiency(efficiencyValue);
        
        return record;
    }

    public void observe(EfficiencyRecordPresenter presenter) {
        this.presenter = presenter;
    }

}
