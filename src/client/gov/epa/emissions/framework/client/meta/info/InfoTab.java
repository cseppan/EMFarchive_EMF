package gov.epa.emissions.framework.client.meta.info;

import gov.epa.emissions.commons.data.ExternalSource;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.emissions.framework.ui.TableData;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class InfoTab extends JPanel implements InfoTabView, RefreshObserver {

    private EmfConsole parentConsole;

    private JPanel sourcesPanel;

    private boolean forViewer;

    private ManageChangeables changeablesList;

    private InfoTabPresenter sourceTabPresenter;
    
    private MessagePanel msgPanel;

    public InfoTab(MessagePanel messagePanel, ManageChangeables changeablesList, EmfConsole parentConsole, boolean forViewer) {
        setName("infoTab");
        this.parentConsole = parentConsole;
        this.forViewer = forViewer;
        this.changeablesList = changeablesList;
        this.msgPanel = messagePanel;

        super.setLayout(new BorderLayout());

        add(createLayout(), BorderLayout.CENTER);
    }

    private JPanel createLayout() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        sourcesPanel = new JPanel(new BorderLayout());
        container.add(sourcesPanel);

        return container;
    }

    public void displayInternalSources(InternalSource[] sources) throws EmfException {
        displaySources("Data Tables", new InternalSourcesTableData(sources), false);
    }

    public void displayExternalSources(ExternalSource[] sources) throws EmfException {
        displaySources("External Files", new ExternalSourcesTableData(sources), true);
    }

    private void displaySources(String title, TableData tableData, boolean external) throws EmfException {
        sourcesPanel.removeAll();
        sourcesPanel.setBorder(new Border(title));
        sourcesPanel.add(createSortFilterPane(tableData, parentConsole, external));
        sourcesPanel.validate();
    }

    private JPanel createSortFilterPane(TableData tableData, EmfConsole parentConsole, boolean external) throws EmfException {
        JPanel tablePanel = new JPanel(new BorderLayout());

        SelectableSortFilterWrapper table = new SelectableSortFilterWrapper(parentConsole, tableData, null);
        tablePanel.add(table, BorderLayout.CENTER);

        EmfDataset dataset = sourceTabPresenter.getDataset();

        if (external && !forViewer) {
            ExternalSource[] extSrcs = sourceTabPresenter.getExternalSrcs(dataset.getId(), 20);

            if (extSrcs != null && extSrcs.length > 0)
                tablePanel.add(controlPanel(), BorderLayout.PAGE_END);
        }

        return tablePanel;
    }

    private JPanel controlPanel() {
        JPanel container = new JPanel();

        Button update = new Button("Update", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                updateSources();
            }
        });
        container.add(update);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(container, BorderLayout.WEST);

        return panel;
    }

    private void updateSources() {
        EmfDataset dataset = sourceTabPresenter.getDataset();
        EmfSession session = sourceTabPresenter.getSession();
        String title = "Update Dataset External Source for Dataset: " + dataset.getName();
        ExternalSourceUpdateWindow view = new ExternalSourceUpdateWindow(title, parentConsole, changeablesList, session);
        ExternalSourceUpdatePresenter updatePresenter = new ExternalSourceUpdatePresenter(sourceTabPresenter);
        updatePresenter.display(view);
    }

    public void observe(InfoTabPresenter presenter) {
        sourceTabPresenter = presenter;
    }

    public void doRefresh() throws EmfException {
        try {
            kickPopulateThread();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    private void kickPopulateThread() {
        Thread populateThread = new Thread(new Runnable() {
            public void run() {
                try {
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    msgPanel.setMessage("Please wait while loading dataset sources...");
                    sourceTabPresenter.doDisplay();
                    msgPanel.setMessage("Finished loading dataset sources.");
                } catch (Exception e) {
                    msgPanel.setError("Cannot retrieve dataset sources.");
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        });
        
        populateThread.start();
    }

}
