package gov.epa.emissions.framework.install.installer;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

public class InstallWindow extends JFrame implements InstallView  {
	
    private InstallPresenter presenter;
    
	JPanel cards; //a panel that uses CardLayout
	final static String PAGEONE = "TRIM Directories";
	final static String PAGETWO = "Projects";
	final static String PAGETHREE = "APEX Directories";
	final static String PAGEFOUR = "Updated Files";
	
	JFrame installFrame;
	JPanel page1, page2;
	JPanel installPanel;
	JPanel installPanelNorth, installPanelSouth, installPanelEast, installPanelWest;
	JPanel nextPanel, statusPanel;
	JTextField url, javaHomeDirField, inputDirField, outputDirField, 
        installDirField, serverField;
	JLabel urlLabel, javaHomeLabel, inputLabel, outputLabel, installHomeLabel;
	JLabel holderLabel1, holderLabel2, holderLabel3, holderLabel4, holderLabel5;
	JLabel sqlUserLabel, sqlPassLabel;
	JLabel statusLabel, load, serverLabel;
	JButton installButton, exitInstallButton, javaHomeBrowser, inputDirBrowser, outputDirBrowser;
	JButton installDirBrowser;
	JButton holderButton1, holderButton2, cancel;
	GridBagLayout gridbag;
	GridBagConstraints c;
	JFileChooser chooser1, chooser2, chooser3, chooser4;
	
	static boolean installFinished = false;
	HashMap names = new HashMap();
	HashMap hm = new HashMap();

	public InstallWindow() {
		installFrame = this;
		
		//Create GridBagLayout.
		gridbag = new GridBagLayout();
		c = new GridBagConstraints();

		//Create widgets.		
		url = new JTextField(30);
		javaHomeDirField = new JTextField(Constants.JAVA_HOME);
		inputDirField = new JTextField(30);
		outputDirField = new JTextField(30);
        installDirField = new JTextField(30);
        serverField = new JTextField(30);
		urlLabel = new JLabel("EMF Download URL", SwingConstants.RIGHT);
		javaHomeLabel = new JLabel("Java Home Directory", SwingConstants.RIGHT);
		inputLabel = new JLabel("Input File Directory", SwingConstants.RIGHT);
		outputLabel = new JLabel("Output File Directory", SwingConstants.RIGHT);
        installHomeLabel = new JLabel("Client Home Directory", SwingConstants.RIGHT);
        serverLabel = new JLabel("Server Address", SwingConstants.RIGHT);
		statusLabel = new JLabel();
		holderLabel1 = new JLabel();
		holderLabel2 = new JLabel();
		holderLabel3 = new JLabel();
        load = new JLabel(Constants.EMF_MESSAGE);
        
		javaHomeBrowser = new JButton("Browse...");
		inputDirBrowser = new JButton("Browse...");
		outputDirBrowser = new JButton("Browse...");
        installDirBrowser = new JButton("Browse...");
		installButton = new JButton("Install");
		exitInstallButton = new JButton(" Exit  ");
        cancel = new JButton("Cancel");
		
		//Create and set up the panel.
		page1 = new JPanel();
		page1.setLayout((new BorderLayout()));
        page2 = new JPanel(); 
        page2.setLayout((new BorderLayout()));
        
		statusPanel = new JPanel();
		statusPanel.setLayout(new GridLayout(1, 1));			
		
		installButton.addActionListener(new InstallButtonListener());
		exitInstallButton.addActionListener(new ExitInstallButtonListener());
        cancel.addActionListener(new CancelButtonListener());
		javaHomeBrowser.addActionListener(new Browse1Listener());
		inputDirBrowser.addActionListener(new Browse2Listener());
		outputDirBrowser.addActionListener(new Browse3Listener());
        installDirBrowser.addActionListener(new InstallDirBrowserListener());
				
        // first look in the user home directory for the preferences file
		File preference = new File(System.getProperty("user.home"), Constants.INSTALLER_PREFERENCES_FILE);
		if(preference.exists()){
            getUserPreferences();
		}
        		
		//Set the default button.
		getRootPane().setDefaultButton(installButton);
		
		page1 = createFirstPage();
		page2 = createSecondPage();
		
		//Create the panel that contains the "cards".
		cards = new JPanel(new CardLayout());
		cards.add(page1, PAGEONE);
		cards.add(page2, PAGETWO);
		
		getContentPane().add(cards,BorderLayout.CENTER);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(new Dimension(600, 800));
		setTitle("EMF Client Installer -- " + Constants.VERSION);
		pack();
	}
    
