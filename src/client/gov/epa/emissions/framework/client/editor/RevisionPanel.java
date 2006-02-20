package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ScrollableTextArea;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.Revision;
import gov.epa.emissions.framework.ui.Border;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class RevisionPanel extends JPanel {

    private TextArea what;

    private TextArea why;

    private User user;

    private EmfDataset dataset;

    private Version version;

    public RevisionPanel(User user, EmfDataset dataset, Version version) {
        this.user = user;
        this.dataset = dataset;
        this.version = version;

        super.add(createLayout());
        super.setBorder(new Border("Revision"));
    }

    private JPanel createLayout() {
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(mainPanel(), BorderLayout.CENTER);
        panel.add(referencesPanel(), BorderLayout.LINE_END);

        return panel;
    }

    private JPanel referencesPanel() {
        JPanel panel = new JPanel();

        Button references = new Button("References", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                // NOTE lookup references
            }
        });

        panel.add(references);

        return panel;
    }

    private JPanel mainPanel() {
        JPanel panel = new JPanel();

        what = new TextArea("", "", 30, 2);
        panel.add(labelValuePanel("What", ScrollableTextArea.createWithVerticalScrollBar(what)));

        why = new TextArea("", "", 30, 2);
        panel.add(labelValuePanel("Why", ScrollableTextArea.createWithVerticalScrollBar(why)));

        return panel;
    }

    private JPanel labelValuePanel(String label, JComponent widget) {
        JPanel panel = new JPanel();
        panel.add(new JLabel(label));
        panel.add(widget);

        return panel;
    }

    public Revision revision() {
        Revision revision = new Revision();
        revision.setCreator(user);
        revision.setDatasetId(dataset.getId());
        revision.setVersion(version.getVersion());
        revision.setWhat(what.getText());
        revision.setWhy(why.getText());
        // revision.setReferences(references.get());
        revision.setDate(new Date());

        return revision;
    }

    public boolean verifyInput() {
        if (what.isEmpty() || why.isEmpty())
            return false;

        return true;
    }
}
