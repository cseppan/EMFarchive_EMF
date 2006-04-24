package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditor;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshButton;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class CaseManagerWindow extends ReusableInteralFrame implements CaseManagerView, RefreshObserver {

    private CaseManagerPresenterImpl presenter;

    private SortFilterSelectModel selectModel;

    private EmfTableModel model;

    private JPanel layout;

    private MessagePanel messagePanel;

    private EmfConsole parentConsole;

    private List cases;

    private EmfSession session;

    public CaseManagerWindow(EmfSession session, EmfConsole parentConsole, DesktopManager desktopManager) {
        super("Case Manager", new Dimension(700, 600), desktopManager);

        this.session = session;
        this.parentConsole = parentConsole;

        layout = new JPanel();
        this.getContentPane().add(layout);
    }

    public void observe(CaseManagerPresenterImpl presenter) {
        this.presenter = presenter;
    }

    public void display(Case[] cases) {
        doLayout(cases);
        super.display();
    }

    public void refresh(Case[] cases) {
        doLayout(cases);
        super.refreshLayout();
    }

    public void doRefresh() throws EmfException {
        presenter.doRefresh();
    }

    private void doLayout(Case[] cases) {
        model = new EmfTableModel(new CasesTableData(cases));
        selectModel = new SortFilterSelectModel(model);
        SortFilterSelectionPanel sortFilterSelectPanel = new SortFilterSelectionPanel(parentConsole, selectModel);

        createLayout(layout, sortFilterSelectPanel);
    }

    private void createLayout(JPanel layout, JPanel sortFilterSelectPanel) {
        layout.removeAll();
        layout.setLayout(new BorderLayout());

        JScrollPane scrollPane = new JScrollPane(sortFilterSelectPanel);
        sortFilterSelectPanel.setPreferredSize(new Dimension(450, 120));

        layout.add(createTopPanel(), BorderLayout.NORTH);
        layout.add(scrollPane, BorderLayout.CENTER);
        layout.add(createControlPanel(), BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.CENTER);

        Button button = new RefreshButton(this, "Refresh Cases", messagePanel);
        panel.add(button, BorderLayout.EAST);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel crudPanel = createCrudPanel();

        JPanel closePanel = new JPanel();
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                presenter.doClose();
            }
        });
        closePanel.add(closeButton);
        getRootPane().setDefaultButton(closeButton);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());

        controlPanel.add(crudPanel, BorderLayout.WEST);
        controlPanel.add(closePanel, BorderLayout.EAST);

        return controlPanel;
    }

    private JPanel createCrudPanel() {
        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout());

        String message = "Opening too many windows. Do you want proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);

        crudPanel.add(viewButton(confirmDialog));
        crudPanel.add(editButton(confirmDialog));

        Button newButton = new Button("New", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                createNewCase();
            }
        });
        crudPanel.add(newButton);

        Button removeButton = new Button("Remove", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                removeSelectedCases();
            }
        });
        crudPanel.add(removeButton);

        return crudPanel;
    }

    private SelectAwareButton editButton(ConfirmDialog confirmDialog) {
        Action editAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                editCases();
            }

        };
        SelectAwareButton editButton = new SelectAwareButton("Edit", editAction, selectModel, confirmDialog);
        return editButton;
    }

    private SelectAwareButton viewButton(ConfirmDialog confirmDialog) {
        Action viewAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                viewCases();
            }
        };

        SelectAwareButton viewButton = new SelectAwareButton("View", viewAction, selectModel, confirmDialog);
        return viewButton;
    }

    private void viewCases() {// TODO
    }

    private void editCases() {
        cases = selected();
        if (cases.isEmpty()) {
            messagePanel.setMessage("Please select one or more Cases");
            return;
        }

        for (Iterator iter = cases.iterator(); iter.hasNext();) {
            Case caseObj = (Case) iter.next();
            CaseEditor view = new CaseEditor(session, desktopManager);
            try {
                presenter.doEdit(view, caseObj);
            } catch (EmfException e) {
                showError(e.getMessage());
            }
        }
    }

    private void createNewCase() {
        NewCaseWindow view = new NewCaseWindow(desktopManager);
        presenter.doNew(view);
    }

    private void removeSelectedCases() {
        for (Iterator iter = selected().iterator(); iter.hasNext();) {
            Case element = (Case) iter.next();
            try {
                presenter.doRemove(element);
                doRefresh();
            } catch (EmfException e) {
                showError("Could not remove " + element + "." + e.getMessage());
            }
        }
    }

    private void showError(String message) {
        messagePanel.setError(message);
    }

    private List selected() {
        return selectModel.selected();
    }

    public EmfConsole getParentConsole() {
        return parentConsole;
    }

}
