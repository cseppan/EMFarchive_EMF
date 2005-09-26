package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.io.importer.TemporalResolution;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.services.EmfDataset;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.InputVerifier;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.JFormattedTextField.AbstractFormatter;

public class SummaryTab extends JPanel implements SummaryTabView {

    private EmfDataset dataset;

    private final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("MM/dd/yyyy    hh:mm:ss");

    private TextField name;

    private TextField project;

    private JFormattedTextField startDateTime;

    private FormattedTextField endDateTime;

    private DefaultComboBoxModel temporalResolutions;

    private TextField region;

    private TextArea description;

    private DefaultListModel sectors;

    private DefaultListModel countries;

    private MessagePanel messagePanel;

    public SummaryTab(EmfDataset dataset, MessagePanel messagePanel) {
        this.dataset = dataset;
        this.messagePanel = messagePanel;

        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        super.add(createOverviewSection());
        super.add(createLowerSection());
    }

    private JPanel createLowerSection() {
        JPanel lowerPanel = new JPanel(new BorderLayout());

        lowerPanel.add(createTimeSpaceSection(), BorderLayout.LINE_START);
        lowerPanel.add(createStatusSection(), BorderLayout.CENTER);

        return lowerPanel;
    }

    private JPanel createStatusSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.GRAY));

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

        layoutGenerator.addLabelWidgetPair("Status", new JLabel(dataset.getStatus()), panel);
        layoutGenerator.addLabelWidgetPair("Last Modified Date", new JLabel(" "), panel);
        layoutGenerator.addLabelWidgetPair("Last Accessed Date", new JLabel(" "), panel);
        layoutGenerator.addLabelWidgetPair("Date of Creation", new JLabel(" "), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 4, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JPanel createTimeSpaceSection() {
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
        // TODO: set the selected
        JComboBox temporalResolutionsCombo = new JComboBox(temporalResolutions);
        temporalResolutionsCombo.setSelectedItem(dataset.getTemporalResolution());
        temporalResolutionsCombo.setName("temporalResolutions");
        temporalResolutionsCombo.setPreferredSize(new Dimension(100, 20));
        layoutGenerator.addLabelWidgetPair("Temporal Resolution", temporalResolutionsCombo, panel);

        // sectors: TODO: lookup sectors
        sectors = new DefaultListModel();
        dumptArrayIntoListModel(new String[] { "TBD", "" }, sectors);
        layoutGenerator.addLabelWidgetPair("Sectors", createList("sectors", sectors), panel);

        // region
        region = new TextField("region", dataset.getRegion(), 15);
        layoutGenerator.addLabelWidgetPair("Region", region, panel);

        // country - TODO: lookup countries
        countries = new DefaultListModel();
        dumptArrayIntoListModel(new String[] { dataset.getCountry(), "" }, countries);
        layoutGenerator.addLabelWidgetPair("Country", createList("countries", countries), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 5, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private void dumptArrayIntoListModel(String[] elements, DefaultListModel listModel) {
        for (int i = 0; i < elements.length; i++) {
            listModel.addElement(elements[i]);
        }
    }

    private JScrollPane createList(String name, ListModel model) {
        JList list = new JList(model);
        list.setName(name);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(0);

        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setPreferredSize(new Dimension(100, 20));

        return scrollPane;
    }

    private JPanel createOverviewSection() {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        // name
        name = new TextField("name", 15);
        name.setText(dataset.getName());
        name.setMaximumSize(new Dimension(300, 15));

        layoutGenerator.addLabelWidgetPair("Name", name, panel);

        // description
        description = new TextArea("description", dataset.getDescription());
        JScrollPane scrollPane = new JScrollPane(description, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        layoutGenerator.addLabelWidgetPair("Description", scrollPane, panel);

        // project
        project = new TextField("project", dataset.getProject(), 15);
        project.setMaximumSize(new Dimension(300, 15));

        layoutGenerator.addLabelWidgetPair("Project", project, panel);

        // creator
        JLabel creator = createLeftAlignedLabel(dataset.getCreator());
        layoutGenerator.addLabelWidgetPair("Creator", creator, panel);

        // dataset type
        JLabel datasetType = createLeftAlignedLabel(dataset.getDatasetType());
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
        dataset.setProject(project.getText());
        dataset.setStartDateTime(toDate(startDateTime.getText()));
        dataset.setStopDateTime(toDate(endDateTime.getText()));
        dataset.setTemporalResolution((String) temporalResolutions.getSelectedItem());
        dataset.setRegion(region.getText());
        // FIXME: selected country needs to be captured
        // dataset.setCountry(countries.getSiz);
    }

    private Date toDate(String text) {
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
            super.setColumns(12);

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
}
