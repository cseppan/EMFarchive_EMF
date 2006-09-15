package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.commons.gui.CheckBox;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.EditableComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.casemanagement.Abbreviations;
import gov.epa.emissions.framework.client.casemanagement.AirQualityModels;
import gov.epa.emissions.framework.client.casemanagement.CaseCategories;
import gov.epa.emissions.framework.client.casemanagement.EmissionsYears;
import gov.epa.emissions.framework.client.casemanagement.Grids;
import gov.epa.emissions.framework.client.casemanagement.MeteorlogicalYears;
import gov.epa.emissions.framework.client.casemanagement.RunStatuses;
import gov.epa.emissions.framework.client.casemanagement.Speciations;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.Projects;
import gov.epa.emissions.framework.client.data.Regions;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.ModelToRun;
import gov.epa.emissions.framework.services.cost.controlmeasure.YearValidation;
import gov.epa.emissions.framework.services.data.EmfDateFormat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class EditableCaseSummaryTab extends JPanel implements EditableCaseSummaryTabView {

    public final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(EmfDateFormat.format());

    private Case caseObj;

    private ManageChangeables changeablesList;

    private TextField name;

    private TextField futureYear;

    private TextField template;

    private TextArea description;

    private EditableComboBox projectsCombo;

    private EmfSession session;

    private EditableComboBox modelToRunCombo;

    private EditableComboBox modRegionsCombo;

    private EditableComboBox controlRegionsCombo;

    private EditableComboBox abbreviationsCombo;

    private EditableComboBox airQualityModelsCombo;

    private EditableComboBox categoriesCombo;

    private EditableComboBox emissionsYearCombo;

    private EditableComboBox gridCombo;

    private EditableComboBox meteorlogicalYearCombo;

    private EditableComboBox speciationCombo;
    
    private EditableComboBox gridResolutionCombo;
    
    private CheckBox isFinal;

    private CheckBox isTemplate;

    private AddRemoveSectorWidget sectorsWidget;

    private Abbreviations abbreviations;

    private Projects projects;

    private AirQualityModels airQualityModels;

    private CaseCategories categories;

    private EmissionsYears emissionsYears;

    private Grids grids;

    private ModelToRuns models;

    private MeteorlogicalYears meteorlogicalYears;

    private Speciations speciations;

    private Regions modRegions;

    private Regions controlRegions;
    
    private GridResolutions gridResolutions;

    private RunStatuses runStatuses;

    private ComboBox runStatusCombo;

    private TextField startDate;

    private TextField endDate;

    private Dimension defaultDimension = new Dimension(200, 20);

    private EditCaseSummaryTabPresenter presenter;

    private EmfConsole parentConsole;

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
        // container.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
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
        layoutGenerator.addLabelWidgetPair("Description:", description(), panel);
        layoutGenerator.addLabelWidgetPair("Project:", projects(), panel);
        layoutGenerator.addLabelWidgetPair("Run Status:", runStatus(), panel);
        layoutGenerator.addLabelWidgetPair("Last Modified Date:", lastModifiedDate(), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 6, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JPanel createRightOverviewSection() throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Abbreviation:", abbreviations(), panel);
        JPanel finalTemplatePanel = new JPanel(new GridLayout(1,2));
        finalTemplatePanel.add(isFinal());
        finalTemplatePanel.add(isTemplate());
        layoutGenerator.addLabelWidgetPair("Is Final:", finalTemplatePanel, panel);
        //layoutGenerator.addLabelWidgetPair("Is Template:", isTemplate(), panel);
        layoutGenerator.addLabelWidgetPair("<html>Sectors:<br><br><br></html>", sectors(), panel);
        // layoutGenerator.addLabelWidgetPair("", addRemoveButtonPanel(), panel);
        layoutGenerator.addLabelWidgetPair("Copied From:", template(), panel);
        // layoutGenerator.addLabelWidgetPair("Last Modified Date:", lastModifiedDate(), panel);
        layoutGenerator.addLabelWidgetPair("Last Modified By:", creator(), panel);
 
        layoutGenerator.makeCompactGrid(panel, 5, 2, 10, 10, 10, 10);

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
        layoutGenerator.addLabelWidgetPair("I/O API Grid Name:", grids(), panel);
        layoutGenerator.addLabelWidgetPair("Grid Resolution:", gridResolution(), panel);
        layoutGenerator.addLabelWidgetPair("Start Date & Time:", startDate(), panel);

        layoutGenerator.makeCompactGrid(panel, 6, 2, 10, 10, 10, 10);

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

        layoutGenerator.makeCompactGrid(panel, 6, 2, 10, 10, 10, 10);

        return panel;
    }

    private JLabel lastModifiedDate() {
        return createLeftAlignedLabel(format(caseObj.getLastModifiedDate()));
    }

    private JLabel creator() {
        return createLeftAlignedLabel(caseObj.getLastModifiedBy().getName());
    }

    private ScrollableComponent description() {
        description = new TextArea("description", caseObj.getDescription(), 19, 3);
        changeablesList.addChangeable(description);
        //description.setPreferredSize(new Dimension(200, 60));

        ScrollableComponent descScrollableTextArea = new ScrollableComponent(description);
        descScrollableTextArea.setMinimumSize(defaultDimension);
        return descScrollableTextArea;
    }

    private TextField name() {
        name = new TextField("name", 20);
        name.setText(caseObj.getName());
        name.setMaximumSize(new Dimension(300, 15));
        changeablesList.addChangeable(name);

        return name;
    }

    private TextField futureYear() {
        futureYear = new TextField("Future Year", 10);
        futureYear.setText(caseObj.getFutureYear() + "");
        changeablesList.addChangeable(futureYear);
        futureYear.setPreferredSize(defaultDimension);

        return futureYear;
    }

    private TextField template() {
        template = new TextField("Template", 20);
        template.setText(caseObj.getTemplateUsed());
        template.setToolTipText(caseObj.getTemplateUsed());
        template.setEditable(false);
        template.setPreferredSize(defaultDimension);

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
        projects = new Projects(session.dataCommonsService().getProjects());
        projectsCombo = new EditableComboBox(projects.names());
        String name = caseObj.getProject() != null ? caseObj.getProject().getName() : "";
        projectsCombo.setSelectedItem(name);
        projectsCombo.setPreferredSize(defaultDimension);

        changeablesList.addChangeable(projectsCombo);

        return projectsCombo;
    }

    private EditableComboBox modelToRun() throws EmfException {
        models = new ModelToRuns(session, session.caseService().getModelToRuns());
        modelToRunCombo = new EditableComboBox(models.getAll());
        
        ModelToRun model = caseObj.getModel();
        modelToRunCombo.setSelectedItem(model);
        modelToRunCombo.setPreferredSize(defaultDimension);
        
        changeablesList.addChangeable(modelToRunCombo);
        
        return modelToRunCombo;
    }

    private EditableComboBox modRegions() throws EmfException {
        modRegions = new Regions(session.dataCommonsService().getRegions());
        modRegionsCombo = new EditableComboBox(modRegions.names());

        String name = caseObj.getModelingRegion() != null ? caseObj.getModelingRegion().getName() : "";
        modRegionsCombo.setSelectedItem(name);
        modRegionsCombo.setPreferredSize(defaultDimension);

        changeablesList.addChangeable(modRegionsCombo);

        return modRegionsCombo;
    }
    
    private EditableComboBox gridResolution() throws EmfException {
        gridResolutions = new GridResolutions(session, session.caseService().getGridResolutions());
        gridResolutionCombo = new EditableComboBox(gridResolutions.getAll());
        gridResolutionCombo.setSelectedItem(caseObj.getGridResolution());
        gridResolutionCombo.setPreferredSize(defaultDimension);
        changeablesList.addChangeable(gridResolutionCombo);
        
        return gridResolutionCombo;
    }

    private EditableComboBox controlRegions() throws EmfException {
        controlRegions = new Regions(session.dataCommonsService().getRegions());
        controlRegionsCombo = new EditableComboBox(controlRegions.names());

        String name = caseObj.getControlRegion() != null ? caseObj.getControlRegion().getName() : "";
        controlRegionsCombo.setSelectedItem(name);
        controlRegionsCombo.setPreferredSize(defaultDimension);

        changeablesList.addChangeable(controlRegionsCombo);

        return controlRegionsCombo;
    }

    private EditableComboBox abbreviations() throws EmfException {
        abbreviations = new Abbreviations(session.caseService().getAbbreviations());
        abbreviationsCombo = new EditableComboBox(abbreviations.names());

        String name = caseObj.getAbbreviation() != null ? caseObj.getAbbreviation().getName() : "";
        abbreviationsCombo.setSelectedItem(name);
        abbreviationsCombo.setPreferredSize(defaultDimension);

        changeablesList.addChangeable(abbreviationsCombo);

        return abbreviationsCombo;
    }

    private EditableComboBox airQualityModels() throws EmfException {
        airQualityModels = new AirQualityModels(session.caseService().getAirQualityModels());
        airQualityModelsCombo = new EditableComboBox(airQualityModels.names());

        String name = caseObj.getAirQualityModel() != null ? caseObj.getAirQualityModel().getName() : "";
        airQualityModelsCombo.setSelectedItem(name);
        airQualityModelsCombo.setPreferredSize(defaultDimension);

        changeablesList.addChangeable(airQualityModelsCombo);

        return airQualityModelsCombo;
    }

    private EditableComboBox categories() throws EmfException {
        categories = new CaseCategories(session.caseService().getCaseCategories());
        categoriesCombo = new EditableComboBox(categories.names());

        String name = caseObj.getCaseCategory() != null ? caseObj.getCaseCategory().getName() : "";
        categoriesCombo.setSelectedItem(name);
        categoriesCombo.setPreferredSize(defaultDimension);

        changeablesList.addChangeable(categoriesCombo);

        return categoriesCombo;
    }

    private JPanel sectors() {
        sectorsWidget = new AddRemoveSectorWidget(presenter.getAllSectors(), changeablesList, parentConsole);
        sectorsWidget.setSectors(caseObj.getSectors());
        sectorsWidget.setPreferredSize(new Dimension(220,80));
        return sectorsWidget;
    }

    private EditableComboBox emissionsYears() throws EmfException {
        emissionsYears = new EmissionsYears(session.caseService().getEmissionsYears());
        emissionsYearCombo = new EditableComboBox(emissionsYears.names());

        String name = caseObj.getEmissionsYear() != null ? caseObj.getEmissionsYear().getName() : "";
        emissionsYearCombo.setSelectedItem(name);
        emissionsYearCombo.setPreferredSize(defaultDimension);

        changeablesList.addChangeable(emissionsYearCombo);

        return emissionsYearCombo;
    }

    private EditableComboBox grids() throws EmfException {
        grids = new Grids(session.caseService().getGrids());
        gridCombo = new EditableComboBox(grids.names());

        String name = caseObj.getGrid() != null ? caseObj.getGrid().getName() : "";
        gridCombo.setSelectedItem(name);
        gridCombo.setPreferredSize(defaultDimension);

        changeablesList.addChangeable(gridCombo);

        return gridCombo;
    }

    private EditableComboBox meteorlogicalYears() throws EmfException {
        meteorlogicalYears = new MeteorlogicalYears(session.caseService().getMeteorlogicalYears());
        meteorlogicalYearCombo = new EditableComboBox(meteorlogicalYears.names());

        String name = caseObj.getMeteorlogicalYear() != null ? caseObj.getMeteorlogicalYear().getName() : "";
        meteorlogicalYearCombo.setSelectedItem(name);
        meteorlogicalYearCombo.setPreferredSize(defaultDimension);

        changeablesList.addChangeable(meteorlogicalYearCombo);

        return meteorlogicalYearCombo;
    }

    private EditableComboBox speciations() throws EmfException {
        speciations = new Speciations(session.caseService().getSpeciations());
        speciationCombo = new EditableComboBox(speciations.all());

        String name = caseObj.getSpeciation() != null ? caseObj.getSpeciation().getName() : "";
        speciationCombo.setSelectedItem(name);
        speciationCombo.setPreferredSize(defaultDimension);

        changeablesList.addChangeable(speciationCombo);

        return speciationCombo;
    }

    private ComboBox runStatus() {
        runStatusCombo = new ComboBox(runStatuses.all());
        runStatusCombo.setPreferredSize(defaultDimension);
        if (caseObj.getRunStatus() == null) {
            runStatusCombo.setSelectedIndex(0);
        }
        changeablesList.addChangeable(runStatusCombo);

        return runStatusCombo;
    }
    
    private TextField startDate() {
        startDate = new TextField("Start Date", 10);
        startDate.setText(format(caseObj.getStartDate()) + "");
        changeablesList.addChangeable(startDate);
        startDate.setPreferredSize(defaultDimension);

        return startDate;
    }

    private TextField endDate() {
        endDate = new TextField("End Date", 10);
        endDate.setText(format(caseObj.getEndDate()) + "");
        changeablesList.addChangeable(endDate);
        endDate.setPreferredSize(defaultDimension);

        return endDate;
    }

    private String format(Date date) {
        if (date == null)
            return "";
        return DATE_FORMATTER.format(date);
    }

    private JLabel createLeftAlignedLabel(String name) {
        JLabel label = new JLabel(name);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        return label;
    }

    // private JPanel addRemoveButtonPanel() {
    // JPanel panel = new JPanel();
    // // TBD: this needs to be changed so you have handles to the buttons
    // // and can set actions
    // Button addButton = new AddButton("Add", addAction());
    // Button removeButton = new RemoveButton("Remove", removeAction());
    //
    // panel.add(addButton);
    // panel.add(removeButton);
    // // for now, disable these
    //
    // return panel;
    // }
    //
    // private Action removeAction() {
    // return new AbstractAction() {
    // public void actionPerformed(ActionEvent e) {
    // removeSectors();
    // }
    // };
    // }
    //
    //    
    // private Action addAction() {
    // return new AbstractAction() {
    // public void actionPerformed(ActionEvent e) {
    // addSectors();
    // }
    // };
    // }
    //
    // private void addSectors() {
    // Sector[] allSectors = presenter.getAllSectors();
    // if (allSectors == null)
    // return;
    // SectorSelector sectorSelector = new SectorSelector(allSectors, sectorsWidget, parentConsole);
    // sectorSelector.display();
    // }
    //    
    // protected void removeSectors() {
    // Object[] removeValues = sectorsWidget.getSelectedValues();
    // sectorsWidget.removeElements(removeValues);
    //        
    // }

    public void save(Case caseObj) throws EmfException {
        caseObj.setName(name.getText());
        saveFutureYear();
        caseObj.setDescription(description.getText());
        caseObj.setCaseTemplate(isTemplate.isSelected());
        caseObj.setIsFinal(isFinal.isSelected());
        caseObj.setProject(projects.get((String) projectsCombo.getSelectedItem()));
        caseObj.setModelingRegion(modRegions.get((String) modRegionsCombo.getSelectedItem()));
        caseObj.setControlRegion(controlRegions.get((String) controlRegionsCombo.getSelectedItem()));
        caseObj.setAbbreviation(abbreviations.get((String) abbreviationsCombo.getSelectedItem()));
        caseObj.setAirQualityModel(airQualityModels.get((String) airQualityModelsCombo.getSelectedItem()));
        caseObj.setCaseCategory(categories.get((String) categoriesCombo.getSelectedItem()));
        caseObj.setEmissionsYear(emissionsYears.get((String) emissionsYearCombo.getSelectedItem()));
        caseObj.setGrid(grids.get((String) gridCombo.getSelectedItem()));
        caseObj.setMeteorlogicalYear(meteorlogicalYears.get((String) meteorlogicalYearCombo.getSelectedItem()));
        caseObj.setSpeciation(speciations.get((String) speciationCombo.getSelectedItem()));
        caseObj.setRunStatus(runStatusCombo.getSelectedItem() + "");
        saveStartDate();
        saveEndDate();
        caseObj.setSectors(sectorsWidget.getSectors());
        caseObj.setModel(models.get(modelToRunCombo.getSelectedItem()));
        caseObj.setGridResolution(gridResolutions.get(gridResolutionCombo.getSelectedItem()));
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
            caseObj.setStartDate(DATE_FORMATTER.parse(startDate.getText()));
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
            caseObj.setEndDate(DATE_FORMATTER.parse(endDate.getText()));
        } catch (ParseException e) {
            throw new EmfException("Please enter the End Date in the correct format (MM/dd/yyyy HH:mm)");
        }
    }

    public void observe(EditCaseSummaryTabPresenter presenter) {
        this.presenter = presenter;
    }

}
