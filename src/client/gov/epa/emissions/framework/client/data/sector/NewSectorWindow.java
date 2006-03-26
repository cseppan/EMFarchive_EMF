package gov.epa.emissions.framework.client.data.sector;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class NewSectorWindow extends DisposableInteralFrame implements NewSectorView {

    private NewSectorPresenter presenter;

    private JPanel layout;

    private SingleLineMessagePanel messagePanel;

    private TextField name;

    private TextArea description;

    private SectorCriteriaTableData criteriaTableData;
    
    private SectorCriteriaPanel criteriaPanel;

    private static int counter;

    public NewSectorWindow(DesktopManager desktopManager) {
        super("Create New Sector", new Dimension(550, 400), desktopManager);

        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        super.getContentPane().add(layout);
    }

    public void observe(NewSectorPresenter presenter) {
        this.presenter = presenter;
    }

    public void display(Sector sector) {
        counter++;
        String name = "Create New Sector " + counter;
        super.setTitle(name);
        super.setName("createNewSector:" + counter);
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
        addChangeable(name);
        layoutGenerator.addLabelWidgetPair("Name:", name, panel);

        description = new TextArea("description", sector.getDescription(), 40);
        addChangeable(description);
        ScrollableComponent descTextArea = new ScrollableComponent(description);
        descTextArea.setMinimumSize(new Dimension(80, 80));
        // .descTextAredescTextArea.setHorizontalScroll
        layoutGenerator.addLabelWidgetPair("Description:", descTextArea, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 2, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JPanel createCriteriaPanel(Sector sector) {
        criteriaTableData = new SectorCriteriaTableData(sector.getSectorCriteria());
        criteriaPanel = new SectorCriteriaPanel("", criteriaTableData, this);
        criteriaPanel.setBorder(BorderFactory.createTitledBorder("Criteria"));

        return criteriaPanel;
    }

    private JPanel createButtonsPanel(Sector sector) {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(10);
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
                sector.setSectorCriteria(criteriaTableData.sources());
                try {
                    if (!name.getText().equals("")) {
                        criteriaPanel.commit();
                        resetChanges();
                        presenter.doSave();
                    } else
                        messagePanel.setError("Name field should be a non-empty string.");
                } catch (EmfException e) {
                    messagePanel.setError("Could not save." + e.getMessage());
                }
            }
        };

        return action;
    }

    public void windowClosing() {
        checkChangesAndCloseWindow();
    }

    private Action closeAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                checkChangesAndCloseWindow();
            }
        };

        return action;
    }

    private void checkChangesAndCloseWindow() {
        if (checkChanges())
            presenter.doClose();
    }

}
