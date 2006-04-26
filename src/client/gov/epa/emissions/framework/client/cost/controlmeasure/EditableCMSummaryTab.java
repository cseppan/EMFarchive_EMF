package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class EditableCMSummaryTab extends JPanel implements EditableCMSummaryTabView {

    private ControlMeasure measure;

    public final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("MM/dd/yyyy HH:mm");

    private TextField name;

    private TextArea description;
    
    private JLabel lastModTime;

    private MessagePanel messagePanel;

    private ManageChangeables changeablesList;
    
    private EmfSession session;

    public EditableCMSummaryTab(ControlMeasure measure,
            EmfSession session, MessagePanel messagePanel, ManageChangeables changeablesList) {
        super.setName("summary");
        this.measure = measure;
        this.messagePanel = messagePanel;
        this.changeablesList = changeablesList;
        this.session = session;

        super.setLayout(new BorderLayout());
        super.add(createOverviewSection(), BorderLayout.PAGE_START);
    }

    private String format(Date date) {
        return DATE_FORMATTER.format(date);
    }

    private JPanel createOverviewSection() {
        JPanel panel = new JPanel(new SpringLayout());
        //panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        // name
        name = new TextField("new control measure name", 25);
        name.setText(measure.getName());
        name.setMaximumSize(new Dimension(300, 15));
        changeablesList.addChangeable(name);

        layoutGenerator.addLabelWidgetPair("Name:", name, panel);

        // description
        description = new TextArea("description", measure.getDescription());
        changeablesList.addChangeable(description);
        layoutGenerator.addLabelWidgetPair("Description:", new ScrollableComponent(description), panel);
        
        // last modified time
        lastModTime = new JLabel(format(new Date()));
        layoutGenerator.addLabelWidgetPair("Last modified time:", lastModTime, panel);

        JLabel creator = createLeftAlignedLabel("");
        layoutGenerator.addLabelWidgetPair("Creator:", creator, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 4, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JLabel createLeftAlignedLabel(String name) {
        JLabel datasetTypeLabel = new JLabel(name);
        datasetTypeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        return datasetTypeLabel;
    }

    public void save(ControlMeasure measure) {
        messagePanel.clear();

        if (name.getText().equals("")) {
            messagePanel.setError("Name field should be a non-empty string.");
            return;
        }

        measure.setName(name.getText());
        measure.setDescription(description.getText());
        measure.setAnnualizedCost(0);
        measure.setCreator(session.user());
        measure.setDeviceCode(0);
        measure.setEquipmentLife(0);
        measure.setMajorPollutant("");
        measure.setRuleEffectiveness(0);
        measure.setRulePenetration(0);
    }

}
