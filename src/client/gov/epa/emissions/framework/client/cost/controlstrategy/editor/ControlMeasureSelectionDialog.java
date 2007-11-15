package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.EmfImageTool;
import gov.epa.emissions.framework.services.cost.LightControlMeasure;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.TrackableSortFilterSelectModel;
import gov.epa.emissions.framework.ui.YesNoDialog;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class ControlMeasureSelectionDialog extends JDialog implements ControlMeasureSelectionView {

    private TrackableSortFilterSelectModel selectModel;

    private EmfConsole parent;

    private ControlMeasureSelectionPresenter presenter;
    
    private ManageChangeables changeables;

    public ControlMeasureSelectionDialog(EmfConsole parent, ManageChangeables changeables) {
        super(parent);
        super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
        
        this.parent = parent;
        this.changeables = changeables;
    }

    public void display(ControlMeasureTableData tableData) {
        EmfTableModel tableModel = new EmfTableModel(tableData);
        selectModel = new TrackableSortFilterSelectModel(tableModel);
        changeables.addChangeable(selectModel);
        SortFilterSelectionPanel panel = new SortFilterSelectionPanel(parent, selectModel);

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(panel, BorderLayout.CENTER);
        contentPane.add(buttonPanel(), BorderLayout.SOUTH);

        setTitle("Select Control Measures");
        this.pack();
        this.setSize(700,600);
        this.setLocation(ScreenUtils.getPointToCenter(parent));
        this.setVisible(true);
    }

    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        panel.add(new OKButton(okAction()));
        panel.add(new CancelButton(cancelAction()));
        return panel;
    }

    private Action cancelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }

        };
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                add();
               }
        };
    }

    private void add() {
        List selected = selectModel.selected();
        
        // prevent from closing window without selecting items
        if (selected.size()==0){
            String message = "Would you like to close without selecting any measures?";
            YesNoDialog dialog = new YesNoDialog(this, "No measures selected", message);
            if (dialog.confirm()){
                setVisible(false);
                dispose();
            }
            return; 
        }
        LightControlMeasure[] cms = (LightControlMeasure[]) selected.toArray(new LightControlMeasure[0]);
        presenter.doAdd(cms);
        setVisible(false);
        dispose();
    }

    public void observe(ControlMeasureSelectionPresenter presenter) {
        this.presenter = presenter;
    }

}
