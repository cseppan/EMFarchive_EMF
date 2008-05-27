package gov.epa.emissions.framework.client.cost.controlprogram.editor;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionDialog;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlProgram;
import gov.epa.emissions.framework.services.cost.ControlProgramType;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class EditControlProgramSummaryTab extends JPanel implements EditControlProgramTabView {

    private EditControlProgramSummaryTabPresenter presenter;

    private ControlProgram controlProgram;

    private ManageChangeables changeablesList;

    private TextField name, startDate, endDate;

    private TextArea description;

    private EmfSession session;

    private MessagePanel messagePanel;

    protected EmfConsole parentConsole;

    private ComboBox controlProgramTypeCombo;

    protected JCheckBox useCostEquationCheck;

//    private DecimalFormat decFormat;
//
//    private NumberFieldVerifier verifier;
    
    private TextField dataset;

    protected ComboBox version;

    private ComboBox dsType;
    
    private Dimension preferredSize = new Dimension(450, 25);

    private Button selectButton;

    public EditControlProgramSummaryTab(ControlProgram controlProgram, EmfSession session, 
            ManageChangeables changeablesList, MessagePanel messagePanel, 
            EmfConsole parentConsole) {
        super.setName("summary");
        this.controlProgram = controlProgram;
        this.session = session;
        this.changeablesList = changeablesList;
        this.messagePanel = messagePanel;
        this.parentConsole = parentConsole;
//        this.decFormat = new DecimalFormat("0.###E0");
//        this.verifier = new NumberFieldVerifier("Summary tab: ");
//        setLayout();
    }

    public void display(ControlProgram controlProgram) throws EmfException {
        setLayout();
    }

    private void setLayout() throws EmfException {
        super.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(getBorderedPanel(createmMainSection(), ""), BorderLayout.CENTER);
        super.add(panel, BorderLayout.CENTER);
    }

    private JPanel createmMainSection() throws EmfException {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        JPanel panelTop = new JPanel(new SpringLayout());
        // panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Name:", name(), panelTop);
        layoutGenerator.addLabelWidgetPair("Description:", new ScrollableComponent(description()), panelTop);
        
        layoutGenerator.addLabelWidgetPair("Start:", start(), panelTop);
        layoutGenerator.addLabelWidgetPair("End:", end(), panelTop);
        layoutGenerator.addLabelWidgetPair("Last Modified Date:", lastModifiedDate(), panelTop);
        layoutGenerator.addLabelWidgetPair("Creator:", creator(), panelTop);
        layoutGenerator.makeCompactGrid(panelTop, 6, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad
        panel.add(panelTop);
        
        JPanel panelBottom = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator1 = new SpringLayoutGenerator();

        layoutGenerator1.addLabelWidgetPair("Type of Control Program:", typeOfAnalysis(), panelBottom);
//        layoutGenerator1.addLabelWidgetPair("Dataset:", start(), panelBottom);
//        layoutGenerator1.addLabelWidgetPair("Dataset Version:", end(), panelBottom);
        
        
        dsType = new ComboBox(presenter.getDatasetTypes());
        dsType.setSelectedItem(controlProgram.getDataset().getDatasetType());
        dsType.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                dataset.setText("");
                fillVersions(null);
            }
        });
        changeablesList.addChangeable(dsType);
        dsType.setPreferredSize(preferredSize);
        layoutGenerator.addLabelWidgetPair("Dataset Type:", dsType, panelBottom);

        layoutGenerator.addLabelWidgetPair("Dataset:", datasetPanel(), panelBottom);

        version = new ComboBox(new Version[] { /*controlProgram.getVersion()*/ });
        fillVersions(controlProgram.getDataset());
        
        if (controlProgram.getDatasetVersion() != null)
            version.setSelectedItem(null/*controlProgram.getVersion()*/);
        
        changeablesList.addChangeable(version);
        version.setPreferredSize(preferredSize);
        layoutGenerator.addLabelWidgetPair("Version:", version, panelBottom);

        
        
        // Lay out the panel.
        layoutGenerator1.makeCompactGrid(panelBottom, 4, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        panel.add(panelBottom);
        return panel;
    }

    private JPanel datasetPanel() {

        dataset = new TextField("dataset", 38);
        dataset.setEditable(false);
        EmfDataset inputDataset = controlProgram.getDataset();
        if(inputDataset!= null )
            dataset.setText(controlProgram.getDataset().getName());

        changeablesList.addChangeable(dataset);
        dataset.setToolTipText("Press select button to choose from a dataset list.");
        selectButton = new AddButton("Select", selectAction());
        selectButton.setMargin(new Insets(1, 2, 1, 2));

        JPanel invPanel = new JPanel(new BorderLayout(5,0));

        invPanel.add(dataset, BorderLayout.LINE_START);
        invPanel.add(selectButton);
        return invPanel;
    }
    
    private ComboBox typeOfAnalysis() throws EmfException {
        ControlProgramType[] types = session.controlProgramService().getControlProgramTypes();
        controlProgramTypeCombo = new ComboBox("Choose a control program type", types);
        controlProgramTypeCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                ControlProgramType controlProgramType = (ControlProgramType)controlProgramTypeCombo.getSelectedItem();
                presenter.doChangeControlProgramType(controlProgramType);
            }
        });
        controlProgramTypeCombo.setSelectedItem(controlProgram.getControlProgramType());
        changeablesList.addChangeable(controlProgramTypeCombo);
        
        return controlProgramTypeCombo;
    }

    private JPanel getBorderedPanel(JPanel component, String border) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new Border(border));
        panel.add(component);

        return panel;
    }


    private JLabel lastModifiedDate() {
        return createLeftAlignedLabel(CustomDateFormat.format_MM_DD_YYYY_HH_mm(controlProgram.getLastModifiedDate()));
    }

    private JLabel creator() {
        return createLeftAlignedLabel(controlProgram.getCreator().getName());
    }

    private TextArea description() {
        description = new TextArea("description", controlProgram.getDescription(), 40, 3);
        changeablesList.addChangeable(description);

        return description;
    }

    private TextField name() {
        name = new TextField("name", 40);
        name.setText(controlProgram.getName());
        name.setMaximumSize(new Dimension(300, 15));
        changeablesList.addChangeable(name);

        return name;
    }

    private TextField start() {
        startDate = new TextField("start", 40);
        startDate.setText(CustomDateFormat.format_YYYY_MM_DD_HH_MM(controlProgram.getStartDate()));
        startDate.setMaximumSize(new Dimension(300, 15));
        changeablesList.addChangeable(startDate);

        return startDate;
    }

    private TextField end() {
        endDate = new TextField("end", 40);
        endDate.setText(CustomDateFormat.format_YYYY_MM_DD_HH_MM(controlProgram.getEndDate()));
        endDate.setMaximumSize(new Dimension(300, 15));
        changeablesList.addChangeable(endDate);

        return endDate;
    }

    private JLabel createLeftAlignedLabel(String name) {
        JLabel label = new JLabel(name);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        return label;
    }

    public void save(ControlProgram controlProgram) {
        messagePanel.clear();
        controlProgram.setName(name.getText());
        controlProgram.setDescription(description.getText());

        // isDatasetSelected(controlProgram);
//        controlProgram.setCostYear(new YearValidation("Cost Year").value(costYear.getText(), costYearTable
//                .getStartYear(), costYearTable.getEndYear()));
//        controlProgram.setInventoryYear(new YearValidation("Inventory Year").value(inventoryYear.getText()));
//        updateRegion();
//        controlProgram.setTargetPollutant(checkMajorPollutant());
//
//        controlProgram.setDiscountRate(checkDiscountRate());
//        controlProgram.setProgramType(checkProgramType());
//        controlProgram.setUseCostEquations(useCostEquationCheck.isSelected());
    }

    public void notifyControlProgramTypeChange(ControlProgramType controlProgramType) {
        // NOTE Auto-generated method stub
        
    }
    
    public void observe(EditControlProgramSummaryTabPresenter presenter) {
        this.presenter = presenter;
    }

    protected void fillVersions(EmfDataset dataset) {
        version.setEnabled(true);
//        version.removeAllItems();
//        if (dataset == null ){
//            version.removeAllItems();
//            return; 
//        }
        try {
            Version[] versions = presenter.getVersions(dataset);
            version.removeAllItems();
            version.setModel(new DefaultComboBoxModel(versions));
            version.revalidate();
            if (versions.length > 0)
                version.setSelectedIndex(getDefaultVersionIndex(versions, dataset));
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private int getDefaultVersionIndex(Version[] versions, EmfDataset dataset) {
        int defaultversion = dataset.getDefaultVersion();

        for (int i = 0; i < versions.length; i++)
            if (defaultversion == versions[i].getVersion())
                return i;

        return 0;
    }

    private Action selectAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    messagePanel.clear();
                    doAddWindow();
                } catch (Exception e1) {
                    messagePanel.setError(e1.getMessage());
                }
            }
        };
    }
    
    private void doAddWindow() throws Exception {
        DatasetType type = (DatasetType) dsType.getSelectedItem();
        
        if (type == null)
            throw new EmfException("Please select a valid dataset type.");
        
        DatasetType[] datasetTypes = new DatasetType[]{type};
        InputDatasetSelectionDialog view = new InputDatasetSelectionDialog (parentConsole, changeablesList);
        InputDatasetSelectionPresenter presenter = new InputDatasetSelectionPresenter(view, session, datasetTypes);
        if (datasetTypes.length == 1)
            presenter.display(datasetTypes[0], true);
        else
            presenter.display(null, true);
        if (view.shouldCreate())
            setDatasets(presenter.getDatasets());
    }
    
    private void setDatasets(EmfDataset [] datasets) {
        dataset.setText(datasets[0].getName());
        controlProgram.setDataset(datasets[0]);
        fillVersions(datasets[0]);
    }
}