package gov.epa.emissions.framework.client.fast.analyzer.tabs;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.fast.AbstractFastAction;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.fast.FastAnalysis;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class FastAnalysisInputsTab extends AbstractAnalysisFastTab {

    private SelectableSortFilterWrapper table;

    private static final String WARNING_MESSAGE = "You have asked to open several windows. Do you want proceed?";;

    public FastAnalysisInputsTab(FastAnalysis analysis, EmfSession session, MessagePanel messagePanel,
            ManageChangeables changeablesList, EmfConsole parentConsole) {

        super(analysis, session, messagePanel, changeablesList, parentConsole);
        this.setName("inputs");
    }

    public void display() {

        this.setLayout(new BorderLayout());
        this.add(this.createTablePanel(this.getAnalysis(), this.getParentConsole(), this.getSession()),
                BorderLayout.CENTER);
        this.add(this.createCrudPanel(), BorderLayout.SOUTH);
        super.display();
    }

    protected void populateFields() {
        /*
         * no-op
         */
    }

    public void save(FastAnalysis analysis) {
        this.clearMessage();
    }

    @Override
    void refreshData() {
        // this.table.refresh(new FastAnalysisInputTableData(this.getAnalysis().getInputs()));
    }

    private JPanel createCrudPanel() {

        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.insets = new Insets(5, 5, 5, 0);

        crudPanel.add(this.createAddButton(), constraints);

        constraints.gridx = 1;

        crudPanel.add(this.createEditButton(), constraints);

        constraints.gridx = 2;

        crudPanel.add(this.createRemoveButton(), constraints);

        JLabel emptyLabel = new JLabel();
        constraints.gridx = 3;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        crudPanel.add(emptyLabel, constraints);

        return crudPanel;
    }

    protected Button createAddButton() {

        Button addButton = new AddButton(new AbstractFastAction(this.getMessagePanel(),
                "Error creating Fast analysis input") {

            @Override
            protected void doActionPerformed(ActionEvent e) throws EmfException {
                createNewInput();
            }
        });

        return addButton;
    }

    protected Button createEditButton() {

        Action editAction = new AbstractFastAction(this.getMessagePanel(), "Error editing Fast analysis inputs") {

            @Override
            protected void doActionPerformed(ActionEvent e) throws EmfException {
                editSelectedInputs();
            }
        };

        return new SelectAwareButton("Edit", editAction, table, new ConfirmDialog(WARNING_MESSAGE, "Warning", this));
    }

    protected Button createRemoveButton() {

        Button removeButton = new RemoveButton(new AbstractFastAction(this.getMessagePanel(),
                "Error removing Fast analysis inputs") {

            @Override
            protected void doActionPerformed(ActionEvent e) throws EmfException {
                removeSelectedInputs();
            }
        });

        return removeButton;
    }

    private void createNewInput() throws EmfException {
    }

    private void removeSelectedInputs() throws EmfException {
    }

    private void editSelectedInputs() throws EmfException {
    }

    private JPanel createTablePanel(FastAnalysis analysis, EmfConsole parentConsole, EmfSession session) {

        JPanel tablePanel = new JPanel(new BorderLayout());
        // this.table = new SelectableSortFilterWrapper(parentConsole, new
        // FastAnalysisInputTableData(analysis.getInputs()),
        // sortCriteria());
        tablePanel.add(this.table, BorderLayout.CENTER);

        return tablePanel;
    }

    private SortCriteria sortCriteria() {
        return new SortCriteria(new String[] { "Name" }, new boolean[] { false }, new boolean[] { true });
    }

    public void viewOnly() {
    }
}
