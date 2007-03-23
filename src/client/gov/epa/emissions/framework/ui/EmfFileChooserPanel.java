package gov.epa.emissions.framework.ui;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

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

public class EmfFileChooserPanel extends JPanel {

    private EmfFileSystemView fsv;

    private EmfFileInfo currentDir;

    private EmfFileInfo[] selectedFile;

    private JTextField folder;

    private JList filesList;

    private JList subdirsList;

    private boolean dirOnly;
    
    private SingleLineMessagePanel messagePanel;

    public EmfFileChooserPanel(EmfFileSystemView fsv, EmfFileInfo initialFile, boolean dirOnly) {
        this.fsv = fsv;
        this.currentDir = initialFile;
        this.dirOnly = dirOnly;
        this.messagePanel = new SingleLineMessagePanel();
        display();
    }

    public void display() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(this.messagePanel);
        add(upperPanel());
        add(fileListPanels());
        setPreferredSize(new Dimension(418, 300));
        setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
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

        folder = new JTextField(currentDir.getAbsolutePath(), 32);
        folder.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent evt) {
                String newvalue = ((JTextField)evt.getSource()).getText();
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
        panel.add(listWedgit(dirs, true));

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
            subdirsList.addMouseListener(new MouseListener(){

                public void mouseClicked(MouseEvent e) {
                    if ( e.getClickCount() == 2) {
                        Object source = e.getSource();
                        EmfFileInfo fileInfo = (EmfFileInfo) ((JList) source).getSelectedValue();
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
        subdirsList.setListData(getAllDirs());
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
