package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.EditableComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.casemanagement.Abbreviations;
import gov.epa.emissions.framework.client.casemanagement.AirQualityModels;
import gov.epa.emissions.framework.client.casemanagement.CaseCategories;
import gov.epa.emissions.framework.client.casemanagement.EmissionsYears;
import gov.epa.emissions.framework.client.casemanagement.Grids;
import gov.epa.emissions.framework.client.casemanagement.MeteorlogicalYears;
import gov.epa.emissions.framework.client.casemanagement.RunStatuses;
import gov.epa.emissions.framework.client.casemanagement.Speciations;
import gov.epa.emissions.framework.client.data.EmfDateFormat;
import gov.epa.emissions.framework.client.data.Projects;
import gov.epa.emissions.framework.client.data.Regions;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class EditableCaseSummaryTab extends JPanel implements EditableCaseSummaryTabView {

    public final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(EmfDateFormat.format());

    private Case caseObj;

    private ManageChangeables changeablesList;

    private TextField name;

    private TextArea description;

    private EditableComboBox projectsCombo;

    private EmfSession session;

    private EditableComboBox regionsCombo;

    private EditableComboBox abbreviationsCombo;

    private EditableComboBox airQualityModelsCombo;

    private EditableComboBox categoriesCombo;

    private EditableComboBox emissionsYearCombo;

    private EditableComboBox gridCombo;

    private EditableComboBox meteorlogicalYearCombo;

    private EditableComboBox speciationCombo;

    private Abbreviations abbreviations;

    private Projects projects;

    private AirQualityModels airQualityModels;

    private CaseCategories categories;

    private EmissionsYears emissionsYears;

    private Grids grids;

    private MeteorlogicalYears meteorlogicalYears;

    private Speciations speciations;

    private Regions regions;

    private RunStatuses runStatuses;

    private ComboBox runStatusCombo;

    public EditableCaseSummaryTab(Case caseObj, EmfSession session, ManageChangeables changeablesList) throws EmfException {
        super.setName("summary");
        this.caseObj = caseObj;
        this.session = session;
        this.changeablesList = changeablesList;

        setLayout();
    }

    private void setLayout() throws EmfException {
        super.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(createOverviewSection());
        panel.add(createLowerSection());

        super.add(panel, BorderLayout.CENTER);
    }

    private JPanel createOverviewSection() throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Name:", name(), panel);
        layoutGenerator.addLabelWidgetPair("Description:", new ScrollableComponent(description()), panel);
        layoutGenerator.addLabelWidgetPair("Category:", categories(), panel);
        layoutGenerator.addLabelWidgetPair("Project:", projects(), panel);
        layoutGenerator.addLabelWidgetPair("Creator:", creator(), panel);
        layoutGenerator.addLabelWidgetPair("Last Modified Date:", lastModifiedDate(), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 6, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JPanel createLowerSection() throws EmfException {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));

        JPanel container = new JPanel();
        container.add(createLowerLeftSection());
        container.add(createLowerRightSection());

        panel.add(container, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLowerLeftSection() throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Emissions Year:", emissionsYears(), panel);
        layoutGenerator.addLabelWidgetPair("Grid:", grids(), panel);
        layoutGenerator.addLabelWidgetPair("Meteorlogical Year:", meteorlogicalYears(), panel);
        layoutGenerator.addLabelWidgetPair("Region:", regions(), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 4, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JPanel createLowerRightSection() throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Abbreviation:", abbreviations(), panel);
        layoutGenerator.addLabelWidgetPair("Air Quality Model:", airQualityModels(), panel);
        layoutGenerator.addLabelWidgetPair("Speciation:", speciations(), panel);
        layoutGenerator.addLabelWidgetPair("Run Status:", runStatus(), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 4, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JLabel lastModifiedDate() {
        return createLeftAlignedLabel(format(caseObj.getLastModifiedDate()));
    }

    private JLabel creator() {
        return createLeftAlignedLabel(caseObj.getLastModifiedBy().getName());
    }

    private TextArea description() {
        description = new TextArea("description", caseObj.getDescription(), 40, 3);
        changeablesList.addChangeable(description);

        return description;
    }

    private TextField name() {
        name = new TextField("name", 25);
        name.setText(caseObj.getName());
        name.setMaximumSize(new Dimension(300, 15));
        changeablesList.addChangeable(name);

        return name;
    }

    private EditableComboBox projects() throws EmfException {
        projects = new Projects(session.dataCommonsService().getProjects());
        projectsCombo = new EditableComboBox(projects.names());
        String name = caseObj.getProject() != null ? caseObj.getProject().getName() : "";
        projectsCombo.setSelectedItem(name);
        projectsCombo.setPreferredSize(new Dimension(250, 20));

        changeablesList.addChangeable(projectsCombo);

        return projectsCombo;
    }

    private EditableComboBox regions() throws EmfException {
        regions = new Regions(session.dataCommonsService().getRegions());
        regionsCombo = new EditableComboBox(regions.names());

        String name = caseObj.getControlRegion() != null ? caseObj.getControlRegion().getName() : "";
        regionsCombo.setSelectedItem(name);
        regionsCombo.setPreferredSize(new Dimension(250, 20));

        changeablesList.addChangeable(regionsCombo);

        return regionsCombo;
    }

    private EditableComboBox abbreviations() throws EmfException {
        abbreviations = new Abbreviations(session.caseService().getAbbreviations());
        abbreviationsCombo = new EditableComboBox(abbreviations.names());

        String name = caseObj.getAbbreviation() != null ? caseObj.getAbbreviation().getName() : "";
        abbreviationsCombo.setSelectedItem(name);
        abbreviationsCombo.setPreferredSize(new Dimension(250, 20));

        changeablesList.addChangeable(abbreviationsCombo);

        return abbreviationsCombo;
    }

    private EditableComboBox airQualityModels() throws EmfException {
        airQualityModels = new AirQualityModels(session.caseService().getAirQualityModels());
        airQualityModelsCombo = new EditableComboBox(airQualityModels.names());

        String name = caseObj.getAirQualityModel() != null ? caseObj.getAirQualityModel().getName() : "";
        airQualityModelsCombo.setSelectedItem(name);
        airQualityModelsCombo.setPreferredSize(new Dimension(250, 20));

        changeablesList.addChangeable(airQualityModelsCombo);

        return airQualityModelsCombo;
    }

    private EditableComboBox categories() throws EmfException {
        categories = new CaseCategories(session.caseService().getCaseCategories());
        categoriesCombo = new EditableComboBox(categories.names());

        String name = caseObj.getCaseCategory() != null ? caseObj.getCaseCategory().getName() : "";
        categoriesCombo.setSelectedItem(name);
        categoriesCombo.setPreferredSize(new Dimension(250, 20));

        changeablesList.addChangeable(categoriesCombo);

        return categoriesCombo;
    }

    private EditableComboBox emissionsYears() throws EmfException {
        emissionsYears = new EmissionsYears(session.caseService().getEmissionsYears());
        emissionsYearCombo = new EditableComboBox(emissionsYears.names());

        String name = caseObj.getEmissionsYear() != null ? caseObj.getEmissionsYear().getName() : "";
        emissionsYearCombo.setSelectedItem(name);
        emissionsYearCombo.setPreferredSize(new Dimension(250, 20));

        changeablesList.addChangeable(emissionsYearCombo);

        return emissionsYearCombo;
    }

    private EditableComboBox grids() throws EmfException {
        grids = new Grids(session.caseService().getGrids());
        gridCombo = new EditableComboBox(grids.names());

        String name = caseObj.getGrid() != null ? caseObj.getGrid().getName() : "";
        gridCombo.setSelectedItem(name);
        gridCombo.setPreferredSize(new Dimension(250, 20));

        changeablesList.addChangeable(gridCombo);

        return gridCombo;
    }

    private EditableComboBox meteorlogicalYears() throws EmfException {
        meteorlogicalYears = new MeteorlogicalYears(session.caseService().getMeteorlogicalYears());
        meteorlogicalYearCombo = new EditableComboBox(meteorlogicalYears.names());

        String name = caseObj.getMeteorlogicalYear() != null ? caseObj.getMeteorlogicalYear().getName() : "";
        meteorlogicalYearCombo.setSelectedItem(name);
        meteorlogicalYearCombo.setPreferredSize(new Dimension(250, 20));

        changeablesList.addChangeable(meteorlogicalYearCombo);

        return meteorlogicalYearCombo;
    }

    private EditableComboBox speciations() throws EmfException {
        speciations = new Speciations(session.caseService().getSpeciations());
        speciationCombo = new EditableComboBox(speciations.all());

        String name = caseObj.getSpeciation() != null ? caseObj.getSpeciation().getName() : "";
        speciationCombo.setSelectedItem(name);
        speciationCombo.setPreferredSize(new Dimension(250, 20));

        changeablesList.addChangeable(speciationCombo);

        return speciationCombo;
    }

    private ComboBox runStatus() {
        runStatuses = new RunStatuses();
        String name = caseObj.getRunStatus() != null ? caseObj.getRunStatus() : "";
        runStatusCombo = new ComboBox(name, runStatuses.all());
        runStatusCombo.setPreferredSize(new Dimension(250, 20));

        changeablesList.addChangeable(runStatusCombo);

        return runStatusCombo;
    }

    private String format(Date date) {
        return DATE_FORMATTER.format(date);
    }

    private JLabel createLeftAlignedLabel(String name) {
        JLabel label = new JLabel(name);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        return label;
    }

    public void save(Case caseObj) {
        caseObj.setName(name.getText());
        caseObj.setDescription(description.getText());

        caseObj.setProject(projects.get((String) projectsCombo.getSelectedItem()));
        caseObj.setControlRegion(regions.get((String) regionsCombo.getSelectedItem()));
        caseObj.setAbbreviation(abbreviations.get((String) abbreviationsCombo.getSelectedItem()));
        caseObj.setAirQualityModel(airQualityModels.get((String) airQualityModelsCombo.getSelectedItem()));
        caseObj.setCaseCategory(categories.get((String) categoriesCombo.getSelectedItem()));
        caseObj.setEmissionsYear(emissionsYears.get((String) emissionsYearCombo.getSelectedItem()));
        caseObj.setGrid(grids.get((String) gridCombo.getSelectedItem()));
        caseObj.setMeteorlogicalYear(meteorlogicalYears.get((String) meteorlogicalYearCombo.getSelectedItem()));
        caseObj.setSpeciation(speciations.get((String) speciationCombo.getSelectedItem()));
        caseObj.setRunStatus((String) runStatusCombo.getSelectedItem());
    }

}
