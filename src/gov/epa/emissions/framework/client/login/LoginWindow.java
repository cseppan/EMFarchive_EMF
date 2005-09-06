package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfConsole;
import gov.epa.emissions.framework.client.EmfConsolePresenter;
import gov.epa.emissions.framework.client.EmfWindow;
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
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

public class LoginWindow extends EmfWindow implements LoginView {

    private JTextField username;

    private JPasswordField password;

    private LoginPresenter presenter;

    private MessagePanel messagePanel;

    private ServiceLocator serviceLocator;

    public LoginWindow(ServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;

        JPanel layoutPanel = createLayout();

        this.setSize(new Dimension(350, 250));
        this.setLocation(new Point(400, 200));
        this.setTitle("Login");

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
        layout.setVgap(25);
        container.setLayout(layout);

        JButton signIn = new JButton("Sign In");
        signIn.setName("signIn");
        addKeyBinding(signIn);
        signIn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                doSignIn();
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

    private void addKeyBinding(JButton signIn) {
        Action doSignIn = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doSignIn();
            }
        };
        signIn.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), "SignIn");
        signIn.getActionMap().put("SignIn", doSignIn);
    }

    private void launchConsole(User user) {
        EmfConsole console = new EmfConsole(user, serviceLocator);
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

        JLabel forgotPassword = new JLabel("<html><a href=''>Forgot your Password ?</a></html>");
        forgotPassword.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {// TODO: deferred
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
                    // TODO: launch failure ??
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
