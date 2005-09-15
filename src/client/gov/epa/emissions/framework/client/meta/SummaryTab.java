package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.services.EmfDataset;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.text.DateFormat;
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
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

public class SummaryTab extends JPanel {

    private EmfDataset dataset;

    private final DateFormat DATE_FORMATTER = new SimpleDateFormat("MM/dd/yyyy    hh:mm:ss");

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

        panel.add(createRightAlignedLabel("Subscribed Users"));
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
        labelsPanel.add(createRightAlignedLabel("Status"));
        labelsPanel.add(Box.createRigidArea(new Dimension(1, 15)));
        labelsPanel.add(createRightAlignedLabel("Last Modified Date"));
        labelsPanel.add(Box.createRigidArea(new Dimension(1, 15)));
        labelsPanel.add(createRightAlignedLabel("Last Accessed Date"));

        panel.add(labelsPanel);

        // values
        JPanel valuesPanel = new JPanel();
        valuesPanel.setLayout(new BoxLayout(valuesPanel, BoxLayout.Y_AXIS));

        valuesPanel.add(Box.createRigidArea(new Dimension(1, 15)));
        valuesPanel.add(createRightAlignedLabel("<TBD>"));
        valuesPanel.add(Box.createRigidArea(new Dimension(1, 13)));

        valuesPanel.add(createRightAlignedLabel("<TBD>"));
        valuesPanel.add(Box.createRigidArea(new Dimension(1, 17)));

        valuesPanel.add(createRightAlignedLabel("<TBD>"));

        panel.add(valuesPanel);

