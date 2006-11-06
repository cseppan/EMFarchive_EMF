package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.CheckBox;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.BrowseButton;
import gov.epa.emissions.commons.gui.buttons.ExportButton;
import gov.epa.emissions.commons.gui.buttons.ViewButton;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.cost.controlstrategy.AnalysisEngineTableApp;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesViewer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.FileChooser;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
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

    private CheckBox inventoryCheckBox;

    private Button createButton;

    public EditControlStrategyOutputTab(ControlStrategy controlStrategy, ControlStrategyResult controlStrategyResults,
            MessagePanel messagePanel, DesktopManager desktopManager, EmfConsole parentConsole) {
        super.setName("output");
        this.controlStrategy = controlStrategy;
        this.messagePanel = messagePanel;
        this.desktopManager = desktopManager;
        this.parentConsole = parentConsole;
        setLayout(controlStrategy, controlStrategyResults);
    }

    private void setLayout(ControlStrategy controlStrategy, ControlStrategyResult controlStrategyResults) {
        setLayout(new BorderLayout());
        removeAll();
        add(outputPanel(controlStrategy, controlStrategyResults));
        add(bottomPanel(controlStrategyResults), BorderLayout.SOUTH);
    }

    public void save(ControlStrategy controlStrategy) {
        // TODO: output settings
    }

    public void observe(EditControlStrategyOutputTabPresenter presenter) {
        this.presenter = presenter;
    }

    public void export() {
        try {
            EmfDataset[] datasets = getSelectedDatasets();
            presenter.doExport(datasets, folder.getText());
            messagePanel.setMessage("Started Export. Please monitor the Status window to track your export request");
        } catch (EmfException e) {
            messagePanel.setMessage(e.getMessage());
        }
    }

    public void analyze() {
        try {
            List list = selectModel.selected();
            EmfDataset[] datasets = (EmfDataset[]) list.toArray(new EmfDataset[0]);
            presenter.doAnalyze(controlStrategy.getName(), datasets);
        } catch (EmfException e) {
            messagePanel.setMessage(e.getMessage());
        }
    }

    private JPanel bottomPanel(ControlStrategyResult controlStrategyResults) {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(productPanel());
        topPanel.add(createButtonPanel(), BorderLayout.SOUTH);
        disableTopPanel(controlStrategyResults);
        topPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createTitledBorder("Outputs")));

        return topPanel;
    }

    private void disableTopPanel(ControlStrategyResult controlStrategyResult) {
        boolean enable = (controlStrategyResult == null) ? false : true;
        if (enable)
            return;
        inventoryCheckBox.setEnabled(enable);
        createButton.setEnabled(enable);
    }

    private JPanel createButtonPanel() {
        createButton = new Button("Create", createOutputAction());
        JPanel createPanel = new JPanel();
        createPanel.add(createButton);
        return createPanel;
    }

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
            presenter.doInventory(controlStrategy);
            messagePanel.setMessage(
                    "Creating controlled inventory. Watch the status window for progress and refresh this window after completion.");
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private JPanel productPanel() {
        JPanel productPanel = new JPanel();
        inventoryCheckBox = new CheckBox("Controlled Inventory");
        inventoryCheckBox.setSelected(true);
        CheckBox summaryFIPS = new CheckBox("Custom Summaries");
        summaryFIPS.setEnabled(false);
        productPanel.add(inventoryCheckBox);
        productPanel.add(summaryFIPS);
        return productPanel;
    }

    private JPanel folderPanel() {
        JLabel folderLabel = new JLabel("Folder: ");
        folder = new TextField("folderName", 30);

        Button browseButton = new BrowseButton(browseAction());

        JPanel panel = new JPanel();
        panel.add(folderLabel);
        panel.add(folder);
        panel.add(browseButton);

        return panel;
    }

    private JPanel outputPanel(ControlStrategy controlStrategy, ControlStrategyResult controlStrategyResults) {
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

    private JPanel tablePanel(ControlStrategy controlStrategy, ControlStrategyResult controlStrategyResult) {
        EmfDataset[] inputDatasets = controlStrategy.getInputDatasets();
        ControlStrategyResult result = (controlStrategyResult != null) ? controlStrategyResult
                : new ControlStrategyResult();
        ControlStrategyOutputTableData tableData = new ControlStrategyOutputTableData(inputDatasets, result);
        EmfTableModel model = new EmfTableModel(tableData);
        selectModel = new SortFilterSelectModel(model);
        JTable table = new JTable(selectModel);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(300, 200));
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(scrollPane);
        return tablePanel;
    }

    private JPanel buttonPanel() {
        Button exportButton = new ExportButton(exportAction());
        Button analysisButton = new Button("Analyze", analysisAction());
        Button view = new ViewButton("View", viewAction());
        view.setToolTipText("View output datasets.");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(exportButton);
        buttonPanel.add(analysisButton);
        buttonPanel.add(view);
        return buttonPanel;
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
        FileChooser chooser = new FileChooser("Select Folder", new File(folder.getText()),
                EditControlStrategyOutputTab.this);

        chooser.setTitle("Select a folder");
        File[] files = chooser.choose();
        if (files == null)
            return;

        if (files[0].isDirectory()) {
            folder.setText(files[0].getAbsolutePath());
        }

        if (files[0].isFile()) {
            folder.setText(files[0].getParent());
        }
    }

    public void recentExportFolder(String recentfolder) {
        if (recentfolder != null)
            this.folder.setText(recentfolder);
    }

    public void displayAnalyzeTable(String controlStrategyName, String[] fileNames) {
        AnalysisEngineTableApp app = new AnalysisEngineTableApp(controlStrategyName, desktopManager, parentConsole);
        app.display(fileNames);
    }

    public void refresh(ControlStrategyResult controlStrategyResult) {
        setLayout(controlStrategy, controlStrategyResult);
    }

    private void viewDataSets() {
        EmfDataset[] datasets = getSelectedDatasets();
        if (datasets.length == 0) {
            messagePanel.setMessage("Please select at least one item.");
            return;
        }

        for (int i = 0; i < datasets.length; i++) {
            DatasetPropertiesViewer view = new DatasetPropertiesViewer(parentConsole, desktopManager);
            presenter.doDisplayPropertiesView(view, datasets[i]);
        }
    }

    private EmfDataset[] getSelectedDatasets() {
        List list = selectModel.selected();
        EmfDataset[] datasets = (EmfDataset[]) list.toArray(new EmfDataset[0]);

        return datasets;
    }

}
