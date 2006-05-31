package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.data.Region;
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
        ruleEffectiveness.setText("1.0");
        rulePenetration.setText("1.0");
    }
    
    public void populateValues() {
        super.populateFields();
    }

    public void save(ControlMeasure measure) throws EmfException {
        validateFields();

        measure.setName(name.getText());
        measure.setDescription(description.getText());
        measure.setMinUncontrolledEmissions(minUnctrldEmiss);
        measure.setMaxUncontrolledEmissions(maxUnctrldEmiss);
        measure.setCreator(session.user());
        measure.setDeviceCode(deviceId);
        measure.setEquipmentLife(life);
        measure.setMajorPollutant(majorPollutant.getSelectedItem() + "");
        measure.setRuleEffectiveness((float)(effectivness/100.0));
        measure.setRulePenetration((float)(penetratrion/100.0));
        measure.setCmClass(cmClass.getSelectedItem() + "");
        measure.setRegion((Region)region.getSelectedItem());
        measure.setLastModifiedTime(new Date());
        measure.setAbbreviation(abbreviation.getText());
        measure.setCostYear(year);
    }

    private void validateFields() throws EmfException {
        messagePanel.clear();

        if (name.getText().equals("")) {
            throw new EmfException("Summary tab: Name should be a non-empty string.");
        }

        year = verifier.parseInteger(costYear);
        deviceId = verifier.parseInteger(deviceCode);
        effectivness = verifier.parseFloat(ruleEffectiveness);
        minUnctrldEmiss = verifier.parseFloat(minUncontrolledEmission);
        maxUnctrldEmiss = verifier.parseFloat(maxUncontrolledEmission);
        life = verifier.parseFloat(equipmentLife);
        penetratrion = verifier.parseFloat(rulePenetration);
        if ((effectivness < 0) || (effectivness > 100))
        {
            throw new EmfException("Rule Effectiveness must be between 0 and 100");
        }
        if ((penetratrion < 0) || (penetratrion > 100))
        {
            throw new EmfException("Rule Penetration must be between 0 and 100");
        }
    }
    
}
