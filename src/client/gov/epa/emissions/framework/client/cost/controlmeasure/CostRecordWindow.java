package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.gui.Button;
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

    private TextField name, pollutant, year, discountRate, a, b;
    
    private int costYear;
    
    private float discount, paramA, paramB;

    private ManageChangeables changeablesList;

    private NumberFieldVerifier verifier;
    
    private boolean verified = false;
    
    private static int count = 0;

    public CostRecordWindow(ManageChangeables changeablesList, DesktopManager desktopManager) {
        super("Cost Record", new Dimension(400, 280), desktopManager);
        this.changeablesList = changeablesList;
        this.verifier = new NumberFieldVerifier("");
    }

    public void display(ControlMeasure measure) {
        super.setLabel(super.getTitle() + " for ControlMeasure: " + measure.getName() + " " + ++count);
        JPanel layout = createLayout(measure);
        super.getContentPane().add(layout);
        super.display();
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

        pollutant = new TextField("", 20);
        pollutant.setName("Pollutant");
        changeablesList.addChangeable(pollutant);
        layoutGenerator.addLabelWidgetPair("Pollutant:", pollutant, panel);

        year = new TextField("", 20);
        year.setName("Cost year");
        changeablesList.addChangeable(year);
        layoutGenerator.addLabelWidgetPair("Cost year:", year, panel);

        discountRate = new TextField("", 20);
        discountRate.setName("Discount rate");
        changeablesList.addChangeable(discountRate);
        layoutGenerator.addLabelWidgetPair("Discount rate:", discountRate, panel);

        a = new TextField("", 20);
        a.setName("Parameter a");
        changeablesList.addChangeable(a);
        layoutGenerator.addLabelWidgetPair("Parameter a:", a, panel);

        b = new TextField("", 20);
        b.setName("Parameter b");
        changeablesList.addChangeable(b);
        layoutGenerator.addLabelWidgetPair("Parameter b:", b, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 6, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    protected void verifyInput() {
        try {
            costYear = verifier.parseInteger(year);
            discount = verifier.parseFloat(discountRate);
            paramA = verifier.parseFloat(a);
            paramB = verifier.parseFloat(b);
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
        
        presenter.addNew(costRecord());
        disposeView();
    }

    public CostRecord costRecord() {
        CostRecord record = new CostRecord();
        record.setName(name.getText());
        record.setPollutant(pollutant.getText());
        record.setCostYear(costYear);
        record.setA(paramA);
        record.setB(paramB);
        record.setDiscountRate(discount);

        return record;
    }

    public void observe(CostRecordPresenter presenter) {
        this.presenter = presenter;
    }

}
