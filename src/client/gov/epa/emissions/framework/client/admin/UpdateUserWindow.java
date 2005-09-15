package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfInteralFrame;
import gov.epa.emissions.framework.client.EmfWidgetContainer;
import gov.epa.emissions.framework.services.User;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

public class UpdateUserWindow extends EmfInteralFrame implements EmfWidgetContainer, UpdateUserView {

    private UpdateUserPresenter presenter;

    private User user;

    private String windowTitle;

    private UserProfilePanel panel;

    public UpdateUserWindow(User user, AdminOption adminOption) {
        super("Update User - " + user.getUsername());

        this.windowTitle = "Update User - " + user.getUsername();
        this.user = user;

        panel = createLayout(adminOption);
        super.getContentPane().add(panel);

        super.setSize(new Dimension(375, 425));
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

        UserProfilePanel panel = new UserProfilePanel(user, saveAction, closeAction, adminOption,
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

    public void close() {
        this.dispose();
    }

    public void display() {
        this.setVisible(true);
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
