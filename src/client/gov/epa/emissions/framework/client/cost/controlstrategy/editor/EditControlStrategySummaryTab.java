package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.EditableComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.cost.controlmeasure.YearValidation;
import gov.epa.emissions.framework.client.data.EmfDateFormat;
import gov.epa.emissions.framework.client.data.Projects;
import gov.epa.emissions.framework.client.data.Regions;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResult;
import gov.epa.emissions.framework.services.cost.data.ControlStrategyResultsSummary;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.IntTextField;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class EditControlStrategySummaryTab extends JPanel implements EditControlStrategySummaryTabView {

    public final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(EmfDateFormat.format());

    private ControlStrategy controlStrategy;

    private ManageChangeables changeablesList;

    private TextField name;

    private TextArea description;

    private EditableComboBox projectsCombo;

    private EmfSession session;

    private Dimension comboSize = new Dimension(200, 20);

    private MessagePanel messagePanel;

    private IntTextField costYear;

    private EditableComboBox regionsCombo;

    private IntTextField analysisYear;

    private ComboBox datasetTypeCombo;

    private ComboBox majorPollutant;

    private Region[] allRegions;

    private Project[] allProjects;

    protected EmfConsole parentConsole;

    private TextField datasetTextField;

    private VersionPanel versionPanel;

    private JLabel startDate, completionDate, costValue, emissionReductionValue;

    private ComboBox strategyTypeCombo;

    public EditControlStrategySummaryTab(ControlStrategy controlStrategy, EmfSession session,
            ManageChangeables changeablesList, MessagePanel messagePanel, EmfConsole parentConsole) throws EmfException {
        super.setName("summary");
        this.controlStrategy = controlStrategy;
        this.session = session;
        this.changeablesList = changeablesList;
        this.messagePanel = messagePanel;
        this.parentConsole = parentConsole;

        setLayout();
    }

    private void setLayout() throws EmfException {
        super.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createmMainSection(), BorderLayout.NORTH);
        panel.add(createMiddleSection(), BorderLayout.CENTER);
        panel.add(createLowerSection(), BorderLayout.SOUTH);

        super.add(panel, BorderLayout.CENTER);
    }

    private JPanel createmMainSection() throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Name:", name(), panel);
        layoutGenerator.addLabelWidgetPair("Description:", new ScrollableComponent(description()), panel);
        layoutGenerator.addLabelWidgetPair("Project:", projects(), panel);
        layoutGenerator.addLabelWidgetPair("Creator:", creator(), panel);
        layoutGenerator.addLabelWidgetPair("Last Modified Date:", lastModifiedDate(), panel);
        layoutGenerator.addLabelWidgetPair("Copied From:", new JLabel("   "), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 6, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JPanel datasetAndVersionPanel() throws EmfException {
        JPanel panel = new JPanel(new BorderLayout(25, 5));
        panel.add(datasetPanel());

        versionPanel = new VersionPanel(controlStrategy, session, changeablesList);
        panel.add(versionPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel datasetPanel() {
        datasetTextField = new TextField("datasets", 25);
        datasetTextField.setEditable(false);
        datasetTextField.setText(selectedDatasets(controlStrategy.getInputDatasets()));
        changeablesList.addChangeable(datasetTextField);

        Button chooseButton = new Button("Choose", chooseDatasetAction());

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(datasetTextField);
        panel.add(chooseButton, BorderLayout.EAST);
        return panel;
    }

    private Action chooseDatasetAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    DatasetType datasetType = selectedDatasetType();
                    DatasetChooserDialog dialog = new DatasetChooserDialog(datasetType, session, parentConsole,
                            EditControlStrategySummaryTab.this);
                    dialog.show();
                    EmfDataset dataset = dialog.dataset();
                    if (dataset != null) {
                        datasetTextField.setText(dataset.getName());
                        controlStrategy.setInputDatasets(new EmfDataset[] { dataset });
                        Version[] versions = session.dataEditorService().getVersions(dataset.getId());
                        versionPanel.update(versions);
                    }
                } catch (EmfException exp) {
                    messagePanel.setError(exp.getMessage());
                }
            }
        };
    }

    protected DatasetType selectedDatasetType() throws EmfException {
        DatasetType datasetType = (DatasetType) datasetTypeCombo.getSelectedItem();
        if (datasetType == null) {
            throw new EmfException("Please select an inventory type");
        }
        return datasetType;
    }

    private String selectedDatasets(EmfDataset[] datasets) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < datasets.length - 1; i++) {
            sb.append(datasets[i].getName() + "\n");
        }
        if (datasets.length > 0)
            sb.append(datasets[datasets.length - 1].getName());

        return sb.toString();
    }

    private ComboBox datasetTypeCombo() throws EmfException {
        DatasetType[] datasetTypes = getORLTypes();
        datasetTypeCombo = new ComboBox("Choose an inventory type", datasetTypes);
        datasetTypeCombo.setSelectedItem(controlStrategy.getDatasetType());
        changeablesList.addChangeable(datasetTypeCombo);
        return datasetTypeCombo;
    }

    private DatasetType[] getORLTypes() throws EmfException {
        List orlTypes = new ArrayList();
        DatasetType[] datasetTypes = session.dataCommonsService().getDatasetTypes();
        for (int i = 0; i < datasetTypes.length; i++)
            if (datasetTypes[i].getImporterClassName().indexOf("ORL") >= 0)
                orlTypes.add(datasetTypes[i]);

        return (DatasetType[]) orlTypes.toArray(new DatasetType[0]);
    }

    private ComboBox typeOfAnalysis() throws EmfException {
        StrategyType[] types = session.controlStrategyService().getStrategyTypes();
        strategyTypeCombo = new ComboBox("Choose a strategy type", types);
        strategyTypeCombo.setSelectedItem(controlStrategy.getStrategyType());
        changeablesList.addChangeable(strategyTypeCombo);

        return strategyTypeCombo;
    }

    private JPanel createMiddleSection() throws EmfException {
        JPanel middlePanel = new JPanel(new SpringLayout());

        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        layoutGenerator.addLabelWidgetPair("Type of Analysis:", typeOfAnalysis(), middlePanel);
        layoutGenerator.addLabelWidgetPair("Inventory Type:", datasetTypeCombo(), middlePanel);
        layoutGenerator.addLabelWidgetPair("Inventory Dataset:", datasetAndVersionPanel(), middlePanel);

        layoutGenerator.makeCompactGrid(middlePanel, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return middlePanel;
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
        layoutGenerator.addLabelWidgetPair("Inventory Year:", analysisYearTextField(), panel);
        layoutGenerator.addLabelWidgetPair("Region:", regions(), panel);
        layoutGenerator.addLabelWidgetPair("Target Pollutant:", majorPollutants(), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 4, 2, // rows, cols
                5, 5, // initialX, initialY
                30, 10);// xPad, yPad

        return panel;
    }

    private IntTextField costYearTextField() {
        costYear = new IntTextField("cost year", 0, Integer.MAX_VALUE, 10);
        costYear.setValue(controlStrategy.getCostYear());
        return costYear;
    }

    private IntTextField analysisYearTextField() {
        analysisYear = new IntTextField("Inventory year", 0, Integer.MAX_VALUE, 10);
        analysisYear.setValue(controlStrategy.getAnalysisYear());
        return analysisYear;
    }

    private JLabel lastModifiedDate() {
        return createLeftAlignedLabel(format(controlStrategy.getLastModifiedDate()));
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

    private ComboBox majorPollutants() {
        String[] pollutants = { "NOx", "PM10", "PM2.5", "SO2", "VOC", "CO", "CO2", "EC", "OC", "NH3", "Hg" };
        majorPollutant = new ComboBox(pollutants);
        majorPollutant.setSelectedItem(controlStrategy.getTargetPollutant());
        majorPollutant.setPreferredSize(comboSize);

        changeablesList.addChangeable(majorPollutant);

        return majorPollutant;
    }

    private String format(Date date) {
        return DATE_FORMATTER.format(date);
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

        String startDateString = getFormmatedDate(controlStrategy.getStartDate());
        startDate = new JLabel(startDateString == null ? "Not started" : startDateString + " by "
                + controlStrategy.getCreator().getName());
        startDate.setBackground(Color.white);

        String completionDateString = getFormmatedDate(controlStrategy.getCompletionDate());
        completionDate = new JLabel(completionDateString == null ? "" : completionDateString);
        completionDate.setBackground(Color.white);

        costValue = new JLabel("" + getTotalCost(controlStrategy));
        costValue.setBackground(Color.white);

        emissionReductionValue = new JLabel("" + getReduction(controlStrategy));
        emissionReductionValue.setBackground(Color.white);

        layoutGenerator.addLabelWidgetPair("Start Date:", startDate, panel);
        layoutGenerator.addLabelWidgetPair("Completion Date:", completionDate, panel);
        layoutGenerator.addLabelWidgetPair("Total Annualized Cost:", costValue, panel);
        layoutGenerator.addLabelWidgetPair("Target Poll. Reduction:", emissionReductionValue, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 4, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private double getReduction(ControlStrategy cs) {
        StrategyResult[] results = cs.getStrategyResults();
        double totalReduction = 0;

        if (results.length > 0)
            for (int i = 0; i < results.length; i++)
                totalReduction += results[i].getTotalReduction();

        return totalReduction;
    }

    private double getTotalCost(ControlStrategy cs) {
        StrategyResult[] results = cs.getStrategyResults();
        double totalCost = 0;

        if (results.length > 0)
            for (int i = 0; i < results.length; i++)
                totalCost += results[i].getTotalCost();

        return totalCost;
    }

    private String getFormmatedDate(Date date) {
        return date == null ? null : DATE_FORMATTER.format(date);
    }

    public void save(ControlStrategy controlStrategy) throws EmfException {
        messagePanel.clear();
        controlStrategy.setName(name.getText());
        controlStrategy.setDescription(description.getText());
        updateProject();

        controlStrategy.setDatasetType(selectedDatasetType());
        isDatasetSelected(controlStrategy);
        controlStrategy.setDatasetVersion(versionPanel.datasetVersion());

        controlStrategy.setCostYear(new YearValidation("Cost Year").value((costYear.getText())));
        controlStrategy.setAnalysisYear(analysisYear.getValue());
        updateRegion();
        controlStrategy.setTargetPollutant((String) majorPollutant.getSelectedItem());
        controlStrategy.setStartDate(this.controlStrategy.getStartDate());
        controlStrategy.setRunStatus(this.controlStrategy.getRunStatus());
        controlStrategy.setProject((Project) projectsCombo.getSelectedItem());
        if (strategyTypeCombo.getSelectedIndex() == 0) {
            throw new EmfException("Please select a strategy type");
        }
        controlStrategy.setStrategyType((StrategyType) this.strategyTypeCombo.getSelectedItem());
    }

    private void isDatasetSelected(ControlStrategy controlStrategy) throws EmfException {
        if (controlStrategy.getInputDatasets().length == 0) {
            throw new EmfException("Please select a dataset");
        }
    }

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
        } else if (selected instanceof Region) {
            controlStrategy.setRegion((Region) selected);
        }
    }

    private Project project(String projectName) {
        return new Projects(allProjects).get(projectName);
    }

    public void setRunMessage(ControlStrategy controlStrategy) {
        messagePanel.clear();
        Date start = controlStrategy.getStartDate();
        if (start == null) {
            start = new Date();
            controlStrategy.setStartDate(start);
            startDate.setText(getFormmatedDate(start));
        }
        controlStrategy.setRunStatus("Running");

        completionDate.setText("Running...");
        clearResultPanel();
    }

    public void stopRun() {
        messagePanel.clear();
        controlStrategy.setRunStatus("Stopped");
        completionDate.setText("Stopped.");
        clearResultPanel();
    }

    private void clearResultPanel() {
        costValue.setText("");
        emissionReductionValue.setText("");
    }

    public void doRefresh() {
        messagePanel.clear();
        StrategyResult[] results = controlStrategy.getStrategyResults();
        if (results.length == 0) {
            completionDate.setText("");
            return;
        }

        ControlStrategyResultsSummary summary = new ControlStrategyResultsSummary(results);
        costValue.setText("" + summary.getStrategyTotalCost());
        emissionReductionValue.setText("" + summary.getStrategyTotalReduction());
    }

}
