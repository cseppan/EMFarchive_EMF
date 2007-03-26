package gov.epa.emissions.framework.client.cost.controlmeasure.io;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.BrowseButton;
import gov.epa.emissions.commons.gui.buttons.ExportButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.ui.EmfFileChooser;
import gov.epa.emissions.framework.ui.ImageResources;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.Icon;
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
    
    private EmfSession session;
    
    private EmfConsole parentConsole;

    public CMExportWindow(ControlMeasure[] controlMeasures, DesktopManager desktopManager, int totalMeasuers, EmfSession session, EmfConsole parentConsole) {
        super(title(controlMeasures, totalMeasuers), desktopManager);
        super.setName("cmExportWindow:" + hashCode());

        this.controlMeasures = controlMeasures;
        this.session = session;
        this.parentConsole = parentConsole;

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
        Icon icon = new ImageResources().open("Export a Control Measure");
        button.setIcon(icon);
        
        JPanel folderPanel = new JPanel(new BorderLayout(2,0));
        folderPanel.add(folder, BorderLayout.LINE_START);
        folderPanel.add(button, BorderLayout.LINE_END);
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
                presenter.doExportWithoutOverwrite(getControlMeasureIds(controlMeasures), folder.getText(), prefix.getText());
            else
                presenter.doExportWithOverwrite(getControlMeasureIds(controlMeasures), folder.getText(), prefix.getText());

            messagePanel.setMessage("Started export. Please monitor the Status window "
                    + "to track your Export request.");
            exportButton.setEnabled(false);
        } catch (EmfException e) {
            exportButton.setEnabled(true);
            messagePanel.setError(e.getMessage());
        }
    }

    private int[] getControlMeasureIds(ControlMeasure[] cms) {
        int[] ids = new int[cms.length];
        
        for (int i = 0; i < cms.length; i++)
            ids[i] = cms[i].getId();
        
        return ids;
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
        EmfFileInfo initDir = new EmfFileInfo(folder.getText(), true, true);
        
        EmfFileChooser chooser = new EmfFileChooser(initDir, new EmfFileSystemView(session.dataCommonsService()));
        chooser.setTitle("Select a folder to hold the exported control measure files");
        int option = chooser.showDialog(parentConsole, "Select a folder");

        EmfFileInfo file = (option == EmfFileChooser.APPROVE_OPTION) ? chooser.getSelectedDir() : null;
        if (file == null)
            return;

        if (file.isDirectory()) {
            folder.setText(file.getAbsolutePath());
        }
    }
}
