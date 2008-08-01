package gov.epa.emissions.framework.client.meta.info;

import gov.epa.emissions.commons.data.ExternalSource;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.emissions.framework.ui.TableData;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class InfoTab extends JPanel implements InfoTabView {

    private EmfConsole parentConsole;

    private JPanel sourcesPanel;
    
    private boolean forViewer;

    private InfoTabPresenter sourceTabPresenter;
    
    private DesktopManager desktopManager;

    public InfoTab(EmfConsole parentConsole, DesktopManager desktopManager, boolean forViewer) {
        setName("infoTab");
        this.parentConsole = parentConsole;
        this.forViewer = forViewer;
        this.desktopManager = desktopManager;

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

    public void displayInternalSources(InternalSource[] sources) {
        displaySources("Data Tables", new InternalSourcesTableData(sources), false);
    }

    public void displayExternalSources(ExternalSource[] sources) {
        displaySources("External Files", new ExternalSourcesTableData(sources), true);
    }

    private void displaySources(String title, TableData tableData, boolean external) {
        sourcesPanel.removeAll();
        sourcesPanel.setBorder(new Border(title));
        sourcesPanel.add(createSortFilterPane(tableData, parentConsole, external));
    }

    private JPanel createSortFilterPane(TableData tableData, EmfConsole parentConsole, boolean external) {
        JPanel tablePanel = new JPanel(new BorderLayout());

        SelectableSortFilterWrapper table = new SelectableSortFilterWrapper(parentConsole, tableData, null);
        tablePanel.add(table, BorderLayout.CENTER);

        if (external && !forViewer)
            tablePanel.add(controlPanel(), BorderLayout.PAGE_END);

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
        ExternalSourceUpdateView view = new ExternalSourceUpdateWindow(title, desktopManager, parentConsole, session);
        ExternalSourceUpdatePresenter updatePresenter = new ExternalSourceUpdatePresenter(dataset, session);
        updatePresenter.display(view);
    }

    public void observe(InfoTabPresenter presenter) {
        sourceTabPresenter = presenter;
    }

}
