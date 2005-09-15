package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.UserException;
import gov.epa.emissions.framework.client.EmfWidgetContainer;
import gov.epa.emissions.framework.services.User;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;

public class RegisterUserPanel extends JPanel implements RegisterUserView {

    private RegisterUserPresenter presenter;

    private PostRegisterStrategy postRegisterStrategy;

    private EmfWidgetContainer parent;

    protected JPanel profileValuesPanel;

    private RegisterCancelStrategy cancelStrategy;

    private UserProfilePanel panel;

    public RegisterUserPanel(PostRegisterStrategy postRegisterStrategy, RegisterCancelStrategy cancelStrategy,
            EmfWidgetContainer parent) {
        this(postRegisterStrategy, cancelStrategy, parent, new NoAdminOption());
    }

    public RegisterUserPanel(PostRegisterStrategy postRegisterStrategy, RegisterCancelStrategy cancelStrategy,
            EmfWidgetContainer parent, AdminOption adminOption) {
        this.postRegisterStrategy = postRegisterStrategy;
        this.cancelStrategy = cancelStrategy;
        this.parent = parent;

        createLayout(adminOption);

        this.setSize(new Dimension(375, 425));
    }

    private void createLayout(AdminOption adminOption) {
        Action okAction = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                registerUser();
            }
        };
        Action cancelAction = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                cancelStrategy.execute(presenter);
            }
        };

        panel = new UserProfilePanel(okAction, cancelAction, adminOption);
        this.add(panel);
    }

    private void registerUser() {
        if (presenter == null)
            return;

        try {
            User user = new User();
            populateUser(user);

            presenter.notifyRegister(user);
            postRegisterStrategy.execute(user);
            close();
        } catch (EmfException e) {
            panel.setError(e.getMessage());
            refresh();
        }
    }

    protected void populateUser(User user) throws UserException {
        panel.populateUser(user);
    }

    public void refresh() {
        this.validate();
    }

    public void close() {
        parent.close();
    }

    public void observe(RegisterUserPresenter presenter) {
        this.presenter = presenter;
    }

    public RegisterUserPresenter getPresenter() {
        return presenter;
    }

}
