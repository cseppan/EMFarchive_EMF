package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesEditor;
import gov.epa.emissions.framework.client.meta.PropertiesEditorPresenter;
import gov.epa.emissions.framework.client.meta.PropertiesEditorPresenterImpl;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.EmfDatasetTableData;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class VersionedDataWindow extends ReusableInteralFrame implements VersionedDataView {

    private EmfConsole parentConsole;
    
    private EmfDataset dataset;

    private SingleLineMessagePanel messagePanel;

    private VersionedDataPresenter presenter;

    private JPanel layout;
    
    private JLabel defaultVersion;

    public VersionedDataWindow(EmfConsole parentConsole, DesktopManager desktopManager) {
        super("Data Versions Editor", new Dimension(750, 350), desktopManager);

        this.parentConsole = parentConsole;
        layout = new JPanel();
        this.getContentPane().add(layout);
    }

    public void display(EmfDataset dataset, EditVersionsPresenter versionsPresenter) {
        this.dataset = dataset;
        layout.setLayout(new BorderLayout());
        setTitle("Dataset Versions Editor: " + dataset.getName());
        setName("Dataset Versions Editor: " + dataset.getId());
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel, BorderLayout.PAGE_START);
        layout.add(createVersionsPanelLayout(versionsPresenter, dataset, messagePanel), BorderLayout.CENTER);
        layout.add(createControlPanel(), BorderLayout.PAGE_END);

        super.display();
    }

    private JPanel createVersionsPanelLayout(EditVersionsPresenter versionsPresenter, EmfDataset dataset,
            MessagePanel messagePanel) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        EditVersionsPanel versionsPanel = createVersionsPanel(versionsPresenter, dataset, messagePanel);
        container.add(versionsPanel);

        return container;
    }

    private EditVersionsPanel createVersionsPanel(EditVersionsPresenter versionsPresenter, EmfDataset dataset,
            MessagePanel messagePanel) {
        EditVersionsPanel versionsPanel = new EditVersionsPanel(dataset, messagePanel, parentConsole, desktopManager);
        try {
            versionsPresenter.display(versionsPanel);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }

        return versionsPanel;
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(leftControlPanel(), BorderLayout.LINE_START);
        panel.add(rightControlPanel(), BorderLayout.LINE_END);
        
        return panel;
        
    }

    private JPanel leftControlPanel() {
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BorderLayout());
        try {
            defaultVersion = new JLabel("  Default Version:  " + presenter.getDatasetNameString());
            labelPanel.add(defaultVersion, BorderLayout.LINE_START);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
        return labelPanel;
    }
    
    private JPanel rightControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JPanel editPanel = new JPanel();
        
        //New code to move Edit Properties button from elsewhere on the Window
        
        ConfirmDialog confirmDialog = new ConfirmDialog("", "Warning", parentConsole);
        SelectAwareButton propButton = new SelectAwareButton("Edit Properties", editPropAction(), getTable(),
                        confirmDialog);
        editPanel.add(propButton);
        panel.add(editPanel);

        JPanel closePanel = new JPanel();
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                presenter.doClose();
            }
        });

        closePanel.add(closeButton);
        getRootPane().setDefaultButton(closeButton);
        panel.add(closePanel, BorderLayout.EAST);

        return panel;
    }
    private SelectableSortFilterWrapper getTable() {
        EmfDatasetTableData tableData = new EmfDatasetTableData(new EmfDataset[] { dataset });
        return new SelectableSortFilterWrapper(parentConsole, tableData, sortCriteria());
    }

    private SortCriteria sortCriteria() {
        String[] columnNames = { "Last Modified Date" };
        return new SortCriteria(columnNames, new boolean[] { false }, new boolean[] { true });
    }

    private Action editPropAction() {
        // DatasetPropertiesViewer view = new DatasetPropertiesViewer(parentConsole, desktopManager);
        return new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {

                DatasetPropertiesEditor view = new DatasetPropertiesEditor(presenter.getSession(), parentConsole,
                        desktopManager);
                PropertiesEditorPresenter editPresenter = new PropertiesEditorPresenterImpl(dataset, view, presenter
                        .getSession());

                clear();
                try {
                    editPresenter.doDisplay();
                } catch (EmfException e) {
                    // NOTE Auto-generated catch block
                    messagePanel.setError(e.getMessage());
                }
            }
        };
    }
    public void observe(VersionedDataPresenter presenter) {
        this.presenter = presenter;
    }
    private void clear() {
        messagePanel.clear();
    }
}
