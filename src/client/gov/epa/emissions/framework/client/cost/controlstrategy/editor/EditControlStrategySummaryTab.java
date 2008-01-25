package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.EditableComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.Projects;
import gov.epa.emissions.framework.client.data.Regions;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.controlmeasure.YearValidation;
import gov.epa.emissions.framework.services.cost.data.ControlStrategyResultsSummary;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.DoubleTextField;
import gov.epa.emissions.framework.ui.IntTextField;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.NumberFieldVerifier;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class EditControlStrategySummaryTab extends JPanel implements EditControlStrategySummaryTabView {

    private ControlStrategy controlStrategy;

    private ManageChangeables changeablesList;

    private TextField name;

    private TextArea description;

    private DoubleTextField discountRate;

    private EditableComboBox projectsCombo;

    private EmfSession session;

    private Dimension comboSize = new Dimension(200, 20);

    private MessagePanel messagePanel;

    private IntTextField costYear;

    private EditableComboBox regionsCombo;

    private IntTextField inventoryYear;

    private ComboBox majorPollutant;

    private Region[] allRegions;

    private Project[] allProjects;

    protected EmfConsole parentConsole;

    private JLabel startDate, completionDate, user, costValue, emissionReductionValue;

    private ComboBox strategyTypeCombo;

    protected JCheckBox useCostEquationCheck, useSQLApproachCheck;

    private ControlStrategyResult[] controlStrategyResults;

    private DecimalFormat decFormat;

    private CostYearTable costYearTable;

    private NumberFieldVerifier verifier;

    public EditControlStrategySummaryTab(ControlStrategy controlStrategy,
            ControlStrategyResult[] controlStrategyResults, EmfSession session, ManageChangeables changeablesList,
            MessagePanel messagePanel, EmfConsole parentConsole, CostYearTable costYearTable) throws EmfException {
        super.setName("summary");
        this.controlStrategy = controlStrategy;
        this.controlStrategyResults = controlStrategyResults;
        this.session = session;
        this.changeablesList = changeablesList;
        this.messagePanel = messagePanel;
        this.parentConsole = parentConsole;
        this.decFormat = new DecimalFormat("0.###E0");
        this.costYearTable = costYearTable;
        this.verifier = new NumberFieldVerifier("Summary tab: ");

        setLayout();
    }

    private void setLayout() throws EmfException {
        super.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(getBorderedPanel(createmMainSection(), ""), BorderLayout.CENTER);
        panel.add(createLowerSection(), BorderLayout.SOUTH);
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
        
        layoutGenerator.addLabelWidgetPair("Project:", projects(), panelTop);
        layoutGenerator.addLabelWidgetPair("Creator:", creator(), panelTop);
        layoutGenerator.addLabelWidgetPair("Last Modified Date:", lastModifiedDate(), panelTop);
        layoutGenerator.addLabelWidgetPair("Copied From:", new JLabel("   "), panelTop);
        layoutGenerator.makeCompactGrid(panelTop, 6, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad
        panel.add(panelTop);
        
        JPanel panelBottom = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator1 = new SpringLayoutGenerator();

        layoutGenerator1.addLabelWidgetPair("Type of Analysis:", typeOfAnalysis(), panelBottom);
        layoutGenerator1.addLabelWidgetPair("Use SQL Approach:", useSQLApproach(), panelBottom);
        // Lay out the panel.
        layoutGenerator1.makeCompactGrid(panelBottom, 1, 4, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        panel.add(panelBottom);
        return panel;
    }

    private ComboBox typeOfAnalysis() throws EmfException {
        StrategyType[] types = session.controlStrategyService().getStrategyTypes();
        strategyTypeCombo = new ComboBox("Choose a strategy type", types);
        strategyTypeCombo.setSelectedItem(controlStrategy.getStrategyType());
        changeablesList.addChangeable(strategyTypeCombo);

        return strategyTypeCombo;
    }

    private JPanel createLowerSection() throws EmfException {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(getBorderedPanel(createLowerLeftSection(), "Parameters"), BorderLayout.WEST);
        panel.add(resultsPanel(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel getBorderedPanel(JPanel component, String border) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new Border(border));
        panel.add(component);

        return panel;
    }

    private JPanel createLowerLeftSection() throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        // panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        // layoutGenerator.addLabelWidgetPair("Discount Rate:", discountRateTextField(), panel);
        layoutGenerator.addLabelWidgetPair("Cost Year:", costYearTextField(), panel);
        layoutGenerator.addLabelWidgetPair("Inventory Year:", inventoryYearTextField(), panel);
        layoutGenerator.addLabelWidgetPair("Region:", regions(), panel);
        layoutGenerator.addLabelWidgetPair("Target Pollutant:", majorPollutants(), panel);
        layoutGenerator.addLabelWidgetPair("Discount Rate (%):", discountRate(), panel);
        layoutGenerator.addLabelWidgetPair("Use Cost Equations:", useCostEquation(), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 6, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private DoubleTextField discountRate() {
        discountRate = new DoubleTextField("discount rate", 1, 20, 10);
        discountRate.setValue((controlStrategy.getDiscountRate()));
        discountRate.setToolTipText("This value is only used for point sources");
        changeablesList.addChangeable(discountRate);
        return discountRate;
    }

    private JCheckBox useCostEquation() {

        useCostEquationCheck = new JCheckBox(" ", null, controlStrategy.getUseCostEquations());
        return useCostEquationCheck;
    }

    private JCheckBox useSQLApproach() {

        useSQLApproachCheck = new JCheckBox(" ", null, true);
        return useSQLApproachCheck;
    }

    private IntTextField costYearTextField() {
        costYear = new IntTextField("cost year", 0, Integer.MAX_VALUE, 10);
        costYear.setValue(controlStrategy.getCostYear());
        return costYear;
    }

    private IntTextField inventoryYearTextField() {
        inventoryYear = new IntTextField("Inventory year", 0, Integer.MAX_VALUE, 10);
        if (controlStrategy.getInventoryYear() != 0)
            inventoryYear.setValue(controlStrategy.getInventoryYear());
        return inventoryYear;
    }

    private JLabel lastModifiedDate() {
        return createLeftAlignedLabel(CustomDateFormat.format_MM_DD_YYYY_HH_mm(controlStrategy.getLastModifiedDate()));
    }

    private JLabel creator() {
        return createLeftAlignedLabel(controlStrategy.getCreator().getName());
    }

    private TextArea description() {
        description = new TextArea("description", controlStrategy.getDescription(), 40, 3);
        changeablesList.addChangeable(description);

        return description;
    }

    private TextField name() {
        name = new TextField("name", 40);
        name.setText(controlStrategy.getName());
        name.setMaximumSize(new Dimension(300, 15));
        changeablesList.addChangeable(name);

        return name;
    }

    private EditableComboBox projects() throws EmfException {
        allProjects = session.dataCommonsService().getProjects();
        projectsCombo = new EditableComboBox(allProjects);
        projectsCombo.setSelectedItem(controlStrategy.getProject());
        projectsCombo.setPreferredSize(comboSize);
        changeablesList.addChangeable(projectsCombo);

        return projectsCombo;
    }

    private EditableComboBox regions() throws EmfException {
        allRegions = session.dataCommonsService().getRegions();
        regionsCombo = new EditableComboBox(allRegions);
        regionsCombo.setSelectedItem(controlStrategy.getRegion());
        regionsCombo.setPreferredSize(comboSize);

        changeablesList.addChangeable(regionsCombo);

        return regionsCombo;
    }

    private ComboBox majorPollutants() throws EmfException {
        Pollutant[] pollutants = getAllPollutants(this.session);
        majorPollutant = new ComboBox(pollutants);
        majorPollutant.setSelectedItem(controlStrategy.getTargetPollutant());
        majorPollutant.setPreferredSize(comboSize);

        changeablesList.addChangeable(majorPollutant);

        return majorPollutant;
    }

    private Pollutant[] getAllPollutants(EmfSession session) throws EmfException {
        return session.dataCommonsService().getPollutants();
    }

    private JLabel createLeftAlignedLabel(String name) {
        JLabel label = new JLabel(name);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        return label;
    }

    private JPanel resultsPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Results"));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        user = new JLabel("");
        user.setBackground(Color.white);
        
        startDate = new JLabel("");
        startDate.setBackground(Color.white);

        completionDate = new JLabel("");
        completionDate.setBackground(Color.white);

        costValue = new JLabel("");
        costValue.setBackground(Color.white);

        emissionReductionValue = new JLabel("");
        emissionReductionValue.setBackground(Color.white);

        updateSummaryResultPanel(controlStrategy, controlStrategyResults);

        layoutGenerator.addLabelWidgetPair("Start Date:", startDate, panel);
        layoutGenerator.addLabelWidgetPair("Completion Date:", completionDate, panel);
        layoutGenerator.addLabelWidgetPair("User:", user, panel);
        layoutGenerator.addLabelWidgetPair("Total Annualized Cost:", costValue, panel);
        layoutGenerator.addLabelWidgetPair("Target Poll. Reduction:", emissionReductionValue, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 5, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    public void save(ControlStrategy controlStrategy) throws EmfException {
        messagePanel.clear();
        controlStrategy.setName(name.getText());
        controlStrategy.setDescription(description.getText());
        updateProject();

        // isDatasetSelected(controlStrategy);
        controlStrategy.setCostYear(new YearValidation("Cost Year").value(costYear.getText(), costYearTable
                .getStartYear(), costYearTable.getEndYear()));
        controlStrategy.setInventoryYear(new YearValidation("Inventory Year").value(inventoryYear.getText()));
        updateRegion();
        controlStrategy.setTargetPollutant(checkMajorPollutant());

        controlStrategy.setDiscountRate(checkDiscountRate());
        controlStrategy.setStrategyType(checkStrategyType());
        controlStrategy.setUseCostEquations(useCostEquationCheck.isSelected());
    }

    private double checkDiscountRate() throws EmfException {
        // check to see that it's not empty
        if (discountRate.getText().trim().length() == 0)
            throw new EmfException("Enter the Discount Rate as a percentage (e.g., 9 for 9% percent)");

        double value = verifier.parseDouble(discountRate.getText());

        // make sure the number makes sense...
        if (value < 1 || value > 20) {
            throw new EmfException("Enter the Discount Rate as a percent between 1 and 20 (e.g., 7% is entered as 7)");
        }
        return value;

    }

    private StrategyType checkStrategyType() throws EmfException {
        StrategyType strategyType = (StrategyType) this.strategyTypeCombo.getSelectedItem();
        if (strategyType == null)
            throw new EmfException("Please select a strategy type");
        return strategyType;
    }

    private Pollutant checkMajorPollutant() throws EmfException {
        Pollutant pollutant = (Pollutant) majorPollutant.getSelectedItem();
        if (pollutant == null) {
            throw new EmfException("Please select a target pollutant");
        }
        return pollutant;
    }

    // private void isDatasetSelected(ControlStrategy controlStrategy) throws EmfException {
    // if (controlStrategy.getControlStrategyInputDatasets().length == 0) {
    // throw new EmfException("Please select a dataset");
    // }
    // }

    private void updateRegion() {
        Object selected = regionsCombo.getSelectedItem();
        if (selected instanceof String) {
            String regionName = ((String) selected).trim();
            if (regionName.length() > 0) {
                Region region = region(regionName);// checking for duplicates
                controlStrategy.setRegion(region);
            }
        } else if (selected instanceof Region) {
            controlStrategy.setRegion((Region) selected);
        }
    }

    private Region region(String regionName) {
        return new Regions(allRegions).get(regionName);
    }

    private void updateProject() {
        Object selected = projectsCombo.getSelectedItem();
        if (selected instanceof String) {
            String projectName = ((String) selected).trim();
            if (projectName.length() > 0) {
                Project project = project(projectName);// checking for duplicates
                controlStrategy.setProject(project);
            }
        } else if (selected instanceof Project) {
            controlStrategy.setProject((Project) selected);
        }
    }

    private Project project(String projectName) {
        return new Projects(allProjects).get(projectName);
    }

    public void setRunMessage(ControlStrategy controlStrategy) {
        messagePanel.clear();
        updateStartDate(controlStrategy);
        updateSummaryPanelValuesExceptStartDate("Running", "", "", "");
    }

    public void stopRun() {
        // TODO:
    }

    public void refresh(ControlStrategyResult[] controlStrategyResults) {
        messagePanel.clear();
        updateSummaryResultPanel(controlStrategy, controlStrategyResults);
    }

    private void updateSummaryResultPanel(ControlStrategy controlStrategy,
            ControlStrategyResult[] controlStrategyResults) {
        if (controlStrategyResults == null || controlStrategyResults.length == 0) {
            updateStartDate(controlStrategy);
            updateSummaryPanelValuesExceptStartDate("", "", "", "");
            return;
        }
        ControlStrategyResultsSummary summary = new ControlStrategyResultsSummary(controlStrategyResults);
        String runStatus = summary.getRunStatus();
        String completionTime = runStatus.indexOf("Failed") == -1 ? summary.getCompletionTime() : "Failed";
        String userName =controlStrategy.getCreator().getName();
//        String userName= summary.getUser().getName()== null ? summary.getUser().getName() : "";
        String startTime = summary.getStartTime()== null ||summary.getStartTime().trim()==""? "":summary.getStartTime();
//        String startTime = controlStrategy.getStartDate();
        updateStartDate(startTime);
        updateSummaryPanelValuesExceptStartDate(""+completionTime, "" + userName , "" + summary.getStrategyTotalCost(), 
                ""+ summary.getStrategyTotalReduction());
    }

    private void updateStartDate(String startTime) {
        startDate.setText(startTime);
    }

    private void updateStartDate(ControlStrategy controlStrategy) {
        String startDateString = CustomDateFormat.format_MM_DD_YYYY_HH_mm_ss(controlStrategy.getStartDate());
        startDate.setText((startDateString == null ||startDateString.trim()=="" ? "Not started" : startDateString));
    }

    private void updateSummaryPanelValuesExceptStartDate(String closeDate, String userName,  String cost, String emisReduction) {
        completionDate.setText(closeDate);
        user.setText(userName);
        costValue.setText(cost.length() == 0 ? "" : decFormat.format(new Double(cost)));
        emissionReductionValue.setText(emisReduction.length() == 0 ? "" : decFormat.format(new Double(emisReduction)));
    }

}
