package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.client.EmfView;
import gov.epa.emissions.framework.services.User;

public interface UserManagerView extends EmfView {

    void observe(UserManagerPresenter presenter);

    void refresh();

    void showMessage(String message);

    void clearMessage();

    boolean promptDelete(User[] users);

    UpdateUserView getUpdateUserView(User user);
}
