package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.EditableComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
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

    private MessagePanel messagePanel;

    private EfficiencyRecordPresenter presenter;

    private TextField name, efficiency;
    
    private EditableComboBox pollutant;
    
    private float efficiencyValue;

    private ManageChangeables changeablesList;

    private NumberFieldVerifier verifier;
    
    private boolean verified = false;
    
    private EfficiencyRecord record;
    
    private static int count = 0;
    
    private String[] pollutants = { "NOx", "PM10", "PM2.5", "SO2", "VOC", "CO",
            "CO2", "EC", "OC", "NH3", "Hg" };

    public EfficiencyRecordWindow(ManageChangeables changeablesList, DesktopManager desktopManager) {
        super("Efficiency Record", new Dimension(400, 200), desktopManager);
        this.changeablesList = changeablesList;
        this.verifier = new NumberFieldVerifier("");
    }

    public void display(ControlMeasure measure, EfficiencyRecord record) {
        super.setLabel(super.getTitle() + " " + ++count + " for Control Measure: " + measure.getName());
        JPanel layout = createLayout(measure);
        super.getContentPane().add(layout);
        super.display();
        this.record = record;
        
        if(record != null)
            populateFields();
    }

    private JPanel createLayout(ControlMeasure measure) {
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

        name = new TextField("", 20);
        name.setName("Name");
        changeablesList.addChangeable(name);
        layoutGenerator.addLabelWidgetPair("Name:", name, panel);

        pollutant = new EditableComboBox(pollutants);
        pollutant.setName("Pollutant");
        changeablesList.addChangeable(pollutant);
        layoutGenerator.addLabelWidgetPair("Pollutant:", pollutant, panel);

        efficiency = new TextField("", 20);
        efficiency.setName("Efficiency");
        changeablesList.addChangeable(efficiency);
        layoutGenerator.addLabelWidgetPair("Efficiency:", efficiency, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }
    
    private void populateFields() {
        name.setText(record.getName());
        pollutant.setSelectedItem(record.getPollutant());
        efficiency.setText(record.getEfficiency()+"");
    }

    protected void verifyInput() {
        try {
            efficiencyValue = verifier.parseFloat(efficiency);
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
        record.setName(name.getText());
        record.setPollutant(pollutant.getSelectedItem()+"");
        record.setEfficiency(efficiencyValue);
        
        return record;
    }

    public void observe(EfficiencyRecordPresenter presenter) {
        this.presenter = presenter;
    }

}
