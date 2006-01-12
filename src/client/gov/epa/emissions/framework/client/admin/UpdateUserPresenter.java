package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.EmfException;

public interface UpdateUserPresenter {

    void display(UpdatableUserView view) throws EmfException;

    void doSave() throws EmfException;

    void doClose() throws EmfException;

    void onChange();

    void displayViewIfLocked(UpdatableUserView update, UserView view) throws EmfException;

}