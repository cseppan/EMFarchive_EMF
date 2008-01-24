package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.BorderlessButton;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.EmptyStrings;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionDialog;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionPresenter;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionView;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesViewer;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

public class EditControlStrategyInventoryFilterTab extends JPanel implements EditControlStrategyTabView {

//    private TextField countyFileTextField;
    
    private TextArea filter;
    
    private MessagePanel messagePanel;
    
    private ControlStrategy controlStrategy;
    
    private EmfSession session;
    
    protected EmfConsole parentConsole;

    private ManageChangeables changeablesList;

    private ControlStrategyInputDatasetTableData tableData;

    private SortFilterSelectModel sortFilterSelectModel;

    private EmfTableModel tableModel;

    private JPanel mainPanel;

    private DesktopManager desktopManager;
    
    private EditControlStrategyPresenter editControlStrategyPresenter;
    
    private ComboBox version, dataset;
    
    public EditControlStrategyInventoryFilterTab(ControlStrategy controlStrategy, ManageChangeables changeablesList, 
            MessagePanel messagePanel, EmfConsole parentConsole, 
            EmfSession session, DesktopManager desktopManager,
            EditControlStrategyPresenter editControlStrategyPresenter) throws EmfException {
        super.setName("csFilter");
        this.changeablesList = changeablesList;
        this.messagePanel = messagePanel;
        this.controlStrategy = controlStrategy;
        this.parentConsole = parentConsole;
        this.session = session;
        this.desktopManager = desktopManager;
        this.editControlStrategyPresenter = editControlStrategyPresenter;
        doLayout(controlStrategy.getControlStrategyInputDatasets());
    }

    private void doLayout(ControlStrategyInputDataset[] controlStrategyInputDatasets) throws EmfException {
        tableData = new ControlStrategyInputDatasetTableData(controlStrategyInputDatasets);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createMiddleSection(controlStrategy), BorderLayout.CENTER);
        
