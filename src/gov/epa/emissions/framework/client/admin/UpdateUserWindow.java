package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfInteralFrame;
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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public class UpdateUserWindow extends EmfInteralFrame implements EmfWidgetContainer, UpdateUserView {

    private UpdateUserPresenter presenter;

    private MessagePanel messagePanel;

    private User user;

    private JTextField affiliation;

    private JTextField phone;

    private JTextField email;

    private JTextField name;

    private JPasswordField password;

    private JPasswordField confirmPassword;

    private String windowTitle;

    // FIXME: refactor the duplication b/w register & update user widgets
    public UpdateUserWindow(User user) {
        super("Update User - " + user.getUserName());
        this.windowTitle = "Update User - " + user.getUserName();

        this.user = user;

        super.setSize(new Dimension(375, 425));
        super.getContentPane().add(createLayout());
        super.setResizable(false);
    }

    private JPanel createLayout() {
        JPanel base = new JPanel();
        base.setLayout(new BoxLayout(base, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        base.add(messagePanel);
        base.add(createProfilePanel());
        base.add(createLoginPanel());
        base.add(createButtonsPanel());

        return base;
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel();

        Border titledBorder = createBorder("Profile");
        panel.setBorder(titledBorder);

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

        name = createEditAwareTextField(user.getFullName());
        valuesPanel.add(name);
        valuesPanel.add(Box.createRigidArea(new Dimension(1, 10)));
        
        affiliation = createEditAwareTextField(user.getAffiliation());
        valuesPanel.add(affiliation);
        valuesPanel.add(Box.createRigidArea(new Dimension(1, 10)));
        
        phone = createEditAwareTextField(user.getWorkPhone());
        valuesPanel.add(phone);
        valuesPanel.add(Box.createRigidArea(new Dimension(1, 10)));
        
        email = createEditAwareTextField(user.getEmailAddr());
        valuesPanel.add(email);

        panel.add(valuesPanel);

        panel.setMaximumSize(new Dimension(300, 175));

        return panel;
    }

    private JTextField createEditAwareTextField(String value) {
        JTextField field = new JTextField(value, 15);

        field.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent event) {
                markAsEdited();
            }
        });

        return field;
    }

    private JPasswordField createEditAwarePasswordField() {
        JPasswordField field = new JPasswordField(10);

        field.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent event) {
                markAsEdited();
            }
        });

        return field;
    }

    private void markAsEdited() {
        this.setTitle(windowTitle + " *");
        if (presenter != null)
            presenter.notifyChanges();
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

        JLabel username = new JLabel(user.getUserName(), 10);
        valuesPanel.add(username);
        password = createEditAwarePasswordField();
        valuesPanel.add(password);
        confirmPassword = createEditAwarePasswordField();
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

        JButton close = new JButton("Close");
        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (presenter != null) {
                    presenter.notifyClose();
                }
            }
        });

        JButton save = new JButton("Save");
        save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                updateUser();
            }
        });

        container.add(save);
        container.add(close);

        panel.add(container, BorderLayout.EAST);

        return panel;
    }

    private void updateUser() {
        if (presenter == null)
            return;
        try {
            user.setFullName(name.getText());
            user.setAffiliation(affiliation.getText());
            user.setWorkPhone(phone.getText());
            user.setEmailAddr(email.getText());
            if (password.getPassword().length > 0) {
                user.setPassword(new String(password.getPassword()));
                user.confirmPassword(new String(confirmPassword.getPassword()));
            }

            presenter.notifySave(user);
            close();
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
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

    public void closeOnConfirmLosingChanges() {
        int option = JOptionPane.showConfirmDialog(null,
                "You will lose changes if you Close without Save. \nContinue ?", "Close", JOptionPane.YES_NO_OPTION);
        if (option == 0)
            close();
    }

}
