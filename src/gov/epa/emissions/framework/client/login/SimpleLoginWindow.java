package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfInteralFrame;
import gov.epa.emissions.framework.client.ErrorMessagePanel;
import gov.epa.emissions.framework.commons.EMFUserAdmin;
import gov.epa.emissions.framework.commons.User;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class SimpleLoginWindow extends EmfInteralFrame implements LoginView {

    private JTextField username;

    private JPasswordField password;

    private LoginPresenter presenter;

    private ErrorMessagePanel errorMessagePanel;

    public SimpleLoginWindow(EMFUserAdmin userAdmin) {
        super("Login");

        this.getContentPane().add(createLayout());
        this.setSize(new Dimension(350, 200));
    }

    private JPanel createLayout() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        errorMessagePanel = new ErrorMessagePanel();
        panel.add(errorMessagePanel);
        panel.add(createLoginPanel());
        panel.add(createButtonsPanel());

        return panel;
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(25);
        container.setLayout(layout);

        JButton signIn = new JButton("Sign In");
        signIn.setName("signIn");
        signIn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (presenter == null)
                    return;
                try {
                    User user = presenter.notifyLogin(username.getText(), new String(password.getPassword()));
                    // TODO: return the user to the listener
                    close();
                } catch (EmfException e) {
                    errorMessagePanel.setMessage(e.getMessage());
                }
            }

        });
        container.add(signIn);

        JButton cancel = new JButton("Cancel");
        cancel.setName("cancel");
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                close();
            }

        });
        container.add(cancel);

        panel.add(container, BorderLayout.EAST);

        return panel;
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel();

        GridLayout labelsLayoutManager = new GridLayout(2, 1);
        labelsLayoutManager.setVgap(15);
        JPanel labelsPanel = new JPanel(labelsLayoutManager);
        labelsPanel.add(new JLabel("Username"));
        labelsPanel.add(new JLabel("Password"));

        panel.add(labelsPanel);

        GridLayout valuesLayoutManager = new GridLayout(2, 1);
        valuesLayoutManager.setVgap(10);
        JPanel valuesPanel = new JPanel(valuesLayoutManager);

        username = new JTextField(10);
        username.setName("username");
        valuesPanel.add(username);
        password = new JPasswordField(10);
        password.setName("password");
        valuesPanel.add(password);

        panel.add(valuesPanel);

        return panel;
    }

    public void close() {
        this.dispose();
    }

    public void setObserver(LoginPresenter presenter) {
        this.presenter = presenter;
    }

    public void display() {
        super.setVisible(true);
    }
}