        setLayout(new BorderLayout(5, 5));
        add(panel,BorderLayout.SOUTH);
        // mainPanel.add(buttonPanel(), BorderLayout.SOUTH);
        mainPanel = new JPanel(new BorderLayout(10, 10));
        buildSortFilterPanel();
        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.add(mainPanel, BorderLayout.CENTER);
    }

    private void buildSortFilterPanel() {
        mainPanel.removeAll();
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new Border("Inventories to Process"));
        SortFilterSelectionPanel sfpanel = sortFilterPanel();
        panel.add(sfpanel, BorderLayout.CENTER);
        panel.add(buttonPanel(), BorderLayout.SOUTH);
        mainPanel.add(panel);
    }

    private SortFilterSelectionPanel sortFilterPanel() {
        tableModel = new EmfTableModel(tableData);
        sortFilterSelectModel = new SortFilterSelectModel(tableModel);
        //changeablesList.addChangeable(sortFilterSelectModel);
        SortFilterSelectionPanel sortFilterSelectionPanel = new SortFilterSelectionPanel(parentConsole, sortFilterSelectModel);
        sortFilterSelectionPanel.setPreferredSize(new Dimension(625, 200));
        return sortFilterSelectionPanel;
    }

    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        Button addButton = new BorderlessButton("Add", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    addAction();
                } catch (EmfException e) {
                    // NOTE Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        panel.add(addButton);
        Button editButton = new BorderlessButton("Set Version", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                    setVersionAction();
            }
        });
        panel.add(editButton);
        Button removeButton = new BorderlessButton("Remove", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {           
                    removeAction();
            }
        });
        panel.add(removeButton);
        Button viewButton = new BorderlessButton("View", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    viewAction();
                } catch (EmfException e) {
                    messagePanel.setError("Error viewing dataset: " + e.getMessage());
                }
            }
        });
        panel.add(viewButton);

        JPanel container = new JPanel(new BorderLayout());
        container.add(panel, BorderLayout.LINE_START);

        return container;
    }

    private void addAction() throws EmfException {
        InputDatasetSelectionView view = new InputDatasetSelectionDialog(parentConsole, changeablesList);
        InputDatasetSelectionPresenter presenter = new InputDatasetSelectionPresenter(view, session,
                new DatasetType[] { 
                    editControlStrategyPresenter.getDatasetType("ORL Nonpoint Inventory (ARINV)"),
                    editControlStrategyPresenter.getDatasetType("ORL Nonroad Inventory (ARINV)"),
                    editControlStrategyPresenter.getDatasetType("ORL Onroad Inventory (MBINV)"),
                    editControlStrategyPresenter.getDatasetType("ORL Point Inventory (PTINV)"),
                    editControlStrategyPresenter.getDatasetType("ORL CoST Point Inventory (PTINV)")
                });
        try {
            presenter.display(null);
            EmfDataset[] inputDatasets = presenter.getDatasets();
            ControlStrategyInputDataset[] controlStrategyInputDatasets = new ControlStrategyInputDataset[inputDatasets.length];
            for (int i = 0; i < inputDatasets.length; i++) {
                controlStrategyInputDatasets[i] = new ControlStrategyInputDataset(inputDatasets[i]);
                controlStrategyInputDatasets[i].setVersion(inputDatasets[i].getDefaultVersion());
            }
            tableData.add(controlStrategyInputDatasets);
            if (inputDatasets.length > 0) editControlStrategyPresenter.fireTracking();
            buildSortFilterPanel();
        } catch (Exception exp) {
            messagePanel.setError(exp.getMessage());
        }
    }

    private void setVersionAction(){
        messagePanel.clear();
        //get a single selected item
        List selected = sortFilterSelectModel.selected();
        if (selected.size() != 1) {
            messagePanel.setMessage("Please select a single item to update.");
            return;
        }
        ControlStrategyInputDataset[] controlStrategyInputDatasets = (ControlStrategyInputDataset[]) selected.toArray(new ControlStrategyInputDataset[0]);

        EmfDataset dataset=controlStrategyInputDatasets[0].getInputDataset();
        //Show select version dialog
        CSInventoryEditDialog dialog=new CSInventoryEditDialog(parentConsole, dataset, editControlStrategyPresenter, this);
        dialog.run();
        
    }
    
    public void editVersion(Version version, EmfDataset dataset) {
        messagePanel.clear();
        //get all measures
        ControlStrategyInputDataset[] datasets =tableData.sources();
        //get versions of selected item
        if (version != null) {
            //validate value
            
            //only update items that have been selected          
            for (int j = 0; j < datasets.length; j++) {
                if (dataset.equals(datasets[j].getInputDataset())) {
                    datasets[j].setVersion(version.getVersion());
                }
            }
            //repopulate the tabe data
            tableData = new ControlStrategyInputDatasetTableData(datasets);
            //rebuild the sort filter panelControlStrategyInputDatasetTableData
            buildSortFilterPanel();
        }
        
    }

    private void fillVersions(EmfDataset dataset) throws EmfException{
        version.setEnabled(true);

        if (dataset != null && dataset.getName().equals("None")) dataset = null;
        Version[] versions = editControlStrategyPresenter.getVersions(dataset);
        version.removeAllItems();
        version.setModel(new DefaultComboBoxModel(versions));
        version.revalidate();
        if (versions.length > 0)
            version.setSelectedIndex(getDefaultVersionIndex(versions, dataset));

    }
    
    private int getDefaultVersionIndex(Version[] versions, EmfDataset dataset) {
        int defaultversion = dataset.getDefaultVersion();

        for (int i = 0; i < versions.length; i++)
            if (defaultversion == versions[i].getVersion())
                return i;

        return 0;
    }
 
    protected void removeAction() {
        messagePanel.clear();
        List selected = sortFilterSelectModel.selected();

        if (selected.size() == 0) {
            messagePanel.setError("Please select an item to remove.");
            return;
        }

        ControlStrategyInputDataset[] controlStrategyInputDatasets = (ControlStrategyInputDataset[]) selected.toArray(new ControlStrategyInputDataset[0]);

        if (controlStrategyInputDatasets.length == 0)
            return;

        String title = "Warning";
        String message = "Are you sure you want to remove the selected row(s)?";
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            tableData.remove(controlStrategyInputDatasets);
            if (controlStrategyInputDatasets.length > 0) editControlStrategyPresenter.fireTracking();
            buildSortFilterPanel();
        }
    }

    protected void viewAction() throws EmfException {
        messagePanel.clear();
        List selected = sortFilterSelectModel.selected();

        if (selected.size() == 0) {
            messagePanel.setError("Please select an item to view.");
            return;
        }

        for (int i = 0; i < selected.size(); i++) {
            PropertiesViewPresenter presenter = new PropertiesViewPresenter(
                    editControlStrategyPresenter.getDataset(((ControlStrategyInputDataset)selected.get(i)).getInputDataset().getId()), session);
            DatasetPropertiesViewer view = new DatasetPropertiesViewer(session, parentConsole, desktopManager);
            presenter.doDisplay(view);
        }
    }


    private JPanel createMiddleSection(ControlStrategy controlStrategy) throws EmfException {
        JPanel middlePanel = new JPanel(new SpringLayout());
        middlePanel.setBorder(new Border("Filters"));

        String value = controlStrategy.getFilter();
        if (value == null)
            value = "";
        
        filter = new TextArea("filter", value, 40, 2);
        filter.setToolTipText("Enter a filter that could be entered as a SQL where clause (e.g., ANN_EMIS>5000 and SCC like '30300%')");
        JScrollPane scrollPane = new JScrollPane(filter);
        changeablesList.addChangeable(filter);
        
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        layoutGenerator.addLabelWidgetPair("Inventory Filter:", scrollPane, middlePanel);

        EmfDataset[] datasets = editControlStrategyPresenter.getDatasets( editControlStrategyPresenter.getDatasetType("List of Counties (CSV)") );
        String width = EmptyStrings.create(80);
//        Dimension size=new Dimension(500, 13);
        
        dataset = new ComboBox("Not selected", datasets);
        dataset.setPrototypeDisplayValue(width);
        if (controlStrategy.getCountyDataset() != null) dataset.setSelectedItem(controlStrategy.getCountyDataset());
//        dataset.setPrototypeDisplayValue(size);
//        dataset.setToolTipText("Browse to find a CSV file with a column named FIPS that lists the counties to which the strategy should apply");

        dataset.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    fillVersions((EmfDataset) dataset.getSelectedItem());
                } catch (EmfException e1) {
                    // NOTE Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });

        version =new ComboBox(new Version[0]);           
        version.setPrototypeDisplayValue(width);
        try {
            fillVersions((EmfDataset) dataset.getSelectedItem());
        } catch (EmfException e1) {
            // NOTE Auto-generated catch block
            e1.printStackTrace();
        }
        if (controlStrategy.getCountyDataset() != null) version.setSelectedItem(controlStrategy.getCountyDatasetVersion());
        
        layoutGenerator.addLabelWidgetPair("County Dataset:", dataset, middlePanel);
        layoutGenerator.addLabelWidgetPair("County Dataset Version:", version, middlePanel);

        layoutGenerator.makeCompactGrid(middlePanel, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 5);// xPad, yPad

        return middlePanel;
    }
    
    public void save(ControlStrategy controlStrategy) throws EmfException {
        String value = filter.getText().trim();
        if (value.length() > 255)
            throw new EmfException("Filter Tab: The length of the sql filter should not exceed 255 characters.");

        controlStrategy.setFilter(value);
//        controlStrategy.setCountyFile(countyFileTextField.getText().trim());
        EmfDataset ds =(EmfDataset) dataset.getSelectedItem();
        if (ds == null) {
            ds = null;
        }
        controlStrategy.setCountyDataset(ds);
        Version ver = (ds !=null ? (Version) version.getSelectedItem(): null);
        Integer verValue = (ver !=null? ver.getVersion(): null);
        controlStrategy.setCountyDatasetVersion(verValue);

        
        ControlStrategyInputDataset[] inputDatasets = {};
        if (tableData != null) {
            inputDatasets = new ControlStrategyInputDataset[tableData.rows().size()];
            for (int i = 0; i < tableData.rows().size(); i++) {
                inputDatasets[i] = (ControlStrategyInputDataset)tableData.element(i);
            }
            controlStrategy.setControlStrategyInputDatasets(inputDatasets);
        }
    }

    public void refresh(ControlStrategyResult[] controlStrategyResults) {
        // do nothing
    }

}