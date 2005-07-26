package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.admin.CreateUserPresenter;
import gov.epa.emissions.framework.client.admin.CreateUserView;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public class CreateUserWindow extends JFrame implements CreateUserView {

    private CreateUserPresenter presenter;

    private JTextField username;

    private JPasswordField password;

    private JPasswordField confirmPassword;

    private JTextField name;

    private JTextField affiliation;

    private JTextField phone;

    private JTextField email;

    public CreateUserWindow() {
        JPanel layoutPanel = createLayout();

        this.setSize(new Dimension(350, 375));
        this.setLocation(new Point(400, 200));
        this.setTitle("Create a New User");

        this.getContentPane().add(layoutPanel);
    }

    private JPanel createLayout() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(createProfilePanel());
        panel.add(createLoginPanel());
        panel.add(createButtonsPanel());

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
                if (CreateUserWindow.this.presenter != null) {
                    CreateUserWindow.this.presenter.notifyCancel();
                }
            }
        });
        container.add(cancel);

        JButton ok = new JButton("Ok");
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (CreateUserWindow.this.presenter != null) {
                    try {
                        CreateUserWindow.this.presenter.notifyCreate();
                    } catch (EmfException e) {// TODO: attach a error handler
                        e.printStackTrace();
                    }
                    //TODO: launch the main window ??
                }
            }
        });
        container.add(ok);

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

        name = new JTextField(10);
        valuesPanel.add(name);
        affiliation = new JTextField(10);
        valuesPanel.add(affiliation);
        phone = new JTextField(10);
        valuesPanel.add(phone);
        email = new JTextField(10);
        valuesPanel.add(email);

        panel.add(valuesPanel);

        panel.setMaximumSize(new Dimension(300, 175));

        return panel;
    }

    private Border createBorder(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(title);
        border.setTitleJustification(TitledBorder.LEFT);

        return border;
    }

    public String getUsername() {
        return username.getText();
    }

    public String getPassword() {
        return password.getText();
    }

    public String getConfirmPassword() {
        return confirmPassword.getText();
    }

    public String getEmail() {
        return email.getText();
    }

    public String getPhone() {
        return phone.getText();
    }

    public String getAffiliation() {
        return affiliation.getText();
    }

    public String getFullName() {
        return name.getText();
    }

    public void close() {
        this.dispose();
    }

    public void setObserver(CreateUserPresenter presenter) {
        this.presenter = presenter;
    }

}
