package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.gui.CheckBox;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.EditableComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.casemanagement.RunStatuses;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.cost.controlmeasure.YearValidation;
import gov.epa.emissions.framework.ui.RefreshObserver;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.text.ParseException;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class EditableCaseSummaryTab extends JPanel implements EditableCaseSummaryTabView, RefreshObserver {

    private Case caseObj;

    private ManageChangeables changeablesList;

    private TextField name;

    private TextField futureYear;

    private TextField template;

    private TextArea description;

    private EditableComboBox projectsCombo;

    private EmfSession session;

    private EditableComboBox modelToRunCombo;

    private ComboBox modRegionsCombo;

    private ComboBox controlRegionsCombo;

    private EditableComboBox abbreviationsCombo;

    private EditableComboBox airQualityModelsCombo;

    private ComboBox categoriesCombo;

    private EditableComboBox emissionsYearCombo;

    private EditableComboBox gridCombo;

    private EditableComboBox meteorlogicalYearCombo;

    private EditableComboBox speciationCombo;

    private EditableComboBox gridResolutionCombo;

    private CheckBox isFinal;

    private CheckBox isTemplate;

    private AddRemoveSectorWidget sectorsWidget;

    private ComboBox runStatusCombo;

    private TextField startDate;

    private TextField endDate;

    private Dimension defaultDimension = new Dimension(255, 22);

    private EditCaseSummaryTabPresenter presenter;

    private EmfConsole parentConsole;
    
    private int fieldWidth=23;
    
    public EditableCaseSummaryTab(Case caseObj, EmfSession session, ManageChangeables changeablesList,
            EmfConsole parentConsole) {
        super.setName("summary");
        this.caseObj = caseObj;
        this.session = session;
        this.changeablesList = changeablesList;
        this.parentConsole = parentConsole;

    }

    public void display() throws EmfException {
        setLayout();
    }

    private void setLayout() throws EmfException {
        super.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(createOverviewSection());
        panel.add(createLowerSection());

        super.add(panel, BorderLayout.CENTER);
    }

    private JPanel createOverviewSection() throws EmfException {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));

        JPanel container = new JPanel();
        container.setLayout(new GridLayout(1, 2));
        container.add(createLeftOverviewSection());
        container.add(createRightOverviewSection());

        panel.add(container, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLeftOverviewSection() throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Name:", name(), panel);
        layoutGenerator.addLabelWidgetPair("Category:", categories(), panel);
        // adding extra spaces in the label shifts things over a bit to align upper and lower panels
        layoutGenerator.addLabelWidgetPair("Description:             ", description(), panel);
        layoutGenerator.addLabelWidgetPair("Project:", projects(), panel);
        layoutGenerator.addLabelWidgetPair("Run Status:", runStatus(), panel);
//        layoutGenerator.addLabelWidgetPair("Last Modified Date:", lastModifiedDate(), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 5, 2, // rows, cols
                10, 10, // initialX, initialY
                5, 10);// xPad, yPad

        return panel;
    }

    private JPanel createRightOverviewSection() throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Abbreviation:", abbreviations(), panel);
        JPanel finalTemplatePanel = new JPanel(new GridLayout(1, 2));
        finalTemplatePanel.add(isFinal());
        finalTemplatePanel.add(isTemplate());
        layoutGenerator.addLabelWidgetPair("Is Final:", finalTemplatePanel, panel);
        layoutGenerator.addLabelWidgetPair("<html>Sectors:<br><br><br></html>", sectors(), panel);
        layoutGenerator.addLabelWidgetPair("Copied From:", template(), panel);
        layoutGenerator.addLabelWidgetPair("Last Modified By:     ", creator(), panel);

        layoutGenerator.makeCompactGrid(panel, 5, 2, 10, 10, 5, 10);

        return panel;
    }

    private JPanel createLowerSection() throws EmfException {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));

        JPanel container = new JPanel();
        container.setLayout(new GridLayout(1, 2));
        container.add(createLowerLeftSection());
        container.add(createLowerRightSection());

        panel.add(container, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLowerLeftSection() throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Model to Run:", modelToRun(), panel);
        layoutGenerator.addLabelWidgetPair("Modeling Region:", modRegions(), panel);
        layoutGenerator.addLabelWidgetPair("Control Region:", controlRegions(), panel);
        layoutGenerator.addLabelWidgetPair("Grid Name:", grids(), panel);
        layoutGenerator.addLabelWidgetPair("Grid Resolution:", gridResolution(), panel);
        layoutGenerator.addLabelWidgetPair("Start Date & Time: ", startDate(), panel);

        layoutGenerator.makeCompactGrid(panel, 6, 2, 10, 10, 5, 10);

        return panel;
    }

    private JPanel createLowerRightSection() throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Air Quality Model:", airQualityModels(), panel);
        layoutGenerator.addLabelWidgetPair("Speciation:", speciations(), panel);
        layoutGenerator.addLabelWidgetPair("Meteorological Year:", meteorlogicalYears(), panel);
        layoutGenerator.addLabelWidgetPair("Base Year:", emissionsYears(), panel);
        layoutGenerator.addLabelWidgetPair("Future Year:", futureYear(), panel);
        layoutGenerator.addLabelWidgetPair("End Date & Time:", endDate(), panel);

        layoutGenerator.makeCompactGrid(panel, 6, 2, 10, 10, 5, 10);

        return panel;
    }

