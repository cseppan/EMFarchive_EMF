package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.CopyButton;
import gov.epa.emissions.commons.gui.buttons.EditButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureService;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class ControlMeasureEfficiencyTab extends JPanel implements ControlMeasureEfficiencyTabView, Runnable {

    private SortFilterSelectModel selectModel;

    private EmfConsole parentConsole;

    private EmfTableModel model;

    private ControlMeasureEfficiencyTableData tableData;

    private JPanel mainPanel;

    private ControlMeasure measure;

    private DesktopManager desktopManager;

    private ManageChangeables changeablesList;

    private MessagePanel messagePanel;

    private EmfSession session;
    
    private EfficiencyRecord[] efficiencyRecords = {};
    private volatile Thread populateThread;
    private ControlMeasureService cmService;
    private ControlMeasurePresenter controlMeasurePresenter;

//    private TextArea sortOrder; 
    private TextArea rowFilter; 
    private TextField recordLimit; 

    private CostYearTable costYearTable;
    int _recordLimit = 100;
    String _rowFilter = "";
    
    public ControlMeasureEfficiencyTab(ControlMeasure measure, ManageChangeables changeablesList, EmfConsole parent,
            EmfSession session, DesktopManager desktopManager, MessagePanel messagePanel, ControlMeasureView editControlMeasureWindowView,
            ControlMeasurePresenter controlMeasurePresenter, CostYearTable costYearTable) {
        this.mainPanel = new JPanel(new BorderLayout());
        this.parentConsole = parent;
        this.changeablesList = changeablesList;
        this.desktopManager = desktopManager;
        this.session = session;
        this.messagePanel = messagePanel;
        this.measure = measure;
        this.controlMeasurePresenter = controlMeasurePresenter;
        this.costYearTable = costYearTable;
        doLayout(measure);
        cmService = session.controlMeasureService();
        this.populateThread = new Thread(this);
        populateThread.start();
    }

    public void run() {
        try {
            if (measure.getId() != 0) {
                messagePanel.setMessage("Please wait while retrieving all efficiency records...");
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                efficiencyRecords = getEfficiencyRecords(measure.getId());
//                doLayout(measure);
                updateMainPanel(efficiencyRecords);                
                messagePanel.clear();
            }
        } catch (Exception e) {
            messagePanel.setError("Cannot retrieve all efficiency records.  " + e.getMessage());
        } finally  {
            setCursor(Cursor.getDefaultCursor());
            this.populateThread = null;
        }
    }

    private void doLayout(ControlMeasure measure) {
        updateMainPanel(efficiencyRecords);

        setLayout(new BorderLayout());
        add(sortFilterPanel(), BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(controlPanel(), BorderLayout.SOUTH);
    }

    private void updateMainPanel(EfficiencyRecord[] records) {
        mainPanel.removeAll();
        initModel(records);
        mainPanel.add(sortFilterPane(parentConsole));
        mainPanel.validate();
    }

    private void initModel(EfficiencyRecord[] records) {
        tableData = new ControlMeasureEfficiencyTableData(records);
        model = new EmfTableModel(tableData);
        selectModel = new SortFilterSelectModel(model);
    }

    private SortFilterSelectionPanel sortFilterPane(EmfConsole parentConsole) {
        SortFilterSelectionPanel panel = new SortFilterSelectionPanel(parentConsole, selectModel);
        panel.getTable().setName("controlMeasureEfficiencyTable");
        panel.sort(sortCriteria());
        panel.setPreferredSize(new Dimension(450, 120));

        return panel;
    }

//    private JScrollPane sortFilterPane(EmfConsole parentConsole) {
//        SortFilterSelectionPanel panel = new SortFilterSelectionPanel(parentConsole, selectModel);
//        panel.getTable().setName("controlMeasureEfficiencyTable");
//        panel.sort(sortCriteria());
//        panel.setPreferredSize(new Dimension(450, 120));
//
//        return new JScrollPane(panel);
//    }
//
    private SortCriteria sortCriteria() {
        String[] columnNames = { "Pollutant", "Control Efficiency" };
        return new SortCriteria(columnNames, new boolean[] { true, true }, new boolean[] { true, true });
    }

    private JPanel controlPanel() {
        Button addButton = new AddButton(addAction());
        Button editButton = new EditButton(editAction());
        Button copyButton = new CopyButton(copyAction());
        Button removeButton = new RemoveButton(removeAction());
        copyButton.setEnabled(false);

        JPanel panel = new JPanel();
        panel.add(addButton);
        panel.add(editButton);
        panel.add(copyButton);
        panel.add(removeButton);

        JPanel container = new JPanel(new BorderLayout());
        container.add(panel, BorderLayout.LINE_START);

        return container;
    }

    private Action editAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                List list = selectModel.selected();
                
                if (list.size() == 0) {
                    messagePanel.setError("Please select an item to edit.");
                    return;
                }
                
                EfficiencyRecord[] records = (EfficiencyRecord[]) list.toArray(new EfficiencyRecord[0]);

                for (int i = 0; i < records.length; i++) {
                    doEdit(records[i]);
                }

            }
        };
    }

    private Action addAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //if this is a new measure, make sure we save it before we proceed.
                //the efficiency editor needs to have a measure in the db first!
                if (measure.getId() == 0) {
                    try {
                        controlMeasurePresenter.doSave();
                    } catch (EmfException e1) {
                        messagePanel.setError("Cannot save control measure: " + e1.getMessage());
                        return;
                    }
                }

                doAdd();
            }
        };
    }

    private Action copyAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                List list = selectModel.selected();
                EfficiencyRecord[] records = (EfficiencyRecord[]) list.toArray(new EfficiencyRecord[0]);
                for (int i = 0; i < records.length; i++) {
                    doCopy(records[i]);
                }
            }
        };
    }

    private Action removeAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doRemove();
            }
        };
    }

    protected void doRemove() {
        messagePanel.clear();

        EfficiencyRecord[] records = getSelectedRecords();

        if (records.length == 0) {
            messagePanel.setError("Please select an item to remove.");
            return;
        }

        String title = "Warning";
        String message = "Are you sure you want to remove the selected row(s)?";
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            for (int i = 0; i < records.length; i++) {
                try {
                    cmService.removeEfficiencyRecord(records[i].getId());
                } catch (EmfException e) {
                    messagePanel.setMessage("Could not remove efficiency records: " + e.getMessage());
                }
            }
            modify();
            tableData.remove(records);
            refreshPanel();
        }
    }

    protected void doAdd() {
        messagePanel.clear();
        NewEfficiencyRecordView view = new NewEfficiencyRecordWindow(changeablesList, desktopManager, session, costYearTable);
        NewEfficiencyRecordPresenter presenter = new NewEfficiencyRecordPresenter(this, view, session, measure);
        
        presenter.display(measure);
    }

    protected void doCopy(EfficiencyRecord record) {
        messagePanel.clear();
        messagePanel.setMessage("Copy functionality is not implemented yet");
    }

    private void doEdit(EfficiencyRecord record) {
        messagePanel.clear();
        EditEfficiencyRecordView view = new EditEfficiencyRecordWindow(changeablesList, desktopManager, session, costYearTable);
        EditEfficiencyRecordPresenter presenter = new EditEfficiencyRecordPresenter(this, view, session, measure);
        presenter.display(measure, record);
    }

    private EfficiencyRecord[] getSelectedRecords() {
        return selectModel.selected().toArray(new EfficiencyRecord[0]);
    }

    public void refreshData() {
        tableData.refresh();
    }

    public void refreshPanel() {
        refreshData();
        updateMainPanel(tableData.sources());
    }

    public void save(ControlMeasure measure) {
        messagePanel.clear();
//        measure.setEfficiencyRecords(tableData.sources());
    }

    public void add(EfficiencyRecord record) {
        tableData.add(record);
        refreshPanel();
        modify();
    }

    public void update(EfficiencyRecord record) {
        refreshPanel();
        modify();
    }

    public void refresh() {
        refreshPanel();
    }

    public EfficiencyRecord[] records() {
       return tableData.sources();
        
    }

    private EfficiencyRecord[] getEfficiencyRecords(int controlMeasureId) throws EmfException {
        return cmService.getEfficiencyRecords(controlMeasureId, _recordLimit, _rowFilter);
    }

    public void refresh(ControlMeasure measure) {
        this.measure = measure;
    }

    public void modify() {
        controlMeasurePresenter.doModify();
    }
    

    private JPanel sortFilterPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setLayout(new BorderLayout(5, 5));

        JPanel container = new JPanel(new BorderLayout(5, 5));
