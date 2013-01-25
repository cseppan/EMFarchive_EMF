package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.data.GeoRegion;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.ListWidget;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;

public class CompareCaseWindow extends DisposableInteralFrame implements CompareCaseView {
    
    private CompareCasePresenter presenter;

    private JPanel layout;

    private SingleLineMessagePanel messagePanel;

    private ComboBox gridNamesCombo;
    
    private ListWidget sectorsListWidget;
    private ListWidget inReportWidget;
    private ListWidget exReportWidget;
    
    private Case[] cases;
    
    private Dimension defaultDimension = new Dimension(255, 22);

    public CompareCaseWindow(DesktopManager desktopManager, Case[] cases) {
        super("Compare Cases Outputs", new Dimension(420, 450), desktopManager);
        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        this.cases = cases;
        super.getContentPane().add(layout);
    }

    private void doLayout(JPanel layout) {
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel);
        
        try {
            layout.add(createInputPanel());
            layout.add(selectReportPanel());
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
  
        gridNamesCombo = new ComboBox("Select one", regions()); 
        gridNamesCombo.setSelectedItem(0);
        gridNamesCombo.setPreferredSize(defaultDimension);
        gridNamesCombo.setToolTipText("Select a region with grid name on the end in (). ");
        layoutGenerator.addLabelWidgetPair("Grid Name:", gridNamesCombo, panel);
        
        layoutGenerator.addLabelWidgetPair("Sector: ", sectors(), panel);
        
        //layoutGenerator.addLabelWidgetPair("Report Dimensions:", reports(), panel);
        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 2, 2, // rows, cols
                10, 0, // initialX, initialY
                10, 10);// xPad, yPad
        
