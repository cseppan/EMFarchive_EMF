package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class EditControlStrategyFilterTab extends JPanel implements EditControlStrategyTabView {

    private TextArea filter;

    private ManageChangeables changeablesList;

    public EditControlStrategyFilterTab(ControlStrategy controlStrategy, ManageChangeables changeablesList) {
        super.setName("csFilter");
        this.changeablesList = changeablesList;
        doLayout(controlStrategy);
    }

    private void doLayout(ControlStrategy controlStrategy) {
        String value = controlStrategy.getFilter();
        if (value == null)
            value = "";

        filter = new TextArea("filter", value, 25, 3);
        JScrollPane scrollPane = new JScrollPane(filter);
        changeablesList.addChangeable(filter);

        JLabel label = new JLabel("Inventory Filter: ");
        label.setToolTipText("Enter a filter that could be entered as a SQL where clause (e.g., ANN_EMIS>5000)");

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(label,BorderLayout.WEST);
        panel.add(scrollPane);

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(50,30,0,30));
        add(panel,BorderLayout.NORTH);
    }

    public void save(ControlStrategy controlStrategy) throws EmfException {
        String value = filter.getText().trim();
        if (value.length() > 255)
            throw new EmfException("Filter Tab: The length of the sql filter should not exceed 255 characters.");

        controlStrategy.setFilter(value);
    }

    public void refresh(ControlStrategyResult controlStrategyResult) {
        // do nothing
    }

}
