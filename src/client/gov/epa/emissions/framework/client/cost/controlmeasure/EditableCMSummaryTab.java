package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.ui.MessagePanel;

public class EditableCMSummaryTab extends ControlMeasureSummaryTab implements EditableCMSummaryTabView{

    public EditableCMSummaryTab(ControlMeasure measure, EmfSession session, MessagePanel messagePanel,
            ManageChangeables changeablesList) {
        super(measure,session,messagePanel,changeablesList);
        super.setName("summary");
        
    }

/*
    private void disableFields() {
        name.setEditable(false);
        description.setEditable(false);
        majorPollutant.setEnabled(false);
        costYear.setEditable(false);
        anualizedCost.setEditable(false);
        deviceCode.setEditable(false);
        equipmentLife.setEditable(false);
        ruleEffectiveness.setEditable(false);
        rulePenetration.setEditable(false);
        region.setEnabled(false);
        cmClass.setEnabled(false);
        sectors.setEnabled(false);
        controlPrograms.setEnabled(false);
    }
*/
    public void save(ControlMeasure measure) throws EmfException {
        validateFields();

        measure.setName(name.getText());
        measure.setDescription(description.getText());
        measure.setAnnualizedCost(cost);
        measure.setCreator(session.user());
        measure.setDeviceCode(deviceId);
        measure.setEquipmentLife(life);
        measure.setMajorPollutant(majorPollutant.getSelectedItem() + "");
        measure.setRuleEffectiveness(eff);
        measure.setRulePenetration(penetr);
    }

    private void validateFields() throws EmfException {
        messagePanel.clear();

        if (name.getText().equals("")) {
            throw new EmfException("Name field should be a non-empty string.");
        }

        parseInteger(costYear);
        deviceId = parseInteger(deviceCode);
        eff = parseFloat(ruleEffectiveness);
        cost = parseFloat(anualizedCost);
        life = parseFloat(equipmentLife);
        penetr = parseFloat(rulePenetration);
    }
    
    private int parseInteger(TextField numberField) throws EmfException {
        int val = 0;
        try {
            Integer.parseInt(numberField.getText());
        } catch (NumberFormatException ex) {
            throw new EmfException(numberField.getName() + " field should be an integer number.");
        }

        return val;
    }
    
    private float parseFloat(TextField numberField) throws EmfException {
        float val = 0;
        try {
            val = Float.parseFloat(numberField.getText());
        } catch (NumberFormatException ex) {
            throw new EmfException(numberField.getName() + " field should be a floating point number.");
        }

        return val;
    }

}
