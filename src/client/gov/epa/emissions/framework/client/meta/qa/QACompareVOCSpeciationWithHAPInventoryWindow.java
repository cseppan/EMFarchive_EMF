package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionDialog;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.ListWidget;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

public class QACompareVOCSpeciationWithHAPInventoryWindow extends DisposableInteralFrame implements EditQAEmissionsView {
    
    protected AddRemoveDatasetWidget datasetWidget;
    
    protected EmfConsole parentConsole;
    
    protected JPanel layout;
    
    protected EditQAEmissionsPresenter presenter1;
    
    protected ListWidget capInvListWidget;
    
    protected ListWidget hapInvListWidget;
    
    protected ListWidget speciationToolGasProfileDatasetListWidget;
    
    protected ListWidget pollToPollConversionDatasetListWidget;
    
    protected ListWidget speciationProfileWeightDatasetsListWidget;
    
    protected ListWidget speciationCrossReferenceDatasetsListWidget;
    
    private TextArea filterTextArea;
    
    protected EmfSession session;
    
    protected SingleLineMessagePanel messagePanel;
    
    private ComboBox summaryTypes;
    
    private EmfDataset capInventory;
    
    private EmfDataset hapInventory;
    
    private EmfDataset speciationToolGasProfileDataset;
    
    private EmfDataset pollToPollConversionDataset;
    
    private EmfDataset[] speciationProfileWeightDatasets;
    
    private EmfDataset[] speciationCrossReferenceDatasets;
    
    private String summaryType; 
    
    private String filter;
    
    private DatasetType orlPointDST;
    
    private DatasetType speciationToolGasProfileDST;
    
    private DatasetType pollToPollConversionDST;
    
    private DatasetType speciationProfileWeightDST;
    
    private DatasetType speciationCrossReferenceDST;
        
    public QACompareVOCSpeciationWithHAPInventoryWindow(DesktopManager desktopManager, 
            String program, 
            EmfSession session, 
            EmfDataset capInventory, 
            EmfDataset hapInventory, 
            EmfDataset speciationToolGasProfileDataset, 
            EmfDataset pollToPollConversionDataset, 
            EmfDataset [] speciationProfileWeightDatasets, 
            EmfDataset [] speciationCrossReferenceDatasets, 
            String filter, 
            String summaryType) {
        
        super("Compare CAP/HAP Inventories Editor", new Dimension(800, 600), desktopManager);
        this.session = session;
        this.capInventory = capInventory;
        this.hapInventory = hapInventory;
        this.speciationToolGasProfileDataset = speciationToolGasProfileDataset;
        this.pollToPollConversionDataset = pollToPollConversionDataset;
        this.speciationProfileWeightDatasets = speciationProfileWeightDatasets;
        this.speciationCrossReferenceDatasets = speciationCrossReferenceDatasets;
        this.filter = filter;
        this.summaryType = summaryType;
    }

