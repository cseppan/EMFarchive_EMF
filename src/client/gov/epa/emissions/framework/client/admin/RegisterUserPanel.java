package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.gui.LabelWidget;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.TextFieldWidget;
import gov.epa.emissions.commons.gui.Widget;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfInternalFrame;
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
    
    private Boolean isNewUser; 

    private ManageChangeables changeablesList;
    
    public RegisterUserPanel(PostRegisterStrategy postRegisterStrategy, RegisterCancelStrategy cancelStrategy,
            EmfView parent, ManageChangeables changeablesList, User user) {
        //this.user = user; 
        this(postRegisterStrategy, cancelStrategy, parent, new NoAdminOption(), 
                changeablesList, user);
    }

    public RegisterUserPanel(PostRegisterStrategy postRegisterStrategy, RegisterCancelStrategy cancelStrategy,
            EmfView parent, AdminOption adminOption, ManageChangeables changeablesList, User user) {
        this.postRegisterStrategy = postRegisterStrategy;
        this.cancelStrategy = cancelStrategy;
        this.container = parent;
        this.changeablesList = changeablesList;
        this.user = user; 
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
                closeWindow();
            }
        };
        Widget username;
        if ( user == null ) {
            user = new User();
            isNewUser = true; 
            username = new TextFieldWidget("username", user.getUsername(), 10);
        }
        else {
            isNewUser = false; 
            username = new LabelWidget("username", user.getUsername());
        }
        panel = new EditableUserProfilePanel(user, username, okAction, cancelAction, adminOption,
                new PopulateUserOnRegisterStrategy(user), changeablesList);
        this.add(panel);
    }

    private void registerUser() {
        try {
            panel.populateUser();

            // FIXME: monitor.resetChanges();
            //user.setLoggedIn(true);
            user = presenter.doRegister(user, isNewUser);
            postRegisterStrategy.execute(user);
        } catch (EmfException e) {
            panel.setError(e.getMessage());
            refresh();
            return;
        }

        container.disposeView();
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

    public boolean confirmDiscardChanges() {
        if (changeablesList instanceof EmfInternalFrame)
            return ((EmfInternalFrame) changeablesList).shouldDiscardChanges();

        return true;
    }

    public void closeWindow() {
        if (confirmDiscardChanges())
            cancelStrategy.execute(presenter);
    }

}
