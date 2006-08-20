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
import gov.epa.emissions.framework.client.data.EmfDateFormat;
import gov.epa.emissions.framework.client.data.Projects;
import gov.epa.emissions.framework.client.data.Regions;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.ui.ListWidget;
import gov.epa.emissions.framework.ui.NumberFieldVerifier;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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

    private EditableComboBox modRegionsCombo;
    
    private EditableComboBox controlRegionsCombo;

    private EditableComboBox abbreviationsCombo;

    private EditableComboBox airQualityModelsCombo;

    private EditableComboBox categoriesCombo;

    private EditableComboBox emissionsYearCombo;

    private EditableComboBox gridCombo;

    private EditableComboBox meteorlogicalYearCombo;

    private EditableComboBox speciationCombo;

    private CheckBox isFinal;

    private CheckBox isTemplate;

    private ListWidget sectorsList;

    private Abbreviations abbreviations;

    private Projects projects;

    private AirQualityModels airQualityModels;

    private CaseCategories categories;

    private EmissionsYears emissionsYears;

    private Grids grids;

    private MeteorlogicalYears meteorlogicalYears;

    private Speciations speciations;

    private Regions modRegions;
    
    private Regions controlRegions;

    private RunStatuses runStatuses;

    private ComboBox runStatusCombo;
    
    private TextField startDate;
    
    private TextField endDate;
    
    private NumberFieldVerifier verifier;
    
    private Dimension defaultDimension = new Dimension(200,20);

    public EditableCaseSummaryTab(Case caseObj, EmfSession session, ManageChangeables changeablesList)
            throws EmfException {
        super.setName("summary");
        this.caseObj = caseObj;
        this.session = session;
        this.changeablesList = changeablesList;
        this.verifier = new NumberFieldVerifier("");

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
        container.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        container.add(createLeftOverviewSection());
        container.add(createRightOverviewSection());

        panel.add(container, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLeftOverviewSection() throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Name:", name(), panel);
        layoutGenerator.addLabelWidgetPair("Project:", projects(), panel);
        layoutGenerator.addLabelWidgetPair("Category:", categories(), panel);
        layoutGenerator.addLabelWidgetPair("Description:", new ScrollableComponent(description()), panel);
        layoutGenerator.addLabelWidgetPair("Run Status:", runStatus(), panel);
        layoutGenerator.addLabelWidgetPair("Last Modified By:", creator(), panel);
        layoutGenerator.addLabelWidgetPair("Last Modified Date:", lastModifiedDate(), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 7, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JPanel createRightOverviewSection() throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Abbreviation:", abbreviations(), panel);
        layoutGenerator.addLabelWidgetPair("Is Final:", isFinal(), panel);
        layoutGenerator.addLabelWidgetPair("Is Template:", isTemplate(), panel);
        layoutGenerator.addLabelWidgetPair("Sectors:", sectors(), panel);
        layoutGenerator.addLabelWidgetPair("", addRemoveButtonPanel(), panel);
        layoutGenerator.addLabelWidgetPair("Template", template(), panel);
//       layoutGenerator.addLabelWidgetPair("Last Modified Date:", lastModifiedDate(), panel);

        layoutGenerator.makeCompactGrid(panel, 6, 2, 10, 10, 10, 10);

        return panel;
    }

    private JPanel createLowerSection() throws EmfException {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));

        JPanel container = new JPanel();
        container.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        container.add(createLowerLeftSection());
        container.add(createLowerRightSection());

        panel.add(container, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLowerLeftSection() throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Modeling Region:", modRegions(), panel);
        layoutGenerator.addLabelWidgetPair("Control Region:", controlRegions(), panel);
        layoutGenerator.addLabelWidgetPair("I/O API Grid Name:", grids(), panel);
        layoutGenerator.addLabelWidgetPair("Start Date:", startDate(), panel);
        
        layoutGenerator.makeCompactGrid(panel, 4, 2, 5, 5, 10, 10);

        return panel;
    }

    private JPanel createLowerRightSection() throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Air Quality Model:", airQualityModels(), panel);
        layoutGenerator.addLabelWidgetPair("Speciation:", speciations(), panel);
        layoutGenerator.addLabelWidgetPair("Emissions Year:", emissionsYears(), panel);
        layoutGenerator.addLabelWidgetPair("Future Year:", futureYear(), panel);
        layoutGenerator.addLabelWidgetPair("Meteorological Year:", meteorlogicalYears(), panel);
        layoutGenerator.addLabelWidgetPair("End Date/Time", endDate(), panel);
        
        layoutGenerator.makeCompactGrid(panel, 6, 2, 5, 5, 10, 10);

        return panel;
    }

    private JLabel lastModifiedDate() {
        return createLeftAlignedLabel(format(caseObj.getLastModifiedDate()));
    }

    private JLabel creator() {
        return createLeftAlignedLabel(caseObj.getLastModifiedBy().getName());
    }

    private TextArea description() {
        description = new TextArea("description", caseObj.getDescription(), 12, 3);
        changeablesList.addChangeable(description);
        description.setPreferredSize(new Dimension(200,60));

        return description;
    }

    private TextField name() {
        name = new TextField("name", 10);
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
        template = new TextField("Template", 10);
        template.setText(caseObj.getTemplateUsed());
        template.setEditable(false);
        template.setPreferredSize(defaultDimension);
       
        return template;
    }

    private JComponent isTemplate() {
        isTemplate = new CheckBox("");
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

    private EditableComboBox modRegions() throws EmfException {
        modRegions = new Regions(session.dataCommonsService().getRegions());
        modRegionsCombo = new EditableComboBox(modRegions.names());

        String name = caseObj.getModelingRegion() != null ? caseObj.getModelingRegion().getName() : "";
        modRegionsCombo.setSelectedItem(name);
        modRegionsCombo.setPreferredSize(defaultDimension);

        changeablesList.addChangeable(modRegionsCombo);

        return modRegionsCombo;
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

    private JScrollPane sectors() 
    {
        sectorsList = new ListWidget(new String[] { "               " }, new String[] { "" });
        JScrollPane listScroller = new JScrollPane(sectorsList);
        listScroller.setPreferredSize(new Dimension(175, 60));

        return listScroller;
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
        runStatuses = new RunStatuses();
        //String name = caseObj.getRunStatus() != null ? caseObj.getRunStatus() : "";
        //runStatusCombo = new ComboBox(name, runStatuses.all());
        runStatusCombo = new ComboBox(runStatuses.all());
        runStatusCombo.setPreferredSize(defaultDimension);
        if (caseObj.getRunStatus() == null)
        {
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

    public void save(Case caseObj) throws EmfException {
        caseObj.setName(name.getText());
        caseObj.setFutureYear(verifier.parseInteger(futureYear));
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
//        caseObj.setStartDate(DATE_FORMATTER.parse(startDate.getText()));
//        caseObj.setEndDate(DATE_FORMATTER.parse(endDate.getText()));
    }

}
