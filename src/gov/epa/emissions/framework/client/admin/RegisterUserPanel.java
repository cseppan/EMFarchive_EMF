package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.gui.DefaultButton;
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

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
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

    private JPanel profileLabelsPanel;

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

        JButton cancel = new DefaultButton("Cancel", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                cancelStrategy.execute(presenter);
            }
        });

        JButton ok = new DefaultButton("Ok", new AbstractAction() {
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
        user.setFullName(name.getText());
        user.setAffiliation(affiliation.getText());
        user.setWorkPhone(phone.getText());
        user.setEmailAddr(email.getText());

        user.setUserName(username.getText());
        user.setPassword(new String(password.getPassword()));
        user.confirmPassword(new String(confirmPassword.getPassword()));
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

        profileLabelsPanel = new JPanel();
        profileLabelsPanel.setLayout(new BoxLayout(profileLabelsPanel, BoxLayout.Y_AXIS));

        profileLabelsPanel.add(new JLabel("Name"));
        profileLabelsPanel.add(Box.createRigidArea(new Dimension(1, 15)));
        profileLabelsPanel.add(new JLabel("Affiliation"));
        profileLabelsPanel.add(Box.createRigidArea(new Dimension(1, 15)));
        profileLabelsPanel.add(new JLabel("Phone"));
        profileLabelsPanel.add(Box.createRigidArea(new Dimension(1, 15)));
        profileLabelsPanel.add(new JLabel("Email"));

        profilePanel.add(profileLabelsPanel);

        profileValuesPanel = new JPanel();
        profileValuesPanel.setLayout(new BoxLayout(profileValuesPanel, BoxLayout.Y_AXIS));

        name = new JTextField(15);
        profileValuesPanel.add(name);
        profileValuesPanel.add(Box.createRigidArea(new Dimension(1, 10)));
        affiliation = new JTextField(15);
        profileValuesPanel.add(affiliation);
        profileValuesPanel.add(Box.createRigidArea(new Dimension(1, 10)));
        phone = new JTextField(15);
        profileValuesPanel.add(phone);
        profileValuesPanel.add(Box.createRigidArea(new Dimension(1, 10)));
        email = new JTextField(15);
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

    // FIXME: a cleaner, refactored version needed
    public void addToProfilePanel(JComponent component) {
        profileLabelsPanel.add(Box.createRigidArea(new Dimension(1, 40)));

        profileValuesPanel.add(Box.createRigidArea(new Dimension(1, 10)));
        profileValuesPanel.add(component);
    }

}
