package gov.epa.emissions.framework.client.meta.summary;

import gov.epa.emissions.commons.gui.ChangeablesList;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.EditableComboBox;
import gov.epa.emissions.commons.gui.FormattedTextField;
import gov.epa.emissions.commons.gui.ScrollableTextArea;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.io.Country;
import gov.epa.emissions.commons.io.Project;
import gov.epa.emissions.commons.io.Region;
import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.commons.io.importer.TemporalResolution;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.ChangeObserver;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.IntendedUse;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

//FIXME: super long class..break it up
public class EditableSummaryTab extends JPanel implements EditableSummaryTabView {

    private EmfDataset dataset;

    public final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("MM/dd/yyyy HH:mm");

    private TextField name;

    private FormattedTextField startDateTime;

    private FormattedTextField endDateTime;

    private TextArea description;

    private MessagePanel messagePanel;

    private ChangeObserver changeObserver;

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
    
    private ChangeablesList changeablesList;
    
    public EditableSummaryTab(EmfDataset dataset, DataCommonsService service, MessagePanel messagePanel,
            ChangeablesList changeablesList) throws EmfException {
        super.setName("summary");
        this.dataset = dataset;
        this.service = service;
        this.messagePanel = messagePanel;
        this.changeablesList = changeablesList;

        super.setLayout(new BorderLayout());
        SummaryTabComboBoxChangesListener comboxBoxListener = new SummaryTabComboBoxChangesListener();
        super.add(createOverviewSection(comboxBoxListener), BorderLayout.PAGE_START);
        super.add(createLowerSection(comboxBoxListener), BorderLayout.CENTER);

        listenForKeyEvents(new SummaryTabKeyListener());
    }

    private JPanel createLowerSection(SummaryTabComboBoxChangesListener comboxBoxListener) throws EmfException {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        container.add(createTimeSpaceSection(comboxBoxListener));
        container.add(createStatusSection());
        
        panel.add(container, BorderLayout.LINE_START);

        return panel;
    }

