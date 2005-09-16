package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.gui.LabelWidget;
import gov.epa.emissions.commons.gui.Widget;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfView;
import gov.epa.emissions.framework.services.User;

import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class UpdateUserWindow extends DisposableInteralFrame implements EmfView, UpdateUserView {

    private UpdateUserPresenter presenter;

    private User user;

    private String windowTitle;

    private UserProfilePanel panel;

    public UpdateUserWindow(User user, AdminOption adminOption) {
        super("Update User - " + user.getUsername());

        this.windowTitle = "Update User - " + user.getUsername();
        this.user = user;

        panel = createLayout(adminOption);
        JPanel container = new JPanel();
        container.add(panel);
        super.getContentPane().add(container);

        super.setSize(panel.getSize());
        super.setResizable(false);
    }

    public UpdateUserWindow(User user) {
        this(user, new NoAdminOption());
    }

    private UserProfilePanel createLayout(AdminOption adminOption) {
        Action saveAction = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                updateUser();
            }
        };
        Action closeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                presenter.notifyClose();
            }
        };

        Widget name = new LabelWidget("name", user.getFullName());
        return createUserProfilePanel(name, saveAction, closeAction, adminOption);
    }

    private UserProfilePanel createUserProfilePanel(Widget name, Action saveAction, Action closeAction,
            AdminOption adminOption) {
        UserProfilePanel panel = new UserProfilePanel(user, name, saveAction, closeAction, adminOption,
                new PopulateUserOnUpdateStrategy(user));

        panel.addEditListener(new KeyAdapter() {
            public void keyTyped(KeyEvent event) {
                markAsEdited();
            }
        });

        return panel;
    }

    private void markAsEdited() {
        this.setTitle(windowTitle + " *");
        presenter.notifyChanges();
    }

    private void updateUser() {
        try {
            panel.populateUser();
            presenter.notifySave(user);
        } catch (EmfException e) {
            panel.setError(e.getMessage());
            return;
        }

        close();
    }

    public void setObserver(UpdateUserPresenter presenter) {
        this.presenter = presenter;
    }

    public void closeOnConfirmLosingChanges() {
        int option = JOptionPane.showConfirmDialog(null,
                "Would you like to Close(without saving and lose the updates)?", "Close", JOptionPane.YES_NO_OPTION);
        if (option == 0)
            close();
    }

}
