package gov.epa.emissions.framework.ui;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;

public class EmfFileChooserPanel extends JPanel {

    private EmfFileSystemView fsv;

    private EmfFileInfo currentDir;

    private EmfFileInfo[] selectedFile;

    private JPanel filePanel;

    private JTextField folder;

    private JList subdirsList;

    private boolean dirOnly;

    private SingleLineMessagePanel messagePanel;

    private Component parent;

    private EmfFileTableData tableData;

    private EmfTableModel model;

    private SortFilterSelectModel selectModel;

    public EmfFileChooserPanel(Component parent, EmfFileSystemView fsv, EmfFileInfo initialFile, boolean dirOnly) {
        this.fsv = fsv;
        this.currentDir = initialFile;
        this.dirOnly = dirOnly;
        this.messagePanel = new SingleLineMessagePanel();
        this.parent = parent;
        display(new EmfFileInfo[0]);
    }

    public void display(EmfFileInfo[] files) {
        removeAll();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(this.messagePanel);
        add(upperPanel());
        add(fileListPanels(files));
        
        if (dirOnly)
            setPreferredSize(new Dimension(418, 300));
        else
            setPreferredSize(new Dimension(650, 400));
        
        setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
    }

    private JPanel fileListPanels(EmfFileInfo[] files) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
        JPanel subdirs = subdirPanel();

        if (dirOnly) {
            container.add(subdirs);
        } else {
            container.add(subdirs);
            container.add(new JLabel("  ")); // to fill out some space in between
            container.add(filesPanel(files));
        }

        return container;
    }

    public JPanel upperPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        folder = new JTextField(currentDir.getAbsolutePath(), 32);
        folder.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                String newvalue = ((JTextField) evt.getSource()).getText();
                EmfFileInfo fileInfo = new EmfFileInfo();
                fileInfo.setAbsolute(true);
                fileInfo.setAbsolutePath(newvalue);
                fileInfo.setDirectory(true);
                updateDirSelections(fileInfo);
            }

        });
        layoutGenerator.addLabelWidgetPair("Folder:     ", folder, panel);

        layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
                0, 10, // initialX, initialY
                0, 10);// xPad, yPad

        return panel;
    }

    private JPanel subdirPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        EmfFileInfo[] dirs = getAllDirs();
        panel.add(new JLabel("Subfolders:"), BorderLayout.NORTH);
        panel.add(subdirListWidgit(dirs));
        panel.setPreferredSize(new Dimension(100,250));

        return panel;
    }

    private JPanel filesPanel(EmfFileInfo[] files) {
        filePanel = new JPanel();

        filePanel.setLayout(new BorderLayout());
        filePanel.add(new JLabel("Files:"), BorderLayout.NORTH);

        Button getFiles = new Button("Get Files", getFilesAction());
        getFiles.setMnemonic('G');
        JPanel getFilesPanel = new JPanel(new FlowLayout());
        getFilesPanel.add(getFiles);
        JPanel middlePanel = new JPanel(new BorderLayout(2, 5));
        middlePanel.add(createFilePanel(files), BorderLayout.CENTER);
        middlePanel.add(getFilesPanel, BorderLayout.SOUTH);
        middlePanel.setBorder(BorderFactory.createEtchedBorder());

        filePanel.add(middlePanel, BorderLayout.CENTER);

        return filePanel;
    }

    private JPanel createFilePanel(EmfFileInfo[] files) {
        tableData = new EmfFileTableData(files);
        model = new EmfTableModel(tableData);
        selectModel = new SortFilterSelectModel(model);
        return new SortFilterSelectionPanel(parent, selectModel);
    }

    private JScrollPane subdirListWidgit(EmfFileInfo[] files) {
        subdirsList = new JList(files);
        subdirsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        subdirsList.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Object source = e.getSource();
                    EmfFileInfo fileInfo = (EmfFileInfo) ((JList) source).getSelectedValue();

                    if (fileInfo.getName().equals("."))
                        return;

                    updateDirSelections(fileInfo);
                }

            }

            public void mouseEntered(MouseEvent e) {
                // NOTE Auto-generated method stub

            }

            public void mouseExited(MouseEvent e) {
                // NOTE Auto-generated method stub

            }

            public void mousePressed(MouseEvent e) {
                // NOTE Auto-generated method stub

            }

            public void mouseReleased(MouseEvent e) {
                // NOTE Auto-generated method stub

            }

        });

        return new JScrollPane(subdirsList);
    }

    protected void updateDirSelections(EmfFileInfo fileInfo) {
        if (fileInfo == null || fileInfo.getAbsolutePath().equals(currentDir.getAbsolutePath()))
            return;

        currentDir = fileInfo;
        subdirsList.setListData(getAllDirs());
        refreshFiles(new EmfFileInfo[0]);
    }

    private EmfFileInfo[] getAllDirs() {
        EmfFileInfo[] dirs = fsv.getSubdirs(currentDir);

        if (dirs == null) {
            this.messagePanel.setError("Please check if the EMF service is running.");
        }

        folder.setText(dirs[0].getAbsolutePath());
        if (!dirs[0].getAbsolutePath().equals(currentDir.getAbsolutePath()))
            currentDir = dirs[0];
        return dirs;
    }

    private Action getFilesAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                getFiles();
            }
        };
    }

    private void getFiles() {
        this.selectedFile = fsv.getFiles(this.currentDir, true);
        refreshFiles(selectedFile);
    }
    
    private void refreshFiles(EmfFileInfo[] files) {
        display(files);
        this.validate();
    }

    public EmfFileInfo[] selectedFiles() {
        List selected = selectModel.selected();

        return (EmfFileInfo[]) selected.toArray(new EmfFileInfo[0]);
    }

    public EmfFileInfo selectedDirectory() {
        return this.currentDir;
    }

}
