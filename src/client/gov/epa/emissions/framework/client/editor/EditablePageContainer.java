package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class EditablePageContainer extends JPanel implements EditablePageManagerView {

    private InternalSource source;

    private JPanel pageContainer;

    private PaginationPanel paginationPanel;

    private EmfDataset dataset;

    private Version version;

    private EditablePage editablePage;

    private MessagePanel messagePanel;

    private EditablePagePanel editablePagePanel;

    private DataSortFilterPanel sortFilterPanel;
    
    private ManageChangeables changeablesList;

    public EditablePageContainer(EmfDataset dataset, Version version, InternalSource source, 
            MessagePanel messagePanel, ManageChangeables changeablesList) {
        super(new BorderLayout());
        super.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        this.dataset = dataset;
        this.version = version;
        this.source = source;
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

        panel.add(sortFilterPanel(messagePanel), BorderLayout.LINE_START);

        paginationPanel = new PaginationPanel(messagePanel);
        panel.add(paginationPanel, BorderLayout.LINE_END);

        return panel;
    }

    private DataSortFilterPanel sortFilterPanel(MessagePanel messagePanel) {
        sortFilterPanel = new DataSortFilterPanel(messagePanel, changeablesList);
        return sortFilterPanel;
    }

    public void observe(TablePresenter presenter) {
        paginationPanel.init(presenter);
        sortFilterPanel.init(presenter);
    }

    public void display(Page page) {
        // TODO: refresh table w/ new data? or remove and add back the table?
        pageContainer.removeAll();

        paginationPanel.updateStatus(page);

        // FIXME: why is Dataset Id 'int', and not long?
        pageContainer.add(createEditablePage(page), BorderLayout.CENTER);
    }

    private EditablePagePanel createEditablePage(Page page) {
        editablePage = new EditablePage((int) dataset.getId(), version, page, cols());
        editablePagePanel = new EditablePagePanel(editablePage, messagePanel, changeablesList);

        return editablePagePanel;
    }

    // Filter out the first four (version-specific cols)
    private String[] cols() {
        List cols = new ArrayList();

        String[] allCols = source.getCols();
        for (int i = 4; i < allCols.length; i++)
            cols.add(allCols[i]);

        return (String[]) cols.toArray(new String[0]);
    }

    public ChangeSet changeset() {
        // if not initialized, no changes
        return editablePage != null ? editablePage.changeset() : new ChangeSet();
    }

    public void updateTotalRecordsCount(int total) {
        paginationPanel.updateTotalRecordsCount(total);
    }

    public void scrollToPageEnd() {
        editablePagePanel.scrollToPageEnd();
    }

}