    private void getUserPreferences() {
        InstallPreferences up;
        try {
            up = new InstallPreferences();
            url.setText(up.emfWebSite());
            inputDirField.setText(up.inputFolder());
            outputDirField.setText(up.outputFolder());
            installDirField.setText(up.emfInstallFolder().replace('/', '\\'));
            serverField.setText(up.emfServer());
        } catch (Exception e) {
            presenter.displayErr("Cann't get user preferences.");
        }
    }
    
	private void setLookAndFeel() {
		String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
		
		try {
			UIManager.setLookAndFeel(lookAndFeel);
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        setLocationRelativeTo(null);
        setResizable(false);
	}
    
    public void display() {
        setLookAndFeel();
        setVisible(true);
    }
    
    public void observe(InstallPresenter presenter) {
        this.presenter = presenter;
    }

    public void close() {
        super.dispose();
    }

	public void showMsg(String msg1, String msg2){
		JOptionPane.showMessageDialog (this, msg1, msg2, JOptionPane.WARNING_MESSAGE);
	}
	
	private void browse(String name, JTextField text){
		JFileChooser chooser;
		File file = new File(text.getText());
		
		if(file.isDirectory()){
			chooser = new JFileChooser(file);
		}else{
			chooser = new JFileChooser("C:\\");
		}
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle("Please select the " + name);
		
		int option = chooser.showDialog(this, "Select");
		if(option == JFileChooser.APPROVE_OPTION){
			text.setText("" + chooser.getSelectedFile());
		}
	}

	private JPanel createFirstPage(){
		JPanel installPanel = new JPanel(gridbag);
		JPanel installPanelNorth = new JPanel();
		JPanel installPanelEast = new JPanel();
		JPanel installPanelWest = new JPanel();
		Box installPanelSouth = new Box(BoxLayout.X_AXIS); 

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.insets = new Insets(1,1,7,5);
		gridbag.setConstraints(urlLabel, c);
		gridbag.setConstraints(url,c);
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		gridbag.setConstraints(holderLabel1,c);
		installPanel.add(urlLabel);
		installPanel.add(url);
		installPanel.add(holderLabel1);
		
		c.gridwidth = 1; //next-to-last in row
		c.insets = new Insets(7,1,7,5);
		gridbag.setConstraints(javaHomeLabel,c);
		gridbag.setConstraints(javaHomeDirField, c);
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		gridbag.setConstraints(javaHomeBrowser,c);
		installPanel.add(javaHomeLabel);
		installPanel.add(javaHomeDirField);
		installPanel.add(javaHomeBrowser);
		
		c.gridwidth = 1; //next-to-last in row
		gridbag.setConstraints(inputLabel,c);
		gridbag.setConstraints(inputDirField,c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(inputDirBrowser,c);
		installPanel.add(inputLabel);
		installPanel.add(inputDirField);
		installPanel.add(inputDirBrowser);
        
        c.gridwidth = 1; //next-to-last in row
        gridbag.setConstraints(outputLabel,c);
        gridbag.setConstraints(outputDirField,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(outputDirBrowser,c);
        installPanel.add(outputLabel);
        installPanel.add(outputDirField);
        installPanel.add(outputDirBrowser);
        
        c.gridwidth = 1; //next-to-last in row
        gridbag.setConstraints(installHomeLabel,c);
        gridbag.setConstraints(installDirField,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(installDirBrowser,c);
        installPanel.add(installHomeLabel);
        installPanel.add(installDirField);
        installPanel.add(installDirBrowser);
        
        c.gridwidth = 1; //next-to-last in row
        gridbag.setConstraints(serverLabel,c);
        gridbag.setConstraints(serverField,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        installPanel.add(serverLabel);
        installPanel.add(serverField);

        installPanelSouth.setBorder(BorderFactory.createEmptyBorder(5, 10, 20, 80));
		installPanelSouth.add(Box.createHorizontalGlue());
		installPanelSouth.add(installButton);
		installPanelSouth.add(Box.createRigidArea(new Dimension(20, 0)));
		installPanelSouth.add(exitInstallButton);
		
		//Assemble the panels	
		page1.add(installPanel, BorderLayout.CENTER);
		page1.add(installPanelNorth, BorderLayout.NORTH);
		page1.add(installPanelSouth, BorderLayout.SOUTH);
		page1.add(installPanelEast, BorderLayout.EAST);
		page1.add(installPanelWest, BorderLayout.WEST);
		
		return page1;
	}

	private JPanel createSecondPage(){
        JPanel upper = new JPanel();
		load.setFont(new Font("default", Font.BOLD, 12));
        upper.add(load);
        
        Box buttons = new Box(BoxLayout.X_AXIS); 
        buttons.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 80));
        buttons.add(Box.createHorizontalGlue());
        buttons.add(cancel);
        buttons.setLocation(0, 100);
        
        statusPanel.setBorder(BorderFactory.createEmptyBorder(1, 10, 2, 10));
        statusPanel.add(statusLabel);
        
        Box southPanel = new Box(BoxLayout.Y_AXIS);
        southPanel.add(buttons);
        southPanel.add(statusPanel);
        
        page2.add(upper, BorderLayout.CENTER);
        page2.add(southPanel, BorderLayout.SOUTH);
        
		return page2;
	}

	private class ExitInstallButtonListener implements ActionListener {	
        public void actionPerformed(ActionEvent e) {	
            System.exit(0);
		}
	}
	
	private class InstallButtonListener implements ActionListener {	
        public void actionPerformed(ActionEvent e) {	
			String javahome = javaHomeDirField.getText();
			String outputdir = outputDirField.getText();
			String inputdir = inputDirField.getText();
            String installhome = installDirField.getText();
            String website = url.getText();
            String server = serverField.getText();
	        
            int option = checkDirs();
            if(option == JOptionPane.OK_OPTION) {
                CardLayout cl = (CardLayout)(cards.getLayout());
                cl.show(cards, PAGETWO);
                presenter.writePreference(website, inputdir, outputdir, javahome, installhome, server);
                presenter.startDownload(website, Constants.FILE_LIST, installhome);
            }
		}
	}
	
	private class Browse1Listener implements ActionListener
	{	public void actionPerformed(ActionEvent e)
		{				
			browse("Java Home Directory", javaHomeDirField);
		}
	}
	
	private class Browse2Listener implements ActionListener {
        public void actionPerformed(ActionEvent e) {				
			browse("Remote Input Drive", inputDirField);
		}
	}
	
	private class Browse3Listener implements ActionListener {
        public void actionPerformed(ActionEvent e) {				
			browse("Remote Output Drive", outputDirField);
		}
	}
    
     private class InstallDirBrowserListener implements ActionListener {
         public void actionPerformed(ActionEvent e) {               
            browse("Install Directory", installDirField);
        }
     }
     
     private class CancelButtonListener implements ActionListener {
         public void actionPerformed(ActionEvent e) {
             if(e.getActionCommand().equalsIgnoreCase("Cancel")) {
                 presenter.stopDownload();
                 CardLayout cl = (CardLayout)(cards.getLayout());
                 cl.show(cards, PAGEONE);
             }
             
             if(e.getActionCommand().equalsIgnoreCase("Done")) {
                 System.exit(0);
             }
        }
     }

    public void setStatus(String status) {
        statusLabel.setText(status);
    }

    public void displayErr(String err) {
        presenter.stopDownload();
        JOptionPane.showMessageDialog (this, err, "EMF client installation Error", 
                JOptionPane.WARNING_MESSAGE);
    }

    public void setFinish() {
        String javahome = javaHomeDirField.getText();
        String installhome = installDirField.getText();
        String server = serverField.getText();
        presenter.createBatchFile(installhome + "\\" + Constants.EMF_BATCH_FILE,
                Constants.EMF_CLIENT_PREFERENCES_FILE, javahome, server);
        presenter.createShortcut();
        load.setText(Constants.EMF_CLOSE_MESSAGE);
        cancel.setText("Done");
    }
    
    public int checkDirs() {
        String[] names = {javaHomeDirField.getText(), inputDirField.getText(),
                outputDirField.getText(), installDirField.getText()};
        String[] labels = {"Java Home Directory", "Input File Directory",
                "Output File Directory", "Client Home Directory"};
        String message = "";
        for(int i = 0; i < names.length; i++) {
            if(!(new File(names[i]).exists())) {
                if(message.equals(""))
                    message += labels[i];
                else 
                    message += ", " + labels[i];
            }
        }
            
        if(!message.equals("")) {
            message += " does not exist." + Constants.SEPARATOR +
                "Do you want to proceed with installing the EMF client?";
            return JOptionPane.showConfirmDialog(this, message);
        }
            
        return JOptionPane.OK_OPTION;
    }
}
