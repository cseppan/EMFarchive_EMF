package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.LightControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureClass;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.ListWidget;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.emissions.framework.ui.TrackableSortFilterSelectModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class EditControlStrategyMeasuresTab extends JPanel implements ControlStrategyMeasuresTabView {

    private ListWidget classesList;

    private ControlMeasureClass[] allClasses;

    private ControlMeasureClass[] classes;

    private EditControlStrategyMeasuresTabPresenter presenter;

    private ManageChangeables changeablesList;

    private ControlMeasureClass defaultClass = new ControlMeasureClass("All");

    private JPanel mainPanel;

    private SingleLineMessagePanel messagePanel;

    private EmfTableModel tableModel;

    private ControlMeasureTableData tableData;

    private TrackableSortFilterSelectModel sortFilterSelectModel;

    private EmfConsole parent;

    private EmfSession session;

    private Button addButton = new AddButton(addAction());
    
    private ControlStrategy controlStrategy;

    // private JPanel sortFilterPanelContainer = new JPanel();

    public EditControlStrategyMeasuresTab(ControlStrategy controlStrategy, ManageChangeables changeablesList,
            SingleLineMessagePanel messagePanel, EmfConsole parentConsole, EmfSession session) {
        this.controlStrategy = controlStrategy;
        this.changeablesList = changeablesList;
        this.messagePanel = messagePanel;
        this.parent = parentConsole;
        this.session = session;
    }

    public void display(ControlStrategy strategy) throws EmfException {
        this.allClasses = presenter.getAllClasses();
        this.classes = presenter.getControlMeasureClasses();
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setupLayout(changeablesList);
    }

    private void setupLayout(ManageChangeables changeables) {
        try {
            LightControlMeasure[] cmObjs = presenter.getControlMeasures();
            tableData = new ControlMeasureTableData(cmObjs);
        } catch (Exception e) {
            messagePanel.setError(e.getMessage());
        }

        this.setLayout(new BorderLayout(10, 10));
        // this.setBorder(BorderFactory.createEmptyBorder(50,50,0,300));
        this.add(createClassesPanel(changeables), BorderLayout.WEST);
        // mainPanel.add(new JLabel("Measures to Include:"), BorderLayout.NORTH);
        buildSortFilterPanel();
        // mainPanel.add(buttonPanel(), BorderLayout.SOUTH);
        this.add(mainPanel, BorderLayout.EAST);
        // disable class filter since there are measures selected
        if (sortFilterSelectModel.getRowCount() > 0) classesList.setEnabled(false);
    }

    private SortFilterSelectionPanel sortFilterPanel() {
        tableModel = new EmfTableModel(tableData);
        sortFilterSelectModel = new TrackableSortFilterSelectModel(tableModel);
        changeablesList.addChangeable(sortFilterSelectModel);
        SortFilterSelectionPanel sortFilterSelectionPanel = new SortFilterSelectionPanel(parent, sortFilterSelectModel);
        sortFilterSelectionPanel.setPreferredSize(new Dimension(550, 300));
        return sortFilterSelectionPanel;
    }

    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        if (presenter.getAllControlMeasures().length == 0)
            addButton.setEnabled(false);
        panel.add(addButton);
        Button removeButton = new RemoveButton(removeAction());
        panel.add(removeButton);

        JPanel container = new JPanel(new BorderLayout());
        container.add(panel, BorderLayout.LINE_START);

        return container;
    }

    private Action addAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                selectionView();
            }
        };
    }

    private void selectionView() {
        ControlMeasureSelectionView view = new ControlMeasureSelectionDialog(parent, changeablesList);
        ControlMeasureSelectionPresenter presenter = new ControlMeasureSelectionPresenter(this, view, session,
                this.presenter.getAllControlMeasures());
        try {
            presenter.display(view);
        } catch (Exception exp) {
            messagePanel.setError(exp.getMessage());
        }
    }

    private Action removeAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                remove();
            }
        };
    }

    protected void remove() {
        messagePanel.clear();
        List selected = sortFilterSelectModel.selected();

        if (selected.size() == 0) {
            messagePanel.setError("Please select an item to remove.");
            return;
        }

        LightControlMeasure[] records = (LightControlMeasure[]) selected.toArray(new LightControlMeasure[0]);

        if (records.length == 0)
            return;

        String title = "Warning";
        String message = "Are you sure you want to remove the selected row(s)?";
        int selection = JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            tableData.remove(records);
            buildSortFilterPanel();
        }

        // disable class filter, if there are measures selected, or enable if no
        // measures are selected
        if (sortFilterSelectModel.getRowCount() == 0) {
            classesList.setEnabled(true);
        } else {
            classesList.setEnabled(false);
        }

    }

    private void buildSortFilterPanel() {
        mainPanel.removeAll();
        mainPanel.add(new JLabel("Measures to Include:"), BorderLayout.NORTH);
        SortFilterSelectionPanel panel = sortFilterPanel();
        mainPanel.add(panel);
        mainPanel.add(buttonPanel(), BorderLayout.SOUTH);

        // SortFilterSelectionPanel panel = sortFilterPanel();
        // sortFilterPanelContainer.removeAll();
        // sortFilterPanelContainer.add(panel);
        // mainPanel.add(sortFilterPanelContainer);
    }

    private JPanel createClassesPanel(ManageChangeables changeables) {

        // get all measure classes and cs classes
        // and add default "ALL" to both lists
        List allClassesList = new ArrayList(Arrays.asList(allClasses));
        allClassesList.add(0, defaultClass);
        allClasses = (ControlMeasureClass[]) allClassesList.toArray(new ControlMeasureClass[0]);
        if (classes.length == 0) {
            List selClassesList = new ArrayList();
            selClassesList.add(defaultClass);
            classes = (ControlMeasureClass[]) selClassesList.toArray(new ControlMeasureClass[0]);
        }
        // build list widget
        this.classesList = new ListWidget(allClasses, classes);
        changeables.addChangeable(classesList);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 0));
        JLabel label = new JLabel("Classes to Include:");
        JScrollPane scrollPane = new JScrollPane(classesList);
        scrollPane.setPreferredSize(new Dimension(20, 100));
        panel.add(label, BorderLayout.NORTH);
        JPanel scrollPanel = new JPanel(new BorderLayout());
        scrollPanel.add(scrollPane, BorderLayout.NORTH);
        panel.add(scrollPanel);
        return panel;
    }

    public void save(ControlStrategy controlStrategy) {
        controlStrategy.setControlMeasureClasses(getControlMeasureClasses());
        LightControlMeasure[] cms = {};
        if (tableData != null) {
            cms = new LightControlMeasure[tableData.rows().size()];
            for (int i = 0; i < tableData.rows().size(); i++) {
                cms[i] = (LightControlMeasure)tableData.element(i);
            }
        } else {
            cms = controlStrategy.getControlMeasures();
        }
        controlStrategy.setControlMeasures(cms);
    }

    private ControlMeasureClass[] getControlMeasureClasses() {
        ControlMeasureClass[] controlMeasureClasses = {};
        ControlMeasureClass[] selClasses = {};
        if (classesList != null){
            selClasses = Arrays.asList(classesList.getSelectedValues()).toArray(
                    new ControlMeasureClass[0]);
        } else {
            selClasses = controlStrategy.getControlMeasureClasses();
        }

        // make sure we don't include the All class, its just for display purposes,
        // its not stored in the database
        if (selClasses.length != 0 && !(selClasses.length == 1 && selClasses[0].equals(defaultClass))) {
            List selClassesList = new ArrayList();
            for (int i = 0; i < selClasses.length; i++)
                if (!selClasses[i].equals(defaultClass))
                    selClassesList.add(selClasses[i]);

            controlMeasureClasses = (ControlMeasureClass[]) selClassesList.toArray(new ControlMeasureClass[0]);
        }
        return controlMeasureClasses;
    }

    public void refresh(ControlStrategyResult controlStrategyResult) {
        // NOTE Auto-generated method stub

    }

    public void observe(EditControlStrategyMeasuresTabPresenter presenter) {
        this.presenter = presenter;
    }

    public void add(LightControlMeasure[] cms) {
        for (int i = 0; i < cms.length; i++) {
            tableData.add(cms);
        }

        buildSortFilterPanel();

        // disable class filter since there are measures selected
        if (sortFilterSelectModel.getRowCount() > 0) classesList.setEnabled(false);
    }

    public void startControlMeasuresRefresh() {
        addButton.setEnabled(false);
    }

    public void endControlMeasuresRefresh() {
        messagePanel.setError(presenter.getAllControlMeasures().length + "");
        addButton.setEnabled(true);
    }
}