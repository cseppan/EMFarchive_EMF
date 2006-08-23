package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.data.SourceGroup;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.EditableComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.data.ControlTechnologies;
import gov.epa.emissions.framework.client.data.EmfDateFormat;
import gov.epa.emissions.framework.client.data.SourceGroups;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.data.ControlTechnology;
import gov.epa.emissions.framework.ui.ListWidget;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.NumberFieldVerifier;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

public class ControlMeasureSummaryTab extends JPanel implements ControlMeasureTabView {

    protected ControlMeasure measure;

    public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(EmfDateFormat.format());

    protected TextField name;

    protected TextArea description;

    private JLabel creator;

    protected ComboBox majorPollutant;

    protected EditableComboBox sourceGroup;

    protected EditableComboBox controlTechnology;

    protected TextField deviceCode;

    protected TextField dateReviewed;

    protected TextField equipmentLife;

    protected TextField costYear;

    protected TextField abbreviation;

    protected JLabel lastModifiedTime;

    protected ComboBox cmClass;

    protected ListWidget sectors;

    protected TextField dataSources;

    protected MessagePanel messagePanel;

    private ManageChangeables changeablesList;

    protected EmfSession session;

    private String[] classes = { "Known", "Emerging", "Hypothetical", "Obselete" };

    protected int deviceId, year;

    protected float cost, life, effectivness, penetration, minUnctrldEmiss, maxUnctrldEmiss;

    protected Pollutant[] allPollutants;

    protected SourceGroup[] allSourceGroups;

    protected ControlTechnology[] allControlTechnologies;

    private NumberFieldVerifier verifier;

    protected static DateFormat dateReviewedFormat = new SimpleDateFormat("MM/dd/yyyy");

    public ControlMeasureSummaryTab(ControlMeasure measure, EmfSession session, MessagePanel messagePanel,
            ManageChangeables changeablesList) {
        super.setName("summary");
        this.measure = measure;
        this.messagePanel = messagePanel;
        this.changeablesList = changeablesList;
        this.session = session;

        super.setLayout(new BorderLayout());
        super.add(createOverviewSection(), BorderLayout.PAGE_START);
        super.add(createAttributeSection(), BorderLayout.CENTER);
        this.verifier = new NumberFieldVerifier("Summary tab: ");
    }

    protected void populateFields() {
        String cmName = measure.getName();
        Date modifiedTime = measure.getLastModifiedTime();
        name.setText(getText(cmName));
        description.setText(getText(measure.getDescription()));
        creator.setText(getText(measure.getCreator().getName()));
        majorPollutant.setSelectedItem(measure.getMajorPollutant());
        sourceGroup.setSelectedItem(measure.getSourceGroup());
        controlTechnology.setSelectedItem(measure.getControlTechnology());
        cmClass.setSelectedItem(getText(measure.getCmClass()));
        // costYear.setText(measure.getCostYear() + "");
        deviceCode.setText(measure.getDeviceCode() + "");
        equipmentLife.setText(measure.getEquipmentLife() + "");
        if (modifiedTime != null)
            lastModifiedTime.setText(DATE_FORMATTER.format(modifiedTime));
        abbreviation.setText(getText(measure.getAbbreviation()));
        dateReviewed.setText(formatDateReviewed());
        dataSources.setText(getText(measure.getDataSouce()));
    }

    private String formatDateReviewed() {
        Date dateReviewed = measure.getDateReviewed();
        return dateReviewed == null ? "" : dateReviewedFormat.format(dateReviewed);
    }

    private String getText(String value) {
        return (value != null) ? value : "";
    }

    private JPanel createOverviewSection() {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addWidgetPair(createLeftOverview(), createRightOverview(), panel);
        widgetLayout(1, 2, 5, 5, 5, 5, layoutGenerator, panel);

        return panel;
    }

    private JPanel createLeftOverview() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        name = new TextField("Control measure name", 27);
        changeablesList.addChangeable(name);
        layoutGenerator.addLabelWidgetPair("Name:", name, panel);

        // description
        description = new TextArea("description", measure.getDescription());
        changeablesList.addChangeable(description);
        ScrollableComponent descPane = new ScrollableComponent(description);
        descPane.setPreferredSize(new Dimension(300, 50));
        layoutGenerator.addLabelWidgetPair("Description:", descPane, panel);

