package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.editor.NonEditableDataViewWindow;
import gov.epa.emissions.framework.client.editor.EditableDataViewWindow;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.ScrollableTable;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;

public class VersionsPanel extends JPanel implements VersionsView {

    private VersionsTableData tableData;

    private MessagePanel messagePanel;

    private VersionsPresenter presenter;

    private EmfDataset dataset;

    private EmfConsole parentConsole;

    private EmfTableModel tableModel;

    private JPanel tablePanel;

    private JComboBox defaultVersionsCombo;

    public VersionsPanel(EmfDataset dataset, MessagePanel messagePanel, EmfConsole parentConsole) {
        super.setLayout(new BorderLayout());
        setBorder();

        this.dataset = dataset;
        this.messagePanel = messagePanel;
        this.parentConsole = parentConsole;
    }

    private void setBorder() {
        javax.swing.border.Border outer = BorderFactory.createEmptyBorder(5, 2, 5, 2);
        CompoundBorder border = BorderFactory.createCompoundBorder(outer, new Border("Versions"));
        super.setBorder(border);
    }

    public void observe(VersionsPresenter presenter) {
        this.presenter = presenter;
    }

    public void display(Version[] versions, InternalSource[] sources) {
        VersionsSet versionsSet = new VersionsSet(versions);
        add(topPanel(dataset, versionsSet), BorderLayout.PAGE_START);
        add(tablePanel(versions), BorderLayout.CENTER);
        add(bottomPanel(sources), BorderLayout.PAGE_END);
    }

    private JPanel topPanel(final EmfDataset dataset, VersionsSet versionsSet) {
        JPanel container = new JPanel(new BorderLayout());

        JPanel right = new JPanel();
        right.add(new JLabel("Default Version"));

        String[] versions = getVersionNames(versionsSet);
        ComboBoxModel model = new DefaultComboBoxModel(versions);
        defaultVersionsCombo = new JComboBox(model);
        String defaultVersion = getDefaultVersion(versionsSet);
        defaultVersionsCombo.setSelectedItem(defaultVersion);
        defaultVersionsCombo.setName("defaultVersions");
        defaultVersionsCombo.setEditable(false);
        defaultVersionsCombo.setPreferredSize(new Dimension(175, 20));
        right.add(defaultVersionsCombo);

        defaultVersionsCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                String version = e.getItem().toString();
                int versionnum = Integer.parseInt(version.split("-")[0].trim());
                dataset.setDefaultVersion(versionnum);
            }
        });

        container.add(right, BorderLayout.LINE_END);

        return container;
    }

    private String getDefaultVersion(VersionsSet versionsSet) {
        String name = versionsSet.getDefaultVersionName(dataset.getDefaultVersion());
        return displayableVersion(name, dataset.getDefaultVersion());
    }

    private String[] getVersionNames(VersionsSet versionsSet) {
        String[] versionNames = versionsSet.names();
        Integer[] versionNums = versionsSet.versions();

        List versions = new ArrayList();
        for (int i = 0; i < versionNames.length; i++) {
            String version = displayableVersion(versionNames[i], versionNums[i].intValue());
            versions.add(version);
        }

        return (String[]) versions.toArray(new String[0]);
    }

    private String displayableVersion(String name, int version) {
        return version + " - " + name;
    }

    public void reload(Version[] versions) {
        tablePanel.removeAll();

        // reload table
        ScrollableTable table = createTable(versions);
        tablePanel.add(table, BorderLayout.CENTER);

        // reload default version list
        VersionsSet versionsSet = new VersionsSet(versions);
        ComboBoxModel model = new DefaultComboBoxModel(versionsSet.finalVersions());
        defaultVersionsCombo.setModel(model);

        refreshLayout();
    }

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

        ScrollableTable table = new ScrollableTable(tableModel);
        table.disableScrolling();
        return table;
    }

    private JPanel bottomPanel(InternalSource[] sources) {
        JPanel container = new JPanel(new BorderLayout());

        container.add(leftControlPanel(), BorderLayout.LINE_START);
        container.add(rightControlPanel(sources), BorderLayout.LINE_END);

        return container;
    }

    private JPanel leftControlPanel() {
        JPanel panel = new JPanel();

        Button view = new Button("New", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doNew(tableData.getValues());
            }
        });
        panel.add(view);

        Button markFinal = new Button("Mark Final", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doMarkFinal(tableData.selected());
            }
        });
        panel.add(markFinal);

        return panel;
    }

    protected void doNew(Version[] versions) {
        clear();

        NewVersionDialog dialog = new NewVersionDialog(dataset, versions, parentConsole);
        dialog.run();

        if (dialog.shouldCreate()) {
            try {
                presenter.doNew(dialog.version(), dialog.name());
            } catch (EmfException e) {
                displayError("Could not create new Version: " + dialog.name() + ". Reason: " + e.getMessage());
            }
        }
    }

    protected void doMarkFinal(Version[] versions) {
        clear();

        try {
            presenter.doMarkFinal(versions);
        } catch (EmfException e) {
            displayError("Could not mark Final. Reason: " + e.getMessage());
        }
    }

    private JPanel rightControlPanel(InternalSource[] sources) {
        JPanel panel = new JPanel();

        panel.add(new Label("Table:"));

        DefaultComboBoxModel tablesModel = new DefaultComboBoxModel(tableNames(sources));
        final JComboBox tableCombo = new JComboBox(tablesModel);
        tableCombo.setSelectedItem("Select Table");
        tableCombo.setName("tables");
        tableCombo.setEditable(false);
        tableCombo.setPreferredSize(new Dimension(175, 20));
        panel.add(tableCombo);

        Button view = new Button("View", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doView(tableCombo);
            }
        });
        panel.add(view);

        Button edit = new Button("Edit", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doEdit(tableCombo);
            }
        });
        panel.add(edit);

        return panel;
    }

    private void doView(final JComboBox tableCombo) {
        clear();

        String table = (String) tableCombo.getSelectedItem();
        if (table.equals("Select Table")) {
            displayError("Please select a Table");
            return;
        }

        Version[] versions = tableData.selected();
        if (versions.length < 1) {
            displayError("Please select at least one Version");
            return;
        }

        for (int i = 0; i < versions.length; i++)
            showView(table, versions[i]);
    }

    private void showView(String table, Version version) {
        NonEditableDataViewWindow view = new NonEditableDataViewWindow(dataset);
        parentConsole.addToDesktop(view);
        try {
            presenter.doView(version, table, view);
        } catch (EmfException e) {
            displayError("Could not open Viewer. Reason: " + e.getMessage());
        }
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
        if (table.equals("Select Table")) {
            displayError("Please select a Table");
            return;
        }

        Version[] versions = tableData.selected();
        if (versions.length != 1) {
            displayError("Please select one Version");
            return;
        }

        showEditor(table, versions[0]);
    }

    private void showEditor(String table, Version version) {
        EditableDataViewWindow view = new EditableDataViewWindow(dataset);
        parentConsole.addToDesktop(view);
        try {
            presenter.doEdit(version, table, view);
        } catch (EmfException e) {
            displayError("Could not open Editor. Reason: " + e.getMessage());
        }
    }

    private void clear() {
        messagePanel.clear();
        refreshLayout();
    }

    private String[] tableNames(InternalSource[] sources) {
        List tables = new ArrayList();
        tables.add("Select Table");
        for (int i = 0; i < sources.length; i++)
            tables.add(sources[i].getTable());

        return (String[]) tables.toArray(new String[0]);
    }

}
