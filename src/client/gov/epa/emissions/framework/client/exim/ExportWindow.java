package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ScrollableTextArea;
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
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class ExportWindow extends DisposableInteralFrame implements ExportView {

    private EmfDataset[] datasets;

    private SingleLineMessagePanel messagePanel;

    private JTextField folder;

    private ExportPresenter presenter;

    private JCheckBox overwrite;

    private TextArea purpose;

    public ExportWindow(EmfDataset[] datasets) {
        super(title(datasets), new Dimension(600, 350));
        super.setName("exportWindow");
        this.datasets = datasets;

        this.getContentPane().add(createLayout());
    }

    private static String title(EmfDataset[] datasets) {
        StringBuffer buf = new StringBuffer("Export: ");
        for (int i = 0; i < datasets.length; i++) {
            buf.append(datasets[i].getName());
            if (i + 1 <= datasets.length)
                buf.append(", ");
         }

        return buf.toString();
    }

    public void observe(ExportPresenter presenter) {
        this.presenter = presenter;
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
        TextArea datasetNames = new TextArea("datasets", getDatasetsLabel(datasets));
        datasetNames.setLineWrap(false);
        datasetNames.setEditable(false);
        ScrollableTextArea dsArea = new ScrollableTextArea(datasetNames);
        dsArea.setMinimumSize(new Dimension(75,75));
        layoutGenerator.addLabelWidgetPair("Datasets", dsArea, panel);

        // folder
        folder = new JTextField(40);
        folder.setName("folder");
        layoutGenerator.addLabelWidgetPair("Folder", folder, panel);

        // purpose
        purpose = new TextArea("purpose", "");
        purpose.setSize(2, 45);
        purpose.setLineWrap(false);
        layoutGenerator.addLabelWidgetPair("Purpose", new ScrollableTextArea(purpose), panel);

        // overwrite
        JPanel overwritePanel = new JPanel(new BorderLayout());
        overwrite = new JCheckBox("Overwrite files if they exist?", true);
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
                presenter.doExport(datasets, folder.getText(), purpose.getText());
            else
                presenter.doExportWithOverwrite(datasets, folder.getText(), purpose.getText());

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
