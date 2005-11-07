package gov.epa.emissions.framework.ui;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;

public class FileChooser {

    private JFileChooser chooser;

    private String action;

    private Component parent;

    public FileChooser(String action, Component parent) {
        this(action, null, parent);
    }

    public FileChooser(String action, File folder, Component parent) {
        this.action = action;
        this.parent = parent;

        chooser = new JFileChooser(folder);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    }

    public File choose() {
        int result = chooser.showDialog(parent, action);
        return (result == JFileChooser.APPROVE_OPTION) ? chooser.getSelectedFile() : null;
    }
}
