package gov.epa.emissions.framework.ui;

import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.UIManager;

public class EmfFileChooser extends JComponent {

    private EmfFileInfo current;

    private String approveButtonText = "Select";

    private String title = "EMF File Chooser";

    private int returnValue = ERROR_OPTION;

    private int dialogType = OPEN_DIALOG;

    private JDialog dialog = null;
    
    private EmfFileChooserPanel chooserPanel;

    public static final int OPEN_DIALOG = 0;

    public static final int SAVE_DIALOG = 1;

    public static final int CUSTOM_DIALOG = 2;

    public static final int CANCEL_OPTION = 1;

    public static final int APPROVE_OPTION = 0;

    public static final int ERROR_OPTION = -1;

    public static final int FILES_ONLY = 0;

    public static final int DIRECTORIES_ONLY = 1;

    public static final int FILES_AND_DIRECTORIES = 2;

    public static final String CANCEL_SELECTION = "CancelSelection";

    public static final String APPROVE_SELECTION = "ApproveSelection";

    public EmfFileChooser(EmfFileInfo dir, EmfFileSystemView fsv) {
        this.current = dir;
        this.chooserPanel = new EmfFileChooserPanel(fsv, current, true);
    }

    public EmfFileChooser(EmfFileSystemView fsv) {
        this(fsv.getDefaultDir(), fsv);
    }

    public EmfFileInfo getSelectedDir() {
        return this.chooserPanel.selectedDirectory();
    }

    public EmfFileInfo[] getSelectedFiles() {
        return this.chooserPanel.selectedFiles();
    }

    public void setApproveButtonText(String approveButtonText) {
        if (this.approveButtonText == approveButtonText) {
            return;
        }
        String oldValue = this.approveButtonText;
        this.approveButtonText = approveButtonText;
        firePropertyChange("ApproveButtonTextChangedProperty", oldValue, approveButtonText);
    }

    public int showDialog(Component parent, String approveButtonText) {
        if (approveButtonText != null) {
            setApproveButtonText(approveButtonText);
            setDialogType(CUSTOM_DIALOG);
        }

        dialog = createDialog(parent);
        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                returnValue = CANCEL_OPTION;
            }
        });
        returnValue = ERROR_OPTION;

        dialog.setVisible(true);
        firePropertyChange("EmfFileChooserDialogIsClosingProperty", dialog, null);
        
        return returnValue;
    }

    protected JDialog createDialog(Component parent) throws HeadlessException {
        JDialog dialog = new JDialog((Frame) parent, title, true);
        dialog.setComponentOrientation(this.getComponentOrientation());

        Container contentPane = dialog.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(this.chooserPanel, BorderLayout.CENTER);
        contentPane.add(buttonsPanel(), BorderLayout.SOUTH);
        
        if (JDialog.isDefaultLookAndFeelDecorated()) {
            boolean supportsWindowDecorations = UIManager.getLookAndFeel().getSupportsWindowDecorations();
            if (supportsWindowDecorations) {
                dialog.getRootPane().setWindowDecorationStyle(JRootPane.FILE_CHOOSER_DIALOG);
            }
        }
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        
        return dialog;
    }

    private void setDialogType(int dialogType) {
        if (this.dialogType == dialogType) {
            return;
        }
        if (!(dialogType == OPEN_DIALOG || dialogType == SAVE_DIALOG || dialogType == CUSTOM_DIALOG)) {
            throw new IllegalArgumentException("Incorrect Dialog Type: " + dialogType);
        }
        int oldValue = this.dialogType;
        this.dialogType = dialogType;
        if (dialogType == OPEN_DIALOG || dialogType == SAVE_DIALOG) {
            setApproveButtonText(null);
        }
        firePropertyChange("DialogTypeChangedProperty", oldValue, dialogType);
    }

    public void setTitle(String newTitle) {
        if (this.title.equals(newTitle))
            return;

        String oldValue = this.title;
        dialog.setTitle(newTitle);
        firePropertyChange("DialogTitleChangedProperty", oldValue, newTitle);
    }
    
    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();
        panel.add(new OKButton(selectAction()));
        panel.add(new CancelButton(cancelAction()));

        JPanel container = new JPanel(new BorderLayout(0, 20));
        container.add(panel, BorderLayout.LINE_END);

        return container;
    }

    private Action cancelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                closeDialog();
                returnValue = CANCEL_OPTION;
            }
        };
    }

    private Action selectAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                closeDialog();
                returnValue = APPROVE_OPTION;
            }
        };
    }
    
    private void closeDialog() {
        dialog.setVisible(false);
        dialog.dispose();
        dialog = null;
    }

}
