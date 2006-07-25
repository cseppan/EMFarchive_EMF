package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.NumberFieldVerifier;

import java.util.Date;

public class EditableCMSummaryTab extends ControlMeasureSummaryTab implements EditableCMTabView{

    private NumberFieldVerifier verifier;
    
    public EditableCMSummaryTab(ControlMeasure measure, EmfSession session, MessagePanel messagePanel,
            ManageChangeables changeablesList) {
        super(measure,session,messagePanel,changeablesList);
        super.setName("summary");
        this.verifier = new NumberFieldVerifier("Summary tab: ");
    }
    
    public void populateDefaultValues() {
        ruleEffectiveness.setText("100.0");
        rulePenetration.setText("100.0");
    }
    
    public void populateValues() {
        super.populateFields();
    }

    public void save(ControlMeasure measure) throws EmfException {
        validateFields();

        measure.setName(name.getText());
        measure.setDescription(description.getText());
        measure.setCreator(session.user());
        if (deviceCode.getText().length() > 0) measure.setDeviceCode(deviceId);
        if (equipmentLife.getText().length() > 0) measure.setEquipmentLife(life);
        measure.setMajorPollutant(majorPollutant.getSelectedItem() + "");
        measure.setCmClass(cmClass.getSelectedItem() + "");
        measure.setLastModifiedTime(new Date());
        measure.setAbbreviation(abbreviation.getText());
        //measure.setCostYear(year);
    }

    private void validateFields() throws EmfException {
        messagePanel.clear();

        if (name.getText().equals("")) {
            throw new EmfException("Summary tab: Name should be a non-empty string.");
        }

        //year = verifier.parseInteger(costYear);
        if (deviceCode.getText().length() > 0) deviceId = verifier.parseInteger(deviceCode);
        effectivness = verifier.parseFloat(ruleEffectiveness);
        if (minUncontrolledEmission.getText().length() > 0) minUnctrldEmiss = verifier.parseFloat(minUncontrolledEmission);
        if (maxUncontrolledEmission.getText().length() > 0) maxUnctrldEmiss = verifier.parseFloat(maxUncontrolledEmission);
        if (equipmentLife.getText().length() > 0) life = verifier.parseFloat(equipmentLife);
        penetration = verifier.parseFloat(rulePenetration);
        if ((effectivness < 0) || (effectivness > 100))
        {
            throw new EmfException("Rule Effectiveness must be between 0 and 100");
        }
        if ((penetration < 0) || (penetration > 100))
        {
            throw new EmfException("Rule Penetration must be between 0 and 100");
        }
        if (abbreviation.getText().length() < 1)
        {
            throw new EmfException("An abbreviation must be specified");            
        }
    }
    
}
