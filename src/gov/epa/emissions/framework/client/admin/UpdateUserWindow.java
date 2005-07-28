package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfInteralFrame;
import gov.epa.emissions.framework.client.EmfWidgetContainer;
import gov.epa.emissions.framework.client.ErrorMessagePanel;
import gov.epa.emissions.framework.commons.User;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public class UpdateUserWindow extends EmfInteralFrame implements EmfWidgetContainer, UpdateUserView {

    private UpdateUserPresenter presenter;

    private ErrorMessagePanel errorMessagePanel;

    private User user;

    private JTextField affiliation;

    private JTextField phone;

    private JTextField email;

    private JTextField name;

    private JPasswordField password;

    private JPasswordField confirmPassword;

    // TODO: refactor the duplication b/w register & update user widgets
    public UpdateUserWindow(User user) {
        super("Update User - " + user.getUserName());

        this.user = user;

        super.setSize(new Dimension(350, 400));
        super.getContentPane().add(createLayout());
    }

    private JPanel createLayout() {
        JPanel layout = new JPanel();

        errorMessagePanel = new ErrorMessagePanel();
        layout.add(errorMessagePanel);
        layout.add(createProfilePanel());
        layout.add(createLoginPanel());
        layout.add(createButtonsPanel());

        return layout;
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel();

        Border titledBorder = createBorder("Profile");
        panel.setBorder(titledBorder);

        GridLayout labelsLayoutManager = new GridLayout(4, 1);
        labelsLayoutManager.setVgap(15);
        JPanel labelsPanel = new JPanel(labelsLayoutManager);
        labelsPanel.add(new JLabel("Name"));
        labelsPanel.add(new JLabel("Affiliation"));
        labelsPanel.add(new JLabel("Phone"));
        labelsPanel.add(new JLabel("Email"));

        panel.add(labelsPanel);

        GridLayout valuesLayoutManager = new GridLayout(4, 1);
        valuesLayoutManager.setVgap(10);
        JPanel valuesPanel = new JPanel(valuesLayoutManager);

        name = new JTextField(user.getFullName(), 10);
        valuesPanel.add(name);
        affiliation = new JTextField(user.getAffiliation(), 10);
        valuesPanel.add(affiliation);
        phone = new JTextField(user.getWorkPhone(), 10);
        valuesPanel.add(phone);
        email = new JTextField(user.getEmailAddr(), 10);
        valuesPanel.add(email);

        panel.add(valuesPanel);

        panel.setMaximumSize(new Dimension(300, 175));

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

        JTextField username = new JTextField(user.getUserName(), 10);
        valuesPanel.add(username);
        password = new JPasswordField(10);
        valuesPanel.add(password);
        confirmPassword = new JPasswordField(10);
        valuesPanel.add(confirmPassword);

        panel.add(valuesPanel);

        panel.setMaximumSize(new Dimension(300, 125));

        return panel;
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
                if (presenter != null) {
                    presenter.notifyCancel();
                }
            }
        });
        container.add(cancel);

        JButton ok = new JButton("Ok");
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                updateUser();
            }
        });
        container.add(ok);

        panel.add(container, BorderLayout.EAST);

        return panel;
    }

    private void updateUser() {
        if (presenter != null) {
            try {
                user.setFullName(name.getText());
                user.setAffiliation(affiliation.getText());
                user.setWorkPhone(phone.getText());
                user.setEmailAddr(email.getText());
                if (password.getText().length() > 0) {
                    user.setPassword(password.getText());
                    user.confirmPassword(confirmPassword.getText());
                }

                presenter.notifyUpdate(user);
                close();
            } catch (EmfException e) {
                errorMessagePanel.setMessage(e.getMessage());
            }
        }
    }

    private Border createBorder(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(title);
        border.setTitleJustification(TitledBorder.LEFT);

        return border;
    }

    public void close() {
        this.dispose();
    }

    public void display() {
        this.setVisible(true);
    }

    public void setObserver(UpdateUserPresenter presenter) {
        this.presenter = presenter;
    }

}
