package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.LabelWidget;
import gov.epa.emissions.commons.gui.PasswordField;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.Widget;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.ui.Border;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ViewableUserProfilePanel extends JPanel {

    private MessagePanel messagePanel;

    private User user;

    // FIXME: one to many params ?
    public ViewableUserProfilePanel(User user, Action closeAction) {
        this.user = user;

        createLayout(user, closeAction);
        this.setSize(new Dimension(375, 425));
    }

    private void createLayout(User user, Action closeAction) {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        this.add(messagePanel);
        this.add(createProfilePanel(user));

        this.add(createLoginPanel());
        this.add(createButtonsPanel(closeAction));
    }

    private JPanel createButtonsPanel(Action closeAction) {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(25);
        container.setLayout(layout);

        Button closeButton = new Button("Close", closeAction);
        container.add(closeButton);

        panel.add(container, BorderLayout.EAST);

        return panel;
    }

    private JPanel createLoginPanel() {
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

        Widget usernameWidget = new LabelWidget("username", user.getUsername());
        valuesPanel.add(usernameWidget.element());
        PasswordField password = new PasswordField("password", 10);
        valuesPanel.add(password);

        panel.add(valuesPanel);

        panel.setMaximumSize(new Dimension(300, 125));

        return panel;
    }

    private JPanel createProfilePanel(User user) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new Border("Profile"));

        panel.add(createManadatoryProfilePanel(user));
        panel.add(createOptionsPanel(user));

        return panel;
    }

    private JPanel createOptionsPanel(User user) {
        JPanel optionsPanel = new JPanel();

        JCheckBox isAdmin = new JCheckBox("Is Admin?");
        isAdmin.setSelected(user.isInAdminGroup());
        optionsPanel.add(isAdmin);

        return optionsPanel;
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

        TextField name = new TextField("name", user.getFullName(), 15);
        valuesPanel.add(name);
        valuesPanel.add(Box.createRigidArea(new Dimension(1, 10)));

        TextField affiliation = new TextField("affiliation", user.getAffiliation(), 15);
        valuesPanel.add(affiliation);
        valuesPanel.add(Box.createRigidArea(new Dimension(1, 10)));

        TextField phone = new TextField("phone", user.getPhone(), 15);
        valuesPanel.add(phone);
        valuesPanel.add(Box.createRigidArea(new Dimension(1, 10)));

        TextField email = new TextField("email", user.getEmail(), 15);
        valuesPanel.add(email);

        panel.add(valuesPanel);

        panel.setMaximumSize(new Dimension(300, 175));

        return panel;
    }

    void setError(String message) {
        messagePanel.setError(message);
    }

}
