package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.services.EmfDataset;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ExportWindow extends ReusableInteralFrame implements ExportView {

    private EmfDataset[] datasets;

    private SingleLineMessagePanel messagePanel;

    private JTextField folder;

    private ExportPresenter presenter;

    public ExportWindow(EmfDataset[] datasets) {
        super("Export Dataset(s)");
        this.datasets = datasets;

        super.setSize(new Dimension(600, 225));

        JPanel layoutPanel = createLayout();
        this.getContentPane().add(layoutPanel);
    }

    public void register(ExportPresenter presenter) {
        this.presenter = presenter;
    }

    public void close() {
        this.dispose();
    }

    private JPanel createLayout() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);
        panel.add(createExportPanel());
        panel.add(createButtonsPanel());

        return panel;
    }

    private JPanel createExportPanel() {
        JPanel panel = new JPanel();

        JPanel labelsPanel = new JPanel();
        labelsPanel.setLayout(new BoxLayout(labelsPanel, BoxLayout.Y_AXIS));

        labelsPanel.add(new JLabel("Datasets"));
        labelsPanel.add(Box.createRigidArea(new Dimension(1, 45)));
        labelsPanel.add(new JLabel("Folder"));
        panel.add(labelsPanel);

        JPanel valuesPanel = new JPanel();
        valuesPanel.setLayout(new BoxLayout(valuesPanel, BoxLayout.Y_AXIS));

        JTextArea datasetNames = new JTextArea(2, 15);
        datasetNames.setLineWrap(false);
        datasetNames.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(datasetNames, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        datasetNames.setText(getDatasetsLabel(datasets));
        valuesPanel.add(scrollPane);
        valuesPanel.add(Box.createRigidArea(new Dimension(1, 15)));
        folder = new JTextField(35);
        folder.setName("folder");
        valuesPanel.add(folder);

        panel.add(valuesPanel);

        // TODO: needs to be implemented
        JPanel overwritePanel = new JPanel();
        overwritePanel.setLayout(new BoxLayout(overwritePanel, BoxLayout.Y_AXIS));
        JCheckBox overwrite = new JCheckBox("Overwrite?", true);
        overwrite.setEnabled(false);
        overwrite.setToolTipText("To be implemented");
        overwritePanel.add(Box.createRigidArea(new Dimension(1, 65)));
        overwritePanel.add(overwrite);

        panel.add(overwritePanel);

        return panel;
    }

    private String getDatasetsLabel(EmfDataset[] datasets) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < datasets.length; i++) {
            buf.append(datasets[i].getName());
            if (i + 1 < datasets.length)
                buf.append(", ");
        }
        return buf.toString();
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(25);
        container.setLayout(layout);

        JButton exportButton = new Button("Export", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                if (presenter == null)
                    return;

                clearMessagePanel();
                doExport();
            }
        });
        container.add(exportButton);
        getRootPane().setDefaultButton(exportButton);

        JButton done = new Button("Done", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                if (presenter != null)
                    presenter.notifyDone();
            }
        });
        container.add(done);

        panel.add(container, BorderLayout.EAST);

        return panel;
    }

    private void refresh() {
        super.validate();
    }

    private void doExport() {
        try {
            presenter.notifyExport(datasets, folder.getText());
            messagePanel.setMessage("Started export. Please monitor the Status window "
                    + "to track your Export request.");
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private void clearMessagePanel() {
        messagePanel.clear();
        refresh();
    }

    public void setMostRecentUsedFolder(String mostRecentUsedFolder) {
        if (mostRecentUsedFolder != null)
            folder.setText(mostRecentUsedFolder);
    }
}
