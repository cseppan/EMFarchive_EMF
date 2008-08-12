package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.gui.CheckBox;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.casemanagement.RunStatuses;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.ModelToRun;
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

public class ViewableCaseSummaryTab extends JPanel implements RefreshObserver {

    private Case caseObj;

    private TextField name;

    private TextField futureYear;

    private TextField template;

    private TextArea description;

    private ComboBox projectsCombo;

    private EmfSession session;

    private ComboBox modelToRunCombo;

    private ComboBox modRegionsCombo;

    private ComboBox controlRegionsCombo;

    private ComboBox abbreviationsCombo;

    private ComboBox airQualityModelsCombo;

    private ComboBox categoriesCombo;

    private ComboBox emissionsYearCombo;

    private ComboBox gridCombo;

    private ComboBox meteorlogicalYearCombo;

    private ComboBox speciationCombo;
    
    private ComboBox gridResolutionCombo;
    
    private CheckBox isFinal;

    private CheckBox isTemplate;

    private AddRemoveSectorWidget sectorsWidget;

    private ComboBox runStatusCombo;

    private TextField startDate;

    private TextField endDate;

    private Dimension defaultDimension = new Dimension(255, 22);

    private int fieldWidth=23;
    
    private EditCaseSummaryTabPresenter presenter;

    private TextField modelVersionField;
    
    private TextField numMetLayers, numEmissionLayers;

    public ViewableCaseSummaryTab(Case caseObj, EmfSession session,
            EmfConsole parentConsole) {
        super.setName("viewSummary");
        this.caseObj = caseObj;
        this.session = session;
    }

    public void display() throws EmfException {
        setLayout();
        viewOnly();
    }

    private void viewOnly(){
        name.setEditable(false);
        futureYear.setEditable(false);
        template.setEditable(false);
        description.setEditable(false);
        isFinal.setEnabled(false);
        isTemplate.setEnabled(false);
        startDate.setEditable(false);
        endDate.setEditable(false);
        numMetLayers.setEditable(false);
        numEmissionLayers.setEditable(false);
        description.setEditable(false);
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

        layoutGenerator.addLabelWidgetPair("Model & Version:", modelToRun(), panel);
        layoutGenerator.addLabelWidgetPair("Modeling Region:", modRegions(), panel);
        //layoutGenerator.addLabelWidgetPair("Control Region:", controlRegions(), panel);
        layoutGenerator.addLabelWidgetPair("Grid Name:", grids(), panel);
        layoutGenerator.addLabelWidgetPair("Grid Resolution:", gridResolution(), panel);
        layoutGenerator.addLabelWidgetPair("Met/Emis Layers:", metEmisLayers(), panel);
        layoutGenerator.addLabelWidgetPair("Start Date & Time: ", startDate(), panel);

        layoutGenerator.makeCompactGrid(panel, 6, 2, 10, 10, 5, 10);

        return panel;
    }

    private JPanel createLowerRightSection() throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Downstream Model:", airQualityModels(), panel);
        layoutGenerator.addLabelWidgetPair("Speciation:", speciations(), panel);
        layoutGenerator.addLabelWidgetPair("Meteorological Year:", meteorlogicalYears(), panel);
        layoutGenerator.addLabelWidgetPair("Base Year:", emissionsYears(), panel);
        layoutGenerator.addLabelWidgetPair("Future Year:", futureYear(), panel);
        layoutGenerator.addLabelWidgetPair("End Date & Time:", endDate(), panel);

        layoutGenerator.makeCompactGrid(panel, 6, 2, 10, 10, 5, 10);

