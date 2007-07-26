package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.gui.BorderlessButton;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.BrowseButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.EmfFileChooser;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.TrackableSortFilterSelectModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class EditControlStrategyInventoryFilterTab extends JPanel implements EditControlStrategyTabView {

    private ComboBox datasetTypeCombo;
    
//    private TextField datasetTextField;
    
    private TextField countyFileTextField;
    
    private VersionPanel versionPanel;
    
    private TextArea filter;
    
    private MessagePanel messagePanel;
    
    private ControlStrategy controlStrategy;
    
    private EmfSession session;
    
    protected EmfConsole parentConsole;

    private ManageChangeables changeablesList;

//    private EditableInputDatasetTableData inputDatasetsTableData;
//
//    private ControlStrategyInputDatasetsPanel inputDatasetsPanel;

    private ControlStrategyInputDatasetTableData tableData;

    private TrackableSortFilterSelectModel sortFilterSelectModel;

    private EmfTableModel tableModel;

    private JPanel mainPanel;

    public EditControlStrategyInventoryFilterTab(ControlStrategy controlStrategy, ManageChangeables changeablesList, 
            MessagePanel messagePanel, EmfConsole parentConsole, EmfSession session) {
        super.setName("csFilter");
        this.changeablesList = changeablesList;
        this.messagePanel = messagePanel;
        this.controlStrategy = controlStrategy;
        this.parentConsole = parentConsole;
        this.session = session;
        doLayout(controlStrategy);
    }

    private void doLayout(ControlStrategy controlStrategy) {
        tableData = new ControlStrategyInputDatasetTableData(controlStrategy.getInputDatasets());
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createMiddleSection(controlStrategy), BorderLayout.CENTER);
        
        setLayout(new BorderLayout(10, 10));
//        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(panel,BorderLayout.NORTH);
        // mainPanel.add(buttonPanel(), BorderLayout.SOUTH);
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildSortFilterPanel();
        add(mainPanel, BorderLayout.SOUTH);
//        add(createInputDatasetsPanel(controlStrategy.getInputDatasets()), BorderLayout.SOUTH);
    }

