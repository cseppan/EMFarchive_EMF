package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.data.SourceGroup;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.data.Pollutants;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.data.ControlTechnology;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.NumberFieldVerifier;

import java.util.Date;

public class EditableCMSummaryTab extends ControlMeasureSummaryTab implements EditableCMTabView {

    private NumberFieldVerifier verifier;

    public EditableCMSummaryTab(ControlMeasure measure, EmfSession session, MessagePanel messagePanel,
            ManageChangeables changeablesList) {
        super(measure, session, messagePanel, changeablesList);
        super.setName("summary");
        this.verifier = new NumberFieldVerifier("Summary tab: ");
    }

    public void populateValues() {
        super.populateFields();
    }

    public void save(ControlMeasure measure) throws EmfException {
        validateFields();
        measure.setName(name.getText());
        measure.setDescription(description.getText());
        measure.setCreator(session.user());
        if (deviceCode.getText().length() > 0)
            measure.setDeviceCode(deviceId);
        if (equipmentLife.getText().length() > 0)
            measure.setEquipmentLife(life);
        updatePollutant();
        measure.setControlTechnology((ControlTechnology) controlTechnology.getSelectedItem());
        measure.setSourceGroup((SourceGroup) sourceGroup.getSelectedItem());
        updateDateReviewed(measure);
        measure.setCmClass(selectedClass(cmClass.getSelectedItem()));
        measure.setLastModifiedTime(new Date());
        measure.setAbbreviation(abbreviation.getText());
        // measure.setCostYear(year);
    }

    private void updateDateReviewed(ControlMeasure measure) throws EmfException {
        try {
            String date = dateReviewed.getText().trim();
            if (date.length() == 0){
                measure.setDateReviewed(null);
                return;
            }
            measure.setDateReviewed(dateReviewedFormat.parse(date));
        } catch (Exception e) {
            throw new EmfException("Please Correct the Date Format(MM/dd/yyyy) in Date Reviewed");
        }
    }

    private String selectedClass(Object selectedItem) {
        return selectedItem == null ? "" : selectedItem + "";
    }

    private void updatePollutant() {
        Object selected = majorPollutant.getSelectedItem();
        if (selected instanceof String) {
            String pollutantName = (String) selected;
            if (pollutantName.length() > 0) {
                Pollutant pollutant = pollutant(pollutantName);// checking for duplicates
                measure.setMajorPollutant(pollutant);
            }
        } else if (selected instanceof Pollutant) {
            measure.setMajorPollutant((Pollutant) selected);
        }
    }

    private Pollutant pollutant(String name) {
        return new Pollutants(allPollutants).get(name);
    }

    private void validateFields() throws EmfException {
        messagePanel.clear();

        if (name.getText().equals("")) {
            throw new EmfException("Summary tab: Name should be a non-empty string.");
        }

        // year = verifier.parseInteger(costYear);
        if (deviceCode.getText().length() > 0)
            deviceId = verifier.parseInteger(deviceCode);
        if (equipmentLife.getText().length() > 0)
            life = verifier.parseFloat(equipmentLife);
        if (abbreviation.getText().length() < 1) {
            throw new EmfException("An abbreviation must be specified");
        }
    }

}
