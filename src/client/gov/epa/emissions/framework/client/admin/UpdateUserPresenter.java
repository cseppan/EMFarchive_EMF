package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.EmfException;

public interface UpdateUserPresenter {

    void doSave() throws EmfException;

    void doClose() throws EmfException;

    void onChange();

    void display(UpdatableUserView update, UserView view) throws EmfException;

}