package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.EmfImageTool;
import gov.epa.emissions.framework.services.cost.EquationType;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class EquationTypeSelectionDialog extends JDialog implements EquationTypeSelectionView {

    private EmfConsole parent;

    private ComboBox equationTypeCombo;
    
    private EquationType equationType;

    public EquationTypeSelectionDialog(EmfConsole parent, ManageChangeables changeables) {
        super(parent);
        super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
        this.parent = parent;
        setModal(true);
    }

    public void display(EquationType[] equationTypes) {

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(5, 5));
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(buildEquationTypeCombo(equationTypes), BorderLayout.NORTH);
        panel.add(buttonPanel(), BorderLayout.SOUTH);
        contentPane.add(panel);

        setTitle("Select Equation Type");
        this.pack();
        this.setSize(500, 200);
        this.setLocation(ScreenUtils.getPointToCenter(parent));
        this.setVisible(true);
    }

    private JPanel buildEquationTypeCombo(EquationType[] equationTypes) {
        JPanel panel = new JPanel(new BorderLayout());
        equationTypeCombo = new ComboBox("Choose an equation type", equationTypes);

        panel.add(equationTypeCombo, BorderLayout.LINE_START);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        return panel;
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
                //set Equation Type to null, they clicked cancel
                equationType = null;
                setVisible(false);
                dispose();
            }

        };
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //set Equation Type...
                equationType = (EquationType)equationTypeCombo.getSelectedItem();
                setVisible(false);
                dispose();
            }
        };
    }

    public EquationType getEquationType() {
        return equationType;
    }

    public void observe(EquationTypeSelectionPresenter presenter) {
        //
    }
}