    public void display(EmfDataset dataset, QAStep qaStep) {
        try {
            this.orlPointDST = presenter1.getDatasetType(DatasetType.orlPointInventory);
            this.speciationToolGasProfileDST = presenter1.getDatasetType("Speciation Tool gas profiles");
            this.pollToPollConversionDST = presenter1.getDatasetType("Pollutant to Pollutant Conversion (GSCNV)");
            this.speciationProfileWeightDST = presenter1.getDatasetType("Speciation Tool profile weights");
            this.speciationCrossReferenceDST = presenter1.getDatasetType("Chemical Speciation Cross-Reference (GSREF)");
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        super.setTitle("Setup "+qaStep.getName()+": " + dataset.getName() + "_" + qaStep.getId() );
        super.display();
        this.getContentPane().add(createLayout(dataset));
    }

    public void observe(EditQAEmissionsPresenter presenter1) {
        this.presenter1 = presenter1;
    }
    
    // A JList with Add and Remove buttons for the Emission Inventories.
    // A Text Field for Adding the Inventory Table with a Select button
    // OK and Cancel buttons.

    private JPanel createLayout(EmfDataset dataset) {
        
        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        JPanel content = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
       
        layoutGenerator.addLabelWidgetPair("CAP Inventory:", capInvPanel(), content);
        layoutGenerator.addLabelWidgetPair("HAP Inventory:", hapInvPanel(), content);
        layoutGenerator.addLabelWidgetPair("Speciation Tool Species Info Dataset:", speciationToolGasProfileDatasetPanel(), content);
        layoutGenerator.addLabelWidgetPair("Pollutant to Pollutant Conversion (GSCNV) Dataset:",  pollToPollConversionDatasetPanel(), content);
        layoutGenerator.addLabelWidgetPair("Speciation Cross-Reference (GSREF) Datasets:",  speciationCrossReferenceDatasetsPanel(), content);
        layoutGenerator.addLabelWidgetPair("Speciation Profile Weight Datasets:",  speciationProfileWeightDatasetsPanel(), content);
        
        summaryTypeCombo();
        layoutGenerator.addLabelWidgetPair("Summary Type:", summaryTypes, content);

//        JPanel middlePanel = new JPanel(new SpringLayout());
//        middlePanel.setBorder(new Border("Filters"));
        
        filterTextArea = new TextArea("filter", filter, 40, 2);
        filterTextArea.setToolTipText("Enter a filter that could be entered as a SQL where clause (e.g., ANN_EMIS>5000 and SCC like '30300%')");
        JScrollPane scrollPane = new JScrollPane(filterTextArea);
        
        layoutGenerator.addLabelWidgetPair("Inventory Filter:", scrollPane, content);

        layoutGenerator.makeCompactGrid(content, 8, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad*/
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel);
        layout.add(content);
        layout.add(buttonPanel());
        
        return layout;
    }
    
    private JPanel capInvPanel() {
        
        capInvListWidget = new ListWidget(new EmfDataset[0]);
        if(!(capInventory == null))
            setDatasetsFromStepWindow(capInvListWidget, new EmfDataset[] { capInventory });
        
        JScrollPane pane = new JScrollPane(capInvListWidget);
        pane.setPreferredSize(new Dimension(350, 25));
        capInvListWidget.setToolTipText("The CAP inventory dataset.  Press select button to choose from a list.");
       
        Button addButton = new AddButton("Select", addCAPInvAction());
        addButton.setMargin(new Insets(1, 2, 1, 2));
        
        JPanel invPanel = new JPanel(new BorderLayout(5,0));
        
        invPanel.add(pane, BorderLayout.LINE_START);
        invPanel.add(addButton);
        return invPanel;
    }
    
    private JPanel hapInvPanel() {
        
        hapInvListWidget = new ListWidget(new EmfDataset[0]);
        if(!(hapInventory==null))
            setDatasetsFromStepWindow(hapInvListWidget, new EmfDataset[] { hapInventory });
        
        JScrollPane pane = new JScrollPane(hapInvListWidget);
        pane.setPreferredSize(new Dimension(350, 25));
        hapInvListWidget.setToolTipText("The HAP inventory dataset.  Press select button to choose from a list.");
       
        Button addButton = new AddButton("Select", addHAPInvAction());
        addButton.setMargin(new Insets(1, 2, 1, 2));
        
        JPanel invPanel = new JPanel(new BorderLayout(5,0));
        
        invPanel.add(pane, BorderLayout.LINE_START);
        invPanel.add(addButton);
        return invPanel;
    }
    
    private JPanel speciationToolGasProfileDatasetPanel() {
        
        speciationToolGasProfileDatasetListWidget = new ListWidget(new EmfDataset[0]);
        if(!(speciationToolGasProfileDataset==null))
            setDatasetsFromStepWindow(speciationToolGasProfileDatasetListWidget, new EmfDataset[] { speciationToolGasProfileDataset });
        
        JScrollPane pane = new JScrollPane(speciationToolGasProfileDatasetListWidget);
        pane.setPreferredSize(new Dimension(350, 25));
        speciationToolGasProfileDatasetListWidget.setToolTipText("The Speciation Tool Gas Profile Dataset.  Press select button to choose from a list.");
       
        Button addButton = new AddButton("Select", addSpeciationToolGasProfileDatasetAction());
        addButton.setMargin(new Insets(1, 2, 1, 2));
        
        JPanel invPanel = new JPanel(new BorderLayout(5,0));
        
        invPanel.add(pane, BorderLayout.LINE_START);
        invPanel.add(addButton);
        return invPanel;
    }
    
    private JPanel pollToPollConversionDatasetPanel() {
        
        pollToPollConversionDatasetListWidget = new ListWidget(new EmfDataset[0]);
        if(!(pollToPollConversionDataset==null))
            setDatasetsFromStepWindow(pollToPollConversionDatasetListWidget, new EmfDataset[] { pollToPollConversionDataset });
        
        JScrollPane pane = new JScrollPane(pollToPollConversionDatasetListWidget);
        pane.setPreferredSize(new Dimension(350, 25));
        pollToPollConversionDatasetListWidget.setToolTipText("The Pollutant To Pollutant Conversion (GSCNV) Dataset.  Press select button to choose from a list.");
       
        Button addButton = new AddButton("Select", addPollToPollConversionDatasetAction());
        addButton.setMargin(new Insets(1, 2, 1, 2));
        
        JPanel invPanel = new JPanel(new BorderLayout(5,0));
        
        invPanel.add(pane, BorderLayout.LINE_START);
        invPanel.add(addButton);
        return invPanel;
    }
    
    private JPanel speciationCrossReferenceDatasetsPanel() {
        
        speciationCrossReferenceDatasetsListWidget = new ListWidget(new EmfDataset[0]);
        if(!(speciationCrossReferenceDatasets==null))
            setDatasetsFromStepWindow(speciationCrossReferenceDatasetsListWidget, speciationCrossReferenceDatasets);
        
        JScrollPane pane = new JScrollPane(speciationCrossReferenceDatasetsListWidget);
        pane.setPreferredSize(new Dimension(350, 50));
        speciationCrossReferenceDatasetsListWidget.setToolTipText("The Speciation Cross-Reference (GSREF) Datasets.  Press select button to choose from a list.");
//        speciationCrossReferenceDatasetsListWidget.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        Button addButton = new AddButton("Select", addSpeciationCrossReferenceDatasetsAction());
        addButton.setMargin(new Insets(1, 2, 1, 2));
        
        JPanel invPanel = new JPanel(new BorderLayout(5,0));
        
        invPanel.add(pane, BorderLayout.LINE_START);
        invPanel.add(addButton);
        return invPanel;
    }
    
    private JPanel speciationProfileWeightDatasetsPanel() {
        
        speciationProfileWeightDatasetsListWidget = new ListWidget(new EmfDataset[0]);
        if(!(speciationProfileWeightDatasets==null))
            setDatasetsFromStepWindow(speciationProfileWeightDatasetsListWidget, speciationProfileWeightDatasets);
        
        JScrollPane pane = new JScrollPane(speciationProfileWeightDatasetsListWidget);
        pane.setPreferredSize(new Dimension(350, 50));
        speciationProfileWeightDatasetsListWidget.setToolTipText("The Speciation Profile Weight Datasets.  Press select button to choose from a list.");
//        speciationProfileWeightDatasetsListWidget.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        Button addButton = new AddButton("Select", addSpeciationProfileWeightDatasetsAction());
        addButton.setMargin(new Insets(1, 2, 1, 2));
        
        JPanel invPanel = new JPanel(new BorderLayout(5,0));
        
        invPanel.add(pane, BorderLayout.LINE_START);
        invPanel.add(addButton);
        return invPanel;
    }
    
    protected void summaryTypeCombo() {
        String [] values= new String[]{"Details", "By NEI Unique Id", "By SCC", "By SIC", "By MACT", "By NAICS", "By Profile Code"};
        summaryTypes = new ComboBox("Not Selected", values);
        summaryTypes.setPreferredSize(new Dimension(350, 25));
        if(!(summaryType==null) && (summaryType.trim().length()>0))
            summaryTypes.setSelectedItem(summaryType);
        
        summaryTypes.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                summaryTypes.getSelectedItem();
            }
        });
    }

    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        panel.add(new OKButton(okAction()));
        panel.add(new CancelButton(cancelAction()));
        return panel;
    }
    
    private Action addCAPInvAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doAddWindow(capInvListWidget, new DatasetType[] {orlPointDST}, orlPointDST, true);
            }
        };
    }
  
    private Action addHAPInvAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doAddWindow(hapInvListWidget, new DatasetType[] {orlPointDST}, orlPointDST, true);
            }
        };
    }
  
    private Action addSpeciationToolGasProfileDatasetAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doAddWindow(speciationToolGasProfileDatasetListWidget, new DatasetType[] {speciationToolGasProfileDST}, speciationToolGasProfileDST, true);
            }
        };
    }
    
    private Action addPollToPollConversionDatasetAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doAddWindow(pollToPollConversionDatasetListWidget, new DatasetType[] {pollToPollConversionDST}, pollToPollConversionDST, true);
            }
        };
    }
    
    private Action addSpeciationCrossReferenceDatasetsAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doAddWindow(speciationCrossReferenceDatasetsListWidget, new DatasetType[] {speciationCrossReferenceDST}, speciationCrossReferenceDST, false);
            }
        };
    }
    
    private Action addSpeciationProfileWeightDatasetsAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doAddWindow(speciationProfileWeightDatasetsListWidget, new DatasetType[] {speciationProfileWeightDST}, speciationProfileWeightDST, false);
            }
        };
    }
    
    protected Action cancelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                dispose();
                disposeView();
            }

        };
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //validate dataset selections
                Object capInv = getCAPInvDataset();
                Object hapInv = getHAPInvDataset();
                Object speciationToolGasProfile = getSpeciationToolGasProfileDataset();
                Object pollToPollConversion = getPollToPollConversionDataset();
                Object[] speciationProfileWeights = getSpeciationProfileWeightDatasets();
                Object[] speciationCrossReferences = getSpeciationCrossReferenceDatasets();
                String filter = filterTextArea.getText();
                String summaryType = getSummaryType();
                String errors = "";

                if (capInv == null) {
                    errors = "Missing CAP inventory. ";
                }
                if (hapInv == null) {
                    errors += "Missing HAP inventory. ";
                }
                if (speciationToolGasProfile == null) {
                    errors += "Missing Speciation Tool Gas Profiles Dataset. ";
                }
                if (pollToPollConversion == null) {
                    errors += "Missing Pollutant-To-Pollutant Conversion Dataset. ";
                }
                if (speciationProfileWeights == null || speciationProfileWeights.length == 0) {
                    errors += "Missing Speciation Profile Weight Dataset(s). ";
                }
                if (speciationCrossReferences == null || speciationCrossReferences.length == 0) {
                    errors += "Missing Speciation Cross Reference Dataset(s). ";
                }
                if (summaryType == null || summaryType.length() == 0) {
                    errors += "Missing summary type value. ";
                }
                
                if (errors.length() > 0) {
                    messagePanel.setError(errors);
                    return;
                }
                presenter1.updateDatasets(capInv, 
                        hapInv,
                        speciationToolGasProfile,
                        pollToPollConversion,
                        speciationProfileWeights,
                        speciationCrossReferences,
                        filter,
                        summaryType);
                dispose();
                disposeView();
            }
        };
    }
    
    protected void doAddWindow(ListWidget listWidget, DatasetType[] datasetTypes, DatasetType defaultDatasetType, boolean selectSingle) {
        try {
            InputDatasetSelectionDialog view = new InputDatasetSelectionDialog (parentConsole);
            InputDatasetSelectionPresenter presenter = new InputDatasetSelectionPresenter(view, session, datasetTypes);
            presenter.display(defaultDatasetType, selectSingle);
            if (view.shouldCreate())
                setDatasets(listWidget, presenter.getDatasets());
        } catch (Exception e) {
             messagePanel.setError(e.getMessage());
        }
    }
    
    protected void setDatasetsFromStepWindow(ListWidget listWidget, EmfDataset[] datasets){
        listWidget.removeAll();
        for (int i = 0; i < datasets.length; i++) {
            listWidget.addElement(datasets[i]);
        }
    }
    
    protected void setDatasets(ListWidget listWidget, EmfDataset [] datasets) {
        listWidget.removeAllElements();
        for (int i = 0; i < datasets.length; i++) {
           //System.out.println(" Inv dataset is: " + datasets[i]);
            listWidget.addElement(datasets[i]);
        }
        
    }
    
    protected Object getCAPInvDataset() {
        return (capInvListWidget.getAllElements() != null && capInvListWidget.getAllElements().length > 0) ? capInvListWidget.getAllElements()[0] : null;
    }
   
    protected Object getHAPInvDataset() {
        return (hapInvListWidget.getAllElements() != null && hapInvListWidget.getAllElements().length > 0) ? hapInvListWidget.getAllElements()[0] : null;
    }
    
    protected Object getSpeciationToolGasProfileDataset() {
        return (speciationToolGasProfileDatasetListWidget.getAllElements() != null && speciationToolGasProfileDatasetListWidget.getAllElements().length > 0) ? speciationToolGasProfileDatasetListWidget.getAllElements()[0] : null;
    }
    
    protected Object getPollToPollConversionDataset() {
        return (pollToPollConversionDatasetListWidget.getAllElements() != null && pollToPollConversionDatasetListWidget.getAllElements().length > 0) ? pollToPollConversionDatasetListWidget.getAllElements()[0] : null;
    }
    
    protected Object[] getSpeciationCrossReferenceDatasets() {
        return (speciationCrossReferenceDatasetsListWidget.getAllElements() != null && speciationCrossReferenceDatasetsListWidget.getAllElements().length > 0) ? speciationCrossReferenceDatasetsListWidget.getAllElements() : null;
    }
    
    protected Object[] getSpeciationProfileWeightDatasets() {
        return (speciationProfileWeightDatasetsListWidget.getAllElements() != null && speciationProfileWeightDatasetsListWidget.getAllElements().length > 0) ? speciationProfileWeightDatasetsListWidget.getAllElements() : null;
    }
    
    protected String getSummaryType(){
       if (summaryTypes.getSelectedItem()==null)
           return ""; 
       return summaryTypes.getSelectedItem().toString();
   }
   
}
