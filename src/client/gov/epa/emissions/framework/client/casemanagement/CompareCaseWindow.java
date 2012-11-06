package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.casemanagement.editor.AddRemoveSectorWidget;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseCategory;
import gov.epa.emissions.framework.services.data.GeoRegion;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class CompareCaseWindow extends DisposableInteralFrame implements CompareCaseView {
    
    private CompareCasePresenter presenter;

    private JPanel layout;

    private SingleLineMessagePanel messagePanel;

    private ComboBox gridNamesCombo;
    
    private ComboBox reportTypesCombo;

    private ComboBox sectorsCombo;
    
    //private int[] ids;
    
    private Dimension defaultDimension = new Dimension(255, 22);

    public CompareCaseWindow(DesktopManager desktopManager) {
        super("Compare Cases Outputs", new Dimension(400, 240), desktopManager);
        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        super.getContentPane().add(layout);
    }

    private void doLayout(JPanel layout) {
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel);
        
        try {
            layout.add(createInputPanel());
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
        
        layout.add(createButtonsPanel());
    }

    public void observe(CompareCasePresenter presenter) {
        this.presenter = presenter;
    }

    public void display() {
        super.setLabel("Compare Case");
        layout.removeAll();
        doLayout(layout);

        super.display();
    }

    private JPanel createInputPanel() throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        gridNamesCombo = new ComboBox(presenter.getAllRegions()); 
        gridNamesCombo.setSelectedItem(0);
        gridNamesCombo.setPreferredSize(defaultDimension);
        gridNamesCombo.setToolTipText("Select a region with grid name on the end in (). ");
        layoutGenerator.addLabelWidgetPair("Grid Name:", gridNamesCombo, panel);
        
        String [] values= new String[]{"Country","State", "County"};
        reportTypesCombo = new ComboBox(values);
        reportTypesCombo.setSelectedItem(1);
        reportTypesCombo.setPreferredSize(defaultDimension);
        reportTypesCombo.setToolTipText("Select a SMOKE annual report type. ");
        layoutGenerator.addLabelWidgetPair("Report Types:", reportTypesCombo, panel);
        
        layoutGenerator.addLabelWidgetPair("Sector: ", sectors(), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 3, 2, // rows, cols
                10, 0, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }
    
    private ComboBox sectors() throws EmfException {
        sectorsCombo = new ComboBox(presenter.getAllSectors());
        sectorsCombo.setSelectedItem(1);
        sectorsCombo.setPreferredSize(defaultDimension);
        sectorsCombo.setToolTipText("Select a specific sector or 'All Sectors'. ");
        return sectorsCombo;
    }
    
    private Action okAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                resetChanges();
                GeoRegion region = (GeoRegion)gridNamesCombo.getSelectedItem();
                Sector sec = (Sector)sectorsCombo.getSelectedItem();
                String repType = (String) reportTypesCombo.getSelectedItem();

                if (region == null ) {
                    messagePanel.setError("Please give a name for the grid.");
                    return;
                }
                    
                if (sec == null) {
                    messagePanel.setError("Please select a valid sector.");
                    return;
                }  
                
                try {
                    presenter.showCaseQA(region.getName(), sec.getName(), repType);
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        };

        return action;
    }

    public void windowClosing() {
        doClose();
    }

    private Action closeAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doClose();
            }
        };

        return action;
    }

    private void doClose() {
        if (shouldDiscardChanges())
            presenter.doClose();
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(25);
        container.setLayout(layout);

        Button okButton = new OKButton(okAction());
        container.add(okButton);
        container.add(new CancelButton(closeAction()));
        getRootPane().setDefaultButton(okButton);

        panel.add(container, BorderLayout.CENTER);

        return panel;
    }

    public void addSector(Sector sector) {
        // NOTE Auto-generated method stub
        
    }

    public void addRegion(GeoRegion region) {
        // NOTE Auto-generated method stub
        
    }

}
