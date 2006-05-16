package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.EditableComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.data.EmfDateFormat;
import gov.epa.emissions.framework.client.data.Projects;
import gov.epa.emissions.framework.client.data.Regions;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.DoubleTextField;
import gov.epa.emissions.framework.ui.IntTextField;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
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

    private DoubleTextField discountRate;

    private MessagePanel messagePanel;

    private IntTextField costYear;

    private EditableComboBox regionsCombo;

    private IntTextField analysisYear;

    private ComboBox datasetTypeCombo;

    private TextField majorPollutant;

    private Region[] allRegions;

    private Project[] allProjects;

    public EditControlStrategySummaryTab(ControlStrategy controlStrategy, EmfSession session,
            ManageChangeables changeablesList, MessagePanel messagePanel) throws EmfException {
        super.setName("summary");
        this.controlStrategy = controlStrategy;
        this.session = session;
        this.changeablesList = changeablesList;
        this.messagePanel = messagePanel;

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
                10, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JPanel datasetPanel() {
        TextArea datasetsTextArea = new TextArea("datasets", selectedDatasets(controlStrategy));
        JScrollPane scrollPane = new JScrollPane(datasetsTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        Button chooseButton = new Button("Choose", chooseDatasetAction());
        JPanel choosePanel = new JPanel();
        choosePanel.setLayout(new BoxLayout(choosePanel, BoxLayout.Y_AXIS));
        choosePanel.add(Box.createVerticalStrut(20));
        choosePanel.add(chooseButton);
        choosePanel.add(Box.createVerticalStrut(20));

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(scrollPane);
        panel.add(choosePanel, BorderLayout.EAST);
        return panel;
    }

    private Action chooseDatasetAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
                messagePanel.setMessage("Under Construction");
            }
        };
    }

    private String selectedDatasets(ControlStrategy controlStrategy) {
        EmfDataset[] datasets = controlStrategy.getDatasets();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < datasets.length - 1; i++) {
            sb.append(datasets[i].getName() + "\n");
        }
        if (datasets.length > 0)
            sb.append(datasets[datasets.length - 1].getName());

        return sb.toString();
    }

    private ComboBox datasetTypeCombo() throws EmfException {
        DatasetType[] datasetTypes = session.dataCommonsService().getDatasetTypes();
        datasetTypeCombo = new ComboBox("Choose a dataset type", datasetTypes);
        datasetTypeCombo.setSelectedItem(controlStrategy.getDatasetType());
        return datasetTypeCombo;
    }

    private ComboBox typeOfAnalysis() {
        // FIXME: temp values
        String[] analysis = { "Maximum Reduction" };
        ComboBox combo = new ComboBox("Choose an Analysis", analysis);

        return combo;
    }

    private JPanel createMiddleSection() throws EmfException {
        JPanel middlePanel = new JPanel(new SpringLayout());

        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        layoutGenerator.addLabelWidgetPair("Type of Analysis:", typeOfAnalysis(), middlePanel);
        layoutGenerator.addLabelWidgetPair("Dataset Type:", datasetTypeCombo(), middlePanel);
        layoutGenerator.addLabelWidgetPair("Datasets :", datasetPanel(), middlePanel);

        layoutGenerator.makeCompactGrid(middlePanel, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return middlePanel;
    }

    private JPanel createLowerSection() throws EmfException {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createLowerLeftSection(), BorderLayout.CENTER);
        panel.add(createLowerRightSection(), BorderLayout.EAST);
        return panel;
    }

    private JPanel createLowerLeftSection() throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Discount Rate:", discountRateTextField(), panel);
        layoutGenerator.addLabelWidgetPair("Cost Year:", costYearTextField(), panel);
        layoutGenerator.addLabelWidgetPair("Analysis Year:", analysisYearTextField(), panel);
        layoutGenerator.addLabelWidgetPair("Region:", regions(), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 4, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private DoubleTextField discountRateTextField() {
        discountRate = new DoubleTextField("disount rate", 0.0, 1.0, 20);
        discountRate.setValue(controlStrategy.getDiscountRate());
        return discountRate;
    }

    private IntTextField costYearTextField() {
        costYear = new IntTextField("cost year", 0, Integer.MAX_VALUE, 20);
        costYear.setValue(controlStrategy.getCostYear());
        return costYear;
    }

    private IntTextField analysisYearTextField() {
        analysisYear = new IntTextField("analysis year", 0, Integer.MAX_VALUE, 20);
        analysisYear.setValue(controlStrategy.getAnalysisYear());
        return analysisYear;
    }

    private JPanel createLowerRightSection() {
        JPanel lowerRightPanel = new JPanel(new BorderLayout());

        JPanel lowerRightUpperpanel = createLowerRightUpperPanel();
        lowerRightPanel.add(lowerRightUpperpanel);
        JPanel lowerRightLowerPanel = resultsPanel();
        lowerRightPanel.add(lowerRightLowerPanel, BorderLayout.SOUTH);

        return lowerRightPanel;
    }

    private JPanel createLowerRightUpperPanel() {
        JPanel panel = new JPanel(new SpringLayout());

        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        layoutGenerator.addLabelWidgetPair("Major Pollutant:", majorPollutantTextField(), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private TextField majorPollutantTextField() {
        majorPollutant = new TextField("majorPollutant", 20);
        majorPollutant.setText(controlStrategy.getMajorPollutant());
        changeablesList.addChangeable(majorPollutant);
        return majorPollutant;
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
        panel.setBorder(BorderFactory.createTitledBorder("Resutls"));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        JLabel costValue = new JLabel("  -   ");
        costValue.setBackground(Color.white);

        JLabel emissionReductionValue = new JLabel("  -   ");
        emissionReductionValue.setBackground(Color.white);

        layoutGenerator.addLabelWidgetPair("Total Cost:", costValue, panel);
        layoutGenerator.addLabelWidgetPair("Major Pollutant Reduction:", emissionReductionValue, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 2, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    public void save(ControlStrategy controlStrategy) throws EmfException {
        controlStrategy.setName(name.getText());
        controlStrategy.setDescription(description.getText());
        updateProject();
        
        controlStrategy.setDatasetType((DatasetType) datasetTypeCombo.getSelectedItem());
        
        controlStrategy.setDiscountRate(discountRate.getValue());
        controlStrategy.setCostYear(costYear.getValue());
        controlStrategy.setAnalysisYear(analysisYear.getValue());
        updateRegion();
        controlStrategy.setMajorPollutant(majorPollutant.getText());
        
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
}
