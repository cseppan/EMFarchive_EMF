package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.gui.ScrollableTextArea;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.commons.io.importer.TemporalResolution;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.ChangeObserver;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.services.Country;
import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.services.EmfDataset;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
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
public class SummaryTab extends JPanel implements SummaryTabView {

    private EmfDataset dataset;

    public final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("MM/dd/yyyy");

    private TextField name;

    private FormattedTextField startDateTime;

    private FormattedTextField endDateTime;

    private DefaultComboBoxModel temporalResolutions;

    private TextArea description;

    private DefaultComboBoxModel sectors;

    private DefaultComboBoxModel countries;

    private MessagePanel messagePanel;

    private DefaultComboBoxModel projects;

    private DefaultComboBoxModel regions;

    private ChangeObserver changeObserver;

    public SummaryTab(EmfDataset dataset, DataCommonsService service, MessagePanel messagePanel) throws EmfException {
        super.setName("summary");
        this.dataset = dataset;
        this.messagePanel = messagePanel;

        super.setLayout(new BorderLayout());
        SummaryTabComboBoxChangesListener comboxBoxListener = new SummaryTabComboBoxChangesListener();
        super.add(createOverviewSection(comboxBoxListener), BorderLayout.PAGE_START);
        super.add(createLowerSection(service, comboxBoxListener), BorderLayout.CENTER);

        listenForKeyEvents(new SummaryTabKeyListener());
    }

    private JPanel createLowerSection(DataCommonsService service, SummaryTabComboBoxChangesListener comboxBoxListener)
            throws EmfException {
        JPanel lowerPanel = new JPanel(new FlowLayout());

        lowerPanel.add(createTimeSpaceSection(service, comboxBoxListener));
        lowerPanel.add(createStatusSection());

        return lowerPanel;
    }

