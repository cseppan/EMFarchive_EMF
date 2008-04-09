package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.data.DataSortFilterPanel;
import gov.epa.emissions.framework.client.data.PaginationPanel;
import gov.epa.emissions.framework.client.data.viewer.TablePresenter;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class EditorPanel extends JPanel implements EditorPanelView {

    private JPanel pageContainer;

    private PaginationPanel paginationPanel;

    private EmfDataset dataset;

    private Version version;

    private EditablePage editablePage;

    private MessagePanel messagePanel;

    private EditablePagePanel editablePagePanel;

    private DataSortFilterPanel sortFilterPanel;
    
    private ManageChangeables changeablesList;

    private TableMetadata tableMetadata;

    private DesktopManager desktopManager;

    public EditorPanel(EmfDataset dataset, Version version, TableMetadata tableMetadata, 
            MessagePanel messagePanel, ManageChangeables changeablesList) {
        super(new BorderLayout());
        super.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        this.dataset = dataset;
        this.version = version;
        this.tableMetadata = tableMetadata;
        this.messagePanel = messagePanel;
        this.changeablesList = changeablesList;

        doLayout(messagePanel);
    }

    private void doLayout(MessagePanel messagePanel) {
        add(topPanel(messagePanel), BorderLayout.PAGE_START);

        pageContainer = new JPanel(new BorderLayout());
        add(pageContainer, BorderLayout.CENTER);
    }

    private JPanel topPanel(MessagePanel messagePanel) {
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(sortFilterPanel(messagePanel), BorderLayout.CENTER);

        paginationPanel = new PaginationPanel(messagePanel);
        panel.add(paginationPanel, BorderLayout.EAST);

        return panel;
    }

    private DataSortFilterPanel sortFilterPanel(MessagePanel messagePanel) {
        sortFilterPanel = new DataSortFilterPanel(messagePanel, dataset, "");
        return sortFilterPanel;
    }

    public void observe(TablePresenter presenter) {
        paginationPanel.init(presenter);
        sortFilterPanel.init(presenter);
    }

    public void display(Page page) {
        pageContainer.removeAll();
        paginationPanel.updateStatus(page);
        pageContainer.add(createEditablePage(page), BorderLayout.CENTER);
    }

    private EditablePagePanel createEditablePage(Page page) {
        editablePage = new EditablePage(dataset.getId(), version, page, tableMetadata);
        editablePagePanel = new EditablePagePanel(editablePage, paginationPanel, messagePanel, changeablesList);
        editablePagePanel.setDesktopManager(desktopManager);

        return editablePagePanel;
    }


    public ChangeSet changeset() {
        // if not initialized, no changes
        return editablePage != null ? editablePage.changeset() : new ChangeSet();
    }

    public void updateFilteredRecordsCount(int filtered) {
        paginationPanel.updateFilteredRecordsCount(filtered);
    }

    public void scrollToPageEnd() {
        editablePagePanel.scrollToPageEnd();
    }
    
    public TableMetadata tableMetadata() {
        return tableMetadata;
    }

    public String getRowFilter() {
        // NOTE Auto-generated method stub
        return null;
    }
    
    public DesktopManager getDesktopManager() {
        return this.desktopManager;
    }
    
    public void setDesktopManager(DesktopManager desktopManager) {
        this.desktopManager = desktopManager;
    }

}
