package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfConsole;
import gov.epa.emissions.framework.client.EmfWindow;
import gov.epa.emissions.framework.client.admin.RegisterUserPresenter;
import gov.epa.emissions.framework.commons.EMFUserAdmin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class LoginWindow extends EmfWindow implements LoginView {

    private JTextField username;

    private JPasswordField password;

    private JLabel errorMessage;

    private LoginPresenter presenter;

    private EMFUserAdmin userAdmin;

    public LoginWindow(EMFUserAdmin userAdmin) throws Exception {
        this.userAdmin = userAdmin;
        JPanel layoutPanel = createLayout();

        this.setSize(new Dimension(350, 250));
        this.setLocation(new Point(400, 200));
        this.setTitle("Login");

        this.getContentPane().add(layoutPanel);
    }

    private JPanel createLayout() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(createErrorMessagePanel());
        panel.add(createLoginPanel());
        panel.add(createButtonsPanel());
        panel.add(new JSeparator(SwingConstants.HORIZONTAL));
        panel.add(createLoginOptionsPanel());

        return panel;
    }

    private JPanel createErrorMessagePanel() {
        JPanel errorMessagePanel = new JPanel();
        errorMessage = new JLabel();
        errorMessagePanel.add(errorMessage);

        return errorMessagePanel;
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(25);
        container.setLayout(layout);

        JButton signIn = new JButton("Sign In");
        signIn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (presenter != null) {
                    try {
                        presenter.notifyLogin(username.getText(), password.getText());
                        clearError();
                        launchConsole();
                    } catch (EmfException e) {
                        setError(e.getMessage());
                    }
                }
            }

        });
        container.add(signIn);

        panel.add(container, BorderLayout.EAST);

        return panel;
    }

    private void launchConsole() {
        try {
            EmfConsole console = new EmfConsole();
            console.setVisible(true);
        } catch (Exception e) {
            // TODO: exit app w/ error notification ?
        }

        this.close();
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
        valuesPanel.add(username);
        password = new JPasswordField(10);
        valuesPanel.add(password);

        panel.add(valuesPanel);

        return panel;
    }

    private JPanel createLoginOptionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel forgotPassword = new JLabel("<html><a href=''>Forgot your Password ?</a></html>");
        forgotPassword.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {//TODO: deferred
            }
        });
        JPanel forgotPasswordPanel = new JPanel(new BorderLayout());
        forgotPasswordPanel.add(forgotPassword);

        panel.add(forgotPasswordPanel, BorderLayout.EAST);

        JLabel register = new JLabel("<html><a href=''>Not yet registered ?</a></html>");
        register.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent arg0) {
                try {
                    launchCreateUser();
                } catch (Exception e) {
                    //TODO: launch failure ??
                }
                close();
            }
        });
        
        JPanel registerPanel = new JPanel(new BorderLayout());
        registerPanel.add(register);

        panel.add(registerPanel, BorderLayout.EAST);

        return panel;
    }

    private void launchCreateUser() throws Exception {
        RegisterUserWindow window = new RegisterUserWindow();
        RegisterUserPresenter presenter = new RegisterUserPresenter(userAdmin, window);
        presenter.init();

        window.setVisible(true);        
    }

    private void clearError() {
        errorMessage.setText("");

        this.validate();
    }

    private void setError(String message) {
        errorMessage.setText("");
        errorMessage.setForeground(Color.RED);
        errorMessage.setText(message);

        this.validate();
    }

    public void close() {
        this.dispose();
    }

    public void setObserver(LoginPresenter presenter) {
        this.presenter = presenter;
    }

}
