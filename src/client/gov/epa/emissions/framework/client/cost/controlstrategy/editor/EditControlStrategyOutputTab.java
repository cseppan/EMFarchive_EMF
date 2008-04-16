package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.BrowseButton;
import gov.epa.emissions.commons.gui.buttons.ExportButton;
import gov.epa.emissions.commons.gui.buttons.ViewButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.cost.controlstrategy.AnalysisEngineTableApp;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesEditor;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesViewer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.EmfFileChooser;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public class EditControlStrategyOutputTab extends JPanel implements EditControlStrategyOutputTabView {

    private TextField folder;

    private EditControlStrategyOutputTabPresenter presenter;

    private ControlStrategy controlStrategy;

    private MessagePanel messagePanel;

    private DesktopManager desktopManager;

    private EmfConsole parentConsole;

    private SortFilterSelectModel selectModel;

//    private CheckBox inventoryCheckBox;

    private Button analysisButton, view, exportButton, createButton, editButton;
    
    private EmfSession session;

    private JRadioButton detailButton;

    private JRadioButton invButton;

    private JRadioButton contInvButton;

    private ButtonGroup buttonGroup;

    private boolean creatingControlledInventories;

    public EditControlStrategyOutputTab(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults,
            MessagePanel messagePanel, DesktopManager desktopManager, EmfConsole parentConsole, EmfSession session) {
        super.setName("output");
        this.session = session;
        this.controlStrategy = controlStrategy;
        this.messagePanel = messagePanel;
        this.desktopManager = desktopManager;
        this.parentConsole = parentConsole;
//        setLayout(controlStrategy, controlStrategyResults);
    }

    public void display(ControlStrategy strategy, ControlStrategyResult[] controlStrategyResults) {
      setLayout(controlStrategy, controlStrategyResults);
    }

    private void setLayout(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults) {
//        //this is called for both the load and refresh window process, so i'll set the boolean flag here...
//        for (int i = 0; i < controlStrategyResults.length; i++) {
//            if (controlStrategyResults[i].getControlledInventoryDataset() != null) 
//            hasControlledInventories = true;
//        }
        
        setLayout(new BorderLayout());
        removeAll();
        add(outputPanel(controlStrategy, controlStrategyResults));
   //     add(bottomPanel(controlStrategyResults), BorderLayout.SOUTH);
    }

    public void save(ControlStrategy controlStrategy) {
        controlStrategy.setExportDirectory(folder.getText());
    }

    public void observe(EditControlStrategyOutputTabPresenter presenter) {
        this.presenter = presenter;
    }

    public void export() {
        try {
            ControlStrategyResult[] controlStrategyResults = getSelectedDatasets();
            List<EmfDataset> datasetList = new ArrayList<EmfDataset>();
            for (int i = 0; i < controlStrategyResults.length; i++) {
//                if (controlStrategyResults[i].getStrategyResultType().getName().equals(StrategyResultType.detailedStrategyResult)) {
                    if (buttonGroup.getSelection().equals(invButton.getModel())) {
                        if (controlStrategyResults[i].getInputDataset() != null)
                            datasetList.add(controlStrategyResults[i].getInputDataset());
                    } else if (buttonGroup.getSelection().equals(detailButton.getModel())) {
                        if (controlStrategyResults[i].getDetailedResultDataset() != null)
                            datasetList.add((EmfDataset)controlStrategyResults[i].getDetailedResultDataset());
                    } else if (buttonGroup.getSelection().equals(contInvButton.getModel())) {
                        if (controlStrategyResults[i].getControlledInventoryDataset() != null) {
                            datasetList.add((EmfDataset)controlStrategyResults[i].getControlledInventoryDataset());
                        } else if (controlStrategyResults[i].getStrategyResultType().getName().equals(StrategyResultType.controlledInventoryResult) && controlStrategyResults[i].getDetailedResultDataset() != null) {
                            datasetList.add((EmfDataset)controlStrategyResults[i].getDetailedResultDataset());
                        } else
                            messagePanel.setError("Please create controled inventory first.");
                    }
//                } else {//if (controlStrategyResults[i].getStrategyResultType().getName().equals(StrategyResultType.strategySummary)) {
//                    datasetList.add(controlStrategyResults[i].getInputDataset());
//                }
            }
            presenter.doExport(datasetList.toArray(new EmfDataset[0]), folder.getText());
            messagePanel.setMessage("Started Export. Please monitor the Status window to track your export request");
        } catch (EmfException e) {
            messagePanel.setMessage(e.getMessage());
        }
    }

    public void analyze() {
        try {
            ControlStrategyResult[] controlStrategyResults = getSelectedDatasets();
            List<EmfDataset> datasetList = new ArrayList<EmfDataset>();
            for (int i = 0; i < controlStrategyResults.length; i++) {
//                if (controlStrategyResults[i].getStrategyResultType().getName().equals(StrategyResultType.detailedStrategyResult)) {
                    if (buttonGroup.getSelection().equals(invButton.getModel())) {
                        if (controlStrategyResults[i].getInputDataset() != null)
                            datasetList.add(controlStrategyResults[i].getInputDataset());
                    } else if (buttonGroup.getSelection().equals(detailButton.getModel())) {
                        if (controlStrategyResults[i].getDetailedResultDataset() != null)
                            datasetList.add((EmfDataset)controlStrategyResults[i].getDetailedResultDataset());
                    } 
                    else if (buttonGroup.getSelection().equals(contInvButton.getModel())) {
                        if (controlStrategyResults[i].getControlledInventoryDataset() != null) {
                            datasetList.add((EmfDataset)controlStrategyResults[i].getControlledInventoryDataset());
                        } else if (controlStrategyResults[i].getStrategyResultType().getName().equals(StrategyResultType.controlledInventoryResult) && controlStrategyResults[i].getDetailedResultDataset() != null) {
                            datasetList.add((EmfDataset)controlStrategyResults[i].getDetailedResultDataset());
                        } else
                            messagePanel.setError("Please create controled inventory first.");
                    }
//                } else {//if (controlStrategyResults[i].getStrategyResultType().getName().equals(StrategyResultType.strategySummary)) {
//                    datasetList.add(controlStrategyResults[i].getInputDataset());
//                }
            }
            presenter.doAnalyze(controlStrategy.getName(), datasetList.toArray(new EmfDataset[0]));
        } catch (EmfException e) {
            messagePanel.setMessage(e.getMessage());
        }
    }

//    private JPanel bottomPanel(ControlStrategyResult[] controlStrategyResults) {
//        JPanel topPanel = new JPanel(new BorderLayout());
////        topPanel.add(productPanel());
////        topPanel.add(createButtonPanel(), BorderLayout.SOUTH);
//        disableTopPanel(controlStrategyResults);
//        topPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5),
//                BorderFactory.createTitledBorder("Outputs")));
//
//        return topPanel;
//    }

//    private void disableTopPanel(ControlStrategyResult[] controlStrategyResults) {
//        boolean enable = (controlStrategyResults == null) ? false : true;
//        if (enable)
//            return;
//        inventoryCheckBox.setEnabled(enable);
//        createButton.setEnabled(enable);
//    }

//    private JPanel createButtonPanel() {
//        createButton = new Button("Create", createOutputAction());
//        JPanel createPanel = new JPanel();
//        createPanel.add(createButton);
//        return createPanel;
//    }

    private Action createOutputAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doInventory();
            }

        };
        return action;
    }

    protected void doInventory() {
        try {
            ControlStrategyResult[] controlStrategyResults = getSelectedDatasets();
            if (controlStrategyResults.length == 0) {
                messagePanel.setError("Please select at least one item.");
                return;
            }
            if (controlStrategyResults.length == 1 && !controlStrategyResults[0].getStrategyResultType().getName().equals(StrategyResultType.detailedStrategyResult)) {
                messagePanel.setError("Please select at least one item that has a controlled inventory.");
                return;
            }
            //see if selected items can produce a controlled inventory.
            boolean hasControllableInventory = false;
            for (ControlStrategyResult result : controlStrategyResults) {
                if (result.getStrategyResultType().getName().equals(StrategyResultType.detailedStrategyResult)) {
                    hasControllableInventory = true;
                }
                if (hasControllableInventory) break;
            }
            if (!hasControllableInventory) {
                messagePanel.setError("Please select a detailed result in order to create a controlled inventory.");
                return;
            }
            //see if there is already a controlled inventory for this strategy.
            boolean hasControlledInventories = false;
            for (ControlStrategyResult result : controlStrategyResults) {
                if (result.getStrategyResultType().getName().equals(StrategyResultType.controlledInventoryResult)) {
                    hasControlledInventories = true;
                } else if (result.getStrategyResultType().getName().equals(StrategyResultType.detailedStrategyResult) && result.getControlledInventoryDataset() != null) {
                    hasControlledInventories = true;
                }
                if (hasControlledInventories) break;
            }
            //see if cont inv are already being created...
            if (creatingControlledInventories || hasControlledInventories) {
                String title = "Warning";
                String message = "Are you sure you want to create controlled inventories, there are controlled inventories that " + (creatingControlledInventories ? "are already being created" : "have already being created") + "?";
                int selection = JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                if (selection != JOptionPane.YES_OPTION) {
                    return;
                }
            }
//            //see if cont inv have already being created...
//            if (creatingControlledInventories) {
//                String title = "Warning";
//                String message = "Are you sure you want to create controlled inventories, there are controlled inventories that are already being created?";
//                int selection = JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.YES_NO_OPTION,
//                        JOptionPane.QUESTION_MESSAGE);
//
//                if (selection != JOptionPane.YES_OPTION) {
//                    return;
//                }
//            }
            presenter.doInventory(controlStrategy, controlStrategyResults);
            //flag to make sure the user doesn't click the button twice...
            creatingControlledInventories = true;
            
//            for (int i = 0; i < controlStrategyResults.length; i++) {
//                ControlStrategyInputDataset controlStrategyInputDataset = null;
//                controlStrategyInputDataset = getControlStrategyInputDataset(controlStrategyResults[i].getInputDatasetId());
//                if (controlStrategyInputDataset != null)
//            }
            messagePanel.setMessage(
                    "Creating controlled inventories. Watch the status window for progress and refresh this window after completion.");
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

//    private JPanel productPanel() {
//        JPanel productPanel = new JPanel();
//        inventoryCheckBox = new CheckBox("Controlled Inventory");
//        inventoryCheckBox.setSelected(true);
//        CheckBox summaryFIPS = new CheckBox("Custom Summaries");
//        summaryFIPS.setEnabled(false);
//        productPanel.add(inventoryCheckBox);
//        productPanel.add(summaryFIPS);
//        return productPanel;
//    }

    private JPanel folderPanel() {
        JLabel folderLabel = new JLabel("Export Folder: ");
        folder = new TextField("folderName", 30);
        String exportDirectory = controlStrategy.getExportDirectory();
        exportDirectory = (exportDirectory != null ? exportDirectory : presenter.folder());
        folder.setText(exportDirectory);
        
        Button browseButton = new BrowseButton(browseAction());

        JPanel panel = new JPanel();
        panel.add(folderLabel);
        panel.add(folder);
        panel.add(browseButton);

        return panel;
    }

    private JPanel outputPanel(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults) {
        JPanel tablePanel = tablePanel(controlStrategy, controlStrategyResults);
        JPanel buttonPanel = buttonPanel();

        JPanel outputPanel = new JPanel(new BorderLayout(5, 10));
        outputPanel.add(tablePanel);
        JPanel panel = new JPanel(new BorderLayout(5, 10));
        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(folderPanel(), BorderLayout.SOUTH);
        outputPanel.add(panel, BorderLayout.SOUTH);

        outputPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createTitledBorder("Output Datasets")));

        return outputPanel;
    }

    private JPanel tablePanel(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults) {
        ControlStrategyOutputTableData tableData = new ControlStrategyOutputTableData(controlStrategyResults);
        EmfTableModel model = new EmfTableModel(tableData);
        selectModel = new SortFilterSelectModel(model);
//        if (selectModel.getRowCount() == 1) selectModel.setValueAt(true, 0, 0);
        SortFilterSelectionPanel sortFilterSelectionPanel = new SortFilterSelectionPanel(parentConsole, selectModel);
        sortFilterSelectionPanel.setPreferredSize(new Dimension(625, 200));
        sortFilterSelectionPanel.sort(sortCriteria());
        selectModel.addTableModelListener(new TableModelListener() {

            public void tableChanged(TableModelEvent e) {
                toggleRadioButtons();
            }
        }
        );
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(sortFilterSelectionPanel);
        return tablePanel;
    }

    private SortCriteria sortCriteria() {
        String[] columnNames = { "Start Time" };
        return new SortCriteria(columnNames, new boolean[] { false }, new boolean[] { false });
    }
    private JPanel buttonPanel() {
        exportButton = new ExportButton(exportAction());
        analysisButton = new Button("Analyze", analysisAction());
        view = new ViewButton("View", viewAction());
        editButton = new Button("Edit", editAction());
        createButton = new Button("Create", createOutputAction());
        createButton.setEnabled(false);
//        editButton.setEnabled(false);
        
        detailButton = new JRadioButton("Result");
        detailButton.addActionListener(radioButtonAction());
        detailButton.setSelected(true);
        invButton = new JRadioButton("Input Inventory");
        invButton.addActionListener(radioButtonAction());
        contInvButton = new JRadioButton("Controlled Inventory");
        contInvButton.addActionListener(radioButtonAction());
        
        //Create logical relationship between JradioButtons 
        buttonGroup = new ButtonGroup();
        buttonGroup.add(invButton);     
        buttonGroup.add(detailButton);
        buttonGroup.add(contInvButton);
        
        JPanel radioPanel = new JPanel();
        radioPanel.add(invButton, radioButtonAction());
        radioPanel.add(detailButton, radioButtonAction());
        radioPanel.add(contInvButton);
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel();
        mainPanel.add(radioPanel, BorderLayout.NORTH);
        buttonPanel.add(view);
        buttonPanel.add(editButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(analysisButton);
        buttonPanel.add(createButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        return mainPanel;
    }
    
    private Action editAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                try {
                    edit();
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }       
        };
    }
    
    private void edit() throws EmfException {
        ControlStrategyResult[] controlStrategyResults = getSelectedDatasets();
        if (controlStrategyResults.length == 0) {
            messagePanel.setError("Please select at least one item.");
            return;
        }

        int counter = 0;
        for (int i = 0; i < controlStrategyResults.length; i++) {
            DatasetPropertiesEditor view = new DatasetPropertiesEditor(session, parentConsole, desktopManager);

//            if (controlStrategyResults[i].getStrategyResultType().getName().equals(StrategyResultType.detailedStrategyResult)) {
                if (buttonGroup.getSelection().equals(invButton.getModel())) {
                    if (controlStrategyResults[i].getInputDataset() != null) {
                        presenter.doDisplayPropertiesEditor(view, controlStrategyResults[i].getInputDataset());
                        counter++;
                    }
                } else if (buttonGroup.getSelection().equals(detailButton.getModel())) {
                    if (controlStrategyResults[i].getDetailedResultDataset() != null) {
                        presenter.doDisplayPropertiesEditor(view, (EmfDataset)controlStrategyResults[i].getDetailedResultDataset());
                        counter++;
                    }
                } else if (buttonGroup.getSelection().equals(contInvButton.getModel())) {
                    if (controlStrategyResults[i].getControlledInventoryDataset() != null) {
                        presenter.doDisplayPropertiesEditor(view, (EmfDataset)controlStrategyResults[i].getControlledInventoryDataset());
                        counter++;
                    } else if (controlStrategyResults[i].getStrategyResultType().getName().equals(StrategyResultType.controlledInventoryResult) && controlStrategyResults[i].getDetailedResultDataset() != null) {
                        presenter.doDisplayPropertiesEditor(view, (EmfDataset)controlStrategyResults[i].getDetailedResultDataset());
                        counter++;
                    }
               }
//            } else {//if (controlStrategyResults[i].getStrategyResultType().getName().equals(StrategyResultType.strategySummary)) {
//                presenter.doDisplayPropertiesEditor(view, controlStrategyResults[i].getInputDataset());
//            }
        }
    }

    
    private Action radioButtonAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                toggleRadioButtons();
            }
        };
    }

        private void toggleRadioButtons() {
        if (buttonGroup.getSelection().equals(invButton.getModel()) ||buttonGroup.getSelection().equals(detailButton.getModel()) ){
            createButton.setEnabled(false);
            view.setEnabled(true);
            analysisButton.setEnabled(true);
            exportButton.setEnabled(true);
            editButton.setEnabled(true);
            
//                editButton.setEnabled(false);
        }
        else if (buttonGroup.getSelection().equals(contInvButton.getModel())){
            ControlStrategyResult[] controlStrategyResults = getSelectedDatasets();

            
            
            //see if there is already a controlled inventory for this strategy.
            boolean hasControlledInventories = false;
            boolean hasControllableInventory = false;
            for (ControlStrategyResult result : controlStrategyResults) {
                if (result.getStrategyResultType().getName().equals(StrategyResultType.controlledInventoryResult)) {
                    hasControlledInventories = true;
                } else if (result.getStrategyResultType().getName().equals(StrategyResultType.detailedStrategyResult) && result.getControlledInventoryDataset() != null) {
                    hasControlledInventories = true;
                }
                if (result.getStrategyResultType().getName().equals(StrategyResultType.detailedStrategyResult)) {
                    hasControllableInventory = true;
                }
                if (hasControllableInventory && hasControlledInventories) break;
            }

            if (hasControllableInventory) 
                createButton.setEnabled(true);                
            else
                createButton.setEnabled(false);
            if (hasControlledInventories) {
                view.setEnabled(true);
                analysisButton.setEnabled(true);
                exportButton.setEnabled(true);
                editButton.setEnabled(true);
            } else {
                view.setEnabled(false);
                analysisButton.setEnabled(false);
                exportButton.setEnabled(false);
                editButton.setEnabled(false);
            }
        }
    }

    private Action exportAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
                export();
            }
        };
    }

    private Action analysisAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
                analyze();
            }
        };
    }

    private Action viewAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
                viewDataSets();
            }
        };
    }

    private Action browseAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                selectFolder();
            }
        };
    }

    private void selectFolder() {
        EmfFileInfo initDir = new EmfFileInfo(folder.getText(), true, true);
        
        EmfFileChooser chooser = new EmfFileChooser(initDir, new EmfFileSystemView(session.dataCommonsService()));
        chooser.setTitle("Select a folder to contain the exported strategy results");
        int option = chooser.showDialog(parentConsole, "Select a folder");

        EmfFileInfo file = (option == EmfFileChooser.APPROVE_OPTION) ? chooser.getSelectedDir() : null;
        if (file == null)
            return;

        if (file.isDirectory()) {
            folder.setText(file.getAbsolutePath());
            presenter.setLastFolder(file.getAbsolutePath());
        }
    }

