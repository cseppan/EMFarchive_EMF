package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.data.CostRecord;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class CostRecordWindow extends DisposableInteralFrame implements CostRecordView {

    private MessagePanel messagePanel;

    private CostRecordPresenter presenter;

    public CostRecordWindow(DesktopManager desktopManager) {
        super("New CostRecord", new Dimension(550, 480), desktopManager);
    }

    public void display(ControlMeasure measure) {
        super.setLabel(super.getTitle() + ": " + measure.getName());
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
        // NOTE Auto-generated method stub
        return new JPanel();
    }

    protected boolean verifyInput() {

        return true;
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
        if (verifyInput()) {
            presenter.addNew(costRecord());
            disposeView();
        }
    }

    public CostRecord costRecord() {
        CostRecord record = new CostRecord();
        record.setName("");
        record.setPollutant("");
        record.setCostYear(1900);
        record.setA(1);
        record.setB(1);
        record.setDiscountRate(1);
        
        return record;
    }

    public void observe(CostRecordPresenter presenter) {
        this.presenter = presenter;
    }

}
