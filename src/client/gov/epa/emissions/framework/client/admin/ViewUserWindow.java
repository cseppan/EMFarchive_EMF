package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfInternalFrame;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

public abstract class ViewUserWindow extends EmfInternalFrame implements UserView {

    private ViewUserPresenter presenter;

    private JPanel layout;

    private SingleLineMessagePanel messagePanel;

    public ViewUserWindow() {
        super("User: ", new Dimension(350, 425));

        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        super.getContentPane().add(layout);
    }

    public void display(User user) {
        doLayout(user);

        super.setTitle("User: " + user.getUsername());
        super.dimensions(layout.getSize());
        super.setResizable(false);

        super.display();
    }

    private void doLayout(User user) {
        messagePanel = new SingleLineMessagePanel();
        messagePanel.setMessage(lockStatus(user));
        layout.add(messagePanel);

        layout.add(createProfilePanel(user));
    }

    private String lockStatus(User user) {
        if (!user.isLocked())
            return "";

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
        return "Locked by " + user.getLockOwner() + " at " + dateFormat.format(user.getLockDate());
    }

    private ViewableUserProfilePanel createProfilePanel(User user) {
        Action closeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    presenter.doClose();
                } catch (EmfException e) {
                    setError(e.getMessage());
                }
            }
        };

        return new ViewableUserProfilePanel(user, closeAction);
    }

    protected void setError(String message) {
        messagePanel.setError(message);
    }

    public void observe(ViewUserPresenter presenter) {
        this.presenter = presenter;
    }

}
