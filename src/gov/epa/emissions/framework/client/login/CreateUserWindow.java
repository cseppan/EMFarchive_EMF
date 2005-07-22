package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.framework.client.admin.CreateUserPresenter;
import gov.epa.emissions.framework.client.admin.CreateUserView;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;

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

    public CreateUserWindow() {
        JPanel layoutPanel = createLayout();

        this.setSize(new Dimension(350, 350));
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
        container.add(new JButton("Cancel"));
        container.add(new JButton("Ok"));
        
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
        valuesPanel.add(new JTextField(10));
        valuesPanel.add(new JPasswordField(10));
        valuesPanel.add(new JPasswordField(10));

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
        valuesPanel.add(new JTextField(10));
        valuesPanel.add(new JTextField(10));
        valuesPanel.add(new JTextField(10));
        valuesPanel.add(new JTextField(10));

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
        return null;
    }

    public String getPassword() {
        return null;
    }

    public String getConfirmPassword() {
        return null;
    }

    public String getEmail() {
        return null;
    }

    public String getPhone() {
        return null;
    }

    public String getAffiliation() {
        return null;
    }

    public void close() {
    }

    public void setObserver(CreateUserPresenter presenter) {
    }

}
