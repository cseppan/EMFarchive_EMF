package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.CopyButton;
import gov.epa.emissions.commons.gui.buttons.EditButton;
import gov.epa.emissions.commons.gui.buttons.NewButton;
import gov.epa.emissions.commons.gui.buttons.ViewButton;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.editor.DataEditor;
import gov.epa.emissions.framework.client.data.viewer.DataViewer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.ScrollableTable;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;

public class EditVersionsPanel extends JPanel implements EditVersionsView {

    private VersionsTableData tableData;

    private MessagePanel messagePanel;

    private EditVersionsPresenter presenter;

    private EmfDataset dataset;

    private EmfConsole parentConsole;

    private EmfTableModel tableModel;

    private JPanel tablePanel;

    private DesktopManager desktopManager;

    public EditVersionsPanel(EmfDataset dataset, MessagePanel messagePanel, EmfConsole parentConsole,
            DesktopManager desktopManger) {
        super.setLayout(new BorderLayout());
        setBorder();

        this.dataset = dataset;
        this.messagePanel = messagePanel;
        this.parentConsole = parentConsole;
        this.desktopManager = desktopManger;
    }

    private void setBorder() {
        javax.swing.border.Border outer = BorderFactory.createEmptyBorder(5, 2, 5, 2);
        CompoundBorder border = BorderFactory.createCompoundBorder(outer, new Border("Versions"));
        super.setBorder(border);
    }

    public void observe(EditVersionsPresenter presenter) {
        this.presenter = presenter;
    }

    public void display(Version[] versions, InternalSource[] sources) {
        add(topRightPanel(sources), BorderLayout.PAGE_START);
        add(tablePanel(versions), BorderLayout.CENTER);
        add(bottomPanel(), BorderLayout.PAGE_END);
        if (dataset.getInternalSources().length == 0) {
            displayError("No version information is available - the dataset may be incomplete or external.");
        }
    }

    public void reload(Version[] versions) {
        tablePanel.removeAll();

        // reload table
        ScrollableTable table = createTable(versions);
        tablePanel.add(table, BorderLayout.CENTER);

        refreshLayout();
    }
    
//    public void doRefresh(){
//        tableModel.refresh();
//    }

    public void add(Version version) {
        tableData.add(version);
        tableModel.refresh();
    }

    private JPanel tablePanel(Version[] versions) {
        ScrollableTable table = createTable(versions);

        tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(table, BorderLayout.CENTER);

        return tablePanel;
    }

    private ScrollableTable createTable(Version[] versions) {
        tableData = new VersionsTableData(versions);
        tableModel = new EmfTableModel(tableData);

        ScrollableTable table = new ScrollableTable(tableModel, null);
        //"Select", "Name", "Version", "Base", "Creator", "Is Final?", "#Records", "Date" 
        // set maximum column width
        String[] columns = { "Select", "Version", "Base", "Is Final?" }; // table.setColWidthsBasedOnColNames();
        table.setMaxColWidth(columns);
        table.disableScrolling();
        return table;
    }

    private JPanel bottomPanel() {
        JPanel container = new JPanel(new BorderLayout());
        container.add(leftControlPanel(), BorderLayout.LINE_START);

        return container;
    }

