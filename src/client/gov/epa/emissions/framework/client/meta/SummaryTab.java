package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.services.EmfDataset;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

public class SummaryTab extends JPanel implements SummaryTabView {

    private EmfDataset dataset;

    private final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("MM/dd/yyyy    hh:mm:ss");

    private TextField name;

    private TextField project;

    private TextField startDateTime;

    private TextField endDateTime;

    private DefaultComboBoxModel temporalResolutions;

    private TextField region;

    private TextArea description;

    private DefaultListModel sectors;

    private DefaultListModel countries;

    public SummaryTab(EmfDataset dataset) {
        this.dataset = dataset;

        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        setLayout();
    }

    private void setLayout() {
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
        JPanel panel = new JPanel();

        // labels
        JPanel labelsPanel = new JPanel();
        labelsPanel.setLayout(new BoxLayout(labelsPanel, BoxLayout.Y_AXIS));

        labelsPanel.add(Box.createRigidArea(new Dimension(1, 15)));
        labelsPanel.add(new JLabel("Status:"));
        labelsPanel.add(Box.createRigidArea(new Dimension(1, 15)));
        labelsPanel.add(new JLabel("Last Modified Date:"));
        labelsPanel.add(Box.createRigidArea(new Dimension(1, 15)));
        labelsPanel.add(new JLabel("Last Accessed Date:"));

        panel.add(labelsPanel);

        // values
        JPanel valuesPanel = new JPanel();
        valuesPanel.setLayout(new BoxLayout(valuesPanel, BoxLayout.Y_AXIS));

        valuesPanel.add(Box.createRigidArea(new Dimension(1, 15)));
        valuesPanel.add(new JLabel("Imported"));
        valuesPanel.add(Box.createRigidArea(new Dimension(1, 13)));

        valuesPanel.add(new JLabel(" "));
        valuesPanel.add(Box.createRigidArea(new Dimension(1, 17)));

        valuesPanel.add(new JLabel(" "));

        panel.add(valuesPanel);

        return panel;
    }

    private JPanel createTimeSpaceSection() {
        JPanel panel = new JPanel();

        // labels
        JPanel labelsPanel = new JPanel();
        labelsPanel.setLayout(new BoxLayout(labelsPanel, BoxLayout.Y_AXIS));

        labelsPanel.add(Box.createRigidArea(new Dimension(1, 10)));
        labelsPanel.add(new JLabel("Time Period"));
        labelsPanel.add(Box.createRigidArea(new Dimension(1, 30)));
        labelsPanel.add(new JLabel("Temporal Resolution"));
        labelsPanel.add(Box.createRigidArea(new Dimension(1, 20)));
        labelsPanel.add(new JLabel("Sectors"));
        labelsPanel.add(Box.createRigidArea(new Dimension(1, 10)));
        labelsPanel.add(new JLabel("Region"));
        labelsPanel.add(Box.createRigidArea(new Dimension(1, 10)));
        labelsPanel.add(new JLabel("Country"));

        panel.add(labelsPanel);

        // values
        JPanel valuesPanel = new JPanel();
        valuesPanel.setLayout(new BoxLayout(valuesPanel, BoxLayout.Y_AXIS));

        JPanel startDatePanel = new JPanel();
        startDatePanel.add(new JLabel("Start: "));
        startDateTime = new TextField("startDateTime", 12);
        startDateTime.setText(format(dataset.getStartDateTime()));
        startDatePanel.add(startDateTime);
        valuesPanel.add(startDatePanel);

        JPanel endDatePanel = new JPanel();
        endDatePanel.add(new JLabel("End:   "));
        endDateTime = new TextField("startDateTime", 12);
        endDateTime.setText(format(dataset.getStopDateTime()));
        endDatePanel.add(endDateTime);
        valuesPanel.add(endDatePanel);

        valuesPanel.add(Box.createRigidArea(new Dimension(1, 5)));

        temporalResolutions = new DefaultComboBoxModel(new String[] { dataset.getTemporalResolution() });
        JComboBox temporalResolutionsCombo = new JComboBox(temporalResolutions);
        temporalResolutionsCombo.setName("temporalResolutions");
        temporalResolutionsCombo.setPreferredSize(new Dimension(100, 20));
        valuesPanel.add(temporalResolutionsCombo);
        valuesPanel.add(Box.createRigidArea(new Dimension(1, 12)));

        // TODO: lookup sectors
        sectors = new DefaultListModel();
        sectors.copyInto(new String[] { "A", "B" });
        valuesPanel.add(createList("sectors", sectors));
        valuesPanel.add(Box.createRigidArea(new Dimension(1, 8)));

        region = new TextField("region", dataset.getRegion(), 15);
        valuesPanel.add(region);
        valuesPanel.add(Box.createRigidArea(new Dimension(1, 6)));

        // TODO: lookup countries
        countries = new DefaultListModel();
        countries.copyInto(new String[] { dataset.getCountry(), "Jamaica" });
        JScrollPane list = createList("countries", countries);
        valuesPanel.add(list);

        panel.add(valuesPanel);

        return panel;
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

    private String format(Date date) {
        return DATE_FORMATTER.format(date);
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
        description.setSize(20, 45);
        JScrollPane scrollPane = new JScrollPane(description, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        layoutGenerator.addLabelWidgetPair("Description", scrollPane, panel);

        // project
        project = new TextField("project", 15);
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

    private Border createBorder() {
        return BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
    }

    public void updateDataset(EmfDataset dataset) {
        dataset.setName(name.getText());
        dataset.setDescription(description.getText());
        dataset.setStartDateTime(toDate(startDateTime.getText()));
        dataset.setStopDateTime(toDate(endDateTime.getText()));
        dataset.setTemporalResolution((String) temporalResolutions.getSelectedItem());
        dataset.setRegion(region.getText());
        // TODO: selected country needs to be captured
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

}
