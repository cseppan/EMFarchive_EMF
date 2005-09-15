package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.gui.TextFieldWidget;
import gov.epa.emissions.commons.gui.Widget;
import gov.epa.emissions.framework.EmfException;
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

    private User user;

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

        user = new User();
        Widget name = new TextFieldWidget("name", user.getFullName(), 15);
        panel = new UserProfilePanel(user, name, okAction, cancelAction, adminOption,
                new PopulateUserOnRegisterStrategy(user));
        this.add(panel);
    }

    private void registerUser() {
        try {
            panel.populateUser();
            presenter.notifyRegister(user);
        } catch (EmfException e) {
            panel.setError(e.getMessage());
            refresh();
            return;
        }

        postRegisterStrategy.execute(user);
        close();
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
