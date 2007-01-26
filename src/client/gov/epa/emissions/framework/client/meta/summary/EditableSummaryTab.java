package gov.epa.emissions.framework.client.meta.summary;

import gov.epa.emissions.commons.data.Country;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.EditableComboBox;
import gov.epa.emissions.commons.gui.FormattedDateField;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.io.importer.TemporalResolution;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.data.IntendedUses;
import gov.epa.emissions.framework.client.data.Projects;
import gov.epa.emissions.framework.client.data.Regions;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.EmfDateFormat;
import gov.epa.emissions.framework.services.data.IntendedUse;
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

public class EditableSummaryTab extends JPanel implements EditableSummaryTabView {

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(EmfDateFormat.PATTERN_yyyyMMddHHmm);
    
    private EmfDataset dataset;

    private TextField name;

    private FormattedDateField startDateTime;

    private FormattedDateField endDateTime;

    private TextArea description;

    private MessagePanel messagePanel;

    private EditableComboBox intendedUseCombo;

    private ComboBox sectorsCombo;

    private DataCommonsService service;

    private EditableComboBox projectsCombo;

    private ComboBox temporalResolutionsCombo;

    private EditableComboBox regionsCombo;

    private ComboBox countriesCombo;

    private Project[] allProjects;

    private Region[] allRegions;

    private IntendedUse[] allIntendedUses;

    private ManageChangeables changeablesList;

    private DefaultVersionPanel defaultVersionPanel;

    public EditableSummaryTab(EmfDataset dataset, Version[] versions, DataCommonsService service,
            MessagePanel messagePanel, ManageChangeables changeablesList) throws EmfException {
        super.setName("summary");
        this.dataset = dataset;
        this.service = service;
        this.messagePanel = messagePanel;
        this.changeablesList = changeablesList;

        super.setLayout(new BorderLayout());
        super.add(createOverviewSection(), BorderLayout.PAGE_START);
        super.add(createLowerSection(versions), BorderLayout.CENTER);
    }

    private JPanel createLowerSection(Version[] versions) throws EmfException {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        container.add(createTimeSpaceSection());
        container.add(createLowerRightSection(versions));

        panel.add(container, BorderLayout.LINE_START);

        return panel;
    }

    private JPanel createLowerRightSection(Version[] versions) throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Status:", new Label("status", dataset.getStatus()), panel);
        layoutGenerator.addLabelWidgetPair("Last Modified Date:", new Label("lastModifiedDate", format(dataset
                .getModifiedDateTime())), panel);
        layoutGenerator.addLabelWidgetPair("Last Accessed Date:", new Label("lastAccessedDate", format(dataset
                .getAccessedDateTime())), panel);
        layoutGenerator.addLabelWidgetPair("Creation Date:", new Label("creationDate", format(dataset
                .getCreatedDateTime())), panel);

        setupIntendedUseCombo();
        layoutGenerator.addLabelWidgetPair("Intended Use: ", intendedUseCombo, panel);
        defaultVersionPanel = new DefaultVersionPanel(dataset, versions, changeablesList, messagePanel);
        layoutGenerator.addLabelWidgetPair("Default Version:", defaultVersionPanel, panel);
        layoutGenerator.makeCompactGrid(panel, 6, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad
        return panel;
    }

    private void setupIntendedUseCombo() throws EmfException {
        allIntendedUses = service.getIntendedUses();
        intendedUseCombo = new EditableComboBox(allIntendedUses);
        IntendedUse intendedUse = dataset.getIntendedUse();
        intendedUseCombo.setSelectedItem(intendedUse);
        changeablesList.addChangeable(intendedUseCombo);
    }

    private String format(Date date) {
        return EmfDateFormat.format_YYYY_MM_DD_HH_MM(date);
    }

    private JPanel createTimeSpaceSection() throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        // time period
        startDateTime = new FormattedDateField("startDateTime", dataset.getStartDateTime(), DATE_FORMATTER,
                messagePanel);
        endDateTime = new FormattedDateField("endDateTime", dataset.getStopDateTime(), DATE_FORMATTER, messagePanel);
        changeablesList.addChangeable(startDateTime);
        changeablesList.addChangeable(endDateTime);
        layoutGenerator.addLabelWidgetPair("Time Period Start:", startDateTime, panel);
        layoutGenerator.addLabelWidgetPair("Time Period End:", endDateTime, panel);

        // temporal resolution
        temporalResolutionsCombo = temporalResolutionCombo();
        layoutGenerator.addLabelWidgetPair("Temporal Resolution:", temporalResolutionsCombo, panel);

        sectorsCombo = new ComboBox("Choose a sector", service.getSectors());
        Sector[] datasetSectors = dataset.getSectors();
        // TODO: Change this code, when multiple sector selection is allowed
        if (datasetSectors != null && datasetSectors.length > 0) {
            sectorsCombo.setSelectedItem(datasetSectors[0]);
        }
        sectorsCombo.setName("sectors");
        sectorsCombo.setPreferredSize(new Dimension(175, 20));
        changeablesList.addChangeable(sectorsCombo);
        layoutGenerator.addLabelWidgetPair("Sector:", sectorsCombo, panel);

