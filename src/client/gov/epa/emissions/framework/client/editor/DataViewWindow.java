package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.ui.EmfTableModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;

public class DataViewWindow extends DisposableInteralFrame implements DataView {

    private EmfTableModel model;

    private JPanel layout;

    private MessagePanel messagePanel;

    public DataViewWindow() {
        super("Data Viewer: ", new Dimension(600, 400));

        layout = new JPanel();
        this.getContentPane().add(layout);
    }

    public void display(Dataset dataset) {
        super.setTitle(super.getTitle() + dataset.getName());
        
        JPanel container = new JPanel(new BorderLayout());

        container.add(topPanel(dataset.getInternalSources()), BorderLayout.PAGE_START);
        container.add(pagePanel(), BorderLayout.CENTER);
        container.add(bottomPanel(), BorderLayout.PAGE_END);

        layout.add(container);

        super.display();
    }

    private JPanel topPanel(InternalSource[] sources) {
        JPanel panel = new JPanel(new BorderLayout());

        DefaultComboBoxModel tablesModel = new DefaultComboBoxModel(tables(sources));
        JComboBox tableCombo = new JComboBox(tablesModel);
        tableCombo.setSelectedItem("Select Table");
        tableCombo.setName("tables");
        tableCombo.setEditable(false);
        tableCombo.setPreferredSize(new Dimension(175, 20));

        panel.add(new Label("Version:"), BorderLayout.LINE_START);
        panel.add(tableCombo, BorderLayout.LINE_END);

        return panel;
    }

    private String[] tables(InternalSource[] sources) {
        List tables = new ArrayList();
        tables.add("Select Table");
        for (int i = 0; i < sources.length; i++)
            tables.add(sources[i].getTable());

        return (String[]) tables.toArray(new String[0]);
    }

    private JPanel pagePanel() {
        return new JPanel();
    }

    private JPanel bottomPanel() {
        return new JPanel();
    }

}
