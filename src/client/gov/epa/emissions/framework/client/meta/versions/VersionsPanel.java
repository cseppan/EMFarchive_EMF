package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.viewer.DataViewer;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.ScrollableTable;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
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

    private VersionsViewPresenter presenter;

    private EmfDataset dataset;

    private EmfConsole parentConsole;

    private EmfTableModel tableModel;

    private JPanel tablePanel;

    private JComboBox defaultVersionsCombo;

    private DesktopManager desktopManager;

    public VersionsPanel(EmfDataset dataset, MessagePanel messagePanel, EmfConsole parentConsole,
            DesktopManager desktopManager) {
        super.setLayout(new BorderLayout());
        setBorder();

        this.dataset = dataset;
        this.messagePanel = messagePanel;
        this.parentConsole = parentConsole;
        this.desktopManager = desktopManager;
    }

    private void setBorder() {
        javax.swing.border.Border outer = BorderFactory.createEmptyBorder(5, 2, 5, 2);
        CompoundBorder border = BorderFactory.createCompoundBorder(outer, new Border("Versions"));
        super.setBorder(border);
    }

    public void observe(VersionsViewPresenter presenter) {
        this.presenter = presenter;
    }

    public void display(Version[] versions, InternalSource[] sources) {
        VersionsSet versionsSet = new VersionsSet(versions);

        if (sources.length != 0)
            add(topPanel(sources), BorderLayout.PAGE_START);
        add(tablePanel(versions), BorderLayout.CENTER);
        add(bottomPanel(versionsSet), BorderLayout.SOUTH);
    }

    private JPanel bottomPanel(VersionsSet versionsSet) {
        JPanel container = new JPanel(new BorderLayout());

        JPanel right = new JPanel();
        right.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 5));
        right.add(new JLabel("Default Version: "));

        String name = versionsSet.getVersionName(dataset.getDefaultVersion());
        if (name == null)
        {
            name = "N/A";
        }
        right.add(new JLabel(displayableVersion(name, dataset)));

        container.add(right, BorderLayout.CENTER);

        return container;
    }

    private String displayableVersion(String name, EmfDataset dataset) {
        return dataset.getDefaultVersion() + " - " + name;
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
        table.setColWidthsBasedOnColNames();
        table.disableScrolling();
        return table;
    }

    private JPanel topPanel(InternalSource[] sources) {
        JPanel container = new JPanel(new BorderLayout());
        container.add(rightControlPanel(sources), BorderLayout.CENTER);

        return container;
    }

    private JPanel rightControlPanel(InternalSource[] sources) {
        JPanel panel = new JPanel();

        panel.add(new Label("Table:"));

        DefaultComboBoxModel tablesModel = new DefaultComboBoxModel(tableNames(sources));
        final JComboBox tableCombo = new JComboBox(tablesModel);
        tableCombo.setName("tables");
        tableCombo.setEditable(false);
        panel.add(tableCombo);

        Button view = new Button("View", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doView(tableCombo);
            }
        });
        panel.add(view);

        return panel;
    }

    private void doView(final JComboBox tableCombo) {
        clear();

        String table = (String) tableCombo.getSelectedItem();
        Version[] versions = tableData.selected();
        if (versions.length < 1) {
            displayError("Please select at least one Version");
            return;
        }

        for (int i = 0; i < versions.length; i++)
            showView(table, versions[i]);
    }

    private void showView(String table, Version version) {
        DataViewer view = new DataViewer(dataset, parentConsole, desktopManager);
        try {
            presenter.doView(version, table, view);
        } catch (EmfException e) {
            displayError(e.getMessage());
        }
    }

    private void displayError(String message) {
        messagePanel.setError(message);
        refreshLayout();
    }

    private void refreshLayout() {
        super.validate();
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

}
