package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.BrowseButton;
import gov.epa.emissions.commons.gui.buttons.EditButton;
import gov.epa.emissions.commons.gui.buttons.ExportButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.commons.gui.buttons.ViewButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesViewer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class EditInputsTab extends JPanel implements EditInputsTabView {

    private EmfConsole parentConsole;

    private EditInputsTabPresenter presenter;

    private Case caseObj;
    
    private int caseId;

    private InputsTableData tableData;

    private SortFilterSelectModel selectModel;

    private JPanel tablePanel;

    private MessagePanel messagePanel;

    private DesktopManager desktopManager;

    private TextField inputDir;

    private EmfSession session;
    
    private ManageChangeables changeables;

    public EditInputsTab(EmfConsole parentConsole, ManageChangeables changeables, MessagePanel messagePanel,
            DesktopManager desktopManager) {
        super.setName("editInputsTab");
        this.parentConsole = parentConsole;
        this.messagePanel = messagePanel;
        this.desktopManager = desktopManager;
        this.changeables = changeables;

        this.inputDir = new TextField("inputdir", 30);
        this.changeables.addChangeable(inputDir);

        super.setLayout(new BorderLayout());
    }

    public void display(EmfSession session, Case caseObj, EditInputsTabPresenter presenter) {
        super.removeAll();
        
        this.caseObj = caseObj;
        this.caseId = caseObj.getId();
        this.presenter = presenter;
        this.session = session;
        inputDir.setText(caseObj.getInputFileDir());
//        inputDir.addKeyListener(new KeyAdapter() {
//            public void keyTyped(KeyEvent e) {
//                saveCaseInputFileDir();
//            }
//        });
        
        try {
            super.add(createLayout(presenter.getCaseInput(caseId), presenter, parentConsole), BorderLayout.CENTER);
        } catch (EmfException e) {
            messagePanel.setError("Cannot retrieve all case inputs.");
        }
    }

    private void doRefresh(CaseInput[] inputs) {
        inputDir.setText(caseObj.getInputFileDir());

        super.removeAll();
        super.add(createLayout(inputs, presenter, parentConsole), BorderLayout.CENTER);
    }

    private JPanel createLayout(CaseInput[] inputs, EditInputsTabPresenter presenter, EmfConsole parentConsole) {
        JPanel layout = new JPanel(new BorderLayout());

        layout.add(createFolderPanel(), BorderLayout.NORTH);
        layout.add(tablePanel(inputs, parentConsole), BorderLayout.CENTER);
        layout.add(controlPanel(presenter), BorderLayout.PAGE_END);

        return layout;
    }

    private JPanel tablePanel(CaseInput[] inputs, EmfConsole parentConsole) {
        tableData = new InputsTableData(inputs);
        changeables.addChangeable(tableData);
        selectModel = new SortFilterSelectModel(new EmfTableModel(tableData));

        tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(createSortFilterPanel(parentConsole), BorderLayout.CENTER);

        return tablePanel;
    }

    private JScrollPane createSortFilterPanel(EmfConsole parentConsole) {
        SortFilterSelectionPanel sortFilterPanel = new SortFilterSelectionPanel(parentConsole, selectModel);
        //sortFilterPanel.sort(sortCriteria());
        
        JScrollPane scrollPane = new JScrollPane(sortFilterPanel);
        sortFilterPanel.setPreferredSize(new Dimension(450, 60));
        return scrollPane;
    }
    
//    private SortCriteria sortCriteria() {
//        String[] columnNames = { "Sector", "Program", "Input"};
//        return new SortCriteria(columnNames, new boolean[] { true, true, true }, new boolean[] { false, false, false });
//    }

    public JPanel createFolderPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Input Folder:", getFolderChooserPanel(inputDir, "input folder"), panel);
        layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
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
        JPanel folderPanel = new JPanel(new BorderLayout());
        folderPanel.add(dir);
        folderPanel.add(browseButton, BorderLayout.EAST);

        return folderPanel;
    }

    private void selectFolder(JTextField dir, String title) {
        JFileChooser chooser = new JFileChooser(new File(dir.getText()));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Please select the " + title);

        int option = chooser.showDialog(this, "Select");
        if (option == JFileChooser.APPROVE_OPTION) {
            caseObj.setInputFileDir("" + chooser.getSelectedFile());
            dir.setText("" + chooser.getSelectedFile());
        }
    }

    private JPanel controlPanel(final EditInputsTabPresenter presenter) {
        Insets insets = new Insets(1,2,1,2);
        
        JPanel container = new JPanel();

        Button add = new AddButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMessage();
                doNewInput(presenter);
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

        Button edit = new EditButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    clearMessage();
                    doEditInput(presenter);
                } catch (EmfException ex) {
                    messagePanel.setError(ex.getMessage());
                }
            }
        });
        edit.setMargin(insets);
        container.add(edit);

        Button view = new ViewButton("View Dataset", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doDisplayInputDatasetsPropertiesViewer();
            }
        });
        view.setMargin(insets);
        container.add(view);

        final JCheckBox showAll = new JCheckBox("Show All", false);
        showAll.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                // doRefresh(showAll);
                clearMessage();
            }
        });
        showAll.setEnabled(false);
        container.add(showAll);
        
        Button export = new ExportButton("Export Inputs", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doExportInputDatasets(getSelectedInputs());
            }
        });
        export.setMargin(insets);
        container.add(export);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(container, BorderLayout.WEST);

        return panel;
    }

    protected void doNewInput(EditInputsTabPresenter presenter) {
        NewInputDialog view = new NewInputDialog(parentConsole);
        try {
            presenter.doAddInput(view);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    protected void doRemove(EditInputsTabPresenter presenter) throws EmfException {
        CaseInput[] inputs = (CaseInput[]) getSelectedInputs().toArray(new CaseInput[0]);

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
            notifychanges();
            presenter.removeInputs(inputs);
        }
    }

    public void notifychanges() {
        tableData.setChanges(true);
        tableData.notifyChanges();
    }

    private void doEditInput(EditInputsTabPresenter presenter) throws EmfException {
        List inputs = getSelectedInputs();

        if (inputs.size() == 0) {
            messagePanel.setMessage("Please select input(s) to edit.");
            return;
        }
        

        for (Iterator iter = inputs.iterator(); iter.hasNext();) {
            CaseInput input = (CaseInput) iter.next();
            EditCaseInputView inputEditor = new EditCaseInputWindow(input.getName() + "(" + input.getId() + ")("
                    + caseObj.getName() + ")", this, desktopManager);
            presenter.doEditInput(input, inputEditor);
        }
    }

    private void doDisplayInputDatasetsPropertiesViewer() {
        List datasets = getSelectedDatasets(getSelectedInputs());
        if (datasets.isEmpty()) {
            messagePanel.setMessage("Please select one or more inputs with datasets specified to view.");
            return;
        }
        for (Iterator iter = datasets.iterator(); iter.hasNext();) {
            DatasetPropertiesViewer view = new DatasetPropertiesViewer(parentConsole, desktopManager);
            EmfDataset dataset = (EmfDataset) iter.next();
            presenter.doDisplayPropertiesView(view, dataset);
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
        
        EmfDataset[] datasets = (EmfDataset[])getSelectedDatasets(inputlist).toArray(new EmfDataset[0]);
        int ok = checkOverWrite();
        
        try {
            if (ok != JOptionPane.YES_OPTION)
                presenter.doExport(datasets, getSelectedDatasetVersions(), getSelectedInputSubdirs(), "");
            else
                presenter.doExportWithOverwrite(datasets, getSelectedDatasetVersions(), getSelectedInputSubdirs(), "");

            messagePanel.setMessage("Started export of "+numberToExport+
                    " input datasets.  Please see the Status Window for additional information.");
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }
    
    private boolean checkExportDir(String exportDir) {
        if (exportDir == null || exportDir.equals("")) {
            messagePanel.setMessage("Please specify the input folder before export.");
            return false;
        }
        
        return true;
    }

    private boolean checkDatasets(List inputList) {
        CaseInput[] inputs = (CaseInput[])inputList.toArray(new CaseInput[0]);
        
        for (int i = 0; i < inputs.length; i++)
            if (inputs[i].isRequired() && inputs[i].getDataset() == null) {
                messagePanel.setMessage("Please specify a dataset for required input \"" + inputs[i].getName() + "\".");
                return false;
            }
        
        return true;
    }
    
    // returns the number of datasets that will actually be exported
    private int checkToWriteStartMessage(List inputList) {
        CaseInput[] inputs = (CaseInput[])inputList.toArray(new CaseInput[0]);
        int count = 0;
        int external = 0;
        
        for (int i = 0; i < inputs.length; i++) {
            DatasetType type = inputs[i].getDatasetType();
            EmfDataset dataset = inputs[i].getDataset();
            if (type != null && dataset != null  && !type.isExternal())
                count++;
            
            if (type != null && dataset != null  && type.isExternal())
                external++;
        }
        
        // this causes an error if there are any external.  We only want an error if they are all external
//        if (external > 0)
//        {
//            messagePanel.setMessage("Please select some inputs to export which do not use external datasets");
//            return false;
//        }
        
        if (count == 0) {
            messagePanel.setMessage("Please select some inputs to export (make sure they have datasets and are not all external)");
            return count;
        }
        
        return count;
    }

    private int checkOverWrite() {
        String title = "Message";
        String message = "Do you want to overwrite exported files if they already exist?";
        return JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
    }

    private List getSelectedDatasets(List inputlist) {
        List datasetList = new ArrayList();

        for (int i = 0; i < inputlist.size(); i++) {
            EmfDataset dataset = ((CaseInput) inputlist.get(i)).getDataset();
            if (dataset != null)
                datasetList.add(dataset);
        }

        return datasetList;
    }

    private Version[] getSelectedDatasetVersions() {
        List list = getSelectedInputs();
        List versionList = new ArrayList();
        
        for (int i = 0; i < list.size(); i++) {
            Version version = ((CaseInput) list.get(i)).getVersion();
            if (version != null)
                versionList.add(version);
        }
        
        return (Version[])versionList.toArray(new Version[0]);
    }

    private String[] getSelectedInputSubdirs() {
        List list = getSelectedInputs();
        List subDirList = new ArrayList();
        String defaultExportDir = session.preferences().outputFolder();
        if (!inputDir.getText().equals(""))
            defaultExportDir = inputDir.getText();
        
        for (int i = 0; i < list.size(); i++) {
            String subdir = ((CaseInput) list.get(i)).getSubdir();
            if (!subdir.equals(""))
                subDirList.add(defaultExportDir + File.separator + subdir);
            else
                subDirList.add(defaultExportDir);
        }
        
        return (String[])subDirList.toArray(new String[0]);
    }

    public void addInput(CaseInput note) {
        tableData.add(note);
        selectModel.refresh();

        tablePanel.removeAll();
        tablePanel.add(createSortFilterPanel(parentConsole));
    }
    
    private List getSelectedInputs() {
        return selectModel.selected();
    }

    public CaseInput[] caseInputs() {
        return tableData.sources();
    }

    public String getCaseInputFileDir() {
        return inputDir.getText();
    }

    public void refresh() {
        doRefresh(tableData.sources());
    }

    public void checkDuplicate(CaseInput input) throws EmfException {
        presenter.doCheckDuplicate(input, tableData.sources());
    }

    public int numberOfRecord() {
        return tableData.sources().length;
    }

    public void clearMessage() {
        messagePanel.clear();
    }

}
