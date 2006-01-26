package gov.epa.emissions.framework.client.meta.summary;

import gov.epa.emissions.commons.gui.ComboBox;
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
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.IntendedUse;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputVerifier;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.JFormattedTextField.AbstractFormatter;

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

    private ComboBox intendedUseCombo;

    private ComboBox sectorsCombo;

    private DataCommonsService service;

    private ComboBox projectsCombo;

    private ComboBox temporalResolutionsCombo;

    private ComboBox regionsCombo;

    private ComboBox countriesCombo;

    public EditableSummaryTab(EmfDataset dataset, DataCommonsService service, MessagePanel messagePanel)
            throws EmfException {
        super.setName("summary");
        this.dataset = dataset;
        this.service = service;
        this.messagePanel = messagePanel;

        super.setLayout(new BorderLayout());
        SummaryTabComboBoxChangesListener comboxBoxListener = new SummaryTabComboBoxChangesListener();
        super.add(createOverviewSection(comboxBoxListener), BorderLayout.PAGE_START);
        super.add(createLowerSection(comboxBoxListener), BorderLayout.CENTER);

        listenForKeyEvents(new SummaryTabKeyListener());
    }

    private JPanel createLowerSection(SummaryTabComboBoxChangesListener comboxBoxListener) throws EmfException {
        JPanel lowerPanel = new JPanel(new FlowLayout());

        lowerPanel.add(createTimeSpaceSection(comboxBoxListener));
        lowerPanel.add(createStatusSection());

        return lowerPanel;
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
        IntendedUse[] intendedUses = service.getIntendedUses();
        intendedUseCombo = new ComboBox("Choose an intended use", intendedUses);
        IntendedUse intendedUse = dataset.getIntendedUse();
        if (intendedUse != null) {
            intendedUseCombo.setSelectedItem(intendedUse);
        }

    }

    private String format(Date date) {
        return DATE_FORMATTER.format(date);
    }

    private JPanel createTimeSpaceSection(SummaryTabComboBoxChangesListener comboxBoxListener) throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        // time period
        startDateTime = new FormattedTextField("startDateTime", dataset.getStartDateTime(), DATE_FORMATTER);
        endDateTime = new FormattedTextField("endDateTime", dataset.getStopDateTime(), DATE_FORMATTER);
        layoutGenerator.addLabelWidgetPair("Time Period Start:", startDateTime, panel);
        layoutGenerator.addLabelWidgetPair("Time Period End:", endDateTime, panel);

        // temporal resolution
        String[] temporalResolutionNames = (String[]) TemporalResolution.NAMES.toArray(new String[0]);
        temporalResolutionsCombo = new ComboBox("Choose a resoluton", temporalResolutionNames);
        temporalResolutionsCombo.setSelectedItem(dataset.getTemporalResolution());
        temporalResolutionsCombo.setName("temporalResolutions");
        temporalResolutionsCombo.setPreferredSize(new Dimension(175, 20));
        temporalResolutionsCombo.addItemListener(comboxBoxListener);
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
        layoutGenerator.addLabelWidgetPair("Sector:", sectorsCombo, panel);

        // region
        Region[] regions = service.getRegions();
        regionsCombo = new ComboBox("Choose a region",regions);
        regionsCombo.setSelectedItem(dataset.getRegion());
        regionsCombo.setName("regionsComboModel");
        regionsCombo.setPreferredSize(new Dimension(125, 20));
        regionsCombo.addItemListener(comboxBoxListener);
        layoutGenerator.addLabelWidgetPair("Region:", regionsCombo, panel);
        Region region = dataset.getRegion();
        if (region != null) {
           regionsCombo.setSelectedItem(region);
        }
        
        // country
        countriesCombo = new ComboBox("Choose a country", service.getCountries());
        countriesCombo.setSelectedItem(dataset.getCountry());
        countriesCombo.setName("countries");
        countriesCombo.addItemListener(comboxBoxListener);
        countriesCombo.setPreferredSize(new Dimension(175, 20));
        Country country = dataset.getCountry();
        if (country != null) {
            countriesCombo.setSelectedItem(country);
        }

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

        layoutGenerator.addLabelWidgetPair("Name:", name, panel);

        // description
        description = new TextArea("description", dataset.getDescription());
        layoutGenerator.addLabelWidgetPair("Description:", new ScrollableTextArea(description), panel);

        // project - TODO: look up all projects
        Project[] allProjects = service.getProjects();
        projectsCombo = new ComboBox("Select or Enter Project", allProjects);
        Project project = dataset.getProject();
        if (project != null) {
            projectsCombo.setSelectedItem(project);
        }
        projectsCombo.setName("projects");
        projectsCombo.setPreferredSize(new Dimension(250, 20));
        projectsCombo.addItemListener(comboxBoxListener);
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
        dataset.setProject((Project) projectsCombo.getSelectedItem());
        dataset.setStartDateTime(toDate(startDateTime.getText()));
        dataset.setStopDateTime(toDate(endDateTime.getText()));
        dataset.setTemporalResolution((String) temporalResolutionsCombo.getSelectedItem());
        dataset.setRegion((Region) regionsCombo.getSelectedItem());
        dataset.setCountry((Country) countriesCombo.getSelectedItem());
        dataset.setSectors(new Sector[] { (Sector) sectorsCombo.getSelectedItem() });
        dataset.setIntendedUse((IntendedUse) intendedUseCombo.getSelectedItem());
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

    public class FormattedTextField extends JFormattedTextField {

        public FormattedTextField(String name, Object value, Format format) {
            super(format);
            super.setName(name);
            super.setValue(value);
            super.setColumns(10);

            super.setInputVerifier(new FormattedTextFieldVerifier());
        }

    }

    public class FormattedTextFieldVerifier extends InputVerifier {
        public boolean verify(JComponent input) {
            JFormattedTextField ftf = (JFormattedTextField) input;
            AbstractFormatter formatter = ftf.getFormatter();
            String text = ftf.getText();
            if (text.trim().length() == 0) {// need not validate empty field
                return true;
            }
            try {
                formatter.stringToValue(text);
                messagePanel.clear();
            } catch (ParseException pe) {
                messagePanel.setError("Invalid date - " + text + ".  Please use the format - "
                        + DATE_FORMATTER.toPattern());
                return false;
            }

            return true;
        }

        public boolean shouldYieldFocus(JComponent input) {
            return verify(input);
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
