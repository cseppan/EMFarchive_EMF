package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataAccessToken;
import gov.epa.emissions.framework.services.DataEditorService;

public class EditableTablePresenterImpl implements EditableTablePresenter {

    private EditablePageManagerView view;

    private TablePaginator paginator;

    private DataEditorService service;

    public EditableTablePresenterImpl(Version version, String table, EditablePageManagerView view,
            DataEditorService service) {
        this.service = service;
        this.view = view;

        paginator = new TablePaginator(version, table, view, service);
    }

    public void observe() {
        view.observe(this);
    }

    public void doDisplayNext() throws EmfException {
        submitChanges();
        paginator.doDisplayNext();
    }

    public void doDisplayPrevious() throws EmfException {
        submitChanges();
        paginator.doDisplayPrevious();
    }

    public void doDisplay(int pageNumber) throws EmfException {
        submitChanges();
        paginator.doDisplay(pageNumber);
    }

    public void doDisplayFirst() throws EmfException {
        submitChanges();
        paginator.doDisplayFirst();
    }

    public void doDisplayLast() throws EmfException {
        submitChanges();
        paginator.doDisplayLast();
    }

    public void doDisplayPageWithRecord(int record) throws EmfException {
        if (paginator.isCurrent(record))
            return;

        submitChanges();
        paginator.doDisplayPageWithRecord(record);
    }

    public int totalRecords() throws EmfException {
        return paginator.totalRecords();
    }

    public void submitChanges() throws EmfException {
        ChangeSet changeset = view.changeset();
        if (changeset.hasChanges()) {
            service.submit(token(), changeset, paginator.pageNumber());
            changeset.clear();
        }

        view.updateTotalRecordsCount(paginator.totalRecords());
    }

    private DataAccessToken token() {
        return paginator.token();
    }

    public boolean hasChanges() {
        return view.changeset().hasChanges();
    }

    public void applyConstraints(String rowFilter, String sortOrder) throws EmfException {
        Page page = service.applyConstraints(token(), rowFilter, sortOrder);
        view.display(page);
    }

}