        return panel;
    }

    private JLabel creator() {
        return createLeftAlignedLabel(caseObj.getLastModifiedBy().getName()+ " on "
                +format(caseObj.getLastModifiedDate()));
    }

    private ScrollableComponent description() {
        description = new TextArea("description", caseObj.getDescription(), fieldWidth, 3);
 
        ScrollableComponent descScrollableTextArea = new ScrollableComponent(description);
        descScrollableTextArea.setPreferredSize(new Dimension(255,80));
        return descScrollableTextArea;
    }

    private TextField name() {
        name = new TextField("name", this.fieldWidth);
        name.setText(caseObj.getName());
        name.setPreferredSize(defaultDimension);
        name.setToolTipText(caseObj.getName());
        name.setEditable(false);

        return name;
    }
    

    private TextField futureYear() {
        futureYear = new TextField("Future Year", fieldWidth);
        futureYear.setText(caseObj.getFutureYear() + "");
        futureYear.setPreferredSize(defaultDimension);
        futureYear.setEditable(false);

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

    private ComboBox projects() throws EmfException {
        projectsCombo = new ComboBox(presenter.getProjects());
        projectsCombo.setSelectedItem(caseObj.getProject());
        projectsCombo.setPreferredSize(defaultDimension);
        addPopupMenuListener(projectsCombo, "projects");

        return projectsCombo;
    }

    private JPanel metEmisLayers() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        numMetLayers = new TextField("Num Met Layers", fieldWidth / 2);
        numEmissionLayers = new TextField("Num Emis Layers", fieldWidth / 2);

        numMetLayers.setText(caseObj.getNumMetLayers() != null ? caseObj.getNumMetLayers() + "" : "");
        numMetLayers.setToolTipText("Enter # of met layers");
        numMetLayers.setPreferredSize(new Dimension(122, 22));

        numEmissionLayers.setText(caseObj.getNumEmissionsLayers() != null ? caseObj.getNumEmissionsLayers() + "" : "");
        numEmissionLayers.setToolTipText("Enter # of emission layers");
        numEmissionLayers.setPreferredSize(new Dimension(122, 22));

        panel.add(numMetLayers);
        panel.add(new Label("empty", "  "));
        panel.add(numEmissionLayers);
        return panel;
    }
    
    private JComponent modelToRun() throws EmfException {
        JPanel panel = new JPanel(); 
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        ModelToRun runModel = caseObj.getModel();
        
        modelToRunCombo = new ComboBox(presenter.getModelToRuns());
        modelToRunCombo.setSelectedItem(runModel);
        modelToRunCombo.setPreferredSize(new Dimension(122, 22));
        
        modelVersionField = new TextField("modelVersion", fieldWidth / 2);
        modelVersionField.setText(caseObj.getModelVersion());
        modelVersionField.setEditable(false);
        modelVersionField.setPreferredSize(new Dimension(122, 22));
        
        panel.add(modelToRunCombo);
        panel.add(new Label("empty", "   "));
        panel.add(modelVersionField);

        return panel;
    }

    private ComboBox modRegions() throws EmfException {
        modRegionsCombo = new ComboBox(presenter.getRegions());
        modRegionsCombo.setSelectedItem(caseObj.getModelingRegion());
        modRegionsCombo.setPreferredSize(defaultDimension);

        return modRegionsCombo;
    }

    private ComboBox gridResolution() throws EmfException {
        gridResolutionCombo = new ComboBox(presenter.getGridResolutions());
        gridResolutionCombo.setSelectedItem(caseObj.getGridResolution());
        gridResolutionCombo.setPreferredSize(defaultDimension);

        return gridResolutionCombo;
    }

    private ComboBox abbreviations() throws EmfException {
        abbreviationsCombo = new ComboBox(presenter.getAbbreviations());
        abbreviationsCombo.setSelectedItem(caseObj.getAbbreviation());
        abbreviationsCombo.setPreferredSize(defaultDimension);
        addPopupMenuListener(abbreviationsCombo, "abbreviations");

        return abbreviationsCombo;
    }

    private ComboBox airQualityModels() throws EmfException {
        airQualityModelsCombo = new ComboBox(presenter.getAirQualityModels());
        airQualityModelsCombo.setSelectedItem(caseObj.getAirQualityModel());
        airQualityModelsCombo.setPreferredSize(defaultDimension);

        return airQualityModelsCombo;
    }

    private ComboBox categories() throws EmfException {
        categoriesCombo = new ComboBox(presenter.getCaseCategories());
        categoriesCombo.setSelectedItem(caseObj.getCaseCategory());
        categoriesCombo.setPreferredSize(defaultDimension);
        addPopupMenuListener(categoriesCombo, "categories");

        return categoriesCombo;
    }

    private JPanel sectors() throws EmfException {
        sectorsWidget = new AddRemoveSectorWidget(presenter.getAllSectors());
        sectorsWidget.setSectors(caseObj.getSectors());
        sectorsWidget.setPreferredSize(new Dimension(255, 80));
        return sectorsWidget;
    }

    private ComboBox emissionsYears() throws EmfException {
        emissionsYearCombo = new ComboBox(presenter.getEmissionsYears());
        emissionsYearCombo.setSelectedItem(caseObj.getEmissionsYear());
        emissionsYearCombo.setPreferredSize(defaultDimension);

        return emissionsYearCombo;
    }

    private ComboBox grids() throws EmfException {
        gridCombo = new ComboBox(presenter.getGrids());
        gridCombo.setSelectedItem(caseObj.getGrid());
        gridCombo.setPreferredSize(defaultDimension);

        return gridCombo;
    }

    private ComboBox meteorlogicalYears() throws EmfException {
        meteorlogicalYearCombo = new ComboBox(presenter.getMeteorlogicalYears());
        meteorlogicalYearCombo.setSelectedItem(caseObj.getMeteorlogicalYear());
        meteorlogicalYearCombo.setPreferredSize(defaultDimension);

        return meteorlogicalYearCombo;
    }

    private ComboBox speciations() throws EmfException {
        speciationCombo = new ComboBox(presenter.getSpeciations());
        speciationCombo.setSelectedItem(caseObj.getSpeciation());
        speciationCombo.setPreferredSize(defaultDimension);

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

        return runStatusCombo;
    }

    private TextField startDate() {
        startDate = new TextField("Start Date", fieldWidth);
        startDate.setText(format(caseObj.getStartDate()) + "");
        startDate.setPreferredSize(defaultDimension);
        startDate.setToolTipText("Date in format MM/dd/yyyy HH:mm");

        return startDate;
    }

    private TextField endDate() {
        endDate = new TextField("End Date", fieldWidth);
        endDate.setText(format(caseObj.getEndDate()) + "");
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
        this.caseObj = session.caseService().reloadCase(caseObj.getId());
        setLayout();
        viewOnly();
    }

}
