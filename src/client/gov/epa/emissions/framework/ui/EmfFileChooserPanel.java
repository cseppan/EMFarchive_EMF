package gov.epa.emissions.framework.ui;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class EmfFileChooserPanel extends JPanel {

    private EmfFileSystemView fsv;

    private EmfFileInfo currentDir;

    private EmfFileInfo[] selectedFile;

    private JTextField folder;

    private JTextField filter;

    private JList filesList;

    private JList subdirsList;

    private boolean dirOnly;

    public EmfFileChooserPanel(EmfFileSystemView fsv, EmfFileInfo initialFile, boolean dirOnly) {
        this.fsv = fsv;
        this.currentDir = initialFile;
        this.dirOnly = dirOnly;
        display();
    }

    public void display() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(upperPanel());
        add(fileListPanels());
        setPreferredSize(new Dimension(415, 240));
        setBorder(BorderFactory.createEmptyBorder(20, 5, 5, 5));
    }

    private JPanel fileListPanels() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
        JPanel subdirs = subdirPanel();

        if (dirOnly) {
            container.add(subdirs);
        } else {
            getFiles();
            JPanel files = filesPanel();
            container.add(subdirs);
            container.add(new JLabel("  ")); // to fill out some space in between
            container.add(files);
        }

        return container;
    }

    public JPanel upperPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        folder = new JTextField(currentDir.getAbsolutePath(), 30);
        layoutGenerator.addLabelWidgetPair("Directory:     ", folder, panel);

        filter = new JTextField("*.*", 30);
        layoutGenerator.addLabelWidgetPair("File Filter:     ", filter, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 2, 2, // rows, cols
                0, 10, // initialX, initialY
                0, 10);// xPad, yPad

        return panel;
    }

    private JPanel subdirPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(new JLabel("Sub Directory:"), BorderLayout.NORTH);
        panel.add(listWedgit(fsv.getSubdirs(currentDir), true));

        return panel;
    }

    private JPanel filesPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JLabel("File:"), BorderLayout.NORTH);
        panel.add(listWedgit(this.selectedFile, false), BorderLayout.CENTER);

        Button getFiles = new Button("Get Files", getFilesAction());
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(getFiles, BorderLayout.LINE_END);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel listWedgit(EmfFileInfo[] files, boolean isSubdir) {
        JPanel panel = new JPanel();
        JScrollPane scrollPane;

        if (isSubdir) {
            subdirsList = new JList(files);
            subdirsList.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {
                        Object source = e.getSource();
                        EmfFileInfo fileInfo = (EmfFileInfo) ((JList) source).getSelectedValue();
                        updateDirSelections(fileInfo);
                    }
                }
            });
            scrollPane = new JScrollPane(subdirsList);
        } else {
            filesList = new JList(files);
            scrollPane = new JScrollPane(filesList);
        }

        panel.setLayout(new BorderLayout());
        panel.add(scrollPane);

        return panel;
    }

    protected void updateDirSelections(EmfFileInfo fileInfo) {
        if (fileInfo == null)
            return;

        currentDir = fileInfo;
        folder.setText(currentDir.getAbsolutePath());
        subdirsList.setListData(fsv.getSubdirs(currentDir));
    }

    private Action getFilesAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                getFiles();
            }
        };
    }

    private void getFiles() {
        if (!dirOnly) {
            this.selectedFile = fsv.getFiles(this.currentDir, true);
            if (filesList != null)
                filesList.setListData(this.selectedFile);
        }
    }

    public EmfFileInfo[] selectedFiles() {
        return this.selectedFile;
    }
    
    public EmfFileInfo selectedDirectory() {
        return this.currentDir;
    }

}
