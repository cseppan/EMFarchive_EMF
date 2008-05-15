package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.CopyButton;
import gov.epa.emissions.commons.gui.buttons.NewButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditor;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseViewer;
import gov.epa.emissions.framework.client.casemanagement.sensitivity.SensitivityWindow;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseCategory;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshButton;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class CaseManagerWindow extends ReusableInteralFrame implements CaseManagerView, RefreshObserver {

    private CaseManagerPresenter presenter;

    private JPanel mainPanel;

    private CasesTableData tableData;

    private SelectableSortFilterWrapper table;

    private JPanel layout;

    private MessagePanel messagePanel;

    private EmfConsole parentConsole;

    private List cases;

    private List<CaseCategory> categories;

    private CaseCategory selectedCategory;

    private EmfSession session;

    private ComboBox categoriesBox;

    public CaseManagerWindow(EmfSession session, EmfConsole parentConsole, DesktopManager desktopManager) {
        super("Case Manager", new Dimension(900, 400), desktopManager);

        this.session = session;
        this.parentConsole = parentConsole;

        layout = new JPanel();
        this.getContentPane().add(layout);
    }

    public void observe(CaseManagerPresenter presenter) {
        this.presenter = presenter;
    }

    public void display() {
        doLayout(new Case[0]);
        super.display();
    }

    public void refresh(Case[] cases) {
        setupTableModel(cases);
        table.refresh(tableData);
        panelRefresh();
    }

    private void panelRefresh() {
        mainPanel.removeAll();
        mainPanel.add(table);
        super.refreshLayout();
    }

    public void refreshWithLastCategory() throws EmfException {
        doLayout(presenter.getCases(selectedCategory));
        super.refreshLayout();
    }

    private void doLayout(Case[] cases) {
        messagePanel = new SingleLineMessagePanel();

        try {
            getAllCategories();
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }

        createCategoriesComboBox();
        setupTableModel(cases);
        // model = new EmfTableModel(tableData);
        // selectModel = new SortFilterSelectModel(model);
        // SortFilterSelectionPanel sortFilterSelectPanel = new SortFilterSelectionPanel(parentConsole, selectModel);
        // sortFilterSelectPanel.sort(sortCriteria());
        createLayout(layout, mainPanel(parentConsole));
    }

    private void setupTableModel(Case[] cases) {
        tableData = new CasesTableData(cases);
    }

    private JPanel mainPanel(EmfConsole parentConsole) {
        this.mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        // JScrollPane sortFilterPane = sortFilterPane(parentConsole);
        table = new SelectableSortFilterWrapper(parentConsole, tableData, sortCriteria());
        mainPanel.add(table);

        return mainPanel;
    }

    private SortCriteria sortCriteria() {
        String[] columnNames = { "Last Modified Date" };
        return new SortCriteria(columnNames, new boolean[] { false }, new boolean[] { false });
    }

    private void getAllCategories() throws EmfException {
        this.categories = new ArrayList<CaseCategory>();
        categories.add(new CaseCategory("All"));
        categories.addAll(Arrays.asList(presenter.getCategories()));
    }

    private void createCategoriesComboBox() {
        categoriesBox = new ComboBox("Select one", categories.toArray(new CaseCategory[0]));

        if (selectedCategory != null)
            categoriesBox.setSelectedItem(selectedCategory);

        categoriesBox.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                CaseCategory category = getSelectedCategory();
                try {
                    if (category == null) {
                        refresh(new Case[0]);
                        return;
                    }

                    refresh(presenter.getCases(category));
                } catch (EmfException e1) {
                    messagePanel.setError("Could not retrieve all cases with -- " + category.getName());
                }
            }
        });
    }

    private CaseCategory getSelectedCategory() {
        this.selectedCategory = (CaseCategory) categoriesBox.getSelectedItem();
        return selectedCategory;
    }

    private void createLayout(JPanel layout, JPanel table) {
        layout.removeAll();
        layout.setLayout(new BorderLayout());

        // JScrollPane scrollPane = new JScrollPane(sortFilterSelectPanel);
        // sortFilterSelectPanel.setPreferredSize(new Dimension(450, 120));

        layout.add(createTopPanel(), BorderLayout.NORTH);
        layout.add(table, BorderLayout.CENTER);
        layout.add(createControlPanel(), BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel msgRefreshPanel = new JPanel(new BorderLayout());
        msgRefreshPanel.add(messagePanel, BorderLayout.CENTER);
        Button button = new RefreshButton(this, "Refresh Cases", messagePanel);
        msgRefreshPanel.add(button, BorderLayout.EAST);

        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(msgRefreshPanel);
        panel.add(getCategoryPanel("Show Cases of Category:", categoriesBox));

        return panel;
    }

    private JPanel getCategoryPanel(String label, JComboBox box) {
        JPanel panel = new JPanel(new BorderLayout(5, 2));
        JLabel jlabel = new JLabel(label);
        jlabel.setHorizontalAlignment(JLabel.RIGHT);
        panel.add(jlabel, BorderLayout.WEST);
        panel.add(box, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(3, 150, 5, 150));

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel crudPanel = createCrudPanel();

        JPanel closePanel = new JPanel();
        Button closeButton = new CloseButton(new AbstractAction() {
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

        String message = "You have asked to open a lot of windows. Do you wish to proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);

        crudPanel.add(viewButton(confirmDialog));
        crudPanel.add(editButton(confirmDialog));

        Button newButton = new NewButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMsgPanel();
                createNewCase();
            }
        });
        crudPanel.add(newButton);

        Button removeButton = new RemoveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMsgPanel();
                removeSelectedCases();
            }
        });
        crudPanel.add(removeButton);

        Button copyButton = new CopyButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMsgPanel();
                copySelectedCases();
            }
        });
        crudPanel.add(copyButton);

        Button sensitivityButton = new Button("Sensitivity", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMsgPanel();
                sensitivityCase();
            }
        });
        crudPanel.add(sensitivityButton);

        return crudPanel;
    }

    private SelectAwareButton editButton(ConfirmDialog confirmDialog) {
        Action editAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMsgPanel();
                editCases();
            }

        };
        SelectAwareButton editButton = new SelectAwareButton("Edit", editAction, table, confirmDialog);
        return editButton;
    }

    private SelectAwareButton viewButton(ConfirmDialog confirmDialog) {
        Action viewAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMsgPanel();
                viewCases();
            }
        };

        SelectAwareButton viewButton = new SelectAwareButton("View", viewAction, table, confirmDialog);
        return viewButton;
    }

    private void viewCases() {
        cases = selected();
        if (cases.isEmpty()) {
            messagePanel.setMessage("Please select one or more Cases");
            return;
        }

        for (Iterator iter = cases.iterator(); iter.hasNext();) {
            Case caseObj = (Case) iter.next();
            CaseViewer view = new CaseViewer(parentConsole, session, desktopManager);
            try {
                presenter.doView(view, caseObj);
            } catch (EmfException e) {
                showError(e.getMessage());
            }
        }
    }

    private void editCases() {
        cases = selected();
        if (cases.isEmpty()) {
            messagePanel.setMessage("Please select one or more Cases");
            return;
        }

        for (Iterator iter = cases.iterator(); iter.hasNext();) {
            Case caseObj = (Case) iter.next();
            CaseEditor view = new CaseEditor(parentConsole, session, desktopManager);
            try {
                presenter.doEdit(view, caseObj);
            } catch (EmfException e) {
                e.printStackTrace();
                showError(e.getMessage());
            }
        }
    }

    private void createNewCase() {
        NewCaseWindow view = new NewCaseWindow(desktopManager);
        presenter.doNew(view);
    }

    private void sensitivityCase() {
        cases = selected();

        if (cases.isEmpty() || cases.size() != 1) {
            messagePanel.setMessage("Please select a single case");
            return;
        }

        SensitivityWindow view = new SensitivityWindow(desktopManager, parentConsole, categories);
        presenter.doSensitivity(view, (Case) cases.get(0));
    }

    private void removeSelectedCases() {
        clearMsgPanel();
        List selected = selected();

        if (selected.isEmpty()) {
            messagePanel.setMessage("Please select one or more cases to remove.");
            return;
        }

        int selection = showWarningMsg("Warning", "Are you sure you want to remove the selected case(s)?");
        
        if (selection == JOptionPane.NO_OPTION) {
            return;
        }

        try {
            for (Iterator iter = selected.iterator(); iter.hasNext();) {
                Case element = (Case) iter.next();

                if (isParentCase(element)) {
                    clearMsgPanel();
                    break;
                }

                messagePanel.setMessage("Please wait while removing cases...");
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                presenter.doRemove(element);
                doRefresh();
                clearMsgPanel();
                messagePanel.setMessage("Finished removing cases.");
            }
        } catch (EmfException e) {
            showError(e.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private boolean isParentCase(Case element) throws EmfException {
        String msg = presenter.checkParentCase(element);

        if (msg.isEmpty())
            return false;
        
        int selection = showWarningMsg("Warning", msg + "\nAre you sure you want to remove the selected case " + element.getName() + "?");

        if (selection == JOptionPane.NO_OPTION) {
            return true;
        }

        return false;
    }

    private int showWarningMsg(String title, String msg) {
        return JOptionPane.showConfirmDialog(parentConsole, msg, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
    }

    private void copySelectedCases() {
        cases = selected();

        if (cases.isEmpty()) {
            messagePanel.setMessage("Please select one or more Cases");
            return;
        }

        messagePanel.setMessage("Please wait while copying cases...");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        int[] caseIds = new int[cases.size()];

        for (int i = 0; i < caseIds.length; i++)
            caseIds[i] = ((Case) cases.get(i)).getId();

        try {
            presenter.doCopyCases(caseIds);
            doRefresh();
            clearMsgPanel();
            messagePanel.setMessage("Finished copying cases.");
        } catch (Exception e) {
            showError("Could not copy cases." + e.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void showError(String message) {
        messagePanel.setError(message);
    }

    private List selected() {
        return table.selected();
    }

    public EmfConsole getParentConsole() {
        return parentConsole;
    }

    private void clearMsgPanel() {
        messagePanel.clear();
    }

    public void doRefresh() throws EmfException {
        refresh(presenter.getCases(getSelectedCategory()));
    }

    public void addNewCaseToTableData(Case newCase) {
        List<Case> cases = new ArrayList<Case>();
        cases.addAll(Arrays.asList(tableData.sources()));
        cases.add(newCase);
        refresh(cases.toArray(new Case[0]));
    }

}
