package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.Dialog;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class EditVersionDialog extends Dialog {

    private TextField name;
    
    protected boolean shouldChange=false;

    private Version version;

    private VersionsSet versionsSet;
    
    public EditVersionDialog(EmfDataset dataset, Version selectedVersion, Version[] versions, EmfConsole parent) {
        super("Edit Version " + selectedVersion.getVersion() + " of " + dataset.getName(), parent);
       // this.dataset = dataset;
        super.setSize(new Dimension(400, 130));
        this.version=selectedVersion;
        versionsSet = new VersionsSet(versions);
        super.getContentPane().add(createLayout());
        super.center();
    }

    private JPanel createLayout() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(inputPanel());
        panel.add(buttonsPanel());

        return panel;
    }

    private JPanel inputPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        name = new TextField("", 25);
        name.setText(version.getName());
        layoutGenerator.addLabelWidgetPair("Name", name, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
                5, 15, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }


    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();
        Button ok = new OKButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (verifyInput()) {
                    shouldChange = true;
                    version.setName(name.getText().trim());
                    close();
                }
            }
        });
        getRootPane().setDefaultButton(ok);
        panel.add(ok);

        Button cancel = new CancelButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                shouldChange = false;
                close();
            }
        });
        panel.add(cancel);

        return panel;
    }

    protected boolean verifyInput() {
        String newName = name().trim();
        if (newName.length() == 0) {
            JOptionPane.showMessageDialog(super.getParent(), 
                    "Please enter a name", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (newName.contains("(") || newName.contains(")"))
        {
            JOptionPane.showMessageDialog(super.getParent(), 
                   "Please enter a name that does not contain parentheses", 
                  "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (isDuplicate(newName)) {
            JOptionPane.showMessageDialog(super.getParent(), 
                    "Please enter a unique 'name'", "Error",JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    private boolean isDuplicate(String value) {
        return versionsSet.contains(value);
    }

    protected void close() {
        super.dispose();
    }
    
    public boolean shouldChange() {
        return shouldChange;
    }

    public void run() {
        super.display();
    }

    public Version getVersion(){
        return version;
    }
    
    public String name() {
        return name.getText();
    }
    
}
