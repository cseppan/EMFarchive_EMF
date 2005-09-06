package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.UserException;
import gov.epa.emissions.framework.client.EmfWidgetContainer;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.services.User;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public class RegisterUserPanel extends JPanel implements RegisterUserView {

    private RegisterUserPresenter presenter;

    private JTextField username;

    private JPasswordField password;

    private JPasswordField confirmPassword;

    private JTextField name;

    private JTextField affiliation;

    private JTextField phone;

    private JTextField email;

    private PostRegisterStrategy postRegisterStrategy;

    private EmfWidgetContainer parent;

    private MessagePanel messagePanel;

    protected JPanel profileValuesPanel;

    private RegisterCancelStrategy cancelStrategy;

    public RegisterUserPanel(PostRegisterStrategy postRegisterStrategy, RegisterCancelStrategy cancelStrategy,
            EmfWidgetContainer parent) {
        this.postRegisterStrategy = postRegisterStrategy;
        this.cancelStrategy = cancelStrategy;
        this.parent = parent;

        createLayout();

        this.setSize(new Dimension(375, 425));
    }

    private void createLayout() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        this.add(messagePanel);
        this.add(createProfilePanel());
        this.add(createLoginPanel());
        this.add(createButtonsPanel());
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(25);
        container.setLayout(layout);

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                cancelStrategy.execute(presenter);
            }
        });

        JButton ok = new JButton("Ok");
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                registerUser();
            }
        });

        container.add(ok);
        container.add(cancel);

        panel.add(container, BorderLayout.EAST);

        return panel;
    }

    private void registerUser() {
        if (presenter == null)
            return;

        try {
            User user = new User();
            populateUser(user);

            presenter.notifyRegister(user);
            postRegisterStrategy.execute(user);
            close();
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
            refresh();
        }
    }

    protected void populateUser(User user) throws UserException {
        user.setUserName(username.getText());
        user.setPassword(new String(password.getPassword()));
        user.confirmPassword(new String(confirmPassword.getPassword()));
        user.setFullName(name.getText());
        user.setAffiliation(affiliation.getText());
        user.setEmailAddr(email.getText());
        user.setWorkPhone(phone.getText());
    }

    public void refresh() {
        this.validate();
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(createBorder("Login"));

        GridLayout labelsLayoutManager = new GridLayout(3, 1);
        labelsLayoutManager.setVgap(15);
        JPanel labelsPanel = new JPanel(labelsLayoutManager);
        labelsPanel.add(new JLabel("Username"));
        labelsPanel.add(new JLabel("Password"));
        labelsPanel.add(new JLabel("Confirm Password"));

        panel.add(labelsPanel);

        GridLayout valuesLayoutManager = new GridLayout(3, 1);
        valuesLayoutManager.setVgap(10);
        JPanel valuesPanel = new JPanel(valuesLayoutManager);

        username = new JTextField(10);
        valuesPanel.add(username);
        password = new JPasswordField(10);
        valuesPanel.add(password);
        confirmPassword = new JPasswordField(10);
        valuesPanel.add(confirmPassword);

        panel.add(valuesPanel);

        panel.setMaximumSize(new Dimension(300, 125));

        return panel;
    }

    private JPanel createProfilePanel() {
        JPanel profilePanel = new JPanel();

        Border titledBorder = createBorder("Profile");
        profilePanel.setBorder(titledBorder);

        GridLayout labelsLayoutManager = new GridLayout(4, 1);
        labelsLayoutManager.setVgap(15);
        JPanel profileLabelsPanel = new JPanel(labelsLayoutManager);

        profileLabelsPanel.setLayout(labelsLayoutManager);
        profileLabelsPanel.add(new JLabel("Name"));
        profileLabelsPanel.add(new JLabel("Affiliation"));
        profileLabelsPanel.add(new JLabel("Phone"));
        profileLabelsPanel.add(new JLabel("Email"));

        profilePanel.add(profileLabelsPanel);

        GridLayout valuesLayoutManager = new GridLayout(4, 1);
        valuesLayoutManager.setVgap(10);
        profileValuesPanel = new JPanel(valuesLayoutManager);

        name = new JTextField(10);
        profileValuesPanel.add(name);
        affiliation = new JTextField(10);
        profileValuesPanel.add(affiliation);
        phone = new JTextField(10);
        profileValuesPanel.add(phone);
        email = new JTextField(10);
        profileValuesPanel.add(email);

        profilePanel.add(profileValuesPanel);

        profilePanel.setMaximumSize(new Dimension(300, 175));

        return profilePanel;
    }

    private Border createBorder(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(title);
        border.setTitleJustification(TitledBorder.LEFT);

        return border;
    }

    public void close() {
        parent.close();
    }

    public void observe(RegisterUserPresenter presenter) {
        this.presenter = presenter;
    }

    public RegisterUserPresenter getPresenter() {
        return presenter;
    }

}
