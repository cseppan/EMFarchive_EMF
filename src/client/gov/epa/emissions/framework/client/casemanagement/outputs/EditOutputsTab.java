package gov.epa.emissions.framework.client.casemanagement.outputs;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.EditButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.commons.gui.buttons.ViewButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshObserver;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class EditOutputsTab extends JPanel implements EditOutputsTabView, RefreshObserver {

    private EmfConsole parentConsole;

    private EditOutputsTabPresenter presenter;

    private OutputsTableData tableData;

    private ManageChangeables changeables;

    private SortFilterSelectModel selectModel;

    private JPanel tablePanel;

    public EditOutputsTab(EmfConsole parentConsole, ManageChangeables changeables, MessagePanel messagePanel,
            DesktopManager desktopManager) {
        super.setName("editOutputsTab");
        this.parentConsole = parentConsole;
        this.changeables = changeables;

        super.setLayout(new BorderLayout());
    }

    public void display(Case caseObj, EditOutputsTabPresenter presenter, EmfSession session) {
        super.removeAll();
        super.add(createLayout(new CaseInput[0], presenter, parentConsole), BorderLayout.CENTER);
        this.presenter = presenter;
    }

    private void doRefresh(CaseInput[] inputs) {
        super.removeAll();
        super.add(createLayout(inputs, presenter, parentConsole), BorderLayout.CENTER);
    }

    private JPanel createLayout(CaseInput[] inputs, EditOutputsTabPresenter presenter, EmfConsole parentConsole) {
        JPanel layout = new JPanel(new BorderLayout());

        layout.add(tablePanel(inputs, parentConsole), BorderLayout.CENTER);
        layout.add(controlPanel(presenter), BorderLayout.PAGE_END);

        return layout;
    }

    private JPanel tablePanel(CaseInput[] inputs, EmfConsole parentConsole) {
        tableData = new OutputsTableData(inputs);
        changeables.addChangeable(tableData);
        selectModel = new SortFilterSelectModel(new EmfTableModel(tableData));

        tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(createSortFilterPanel(parentConsole), BorderLayout.CENTER);

        return tablePanel;
    }

    private JScrollPane createSortFilterPanel(EmfConsole parentConsole) {
        SortFilterSelectionPanel sortFilterPanel = new SortFilterSelectionPanel(parentConsole, selectModel);

        JScrollPane scrollPane = new JScrollPane(sortFilterPanel);
        sortFilterPanel.setPreferredSize(new Dimension(450, 60));
        return scrollPane;
    }

    private JPanel controlPanel(final EditOutputsTabPresenter presenter) {
        JPanel container = new JPanel();
        Insets insets = new Insets(1, 2, 1, 2);

        Button add = new AddButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //
            }
        });
        add.setMargin(insets);
        add.setEnabled(false);
        container.add(add);

        Button remove = new RemoveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //
            }
        });
        remove.setMargin(insets);
        remove.setEnabled(false);
        container.add(remove);

        Button defaultButton = new Button("Defaults", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //
            }
        });
        defaultButton.setMargin(insets);
        defaultButton.setEnabled(false);
        container.add(defaultButton);

        Button edit = new EditButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //
            }
        });
        edit.setMargin(insets);
        edit.setEnabled(false);
        container.add(edit);

        Button view = new ViewButton("View Dataset", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //
            }
        });
        view.setMargin(insets);
        view.setEnabled(false);
        container.add(view);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(container, BorderLayout.WEST);

        return panel;
    }

    public void refresh() {
        doRefresh(tableData.sources());
    }

    public void doRefresh() throws EmfException {
        if (false)
            throw new EmfException("under construction...");
    }

}
