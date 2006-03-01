package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

public class ImportWindow extends ReusableInteralFrame implements ImportView {

    private ImportPresenter presenter;

    private MessagePanel messagePanel;

    private DataCommonsService service;
    
    private ImportInputPanel importInputPanel;

    public ImportWindow(DataCommonsService service, DesktopManager desktopManager) throws EmfException {
        super("Import Dataset", new Dimension(650, 400), desktopManager);
        super.setName("importDatasets");

        this.service = service;

        this.getContentPane().add(createLayout());
    }

    private JPanel createLayout() throws EmfException {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        importInputPanel = new ImportInputPanel(service, messagePanel, this);
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

        JButton importButton = new Button("Import", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doImport();
            }
        });
        container.add(importButton);
        getRootPane().setDefaultButton(importButton);

        JButton done = new Button("Done", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                presenter.doDone();
            }
        });
        container.add(done);

        panel.add(container, BorderLayout.EAST);

        return panel;
    }
    
    private void doImport() {
        importInputPanel.doImport(presenter);
    }

    public void register(ImportPresenter presenter) {
        this.presenter = presenter;
    }

    public void clearMessagePanel() {
        messagePanel.clear();
        super.refreshLayout();
    }

    public void setDefaultBaseFolder(String folder) {
        importInputPanel.setDefaultBaseFolder(folder);
    }
}
