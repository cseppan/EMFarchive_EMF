package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionDialog;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.ListWidget;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;

public class QAAnnualStateSummariesCrosstabWindow extends DisposableInteralFrame {
    
    private EmfConsole parentConsole;
    
    private EditQAAnnualStateSummariesCrosstabEmissionsPresenter presenter;
    
    private ListWidget smkRptDatasetsListWidget;

    private EmfSession session;
    
    private SingleLineMessagePanel messagePanel;
    
    private EmfDataset[] smkRpts;
    
    private DatasetType smkRptDST;
    
    private DatasetType countryStateCountyNamesAndDataCOSTCYDST;

    private ListWidget coStCyDatasetListWidget;

    private ListWidget masterPollListWidget;

    private ListWidget leftPollListWidget;

    private ListWidget exclPollListWidget;

    private Map<String,String> pollMap; //stores master list of pollutants that could be available to the client
        // could be user-defined or could be determined by getting a distinct list of polls from the various smkrpt datasets

    private EmfDataset coStCyDataset;

    private String[] pollList;

    private String[] specieList;

    private String[] exclPollList;

//    private String[] sortPollList;

//    private String program;
    
    public QAAnnualStateSummariesCrosstabWindow(DesktopManager desktopManager, String programVal, EmfSession session,
            EmfDataset[] smkRptDatasets, EmfDataset coStCyDataset, String[] pollList,
            String[] specieList, String[] exclPollList, String[] sortPollList) {
        super("Compare annual state summaries", new Dimension(800, 450), desktopManager);
        this.setMinimumSize(new Dimension(800, 450));
//      this.program = program;
        this.session = session;
        this.smkRpts = smkRptDatasets;
        this.coStCyDataset = coStCyDataset;
        this.pollList = pollList;
        this.specieList = specieList;
        this.exclPollList = exclPollList;
//        this.sortPollList = sortPollList;
        this.pollMap = new TreeMap<String,String>();
    }

