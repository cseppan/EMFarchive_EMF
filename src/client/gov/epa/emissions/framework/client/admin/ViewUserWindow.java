package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfInternalFrame;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;

public abstract class ViewUserWindow extends EmfInternalFrame implements UserView {

    private ViewUserPresenter presenter;

    private ViewableUserProfilePanel panel;

    public ViewUserWindow() {
        super("User: ");
    }

    public void display(User user) {
        super.setTitle("User: " + user.getUsername());

        JPanel container = new JPanel();
        panel = createLayout(user);
        container.add(panel);

        super.getContentPane().add(container);
        super.dimensions(panel.getSize());
        super.setResizable(false);

        super.display();
    }

    private ViewableUserProfilePanel createLayout(User user) {
        Action closeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    presenter.doClose();
                } catch (EmfException e) {
                    panel.setError(e.getMessage());
                }
            }
        };

        return new ViewableUserProfilePanel(user, closeAction);
    }

    public void observe(ViewUserPresenter presenter) {
        this.presenter = presenter;
    }

}
