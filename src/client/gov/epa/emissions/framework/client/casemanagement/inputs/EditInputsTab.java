package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.BrowseButton;
import gov.epa.emissions.commons.gui.buttons.ExportButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.commons.gui.buttons.ViewButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.casemanagement.editor.FindCaseWindow;
import gov.epa.emissions.framework.client.casemanagement.editor.RelatedCaseView;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesViewer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.EmfFileChooser;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class EditInputsTab extends JPanel implements EditInputsTabView, RefreshObserver {

    private EmfConsole parentConsole;

    private EditInputsTabPresenterImpl presenter;

    private Case caseObj;

    private int caseId;

    private InputsTableData tableData;

    private JPanel mainPanel;

    private SelectableSortFilterWrapper table;

    private MessagePanel messagePanel;

    private DesktopManager desktopManager;

    private TextField inputDir;

    private ComboBox sectorsComboBox;

    private JCheckBox showAll;

    private EmfSession session;

    private ManageChangeables changeables;

    public EditInputsTab(EmfConsole parentConsole, ManageChangeables changeables, MessagePanel messagePanel,
            DesktopManager desktopManager) {
        super.setName("editInputsTab");
        this.parentConsole = parentConsole;
        this.messagePanel = messagePanel;
        this.desktopManager = desktopManager;
        this.changeables = changeables;
        super.setLayout(new BorderLayout());
    }

    public void display(EmfSession session, Case caseObj, EditInputsTabPresenter presenter) {
        super.removeAll();

        this.caseObj = caseObj;
        this.caseId = caseObj.getId();
        this.presenter = (EditInputsTabPresenterImpl) presenter;
        this.session = session;
        this.inputDir = new TextField("inputdir", 50);
        inputDir.setText(caseObj.getInputFileDir());
        this.changeables.addChangeable(inputDir);

        try {
            super.add(createLayout(new CaseInput[0], presenter, parentConsole), BorderLayout.CENTER);
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

            try {
                presenter.checkIfLockedByCurrentUser();
            } catch (Exception e) {
                messagePanel.setMessage(e.getMessage());
            }
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

    private void setupTableModel(CaseInput[] inputs) {
        tableData = new InputsTableData(inputs, session);
    }

    private JPanel createLayout(CaseInput[] inputs, EditInputsTabPresenter presenter, EmfConsole parentConsole)
            throws Exception {
        final JPanel layout = new JPanel(new BorderLayout());

        layout.add(createFolderNSectorPanel(), BorderLayout.NORTH);
        layout.add(tablePanel(inputs, parentConsole), BorderLayout.CENTER);
        layout.add(controlPanel(presenter), BorderLayout.PAGE_END);

        return layout;
    }

    private JPanel tablePanel(CaseInput[] inputs, EmfConsole parentConsole) {
        setupTableModel(inputs);

        mainPanel = new JPanel(new BorderLayout());
        table = new SelectableSortFilterWrapper(parentConsole, tableData, sortCriteria());
        mainPanel.add(table);

        return mainPanel;
    }

    private SortCriteria sortCriteria() {
        String[] columnNames = { "Envt. Var.", "Sector", "Input", "Job" };
        return new SortCriteria(columnNames, new boolean[] { true, true, true, true }, new boolean[] { false, false, false, false });
    }

    private JPanel createFolderNSectorPanel() throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Input Folder:", getFolderChooserPanel(inputDir,
                "Select the base Input Folder for the Case"), panel);

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

    private JPanel getFolderChooserPanel(final JTextField dir, final String title) {
        Button browseButton = new BrowseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                clearMessage();
                selectFolder(dir, title);
            }
        });
        JPanel folderPanel = new JPanel(new BorderLayout(2, 0));
        folderPanel.add(dir, BorderLayout.LINE_START);
        folderPanel.add(browseButton, BorderLayout.LINE_END);

        return folderPanel;
    }

    private void selectFolder(JTextField dir, String title) {
        EmfFileInfo initDir = new EmfFileInfo(dir.getText(), true, true);
        EmfFileChooser chooser = new EmfFileChooser(initDir, new EmfFileSystemView(session.dataCommonsService()));
        chooser.setTitle(title);
        int option = chooser.showDialog(parentConsole, "Select a folder");

        EmfFileInfo file = (option == EmfFileChooser.APPROVE_OPTION) ? chooser.getSelectedDir() : null;
        if (file == null)
            return;

        if (file.isDirectory()) {
            caseObj.setInputFileDir(file.getAbsolutePath());
            dir.setText(file.getAbsolutePath());
        }
    }

    private JPanel controlPanel(final EditInputsTabPresenter presenter) {
        Insets insets = new Insets(1, 2, 1, 2);
        JPanel container = new JPanel();
        
        Button add = new AddButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMessage();
                try {
                    doNewInput(presenter);
                } catch (EmfException e1) {
                    messagePanel.setError(e1.getMessage());
                }
            }
        });
        add.setMargin(insets);
        container.add(add);

        Button remove = new RemoveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMessage();
                try {
                    doRemove(presenter);
                } catch (EmfException exc) {
                    messagePanel.setError(exc.getMessage());
                }
            }
        });
        remove.setMargin(insets);
        container.add(remove);
        
        String message1 = "You have asked to open a lot of windows. Do you wish to proceed?";
        ConfirmDialog confirmDialog1 = new ConfirmDialog(message1, "Warning", this);
        SelectAwareButton edit = new SelectAwareButton("Edit", editAction(), table, confirmDialog1);
        edit.setMargin(insets);
        container.add(edit);

        Button copy = new Button("Copy", copyAction(presenter));
        copy.setMargin(insets);
        container.add(copy);

        Button view = new ViewButton("View Dataset", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doDisplayInputDatasetsPropertiesViewer();
            }
        });
        view.setMargin(insets);
        container.add(view);

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

    private Action copyAction(final EditInputsTabPresenter localPresenter) {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                try {
                    clearMessage();
                    copyInputs(localPresenter);
                } catch (Exception ex) {
                    messagePanel.setError(ex.getMessage());
                }
            }
        };
    }
    
    private Action editAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    clearMessage();
                    doEditInput(presenter);
                } catch (EmfException ex) {
                    messagePanel.setError(ex.getMessage());
                }
            }
        };
        return action; 
    }

    protected void doNewInput(EditInputsTabPresenter presenter) throws EmfException {
        checkModelToRun();
        NewInputDialog view = new NewInputDialog(parentConsole);
        try {
            CaseInput newInput = new CaseInput();
            newInput.setCaseID(caseId);
            newInput.setRequired(true);
            newInput.setLocal(true);
            presenter.addNewInputDialog(view, newInput);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    protected void doRemove(EditInputsTabPresenter presenter) throws EmfException {
        CaseInput[] inputs = getSelectedInputs().toArray(new CaseInput[0]);

        if (inputs.length == 0) {
            messagePanel.setMessage("Please select input(s) to remove.");
            return;
        }

        String title = "Warning";
        String message = "Are you sure you want to remove the selected input(s)?";
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            tableData.remove(inputs);
            refresh();
            presenter.removeInputs(inputs);
        }
    }

    private void doEditInput(EditInputsTabPresenter presenter) throws EmfException {
        checkModelToRun(); 
        
        List<CaseInput> inputs = getSelectedInputs();

        if (inputs.size() == 0) {
            messagePanel.setMessage("Please select input(s) to edit.");
            return;
        }

        for (Iterator<CaseInput> iter = inputs.iterator(); iter.hasNext();) {
            CaseInput input = iter.next();
            String title = "Edit Case Input: "+input.getName() + "(" + input.getId() + ")(" + caseObj.getName() + ")";
            EditCaseInputView inputEditor = new EditCaseInputWindow(title, desktopManager, parentConsole);
            presenter.doEditInput(input, inputEditor);
        }
    }
    
    private void checkModelToRun() throws EmfException{
        if (caseObj.getModel() == null || caseObj.getModel().getId()==0)
            throw new EmfException("Please specify model to run on summary tab. ");
    }

    private void copyInputs(EditInputsTabPresenter presenter) throws Exception {
        checkModelToRun();
        List<CaseInput> inputs = getSelectedInputs();

        if (inputs.size() == 0) {
            messagePanel.setMessage("Please select input(s) to copy.");
            return;
        }

        Object[] selected = presenter.getAllCaseNameIDs();
        String selectedCase = (String) JOptionPane.showInputDialog(parentConsole, "Copy " + inputs.size()
                + " case input(s) to case: ", "Copy Case Inputs", JOptionPane.PLAIN_MESSAGE, getCopyIcon(), selected,
                selected[getDefultIndex(selected)]);

        if ((selectedCase != null) && (selectedCase.length() > 0)) {
            int selectedCaseId = getCaseId(selectedCase);

            if (selectedCaseId != this.caseId) {
                presenter.copyInput(selectedCaseId, inputs);
                return;
            }

            showEditor(presenter, inputs, selectedCase);
        }
    }
    
    private void showEditor(EditInputsTabPresenter presenter, List<CaseInput> inputs, String selectedCase)
    throws Exception {
        for (Iterator<CaseInput> iter = inputs.iterator(); iter.hasNext();) {
            CaseInput input = iter.next();
            NewInputDialog view = new NewInputDialog(parentConsole);
            view.setModal(false);
            view.setLocationByPlatform(true);
            presenter.copyInput(input, view);
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
                e.printStackTrace();
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

    private int checkOverWrite() {
        // String title = "Message";
        // String message = "Would you like to remove previously exported files prior to export?";
        // return JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.YES_NO_OPTION,
        // JOptionPane.QUESTION_MESSAGE);
        // FIXME: Temporal setting till gets back from Marc on this policy 11/09/2007 Qun
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

    public void addInput(CaseInput input) {
        tableData.add(input);
        table.refresh(tableData);
        panelRefresh();
    }

    private List<CaseInput> getSelectedInputs() {
        return (List<CaseInput>) table.selected();
    }

    private Sector getSelectedSector() {
        return (Sector) sectorsComboBox.getSelectedItem();
    }

    public CaseInput[] caseInputs() {
        return tableData.sources();
    }

    public String getCaseInputFileDir() {
        if (inputDir == null)
            return null;
        return inputDir.getText();
    }

    private int getDefultIndex(Object[] selected) {
        int currentCaseId = this.caseObj.getId();
        int length = selected.length;

        for (int i = 0; i < length; i++)
            if (selected[i].toString().contains("(" + currentCaseId + ")"))
                return i;

        return 0;
    }

    private int getCaseId(String selectedCase) {
        int index1 = selectedCase.indexOf("(") + 1;
        int index2 = selectedCase.indexOf(")");

        return Integer.parseInt(selectedCase.substring(index1, index2));
    }

    private Icon getCopyIcon() {
        URL imgURL = getClass().getResource("/toolbarButtonGraphics/general/Copy24.gif");

        if (imgURL != null) {
            return new ImageIcon(imgURL);
        }

        return null;
    }

    private CaseInput[] listFreshInputs() throws EmfException {
        CaseInput[] freshList = presenter.getCaseInput(caseId, getSelectedSector(), showAll.isSelected());
        
        if (getSelectedSector() == null && freshList.length == presenter.getPageSize())
            setMessage("Please select a sector to see full list of inputs.");
        else
            messagePanel.clear();
        
        return freshList;
    }
    
    public void refresh() {
        // note that this will get called when the case is save
        try {
            if (tableData != null) // it's still null if you've never displayed this tab
                doRefresh(tableData.sources());
        } catch (Exception e) {
            setErrorMessage("Cannot refresh current tab. " + e.getMessage());
        }
    }

    public int numberOfRecord() {
        return tableData.sources().length;
    }

    public void clearMessage() {
        messagePanel.clear();
    }

    public void setErrorMessage(String message) {
        messagePanel.setError(message);
    }

    public void setMessage(String message) {
        messagePanel.setMessage(message);
    }

    public void doRefresh() throws EmfException {
        try {
            kickPopulateThread();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
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

}
