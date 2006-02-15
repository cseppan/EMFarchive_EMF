package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.Revision;
import gov.epa.emissions.framework.ui.Dialog;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class RevisionDialog extends Dialog {

    private TextField what;

    protected boolean shouldCreate;

    private TextArea why;

    private TextField references;

    private User user;

    private EmfDataset dataset;

    private Version version;

    public RevisionDialog(EmfConsole parent) {
        super("Add Revision", parent);
        super.setSize(new Dimension(550, 250));

        super.center();
    }

    public void display(User user, EmfDataset dataset, Version version) {
        this.user = user;
        this.dataset = dataset;
        this.version = version;

        super.getContentPane().add(createLayout());
        super.show();
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

        what = new TextField("", 40);
        layoutGenerator.addLabelWidgetPair("What", what, panel);

        why = new TextArea("", "", 40, 3);
        layoutGenerator.addLabelWidgetPair("Why", why, panel);

        references = new TextField("", 40);
        layoutGenerator.addLabelWidgetPair("References", references, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();
        Button ok = new Button("Ok", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (verifyInput()) {
                    shouldCreate = true;
                    close();
                }
            }
        });
        getRootPane().setDefaultButton(ok);
        panel.add(ok);

        Button cancel = new Button("Cancel", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                shouldCreate = false;
                close();
            }
        });
        panel.add(cancel);

        return panel;
    }

    protected boolean verifyInput() {
        if (what.getText().trim().length() != 0)
            return true;

        JOptionPane.showMessageDialog(super.getParent(), "Please enter Name", "Error", JOptionPane.ERROR_MESSAGE);
        return false;
    }

    protected void close() {
        super.dispose();
    }

    public boolean shouldCreate() {
        return shouldCreate;
    }

    public Revision revision() {
        Revision revision = new Revision();
        revision.setCreator(user);
        revision.setDatasetId(dataset.getId());
        revision.setVersion(version.getVersion());
        revision.setWhat(what.getText());
        revision.setWhy(why.getText());
        revision.setReferences(references.getText());
        revision.setDate(new Date());

        return revision;
    }

}