        allRegions = service.getRegions();
        regionsCombo = new EditableComboBox(allRegions);
        regionsCombo.setSelectedItem(dataset.getRegion());
        regionsCombo.setName("regionsComboModel");
        regionsCombo.setPreferredSize(new Dimension(125, 20));
        changeablesList.addChangeable(regionsCombo);
        layoutGenerator.addLabelWidgetPair("Region:", regionsCombo, panel);
        Region region = dataset.getRegion();
        regionsCombo.setSelectedItem(region);

        // country
        countriesCombo = new ComboBox("Choose a country", service.getCountries());
        countriesCombo.setSelectedItem(dataset.getCountry());
        countriesCombo.setName("countries");
        countriesCombo.setPreferredSize(new Dimension(175, 20));
        Country country = dataset.getCountry();
        countriesCombo.setSelectedItem(country);
        changeablesList.addChangeable(countriesCombo);

        layoutGenerator.addLabelWidgetPair("Country:", countriesCombo, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 6, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private ComboBox temporalResolutionCombo() {
        ComboBox combo = new ComboBox("Choose a resolution", TemporalResolution.NAMES.toArray());
        combo.setName("temporalResolutions");
        combo.setPreferredSize(new Dimension(170, 20));

        String temporalResolution = dataset.getTemporalResolution();
        combo.setSelectedItem(temporalResolution);

        changeablesList.addChangeable(combo);

        return combo;
    }

    private JPanel createOverviewSection() throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        // name
        name = new TextField("name", 25);
        name.setText(dataset.getName());
        name.setMaximumSize(new Dimension(300, 15));
        changeablesList.addChangeable(name);

        layoutGenerator.addLabelWidgetPair("Name:", name, panel);

        // description
        description = new TextArea("description", dataset.getDescription());
        changeablesList.addChangeable(description);
        layoutGenerator.addLabelWidgetPair("Description:", new ScrollableComponent(description), panel);

        allProjects = service.getProjects();
        projectsCombo = new EditableComboBox(allProjects);
        projectsCombo.setSelectedItem(dataset.getProject());
        projectsCombo.setPreferredSize(new Dimension(250, 20));
        changeablesList.addChangeable(projectsCombo);
        layoutGenerator.addLabelWidgetPair("Project:", projectsCombo, panel);

        // creator
        JLabel creator = createLeftAlignedLabel(dataset.getCreator());
        layoutGenerator.addLabelWidgetPair("Creator:", creator, panel);

        // dataset type
        JLabel datasetType = createLeftAlignedLabel(dataset.getDatasetTypeName());
        layoutGenerator.addLabelWidgetPair("Dataset Type:", datasetType, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 5, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JLabel createLeftAlignedLabel(String name) {
        JLabel datasetTypeLabel = new JLabel(name);
        datasetTypeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        return datasetTypeLabel;
    }

    public void save(EmfDataset dataset) {
        messagePanel.clear();
        
        dataset.setName(name.getText());
        dataset.setDescription(description.getText());
        updateProject();
        dataset.setStartDateTime(startDateTime.value());
        dataset.setStopDateTime(endDateTime.value());
        dataset.setTemporalResolution((String) temporalResolutionsCombo.getSelectedItem());
        updateRegion();
        dataset.setCountry((Country) countriesCombo.getSelectedItem());
        dataset.setSectors(new Sector[] { (Sector) sectorsCombo.getSelectedItem() });
        updateIntendedUse();
        dataset.setDefaultVersion(defaultVersionPanel.getSelectedDefaultVersionNum());
    }

    private void updateProject() {
        Object selected = projectsCombo.getSelectedItem();
        if (selected instanceof String) {
            String projectName = (String) selected;
            if (projectName.length() > 0) {
                Project project = project(projectName);// checking for duplicates
                dataset.setProject(project);
            }
        } else if (selected instanceof Project) {
            dataset.setProject((Project) selected);
        }
    }

    private Project project(String name) {
        return new Projects(allProjects).get(name);
    }

    private void updateRegion() {
        Object selected = regionsCombo.getSelectedItem();
        if (selected instanceof String) {
            String regionName = (String) selected;
            if (regionName.length() > 0) {
                Region region = region(regionName);// checking for duplicates
                dataset.setRegion(region);
            }
        } else if (selected instanceof Region) {
            dataset.setRegion((Region) selected);
        }
    }

    private Region region(String name) {
        return new Regions(allRegions).get(name);
    }

    private void updateIntendedUse() {
        Object selected = intendedUseCombo.getSelectedItem();
        if (selected instanceof String) {
            String intendedUseName = (String) selected;
            if (intendedUseName.length() > 0) {
                IntendedUse intendedUse = intendedUse(intendedUseName);// checking for duplicates
                dataset.setIntendedUse(intendedUse);
            }
        } else if (selected instanceof IntendedUse) {
            dataset.setIntendedUse((IntendedUse) selected);
        }
    }

    private IntendedUse intendedUse(String name) {
        return new IntendedUses(allIntendedUses).get(name);
    }

}
