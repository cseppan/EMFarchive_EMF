package gov.epa.emissions.framework.client.fast;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.BrowseButton;
import gov.epa.emissions.commons.gui.buttons.ExportButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.fast.FastOutputExportWrapper;
import gov.epa.emissions.framework.ui.EmfFileChooser;
import gov.epa.emissions.framework.ui.ImageResources;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

public class ExportToNetCDFWindow extends ExportWindow {

    private List<FastOutputExportWrapper> outputs;

    private SingleLineMessagePanel messagePanel;

    private JTextField folderTextField;

    private ExportPresenter presenter;

    // private JCheckBox overwriteCheckbox;

    private JButton exportButton;

    private DataCommonsService service;

    private EmfConsole parentConsole;

    public ExportToNetCDFWindow(List<FastOutputExportWrapper> outputs, DesktopManager desktopManager, EmfConsole parentConsole,
            EmfSession session) {
        super(outputs, desktopManager, parentConsole,
                session);
    }

    protected void doExport() {
        try {
            validateFolder(folderTextField.getText());

            // if (!overwriteCheckbox.isSelected()) {
            // presenter.doExport(this.outputs, folderTextField.getText());
            // } else {
            presenter.doExportWithOverwrite(this.outputs, folderTextField.getText());
            // }

            messagePanel.setMessage("Started export. Please monitor the Status window "
                    + "to track your Export request.");

            exportButton.setEnabled(false);
        } catch (EmfException e) {
            e.printStackTrace();
            messagePanel.setError(e.getMessage());
        }
    }
}