    private JPanel createStatusSection() throws EmfException {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        panel.add(createStatusDatesAndIntendedUsePanel(), BorderLayout.PAGE_START);
        panel.add(createSubscriptionPanel(), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSubscriptionPanel() {
        JPanel panel = new JPanel();
        JCheckBox subscribed = new JCheckBox("Subscribed?", true);
        subscribed.setToolTipText("TBD");
        // panel.add(subscribed);

        // panel.add(new JLabel("Subscribed Users"));
        DefaultComboBoxModel subscribedUsersModel = new DefaultComboBoxModel(new String[0]);
        JComboBox subscribedUsers = new JComboBox(subscribedUsersModel);
        subscribedUsers.setName("subscribedUser");
        subscribedUsers.setPreferredSize(new Dimension(100, 20));
        // panel.add(subscribedUsers);

        return panel;
    }

    private JPanel createStatusDatesAndIntendedUsePanel() throws EmfException {
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
        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 5, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad
        return panel;
    }

    private void setupIntendedUseCombo() throws EmfException {
        allIntendedUses = service.getIntendedUses();
        intendedUseCombo = new EditableComboBox(allIntendedUses);
        IntendedUse intendedUse = dataset.getIntendedUse();
        intendedUseCombo.setSelectedItem(intendedUse);
        changeablesList.add(intendedUseCombo);
        intendedUseCombo.addListeners();
    }

    private String format(Date date) {
        return DATE_FORMATTER.format(date);
    }

    private JPanel createTimeSpaceSection(SummaryTabComboBoxChangesListener comboxBoxListener) throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        // time period
        startDateTime = new FormattedTextField("startDateTime", dataset.getStartDateTime(), DATE_FORMATTER, messagePanel);
        endDateTime = new FormattedTextField("endDateTime", dataset.getStopDateTime(), DATE_FORMATTER, messagePanel);
        changeablesList.add(startDateTime);
        changeablesList.add(endDateTime);
        startDateTime.addTextListener();
        endDateTime.addTextListener();
        layoutGenerator.addLabelWidgetPair("Time Period Start:", startDateTime, panel);
        layoutGenerator.addLabelWidgetPair("Time Period End:", endDateTime, panel);

        // temporal resolution
        String[] temporalResolutionNames = (String[]) TemporalResolution.NAMES.toArray(new String[0]);
        temporalResolutionsCombo = new ComboBox("Choose a resolution", temporalResolutionNames);
        temporalResolutionsCombo.setSelectedItem(dataset.getTemporalResolution());
        temporalResolutionsCombo.setName("temporalResolutions");
        temporalResolutionsCombo.setPreferredSize(new Dimension(170, 20));
        temporalResolutionsCombo.addItemListener(comboxBoxListener);
        String temporalResolution = dataset.getTemporalResolution();
        temporalResolutionsCombo.setSelectedItem(temporalResolution);
        changeablesList.add(temporalResolutionsCombo);
        temporalResolutionsCombo.addItemChangeListener();
        layoutGenerator.addLabelWidgetPair("Temporal Resolution:", temporalResolutionsCombo, panel);

        sectorsCombo = new ComboBox("Choose a sector", service.getSectors());
        Sector[] datasetSectors = dataset.getSectors();
        // TODO: Change this code, when multiple sector selection is allowed
        if (datasetSectors != null && datasetSectors.length > 0) {
            sectorsCombo.setSelectedItem(datasetSectors[0]);
        }
        sectorsCombo.setName("sectors");
        sectorsCombo.setPreferredSize(new Dimension(175, 20));
        sectorsCombo.addItemListener(comboxBoxListener);
        changeablesList.add(sectorsCombo);
        sectorsCombo.addItemChangeListener();
        layoutGenerator.addLabelWidgetPair("Sector:", sectorsCombo, panel);

        allRegions = service.getRegions();
        regionsCombo = new EditableComboBox(allRegions);
        regionsCombo.setSelectedItem(dataset.getRegion());
        regionsCombo.setName("regionsComboModel");
        regionsCombo.setPreferredSize(new Dimension(125, 20));
        regionsCombo.addItemListener(comboxBoxListener);
        changeablesList.add(regionsCombo);
        regionsCombo.addListeners();
        layoutGenerator.addLabelWidgetPair("Region:", regionsCombo, panel);
        Region region = dataset.getRegion();
        regionsCombo.setSelectedItem(region);

        // country
        countriesCombo = new ComboBox("Choose a country", service.getCountries());
        countriesCombo.setSelectedItem(dataset.getCountry());
        countriesCombo.setName("countries");
        countriesCombo.addItemListener(comboxBoxListener);
        countriesCombo.setPreferredSize(new Dimension(175, 20));
        Country country = dataset.getCountry();
        countriesCombo.setSelectedItem(country);
        changeablesList.add(countriesCombo);
        countriesCombo.addItemChangeListener();

        layoutGenerator.addLabelWidgetPair("Country:", countriesCombo, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 6, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JPanel createOverviewSection(SummaryTabComboBoxChangesListener comboxBoxListener) throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        // name
        name = new TextField("name", 25);
        name.setText(dataset.getName());
        name.setMaximumSize(new Dimension(300, 15));
        changeablesList.add(name);
        name.addTextListener();

        layoutGenerator.addLabelWidgetPair("Name:", name, panel);

        // description
        description = new TextArea("description", dataset.getDescription());
        changeablesList.add(description);
        description.addTextListener();
        layoutGenerator.addLabelWidgetPair("Description:", new ScrollableTextArea(description), panel);

        allProjects = service.getProjects();
        projectsCombo = new EditableComboBox(allProjects);
        projectsCombo.setSelectedItem(dataset.getProject());
        projectsCombo.setName("projects");
        projectsCombo.setPreferredSize(new Dimension(250, 20));
        projectsCombo.addItemListener(comboxBoxListener);
        changeablesList.add(projectsCombo);
        projectsCombo.addListeners();
        layoutGenerator.addLabelWidgetPair("Project:", projectsCombo, panel);

        // creator
        JLabel creator = createLeftAlignedLabel(dataset.getCreator());
        creator.setName("creator");
        layoutGenerator.addLabelWidgetPair("Creator:", creator, panel);

        // dataset type
        JLabel datasetType = createLeftAlignedLabel(dataset.getDatasetTypeName());
        datasetType.setName("datasetType");
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

    public void updateDataset(EmfDataset dataset) {
        dataset.setName(name.getText());
        dataset.setDescription(description.getText());
        updateProject();
        dataset.setStartDateTime(toDate(startDateTime.getText()));
        dataset.setStopDateTime(toDate(endDateTime.getText()));
        dataset.setTemporalResolution((String) temporalResolutionsCombo.getSelectedItem());
        updateRegion();
        dataset.setCountry((Country) countriesCombo.getSelectedItem());
        dataset.setSectors(new Sector[] { (Sector) sectorsCombo.getSelectedItem() });
        updateIntendedUse();
    }

    private void updateProject() {
        Object selected = projectsCombo.getSelectedItem();
        if (selected instanceof String) {
            String projectName = (String) selected;
            if (projectName.length() > 0) {
                Project project = project(projectName);//checking for duplicates
                dataset.setProject(project);
            }
        } else if (selected instanceof Project) {
            dataset.setProject((Project) selected);
        }
    }

    private Project project(String projectName) {
        for (int i = 0; i < allProjects.length; i++) {
            if(projectName.equals(allProjects[i].getName())){
                return allProjects[i];
            }
        }
        return new Project(projectName);
    }
    
    private void updateRegion() {
        Object selected = regionsCombo.getSelectedItem();
        if (selected instanceof String) {
            String regionName = (String) selected;
            if (regionName.length() > 0) {
                Region region = region(regionName);//checking for duplicates
                dataset.setRegion(region);
            }
        } else if (selected instanceof Region) {
            dataset.setRegion((Region) selected);
        }
    }

    private Region region(String region) {
        for (int i = 0; i < allRegions.length; i++) {
            if(region.equals(allRegions[i].getName())){
                return allRegions[i];
            }
        }
        return new Region(region);
    }
    
    private void updateIntendedUse() {
        Object selected = intendedUseCombo.getSelectedItem();
        if (selected instanceof String) {
            String intendedUseName = (String) selected;
            if (intendedUseName.length() > 0) {
                IntendedUse intendedUse = intendedUse(intendedUseName);//checking for duplicates
                dataset.setIntendedUse(intendedUse);
            }
        } else if (selected instanceof IntendedUse) {
            dataset.setIntendedUse((IntendedUse) selected);
        }
    }

    private IntendedUse intendedUse(String intendedUseName) {
        for (int i = 0; i < allIntendedUses.length; i++) {
            if(intendedUseName.equals(allIntendedUses[i].getName())){
                return allIntendedUses[i];
            }
        }
        return new IntendedUse(intendedUseName);
    }
    
    private Date toDate(String text) {
        if (text == null || text.length() == 0)
            return null;

        try {
            return DATE_FORMATTER.parse(text);
        } catch (ParseException e) {
            throw new RuntimeException("could not parse Date - " + text + ". Expected format - "
                    + DATE_FORMATTER.toPattern());
        }
    }

    private void listenForKeyEvents(KeyListener keyListener) {
        name.addKeyListener(keyListener);
        description.addKeyListener(keyListener);
        startDateTime.addKeyListener(keyListener);
        endDateTime.addKeyListener(keyListener);
    }

    public void observeChanges(ChangeObserver observer) {
        this.changeObserver = observer;
    }

    public class SummaryTabKeyListener extends KeyAdapter {
        public void keyTyped(KeyEvent e) {
            if (changeObserver != null)
                changeObserver.onChange();
        }
    }

    public class SummaryTabComboBoxChangesListener implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            if (changeObserver != null)
                changeObserver.onChange();
        }
    }

}