    private JPanel leftControlPanel() {
        JPanel panel = new JPanel();

        Button newButton = new NewButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doNew(tableData.getValues());
            }
        });
        newButton.setToolTipText("Create a new version");
        newButton.setMargin(new Insets(2, 2, 2, 2));
        panel.add(newButton);
        if (dataset.getInternalSources().length == 0) {
            newButton.setEnabled(false);
        }

        Button renameButton = new Button("Rename", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    doEdit();
                } catch (EmfException e1) {
                    messagePanel.setError(e1.getMessage());
                    e1.printStackTrace();
                }
            }
        });
        renameButton.setToolTipText("Change version name");
        renameButton.setMargin(new Insets(2, 2, 2, 2));
        panel.add(renameButton);
        if (dataset.getInternalSources().length == 0) {
            renameButton.setEnabled(false);
        }

        Button markFinal = new Button("Mark Final", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    doMarkFinal(tableData.selected());
                } catch (Exception e1) {
                    e1.printStackTrace();
                    displayError(e1.getMessage());
                }
            }
        });
        markFinal.setToolTipText("Mark the selected versions as final so that no more edits can be made");
        markFinal.setMargin(new Insets(2, 2, 2, 2));

        if (dataset.getInternalSources().length == 0) {
            markFinal.setEnabled(false);
        }
        panel.add(markFinal);
        // moved Edit Properties button to another part of the window

        /*
         * ConfirmDialog confirmDialog = new ConfirmDialog("", "Warning", parentConsole); SelectAwareButton propButton =
         * new SelectAwareButton("Edit Properties", editPropAction(), getTable(), confirmDialog); panel.add(propButton);
         */
        return panel;
    }

    /*
     * private SelectableSortFilterWrapper getTable() { EmfDatasetTableData tableData = new EmfDatasetTableData(new
     * EmfDataset[] { dataset }); return new SelectableSortFilterWrapper(parentConsole, tableData, sortCriteria()); }
     */

    /*
     * private SortCriteria sortCriteria() { String[] columnNames = { "Last Modified Date" }; return new
     * SortCriteria(columnNames, new boolean[] { false }, new boolean[] { true }); }
     */

    /*
     * private Action editPropAction() { // DatasetPropertiesViewer view = new DatasetPropertiesViewer(parentConsole,
     * desktopManager); return new AbstractAction() { public void actionPerformed(ActionEvent arg0) {
     * 
     * DatasetPropertiesEditor view = new DatasetPropertiesEditor(presenter.getSession(), parentConsole,
     * desktopManager); PropertiesEditorPresenter editPresenter = new PropertiesEditorPresenterImpl(dataset, view,
     * presenter .getSession());
     * 
     * clear(); try { editPresenter.doDisplay(); } catch (EmfException e) { // NOTE Auto-generated catch block
     * messagePanel.setError(e.getMessage()); } } }; }
     */

    protected void doNew(Version[] versions) {
        clear();

        NewVersionDialog dialog = new NewVersionDialog(dataset, versions, parentConsole);
        dialog.run();

        if (dialog.shouldCreate()) {
            try {
                presenter.doNew(dialog.version(), dialog.name());
            } catch (EmfException e) {
                displayError(e.getMessage());
            }
        }
    }

    protected void doEdit() throws EmfException {
        clear();
        Version[] selectedVersions = tableData.selected();

        if (selectedVersions.length > 1 || selectedVersions.length == 0) {
            messagePanel.setMessage("Please select one version only");
            return;
        }

        Version lockedVersion = presenter.getLockedVersion(selectedVersions[0]);
        if (lockedVersion == null)
            return;

        boolean locked = true;
        EditVersionDialog dialog = new EditVersionDialog(dataset, lockedVersion, tableData.getValues(), parentConsole);
        dialog.run();

        try {
            if (dialog.shouldChange()) {
                presenter.doChangeVersionName(dialog.getVersion());
                locked = false;
            }
        } catch (EmfException e) {
            e.printStackTrace();
            messagePanel.setError(e.getMessage());
        } finally {
            if (locked)
                presenter.releaseLock(lockedVersion);
        }
    }

    protected void doMarkFinal(Version[] versions) throws Exception {
        clear();

        if (versions == null || versions.length == 0)
            messagePanel.setMessage("Please select version(s) to mark as final.");

        if (versions.length == 1 && !versions[0].isFinalVersion()) {
            String msg = "Would you like to make version " + versions[0].getVersion() + 
                " the default version for the dataset?";
            int makeDefault = JOptionPane.showConfirmDialog(parentConsole, msg, "Make Default Version",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null);

            if (makeDefault == JOptionPane.YES_OPTION) {
                presenter.markFinalNDefault(versions[0]);

                return;
            }
        }

        try {
            presenter.doMarkFinal(versions);
        } catch (EmfException e) {
            displayError(e.getMessage());
        }
    }

    private JPanel topRightPanel(InternalSource[] sources) {
        JPanel container = new JPanel(new BorderLayout());

        JPanel panel = new JPanel();

        panel.add(new Label("Table:"));

        DefaultComboBoxModel tablesModel = new DefaultComboBoxModel(tableNames(sources));
        final JComboBox tableCombo = new JComboBox(tablesModel);
        tableCombo.setName("tables");
        tableCombo.setEditable(false);
        panel.add(tableCombo);

        Button view = viewButton(tableCombo);
        if (dataset.getInternalSources().length == 0) {
            view.setEnabled(false);
        }
        panel.add(view);

        Button edit = editButton(tableCombo);
        if (dataset.getInternalSources().length == 0) {
            edit.setEnabled(false);
        }
        panel.add(edit);
        
        Button copy = copyButton(tableCombo);
        if (dataset.getInternalSources().length == 0) {
            edit.setEnabled(false);
        }
        panel.add(copy);

        container.add(panel, BorderLayout.CENTER);

        return container;
    }

    private Button viewButton(final JComboBox tableCombo) {
        Button view = new ViewButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doView(tableCombo);
            }
        });
        view.setToolTipText("View the specified table for the selected versions");
        return view;
    }

    private Button editButton(final JComboBox tableCombo) {
        Button edit = new EditButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doEdit(tableCombo);
            }
        });
        edit.setToolTipText("Edit the specified table for the selected versions");
        return edit;
    }
    
    private Button copyButton(final JComboBox tableCombo) {
        Button copy = new CopyButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clear();
                copyDataSet(tableCombo.getSelectedItem());
            }
        });
        copy.setToolTipText("Copy a Version to New Dataset");
        return copy;
    }

    private void doView(final JComboBox tableCombo) {
        clear();

        String table = (String) tableCombo.getSelectedItem();
        Version[] versions = tableData.selected();
        if (versions.length < 1) {
            displayError("Please select at least one version to view");
            return;
        }

        for (int i = 0; i < versions.length; i++)
            showView(table, versions[i]);
    }

    private void showView(String table, Version version) {
        DataViewer view = new DataViewer(dataset, parentConsole, desktopManager);
        try {
            if (dataset.getInternalSources().length > 0)
                presenter.doView(version, table, view);
            else
                displayError("Could not open viewer.This is an external file.");
        } catch (EmfException e) {
            displayError(e.getMessage());
        }
    }
    
    private void copyDataSet(Object table) {
        Version[] versions = tableData.selected();
        
        if (versions.length < 1) {
            displayError("Please select a final version to copy");
            return;
        }
        
        if (versions.length > 1) {
            displayError("Please select only one final version to copy");
            return;
        }
        
        try {
            if ( getYesNoSelection() == JOptionPane.YES_OPTION)
                presenter.copyDataset(versions[0]);
            else
                return; 
        } catch (EmfException e) {
            displayError(e.getMessage());
            return;
        }
        
        messagePanel.setMessage("Please go to the dataset manager window and Refresh to see the copied dataset.");
    }
    
    private int getYesNoSelection(){
        String message = " Would you like to copy a version to new dataset? ";
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, "Warning", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        return selection;
    }

    private void displayError(String message) {
        messagePanel.setError(message);
        refreshLayout();
    }

    private void refreshLayout() {
        super.validate();
    }

    private void doEdit(final JComboBox tableCombo) {
        clear();

        String table = (String) tableCombo.getSelectedItem();
        Version[] versions = tableData.selected();
        if (versions.length != 1) {
            displayError("Please select a single version to edit");
            return;
        }

        showEditor(table, versions[0]);
    }

    private void showEditor(String table, Version version) {
        DataEditor view = new DataEditor(dataset, parentConsole, desktopManager);
        try {
            if (dataset.isExternal()) {
                displayError("Could not open data editor: the Dataset references external files.");
                return;
            }
            presenter.doEdit(version, table, view);
        } catch (EmfException e) {
            displayError(e.getMessage());
        }
    }

    private void clear() {
        messagePanel.clear();
        refreshLayout();
    }

    private String[] tableNames(InternalSource[] sources) {
        List tables = new ArrayList();
        for (int i = 0; i < sources.length; i++)
            tables.add(sources[i].getTable());

        return (String[]) tables.toArray(new String[0]);
    }

    public void notifyLockFailure(Version version) {
        clear();
        messagePanel.setError("Cannot obtain a lock for version \"" + version.getName() + "\".");
    }

}