    public void display(EmfDataset dataset, QAStep qaStep) throws EmfException  {
        try {
            this.smkRptDST = presenter.getDatasetType(DatasetType.smkmergeRptStateAnnualSummary);
            this.countryStateCountyNamesAndDataCOSTCYDST = presenter.getDatasetType(DatasetType.countryStateCountyNamesAndDataCOSTCY);
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        super.setTitle("Setup "+qaStep.getName()+": " + dataset.getName() + "_" + qaStep.getId() );
        super.display();
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        messagePanel = new SingleLineMessagePanel();
        this.getContentPane().add(messagePanel);
        this.getContentPane().add(createLayout(dataset));
        this.getContentPane().add(pollSelectorPanel());
        this.getContentPane().add(buttonPanel());
    }

    public void observe(EditQAAnnualStateSummariesCrosstabEmissionsPresenter presenter) {
        this.presenter = presenter;
    }
    
    private JPanel createLayout(EmfDataset dataset) {
        
        JPanel content = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
       
        layoutGenerator.addLabelWidgetPair("<html>" + DatasetType.smkmergeRptStateAnnualSummary.replaceFirst("annual summary", "annual<br/>summary") + " Datasets:</html>",  smokeReportDatasetsPanel(), content);
        layoutGenerator.addLabelWidgetPair("<html>" + DatasetType.countryStateCountyNamesAndDataCOSTCY.replaceFirst("names and", "names<br/>and") + " Dataset:</html>",  coStCyDatasetPanel(), content);
        
        layoutGenerator.makeCompactGrid(content, 2, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad*/
        
        return content;
    }
    
    private JPanel coStCyDatasetPanel() {
        
        coStCyDatasetListWidget = new ListWidget(new EmfDataset[0]);
        coStCyDatasetListWidget.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        if(!(coStCyDataset==null))
            setDatasetsFromStepWindow(coStCyDatasetListWidget, new EmfDataset[] {coStCyDataset});
        
        JScrollPane pane = new JScrollPane(coStCyDatasetListWidget);
        pane.setPreferredSize(new Dimension(450, 25));
        coStCyDatasetListWidget.setToolTipText("The " + DatasetType.countryStateCountyNamesAndDataCOSTCY + " dataset.  Press select button to choose from a list.");
        
        Button addButton = new AddButton("Select", addCoStCyDatasetAction());
        addButton.setMargin(new Insets(1, 2, 1, 2));
        
        JPanel container = new JPanel(new FlowLayout());

        container.add(pane);
        container.add(addButton);
        
        return container;
    }
    
    private JPanel pollSelectorPanel() throws EmfException {
        JPanel leftPollListPanel = new JPanel();
        leftPollListPanel.setLayout(new BoxLayout(leftPollListPanel, BoxLayout.Y_AXIS));

        leftPollListWidget = new ListWidget(new EmfDataset[0]);
        leftPollListWidget.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane leftPane = new JScrollPane(leftPollListWidget);
        leftPane.setPreferredSize(new Dimension(100, 75));
        leftPollListWidget.setToolTipText("The fixed left column pollutants of the report.  Press select Add button to add to list.");
        if (pollList != null && pollList.length > 0) {
            for (String poll : pollList) {
                leftPollListWidget.addElement(poll);
            }
        }
        leftPollListPanel.add(new JLabel("<html>Fixed Left<br/>Columns:</html>"));
        leftPollListPanel.add(leftPane);
        Button removeLeftPollButton = new RemoveButton("Remove", removeLeftPollAction());
        removeLeftPollButton.setMargin(new Insets(1, 2, 1, 2));
        leftPollListPanel.add(removeLeftPollButton);

        JPanel masterPollListPanel = new JPanel();
        masterPollListPanel.setLayout(new BoxLayout(masterPollListPanel, BoxLayout.Y_AXIS));
        masterPollListWidget = new ListWidget(new EmfDataset[0]);
        masterPollListWidget.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane pane = new JScrollPane(masterPollListWidget);
        pane.setPreferredSize(new Dimension(100, 75));
        masterPollListWidget.setToolTipText("The alphabetical right column pollutants of the report.");
//this should be populated with a distinct (unioned) list of pollutants from all the smk rpts
//        if (specieList != null && specieList.length > 0) {
//            for (String poll : specieList) {
//                masterPollListWidget.addElement(poll);
//            }
//        }
        masterPollListPanel.add(new JLabel("<html>Alphabetical Right<br/>Columns:</html>"));
        masterPollListPanel.add(pane);
        JLabel fakeLabel = new JLabel("");
        fakeLabel.setPreferredSize(new Dimension(0, 25));
        fakeLabel.setOpaque(false);
        masterPollListPanel.add(fakeLabel);
        
        JPanel exclPollListPanel = new JPanel();
        exclPollListPanel.setLayout(new BoxLayout(exclPollListPanel, BoxLayout.Y_AXIS));
        exclPollListWidget = new ListWidget(new EmfDataset[0]);
        exclPollListWidget.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane excludePane = new JScrollPane(exclPollListWidget);
        excludePane.setPreferredSize(new Dimension(100, 75));
        exclPollListWidget.setToolTipText("The pollutants/species to exclude from the report.  Press select Exclude button to add to list.");
        if (exclPollList != null && exclPollList.length > 0) {
            for (String poll : exclPollList) {
                exclPollListWidget.addElement(poll);
            }
        }
        exclPollListPanel.add(new JLabel("<html>Pollutants/Species<br/>To Exclude:</html>"));
        exclPollListPanel.add(excludePane);
        Button removeExcludePollButton = new RemoveButton("Remove", removeExcludePollAction());
        removeExcludePollButton.setMargin(new Insets(1, 2, 1, 2));
        exclPollListPanel.add(removeExcludePollButton);

        //First lets populate master poll Map with any user-defined pollutants from the QA step arguments
        if (pollList != null && pollList.length > 0)
            for (String poll : pollList) 
                pollMap.put(poll, poll);
        if (specieList != null && specieList.length > 0)
            for (String specie : specieList) 
                pollMap.put(specie, specie);
        //do this only after all lists have been created.
        mergePollList();
        populateMasterPollListWidget();

        Button addPollButton = new AddButton("<<Add", addPollAction());
        addPollButton.setMargin(new Insets(1, 2, 1, 2));
        Button excludePollButton = new AddButton("Exclude>>", addExcludedPollAction());
        excludePollButton.setMargin(new Insets(1, 2, 1, 2));

        JPanel container = new JPanel(new FlowLayout());

        container.add(leftPollListPanel);
        container.add(addPollButton);
        container.add(masterPollListPanel);
        container.add(excludePollButton);
        container.add(exclPollListPanel);

        return container;
    }

    private JPanel smokeReportDatasetsPanel() {
        
        smkRptDatasetsListWidget = new ListWidget(new EmfDataset[0]);
        smkRptDatasetsListWidget.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        if(!(smkRpts==null))
            setDatasetsFromStepWindow(smkRptDatasetsListWidget, smkRpts);
        
        JScrollPane pane = new JScrollPane(smkRptDatasetsListWidget);
        pane.setPreferredSize(new Dimension(450, 75));
        smkRptDatasetsListWidget.setToolTipText("The " + DatasetType.smkmergeRptStateAnnualSummary + " datasets.  Press select button to choose from a list.");
//        speciationProfileWeightDatasetsListWidget.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        Button addButton = new AddButton("Select", addSmkRptDatasetsAction());
        addButton.setMargin(new Insets(1, 2, 1, 2));
        
        JPanel container = new JPanel(new FlowLayout());

        container.add(pane);
        container.add(addButton);
        
        return container;
    }
    
    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        panel.add(new OKButton(okAction()));
        panel.add(new CancelButton(cancelAction()));
        return panel;
    }
  
    private Action addSmkRptDatasetsAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doAddSmkRptDatasetsWindow();
            }
        };
    }
    
    private Action addCoStCyDatasetAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doAddCoStCyDatasetWindow();
            }
        };
    }
    
    private Action addExcludedPollAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                addToExcludePollListWidget(masterPollListWidget.getSelectedValues());
            }
        };
    }
    
    private Action addPollAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                addToPollListWidget(masterPollListWidget.getSelectedValues());
            }
        };
    }
    
    private Action removeLeftPollAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                leftPollListWidget.removeElements(leftPollListWidget.getSelectedValues());
                populateMasterPollListWidget();
            }
        };
    }
    
    private Action removeExcludePollAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                exclPollListWidget.removeElements(exclPollListWidget.getSelectedValues());
                populateMasterPollListWidget();
            }
        };
    }
    
    protected Action cancelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                dispose();
                disposeView();
            }

        };
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //validate dataset selections
                Object[] smkRpts = getSmkRptDatasets();
                Object coStCy = getCoStCyDataset();
                Object[] polls = getPollutants();
                Object[] species = getSpecies();
                Object[] exclPollutants = getExcludedPollutants();
