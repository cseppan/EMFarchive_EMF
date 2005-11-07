package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.framework.ui.FileChooser;
import gov.epa.emissions.framework.ui.ImageResources;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class FileChooserLauncher extends JPanel implements ActionListener {
    JButton openButton;

    FileChooser chooser;

    public FileChooserLauncher() {
        super(new BorderLayout());

        chooser = new FileChooser("Open", this);

        openButton = new JButton("Open a File...", openIcon());
        openButton.addActionListener(this);

        JPanel buttonPanel = new JPanel(); // use FlowLayout
        buttonPanel.add(openButton);

        add(buttonPanel, BorderLayout.PAGE_START);
    }

    private Icon openIcon() {
        return new ImageResources().open("Open a File");
    }

    public void actionPerformed(ActionEvent e) {
        File file = chooser.choose();
        System.out.println("file to be opened: " + file.getAbsolutePath());
    }

    private static void createAndShowGUI() {
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);

        // Create and set up the window.
        JFrame frame = new JFrame("FileChooserDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create and set up the content pane.
        JComponent newContentPane = new FileChooserLauncher();
        frame.setContentPane(newContentPane);

        // Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
