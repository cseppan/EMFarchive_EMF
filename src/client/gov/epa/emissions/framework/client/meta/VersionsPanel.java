package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.client.Label;
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

public class VersionsPanel extends JPanel {

    public VersionsPanel() {
        super.setLayout(new BorderLayout());
        super.setBorder(new Border("Versions"));
    }

    public void display(Version[] versions, InternalSource[] sources) {
        add(tablePanel(versions), BorderLayout.CENTER);
        add(bottomPanel(sources), BorderLayout.PAGE_END);
    }

    private JPanel tablePanel(Version[] versions) {
        EmfTableModel tableModel = new EmfTableModel(new VersionsTableData(versions));
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
                // TODO: launch VersionedDataView
            }
        });
        panel.add(view);

        return panel;
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
                String table = (String) tableCombo.getSelectedItem();
                // TODO: launch VersionedDataView
            }
        });
        panel.add(view);

        return panel;
    }

    private String[] tableNames(InternalSource[] sources) {
        List tables = new ArrayList();
        tables.add("Select Table");
        for (int i = 0; i < sources.length; i++)
            tables.add(sources[i].getTable());

        return (String[]) tables.toArray(new String[0]);
    }
}