//    private JLabel lastModifiedDate() {
//        return createLeftAlignedLabel(format(caseObj.getLastModifiedDate()));
//    }

    private JLabel creator() {
        return createLeftAlignedLabel(caseObj.getLastModifiedBy().getName()+ " on "
                +format(caseObj.getLastModifiedDate()));
    }

    private ScrollableComponent description() {
        description = new TextArea("description", caseObj.getDescription(), fieldWidth, 3);
        changeablesList.addChangeable(description);
 
        ScrollableComponent descScrollableTextArea = new ScrollableComponent(description);
        descScrollableTextArea.setPreferredSize(new Dimension(255,80));
        return descScrollableTextArea;
    }

    private TextField name() {
        name = new TextField("name", this.fieldWidth);
        name.setText(caseObj.getName());
        name.setPreferredSize(defaultDimension);
        name.setToolTipText(caseObj.getName());
        changeablesList.addChangeable(name);

        return name;
    }
    

    private TextField futureYear() {
        futureYear = new TextField("Future Year", fieldWidth);
        futureYear.setText(caseObj.getFutureYear() + "");
        changeablesList.addChangeable(futureYear);
        futureYear.setPreferredSize(defaultDimension);

        return futureYear;
    }

    private TextField template() {
        template = new TextField("Template", fieldWidth);
        template.setText(caseObj.getTemplateUsed());
        template.setToolTipText(caseObj.getTemplateUsed());
        template.setEditable(false);
        template.setPreferredSize(defaultDimension);
        template.setMaximumSize(defaultDimension);

        return template;
    }

    private JComponent isTemplate() {
        isTemplate = new CheckBox(" Is Template");
        isTemplate.setSelected(caseObj.isCaseTemplate());

        return isTemplate;
    }

    private JComponent isFinal() {
        isFinal = new CheckBox("");
        isFinal.setSelected(caseObj.getIsFinal());

        return isFinal;
    }

    private EditableComboBox projects() throws EmfException {
        projectsCombo = new EditableComboBox(presenter.getProjects());
        projectsCombo.setSelectedItem(caseObj.getProject());
        projectsCombo.setPreferredSize(defaultDimension);
        addPopupMenuListener(projectsCombo, "projects");
        changeablesList.addChangeable(projectsCombo);

        return projectsCombo;
    }

    private EditableComboBox modelToRun() throws EmfException {
        modelToRunCombo = new EditableComboBox(presenter.getModelToRuns());
        modelToRunCombo.setSelectedItem(caseObj.getModel());
        modelToRunCombo.setPreferredSize(defaultDimension);
        addPopupMenuListener(modelToRunCombo, "modeltoruns");
        changeablesList.addChangeable(modelToRunCombo);

        return modelToRunCombo;
    }

    private ComboBox modRegions() throws EmfException {
        modRegionsCombo = new ComboBox(presenter.getRegions());
        modRegionsCombo.setSelectedItem(caseObj.getModelingRegion());
        modRegionsCombo.setPreferredSize(defaultDimension);
        changeablesList.addChangeable(modRegionsCombo);

        return modRegionsCombo;
    }

    private EditableComboBox gridResolution() throws EmfException {
        gridResolutionCombo = new EditableComboBox(presenter.getGridResolutions());
        gridResolutionCombo.setSelectedItem(caseObj.getGridResolution());
        gridResolutionCombo.setPreferredSize(defaultDimension);
        changeablesList.addChangeable(gridResolutionCombo);

        return gridResolutionCombo;
    }

    private ComboBox controlRegions() throws EmfException {
        controlRegionsCombo = new ComboBox(presenter.getRegions());
        controlRegionsCombo.setSelectedItem(caseObj.getControlRegion());
        controlRegionsCombo.setPreferredSize(defaultDimension);
        changeablesList.addChangeable(controlRegionsCombo);

        return controlRegionsCombo;
    }

    private EditableComboBox abbreviations() throws EmfException {
        abbreviationsCombo = new EditableComboBox(presenter.getAbbreviations());
        abbreviationsCombo.setSelectedItem(caseObj.getAbbreviation());
        abbreviationsCombo.setPreferredSize(defaultDimension);
        addPopupMenuListener(abbreviationsCombo, "abbreviations");
        changeablesList.addChangeable(abbreviationsCombo);

        return abbreviationsCombo;
    }

    private EditableComboBox airQualityModels() throws EmfException {
        airQualityModelsCombo = new EditableComboBox(presenter.getAirQualityModels());
        airQualityModelsCombo.setSelectedItem(caseObj.getAirQualityModel());
        airQualityModelsCombo.setPreferredSize(defaultDimension);

        changeablesList.addChangeable(airQualityModelsCombo);

        return airQualityModelsCombo;
    }

    private ComboBox categories() throws EmfException {
        categoriesCombo = new ComboBox(presenter.getCaseCategories());
        categoriesCombo.setSelectedItem(caseObj.getCaseCategory());
        categoriesCombo.setPreferredSize(defaultDimension);
        addPopupMenuListener(categoriesCombo, "categories");
        changeablesList.addChangeable(categoriesCombo);

        return categoriesCombo;
    }

    private JPanel sectors() throws EmfException {
        sectorsWidget = new AddRemoveSectorWidget(presenter.getAllSectors(), changeablesList, parentConsole);
        sectorsWidget.setSectors(caseObj.getSectors());
        sectorsWidget.setPreferredSize(new Dimension(255, 80));
        return sectorsWidget;
    }

    private EditableComboBox emissionsYears() throws EmfException {
        emissionsYearCombo = new EditableComboBox(presenter.getEmissionsYears());
        emissionsYearCombo.setSelectedItem(caseObj.getEmissionsYear());
        emissionsYearCombo.setPreferredSize(defaultDimension);
        changeablesList.addChangeable(emissionsYearCombo);

        return emissionsYearCombo;
    }

    private EditableComboBox grids() throws EmfException {
        gridCombo = new EditableComboBox(presenter.getGrids());
        gridCombo.setSelectedItem(caseObj.getGrid());
        gridCombo.setPreferredSize(defaultDimension);
        changeablesList.addChangeable(gridCombo);

        return gridCombo;
    }

    private EditableComboBox meteorlogicalYears() throws EmfException {
        meteorlogicalYearCombo = new EditableComboBox(presenter.getMeteorlogicalYears());
        meteorlogicalYearCombo.setSelectedItem(caseObj.getMeteorlogicalYear());
        meteorlogicalYearCombo.setPreferredSize(defaultDimension);
        changeablesList.addChangeable(meteorlogicalYearCombo);

        return meteorlogicalYearCombo;
    }

    private EditableComboBox speciations() throws EmfException {
        speciationCombo = new EditableComboBox(presenter.getSpeciations());
        speciationCombo.setSelectedItem(caseObj.getSpeciation());
        speciationCombo.setPreferredSize(defaultDimension);

        changeablesList.addChangeable(speciationCombo);

        return speciationCombo;
    }

    private ComboBox runStatus() {
        runStatusCombo = new ComboBox(RunStatuses.all());
        runStatusCombo.setPreferredSize(defaultDimension);
        if (caseObj.getRunStatus() == null) {
            runStatusCombo.setSelectedIndex(0);
        } else {
            runStatusCombo.setSelectedItem(caseObj.getRunStatus());
        }
        changeablesList.addChangeable(runStatusCombo);

        return runStatusCombo;
    }

    private TextField startDate() {
        startDate = new TextField("Start Date", fieldWidth);
        startDate.setText(format(caseObj.getStartDate()) + "");
        changeablesList.addChangeable(startDate);
        startDate.setPreferredSize(defaultDimension);
        startDate.setToolTipText("Date in format MM/dd/yyyy HH:mm");

        return startDate;
    }

    private TextField endDate() {
        endDate = new TextField("End Date", fieldWidth);
        endDate.setText(format(caseObj.getEndDate()) + "");
        changeablesList.addChangeable(endDate);
        endDate.setPreferredSize(defaultDimension);
        endDate.setToolTipText("Date in format MM/dd/yyyy HH:mm");

        return endDate;
    }

    private void addPopupMenuListener(final JComboBox box, final String toget) {
        box.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuCanceled(PopupMenuEvent event) {
                // NOTE Auto-generated method stub
            }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent event) {
                // NOTE Auto-generated method stub
            }

            public void popupMenuWillBecomeVisible(PopupMenuEvent event) {
                try {
                    Object selected = box.getSelectedItem();
                    box.setModel(new DefaultComboBoxModel(getAllObjects(toget)));
                    box.setSelectedItem(selected);
                } catch (Exception e) {
                    e.printStackTrace();
                    // messagePanel.setError(e.getMessage());
                }
            }
        });
    }

    protected Object[] getAllObjects(String toget) throws EmfException {
        if (toget.equals("categories"))
            return presenter.getCaseCategories();

        else if (toget.equals("abbreviations"))
            return presenter.getAbbreviations();

        else if (toget.equals("projects"))
            return presenter.getProjects();

        else if (toget.equals("modeltoruns"))
            return presenter.getModelToRuns();

        // else if (toget.equals("sectors"))
        // return presenter.getSectors();
        //
        // else if (toget.equals("subdirs"))
        // return presenter.getSubdirs();
        //
        // else
        // throw new EmfException("Unknown object type: " + toget);

        return new Object[0];

    }

    private String format(Date date) {
        return CustomDateFormat.format_MM_DD_YYYY_HH_mm(date);
    }

    private JLabel createLeftAlignedLabel(String name) {
        JLabel label = new JLabel(name);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        return label;
    }

    public void save(Case caseObj) throws EmfException {
        caseObj.setName(name.getText());
        saveFutureYear();
        caseObj.setDescription(description.getText());
        caseObj.setCaseTemplate(isTemplate.isSelected());
        caseObj.setIsFinal(isFinal.isSelected());
        caseObj.setProject(presenter.getProject(projectsCombo.getSelectedItem()));
        caseObj.setModelingRegion((Region) modRegionsCombo.getSelectedItem());
        caseObj.setControlRegion((Region) controlRegionsCombo.getSelectedItem());
        caseObj.setAbbreviation(presenter.getAbbreviation(abbreviationsCombo.getSelectedItem()));
        caseObj.setAirQualityModel(presenter.getAirQualityModel(airQualityModelsCombo.getSelectedItem()));
        caseObj.setCaseCategory(presenter.getCaseCategory(categoriesCombo.getSelectedItem()));
        caseObj.setEmissionsYear(presenter.getEmissionsYear(emissionsYearCombo.getSelectedItem()));
        caseObj.setGrid(presenter.getGrid(gridCombo.getSelectedItem()));
        caseObj.setMeteorlogicalYear(presenter.getMeteorlogicalYear(meteorlogicalYearCombo.getSelectedItem()));
        caseObj.setSpeciation(presenter.getSpeciation(speciationCombo.getSelectedItem()));
        caseObj.setRunStatus(runStatusCombo.getSelectedItem() + "");
        saveStartDate();
        saveEndDate();
        caseObj.setSectors(sectorsWidget.getSectors());
        caseObj.setModel(presenter.getModelToRun(modelToRunCombo.getSelectedItem()));
        caseObj.setGridResolution(presenter.getGridResolutionl(gridResolutionCombo.getSelectedItem()));
    }

    private void saveFutureYear() throws EmfException {
        String year = futureYear.getText().trim();
        if (year.length() == 0 || year.equals("0")) {
            caseObj.setFutureYear(0);
            return;
        }
        YearValidation validation = new YearValidation("Future Year");
        caseObj.setFutureYear(validation.value(futureYear.getText()));
    }

    private void saveEndDate() throws EmfException {
        try {
            String date = startDate.getText().trim();
            if (date.length() == 0) {
                caseObj.setStartDate(null);
                return;
            }
            caseObj.setStartDate(CustomDateFormat.parse_MM_DD_YYYY_HH_mm(startDate.getText()));
        } catch (ParseException e) {
            throw new EmfException("Please enter the Start Date in the correct format (MM/dd/yyyy HH:mm)");
        }
    }

    private void saveStartDate() throws EmfException {
        try {
            String date = endDate.getText().trim();
            if (date.length() == 0) {
                caseObj.setEndDate(null);
                return;
            }
            caseObj.setEndDate(CustomDateFormat.parse_MM_DD_YYYY_HH_mm(endDate.getText()));
        } catch (ParseException e) {
            throw new EmfException("Please enter the End Date in the correct format (MM/dd/yyyy HH:mm)");
        }
    }

    public void observe(EditCaseSummaryTabPresenter presenter) {
        this.presenter = presenter;
    }

    public void doRefresh() throws EmfException {
        super.removeAll();
        setLayout();
        checkIfLockedByCurrentUser();
    }

    public void checkIfLockedByCurrentUser() throws EmfException {
        Case reloaded = session.caseService().reloadCase(caseObj.getId());

        if (!reloaded.isLocked(session.user()))
            throw new EmfException("Lock on current case object expired. User " + reloaded.getLockOwner()
                    + " has it now.");
    }

}
