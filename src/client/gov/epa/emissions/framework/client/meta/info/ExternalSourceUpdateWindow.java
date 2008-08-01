package gov.epa.emissions.framework.client.meta.info;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.buttons.BrowseButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;
import gov.epa.emissions.framework.services.data.DataCommonsService;
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

public class ExternalSourceUpdateWindow extends DisposableInteralFrame implements ExternalSourceUpdateView {

    private SingleLineMessagePanel messagePanel;

    private JTextField folder;

    private ExternalSourceUpdatePresenter presenter;

    private JCheckBox massLoc;
    
    private JCheckBox workLoc;

    private TextArea purpose;

    private JButton updateButton;

    private EmfConsole parentConsole;

    private DataCommonsService service;

    public ExternalSourceUpdateWindow(String title, DesktopManager desktopManager, EmfConsole parentConsole, EmfSession session) {
        super(title, desktopManager);
        super.setName("externalSourceUpdateWindow:" + hashCode());

        this.parentConsole = parentConsole;
        this.service = session.dataCommonsService();

        this.getContentPane().add(createLayout());
        this.pack();
    }

    public void observe(ExternalSourceUpdatePresenter presenter) {
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
        folder = new JTextField(40);
        folder.setName("folder");
        Button button = new BrowseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                selectFolder();
            }
        });
        Icon icon = new ImageResources().open("Export a Dataset");
        button.setIcon(icon);

        JPanel folderPanel = new JPanel(new BorderLayout(2, 0));
        folderPanel.add(folder, BorderLayout.LINE_START);
        folderPanel.add(button, BorderLayout.LINE_END);
        layoutGenerator.addLabelWidgetPair("Folder", folderPanel, panel);

        // purpose
        purpose = new TextArea("purpose", "");
        purpose.setSize(2, 45);
        purpose.setLineWrap(false);
        layoutGenerator.addLabelWidgetPair("Purpose", new ScrollableComponent(purpose), panel);

        // work location, mass storage location
        JPanel workLocPanel = new JPanel(new BorderLayout());
        workLoc = new JCheckBox("Is this a work location?", false);
        workLoc.setEnabled(true);
        workLoc.setName("worklocation");
        workLocPanel.add(workLoc, BorderLayout.LINE_START);

        panel.add(new JPanel());// filler
        panel.add(workLocPanel);
        
        JPanel massLocPanel = new JPanel(new BorderLayout());
        massLoc = new JCheckBox("Is this a mass storage location?", false);
        massLoc.setEnabled(true);
        massLoc.setName("masslocation");
        massLocPanel.add(massLoc, BorderLayout.LINE_START);

        panel.add(new JPanel());// filler
        panel.add(massLocPanel);


        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 4, 2, // rows, cols
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

        updateButton = new Button("Update", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                clearMessagePanel();
                update();
            }
        });
        container.add(updateButton);
        getRootPane().setDefaultButton(updateButton);

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

    private void update() {
        try {
            presenter.update(folder.getText(), purpose.getText(), workLoc.isSelected(), massLoc.isSelected());
            updateButton.setEnabled(false);
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
        EmfFileInfo initDir = new EmfFileInfo(folder.getText(), true, true);

        EmfFileChooser chooser = new EmfFileChooser(initDir, new EmfFileSystemView(service));
        chooser.setTitle("Select a folder containing the external files for the dataset");
        int option = chooser.showDialog(parentConsole, "Select a folder");

        EmfFileInfo file = (option == EmfFileChooser.APPROVE_OPTION) ? chooser.getSelectedDir() : null;
        if (file == null)
            return;

        if (file.isDirectory()) {
            folder.setText(file.getAbsolutePath());
        }
    }

}