//    public void recentExportFolder(String recentfolder) {
//        if (recentfolder != null)
//            folder.setText(recentfolder);
//    }

    public String getExportFolder() {
        return folder.getText();
    }
    public void displayAnalyzeTable(String controlStrategyName, String[] fileNames) {
        AnalysisEngineTableApp app = new AnalysisEngineTableApp("Analyze Control Strategy: " + controlStrategyName, new Dimension(500, 500), desktopManager, parentConsole);
        app.display(fileNames);
    }

    public void refresh(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults) {
        setLayout(controlStrategy, controlStrategyResults);
    }

    private void viewDataSets() {
        ControlStrategyResult[] controlStrategyResults = getSelectedDatasets();
        if (controlStrategyResults.length == 0) {
            messagePanel.setError("Please select at least one item.");
            return;
        }
         
        try{ 
            for (int i = 0; i < controlStrategyResults.length; i++) {
                DatasetPropertiesViewer view = new DatasetPropertiesViewer(session, parentConsole, desktopManager);
     
//                if (controlStrategyResults[i].getStrategyResultType().getName().equals(StrategyResultType.detailedStrategyResult)) {
                    if (buttonGroup.getSelection().equals(invButton.getModel())) {
                        if (controlStrategyResults[i].getInputDataset() != null) 
                            presenter.doDisplayPropertiesView(view, controlStrategyResults[i].getInputDataset());
                    } else if (buttonGroup.getSelection().equals(detailButton.getModel())) {
                        if (controlStrategyResults[i].getDetailedResultDataset() != null) 
                            presenter.doDisplayPropertiesView(view, (EmfDataset)controlStrategyResults[i].getDetailedResultDataset());
    
                    } else if (buttonGroup.getSelection().equals(contInvButton.getModel())) {
                        if (controlStrategyResults[i].getControlledInventoryDataset() != null) {
                            presenter.doDisplayPropertiesView(view, (EmfDataset)controlStrategyResults[i].getControlledInventoryDataset());
                        } else if (controlStrategyResults[i].getStrategyResultType().getName().equals(StrategyResultType.controlledInventoryResult) && controlStrategyResults[i].getDetailedResultDataset() != null) {
                            presenter.doDisplayPropertiesView(view, (EmfDataset)controlStrategyResults[i].getDetailedResultDataset());
                        }
                    }
//                } else {//if (controlStrategyResults[i].getStrategyResultType().getName().equals(StrategyResultType.strategySummary)) {
//                    presenter.doDisplayPropertiesView(view, controlStrategyResults[i].getInputDataset());
//                }
            }
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }
    

    private ControlStrategyResult[] getSelectedDatasets() {
        return selectModel.selected().toArray(new ControlStrategyResult[0]);
    }

    public void clearMsgPanel() {
        this.messagePanel.clear();
    }

    public void notifyStrategyTypeChange(StrategyType strategyType) {
        // NOTE Auto-generated method stub
        
    }

}
