package gov.epa.emissions.framework.client.meta.summary;

import gov.epa.emissions.commons.gui.ScrollableTextArea;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.services.EmfDataset;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
//import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class SummaryTab extends JPanel implements SummaryTabView {

    private EmfDataset dataset;

    public final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("MM/dd/yyyy HH:mm");

    public SummaryTab(EmfDataset dataset) {
        super.setName("summary");
        this.dataset = dataset;

        super.setLayout(new BorderLayout());
        super.add(createOverviewSection(), BorderLayout.PAGE_START);
        super.add(createLowerSection(), BorderLayout.CENTER);
    }

    private JPanel createLowerSection() {
        JPanel lowerPanel = new JPanel(new FlowLayout());

        lowerPanel.add(createTimeSpaceSection());
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
 //       panel.add(subscribed);

//        panel.add(new JLabel("Subscribed Users"));
        DefaultComboBoxModel subscribedUsersModel = new DefaultComboBoxModel(new String[0]);
        JComboBox subscribedUsers = new JComboBox(subscribedUsersModel);
        subscribedUsers.setName("subscribedUser");
        subscribedUsers.setPreferredSize(new Dimension(100, 20));
 //       panel.add(subscribedUsers);

        return panel;
    }

    private JPanel createStatusDatesPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Status:", new Label("status", dataset.getStatus()), panel);
        layoutGenerator.addLabelWidgetPair("Last Modified Date:", new Label("lastModifiedDate", formatDate(dataset
                .getModifiedDateTime())), panel);
        layoutGenerator.addLabelWidgetPair("Last Accessed Date:", new Label("lastAccessedDate", formatDate(dataset
                .getAccessedDateTime())), panel);
        layoutGenerator.addLabelWidgetPair("Creation Date:", new Label("creationDate", formatDate(dataset
                .getCreatedDateTime())), panel);

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
//        JPanel startDatePanel = new JPanel();
//        startDatePanel.add(new JLabel("Time Period Start:"));
//        startDatePanel.add(new JLabel(formatDate(dataset.getStartDateTime())));
//
//        JPanel endDatePanel = new JPanel();
//        endDatePanel.add(new JLabel("Time Period End:  "));
//        endDatePanel.add(new JLabel(formatDate(dataset.getStopDateTime())));
//
//        JPanel datesPanel = new JPanel();
//        datesPanel.setLayout(new BoxLayout(datesPanel, BoxLayout.Y_AXIS));
//        datesPanel.add(startDatePanel);
//        datesPanel.add(endDatePanel);

//        layoutGenerator.addLabelWidgetPair("", datesPanel, panel);
        layoutGenerator.addLabelWidgetPair("Time Period Start:", new JLabel(formatDate(dataset.getStartDateTime())), panel);
        layoutGenerator.addLabelWidgetPair("Time Period End:", new JLabel(formatDate(dataset.getStopDateTime())), panel);
        layoutGenerator.addLabelWidgetPair("Temporal Resolution:", new JLabel(dataset.getTemporalResolution()), panel);
        layoutGenerator.addLabelWidgetPair("Sector:", new JLabel(dataset.getSector()), panel);
        layoutGenerator.addLabelWidgetPair("Region:", new JLabel(dataset.getRegion()), panel);
        layoutGenerator.addLabelWidgetPair("Country:", new JLabel(dataset.getCountry()), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 6, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private String formatDate(Date date) {
        return date != null ? DATE_FORMATTER.format(date) : "";
    }

    private JPanel createOverviewSection() {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        // name
        layoutGenerator.addLabelWidgetPair("Name:", new JLabel(dataset.getName()), panel);

        // description
        TextArea description = new TextArea("description", dataset.getDescription());
        description.setEditable(false);
        layoutGenerator.addLabelWidgetPair("Description:", new ScrollableTextArea(description), panel);

        layoutGenerator.addLabelWidgetPair("Project:", new JLabel(dataset.getProject()), panel);
        layoutGenerator.addLabelWidgetPair("Creator:", new JLabel(dataset.getCreator()), panel);
        layoutGenerator.addLabelWidgetPair("Dataset Type:", new JLabel(dataset.getDatasetTypeName()), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 5, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

}
