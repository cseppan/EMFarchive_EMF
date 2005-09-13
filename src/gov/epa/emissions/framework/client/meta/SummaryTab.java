package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.io.EmfDataset;

import java.awt.Dimension;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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

        JPanel timespacePanel = createTimeSpaceSection();
        JPanel statusPanel = createStatusSection();

        JPanel lowerPanel = new JPanel();
        lowerPanel.add(timespacePanel);
        lowerPanel.add(statusPanel);

        super.add(lowerPanel);
    }

    private JPanel createStatusSection() {
        JPanel panel = new JPanel();
        panel.setBorder(createBorder());

        // labels
        JPanel labelsPanel = new JPanel();
        labelsPanel.setLayout(new BoxLayout(labelsPanel, BoxLayout.Y_AXIS));

        labelsPanel.add(new JLabel("Status"));
        labelsPanel.add(Box.createRigidArea(new Dimension(1, 15)));
        labelsPanel.add(new JLabel("Last Modified Date"));
        labelsPanel.add(Box.createRigidArea(new Dimension(1, 15)));
        labelsPanel.add(new JLabel("Last Accessed Date"));
        labelsPanel.add(Box.createRigidArea(new Dimension(1, 10)));

        panel.add(labelsPanel);

        // values
        JPanel valuesPanel = new JPanel();
        valuesPanel.setLayout(new BoxLayout(valuesPanel, BoxLayout.Y_AXIS));

        JLabel status = new JLabel();
        valuesPanel.add(status);
        valuesPanel.add(Box.createRigidArea(new Dimension(1, 10)));

        JLabel lastModifiedDate = new JLabel();
        valuesPanel.add(lastModifiedDate);
        valuesPanel.add(Box.createRigidArea(new Dimension(1, 10)));

        JLabel lastAccessedDate = new JLabel();
        valuesPanel.add(lastAccessedDate);

        panel.add(valuesPanel);

        return panel;
    }

    private JPanel createTimeSpaceSection() {
        JPanel panel = new JPanel();
        panel.setBorder(createBorder());

        // labels
        JPanel labelsPanel = new JPanel();
        labelsPanel.setLayout(new BoxLayout(labelsPanel, BoxLayout.Y_AXIS));

        labelsPanel.add(new JLabel("Time Period"));
        labelsPanel.add(Box.createRigidArea(new Dimension(1, 20)));
        labelsPanel.add(new JLabel("Temporal Resolution"));
        labelsPanel.add(Box.createRigidArea(new Dimension(1, 15)));
        labelsPanel.add(new JLabel("Sectors"));
        labelsPanel.add(Box.createRigidArea(new Dimension(1, 10)));
        labelsPanel.add(new JLabel("Region"));
        labelsPanel.add(Box.createRigidArea(new Dimension(1, 10)));
        labelsPanel.add(new JLabel("Country"));

        panel.add(labelsPanel);

        // values
        JPanel valuesPanel = new JPanel();
        valuesPanel.setLayout(new BoxLayout(valuesPanel, BoxLayout.Y_AXIS));

        JLabel startDateTime = new JLabel("Start: " + format(dataset.getStartDateTime()));
        valuesPanel.add(startDateTime);
        JLabel endDateTime = new JLabel("End:   " + format(dataset.getStopDateTime()));
        valuesPanel.add(endDateTime);
        valuesPanel.add(Box.createRigidArea(new Dimension(1, 10)));

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

        labelsPanel.add(new JLabel("Name"));
        labelsPanel.add(Box.createRigidArea(new Dimension(1, 15)));
        labelsPanel.add(new JLabel("Description"));
        labelsPanel.add(Box.createRigidArea(new Dimension(1, 15)));
        labelsPanel.add(new JLabel("Project"));
        labelsPanel.add(Box.createRigidArea(new Dimension(1, 10)));
        labelsPanel.add(new JLabel("Creator"));
        labelsPanel.add(Box.createRigidArea(new Dimension(1, 10)));
        labelsPanel.add(new JLabel("Dataset Type"));

        panel.add(labelsPanel);

        // values
        JPanel valuesPanel = new JPanel();
        valuesPanel.setLayout(new BoxLayout(valuesPanel, BoxLayout.Y_AXIS));

        TextField name = new TextField("name", 15);
        name.setText(dataset.getName());
        name.setMaximumSize(new Dimension(300, 15));
        valuesPanel.add(name);
        valuesPanel.add(Box.createRigidArea(new Dimension(1, 10)));

        TextField description = new TextField("description", 40);
        description.setText(dataset.getDescription());
        valuesPanel.add(description);
        valuesPanel.add(Box.createRigidArea(new Dimension(1, 10)));

        TextField project = new TextField("project", 15);
        project.setMaximumSize(new Dimension(300, 15));
        valuesPanel.add(project);
        valuesPanel.add(Box.createRigidArea(new Dimension(1, 8)));

        JLabel creator = new JLabel(dataset.getCreator());
        valuesPanel.add(creator);
        valuesPanel.add(Box.createRigidArea(new Dimension(1, 10)));

        JLabel datasetType = new JLabel(dataset.getDatasetType());
        valuesPanel.add(datasetType);

        panel.add(valuesPanel);

        return panel;
    }

    private Border createBorder() {
        return BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
    }

}
