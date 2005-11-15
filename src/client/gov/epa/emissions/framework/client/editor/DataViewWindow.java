package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.services.DataEditorServices;
import gov.epa.emissions.framework.ui.Border;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;

public class DataViewWindow extends DisposableInteralFrame implements DataView {

    private JPanel layout;

    private MessagePanel messagePanel;

    private DataViewPresenter presenter;

    private JPanel pageContainer;

    private Dataset dataset;

    private DataEditorServices services;

    public DataViewWindow(DataEditorServices services) {
        super("Data Viewer: ", new Dimension(900, 700));
        this.services = services;

        layout = new JPanel(new BorderLayout());
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel, BorderLayout.PAGE_START);

        this.getContentPane().add(layout);
    }

    public void observe(DataViewPresenter presenter) {
        this.presenter = presenter;
    }

    public void display(Dataset dataset) {
        super.setTitle(super.getTitle() + dataset.getName());
        this.dataset = dataset;

        JPanel container = new JPanel(new BorderLayout());

        container.add(topPanel(dataset.getInternalSources()), BorderLayout.PAGE_START);
        container.add(pagePanel(), BorderLayout.CENTER);
        container.add(controlsPanel(), BorderLayout.PAGE_END);

        layout.add(container, BorderLayout.CENTER);

        super.display();
    }

    private JPanel topPanel(InternalSource[] sources) {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel versionPanel = new JPanel();
        versionPanel.add(new Label("Version:"));
        panel.add(versionPanel, BorderLayout.LINE_START);

        DefaultComboBoxModel tablesModel = new DefaultComboBoxModel(tableNames(sources));
        JComboBox tableCombo = new JComboBox(tablesModel);
        tableCombo.setSelectedItem("Select Table");
        tableCombo.setName("tables");
        tableCombo.setEditable(false);
        tableCombo.addActionListener(new TableSelectionListener());
        tableCombo.setPreferredSize(new Dimension(175, 20));

        JPanel tablesPanel = new JPanel();
        tablesPanel.add(tableCombo);
        panel.add(tablesPanel, BorderLayout.LINE_END);

        return panel;
    }

    private String[] tableNames(InternalSource[] sources) {
        List tables = new ArrayList();
        tables.add("Select Table");
        for (int i = 0; i < sources.length; i++)
            tables.add(sources[i].getTable());

        return (String[]) tables.toArray(new String[0]);
    }

    private JPanel pagePanel() {
        pageContainer = new JPanel(new BorderLayout());
        pageContainer.setBorder(new Border("Data"));

        return pageContainer;
    }

    private JPanel controlsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        Button close = new Button("Close", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                presenter.doClose();
            }
        });
        panel.add(close, BorderLayout.LINE_END);

        return panel;
    }

    public class TableSelectionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JComboBox cb = (JComboBox) e.getSource();
            String table = (String) cb.getSelectedItem();
            showTable(table);
        }
    }

    public void showTable(String table) {
        if (table.equals("Select Table"))
            return;

        PageViewPanel panel = new PageViewPanel(source(table, dataset.getInternalSources()), messagePanel);
        pageContainer.add(panel, BorderLayout.CENTER);

        PageViewPresenter presenter = new PageViewPresenter(services, panel, table);
        presenter.observeView();
        try {
            presenter.doDisplayNext();
        } catch (EmfException e) {
            messagePanel.setError("Could not fetch first page of table: " + table + ". Reason: " + e.getMessage());
        }
    }

    private InternalSource source(String table, InternalSource[] sources) {
        for (int i = 0; i < sources.length; i++) {
            if (sources[i].getTable().equals(table))
                return sources[i];
        }

        return null;
    }
}