//    private JPanel createInputDatasetsPanel(EmfDataset[] inputDatasets) {
//        inputDatasetsTableData = new EditableInputDatasetTableData(controlStrategy.getInputDatasets());
//        inputDatasetsPanel = new ControlStrategyInputDatasetsPanel(inputDatasetsTableData, changeablesList, parentConsole);
//        inputDatasetsPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
//
//        return inputDatasetsPanel;
//    }

    private void buildSortFilterPanel() {
        mainPanel.removeAll();
        mainPanel.add(sortFilterPanel());
    }

    private JPanel sortFilterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(new JLabel("Inventories:"), BorderLayout.NORTH);

        tableModel = new EmfTableModel(tableData);
        sortFilterSelectModel = new TrackableSortFilterSelectModel(tableModel);
        changeablesList.addChangeable(sortFilterSelectModel);
        SortFilterSelectionPanel sortFilterSelectionPanel = new SortFilterSelectionPanel(parentConsole, sortFilterSelectModel);
        sortFilterSelectionPanel.setPreferredSize(new Dimension(625, 200));
        panel.add(sortFilterSelectionPanel);
        panel.add(buttonPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        Button addButton = new BorderlessButton("Add", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    addAction();
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        });
        panel.add(addButton);
        Button editButton = new BorderlessButton("Edit", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    editAction();
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        });
        panel.add(editButton);
        Button removeButton = new BorderlessButton("Remove", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                removeAction();
            }
        });
        panel.add(removeButton);

        JPanel container = new JPanel(new BorderLayout());
        container.add(panel, BorderLayout.LINE_START);

        return container;
    }

    private void addAction() throws EmfException {
        throw new EmfException("");
//        ControlMeasureSelectionView view = new ControlMeasureSelectionDialog(parent, changeablesList);
//        ControlMeasureSelectionPresenter presenter = new ControlMeasureSelectionPresenter(this, view, session,
//                this.presenter.getAllControlMeasures());
//        try {
//            presenter.display(view);
//        } catch (Exception exp) {
//            messagePanel.setError(exp.getMessage());
//        }
    }

    private void editAction() throws EmfException {
        throw new EmfException("");
//        ControlMeasureSelectionView view = new ControlMeasureSelectionDialog(parent, changeablesList);
//        ControlMeasureSelectionPresenter presenter = new ControlMeasureSelectionPresenter(this, view, session,
//                this.presenter.getAllControlMeasures());
//        try {
//            presenter.display(view);
//        } catch (Exception exp) {
//            messagePanel.setError(exp.getMessage());
//        }
    }

    protected void removeAction() {
        messagePanel.clear();
        List selected = sortFilterSelectModel.selected();

        if (selected.size() == 0) {
            messagePanel.setError("Please select an item to remove.");
            return;
        }

        EmfDataset[] inputDatasets = (EmfDataset[]) selected.toArray(new EmfDataset[0]);

        if (inputDatasets.length == 0)
            return;

        String title = "Warning";
        String message = "Are you sure you want to remove the selected row(s)?";
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            tableData.remove(inputDatasets);
            buildSortFilterPanel();
        }
    }


    private JPanel createMiddleSection(ControlStrategy controlStrategy) {
        JPanel middlePanel = new JPanel(new SpringLayout());

        String value = controlStrategy.getFilter();
        if (value == null)
            value = "";
        
        filter = new TextArea("filter", value, 40, 4);
        filter.setToolTipText("Enter a filter that could be entered as a SQL where clause (e.g., ANN_EMIS>5000)");
        JScrollPane scrollPane = new JScrollPane(filter);
        changeablesList.addChangeable(filter);
        
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
//        layoutGenerator.addLabelWidgetPair("Inventory Type:", datasetTypeCombo(controlStrategy), middlePanel);
//        layoutGenerator.addLabelWidgetPair("Inventory Dataset:", datasetPanel(), middlePanel);
//        layoutGenerator.addLabelWidgetPair("Dataset Version:", versionPanel(), middlePanel);
        layoutGenerator.addLabelWidgetPair("Inventory Filter:", scrollPane, middlePanel);
        layoutGenerator.addLabelWidgetPair("County File:", countyFilePanel(), middlePanel);

        layoutGenerator.makeCompactGrid(middlePanel, 2, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return middlePanel;
    }
    
//    private ComboBox datasetTypeCombo(ControlStrategy controlStrategy) throws EmfException {
//        DatasetType[] datasetTypes = getORLTypes();
//        datasetTypeCombo = new ComboBox("Choose an inventory type", datasetTypes);
//        datasetTypeCombo.setSelectedItem(controlStrategy.getDatasetType());
////        datasetTypeCombo.addActionListener(new AbstractAction() {
////            public void actionPerformed(ActionEvent e) {
////                messagePanel.setMessage("clear test field.");
////                datasetTextField.clear();
////                datasetTextField.repaint();
////            }
////        });
//        changeablesList.addChangeable(datasetTypeCombo);
//        return datasetTypeCombo;
//    }
    
//    private DatasetType[] getORLTypes() throws EmfException {
//        List orlTypes = new ArrayList();
//        DatasetType[] datasetTypes = session.dataCommonsService().getDatasetTypes();
//        for (int i = 0; i < datasetTypes.length; i++)
//            if (datasetTypes[i].getImporterClassName().indexOf("ORL") >= 0)
//                orlTypes.add(datasetTypes[i]);
//
//        return (DatasetType[]) orlTypes.toArray(new DatasetType[0]);
//    }
//    
//    private JPanel versionPanel() throws EmfException {
//        this.versionPanel = new VersionPanelWithoutLabel(controlStrategy, session, changeablesList);
//        return versionPanel;
//    }
//    
//    private JPanel datasetPanel() {
//        datasetTextField = new TextField("datasets", 40);
//        datasetTextField.setEditable(false);
//        datasetTextField.setText(selectedDatasets(controlStrategy.getInputDatasets()));
//        changeablesList.addChangeable(datasetTextField);
//
//        Button chooseButton = new Button("Choose", chooseDatasetAction());
//
//        JPanel panel = new JPanel(new BorderLayout(5, 5));
//        panel.add(datasetTextField);
//        panel.add(chooseButton, BorderLayout.EAST);
//        return panel;
//    }
    
    private JPanel countyFilePanel() {
        countyFileTextField = new TextField("countyFile", 40);
        countyFileTextField.setText(controlStrategy.getCountyFile());
        changeablesList.addChangeable(countyFileTextField);

        return getFileChooserPanel(countyFileTextField, "TITLE");
    }
    
    private JPanel getFileChooserPanel(final JTextField dir, final String title) {
        Button browseButton = new BrowseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                messagePanel.clear();
                selectFile();
            }
        });
        JPanel folderPanel = new JPanel(new BorderLayout(2,0));
        folderPanel.add(dir, BorderLayout.LINE_START);
        folderPanel.add(browseButton, BorderLayout.LINE_END);

        return folderPanel;
    }

    private void selectFile() {
        EmfFileInfo initDir = new EmfFileInfo(session.preferences().inputFolder(), true, true);
        
        EmfFileChooser chooser = new EmfFileChooser(initDir, new EmfFileSystemView(session.dataCommonsService()));
        chooser.setTitle("Select files containing counties to include in strategy");
        chooser.setDirectoryAndFileMode();
        
        int option = chooser.showDialog(parentConsole, "Select a file");

        EmfFileInfo[] files = (option == EmfFileChooser.APPROVE_OPTION) ? chooser.getSelectedFiles() : null;
//        EmfFileInfo dir = (option == EmfFileChooser.APPROVE_OPTION) ? chooser.getSelectedDir() : null;
//        
        if (files == null || files.length == 0)
            return;

        if (files.length > 1) {
            messagePanel.setError("Choose only one county file.");
        } else {
            countyFileTextField.setText(files[0].getAbsolutePath());
        }
    }

