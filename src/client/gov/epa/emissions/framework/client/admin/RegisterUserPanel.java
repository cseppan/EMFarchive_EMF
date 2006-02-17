package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.gui.ChangeObserver;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.TextFieldWidget;
import gov.epa.emissions.commons.gui.Widget;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfInternalFrame;
import gov.epa.emissions.framework.client.EmfView;
import gov.epa.emissions.framework.client.ManagedView;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

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
    
    private String parentWindowTitle;

    public RegisterUserPanel(PostRegisterStrategy postRegisterStrategy, RegisterCancelStrategy cancelStrategy,
            EmfView parent, ChangeObserver changeObserver, ManageChangeables changeablesList) {
        this(postRegisterStrategy, cancelStrategy, parent, new NoAdminOption(), changeObserver, changeablesList);
    }

    public RegisterUserPanel(PostRegisterStrategy postRegisterStrategy, RegisterCancelStrategy cancelStrategy,
            EmfView parent, AdminOption adminOption, ChangeObserver changeObserver, ManageChangeables changeablesList) {
        this.postRegisterStrategy = postRegisterStrategy;
        this.cancelStrategy = cancelStrategy;
        this.container = parent;
        this.changeablesList = changeablesList;
        this.parentWindowTitle = getParentWindowTitle();
        createLayout(adminOption);

        this.setSize(new Dimension(375, 425));
    }
    
    private String getParentWindowTitle() {
        return ((EmfView)changeablesList).getTitle();
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

        user = new User();
        Widget username = new TextFieldWidget("username", user.getUsername(), 10);
        panel = new EditableUserProfilePanel(user, username, okAction, cancelAction, adminOption,
                new PopulateUserOnRegisterStrategy(user), changeablesList);
        panel.addEditListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if(changeablesList instanceof ManagedView)
                    markAsEdited((ManagedView)changeablesList);
            }
        });
        this.add(panel);
    }
    
    private void markAsEdited(ManagedView window) {
        window.setTitle(parentWindowTitle + " *");
    }

    private void registerUser() {
        try {
            panel.populateUser();
            
            //FIXME: monitor.resetChanges();
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
        if(changeablesList instanceof EmfInternalFrame)
            return ((EmfInternalFrame)changeablesList).checkChanges();
        
        return true;
    }

    public void closeWindow() {
        if(confirmDiscardChanges())
            cancelStrategy.execute(presenter);
    }

}
