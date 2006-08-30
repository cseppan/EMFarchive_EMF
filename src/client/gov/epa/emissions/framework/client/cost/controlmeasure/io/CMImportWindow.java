package gov.epa.emissions.framework.client.cost.controlmeasure.io;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.ImportButton;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshButton;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class CMImportWindow extends ReusableInteralFrame implements CMImportView, RefreshObserver {

    private CMImportPresenter presenter;

    private MessagePanel messagePanel;

    private CMImportInputPanel importInputPanel;

    public CMImportWindow(DesktopManager desktopManager) {
        super("Import Control Measures", new Dimension(650, 400), desktopManager);
        super.setName("importControlMeasures");

        this.getContentPane().add(createLayout());
    }

    private JPanel createLayout() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        importInputPanel = new CMImportInputPanel(messagePanel);

        panel.add(messagePanel);
        panel.add(importInputPanel);
        panel.add(createButtonsPanel());

        return panel;
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(25);
        container.setLayout(layout);

        Button importButton = new ImportButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doImport();
            }
        });
        container.add(importButton);
        getRootPane().setDefaultButton(importButton);

        Button importStatusButton = new RefreshButton("Import Status", 16, true, this,
                "Refresh Control Measure Import Status", messagePanel);

        container.add(importStatusButton);

        Button done = new Button("Done", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                presenter.doDone();
            }
        });
        container.add(done);
        panel.add(container, BorderLayout.EAST);

        return panel;
    }

    private void doImport() {
        try {
            presenter.doImport(importInputPanel.folder(), importInputPanel.files());
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    public void register(CMImportPresenter presenter) {
        this.presenter = presenter;
        importInputPanel.register(presenter);
    }

    public void setDefaultBaseFolder(String folder) {
        importInputPanel.setDefaultBaseFolder(folder);
    }

    public void setMessage(String message) {
        importInputPanel.setStartImportMessage(message);

    }

    public void doRefresh() throws EmfException {
        doImportStatus();
    }

    protected void doImportStatus() throws EmfException {
        Status[] importStatus = presenter.getImportStatus();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < importStatus.length; i++) {
            sb.append(importStatus[i].getMessage());
        }
        importInputPanel.addStatusMessage(sb.toString());
    }
}
