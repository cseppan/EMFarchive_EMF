package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.client.ManagedView;

public interface UpdateUserView extends ManagedView {

    void observe(UpdateUserPresenter presenter);

    /**
     * Close the window, if user confirms that he/she would'nt care of losing
     * any user edits (i.e. data changes)
     */
    void closeOnConfirmLosingChanges();

}
