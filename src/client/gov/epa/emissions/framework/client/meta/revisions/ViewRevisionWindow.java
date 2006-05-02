package gov.epa.emissions.framework.client.meta.revisions;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.editor.Revision;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class ViewRevisionWindow extends DisposableInteralFrame implements RevisionView {

    private JPanel layout;

    public ViewRevisionWindow(DesktopManager desktopManager) {
        super("View Note", new Dimension(350, 275), desktopManager);

        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        super.getContentPane().add(layout);
    }

    public void display(Revision revision, EmfDataset dataset) {
        String name = "View Note: " + revision.getWhat();
        super.setTitle(name);
        super.setName(name);

        doLayout(revision, dataset);
        super.display();
    }

    private void doLayout(Revision revision, EmfDataset dataset) {
        layout.add(inputPanel(revision, dataset));
        layout.add(buttonsPanel());
    }

    private JPanel inputPanel(Revision revision, EmfDataset dataset) {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Summary", new Label(revision.getWhat()), panel);

        TextArea details = new TextArea("Why", revision.getWhy(), 40, 3);
        details.setEditable(false);
        ScrollableComponent scrollableDetails = ScrollableComponent.createWithVerticalScrollBar(details);
        layoutGenerator.addLabelWidgetPair("Why", scrollableDetails, panel);

        layoutGenerator.addLabelWidgetPair("Dataset", new Label(dataset.getName()), panel);
        layoutGenerator.addLabelWidgetPair("Version", new Label("" + revision.getVersion()), panel);
        layoutGenerator.addLabelWidgetPair("Creator", new Label(revision.getCreator().getName()), panel);
        layoutGenerator.addLabelWidgetPair("Date", new Label(format(revision.getDate())), panel);
        layoutGenerator.addLabelWidgetPair("References", new Label(references(revision)), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 7, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private String references(Revision revision) {
        return revision.getReferences() != null ? revision.getReferences() : "";
    }

    private String format(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        return date != null ? format.format(date) : "";
    }

    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();
        Button ok = new Button("Ok", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                disposeView();
            }
        });
        getRootPane().setDefaultButton(ok);
        panel.add(ok);

        return panel;
    }

}
