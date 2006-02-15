package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.gui.ChangeablesList;
import gov.epa.emissions.commons.gui.TextFieldWidget;
import gov.epa.emissions.commons.gui.Widget;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfView;
import gov.epa.emissions.framework.client.WidgetChangesMonitor;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class RegisterUserPanel extends JPanel {

    private RegisterUserPresenter presenter;

    private PostRegisterStrategy postRegisterStrategy;

    private EmfView container;

    protected JPanel profileValuesPanel;

    private RegisterCancelStrategy cancelStrategy;

    private EditableUserProfilePanel panel;

    private User user;
    
    private ChangeablesList changeablesList;
    
    private WidgetChangesMonitor monitor;

    public RegisterUserPanel(PostRegisterStrategy postRegisterStrategy, RegisterCancelStrategy cancelStrategy,
            EmfView parent) {
        this(postRegisterStrategy, cancelStrategy, parent, new NoAdminOption());
    }

    public RegisterUserPanel(PostRegisterStrategy postRegisterStrategy, RegisterCancelStrategy cancelStrategy,
            EmfView parent, AdminOption adminOption) {
        this.postRegisterStrategy = postRegisterStrategy;
        this.cancelStrategy = cancelStrategy;
        this.container = parent;
        changeablesList = new ChangeablesList((RegisterUserInternalFrame)container);
        monitor = new WidgetChangesMonitor(changeablesList, null);

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
                confirmDiscardChanges();
                cancelStrategy.execute(presenter);
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
            monitor.resetChanges();
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
    
    public boolean confirmDiscardChanges() {
        return (monitor.checkChanges() == JOptionPane.OK_OPTION);
    }
    
    public void closeWindow() {
        confirmDiscardChanges();
        cancelStrategy.execute(presenter);
    }

}
