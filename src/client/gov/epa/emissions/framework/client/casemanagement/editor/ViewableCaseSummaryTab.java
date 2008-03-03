package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.commons.gui.CheckBox;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.EditableComboBox;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.casemanagement.AirQualityModels;
import gov.epa.emissions.framework.client.casemanagement.EmissionsYears;
import gov.epa.emissions.framework.client.casemanagement.Grids;
import gov.epa.emissions.framework.client.casemanagement.MeteorlogicalYears;
import gov.epa.emissions.framework.client.casemanagement.RunStatuses;
import gov.epa.emissions.framework.client.casemanagement.Speciations;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.ui.RefreshObserver;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class ViewableCaseSummaryTab extends JPanel implements RefreshObserver {

    private Case caseObj;

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

    private EditableComboBox categoriesCombo;

    private EditableComboBox emissionsYearCombo;

    private EditableComboBox gridCombo;

    private EditableComboBox meteorlogicalYearCombo;

    private EditableComboBox speciationCombo;
    
    private EditableComboBox gridResolutionCombo;
    
    private CheckBox isFinal;

    private CheckBox isTemplate;

    private AddRemoveSectorWidget sectorsWidget;

    private AirQualityModels airQualityModels;

    private EmissionsYears emissionsYears;

    private Grids grids;

    private MeteorlogicalYears meteorlogicalYears;

    private Speciations speciations;

    private GridResolutions gridResolutions;

    private ComboBox runStatusCombo;

    private TextField startDate;

    private TextField endDate;

    private Dimension defaultDimension = new Dimension(200, 20);
    
    private EditCaseSummaryTabPresenter presenter;

//    private EmfConsole parentConsole;

    public ViewableCaseSummaryTab(Case caseObj, EmfSession session,
            EmfConsole parentConsole) {
        super.setName("summary");
        this.caseObj = caseObj;
        this.session = session;
//        this.parentConsole = parentConsole;

    }

    public void display() throws EmfException {
        setLayout();
        viewOnly();
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
        //description.setPreferredSize(new Dimension(200, 60));
        description.setEditable(false);
        ScrollableComponent descScrollableTextArea = new ScrollableComponent(description);
        descScrollableTextArea.setMinimumSize(defaultDimension);
        return descScrollableTextArea;
    }

    private TextField name() {
        name = new TextField("name", 20);
        name.setText(caseObj.getName());
        name.setMaximumSize(new Dimension(300, 15));
        return name;
    }
    
    private TextField futureYear() {
        futureYear = new TextField("Future Year", 10);
        futureYear.setText(caseObj.getFutureYear() + "");
        futureYear.setPreferredSize(defaultDimension);
        return futureYear;
    }

    private TextField template() {
        template = new TextField("Template", 20);
        template.setText(caseObj.getTemplateUsed());
        template.setToolTipText(caseObj.getTemplateUsed());
        template.setEditable(false);
        template.setPreferredSize(defaultDimension);
        template.setEditable(false);
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
//        addPopupMenuListener(projectsCombo, "projects");
        projectsCombo.setEnabled(false);
        return projectsCombo;
    }

    private EditableComboBox modelToRun() throws EmfException {
        modelToRunCombo = new EditableComboBox(presenter.getModelToRuns());
        modelToRunCombo.setSelectedItem(caseObj.getModel());
        modelToRunCombo.setPreferredSize(defaultDimension);
//        addPopupMenuListener(modelToRunCombo, "modeltoruns");
        modelToRunCombo.setEnabled(false);
        return modelToRunCombo;
    }

    private ComboBox modRegions() throws EmfException {
        modRegionsCombo = new ComboBox(presenter.getRegions());
        modRegionsCombo.setSelectedItem(caseObj.getModelingRegion());
        modRegionsCombo.setPreferredSize(defaultDimension);
        modRegionsCombo.setEnabled(false);
        return modRegionsCombo;
    }
    
    private EditableComboBox gridResolution() throws EmfException {
        gridResolutions = new GridResolutions(session, session.caseService().getGridResolutions());
        gridResolutionCombo = new EditableComboBox(gridResolutions.getAll());
        gridResolutionCombo.setSelectedItem(caseObj.getGridResolution());
        gridResolutionCombo.setPreferredSize(defaultDimension);
        
        return gridResolutionCombo;
    }

    private ComboBox controlRegions() throws EmfException {
        controlRegionsCombo = new ComboBox(presenter.getRegions());
        controlRegionsCombo.setSelectedItem(caseObj.getControlRegion());
        controlRegionsCombo.setPreferredSize(defaultDimension);

        return controlRegionsCombo;
    }

    private EditableComboBox abbreviations() throws EmfException {
        abbreviationsCombo = new EditableComboBox(presenter.getAbbreviations());
        abbreviationsCombo.setSelectedItem(caseObj.getAbbreviation());
        abbreviationsCombo.setPreferredSize(defaultDimension);
//        addPopupMenuListener(abbreviationsCombo, "abbreviations");

        return abbreviationsCombo;
    }

    private EditableComboBox airQualityModels() throws EmfException {
        airQualityModels = new AirQualityModels(session.caseService().getAirQualityModels());
        airQualityModelsCombo = new EditableComboBox(airQualityModels.names());

        String name = caseObj.getAirQualityModel() != null ? caseObj.getAirQualityModel().getName() : "";
        airQualityModelsCombo.setSelectedItem(name);
        airQualityModelsCombo.setPreferredSize(defaultDimension);

        return airQualityModelsCombo;
    }

    private EditableComboBox categories() throws EmfException {
        categoriesCombo = new EditableComboBox(presenter.getCaseCategories());
        categoriesCombo.setSelectedItem(caseObj.getCaseCategory());
        categoriesCombo.setPreferredSize(defaultDimension);
//        addPopupMenuListener(categoriesCombo, "categories");

        return categoriesCombo;
    }

    private JPanel sectors() throws EmfException {
        sectorsWidget = new AddRemoveSectorWidget(presenter.getAllSectors());
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

        return emissionsYearCombo;
    }

    private EditableComboBox grids() throws EmfException {
        grids = new Grids(session.caseService().getGrids());
        gridCombo = new EditableComboBox(grids.names());

        String name = caseObj.getGrid() != null ? caseObj.getGrid().getName() : "";
        gridCombo.setSelectedItem(name);
        gridCombo.setPreferredSize(defaultDimension);

        return gridCombo;
    }

    private EditableComboBox meteorlogicalYears() throws EmfException {
        meteorlogicalYears = new MeteorlogicalYears(session.caseService().getMeteorlogicalYears());
        meteorlogicalYearCombo = new EditableComboBox(meteorlogicalYears.names());

        String name = caseObj.getMeteorlogicalYear() != null ? caseObj.getMeteorlogicalYear().getName() : "";
        meteorlogicalYearCombo.setSelectedItem(name);
        meteorlogicalYearCombo.setPreferredSize(defaultDimension);

        return meteorlogicalYearCombo;
    }

    private EditableComboBox speciations() throws EmfException {
        speciations = new Speciations(session.caseService().getSpeciations());
        speciationCombo = new EditableComboBox(speciations.all());

        String name = caseObj.getSpeciation() != null ? caseObj.getSpeciation().getName() : "";
        speciationCombo.setSelectedItem(name);
        speciationCombo.setPreferredSize(defaultDimension);

        return speciationCombo;
    }

    private ComboBox runStatus() {
        runStatusCombo = new ComboBox(RunStatuses.all());
        runStatusCombo.setPreferredSize(defaultDimension);
        if (caseObj.getRunStatus() == null) {
            runStatusCombo.setSelectedIndex(0);
        }
        else
        {
            runStatusCombo.setSelectedItem(caseObj.getRunStatus());
        }

        return runStatusCombo;
    }
    
    private TextField startDate() {
        startDate = new TextField("Start Date", 10);
        startDate.setText(format(caseObj.getStartDate()) + "");
        startDate.setPreferredSize(defaultDimension);
        startDate.setToolTipText("Date in format MM/dd/yyyy HH:mm");

        return startDate;
    }

    private TextField endDate() {
        endDate = new TextField("End Date", 10);
        endDate.setText(format(caseObj.getEndDate()) + "");
        endDate.setPreferredSize(defaultDimension);
        endDate.setToolTipText("Date in format MM/dd/yyyy HH:mm");

        return endDate;
    }

//    private void addPopupMenuListener(final JComboBox box, final String toget) {
//        box.addPopupMenuListener(new PopupMenuListener() {
//            public void popupMenuCanceled(PopupMenuEvent event) {
//                // NOTE Auto-generated method stub
//            }
//
//            public void popupMenuWillBecomeInvisible(PopupMenuEvent event) {
//                // NOTE Auto-generated method stub
//            }
//
//            public void popupMenuWillBecomeVisible(PopupMenuEvent event) {
//                try {
//                    Object selected = box.getSelectedItem();
//                    box.setModel(new DefaultComboBoxModel(getAllObjects(toget)));
//                    box.setSelectedItem(selected);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    //messagePanel.setError(e.getMessage());
//                }
//            }
//        });
//    }

    protected Object[] getAllObjects(String toget) throws EmfException {
        if (toget.equals("categories"))
            return presenter.getCaseCategories();

        else if (toget.equals("abbreviations"))
            return presenter.getAbbreviations();

        else if (toget.equals("projects"))
            return presenter.getProjects();

        else if (toget.equals("modeltoruns"))
            return presenter.getModelToRuns();

//        else if (toget.equals("sectors"))
//            return presenter.getSectors();
//
//        else if (toget.equals("subdirs"))
//            return presenter.getSubdirs();
//
//        else
//            throw new EmfException("Unknown object type: " + toget);
        
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


    public void observe(EditCaseSummaryTabPresenter presenter) {
        this.presenter = presenter;
    }

    public void doRefresh() throws EmfException {
        if (false)
            throw new EmfException("No need to refresh when the case object is locked.");
    }
    
    private void viewOnly(){
        name.setEditable(false);
        futureYear.setEditable(false);
        template.setEditable(false);
        description.setEditable(false);
        projectsCombo.setEnabled(false);
        modelToRunCombo.setEnabled(false);
        modRegionsCombo.setEnabled(false);
        controlRegionsCombo.setEnabled(false);
        abbreviationsCombo.setEnabled(false);
        airQualityModelsCombo.setEnabled(false);
        categoriesCombo.setEnabled(false);
        emissionsYearCombo.setEnabled(false);
        gridCombo.setEnabled(false);
        meteorlogicalYearCombo.setEnabled(false);
        speciationCombo.setEnabled(false);
        gridResolutionCombo.setEnabled(false);
        //isFinal;
        //isTemplate;
        //sectorsWidget.viewOnly();
        //airQualityModels;
        //emissionsYears;
        //grids;
        //meteorlogicalYears;
        //speciations;
        //gridResolutions;
        runStatusCombo.setEditable(false);
        startDate.setEditable(false);
        endDate.setEditable(false);
    }


}
