package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.services.EmfDataset;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class ExportWindow extends DisposableInteralFrame implements ExportView {

    private EmfDataset[] datasets;

    private SingleLineMessagePanel messagePanel;

    private JTextField folder;

    private ExportPresenter presenter;

    private JCheckBox overwrite;

    private JTextArea purpose;

    public ExportWindow(EmfDataset[] datasets) {
        super("Export Dataset(s)");
        super.setName("exportWindow");
        this.datasets = datasets;

        super.setSize(new Dimension(600, 300));

        this.getContentPane().add(createLayout());
    }

    public void observe(ExportPresenter presenter) {
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
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        // datasets
        JTextArea datasetNames = new JTextArea(2, 45);
        datasetNames.setLineWrap(false);
        datasetNames.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(datasetNames, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        datasetNames.setText(getDatasetsLabel(datasets));

        layoutGenerator.addLabelWidgetPair("Datasets", scrollPane, panel);

        // folder
        folder = new JTextField(40);
        folder.setName("folder");
        layoutGenerator.addLabelWidgetPair("Folder", folder, panel);

        // purpose
        purpose = new TextArea("purpose", "");
        purpose.setSize(2, 45);
        purpose.setLineWrap(false);
        JScrollPane purposeScrollPane = new JScrollPane(purpose, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        layoutGenerator.addLabelWidgetPair("Purpose", purposeScrollPane, panel);

        // overwrite
        JPanel overwritePanel = new JPanel(new BorderLayout());
        overwrite = new JCheckBox("Overwrite?", true);
        overwrite.setEnabled(true);
        overwrite.setName("overwrite");
        overwritePanel.add(overwrite, BorderLayout.LINE_START);

        panel.add(new JPanel());// filler
        panel.add(overwritePanel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 4, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 5);// xPad, yPad

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
            if (!overwrite.isSelected())
                presenter.doExportWithoutOverwrite(datasets, folder.getText(), purpose.getText());
            else
                presenter.doExport(datasets, folder.getText(), purpose.getText());

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
