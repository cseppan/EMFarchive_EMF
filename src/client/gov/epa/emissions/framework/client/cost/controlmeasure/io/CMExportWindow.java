package gov.epa.emissions.framework.client.cost.controlmeasure.io;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.BrowseButton;
import gov.epa.emissions.commons.gui.buttons.ExportButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.ui.FileChooser;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
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

public class CMExportWindow extends DisposableInteralFrame implements CMExportView {

    private ControlMeasure[] controlMeasures;

    private SingleLineMessagePanel messagePanel;

    private JTextField folder;

    private CMExportPresenter presenter;

    private JCheckBox overwrite;

    private JTextField prefix;
    
    private JButton exportButton;

    public CMExportWindow(ControlMeasure[] controlMeasures, DesktopManager desktopManager, int totalMeasuers) {
        super(title(controlMeasures, totalMeasuers), desktopManager);
        super.setName("cmExportWindow:" + hashCode());

        this.controlMeasures = controlMeasures;

        this.getContentPane().add(createLayout());
        this.pack();
    }

    private static String title(ControlMeasure[] controlMeasures, int total) {
        int num = controlMeasures.length;
        StringBuffer buf = new StringBuffer("Exporting " + num + " of the " + total + " Control Measures.");

        return buf.toString();
    }

    public void observe(CMExportPresenter presenter) {
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

        // folder
        folder = new JTextField(30);
        folder.setName("folder");
        Button button = new BrowseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                selectFolder();
            }
        });
        JPanel folderPanel = new JPanel(new BorderLayout());
        folderPanel.add(folder);
        folderPanel.add(button, BorderLayout.EAST);
        layoutGenerator.addLabelWidgetPair("Folder", folderPanel, panel);

        // purpose
        prefix = new JTextField(30);
        prefix.setName("prefix");
        layoutGenerator.addLabelWidgetPair("Prefix:", prefix, panel);

        // overwrite
        JPanel overwritePanel = new JPanel(new BorderLayout());
        overwrite = new JCheckBox("Overwrite files if they exist?", false);
        overwrite.setEnabled(true);
        overwrite.setName("overwrite");
        overwritePanel.add(overwrite, BorderLayout.LINE_START);

        panel.add(new JPanel());// filler
        panel.add(overwritePanel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 5);// xPad, yPad

        return panel;
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(25);
        container.setLayout(layout);

        exportButton = new ExportButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                clearMessagePanel();
                doExport();
            }
        });
        container.add(exportButton);
        getRootPane().setDefaultButton(exportButton);

        JButton done = new Button("Close", new AbstractAction() {
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
                presenter.doExport(controlMeasures, folder.getText(), prefix.getText());
            else
                presenter.doExportWithOverwrite(controlMeasures, folder.getText(), prefix.getText());

            messagePanel.setMessage("Started export. Please monitor the Status window "
                    + "to track your Export request.");
            exportButton.setEnabled(false);
        } catch (EmfException e) {
            exportButton.setEnabled(true);
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
        FileChooser chooser = new FileChooser("Select Folder", new File(folder.getText()), CMExportWindow.this);

        chooser.setTitle("Select a folder");
        File[] file = chooser.choose();
        if (file == null || file.length == 0)
            return;

        if (file[0].isDirectory()) {
            folder.setText(file[0].getAbsolutePath());
        }

        if (file[0].isFile()) {
            folder.setText(file[0].getParent());
        }
    }

}