//        container.setLayout(new GridLayout(2, 1, 5, 5));

        container.add(recordLimitPanel(), BorderLayout.NORTH);
        container.add(rowFilterPanel(), BorderLayout.CENTER);

        panel.add(container, BorderLayout.CENTER);
//        panel.add(sortFilterPanel(), BorderLayout.CENTER);
        panel.add(sortFilterControlPanel(), BorderLayout.EAST);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return panel;
    }

    private JPanel sortFilterControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel actionPanel = new JPanel(new GridLayout(3, 1));
        init(actionPanel);
        panel.add(actionPanel);

        return panel;
    }

    private JPanel recordLimitPanel() {
        JPanel container = new JPanel(new BorderLayout());
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        recordLimit = new TextField("recordLimit", 10);
        recordLimit.setToolTipText(_recordLimit + "");
        recordLimit.setText(_recordLimit + "");
        layoutGenerator.addLabelWidgetPair("Row Limit ", recordLimit, panel);

        widgetLayout(1, 2, 5, 5, 4, 4, layoutGenerator, panel);
        container.add(panel, BorderLayout.NORTH);
        return container;
    }

    private JPanel rowFilterPanel() {
        JPanel container = new JPanel(new BorderLayout());
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        rowFilter = new TextArea("rowFilter", "", 25, 2);
        rowFilter.setToolTipText(_rowFilter);
        ScrollableComponent scrollPane = new ScrollableComponent(rowFilter);
        scrollPane.setPreferredSize(new Dimension(550, 55));
        layoutGenerator.addLabelWidgetPair("Row Filter ", scrollPane, panel);

        widgetLayout(1, 2, 5, 5, 4, 4, layoutGenerator, panel);
        container.add(panel, BorderLayout.NORTH);
        return container;
    }

