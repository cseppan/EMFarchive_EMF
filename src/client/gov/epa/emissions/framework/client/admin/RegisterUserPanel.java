package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.TextFieldWidget;
import gov.epa.emissions.commons.gui.Widget;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfView;
import gov.epa.emissions.framework.services.EmfException;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;

public class RegisterUserPanel extends JPanel {

    private RegisterUserPresenter presenter;

    private PostRegisterStrategy postRegisterStrategy;

    private EmfView container;

    protected JPanel profileValuesPanel;

    private RegisterCancelStrategy cancelStrategy;

    private EditableUserProfilePanel panel;

    private User user;

    private ManageChangeables changeablesList;

    public RegisterUserPanel(PostRegisterStrategy postRegisterStrategy, RegisterCancelStrategy cancelStrategy,
            EmfView parent, ManageChangeables changeablesList) {
        this(postRegisterStrategy, cancelStrategy, parent, new NoAdminOption(), changeablesList);
    }

    public RegisterUserPanel(PostRegisterStrategy postRegisterStrategy, RegisterCancelStrategy cancelStrategy,
            EmfView parent, AdminOption adminOption, ManageChangeables changeablesList) {
        this.postRegisterStrategy = postRegisterStrategy;
        this.cancelStrategy = cancelStrategy;
        this.container = parent;
        this.changeablesList = changeablesList;
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
                doCancel();
            }
        };

        user = new User();
        Widget username = new TextFieldWidget("username", user.getUsername(), 10);
        panel = new EditableUserProfilePanel(user, username, okAction, cancelAction, adminOption,
                new PopulateUserOnRegisterStrategy(user), changeablesList);
        this.add(panel);
    }

    private void registerUser() {
        try {
            panel.populateUser();

            // FIXME: monitor.resetChanges();
            presenter.doRegister(user);
            postRegisterStrategy.execute(user);
        } catch (EmfException e) {
            panel.setError(e.getMessage());
            refresh();
            return;
        }

        container.close();
    }

    public void refresh() {
        this.validate();
    }

    public void observe(RegisterUserPresenter presenter) {
        this.presenter = presenter;
    }

    public RegisterUserPresenter getPresenter() {
        return presenter;
    }

<<<<<<< RegisterUserPanel.java
    public boolean confirmDiscardChanges() {
        if (changeablesList instanceof EmfInternalFrame)
            return ((EmfInternalFrame) changeablesList).shouldDiscardChanges();

        return true;
    }

    public void closeWindow() {
        if (confirmDiscardChanges())
            cancelStrategy.execute(presenter);
=======
    public void doCancel() {
        cancelStrategy.execute(presenter);
>>>>>>> 1.19
    }

}
