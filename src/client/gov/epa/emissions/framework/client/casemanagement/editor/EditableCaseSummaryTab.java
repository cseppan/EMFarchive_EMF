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
import gov.epa.emissions.framework.services.casemanagement.GridResolution;
import gov.epa.emissions.framework.services.casemanagement.ModelToRun;
import gov.epa.emissions.framework.services.cost.controlmeasure.YearValidation;
import gov.epa.emissions.framework.services.data.EmfDateFormat;
import gov.epa.emissions.framework.ui.MessagePanel;
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

    private MessagePanel messagePanel;

    public EditableCaseSummaryTab(Case caseObj, MessagePanel messagePanel, EmfSession session,
            ManageChangeables changeablesList, EmfConsole parentConsole) {
        super.setName("summary");
        this.caseObj = caseObj;
        this.session = session;
        this.changeablesList = changeablesList;
        this.parentConsole = parentConsole;
        this.messagePanel = messagePanel;
    }

    public void display() {
        setLayout();
    }

    private void setLayout() {
        super.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(createOverviewSection());
        panel.add(createLowerSection());

        super.add(panel, BorderLayout.CENTER);
    }

    private JPanel createOverviewSection() {
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

    private JPanel createLeftOverviewSection() {
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

    private JPanel createRightOverviewSection() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Abbreviation:", abbreviations(), panel);
        JPanel finalTemplatePanel = new JPanel(new GridLayout(1, 2));
        finalTemplatePanel.add(isFinal());
        finalTemplatePanel.add(isTemplate());
        layoutGenerator.addLabelWidgetPair("Is Final:", finalTemplatePanel, panel);
        // layoutGenerator.addLabelWidgetPair("Is Template:", isTemplate(), panel);
        layoutGenerator.addLabelWidgetPair("<html>Sectors:<br><br><br></html>", sectors(), panel);
        // layoutGenerator.addLabelWidgetPair("", addRemoveButtonPanel(), panel);
        layoutGenerator.addLabelWidgetPair("Copied From:", template(), panel);
        // layoutGenerator.addLabelWidgetPair("Last Modified Date:", lastModifiedDate(), panel);
        layoutGenerator.addLabelWidgetPair("Last Modified By:", creator(), panel);

        layoutGenerator.makeCompactGrid(panel, 5, 2, 10, 10, 10, 10);

        return panel;
    }

    private JPanel createLowerSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));

        JPanel container = new JPanel();
        container.setLayout(new GridLayout(1, 2));
        container.add(createLowerLeftSection());
        container.add(createLowerRightSection());

        panel.add(container, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLowerLeftSection() {
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

    private JPanel createLowerRightSection() {
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
        // description.setPreferredSize(new Dimension(200, 60));

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

    private EditableComboBox projects() {
        String name = caseObj.getProject() != null ? caseObj.getProject().getName() : "";
        projectsCombo = new EditableComboBox(new String[] { name });
        projectsCombo.setPreferredSize(defaultDimension);
        addPopupMenuListener(projectsCombo, "projects");
        changeablesList.addChangeable(projectsCombo);

        return projectsCombo;
    }

    private EditableComboBox modelToRun() {
        ModelToRun model = caseObj.getModel();
        modelToRunCombo = new EditableComboBox(new ModelToRun[] { model });
        modelToRunCombo.setPreferredSize(defaultDimension);
        addPopupMenuListener(modelToRunCombo, "runmodels");
        changeablesList.addChangeable(modelToRunCombo);

        return modelToRunCombo;
    }

    private ComboBox modRegions() {
        String name = caseObj.getModelingRegion() != null ? caseObj.getModelingRegion().getName() : "";
        modRegionsCombo = new ComboBox(new String[] { name });
        modRegionsCombo.setPreferredSize(defaultDimension);
        addPopupMenuListener(modRegionsCombo, "modregions");
        changeablesList.addChangeable(modRegionsCombo);

        return modRegionsCombo;
    }

    private EditableComboBox gridResolution() {
        gridResolutionCombo = new EditableComboBox(new GridResolution[] { caseObj.getGridResolution() });
        gridResolutionCombo.setPreferredSize(defaultDimension);
        addPopupMenuListener(gridResolutionCombo, "gridreslns");
        changeablesList.addChangeable(gridResolutionCombo);

        return gridResolutionCombo;
    }

    private ComboBox controlRegions() {
        String name = caseObj.getControlRegion() != null ? caseObj.getControlRegion().getName() : "";
        controlRegionsCombo = new ComboBox(new String[] { name });
        controlRegionsCombo.setPreferredSize(defaultDimension);
        addPopupMenuListener(controlRegionsCombo, "contrlregions");
        changeablesList.addChangeable(controlRegionsCombo);

        return controlRegionsCombo;
    }

    private EditableComboBox abbreviations() {
        String name = caseObj.getAbbreviation() != null ? caseObj.getAbbreviation().getName() : "";
        abbreviationsCombo = new EditableComboBox(new String[] { name });
        abbreviationsCombo.setPreferredSize(defaultDimension);
        addPopupMenuListener(abbreviationsCombo, "abbrs");
        changeablesList.addChangeable(abbreviationsCombo);

        return abbreviationsCombo;
    }

    private EditableComboBox airQualityModels() {
        String name = caseObj.getAirQualityModel() != null ? caseObj.getAirQualityModel().getName() : "";
        airQualityModelsCombo = new EditableComboBox(new String[] { name });
        airQualityModelsCombo.setPreferredSize(defaultDimension);
        addPopupMenuListener(airQualityModelsCombo, "aqmodels");
        changeablesList.addChangeable(airQualityModelsCombo);

        return airQualityModelsCombo;
    }

    private EditableComboBox categories() {
        String name = caseObj.getCaseCategory() != null ? caseObj.getCaseCategory().getName() : "";
        categoriesCombo = new EditableComboBox(new String[] { name });
        categoriesCombo.setPreferredSize(defaultDimension);
        addPopupMenuListener(categoriesCombo, "categories");
        changeablesList.addChangeable(categoriesCombo);

        return categoriesCombo;
    }

    private JPanel sectors() {
        try {
            sectorsWidget = new AddRemoveSectorWidget(presenter.getAllSectors(), changeablesList, parentConsole);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
        sectorsWidget.setSectors(caseObj.getSectors());
        sectorsWidget.setPreferredSize(new Dimension(220, 80));
        return sectorsWidget;
    }

    private EditableComboBox emissionsYears() {
        String name = caseObj.getEmissionsYear() != null ? caseObj.getEmissionsYear().getName() : "";
        emissionsYearCombo = new EditableComboBox(new String[] { name });
        emissionsYearCombo.setPreferredSize(defaultDimension);
        addPopupMenuListener(emissionsYearCombo, "emisyears");
        changeablesList.addChangeable(emissionsYearCombo);

        return emissionsYearCombo;
    }

    private EditableComboBox grids() {
        String name = caseObj.getGrid() != null ? caseObj.getGrid().getName() : "";
        gridCombo = new EditableComboBox(new String[] { name });
        gridCombo.setPreferredSize(defaultDimension);
        addPopupMenuListener(gridCombo, "grids");
        changeablesList.addChangeable(gridCombo);

        return gridCombo;
    }

    private EditableComboBox meteorlogicalYears() {
        String name = caseObj.getMeteorlogicalYear() != null ? caseObj.getMeteorlogicalYear().getName() : "";
        meteorlogicalYearCombo = new EditableComboBox(new String[] { name });
        meteorlogicalYearCombo.setPreferredSize(defaultDimension);
        addPopupMenuListener(meteorlogicalYearCombo, "meteoyears");
        changeablesList.addChangeable(meteorlogicalYearCombo);

        return meteorlogicalYearCombo;
    }

    private EditableComboBox speciations() {
        String name = caseObj.getSpeciation() != null ? caseObj.getSpeciation().getName() : "";
        speciationCombo = new EditableComboBox(new String[] { name });
        speciationCombo.setPreferredSize(defaultDimension);
        addPopupMenuListener(speciationCombo, "speciations");

        changeablesList.addChangeable(speciationCombo);

        return speciationCombo;
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
                    box.setModel(new DefaultComboBoxModel(getAllObjects(toget)));
                    box.revalidate();
                    refresh();
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        });
    }

    protected Object[] getAllObjects(String toget) throws EmfException {
        if (toget.equals("projects"))
            return new Projects(session.dataCommonsService().getProjects()).names();

        if (toget.equals("runmodels"))
            return new ModelToRuns(session, session.caseService().getModelToRuns()).getAll();

        if (toget.equals("modregions"))
            return new Regions(session.dataCommonsService().getRegions()).names();

        if (toget.equals("gridreslns"))
            return new GridResolutions(session, session.caseService().getGridResolutions()).getAll();

        if (toget.equals("contrlregions"))
            return new Regions(session.dataCommonsService().getRegions()).names();

        if (toget.equals("abbrs"))
            return new Abbreviations(session.caseService().getAbbreviations()).names();

        if (toget.equals("aqmodels"))
            return new AirQualityModels(session.caseService().getAirQualityModels()).names();

        if (toget.equals("categories"))
            return new CaseCategories(session.caseService().getCaseCategories()).names();

        if (toget.equals("emisyears"))
            return new EmissionsYears(session.caseService().getEmissionsYears()).names();

        if (toget.equals("grids"))
            return new Grids(session.caseService().getGrids()).names();

        if (toget.equals("meteoyears"))
            return new MeteorlogicalYears(session.caseService().getMeteorlogicalYears()).names();

        if (toget.equals("speciations"))
            return new Speciations(session.caseService().getSpeciations()).all();

        return null;
    }

    private ComboBox runStatus() {
        runStatusCombo = new ComboBox(runStatuses.all());
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
        return EmfDateFormat.format_MM_DD_YYYY_HH_mm(date);
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
        if (projects != null)
            caseObj.setProject(projects.get((String) projectsCombo.getSelectedItem()));

        if (modRegions != null)
            caseObj.setModelingRegion(modRegions.get((String) modRegionsCombo.getSelectedItem()));

        if (controlRegions != null)
            caseObj.setControlRegion(controlRegions.get((String) controlRegionsCombo.getSelectedItem()));

        if (abbreviations != null)
            caseObj.setAbbreviation(abbreviations.get((String) abbreviationsCombo.getSelectedItem()));

        if (airQualityModels != null)
            caseObj.setAirQualityModel(airQualityModels.get((String) airQualityModelsCombo.getSelectedItem()));

        if (categories != null)
            caseObj.setCaseCategory(categories.get((String) categoriesCombo.getSelectedItem()));

        if (emissionsYears != null)
            caseObj.setEmissionsYear(emissionsYears.get((String) emissionsYearCombo.getSelectedItem()));

        if (grids != null)
            caseObj.setGrid(grids.get((String) gridCombo.getSelectedItem()));

        if (meteorlogicalYears != null)
            caseObj.setMeteorlogicalYear(meteorlogicalYears.get((String) meteorlogicalYearCombo.getSelectedItem()));

        if (speciations != null)
            caseObj.setSpeciation(speciations.get((String) speciationCombo.getSelectedItem()));

        caseObj.setRunStatus(runStatusCombo.getSelectedItem() + "");
        saveStartDate();
        saveEndDate();
        caseObj.setSectors(sectorsWidget.getSectors());

        if (models != null)
            caseObj.setModel(models.get(modelToRunCombo.getSelectedItem()));

        if (gridResolutions != null)
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
            caseObj.setStartDate(EmfDateFormat.parse_YYYY_MM_DD_HH_MM(startDate.getText()));
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
            caseObj.setEndDate(EmfDateFormat.parse_YYYY_MM_DD_HH_MM(endDate.getText()));
        } catch (ParseException e) {
            throw new EmfException("Please enter the End Date in the correct format (MM/dd/yyyy HH:mm)");
        }
    }

    public void observe(EditCaseSummaryTabPresenter presenter) {
        this.presenter = presenter;
    }

    private void refresh() {
        super.revalidate();
    }

    public void doRefresh() throws EmfException {
        if (false)
            throw new EmfException("Nothing to update since case object is locked.");
    }

}
