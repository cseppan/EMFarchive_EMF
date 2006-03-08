package gov.epa.emissions.framework.client.meta.summary;

import gov.epa.emissions.commons.gui.ScrollableTextArea;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.io.Country;
import gov.epa.emissions.commons.io.Project;
import gov.epa.emissions.commons.io.Region;
import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.IntendedUse;

import java.awt.BorderLayout;
import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
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
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        container.add(createTimeSpaceSection());
        container.add(createStatusSection());

        panel.add(container, BorderLayout.LINE_START);

        return panel;
    }

    private JPanel createStatusSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        panel.add(createStatusDatesPanel(), BorderLayout.PAGE_START);

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

        IntendedUse intendedUse = dataset.getIntendedUse();
        String intendedUseName = (intendedUse != null) ? intendedUse.getName() : "";
        layoutGenerator.addLabelWidgetPair("Intended Use:", new Label("intendedUse", intendedUseName), panel);

        layoutGenerator.addLabelWidgetPair("Default Version:", new Label("defaultVersion", ""
                + dataset.getDefaultVersion()), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 6, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JPanel createTimeSpaceSection() {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        // time period
        layoutGenerator.addLabelWidgetPair("Time Period Start:", new JLabel(formatDate(dataset.getStartDateTime())),
                panel);
        layoutGenerator
                .addLabelWidgetPair("Time Period End:", new JLabel(formatDate(dataset.getStopDateTime())), panel);
        layoutGenerator.addLabelWidgetPair("Temporal Resolution:", new JLabel(dataset.getTemporalResolution()), panel);
        Sector[] sectors = dataset.getSectors();
        String sectorLabel = "";
        if (sectors != null && sectors.length > 0) {
            sectorLabel = sectors[0].toString();
        }
        layoutGenerator.addLabelWidgetPair("Sector:", new JLabel(sectorLabel), panel);
        Region region = dataset.getRegion();
        String regionName = (region != null) ? region.getName() : "";
        layoutGenerator.addLabelWidgetPair("Region:", new JLabel(regionName), panel);

        Country country = dataset.getCountry();
        String countryName = (country != null) ? country.getName() : "";
        layoutGenerator.addLabelWidgetPair("Country:", new JLabel(countryName), panel);

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

        Project project = dataset.getProject();
        String projectName = (project != null) ? project.getName() : "";
        layoutGenerator.addLabelWidgetPair("Project:", new JLabel(projectName), panel);
        layoutGenerator.addLabelWidgetPair("Creator:", new JLabel(dataset.getCreator()), panel);
        layoutGenerator.addLabelWidgetPair("Dataset Type:", new JLabel(dataset.getDatasetTypeName()), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 5, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

}
