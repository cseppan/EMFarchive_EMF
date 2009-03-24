package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.CopyButton;
import gov.epa.emissions.commons.gui.buttons.ViewButton;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.viewer.DataViewer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
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
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
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
        if (sources.length != 0)
            add(topPanel(sources), BorderLayout.PAGE_START);
        add(tablePanel(versions), BorderLayout.CENTER);
    }

    public void reload(Version[] versions) {
        tablePanel.removeAll();

        // reload table
        ScrollableTable table = createTable(versions);
        tablePanel.add(table, BorderLayout.CENTER);

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

        ScrollableTable table = new ScrollableTable(tableModel, null);
        String[] columns = {"Select", "Version", "Base", "Is Final?"}; 
        table.setMaxColWidth(columns);
        //table.setColWidthsBasedOnColNames();
        table.disableScrolling();
        table.resetTextFont(this.getFont());
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

        Button view = new ViewButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clear();
                doView(tableCombo);
            }
        });
        
        panel.add(view);

        Button copy = new CopyButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clear();
                copyDataSet(tableCombo.getSelectedItem());
            }
        });
        
        copy.setToolTipText("Copy a Version to New Dataset");
        panel.add(copy);
        
        return panel;
    }
 
    private int getYesNoSelection(){
        String message = " Would you like to copy a version to new dataset? ";
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, "Warning", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        return selection;
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
        
        showMsg("Please go to the dataset manager window and Refresh to see the copied dataset.");
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
            presenter.doView(version, table, view);
        } catch (EmfException e) {
            displayError(e.getMessage());
        }
    }

    private void displayError(String message) {
        messagePanel.setError(message);
        refreshLayout();
    }
    
    private void showMsg(String message) {
        messagePanel.setMessage(message);
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
