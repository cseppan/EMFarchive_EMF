package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.cost.ControlProgram;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ControlStrategyProgramsTab extends JPanel implements EditControlStrategyTabView, ControlProgramSelectorView {

    private ManageChangeables changeablesList;

    private JPanel tablePanel; 

    private SingleLineMessagePanel messagePanel;

    private SelectableSortFilterWrapper table;

    private ControlStrategyProgramTableData tableData;

    private EmfConsole parent;

    private Button addButton = new AddButton(addAction());
    
    private EmfSession session;
    
    public ControlStrategyProgramsTab(ControlStrategy controlStrategy, ManageChangeables changeablesList,
            SingleLineMessagePanel messagePanel, EmfConsole parentConsole, EmfSession session) {
//        this.controlProgram = controlProgram;
        this.changeablesList = changeablesList;
        this.messagePanel = messagePanel;
        this.parent = parentConsole;
        this.session = session;
    }

    public void display(ControlStrategy controlStrategy) {
        setupLayout(changeablesList, controlStrategy);
    }

    private void setupLayout(ManageChangeables changeables,
            ControlStrategy controlStrategy) {
        try {
            tableData = new ControlStrategyProgramTableData(controlStrategy.getControlPrograms());
        } catch (Exception e) {
            messagePanel.setError(e.getMessage());
        }
        this.setLayout(new BorderLayout(5, 5));
        // this.setBorder(BorderFactory.createEmptyBorder(50,50,0,300));
        //buildSortFilterPanel();
        this.add(mainPanel(), BorderLayout.CENTER);
    }


    private SortCriteria sortCriteria() {
        String[] columnNames = { "Name" };
        return new SortCriteria(columnNames, new boolean[] {true }, new boolean[] { true });
    }

    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        addButton.setMargin(new Insets(2, 5, 2, 5));
        panel.add(addButton);
        Button removeButton = new RemoveButton(removeAction());
        removeButton.setMargin(new Insets(2, 5, 2, 5));
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
        ControlProgramSelectionView view = new ControlProgramSelectionDialog(parent, changeablesList);
        ControlProgramSelectionPresenter presenter = new ControlProgramSelectionPresenter(this, view, session);
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
        List selected = table.selected();

        if (selected.size() == 0) {
            messagePanel.setError("Please select an item to remove.");
            return;
        }

        ControlProgram[] records = (ControlProgram[]) selected.toArray(new ControlProgram[0]);

        if (records.length == 0)
            return;

        String title = "Warning";
        String message = "Are you sure you want to remove the selected row(s)?";
        int selection = JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            tableData.remove(records);
            refresh();
        }
    }

    private JPanel mainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      
        mainPanel.removeAll();
        mainPanel.add(new JLabel("Technologies to Include:"), BorderLayout.NORTH);
        mainPanel.add(tablePanel());
        mainPanel.add(buttonPanel(), BorderLayout.SOUTH);
        return mainPanel; 
    }
    
    private JPanel tablePanel() {
        tablePanel = new JPanel(new BorderLayout());
        table = new SelectableSortFilterWrapper(parent, tableData, sortCriteria());
        tablePanel.add(table);

        return tablePanel;
    }
    
    private void refresh(){
        table.refresh(tableData);
        panelRefresh();
    }
    
    private void panelRefresh() {
        tablePanel.removeAll();
        tablePanel.add(table);
        super.validate();
    }

    public void observe(ControlStrategyProgramsTabPresenter presenter) {
        //
    }

    public void notifyStrategyTypeChange(StrategyType strategyType) {
        // NOTE Auto-generated method stub
    }

    public void refresh(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults) {
        // NOTE Auto-generated method stub
    }

    public void save(ControlStrategy controlStrategy) {
        ControlProgram[] controlPrograms = new ControlProgram[] {};//controlStrategy.getTechnologies();
        if (tableData != null) {
            controlPrograms = new ControlProgram[tableData.rows().size()];
            for (int i = 0; i < tableData.rows().size(); i++) {
                controlPrograms[i] = (ControlProgram)tableData.element(i);
            }
        }
        controlStrategy.setControlPrograms(controlPrograms);
    }

    public void add(ControlProgram[] controlPrograms) {
        messagePanel.clear();
        if (controlPrograms.length > 0 ) {
            tableData.add(controlPrograms);

            refresh();

        }
    }
}