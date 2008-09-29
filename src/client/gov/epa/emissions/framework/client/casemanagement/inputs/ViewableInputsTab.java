package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.ExportButton;
import gov.epa.emissions.commons.gui.buttons.ViewButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.casemanagement.editor.FindCaseWindow;
import gov.epa.emissions.framework.client.casemanagement.editor.RelatedCaseView;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesViewer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class ViewableInputsTab extends JPanel implements RefreshObserver {

    private EmfConsole parentConsole;

    private ViewableInputsTabPresenterImpl presenter;

    private Case caseObj;

    private int caseId;

    private InputsTableData tableData;

    private JPanel mainPanel;
    
    private SelectableSortFilterWrapper table;

    private MessagePanel messagePanel;

    private DesktopManager desktopManager;

    private TextField inputDir;
    
    private JCheckBox showAll;
    
    private ComboBox sectorsComboBox;

    private EmfSession session;


    public ViewableInputsTab(EmfConsole parentConsole, MessagePanel messagePanel,
            DesktopManager desktopManager) {
        super.setName("viewInputsTab");
        this.parentConsole = parentConsole;
        this.messagePanel = messagePanel;
        this.desktopManager = desktopManager;
        super.setLayout(new BorderLayout());
    }

    public void display(EmfSession session, Case caseObj, ViewableInputsTabPresenterImpl presenter) {
        super.removeAll();

        this.caseObj = caseObj;
        this.caseId = caseObj.getId();
        this.presenter = presenter;
        this.session = session;
        this.inputDir = new TextField("inputdir", 50);
        inputDir.setText(caseObj.getInputFileDir());
        inputDir.setEditable(false);

        try {
            super.add(createLayout(new CaseInput[0], parentConsole), BorderLayout.CENTER);
        } catch (Exception e) {
            messagePanel.setError("Cannot retrieve all case inputs.");
        }

        kickPopulateThread();
    }

    private void kickPopulateThread() {
        Thread populateThread = new Thread(new Runnable() {
            public void run() {
                retrieveInputs();
            }
        });
        populateThread.start();
    }

    private synchronized void retrieveInputs() {
        try {
            messagePanel.setMessage("Please wait while retrieving all case inputs...");
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            doRefresh(listFreshInputs());
        } catch (Exception e) {
            messagePanel.setError("Cannot retrieve all case inputs.");
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void doRefresh(CaseInput[] inputs) throws Exception {
        String inputFileDir = caseObj.getInputFileDir();
        if (!inputDir.getText().equalsIgnoreCase(inputFileDir))
            inputDir.setText(inputFileDir);
        setupTableModel(inputs);
        table.refresh(tableData);
        panelRefresh();
    }
    
    private void panelRefresh() {
        mainPanel.removeAll();
        mainPanel.add(table);
        super.validate();
    }

    private JPanel createLayout(CaseInput[] inputs, EmfConsole parentConsole)
            throws Exception {
        final JPanel layout = new JPanel(new BorderLayout());

        layout.add(createFolderNSectorPanel(), BorderLayout.NORTH);
        layout.add(tablePanel(inputs, parentConsole), BorderLayout.CENTER);
        layout.add(controlPanel(), BorderLayout.PAGE_END);

        return layout;
    }

    private JPanel tablePanel(CaseInput[] inputs, EmfConsole parentConsole) {
        setupTableModel(inputs);

        mainPanel = new JPanel(new BorderLayout());
        table = new SelectableSortFilterWrapper(parentConsole, tableData, sortCriteria());
        mainPanel.add(table);

        return mainPanel;
    }
    
    private void setupTableModel(CaseInput[] inputs){
        tableData = new InputsTableData(inputs, session);
    }

    private JPanel createFolderNSectorPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Input Folder:", inputDir, panel);

        sectorsComboBox = new ComboBox("Select a Sector", presenter.getAllSetcors());
        sectorsComboBox.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    doRefresh(listFreshInputs());
                } catch (Exception exc) {
                    setErrorMessage(exc.getMessage());
                }
            }
        });

        layoutGenerator.addLabelWidgetPair("Sector:", sectorsComboBox, panel);
        layoutGenerator.makeCompactGrid(panel, 2, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 5);// xPad, yPad

        return panel;
    }

    private SortCriteria sortCriteria() {
        String[] columnNames = { "Envt. Var.", "Sector", "Input", "Job" };
        return new SortCriteria(columnNames, new boolean[] { true, true, true, true }, new boolean[] { false, false, false, false });
    }

    private JPanel controlPanel() {
        Insets insets = new Insets(1, 2, 1, 2);
        JPanel container = new JPanel();
        
        String message = "You have asked to open a lot of windows. Do you wish to proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);
        SelectAwareButton view = new SelectAwareButton("View", viewAction(), table, confirmDialog);
        view.setMargin(insets);
        container.add(view);

        Button viewDS = new ViewButton("View Dataset", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doDisplayInputDatasetsPropertiesViewer();
            }
        });
        viewDS.setMargin(insets);
        container.add(viewDS);

        Button export = new ExportButton("Export Inputs", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doExportInputDatasets(getSelectedInputs());
            }
        });
        export.setMargin(insets);
        container.add(export);
        
        Button findRelated = new Button("Find", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                viewCasesReleatedToDataset();
            }
        });
        findRelated.setMargin(insets);
        container.add(findRelated);

        showAll = new JCheckBox("Show All", false);
        showAll.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMessage();
                try {
                    doRefresh(listFreshInputs());
                } catch (Exception ex) {
                    setErrorMessage(ex.getMessage());
                }
            }
        });
        container.add(showAll);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(container, BorderLayout.WEST);

        return panel;
    }
    
    private Action viewAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    clearMessage();
                    doView();
                } catch (EmfException ex) {
                    messagePanel.setError(ex.getMessage());
                }
            }
        };
        return action; 
    }
    
    private void doView() throws EmfException {
        List inputs = getSelectedInputs();

        if (inputs.size() == 0) {
            messagePanel.setMessage("Please select input(s) to edit.");
            return;
        }

        for (Iterator iter = inputs.iterator(); iter.hasNext();) {
            CaseInput input = (CaseInput) iter.next();
            String title = "View Case Input:"+input.getName() + "(" + input.getId() + ")(" + caseObj.getName() + ")";
            EditCaseInputView inputEditor = new EditCaseInputWindow(title, desktopManager, parentConsole);
            presenter.doEditInput(input, inputEditor);
            inputEditor.viewOnly(title);
        }
    }

    private void doDisplayInputDatasetsPropertiesViewer() {
        List<EmfDataset> datasets = getSelectedDatasets(getSelectedInputs());
        if (datasets.isEmpty()) {
            messagePanel.setMessage("Please select one or more inputs with datasets specified to view.");
            return;
        }
        for (Iterator<EmfDataset> iter = datasets.iterator(); iter.hasNext();) {
            DatasetPropertiesViewer view = new DatasetPropertiesViewer(session, parentConsole, desktopManager);
            EmfDataset dataset = iter.next();
            try {
                presenter.doDisplayPropertiesView(view, dataset);
            } catch (EmfException e) {
                messagePanel.setError(e.getMessage());
            }
        }
    }

    private void doExportInputDatasets(List inputlist) {
        if (inputlist.size() == 0) {
            messagePanel.setMessage("Please select input(s) to export.");
            return;
        }

        int numberToExport = checkToWriteStartMessage(inputlist);

        if (!checkExportDir(inputDir.getText()) || !checkDatasets(inputlist) || numberToExport < 1)
            return;

        int ok = checkOverWrite();
        String purpose = "Used by case: " + this.caseObj.getName() + ".";

        try {
            if (ok != JOptionPane.YES_OPTION) {
                presenter.exportCaseInputs(inputlist, purpose);
            } else {
                presenter.exportCaseInputsWithOverwrite(inputlist, purpose);
            }
            messagePanel.setMessage("Started export of " + numberToExport
                    + " input datasets.  Please see the Status Window for additional information.");
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private boolean checkExportDir(String exportDir) {
        if (exportDir == null || exportDir.equals("")) {
            messagePanel.setMessage("Please specify the input folder before exporting the case inputs.");
            return false;
        }

        return true;
    }

    private boolean checkDatasets(List inputList) {
        CaseInput[] inputs = (CaseInput[]) inputList.toArray(new CaseInput[0]);

        for (int i = 0; i < inputs.length; i++)
            if (inputs[i].isRequired() && inputs[i].getDataset() == null) {
                messagePanel.setMessage("Please specify a dataset for the required input \"" + inputs[i].getName()
                        + "\".");
                return false;
            }

        return true;
    }

    // returns the number of datasets that will actually be exported
    private int checkToWriteStartMessage(List inputList) {
        CaseInput[] inputs = (CaseInput[]) inputList.toArray(new CaseInput[0]);
        int count = 0;

        for (int i = 0; i < inputs.length; i++) {
            DatasetType type = inputs[i].getDatasetType();
            EmfDataset dataset = inputs[i].getDataset();
            if (type != null && dataset != null)
                count++;
        }

        if (count == 0)
            messagePanel.setMessage("Please make sure the selected inputs have datasets in them).");

        return count;
    }
    
    private void viewCasesReleatedToDataset() {
        List<CaseInput> inputlist = getSelectedInputs();
        if (inputlist == null || inputlist.size() != 1 ){
            messagePanel.setMessage("Please select one input. ");
            return; 
        }
        
        EmfDataset dataset = inputlist.get(0).getDataset();
        if (dataset == null ){
            messagePanel.setMessage("No dataset available. ");
            return; 
        }
        
        try {
            Case[] casesByInputDataset = presenter.getCasesByInputDataset(dataset.getId());
            Case[] casesByOutputDataset  = presenter.getCasesByOutputDatasets(new int[] {dataset.getId()});
            String title = "Find Uses of Dataset: " + dataset.getName();
            RelatedCaseView view = new FindCaseWindow(title, session, parentConsole, desktopManager);
            presenter.doViewRelated(view, casesByOutputDataset, casesByInputDataset);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }

    } 


    private int checkOverWrite() {
        //FIXME: Temporal setting till gets back from Marc on this policy 11/09/2007 Qun
        return JOptionPane.YES_OPTION;
    }

    private List<EmfDataset> getSelectedDatasets(List inputlist) {
        List<EmfDataset> datasetList = new ArrayList<EmfDataset>();

        for (int i = 0; i < inputlist.size(); i++) {
            EmfDataset dataset = ((CaseInput) inputlist.get(i)).getDataset();
            if (dataset != null)
                datasetList.add(dataset);
        }

        return datasetList;
    }

    private CaseInput[] listFreshInputs() throws EmfException {
        CaseInput[] freshList = presenter.getCaseInput(caseId, getSelectedSector(), showAll.isSelected());
        
        if (getSelectedSector() == null && freshList.length == presenter.getPageSize())
            setMessage("Please select a sector to see full list of inputs.");
        else
            messagePanel.clear();
        
        return freshList;
    }
    
    private Sector getSelectedSector() {
        return (Sector) sectorsComboBox.getSelectedItem();
    }
    
    public void setMessage(String message) {
        messagePanel.setMessage(message);
    }
    
    public void setErrorMessage(String message) {
        messagePanel.setError(message);
    }
    
    public void addInput(CaseInput note) {
        tableData.add(note);
        table.refresh(tableData);
        panelRefresh();
    }

    private List getSelectedInputs() {
        return table.selected();
    }

    public CaseInput[] caseInputs() {
        return tableData.sources();
    }

    public void refresh() {
        // note that this will get called when the case is save
        try {
            if (tableData != null) // it's still null if you've never displayed this tab
                doRefresh(tableData.sources());
        } catch (Exception e) {
            messagePanel.setError("Cannot refresh current tab. " + e.getMessage());
        }
    }

    public int numberOfRecord() {
        return tableData.sources().length;
    }

    public void clearMessage() {
        messagePanel.clear();
    }

    public void doRefresh() throws EmfException {
        try {
            kickPopulateThread();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }
    
    

}