    private JPanel createStatusSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        panel.add(createStatusDatesPanel(), BorderLayout.PAGE_START);
        panel.add(createSubscriptionPanel(), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSubscriptionPanel() {
        JPanel panel = new JPanel();

        JCheckBox subscribed = new JCheckBox("Subscribed?", true);
        subscribed.setToolTipText("TBD");
        panel.add(subscribed);

        panel.add(new JLabel("Subscribed Users"));
        DefaultComboBoxModel subscribedUsersModel = new DefaultComboBoxModel(new String[0]);
        JComboBox subscribedUsers = new JComboBox(subscribedUsersModel);
        subscribedUsers.setName("subscribedUser");
        subscribedUsers.setPreferredSize(new Dimension(100, 20));
        panel.add(subscribedUsers);

        return panel;
    }

    private JPanel createStatusDatesPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Status", new Label("status", dataset.getStatus()), panel);
        layoutGenerator.addLabelWidgetPair("Last Modified Date", new Label("lastModifiedDate", format(dataset
                .getModifiedDateTime())), panel);
        layoutGenerator.addLabelWidgetPair("Last Accessed Date", new Label("lastAccessedDate", format(dataset
                .getAccessedDateTime())), panel);
        layoutGenerator.addLabelWidgetPair("Creation Date", new Label("creationDate", format(dataset
                .getCreatedDateTime())), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 4, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private String format(Date date) {
        return DATE_FORMATTER.format(date);
    }

    private JPanel createTimeSpaceSection(DataCommonsService service,
            SummaryTabComboBoxChangesListener comboxBoxListener) throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        // time period
        JPanel startDatePanel = new JPanel();
        startDatePanel.add(new JLabel("Start"));
        startDateTime = new FormattedTextField("startDateTime", dataset.getStartDateTime(), DATE_FORMATTER);
        startDateTime.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        startDatePanel.add(startDateTime);

        JPanel endDatePanel = new JPanel();
        endDatePanel.add(new JLabel("End  "));
        endDateTime = new FormattedTextField("endDateTime", dataset.getStopDateTime(), DATE_FORMATTER);
        endDatePanel.add(endDateTime);

        JPanel datesPanel = new JPanel();
        datesPanel.setLayout(new BoxLayout(datesPanel, BoxLayout.Y_AXIS));
        datesPanel.add(startDatePanel);
        datesPanel.add(endDatePanel);

        layoutGenerator.addLabelWidgetPair("Time Period", datesPanel, panel);

        // temporal resolution
        String[] temporalResolutionNames = (String[]) TemporalResolution.NAMES.toArray(new String[0]);
        temporalResolutions = new DefaultComboBoxModel(temporalResolutionNames);
        JComboBox temporalResolutionsCombo = new JComboBox(temporalResolutions);
        temporalResolutionsCombo.setSelectedItem(dataset.getTemporalResolution());
        temporalResolutionsCombo.setName("temporalResolutions");
        temporalResolutionsCombo.setPreferredSize(new Dimension(100, 20));
        temporalResolutionsCombo.addItemListener(comboxBoxListener);
        layoutGenerator.addLabelWidgetPair("Temporal Resolution", temporalResolutionsCombo, panel);

        // sectors
        sectors = new DefaultComboBoxModel(sectorNames(dataset.getSector(), service.getSectors()));
        JComboBox sectorsCombo = new JComboBox(sectors);
        sectorsCombo.setSelectedItem(dataset.getSector());
        sectorsCombo.setName("sectors");
        sectorsCombo.setEditable(true);
        sectorsCombo.setPreferredSize(new Dimension(175, 20));
        sectorsCombo.addItemListener(comboxBoxListener);
        layoutGenerator.addLabelWidgetPair("Sector", sectorsCombo, panel);

        // region
        String[] regionNames = new String[] { dataset.getRegion() };
        regions = new DefaultComboBoxModel(regionNames);
        JComboBox regionsCombo = new JComboBox(regions);
        regionsCombo.setSelectedItem(dataset.getRegion());
        regionsCombo.setName("regions");
        regionsCombo.setEditable(true);
        regionsCombo.setPreferredSize(new Dimension(125, 20));
        regionsCombo.addItemListener(comboxBoxListener);
        layoutGenerator.addLabelWidgetPair("Region", regionsCombo, panel);

        // country
        countries = new DefaultComboBoxModel(countryNames(dataset.getCountry(), service.getCountries()));
        JComboBox countriesCombo = new JComboBox(countries);
        countriesCombo.setSelectedItem(dataset.getCountry());
        countriesCombo.setName("countries");
        countriesCombo.setEditable(false);
        countriesCombo.addItemListener(comboxBoxListener);
        countriesCombo.setPreferredSize(new Dimension(175, 20));

        layoutGenerator.addLabelWidgetPair("Country", countriesCombo, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 5, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private String[] countryNames(String selectedCountry, Country[] countries) {
        List list = new ArrayList();
        for (int i = 0; i < countries.length; i++) {
            list.add(countries[i].getName());
        }

        if (!list.contains(selectedCountry))
            list.add(selectedCountry);

        return (String[]) list.toArray(new String[0]);
    }

    private String[] sectorNames(String selectedSector, Sector[] sectors) {
        List list = new ArrayList();
        for (int i = 0; i < sectors.length; i++) {
            list.add(sectors[i].getName());
        }

        if (!list.contains(selectedSector))
            list.add(selectedSector);

        return (String[]) list.toArray(new String[0]);
    }

    private JPanel createOverviewSection(SummaryTabComboBoxChangesListener comboxBoxListener) {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        // name
        name = new TextField("name", 25);
        name.setText(dataset.getName());
        name.setMaximumSize(new Dimension(300, 15));

        layoutGenerator.addLabelWidgetPair("Name", name, panel);

        // description
        description = new TextArea("description", dataset.getDescription());
        layoutGenerator.addLabelWidgetPair("Description", new ScrollableTextArea(description), panel);

        // project - TODO: look up all projects
        String[] projectNames = new String[] { dataset.getProject() };
        projects = new DefaultComboBoxModel(projectNames);
        JComboBox projectsCombo = new JComboBox(projects);
        projectsCombo.setSelectedItem(dataset.getProject());
        projectsCombo.setName("projects");
        projectsCombo.setEditable(true);
        projectsCombo.setPreferredSize(new Dimension(250, 20));
        projectsCombo.addItemListener(comboxBoxListener);
        layoutGenerator.addLabelWidgetPair("Project", projectsCombo, panel);

        // creator
        JLabel creator = createLeftAlignedLabel(dataset.getCreator());
        creator.setName("creator");
        layoutGenerator.addLabelWidgetPair("Creator", creator, panel);

        // dataset type
        JLabel datasetType = createLeftAlignedLabel(dataset.getDatasetTypeName());
        datasetType.setName("datasetType");
        layoutGenerator.addLabelWidgetPair("Dataset Type", datasetType, panel);

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
        dataset.setProject((String) projects.getSelectedItem());
        dataset.setStartDateTime(toDate(startDateTime.getText()));
        dataset.setStopDateTime(toDate(endDateTime.getText()));
        dataset.setTemporalResolution((String) temporalResolutions.getSelectedItem());
        dataset.setRegion((String) regions.getSelectedItem());
        dataset.setCountry((String) countries.getSelectedItem());
        dataset.setSector((String) sectors.getSelectedItem());
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
            super.setColumns(8);

            super.setInputVerifier(new FormattedTextFieldVerifier());
        }

    }

    public class FormattedTextFieldVerifier extends InputVerifier {
        public boolean verify(JComponent input) {
            JFormattedTextField ftf = (JFormattedTextField) input;
            AbstractFormatter formatter = ftf.getFormatter();
            String text = ftf.getText();
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