        return panel;
    }
    
    private JPanel selectReportPanel() throws EmfException {
        JPanel panel = new JPanel();
        panel.setBorder(new Border("Report Dimentions"));
       
        JPanel reportPanel = new JPanel(new BorderLayout(10,0));        
        reportPanel.add(reportPanel());  
        panel.add(reportPanel);        
        panel.setMaximumSize(new Dimension(380, 120));
        return panel;
    }
    
    private JScrollPane sectors() {
        List<Sector> sectorList = new ArrayList<Sector>();
        sectorList.add(new Sector("All", "All"));
        sectorList.addAll(getCaseSectors());

        sectorsListWidget = new ListWidget(sectorList.toArray(new Sector[0]));
        sectorsListWidget.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane exDTscrollPane = new JScrollPane(sectorsListWidget, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        exDTscrollPane.setPreferredSize(new Dimension(255, 150));

        return exDTscrollPane;
    }
    
    private List<Sector> getCaseSectors(){
        List<Sector> sectorList = new ArrayList<Sector>();
        for ( int i = 0; i < cases.length; i++) {
            Sector[] caseSectors = cases[i].getSectors();
            for (int j = 0; j < caseSectors.length; j++) 
                if ( !sectorList.contains(caseSectors[j]))
                    sectorList.add(caseSectors[j]);
        }
        return sectorList;
    }
    
    private JPanel reportPanel() {
        JPanel panel1 = new JPanel();
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));
                 
        panel1.add(Box.createRigidArea(new Dimension(8,0)));
        panel1.add(inPanel());
        panel1.add(Box.createRigidArea(new Dimension(8,0)));
        panel1.add(reportButtonsPanel());
        panel1.add(Box.createRigidArea(new Dimension(8,0)));
        panel1.add(exPanel());  
        panel1.add(Box.createRigidArea(new Dimension(8,0)));
           
        return panel1;
    }
    
    private JPanel inPanel(){
        String [] values= new String[]{"State", "Sector", "Species"};
        inReportWidget = new ListWidget(values);        
        inReportWidget.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);        
        JScrollPane inReportScrollPane = new JScrollPane(inReportWidget, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        inReportScrollPane.setPreferredSize(new Dimension(100, 100));
        JPanel inPanel = new JPanel();
        inPanel.setLayout(new BoxLayout(inPanel, BoxLayout.Y_AXIS));
        inPanel.add(new JLabel("included: "));
        inPanel.add(inReportScrollPane);
        return inPanel;        
    }
    
    private JPanel exPanel(){
        String [] values= new String[]{};
        exReportWidget = new ListWidget(values);        
        exReportWidget.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);        
        JScrollPane exReportScrollPane = new JScrollPane(exReportWidget, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        exReportScrollPane.setPreferredSize(new Dimension(100, 100));

        JPanel exPanel = new JPanel();
        exPanel.setLayout(new BoxLayout(exPanel, BoxLayout.Y_AXIS));
        exPanel.add(new JLabel("Excluded: "));
        exPanel.add(exReportScrollPane);
        return exPanel;        
    }
    
    private JPanel reportButtonsPanel() {
        JPanel buttonPanel =  new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        JPanel includeButtonPanel =  new JPanel();
        includeButtonPanel.setLayout(new BorderLayout(0, 0));
        includeButtonPanel.setPreferredSize(new Dimension(50, 45));
        includeButtonPanel.setMinimumSize(new Dimension(50, 45));
        JPanel excludeButtonPanel =  new JPanel();
        excludeButtonPanel.setLayout(new BorderLayout(0, 0));
        excludeButtonPanel.setPreferredSize(new Dimension(50, 45));
        excludeButtonPanel.setMinimumSize(new Dimension(50, 45));
        Button includeButton = new AddButton("<<", includeAction());
        includeButtonPanel.add(includeButton, BorderLayout.SOUTH);
//        JPanel excludeButtonPanel =  new JPanel();
//        excludeButtonPanel.setLayout(new BorderLayout(0, 0));
//        excludeButtonPanel.setPreferredSize(new Dimension(80, 45));
        Button excludeButton = new AddButton(">>", excludeAction());
        excludeButtonPanel.add(excludeButton, BorderLayout.NORTH);
        buttonPanel.add(includeButtonPanel);
        buttonPanel.add(Box.createRigidArea(new Dimension(0,10)));
        buttonPanel.add(excludeButtonPanel);
        return buttonPanel;
    }
    
    private Action includeAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                String[] objects= Arrays.asList(exReportWidget.getSelectedValues()).toArray(new String[0]);
                if ( objects != null && objects.length> 0 ){    
                    for (Object obj : objects) 
                        inReportWidget.addElement(obj);
                    exReportWidget.removeElements(objects);
                }
            }
        };
    }
    
    private Action excludeAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                String[] objects= Arrays.asList(inReportWidget.getSelectedValues()).toArray(new String[0]);
                if ( objects != null && objects.length> 0 ){    
                    for (Object obj : objects) 
                        exReportWidget.addElement(obj);
                    inReportWidget.removeElements(objects);
                }
            }
        };
    }

    private GeoRegion[] regions() {
        List<GeoRegion> regionList = new ArrayList<GeoRegion>();
        for ( int i = 0; i < cases.length; i++) {
            GeoRegion[] caseRegions = cases[i].getRegions();
            for (int j = 0; j < caseRegions.length; j++) 
                if ( !regionList.contains(caseRegions[j]))
                    regionList.add(caseRegions[j]);
        }
        return regionList.toArray(new GeoRegion[0]);
    }

    private Action okAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                messagePanel.clear();
                resetChanges();
                GeoRegion selectedRegion = (GeoRegion)gridNamesCombo.getSelectedItem();
                Sector[] selectedSectors= Arrays.asList(sectorsListWidget.getSelectedValues()).toArray(new Sector[0]);
                String[] repDims = Arrays.asList(inReportWidget.getAllElements()).toArray(new String[0]);
                if (selectedRegion == null || selectedRegion.getName().equalsIgnoreCase("Select one"))
                {
                    messagePanel.setError("Please select a grid.");
                    return;
                }                
                    
                if ( selectedSectors == null || selectedSectors.length == 0 ) {
                    messagePanel.setError("Please select a valid sector.");
                    return;
                } else if (selectedSectors[0].getName().equalsIgnoreCase("All")) {
                    selectedSectors= getCaseSectors().toArray(new Sector[0]);
                    //System.out.println("selectedSectors-- " + selectedSectors.length);
                }
                
                if ( repDims == null || repDims.length == 0 ) {
                    messagePanel.setError("Please select columns included.");
                    return;
                }  
                
                try {
                    presenter.showCaseQA(selectedRegion.getName(), selectedSectors, repDims);
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
