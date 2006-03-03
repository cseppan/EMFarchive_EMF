package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.DefaultEmfSession;
import gov.epa.emissions.framework.client.EmfFrame;
import gov.epa.emissions.framework.client.admin.PostRegisterStrategy;
import gov.epa.emissions.framework.client.admin.RegisterUserPresenter;
import gov.epa.emissions.framework.client.admin.RegisterUserWindow;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.console.EmfConsolePresenter;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class LoginWindow extends EmfFrame implements LoginView {

    private JTextField username;

    private JPasswordField password;

    private LoginPresenter presenter;

    private MessagePanel messagePanel;

    private ServiceLocator serviceLocator;

    public LoginWindow(ServiceLocator serviceLocator) {
        super("Login", "Login to the Emissions Modeling Framework v0.4");
        this.serviceLocator = serviceLocator;

        JPanel layoutPanel = createLayout();

        this.setSize(new Dimension(350, 225));
        this.setLocation(ScreenUtils.getPointToCenter(this));

        this.getContentPane().add(layoutPanel);
    }

    private JPanel createLayout() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);
        panel.add(createLoginPanel());
        panel.add(createButtonsPanel());
        panel.add(new JSeparator(SwingConstants.HORIZONTAL));
        panel.add(createLoginOptionsPanel());

        return panel;
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(15);
        container.setLayout(layout);

        Button signIn = new Button("Log In", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doSignIn();
            }
        });
        container.add(signIn);
        setDefaultButton(signIn);

        JButton cancel = new Button("Cancel", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                close();
            }
        });
        container.add(cancel);

        panel.add(container, BorderLayout.CENTER);

        return panel;
    }

    private void doSignIn() {
        if (presenter == null)
            return;
        try {
            User user = presenter.doLogin(username.getText(), new String(password.getPassword()));
            messagePanel.clear();
            super.refreshLayout();
            launchConsole(user);
            close();
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private void launchConsole(User user) throws EmfException {
        EmfConsole console = new EmfConsole(new DefaultEmfSession(user, serviceLocator));
        EmfConsolePresenter presenter = new EmfConsolePresenter();
        presenter.display(console);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel();

        GridLayout labelsLayoutManager = new GridLayout(2, 1);
        labelsLayoutManager.setVgap(15);
        JPanel labelsPanel = new JPanel(labelsLayoutManager);
        labelsPanel.add(new JLabel("EMF Username"));
        labelsPanel.add(new JLabel("EMF Password"));

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

    private JPanel createLoginOptionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JButton forgotPassword = new JButton("Reset Password");
        forgotPassword.setEnabled(false);
        forgotPassword.setToolTipText("Not yet implemented");
        JPanel forgotPasswordPanel = new JPanel(new BorderLayout());
        forgotPasswordPanel.add(forgotPassword);

        JButton register = new Button("Register New User", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doRegisterNewUser();
            }
        });
        register.setToolTipText("Register as a new user");

        JPanel registerPanel = new JPanel(new BorderLayout());
        registerPanel.add(register);

        panel.add(registerPanel, BorderLayout.WEST);
        panel.add(forgotPasswordPanel, BorderLayout.EAST);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return panel;
    }

    private void doRegisterNewUser() {
        try {
            launchRegisterUser();
        } catch (Exception ex) {
            messagePanel.setError(ex.getMessage());
            return;
        }
        close();
    }

    private void launchRegisterUser() throws Exception {
        PostRegisterStrategy strategy = new LaunchEmfConsolePostRegisterStrategy(serviceLocator);
        RegisterUserWindow view = new RegisterUserWindow(serviceLocator, strategy);

        RegisterUserPresenter presenter = new RegisterUserPresenter(serviceLocator.userService());
        presenter.display(view);
    }

    public void observe(LoginPresenter presenter) {
        this.presenter = presenter;
    }

}
