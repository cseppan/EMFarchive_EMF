package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.data.EmfDateFormat;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

public class CMEfficiencyRecordReader {

    private CMEfficiencyFileFormat fileFormat;

    private Pollutants pollutants;

    private CMAddImportStatus status;

    public CMEfficiencyRecordReader(CMEfficiencyFileFormat fileFormat, User user, HibernateSessionFactory sessionFactory) {
        this.fileFormat = fileFormat;
        this.status = new CMAddImportStatus(user, sessionFactory);
        pollutants = new Pollutants(sessionFactory);

    }

    public void parse(Map controlMeasures, Record record, int lineNo) throws CMImporterException {
        String[] tokens = modify(record);
        StringBuffer sb = new StringBuffer();
        ControlMeasure cm = (ControlMeasure) controlMeasures.get(tokens[0]);
        if (cm == null) {
            sb.append(format("abbreviation '" + tokens[0] + "' is not in the control measure summary file"));
            return;
        }
        EfficiencyRecord efficiencyRecord = new EfficiencyRecord();
        pollutant(efficiencyRecord, tokens[1], lineNo);
        locale(efficiencyRecord, tokens[2], lineNo);
        effectiveDate(efficiencyRecord, tokens[3], lineNo);
        existingMeasureAbbrev(efficiencyRecord, tokens[4], lineNo);
        controlEfficiency(efficiencyRecord, tokens[6], lineNo);
        costYear(efficiencyRecord, tokens[7], lineNo);
        costPerTon(efficiencyRecord, tokens[8], lineNo);
        ruleEffectiveness(efficiencyRecord, tokens[9], lineNo);
        rulePenetration(efficiencyRecord, tokens[10], lineNo);
        equationType(efficiencyRecord, tokens[11], lineNo);
        capitalRecoveryFactor(efficiencyRecord, tokens[12], lineNo);
        discountFactor(efficiencyRecord, tokens[13], lineNo);
        details(efficiencyRecord, tokens[14], lineNo);

        cm.addEfficiencyRecord(efficiencyRecord);
    }

    private void pollutant(EfficiencyRecord efficiencyRecord, String name, int lineNo) throws CMImporterException {
        if (name.length() == 0)
            throw new CMImporterException("The Pollutant should not be empty. line no: " + lineNo);

        try {
            Pollutant pollutant = pollutants.getPollutant(name);
            efficiencyRecord.setPollutant(pollutant);
        } catch (CMImporterException e) {
            throw new CMImporterException(e.getMessage() + " line no: " + lineNo);
        }

    }

    private void locale(EfficiencyRecord efficiencyRecord, String locale, int lineNo) {
        efficiencyRecord.setLocale(locale);
    }

    private void effectiveDate(EfficiencyRecord efficiencyRecord, String effectiveDate, int lineNo)
            throws CMImporterException {
        try {
            if (effectiveDate.length() == 0)// FIXME: throw an exception?
                return;
            Date date = EmfDateFormat.parse_YYYY(effectiveDate);
            efficiencyRecord.setEffectiveDate(date);
        } catch (ParseException e) {
            throw new CMImporterException("effective date - incorrect format. line no: " + lineNo);
        }
    }

    private void existingMeasureAbbrev(EfficiencyRecord efficiencyRecord, String existMeasureAbbrev, int lineNo) {
        efficiencyRecord.setExistingMeasureAbbr(existMeasureAbbrev);
    }

    // FIXME: handle exceptions related '%'
    private void controlEfficiency(EfficiencyRecord efficiencyRecord, String controlEfficiency, int lineNo) {
        float ce = floatValue(controlEfficiency.split("%")[0]);
        efficiencyRecord.setEfficiency(ce);
    }

    private void costYear(EfficiencyRecord efficiencyRecord, String year, int lineNo) {
        int costYear = 0;
        if (year.length() != 0)
            costYear = Integer.parseInt(year);

        efficiencyRecord.setCostYear(costYear);

    }

    private void costPerTon(EfficiencyRecord efficiencyRecord, String costValue, int lineNo) {
        float costPerTon = floatValue(costValue);
        efficiencyRecord.setCostPerTon(costPerTon);
    }

    private void ruleEffectiveness(EfficiencyRecord efficiencyRecord, String ruleEffectiveness, int lineNo) {
        float re = floatValue(ruleEffectiveness);
        efficiencyRecord.setRuleEffectiveness(re);
    }

    private void rulePenetration(EfficiencyRecord efficiencyRecord, String rulePenetration, int lineNo) {
        float rp = floatValue(rulePenetration);
        efficiencyRecord.setRulePenetration(rp);
    }

    private void equationType(EfficiencyRecord efficiencyRecord, String eqType, int lineNo) {
        efficiencyRecord.setEquationType(eqType);
    }

    private void capitalRecoveryFactor(EfficiencyRecord efficiencyRecord, String factor, int lineNo) {
        float capRecFactor = floatValue(factor);
        efficiencyRecord.setCapRecFactor(capRecFactor);
    }

    private void discountFactor(EfficiencyRecord efficiencyRecord, String factor, int lineNo) {
        float discountFactor = floatValue(factor);
        efficiencyRecord.setDiscountRate(discountFactor);
    }

    private void details(EfficiencyRecord efficiencyRecord, String details, int lineNo) {
        efficiencyRecord.setDetail(details);
    }

    private float floatValue(String value) {
        if (value.length() != 0)
            return Float.parseFloat(value);
        return 0;
    }

    private String[] modify(Record record) throws CMImporterException {
        int sizeDiff = fileFormat.cols().length - record.getTokens().length;
        if (sizeDiff == 0)
            return record.getTokens();

        if (sizeDiff > 0) {
            for (int i = 0; i < sizeDiff; i++) {
                record.add("");
            }
            return record.getTokens();
        }

        throw new CMImporterException("This record has more tokens");
    }

    private String format(String text) {
        return status.format(text);
    }

}