        return panel;
    }

    private JPanel createTimeSpaceSection() {
        JPanel panel = new JPanel();

        // labels
        JPanel labelsPanel = new JPanel();
        labelsPanel.setLayout(new BoxLayout(labelsPanel, BoxLayout.Y_AXIS));

        labelsPanel.add(Box.createRigidArea(new Dimension(1, 10)));
        labelsPanel.add(createRightAlignedLabel("Time Period"));
        labelsPanel.add(Box.createRigidArea(new Dimension(1, 30)));
        labelsPanel.add(createRightAlignedLabel("Temporal Resolution"));
        labelsPanel.add(Box.createRigidArea(new Dimension(1, 20)));
        labelsPanel.add(createRightAlignedLabel("Sectors"));
        labelsPanel.add(Box.createRigidArea(new Dimension(1, 10)));
        labelsPanel.add(createRightAlignedLabel("Region"));
        labelsPanel.add(Box.createRigidArea(new Dimension(1, 10)));
        labelsPanel.add(createRightAlignedLabel("Country"));

        panel.add(labelsPanel);

        // values
        JPanel valuesPanel = new JPanel();
        valuesPanel.setLayout(new BoxLayout(valuesPanel, BoxLayout.Y_AXIS));

        JPanel startDatePanel = new JPanel();
        startDatePanel.add(new JLabel("Start: "));
        TextField startDateTime = new TextField("startDateTime", 12);
        startDateTime.setText(format(dataset.getStartDateTime()));
        startDatePanel.add(startDateTime);
        valuesPanel.add(startDatePanel);

        JPanel endDatePanel = new JPanel();
        endDatePanel.add(new JLabel("End:   "));
        TextField endDateTime = new TextField("startDateTime", 12);
        endDateTime.setText(format(dataset.getStopDateTime()));
        endDatePanel.add(endDateTime);
        valuesPanel.add(endDatePanel);

        valuesPanel.add(Box.createRigidArea(new Dimension(1, 5)));

        DefaultComboBoxModel temporalResolutionsModel = new DefaultComboBoxModel(new String[] { dataset
                .getTemporalResolution() });
        JComboBox temporalResolutions = new JComboBox(temporalResolutionsModel);
        temporalResolutions.setName("temporalResolutions");
        temporalResolutions.setPreferredSize(new Dimension(100, 20));
        valuesPanel.add(temporalResolutions);
        valuesPanel.add(Box.createRigidArea(new Dimension(1, 12)));

        // TODO: lookup sectors
        valuesPanel.add(createList("sectors", new String[] { "A", "B" }));
        valuesPanel.add(Box.createRigidArea(new Dimension(1, 8)));

        TextField region = new TextField("region", 15);
        valuesPanel.add(region);
        valuesPanel.add(Box.createRigidArea(new Dimension(1, 6)));

        // TODO: lookup countries
        JScrollPane list = createList("countries", new String[] { dataset.getCountry(), "Jamaica" });
        valuesPanel.add(list);

        panel.add(valuesPanel);

        return panel;
    }

    private JScrollPane createList(String name, String[] values) {
        DefaultListModel model = new DefaultListModel();
        for (int i = 0; i < values.length; i++) {
            model.addElement(values[i]);
        }
        JList list = new JList(model);
        list.setName(name);
        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        list.setVisibleRowCount(0);

        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setPreferredSize(new Dimension(100, 20));

        return scrollPane;
    }

    private String format(Date date) {
        return DATE_FORMATTER.format(date);
    }

    private JPanel createOverviewSection() {
        JPanel panel = new JPanel();
        panel.setBorder(createBorder());

        // labels
        JPanel labelsPanel = new JPanel();
        labelsPanel.setLayout(new BoxLayout(labelsPanel, BoxLayout.Y_AXIS));

        labelsPanel.add(createRightAlignedLabel("Name"));
        labelsPanel.add(Box.createRigidArea(new Dimension(1, 10)));
        labelsPanel.add(createRightAlignedLabel("Description"));
        labelsPanel.add(Box.createRigidArea(new Dimension(1, 20)));
        labelsPanel.add(createRightAlignedLabel("Project"));
        labelsPanel.add(Box.createRigidArea(new Dimension(1, 10)));
        labelsPanel.add(createRightAlignedLabel("Creator"));
        labelsPanel.add(Box.createRigidArea(new Dimension(1, 10)));
        labelsPanel.add(createRightAlignedLabel("Dataset Type"));

        panel.add(labelsPanel);

        // values
        JPanel valuesPanel = new JPanel();
        valuesPanel.setLayout(new BoxLayout(valuesPanel, BoxLayout.Y_AXIS));

        TextField name = new TextField("name", 15);
        name.setText(dataset.getName());
        name.setMaximumSize(new Dimension(300, 15));
        valuesPanel.add(name);
        valuesPanel.add(Box.createRigidArea(new Dimension(1, 5)));

        JScrollPane scrollPane = createScrollableTextArea("description", dataset.getDescription());
        valuesPanel.add(scrollPane);
        valuesPanel.add(Box.createRigidArea(new Dimension(1, 5)));

        TextField project = new TextField("project", 15);
        project.setMaximumSize(new Dimension(300, 15));
        valuesPanel.add(project);
        valuesPanel.add(Box.createRigidArea(new Dimension(1, 4)));

        JLabel creator = createLeftAlignedLabel(dataset.getCreator());
        valuesPanel.add(creator);
        valuesPanel.add(Box.createRigidArea(new Dimension(1, 10)));

        JLabel datasetType = createLeftAlignedLabel(dataset.getDatasetType());
        valuesPanel.add(datasetType);

        panel.add(valuesPanel);

        return panel;
    }

    private JLabel createRightAlignedLabel(String name) {
        JLabel datasetTypeLabel = new JLabel(name);
        datasetTypeLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        return datasetTypeLabel;
    }

    private JLabel createLeftAlignedLabel(String name) {
        JLabel datasetTypeLabel = new JLabel(name);
        datasetTypeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        return datasetTypeLabel;
    }

    private JScrollPane createScrollableTextArea(String name, String value) {
        JTextArea description = new TextArea(name, value);

        return new JScrollPane(description, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    private Border createBorder() {
        return BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
    }

}
