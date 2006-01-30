package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.services.EmfDataset;

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

    public EditablePageContainer(EmfDataset dataset, Version version, InternalSource source, MessagePanel messagePanel) {
        super(new BorderLayout());
        super.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        this.dataset = dataset;
        this.version = version;
        this.source = source;
        this.messagePanel = messagePanel;

        doLayout(messagePanel);
    }

    private void doLayout(MessagePanel messagePanel) {
        paginationPanel = new PaginationPanel(messagePanel);
        add(paginationPanel, BorderLayout.PAGE_START);

        pageContainer = new JPanel(new BorderLayout());
        add(pageContainer, BorderLayout.CENTER);
    }

    public void observe(EditableTablePresenter presenter) {
        paginationPanel.init(presenter);
    }

    public void display(Page page) {
        // TODO: refresh table w/ new data? or remove and add back the table?
        pageContainer.removeAll();

        paginationPanel.updateStatus(page);

        // FIXME: why is Dataset Id 'int', and not long?
        pageContainer.add(createEditablePage(page), BorderLayout.CENTER);
    }

    private EditablePagePanel createEditablePage(Page page) {
        editablePage = new EditablePage((int) dataset.getDatasetid(), version, page, cols());
        editablePagePanel = new EditablePagePanel(editablePage, messagePanel);
        
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
