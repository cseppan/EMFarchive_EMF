package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.gui.LabelWidget;
import gov.epa.emissions.commons.gui.Widget;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfInternalFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.EmfDialog;

import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public abstract class UpdateUserWindow extends EmfInternalFrame implements UpdatableUserView {

    private UpdateUserPresenter presenter;

    private User user;

    private String windowTitle;

    private EditableUserProfilePanel panel;

    private AdminOption adminOption;

    public UpdateUserWindow(AdminOption adminOption, DesktopManager desktopManager) {
        super("Update User", desktopManager);
        this.adminOption = adminOption;

        super.setResizable(false);
    }

    public UpdateUserWindow(DesktopManager desktopManager) {
        this(new NoAdminOption(), desktopManager);
    }

    public void display(User user) {
        this.user = user;
        setEmbellishments(user);

        JPanel container = new JPanel();
        panel = createLayout(adminOption);
        container.add(panel);

        super.getContentPane().add(container);
        super.dimensions(panel.getSize());

        super.display();
    }

    private void setEmbellishments(User user) {
        super.setTitle("Update User: " + user.getUsername());
        this.windowTitle = "Update User: " + user.getUsername();
        super.setName("updateUser" + user.getId());
    }

    private EditableUserProfilePanel createLayout(AdminOption adminOption) {
        Action saveAction = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                updateUser();
            }
        };
        Action closeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    presenter.doClose();
                } catch (EmfException e) {
                    panel.setError(e.getMessage());
                }
            }
        };

        Widget username = new LabelWidget("username", user.getUsername());
        return createUserProfilePanel(username, saveAction, closeAction, adminOption);
    }

    private EditableUserProfilePanel createUserProfilePanel(Widget username, Action saveAction, Action closeAction,
            AdminOption adminOption) {
        EditableUserProfilePanel panel = new EditableUserProfilePanel(user, username, saveAction, closeAction,
                adminOption, new PopulateUserOnUpdateStrategy(user), this);

        panel.addEditListener(new KeyAdapter() {
            public void keyTyped(KeyEvent event) {
                markAsEdited();
            }
        });

        return panel;
    }

    private void markAsEdited() {
        this.setTitle(windowTitle + " *");
        presenter.onChange();
    }

    private void updateUser() {
        try {
            panel.populateUser();
            presenter.doSave();
        } catch (EmfException e) {
            panel.setError(e.getMessage());
            return;
        }

        close();
    }

    public void observe(UpdateUserPresenter presenter) {
        this.presenter = presenter;
    }

    public void closeOnConfirmLosingChanges() {
        String message = "Would you like to close without saving and lose your updates?";
        EmfDialog dialog = new EmfDialog(this, "Close", JOptionPane.QUESTION_MESSAGE, message,
                JOptionPane.YES_NO_OPTION);
        if (dialog.confirm())
            close();
    }

    public void windowClosing() {
        try {
            presenter.doClose();
        } catch (EmfException e) {
            panel.setError(e.getMessage());
        }
    }

}