//    private Action chooseDatasetAction() {
//        return new AbstractAction() {
//            public void actionPerformed(ActionEvent e) {
//                try {
//                    DatasetType datasetType = selectedDatasetType();
//                    DatasetChooserDialog dialog = new DatasetChooserDialog(datasetType, session, parentConsole,
//                            EditControlStrategyInventoryFilterTab.this);
//                    dialog.show();
//                    EmfDataset dataset = dialog.dataset();
//                    if (dataset != null) {
//                        datasetTextField.setText(dataset.getName());
//                        controlStrategy.setInputDatasets(new EmfDataset[] { dataset });
//                        Version[] versions = session.dataEditorService().getVersions(dataset.getId());
//                        versionPanel.update(versions);
//                    }
//                } catch (EmfException exp) {
//                    messagePanel.setError(exp.getMessage());
//                }
//            }
//        };
//    }
    
    protected DatasetType selectedDatasetType() throws EmfException {
        DatasetType datasetType = (DatasetType) datasetTypeCombo.getSelectedItem();
        if (datasetType == null) {
            throw new EmfException("Please select an inventory type");
        }
        return datasetType;
    }

//    private String selectedDatasets(EmfDataset[] datasets) {
//        StringBuffer sb = new StringBuffer();
//        for (int i = 0; i < datasets.length - 1; i++) {
//            sb.append(datasets[i].getName() + "\n");
//        }
//        if (datasets.length > 0)
//            sb.append(datasets[datasets.length - 1].getName());
//
//        return sb.toString();
//    }

    public void save(ControlStrategy controlStrategy) throws EmfException {
        String value = filter.getText().trim();
        if (value.length() > 255)
            throw new EmfException("Filter Tab: The length of the sql filter should not exceed 255 characters.");

        controlStrategy.setDatasetType(selectedDatasetType());
        controlStrategy.setDatasetVersion(versionPanel.datasetVersion());
        controlStrategy.setFilter(value);
        controlStrategy.setCountyFile(countyFileTextField.getText().trim());
    }

    public void refresh(ControlStrategyResult[] controlStrategyResults) {
        // do nothing
    }
}