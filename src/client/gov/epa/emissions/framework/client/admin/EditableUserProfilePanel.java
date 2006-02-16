package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ChangeablesList;
import gov.epa.emissions.commons.gui.PasswordField;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.Widget;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.KeyListener;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

public class EditableUserProfilePanel extends JPanel {

    private Widget username;

    private JPasswordField password;

    private JPasswordField confirmPassword;

    private TextField name;

    private TextField affiliation;

    private TextField phone;

    private TextField email;

    private MessagePanel messagePanel;

    private AdminOption adminOption;

    private PopulateUserStrategy populateUserStrategy;
    
    private ChangeablesList changeablesList;

    private User user;

    // FIXME: one to many params ?
    public EditableUserProfilePanel(User user, Widget usernameWidget, Action okAction, Action cancelAction,
            AdminOption adminOption, PopulateUserStrategy populateUserStrategy, ChangeablesList changeablesList) {
        this.user = user;
        this.adminOption = adminOption;
        this.populateUserStrategy = populateUserStrategy;
        this.changeablesList = changeablesList;

        createLayout(user, usernameWidget, okAction, cancelAction, adminOption);
        this.setSize(new Dimension(375, 425));
    }

    private void createLayout(User user, Widget usernameWidget, Action okAction, Action cancelAction,
            AdminOption adminOption) {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        this.add(messagePanel);
        this.add(createProfilePanel(user, adminOption));

        this.add(createLoginPanel(usernameWidget));
        this.add(createButtonsPanel(okAction, cancelAction));
    }

    private JPanel createButtonsPanel(Action okAction, Action cancelAction) {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(25);
        container.setLayout(layout);

        Button okButton = new Button("Save", okAction);
        container.add(okButton);
        container.add(new Button("Cancel", cancelAction));

        panel.add(container, BorderLayout.EAST);

        return panel;
    }

    private JPanel createLoginPanel(Widget usernameWidget) {
        JPanel panel = new JPanel();
        panel.setBorder(new Border("Login"));

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

        username = usernameWidget;
        valuesPanel.add(usernameWidget.element());
        password = new PasswordField("password", 10);
        valuesPanel.add(password);
        confirmPassword = new PasswordField("confirmPassword", 10);
        valuesPanel.add(confirmPassword);

        panel.add(valuesPanel);

        panel.setMaximumSize(new Dimension(300, 125));

        return panel;
    }

    private JPanel createProfilePanel(User user, AdminOption adminOption) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new Border("Profile"));

        JPanel mandatoryPanel = createManadatoryProfilePanel(user);
        panel.add(mandatoryPanel);

        JPanel optionsPanel = new JPanel();
        adminOption.add(optionsPanel);
        panel.add(optionsPanel);

        return panel;
    }

    private JPanel createManadatoryProfilePanel(User user) {
        JPanel panel = new JPanel();

        JPanel labelsPanel = new JPanel();
        labelsPanel.setLayout(new BoxLayout(labelsPanel, BoxLayout.Y_AXIS));

        labelsPanel.add(new JLabel("Name"));
        labelsPanel.add(Box.createRigidArea(new Dimension(1, 15)));
        labelsPanel.add(new JLabel("Affiliation"));
        labelsPanel.add(Box.createRigidArea(new Dimension(1, 15)));
        labelsPanel.add(new JLabel("Phone"));
        labelsPanel.add(Box.createRigidArea(new Dimension(1, 15)));
        labelsPanel.add(new JLabel("Email"));

        panel.add(labelsPanel);

        JPanel valuesPanel = new JPanel();
        valuesPanel.setLayout(new BoxLayout(valuesPanel, BoxLayout.Y_AXIS));

        name = new TextField("name", user.getName(), 15);
        if(changeablesList != null) {
            changeablesList.add(name);
            name.addTextListener();
        }
        valuesPanel.add(name);
        valuesPanel.add(Box.createRigidArea(new Dimension(1, 10)));

        affiliation = new TextField("affiliation", user.getAffiliation(), 15);
        if(changeablesList != null) {
            changeablesList.add(affiliation);
            affiliation.addTextListener();
        }
        valuesPanel.add(affiliation);
        valuesPanel.add(Box.createRigidArea(new Dimension(1, 10)));

        phone = new TextField("phone", user.getPhone(), 15);
        if(changeablesList != null) {
            changeablesList.add(phone);
            phone.addTextListener();
        }
        valuesPanel.add(phone);
        valuesPanel.add(Box.createRigidArea(new Dimension(1, 10)));

        email = new TextField("email", user.getEmail(), 15);
        if(changeablesList != null) {
            changeablesList.add(email);
            email.addTextListener();
        }
        valuesPanel.add(email);

        panel.add(valuesPanel);

        panel.setMaximumSize(new Dimension(300, 175));

        return panel;
    }

    protected void populateUser() throws EmfException {
        populateUserStrategy.populate(name.getText(), affiliation.getText(), phone.getText(), email.getText(), username
                .value(), password.getPassword(), confirmPassword.getPassword());

        // FIXME: where should we put this ?
        adminOption.setInAdminGroup(user);
    }

    void setError(String message) {
        messagePanel.setError(message);
    }

    // TODO: add admin option listener
    public void addEditListener(KeyListener listener) {
        name.addKeyListener(listener);
        affiliation.addKeyListener(listener);
        phone.addKeyListener(listener);
        email.addKeyListener(listener);

        username.addKeyListener(listener);
        password.addKeyListener(listener);
        confirmPassword.addKeyListener(listener);
    }

}
