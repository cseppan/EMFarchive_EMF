package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.editor.VersionedDataViewWindow;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.ScrollableTable;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class VersionsPanel extends JPanel implements VersionsView {

    private VersionsTableData tableData;

    private MessagePanel messagePanel;

    private VersionsPresenter presenter;

    private EmfDataset dataset;

    private EmfConsole parentConsole;

    public VersionsPanel(EmfDataset dataset, MessagePanel messagePanel, EmfConsole parentConsole) {
        super.setLayout(new BorderLayout());
        super.setBorder(new Border("Versions"));

        this.dataset = dataset;
        this.messagePanel = messagePanel;
        this.parentConsole = parentConsole;
    }

    public void observe(VersionsPresenter presenter) {
        this.presenter = presenter;
    }

    public void display(Version[] versions, InternalSource[] sources) {
        add(tablePanel(versions), BorderLayout.CENTER);
        add(bottomPanel(sources), BorderLayout.PAGE_END);
    }

    private JPanel tablePanel(Version[] versions) {
        tableData = new VersionsTableData(versions);
        EmfTableModel tableModel = new EmfTableModel(tableData);
        JScrollPane table = new ScrollableTable(tableModel);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(table, BorderLayout.CENTER);

        return panel;
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
                doNew();
            }
        });
        panel.add(view);

        return panel;
    }

    protected void doNew() {
        clear();
        // TODO: launch VersionedDataView
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

        return panel;
    }

    private void doView(final JComboBox tableCombo) {
        clear();

        String table = (String) tableCombo.getSelectedItem();
        if (table.equals("Select Table"))
            return;

        Version[] versions = tableData.selected();
        if (versions.length != 1)
            messagePanel.setError("Please select only one Version");

        VersionedDataViewWindow view = new VersionedDataViewWindow(dataset);
        parentConsole.addToDesktop(view);
        presenter.doView(versions[0], table, view);
    }

    private void clear() {
        messagePanel.clear();
    }

    private String[] tableNames(InternalSource[] sources) {
        List tables = new ArrayList();
        tables.add("Select Table");
        for (int i = 0; i < sources.length; i++)
            tables.add(sources[i].getTable());

        return (String[]) tables.toArray(new String[0]);
    }

}
