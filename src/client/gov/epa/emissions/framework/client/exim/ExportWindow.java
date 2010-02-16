package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.BrowseButton;
import gov.epa.emissions.commons.gui.buttons.ExportButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.EmfFileChooser;
import gov.epa.emissions.framework.ui.ImageResources;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

public class ExportWindow extends DisposableInteralFrame implements ExportView {

    private EmfDataset[] datasets;

    private SingleLineMessagePanel messagePanel;

    private JTextField folder;

    private ExportPresenter presenter;

    private JCheckBox overwrite;

    private TextArea purpose;

    private JButton exportButton;

    private EmfConsole parentConsole;

    private DataCommonsService service;

    public ExportWindow(EmfDataset[] datasets, DesktopManager desktopManager, EmfConsole parentConsole,
            EmfSession session) {
        super(title(datasets), desktopManager);
        super.setName("exportWindow:" + hashCode());

        this.datasets = datasets;
        this.parentConsole = parentConsole;
        this.service = session.dataCommonsService();

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
        JPanel panel = new JPanel();
        int width = 40;
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));

        // datasets
        JPanel datasetNamesPanel = new JPanel(new BorderLayout(4,10));
        
        TextArea datasetNames = new TextArea("datasets", getDatasetsLabel(datasets), width, 6);
        datasetNames.setEditable(false);
        JScrollPane dsArea = new JScrollPane(datasetNames, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        datasetNamesPanel.add(new JLabel("Datasets  "),BorderLayout.WEST);
        datasetNamesPanel.add(dsArea);

        // folder
        JPanel chooser = new JPanel(new BorderLayout(10, 10));
        folder = new TextField("folder", width);
        chooser.add(new JLabel("Folder     "),BorderLayout.WEST);
        chooser.add(folder);
        chooser.add(browseFileButton(), BorderLayout.EAST);
        
        // purpose
        JPanel purposePanel = new JPanel(new BorderLayout(4,10));
        purpose = new TextArea("purpose", "", width, 6);
        JScrollPane purposeArea = new JScrollPane(purpose, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        purposePanel.add(new JLabel("Purpose   "),BorderLayout.WEST);
        purposePanel.add(purposeArea);
 
        // overwrite
        JPanel overwritePanel = new JPanel(new BorderLayout());
        overwrite = new JCheckBox("Overwrite files if they exist?", false);
        overwrite.setEnabled(true);
        overwrite.setName("overwrite");
        //overwrite.setVisible(false);
        overwritePanel.add(overwrite, BorderLayout.LINE_START);
        overwritePanel.setVisible(false);
        
        mainPanel.add(datasetNamesPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(chooser);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(purposePanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(overwritePanel);
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,20));
        panel.setLayout(new BorderLayout(10,10));
        panel.add(mainPanel,BorderLayout.NORTH);       
 
        return panel;
    }
    
    private JButton browseFileButton() {
        Button button = new BrowseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                selectFolder();
            }
        });   
        Icon icon = new ImageResources().open("Export a Dataset");
        button.setIcon(icon);
        return button;
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

        exportButton = new ExportButton(new AbstractAction() {
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
            validateFolder(folder.getText());
            
            if (!overwrite.isSelected())
                presenter.doExport(datasets, folder.getText(), purpose.getText());
            else
                presenter.doExportWithOverwrite(datasets, folder.getText(), purpose.getText());

            messagePanel.setMessage("Started export. Please monitor the Status window "
                    + "to track your Export request.");

            exportButton.setEnabled(false);
        } catch (EmfException e) {
            e.printStackTrace();
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
        EmfFileInfo initDir = new EmfFileInfo(folder.getText(), true, true);
        
        EmfFileChooser chooser = new EmfFileChooser(initDir, new EmfFileSystemView(service));
        chooser.setTitle("Select a folder to contain the exported Datasets");
        int option = chooser.showDialog(parentConsole, "Select a folder");

        EmfFileInfo file = (option == EmfFileChooser.APPROVE_OPTION) ? chooser.getSelectedDir() : null;
        if (file == null)
            return;

        if (file.isDirectory()) {
            folder.setText(file.getAbsolutePath());
        }
    }
    
    private void validateFolder(String folder) throws EmfException {
        if (folder == null || folder.trim().isEmpty())
            throw new EmfException("Please specify a valid export folder.");
        
        if (folder.contains("/home/") || folder.endsWith("/home")) {
            throw new EmfException("Export data into user's home directory is not allowed.");
        }
    }

}
