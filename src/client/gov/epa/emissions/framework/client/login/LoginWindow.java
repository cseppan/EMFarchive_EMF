package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfConsole;
import gov.epa.emissions.framework.client.EmfConsolePresenter;
import gov.epa.emissions.framework.client.DefaultEmfSession;
import gov.epa.emissions.framework.client.EmfFrame;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.client.admin.PostRegisterStrategy;
import gov.epa.emissions.framework.client.admin.RegisterUserPresenter;
import gov.epa.emissions.framework.client.admin.RegisterUserWindow;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.User;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
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
        super("Login", "Login to EMF");
        this.serviceLocator = serviceLocator;

        JPanel layoutPanel = createLayout();

        this.setSize(new Dimension(350, 225));
        this.setLocation(new Point(400, 200));

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

        JButton signIn = new Button("Sign In", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doSignIn();
            }
        });
        container.add(signIn);
        getRootPane().setDefaultButton(signIn);

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
            User user = presenter.notifyLogin(username.getText(), new String(password.getPassword()));
            messagePanel.clear();
            refresh();
            launchConsole(user);
            close();
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private void launchConsole(User user) throws EmfException {
        EmfConsole console = new EmfConsole(new DefaultEmfSession(user, serviceLocator));
        EmfConsolePresenter presenter = new EmfConsolePresenter(console);
        presenter.observe();

        console.display();
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

    private JPanel createLoginOptionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel forgotPassword = new JLabel("    Forgot your Password ?");
        forgotPassword.setEnabled(false);
        forgotPassword.setToolTipText("To be implemented");
        JPanel forgotPasswordPanel = new JPanel(new BorderLayout());
        forgotPasswordPanel.add(forgotPassword);

        panel.add(forgotPasswordPanel);

        JLabel register = new JLabel("<html>&nbsp;&nbsp;&nbsp;&nbsp;<a href=''>Not yet registered ?</a></html>");
        register.setName("registerUser");
        register.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent arg0) {
                try {
                    launchRegisterUser();
                } catch (Exception e) {
                    messagePanel.setError(e.getMessage());
                }
                close();
            }
        });
        register.setToolTipText("Register as a new user");

        JPanel registerPanel = new JPanel(new BorderLayout());
        registerPanel.add(register);

        panel.add(registerPanel);

        return panel;
    }

    private void launchRegisterUser() throws Exception {
        PostRegisterStrategy strategy = new LaunchEmfConsolePostRegisterStrategy(serviceLocator);
        RegisterUserWindow window = new RegisterUserWindow(serviceLocator, strategy);
        RegisterUserPresenter presenter = new RegisterUserPresenter(serviceLocator.getUserServices(), window.getView());
        presenter.observe();

        window.display();
    }

    private void refresh() {
        super.validate();
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
