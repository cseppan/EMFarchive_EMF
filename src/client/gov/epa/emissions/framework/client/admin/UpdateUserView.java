package gov.epa.emissions.framework.client.admin;

public interface UpdateUserView {

    void setObserver(UpdateUserPresenter presenter);

    /**
     * Close the window, if user confirms that he/she would'nt care of losing
     * any user edits (i.e. data changes)
     */
    void closeOnConfirmLosingChanges();

    void close();
}