        widgetLayout(2, 2, 5, 5, 10, 10, layoutGenerator, panel);

        return panel;
    }

    private JPanel createRightOverview() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        abbreviation = new TextField("Abbreviation", 12);
        changeablesList.addChangeable(abbreviation);
        layoutGenerator.addLabelWidgetPair("Abbreviation:", abbreviation, panel);

        creator = new JLabel(session.user().getName());
        layoutGenerator.addLabelWidgetPair("Creator:", creator, panel);

        // AME: Moved temporarily to improve symmetry
        // lastModifiedTime = new JLabel("");
        // layoutGenerator.addLabelWidgetPair("Last Modified Time:", lastModifiedTime, panel);
        JPanel tempPanel = tempPanel(50, 20);
        layoutGenerator.addLabelWidgetPair("", tempPanel, panel);

        widgetLayout(3, 2, 5, 5, 10, 10, layoutGenerator, panel);

        return panel;
    }

    private JPanel tempPanel(int width, int height) {
        JPanel tempPanel = new JPanel();
        tempPanel.setPreferredSize(new Dimension(width, height));
        return tempPanel;
    }

    private JPanel createAttributeSection() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        container.add(createLeftPanel());
        container.add(createRightPanel());

        panel.add(container);

        return panel;
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        lastModifiedTime = new JLabel("");
        layoutGenerator.addLabelWidgetPair("Last Modified Time:", lastModifiedTime, panel);

        try {
            allPollutants = session.dataCommonsService().getPollutants();
            majorPollutant = new ComboBox("Choose a pollutant", allPollutants);
        } catch (EmfException e1) {
            messagePanel.setError("Could not retrieve Pollutants");
        }
        changeablesList.addChangeable(majorPollutant);
        layoutGenerator.addLabelWidgetPair("Major Pollutant:", majorPollutant, panel);

        deviceCode = new TextField("NEI Device code", 15);
        changeablesList.addChangeable(deviceCode);
        layoutGenerator.addLabelWidgetPair("NEI Device code:", deviceCode, panel);

        equipmentLife = new TextField("Equipment life", 15);
        changeablesList.addChangeable(equipmentLife);
        layoutGenerator.addLabelWidgetPair("Equipment life (yrs):", equipmentLife, panel);

        dateReviewed = new TextField("Date Reviewed", 15);
        changeablesList.addChangeable(dateReviewed);
        layoutGenerator.addLabelWidgetPair("Date Reviewed:", dateReviewed, panel);

        dataSources = new TextField("Data Sources:", 15);
        layoutGenerator.addLabelWidgetPair("Data Sources:", dataSources, panel);

        layoutGenerator.addLabelWidgetPair("", tempPanel(20, 20), panel);
        widgetLayout(7, 2, 5, 5, 10, 10, layoutGenerator, panel);

        return panel;
    }

    private Component createRightPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        cmClass = new ComboBox("Choose a class", classes);
        changeablesList.addChangeable(cmClass);
        layoutGenerator.addLabelWidgetPair("Class:", cmClass, panel);

        try {
            allControlTechnologies = session.controlMeasureService().getControlTechnologies();
            controlTechnology = new EditableComboBox(allControlTechnologies);
        } catch (EmfException e) {
            messagePanel.setError("Could not retrieve all Control Technologies");
        }
        changeablesList.addChangeable(controlTechnology);
        layoutGenerator.addLabelWidgetPair("Control Technology:", controlTechnology, panel);

        try {
            allSourceGroups = session.dataCommonsService().getSourceGroups();
            sourceGroup = new EditableComboBox(allSourceGroups);
        } catch (EmfException e) {
            messagePanel.setError("Could not retrieve Source Groups");
        }

        changeablesList.addChangeable(sourceGroup);
        layoutGenerator.addLabelWidgetPair("Source Group:", sourceGroup, panel);

        sectors = new ListWidget(new String[] { "               " }, new String[] { "" });
        JScrollPane listScroller = new JScrollPane(sectors);
        listScroller.setPreferredSize(new Dimension(170, 60));
        layoutGenerator.addLabelWidgetPair("Sectors:", listScroller, panel);

        layoutGenerator.addLabelWidgetPair("", addRemoveButtonPanel(), panel);

        widgetLayout(5, 2, 5, 5, 10, 10, layoutGenerator, panel);

        return panel;
    }

    private void widgetLayout(int rows, int cols, int initX, int initY, int xPad, int yPad,
            SpringLayoutGenerator layoutGenerator, JPanel panel) {
        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, rows, cols, // rows, cols
                initX, initY, // initialX, initialY
                xPad, yPad);// xPad, yPad
    }

    private JPanel addRemoveButtonPanel() {
        JPanel panel = new JPanel();
        // TBD: this needs to be changed so you have handles to the buttons
        // and can set actions
        JButton addButton = new JButton("Add");
        addButton.setEnabled(false);
        JButton removeButton = new JButton("Remove");
        removeButton.setEnabled(false);

        panel.add(addButton);
        panel.add(removeButton);
        // for now, disable these

        return panel;
    }

    public void save(ControlMeasure measure) throws EmfException {
        messagePanel.clear();
        validateFields();
        measure.setName(name.getText());
        measure.setDescription(description.getText());
        measure.setCreator(session.user());
        if (deviceCode.getText().length() > 0)
            measure.setDeviceCode(deviceId);
        if (equipmentLife.getText().length() > 0)
            measure.setEquipmentLife(life);
        updatePollutant();
        updateControlTechnology();
        updateSourceGroup();
        updateDateReviewed(measure);
        measure.setCmClass(selectedClass(cmClass.getSelectedItem()));
        measure.setLastModifiedTime(new Date());
        measure.setAbbreviation(abbreviation.getText());
        measure.setDataSouce(dataSources.getText());

    }

    private void updateDateReviewed(ControlMeasure measure) throws EmfException {
        try {
            String date = dateReviewed.getText().trim();
            if (date.length() == 0) {
                measure.setDateReviewed(null);
                return;
            }
            measure.setDateReviewed(dateReviewedFormat.parse(date));
        } catch (Exception e) {
            throw new EmfException("Please Correct the Date Format(MM/dd/yyyy) in Date Reviewed");
        }
    }

    private String selectedClass(Object selectedItem) {
        return selectedItem == null ? "" : selectedItem + "";
    }

    private void updateControlTechnology() {
        Object selected = controlTechnology.getSelectedItem();
        if (selected instanceof String) {
            String controltechnologyName = (String) selected;
            if (controltechnologyName.length() > 0) {
                ControlTechnology controltechnology = controltechnology(controltechnologyName);// checking for
                // duplicates
                measure.setControlTechnology(controltechnology);
            }
        } else if (selected instanceof ControlTechnology) {
            measure.setControlTechnology((ControlTechnology) selected);
        }
    }

    private ControlTechnology controltechnology(String name) {
        return new ControlTechnologies(allControlTechnologies).get(name);
    }

    private void updateSourceGroup() {
        Object selected = sourceGroup.getSelectedItem();
        if (selected instanceof String) {
            String sourcegroupName = (String) selected;
            if (sourcegroupName.length() > 0) {
                SourceGroup sourcegroup = sourcegroup(sourcegroupName);// checking for duplicates
                measure.setSourceGroup(sourcegroup);
            }
        } else if (selected instanceof SourceGroup) {
            measure.setSourceGroup((SourceGroup) selected);
        }
    }

    private SourceGroup sourcegroup(String name) {
        return new SourceGroups(allSourceGroups).get(name);
    }

    private void updatePollutant() {
        Object selected = majorPollutant.getSelectedItem();
        measure.setMajorPollutant((Pollutant) selected);
    }

    private void validateFields() throws EmfException {
        messagePanel.clear();

        if (name.getText().equals(""))
            throw new EmfException("Summary tab: Name should be a non-empty string.");

        if (abbreviation.getText().trim().length() < 1) {
            throw new EmfException("Summary tab: An abbreviation must be specified");
        }

        if (majorPollutant.getSelectedItem() == null)
            throw new EmfException("Summary tab: Please select a major pollutant");

        if (cmClass.getSelectedItem() == null)
            throw new EmfException("Summary tab: Please select a class");

        if (deviceCode.getText().trim().length() > 0)
            deviceId = verifier.parseInteger(deviceCode);

        if (equipmentLife.getText().trim().length() > 0)
            life = verifier.parseFloat(equipmentLife);

    }

}
