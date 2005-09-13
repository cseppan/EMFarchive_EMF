package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.io.EmfDataset;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

public class SummaryTab extends JPanel {

    private EmfDataset dataset;

    public SummaryTab(EmfDataset dataset) {
        this.dataset = dataset;

        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        setLayout();
    }

    private void setLayout() {
        super.add(createOverview());

        JPanel localePanel = createLocalePanel();
        JPanel statusPanel = createStatusPanel();

        JPanel lowerPanel = new JPanel();
        lowerPanel.add(localePanel);
        lowerPanel.add(statusPanel);

        super.add(lowerPanel);
    }

    private JPanel createStatusPanel() {
        return new JPanel();
    }

    private JPanel createLocalePanel() {
        return new JPanel();
    }

    private JPanel createOverview() {
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
        valuesPanel.add(Box.createRigidArea(new Dimension(1, 10)));

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
