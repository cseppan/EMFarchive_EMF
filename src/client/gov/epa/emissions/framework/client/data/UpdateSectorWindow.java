package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ScrollableTextArea;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class UpdateSectorWindow extends DisposableInteralFrame implements UpdateSectorView {

    private UpdateSectorPresenter presenter;

    private JPanel layout;

    private SingleLineMessagePanel messagePanel;

    private TextField name;

    private TextArea description;

    private SectorCriteriaTableData criteriaTableData;

    public UpdateSectorWindow() {
        super("Update Sector");

        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        super.getContentPane().add(layout);

        super.setSize(new Dimension(400, 300));
    }

    public void observe(UpdateSectorPresenter presenter) {
        this.presenter = presenter;
    }

    public void display(Sector sector) {
        layout.removeAll();
        doLayout(layout, sector);

        super.display();
    }

    // FIXME: CRUD panel. Refactor to use in DatasetTypes Manager
    private void doLayout(JPanel layout, Sector sector) {
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel);
        layout.add(createInputPanel(sector));
        layout.add(createCriteriaPanel(sector));
        layout.add(createButtonsPanel(sector));
    }

    private JPanel createInputPanel(Sector sector) {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        name = new TextField("name", sector.getName(), 20);
        layoutGenerator.addLabelWidgetPair("Name", name, panel);

        description = new TextArea("description", sector.getDescription(), 25);
        layoutGenerator.addLabelWidgetPair("Description", new ScrollableTextArea(description), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 2, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JPanel createCriteriaPanel(Sector sector) {
        criteriaTableData = new SectorCriteriaTableData(sector.getSectorCriteria());
        SectorCriteriaPanel panel = new SectorCriteriaPanel(criteriaTableData);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.GRAY));

        return panel;
    }

    private JPanel createButtonsPanel(Sector sector) {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(25);
        container.setLayout(layout);

        Button saveButton = new Button("Save", saveAction(sector));
        container.add(saveButton);
        container.add(new Button("Close", closeAction()));
        getRootPane().setDefaultButton(saveButton);

        panel.add(container, BorderLayout.CENTER);

        return panel;
    }

    private Action saveAction(final Sector sector) {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                sector.setName(name.getText());
                sector.setDescription(description.getText());

                try {
                    presenter.doSave();
                } catch (EmfException e) {
                    messagePanel.setError("Could not save. Reason: " + e.getMessage());
                }
            }
        };

        return action;
    }

    private Action closeAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                presenter.doClose();
            }
        };

        return action;
    }

}