//    private JPanel sortOrderPanel() {
//        JPanel panel = new JPanel(new BorderLayout());
//
//        panel.add(new Label("Sort Order "), BorderLayout.WEST);
//        sortOrder = new TextArea("sortOrder", "", 25, 2);
//        sortOrder.setToolTipText(sortOrder.getText());
//        panel.add(ScrollableComponent.createWithVerticalScrollBar(sortOrder), BorderLayout.CENTER);
//
//        return panel;
//    }

//    private JPanel rowFilterPanel() {
//        JPanel panel = new JPanel(new BorderLayout());
//
//        panel.add(new Label("Row Filter  "), BorderLayout.WEST);
//        rowFilter = new TextArea("rowFilter", "", 25, 2);
//        rowFilter.setToolTipText(rowFilter.getText());
//        panel.add(ScrollableComponent.createWithVerticalScrollBar(rowFilter), BorderLayout.CENTER);
//
//        return panel;
//    }

    public void init(JPanel actionPanel) {
        Button apply = new Button("Apply", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                applySortFilter();
            }
        });
        apply.setToolTipText("Apply the Row Filter constraints to the table");
        actionPanel.add(new JLabel(""));
        actionPanel.add(apply);
        actionPanel.add(new JLabel(""));
    }

//    private void doApplyConstraints(final TablePresenter presenter) {
//        try {
//            String rowFilterValue = rowFilter.getText().trim();
//            String sortOrderValue = sortOrder.getText().trim();
//            presenter.doApplyConstraints(rowFilterValue, sortOrderValue);
//
//            if (rowFilterValue.length() == 0)
//                rowFilterValue = "No filter";
//            String sortMessage = sortOrderValue;
//            if (sortMessage.length() == 0)
//                sortMessage = "No sort";
//
//            messagePanel.setMessage("Saved any changes and applied Sort '" + sortMessage + "' and Filter '" + rowFilterValue + "'");
//        } catch (EmfException ex) {
//            messagePanel.setError(ex.getMessage());
//        }
//    }

    private void applySortFilter() {
        //validate the record limit field...
        try {
            _recordLimit = Math.abs(new Integer(recordLimit.getText()));
        } catch (NumberFormatException ex) {
            messagePanel.setMessage("The row limit must be a number and must be a positive whole number");
            return;
        }
        _rowFilter = rowFilter.getText();
        updateMainPanel(new EfficiencyRecord[] {});
        this.populateThread = new Thread(this);
        populateThread.start();
    }

    private void widgetLayout(int rows, int cols, int initX, int initY, int xPad, int yPad,
            SpringLayoutGenerator layoutGenerator, JPanel panel) {
        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, rows, cols, // rows, cols
                initX, initY, // initialX, initialY
                xPad, yPad);// xPad, yPad
    }
}
