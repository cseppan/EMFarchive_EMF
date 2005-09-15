package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.PasswordField;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.UserException;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.services.User;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public class UserProfilePanel extends JPanel {

    private JTextField username;

    private JPasswordField password;

    private JPasswordField confirmPassword;

    private JTextField name;

    private JTextField affiliation;

    private JTextField phone;

    private JTextField email;

    private MessagePanel messagePanel;

    protected JPanel profileValuesPanel;

    private JPanel profileLabelsPanel;

    public UserProfilePanel(Action okAction, Action cancelAction) {
        createLayout(okAction, cancelAction);

        this.setSize(new Dimension(375, 425));
    }

    private void createLayout(Action okAction, Action cancelAction) {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        this.add(messagePanel);
        this.add(createProfilePanel());
        this.add(createLoginPanel());
        this.add(createButtonsPanel(okAction, cancelAction));
    }

    private JPanel createButtonsPanel(Action okAction, Action cancelAction) {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(25);
        container.setLayout(layout);

        container.add(new Button("Ok", okAction));
        container.add(new Button("Cancel", cancelAction));

        panel.add(container, BorderLayout.EAST);

        return panel;
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

        username = new TextField("username", 10);
        valuesPanel.add(username);
        password = new PasswordField("password", 10);
        valuesPanel.add(password);
        confirmPassword = new PasswordField("confirmPassword", 10);
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

        name = new TextField("name", 15);
        profileValuesPanel.add(name);
        profileValuesPanel.add(Box.createRigidArea(new Dimension(1, 10)));
        affiliation = new TextField("affiliation", 15);
        profileValuesPanel.add(affiliation);
        profileValuesPanel.add(Box.createRigidArea(new Dimension(1, 10)));
        phone = new TextField("phone", 15);
        profileValuesPanel.add(phone);
        profileValuesPanel.add(Box.createRigidArea(new Dimension(1, 10)));
        email = new TextField("email", 15);
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

    protected void populateUser(User user) throws UserException {
        user.setFullName(name.getText());
        user.setAffiliation(affiliation.getText());
        user.setWorkPhone(phone.getText());
        user.setEmailAddr(email.getText());

        user.setUserName(username.getText());
        user.setPassword(new String(password.getPassword()));
        user.confirmPassword(new String(confirmPassword.getPassword()));
    }

    // FIXME: a cleaner, refactored version needed
    void addToProfilePanel(JComponent component) {
        profileLabelsPanel.add(Box.createRigidArea(new Dimension(1, 40)));

        profileValuesPanel.add(Box.createRigidArea(new Dimension(1, 10)));
        profileValuesPanel.add(component);
    }

    void setError(String message) {
        messagePanel.setError(message);
    }

}
