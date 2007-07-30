package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.BrowseButton;
import gov.epa.emissions.commons.gui.buttons.ExportButton;
import gov.epa.emissions.commons.gui.buttons.ViewButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.cost.controlstrategy.AnalysisEngineTableApp;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesViewer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.EmfFileChooser;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;

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
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class EditControlStrategyOutputTab extends JPanel implements EditControlStrategyOutputTabView {

    private TextField folder;

    private EditControlStrategyOutputTabPresenter presenter;

    private ControlStrategy controlStrategy;

    private MessagePanel messagePanel;

    private DesktopManager desktopManager;

    private EmfConsole parentConsole;

    private SortFilterSelectModel selectModel;

//    private CheckBox inventoryCheckBox;

    private Button createButton;
    
    private EmfSession session;

    private JRadioButton detailButton;

    private JRadioButton invButton;

    private JRadioButton contInvButton;

    private ButtonGroup buttonGroup;

    public EditControlStrategyOutputTab(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults,
            MessagePanel messagePanel, DesktopManager desktopManager, EmfConsole parentConsole, EmfSession session) {
        super.setName("output");
        this.session = session;
        this.controlStrategy = controlStrategy;
        this.messagePanel = messagePanel;
        this.desktopManager = desktopManager;
        this.parentConsole = parentConsole;
        setLayout(controlStrategy, controlStrategyResults);
    }

    private void setLayout(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults) {
        setLayout(new BorderLayout());
        removeAll();
        add(outputPanel(controlStrategy, controlStrategyResults));
   //     add(bottomPanel(controlStrategyResults), BorderLayout.SOUTH);
    }

    public void save(ControlStrategy controlStrategy) {
        // TODO: output settings
    }

    public void observe(EditControlStrategyOutputTabPresenter presenter) {
        this.presenter = presenter;
    }

    public void export() {
        try {
            ControlStrategyResult[] controlStrategyResults = getSelectedDatasets();
            List<EmfDataset> datasetList = new ArrayList<EmfDataset>();
            for (int i = 0; i < controlStrategyResults.length; i++) {
                if (buttonGroup.getSelection().equals(detailButton.getModel())) {
                    datasetList.add((EmfDataset)controlStrategyResults[i].getDetailedResultDataset());
                } else if (buttonGroup.getSelection().equals(contInvButton.getModel())) {
                    if (controlStrategyResults[i].getControlledInventoryDataset() != null)
                        datasetList.add((EmfDataset)controlStrategyResults[i].getControlledInventoryDataset());
                }
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
                if (buttonGroup.getSelection().equals(detailButton.getModel())) {
                    datasetList.add((EmfDataset)controlStrategyResults[i].getDetailedResultDataset());
                } 
                else if (buttonGroup.getSelection().equals(contInvButton.getModel())) {
                    if (controlStrategyResults[i].getControlledInventoryDataset() != null)
                        datasetList.add((EmfDataset)controlStrategyResults[i].getControlledInventoryDataset());
                }
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
            ControlStrategyInputDataset[] inputDatasets = controlStrategy.getControlStrategyInputDatasets();
           // EmfDataset[] inputDatasets = controlStrategy.getInputDatasets();
            for (int i = 0; i < controlStrategyResults.length; i++) {
                EmfDataset inputDataset = null;
                for (int j = 0; j < inputDatasets.length; j++) {
                    if (inputDatasets[j].getInputDataset().getId() == controlStrategyResults[i].getInputDatasetId()) {
                        inputDataset = inputDatasets[j].getInputDataset();
                        break;
                    }
                }
                inputDataset=getInputDataset(controlStrategyResults[i].getInputDatasetId());               
                if (inputDataset != null)
                    presenter.doInventory(controlStrategy, inputDataset);
               // inputDatasets[i]=inputDataset;
            }
            messagePanel.setMessage(
                    "Creating controlled inventories. Watch the status window for progress and refresh this window after completion.");
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private EmfDataset getInputDataset(int datasetId) {
        ControlStrategyInputDataset[] controlStrategyInputDatasets = controlStrategy.getControlStrategyInputDatasets();
        EmfDataset inputDataset = null;
        for (int j = 0; j < controlStrategyInputDatasets.length; j++) {
            if (controlStrategyInputDatasets[j].getInputDataset().getId() == datasetId) {
                inputDataset = controlStrategyInputDatasets[j].getInputDataset();
                break;
            }
        }
        return inputDataset;
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
        outputPanel.add(folderPanel(), BorderLayout.NORTH);
        outputPanel.add(tablePanel);
        outputPanel.add(buttonPanel, BorderLayout.SOUTH);

        outputPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createTitledBorder("Results")));

        return outputPanel;
    }

    private JPanel tablePanel(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults) {
        ControlStrategyInputDataset[] controlStrategyInputDatasets = controlStrategy.getControlStrategyInputDatasets();
        ControlStrategyOutputTableData tableData = new ControlStrategyOutputTableData(controlStrategyInputDatasets, controlStrategyResults);
        EmfTableModel model = new EmfTableModel(tableData);
        selectModel = new SortFilterSelectModel(model);
        JTable table = new JTable(selectModel);
        JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(300, 150));
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(scrollPane);
        return tablePanel;
    }

    private JPanel buttonPanel() {
        Button exportButton = new ExportButton(exportAction());
        Button analysisButton = new Button("Analyze", analysisAction());
        Button view = new ViewButton("View", viewAction());
        createButton = new Button("Create", createOutputAction());
        createButton.setEnabled(false);
        
        detailButton = new JRadioButton("Detailed Result");
        detailButton.addActionListener(radioButtonAction());
        detailButton.setSelected(true);
        invButton = new JRadioButton("Input Inventory");
        invButton.addActionListener(radioButtonAction());
        contInvButton = new JRadioButton("Controlled Inventory");
        contInvButton.addActionListener(radioButtonAction());
        
        //Create logical relationship btween JradioButtons 
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
        buttonPanel.add(exportButton);
        buttonPanel.add(analysisButton);
        buttonPanel.add(view);
        buttonPanel.add(createButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        return mainPanel;
    }
    
    private Action radioButtonAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (buttonGroup.getSelection().equals(invButton.getModel()) ||buttonGroup.getSelection().equals(detailButton.getModel()) ){
                    createButton.setEnabled(false);
                    
                }
                else
                    createButton.setEnabled(true);   
            }
        };
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
        }
    }

    public void recentExportFolder(String recentfolder) {
        if (recentfolder != null)
            this.folder.setText(recentfolder);
    }

    public void displayAnalyzeTable(String controlStrategyName, String[] fileNames) {
        AnalysisEngineTableApp app = new AnalysisEngineTableApp("Analyze Control Strategy: " + controlStrategyName, new Dimension(500, 500), desktopManager, parentConsole);
        app.display(fileNames);
    }

    public void refresh(ControlStrategyResult[] controlStrategyResults) {
        setLayout(controlStrategy, controlStrategyResults);
    }

    private void viewDataSets() {
        ControlStrategyResult[] controlStrategyResults = getSelectedDatasets();
        if (controlStrategyResults.length == 0) {
            messagePanel.setError("Please select at least one item.");
            return;
        }

        int counter = 0;
        for (int i = 0; i < controlStrategyResults.length; i++) {
            DatasetPropertiesViewer view = new DatasetPropertiesViewer(parentConsole, desktopManager);
            if (buttonGroup.getSelection().equals(invButton.getModel())) {
                presenter.doDisplayPropertiesView(view, getInputDataset(controlStrategyResults[i].getInputDatasetId()));
                counter++;
            } 
            else if (buttonGroup.getSelection().equals(detailButton.getModel())) {
                presenter.doDisplayPropertiesView(view, (EmfDataset)controlStrategyResults[i].getDetailedResultDataset());
                counter++;
            } 
            else if (buttonGroup.getSelection().equals(contInvButton.getModel())) {
                if (controlStrategyResults[i].getControlledInventoryDataset() != null) {
                    presenter.doDisplayPropertiesView(view, (EmfDataset)controlStrategyResults[i].getControlledInventoryDataset());
                    counter++;
                }
            }
        }
        
        if (counter == 0) {
            messagePanel.setError("Please select at least one item.");
            return;
        }
    }

    private ControlStrategyResult[] getSelectedDatasets() {
        return selectModel.selected().toArray(new ControlStrategyResult[0]);
    }

}
