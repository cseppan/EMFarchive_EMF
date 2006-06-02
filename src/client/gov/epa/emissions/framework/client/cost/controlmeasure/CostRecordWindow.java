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
import gov.epa.emissions.framework.services.cost.data.CostRecord;
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

public class CostRecordWindow extends DisposableInteralFrame implements CostRecordView {

    private MessagePanel messagePanel;

    private CostRecordPresenter presenter;

    private TextField year, costPerTon;
    
    private EditableComboBox pollutant;
    
    private int costYear;
    
    private float cost;

    private ManageChangeables changeablesList;

    private NumberFieldVerifier verifier;
    
    private boolean verified = false;
    
    private CostRecord record;
    
    private static int count = 0;
    
    private String[] pollutants = { "NOx", "PM10", "PM2.5", "SO2", "VOC", "CO",
            "CO2", "EC", "OC", "NH3", "Hg" };

    public CostRecordWindow(ManageChangeables changeablesList, DesktopManager desktopManager) {
        super("Cost Record", new Dimension(430, 200), desktopManager);
        this.changeablesList = changeablesList;
        this.verifier = new NumberFieldVerifier("");
    }

    public void display(ControlMeasure measure, CostRecord record) {
        String name = measure.getName();
        if(name == null)
            name = "New Control Measure";
        super.setLabel(super.getTitle() + " " + ++count + " for Control Measure: " + name);
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

        pollutant = new EditableComboBox(pollutants);
        pollutant.setName("Pollutant");
        changeablesList.addChangeable(pollutant);
        layoutGenerator.addLabelWidgetPair("Pollutant:", pollutant, panel);

        year = new TextField("", 20);
        year.setName("Cost year");
        changeablesList.addChangeable(year);
        layoutGenerator.addLabelWidgetPair("Cost year:", year, panel);

        costPerTon = new TextField("", 20);
        costPerTon.setName("Cost per Ton");
        changeablesList.addChangeable(costPerTon);
        layoutGenerator.addLabelWidgetPair("Cost per Ton:", costPerTon, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }
    
    private void populateFields() {
        pollutant.setSelectedItem(record.getPollutant());
        year.setText(record.getCostYear()+"");
        costPerTon.setText(record.getCostPerTon()+"");
    }

    protected void verifyInput() {
        try {
            costYear = verifier.parseInteger(year);
            cost = verifier.parseFloat(costPerTon);
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
                doOK();
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

    protected void doOK() {
        if(this.record == null)
            doNew();
        else 
            doEdit(this.record);
    }

    private void doNew() {
        verifyInput();
        if(verified == false)
            return;
        
        presenter.addNew(costRecord());
        disposeView();
    }
    
    private void doEdit(CostRecord record) {
        verifyInput();
        if(verified == false)
            return;
        
        presenter.doEdit(setRecord(record));
        disposeView();
    }

    public CostRecord costRecord() {
        CostRecord record = new CostRecord();
        
        return setRecord(record);
    }

    private CostRecord setRecord(CostRecord record) {
        record.setPollutant(pollutant.getSelectedItem()+"");
        record.setCostYear(costYear);
        record.setCostPerTon(cost);
        
        return record;
    }

    public void observe(CostRecordPresenter presenter) {
        this.presenter = presenter;
    }

}
