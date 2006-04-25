package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.gui.EditableComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Abbreviation;
import gov.epa.emissions.framework.services.casemanagement.AirQualityModel;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseCategory;
import gov.epa.emissions.framework.services.casemanagement.EmissionsYear;
import gov.epa.emissions.framework.services.casemanagement.Grid;
import gov.epa.emissions.framework.services.casemanagement.MeteorlogicalYear;
import gov.epa.emissions.framework.services.casemanagement.Speciation;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class EditableCaseSummaryTab extends JPanel implements EditableCaseSummaryTabView {

    public final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("MM/dd/yyyy HH:mm");

    private Case caseObj;

    private ManageChangeables changeablesList;

    private TextField name;

    private TextArea description;

    private EditableComboBox projectsCombo;

    private EmfSession session;

    private Project[] allProjects;

    private Region[] allRegions;

    private EditableComboBox regionsCombo;

    private Abbreviation[] allAbbreviations;

    private EditableComboBox abbreviationsCombo;

    private AirQualityModel[] allAirQualityModels;

    private EditableComboBox airQualityModelsCombo;

    private CaseCategory[] allCategories;

    private EditableComboBox categoriesCombo;

    private EmissionsYear[] allEmissionsYears;

    private EditableComboBox emissionsYearCombo;

    private Grid[] allGrids;

    private EditableComboBox gridCombo;

    private MeteorlogicalYear[] allMeteorlogicalYears;

    private EditableComboBox meteorlogicalYearCombo;

    private Speciation[] allSpeciations;

    private EditableComboBox speciationCombo;

    public EditableCaseSummaryTab(Case caseObj, EmfSession session, MessagePanel messagePanel,
            ManageChangeables changeablesList) throws EmfException {
        super.setName("summary");
        this.caseObj = caseObj;
        this.session = session;
        this.changeablesList = changeablesList;

        setLayout();
    }

    private void setLayout() throws EmfException {
        super.setLayout(new BorderLayout());
        super.add(createOverviewSection(), BorderLayout.PAGE_START);
        super.add(createLowerSection(), BorderLayout.CENTER);
    }

    private JPanel createOverviewSection() throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Name:", name(), panel);
        layoutGenerator.addLabelWidgetPair("Description:", new ScrollableComponent(description()), panel);
        layoutGenerator.addLabelWidgetPair("Project:", projects(), panel);
        layoutGenerator.addLabelWidgetPair("Region:", regions(), panel);
        layoutGenerator.addLabelWidgetPair("Creator:", creator(), panel);
        layoutGenerator.addLabelWidgetPair("Last Modified Time:", lastModifiedDate(), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 6, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JPanel createLowerSection() throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Abbreviation:", abbreviations(), panel);
        layoutGenerator.addLabelWidgetPair("Air Quality Model:", airQualityModels(), panel);
        layoutGenerator.addLabelWidgetPair("Category:", categories(), panel);
        layoutGenerator.addLabelWidgetPair("Emissions Year:", emissionsYears(), panel);
        layoutGenerator.addLabelWidgetPair("Grid:", grids(), panel);
        layoutGenerator.addLabelWidgetPair("Meteorlogical Year:", meteorlogicalYears(), panel);
        layoutGenerator.addLabelWidgetPair("Speciation:", speciations(), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 7, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JLabel lastModifiedDate() {
        return createLeftAlignedLabel(format(caseObj.getLastModifiedDate()));
    }

    private JLabel creator() {
        return createLeftAlignedLabel(caseObj.getCreator().getName());
    }

    private TextArea description() {
        description = new TextArea("description", caseObj.getDescription());
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
        allProjects = session.dataCommonsService().getProjects();
        projectsCombo = new EditableComboBox(allProjects);
        projectsCombo.setSelectedItem(caseObj.getProject());
        projectsCombo.setPreferredSize(new Dimension(250, 20));

        changeablesList.addChangeable(projectsCombo);

        return projectsCombo;
    }

    private EditableComboBox abbreviations() throws EmfException {
        allAbbreviations = session.caseService().getAbbreviations();
        abbreviationsCombo = new EditableComboBox(allAbbreviations);
        abbreviationsCombo.setSelectedItem(caseObj.getAbbreviation());
        abbreviationsCombo.setPreferredSize(new Dimension(250, 20));

        changeablesList.addChangeable(abbreviationsCombo);

        return abbreviationsCombo;
    }

    private EditableComboBox airQualityModels() throws EmfException {
        allAirQualityModels = session.caseService().getAirQualityModels();
        airQualityModelsCombo = new EditableComboBox(allAirQualityModels);
        airQualityModelsCombo.setSelectedItem(caseObj.getAirQualityModel());
        airQualityModelsCombo.setPreferredSize(new Dimension(250, 20));

        changeablesList.addChangeable(airQualityModelsCombo);

        return airQualityModelsCombo;
    }

    private EditableComboBox categories() throws EmfException {
        allCategories = session.caseService().getCaseCategories();
        categoriesCombo = new EditableComboBox(allCategories);
        categoriesCombo.setSelectedItem(caseObj.getAirQualityModel());
        categoriesCombo.setPreferredSize(new Dimension(250, 20));

        changeablesList.addChangeable(categoriesCombo);

        return categoriesCombo;
    }

    private EditableComboBox emissionsYears() throws EmfException {
        allEmissionsYears = session.caseService().getEmissionsYears();
        emissionsYearCombo = new EditableComboBox(allEmissionsYears);
        emissionsYearCombo.setSelectedItem(caseObj.getEmissionsYear());
        emissionsYearCombo.setPreferredSize(new Dimension(250, 20));

        changeablesList.addChangeable(emissionsYearCombo);

        return emissionsYearCombo;
    }

    private EditableComboBox grids() throws EmfException {
        allGrids = session.caseService().getGrids();
        gridCombo = new EditableComboBox(allGrids);
        gridCombo.setSelectedItem(caseObj.getGrid());
        gridCombo.setPreferredSize(new Dimension(250, 20));

        changeablesList.addChangeable(gridCombo);

        return gridCombo;
    }

    private EditableComboBox meteorlogicalYears() throws EmfException {
        allMeteorlogicalYears = session.caseService().getMeteorlogicalYears();
        meteorlogicalYearCombo = new EditableComboBox(allMeteorlogicalYears);
        meteorlogicalYearCombo.setSelectedItem(caseObj.getMeteorlogicalYear());
        meteorlogicalYearCombo.setPreferredSize(new Dimension(250, 20));

        changeablesList.addChangeable(meteorlogicalYearCombo);

        return meteorlogicalYearCombo;
    }

    private EditableComboBox speciations() throws EmfException {
        allSpeciations = session.caseService().getSpeciations();
        speciationCombo = new EditableComboBox(allSpeciations);
        speciationCombo.setSelectedItem(caseObj.getSpeciation());
        speciationCombo.setPreferredSize(new Dimension(250, 20));

        changeablesList.addChangeable(speciationCombo);

        return speciationCombo;
    }

    private EditableComboBox regions() throws EmfException {
        allRegions = session.dataCommonsService().getRegions();
        regionsCombo = new EditableComboBox(allRegions);
        regionsCombo.setSelectedItem(caseObj.getRegion());
        regionsCombo.setPreferredSize(new Dimension(250, 20));

        changeablesList.addChangeable(regionsCombo);

        return projectsCombo;
    }

    private String format(Date date) {
        return DATE_FORMATTER.format(date);
    }

    private JLabel createLeftAlignedLabel(String name) {
        JLabel label = new JLabel(name);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        return label;
    }

    public void save(Case caseObj) {// TODO
    }

}
