package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.FileChooser;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;

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

    public ExportWindow(EmfDataset[] datasets, DesktopManager desktopManager) {
        super(title(datasets), desktopManager);
        super.setName("exportWindow:"+hashCode());
        
        this.datasets = datasets;

        this.getContentPane().add(createLayout());
        this.pack();
    }

    private static String title(EmfDataset[] datasets) {
        StringBuffer buf = new StringBuffer("Export: ");
        for (int i = 0; i < datasets.length; i++) {
            buf.append(datasets[i].getName());
            if (i + 1 < datasets.length)
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
        datasetNames.setEditable(false);
        ScrollableComponent dsArea = new ScrollableComponent(datasetNames);
        datasetNames.setWrapStyleWord(true);
        datasetNames.setLineWrap(true);
        dsArea.setMinimumSize(new Dimension(75, 75));
        layoutGenerator.addLabelWidgetPair("Datasets", dsArea, panel);

        // folder
        folder = new JTextField(40);
        folder.setName("folder");
        Button button = new Button("Choose Folder", new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                selectFolder();
            }
        });
        JPanel folderPanel = new JPanel(new BorderLayout());
        folderPanel.add(folder);
        folderPanel.add(button, BorderLayout.EAST);
        layoutGenerator.addLabelWidgetPair("Folder", folderPanel, panel);

        // purpose
        purpose = new TextArea("purpose", "");
        purpose.setSize(2, 45);
        purpose.setLineWrap(false);
        layoutGenerator.addLabelWidgetPair("Purpose", new ScrollableComponent(purpose), panel);

        // overwrite
        JPanel overwritePanel = new JPanel(new BorderLayout());
        overwrite = new JCheckBox("Overwrite files if they exist?", false);
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

    private void selectFolder() {
        FileChooser chooser = new FileChooser("Select Folder", new File(folder.getText()), ExportWindow.this);

        chooser.setTitle("Select a folder");
        File[] file = chooser.choose();
        if (file == null)
            return;

        if (file[0].isDirectory()) {
            folder.setText(file[0].getAbsolutePath());
        }

        if (file[0].isFile()) {
            folder.setText(file[0].getParent());
        }
    }

}