//                Object[] pollutantsSort = getPollutantsSort();
                String errors = "";

                if (smkRpts == null || smkRpts.length == 0) {
                    errors += "Missing SMOKE Report Dataset(s). ";
                }
                if (coStCy == null ) {
                    errors += "Missing " + DatasetType.countryStateCountyNamesAndDataCOSTCY + " Dataset. ";
                }
                
                if (errors.length() > 0) {
                    messagePanel.setError(errors);
                    return;
                }
                presenter.updateAnnualStateSummariesDatasets(smkRpts,
                        coStCy,
                        polls,
                        species,
                        exclPollutants);
                dispose();
                disposeView();
            }
        };
    }
    
    private void doAddSmkRptDatasetsWindow() {
        try {
            InputDatasetSelectionDialog view = new InputDatasetSelectionDialog (parentConsole);
            InputDatasetSelectionPresenter presenter = new InputDatasetSelectionPresenter(view, session, new DatasetType[] {smkRptDST});
            presenter.display(smkRptDST, false);
            if (view.shouldCreate())
                setSmkRptDatasets(presenter.getDatasets());
        } catch (Exception e) {
             messagePanel.setError(e.getMessage());
        }
    }
    
    private void doAddCoStCyDatasetWindow() {
        try {
            InputDatasetSelectionDialog view = new InputDatasetSelectionDialog (parentConsole);
            InputDatasetSelectionPresenter presenter = new InputDatasetSelectionPresenter(view, session, new DatasetType[] {countryStateCountyNamesAndDataCOSTCYDST});
            presenter.display(countryStateCountyNamesAndDataCOSTCYDST, true);
            if (view.shouldCreate()) {
                coStCyDatasetListWidget.removeAllElements();
                for (EmfDataset dataset : presenter.getDatasets())
                    coStCyDatasetListWidget.addElement(dataset);
            }
        } catch (Exception e) {
             messagePanel.setError(e.getMessage());
        }
    }
    
    private void populateMasterPollListWidget(){
        masterPollListWidget.removeAllElements();
        for (String poll : pollMap.values()) {
            //only add if other two lists don't contain it
            if (!leftPollListWidget.contains(poll) && !exclPollListWidget.contains(poll))
                masterPollListWidget.addElement(poll);
        }
    }
    
    private void mergePollList() throws EmfException {
        for (Object smkRpt : smkRptDatasetsListWidget.getAllElements()) {
            EmfDataset smkRptDS = (EmfDataset)smkRpt;
            String[] polls = presenter.getTableColumnDistinctValues(smkRptDS.getId(), smkRptDS.getDefaultVersion(), "species", "", "species");
            for (String poll : polls) 
                pollMap.put(poll, poll);
        }
    }
    
    private void setDatasetsFromStepWindow(ListWidget listWidget, EmfDataset[] datasets){
        listWidget.removeAllElements();
        for (int i = 0; i < datasets.length; i++) {
            listWidget.addElement(datasets[i]);
        }
    }
    
    private void setSmkRptDatasets(EmfDataset [] datasets) throws EmfException {
        smkRptDatasetsListWidget.removeAllElements();
        for (int i = 0; i < datasets.length; i++) {
            if (!smkRptDatasetsListWidget.contains(datasets[i])) 
           //System.out.println(" Inv dataset is: " + datasets[i]);
                smkRptDatasetsListWidget.addElement(datasets[i]);
        }
        //refresh JLists...
        mergePollList();
        populateMasterPollListWidget();

    }

    private void addToPollListWidget(Object[] polls) {
        for (Object poll : polls) {
            if (!leftPollListWidget.contains(poll)) {
                leftPollListWidget.addElement(poll);
            }
            masterPollListWidget.removeElements(new Object[] {poll});
        }
    }

    private void addToExcludePollListWidget(Object[] polls) {
        for (Object poll : polls) {
            if (!exclPollListWidget.contains(poll)) {
                exclPollListWidget.addElement(poll);
            }
            masterPollListWidget.removeElements(new Object[] {poll});
        }
    }

    protected Object getCoStCyDataset() {
        return (coStCyDatasetListWidget.getAllElements() != null && coStCyDatasetListWidget.getAllElements().length > 0) ? coStCyDatasetListWidget.getAllElements()[0] : null;
    }
    
    protected Object[] getSmkRptDatasets() {
        return (smkRptDatasetsListWidget.getAllElements() != null && smkRptDatasetsListWidget.getAllElements().length > 0) ? smkRptDatasetsListWidget.getAllElements() : null;
    }
    
    protected Object[] getPollutants() {
        return (leftPollListWidget.getAllElements() != null && leftPollListWidget.getAllElements().length > 0) ? leftPollListWidget.getAllElements() : null;
    }
    
    protected Object[] getSpecies() {
        return (masterPollListWidget.getAllElements() != null && masterPollListWidget.getAllElements().length > 0) ? masterPollListWidget.getAllElements() : null;
    }
    
    protected Object[] getExcludedPollutants() {
        return (exclPollListWidget.getAllElements() != null && exclPollListWidget.getAllElements().length > 0) ? exclPollListWidget.getAllElements() : null;
    }
    
//    protected Object[] getPollutantsSort() {
//        return (sortPollList != null && sortPollList.length > 0) ? sortPollList : null;
//    }

//    public void refreshMasterPollList(String[] polls) {
//        for (String poll : polls)
//            pollMap.put(poll, poll);
//        
//        //refresh JList...
//        reconcilePollListWidget();
//        reconcileExclPollListWidget();
//        populateMasterPollListWidget();
//    }
}
