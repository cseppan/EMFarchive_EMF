package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataAccessToken;
import gov.epa.emissions.framework.services.DataEditorService;

public class ClosingRule {

    private EditableTablePresenter tablePresenter;

    private DataEditorService service;

    private DataAccessToken token;

    private DataEditorView view;

    public ClosingRule() {// To support unit testing
    }

    public ClosingRule(DataEditorView view, EditableTablePresenter tablePresenter, DataEditorService service,
            DataAccessToken token) {
        this.view = view;
        this.tablePresenter = tablePresenter;
        this.service = service;
        this.token = token;
    }

    public boolean hasChanges() throws EmfException {
        return tablePresenter.hasChanges() || service.hasChanges(token);
    }

    public void close() throws EmfException {
        if (shouldCancelClose())
            return;

        proceedWithClose();
    }

    public boolean shouldCancelClose() throws EmfException {
        return hasChanges() && !view.confirmDiscardChanges();
    }

    public void proceedWithClose() throws EmfException {
        service.closeSession(token);
        view.close();
    }
}