package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.controlmeasure.EfficiencyRecordValidation;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.Map;

public class CMEfficiencyRecordReader {

    private CMEfficiencyFileFormat fileFormat;

    private Pollutants pollutants;

    private CMAddImportStatus status;

    private EfficiencyRecordValidation validation;

    public CMEfficiencyRecordReader(CMEfficiencyFileFormat fileFormat, User user, HibernateSessionFactory sessionFactory) {
        this.fileFormat = fileFormat;
        this.status = new CMAddImportStatus(user, sessionFactory);
        pollutants = new Pollutants(sessionFactory);
        validation = new EfficiencyRecordValidation();
    }

    public void parse(Map controlMeasures, Record record, int lineNo) {
        StringBuffer sb = new StringBuffer();
        String[] tokens = modify(record, sb, lineNo);

        ControlMeasure cm = controlMeasure(tokens[0], controlMeasures, sb, lineNo);
        if (tokens == null || cm == null || !checkForConstraints(tokens, sb, lineNo))
            return;

        try {
            EfficiencyRecord efficiencyRecord = new EfficiencyRecord();
            pollutant(efficiencyRecord, tokens[1], sb);
            locale(efficiencyRecord, tokens[2], sb);
            effectiveDate(efficiencyRecord, tokens[3], sb);
            existingMeasureAbbrev(efficiencyRecord, tokens[4]);
            controlEfficiency(efficiencyRecord, tokens[6], sb);
            costYear(efficiencyRecord, tokens[7], sb);
            costPerTon(efficiencyRecord, tokens[8], sb);
            ruleEffectiveness(efficiencyRecord, tokens[9], sb);
            rulePenetration(efficiencyRecord, tokens[10], sb);
            equationType(efficiencyRecord, tokens[11]);
            capitalRecoveryFactor(efficiencyRecord, tokens[12], sb);
            discountFactor(efficiencyRecord, tokens[13], sb);
            details(efficiencyRecord, tokens[14]);

            cm.addEfficiencyRecord(efficiencyRecord);
        } catch (EmfException e) {
            // don't add the efficiency record if the validation fails
        }
        status.addStatus(lineNo, sb);
    }

    private ControlMeasure controlMeasure(String token, Map controlMeasures, StringBuffer sb, int lineNo) {
        ControlMeasure cm = (ControlMeasure) controlMeasures.get(token);
        if (cm == null) {
            sb.append(format("abbreviation '" + token + "' is not in the control measure summary file"));
            status.addStatus(lineNo, sb);
        }
        return cm;
    }

    private boolean checkForConstraints(String[] tokens, StringBuffer sb, int lineNo) {
        if (tokens[0].length() == 0) {
            sb.append("pollutant should not be empty.");
            status.addStatus(lineNo, sb);
            return false;
        }

        return true;
    }

    private void pollutant(EfficiencyRecord efficiencyRecord, String name, StringBuffer sb) {
        try {
            Pollutant pollutant = pollutants.getPollutant(name);
            efficiencyRecord.setPollutant(pollutant);
        } catch (CMImporterException e) {
            sb.append(format(e.getMessage()));
        }
    }

    private void locale(EfficiencyRecord efficiencyRecord, String locale, StringBuffer sb) {
        try {
            efficiencyRecord.setLocale(validation.locale(locale));
        } catch (EmfException e) {
            sb.append(format(e.getMessage()));
        }
    }

    private void effectiveDate(EfficiencyRecord efficiencyRecord, String effectiveDate, StringBuffer sb) {
        try {
            efficiencyRecord.setEffectiveDate(validation.effectiveDate(effectiveDate));
        } catch (EmfException e) {
            sb.append(format(e.getMessage()));
        }
    }

    private void existingMeasureAbbrev(EfficiencyRecord efficiencyRecord, String existMeasureAbbrev) {
        efficiencyRecord.setExistingMeasureAbbr(existMeasureAbbrev);
    }

    private void controlEfficiency(EfficiencyRecord efficiencyRecord, String ce, StringBuffer sb) throws EmfException {
        String efficiency = (ce.indexOf('%') != -1) ? ce.split("%")[0] : ce;

        try {
            efficiencyRecord.setEfficiency(validation.efficiency(efficiency));
        } catch (EmfException e) {
            sb.append(format(e.getMessage()));
            // If control Efficiency is not valid, we want the validation process to stop
            // so let the exception go up a level
            throw e;
        }
    }

    private void costYear(EfficiencyRecord efficiencyRecord, String year, StringBuffer sb) {
        try {
            efficiencyRecord.setCostYear(validation.costYear(year));
        } catch (EmfException e) {
            sb.append(format(e.getMessage()));
        }

    }

    private void costPerTon(EfficiencyRecord efficiencyRecord, String costValue, StringBuffer sb) {
        try {
            efficiencyRecord.setCostPerTon(validation.costPerTon(costValue));
        } catch (EmfException e) {
            sb.append(format(e.getMessage()));
        }
    }

    private void ruleEffectiveness(EfficiencyRecord efficiencyRecord, String ruleEffectiveness, StringBuffer sb) {
        try {
            efficiencyRecord.setRuleEffectiveness(validation.ruleEffectiveness(ruleEffectiveness));
        } catch (EmfException e) {
            sb.append(format(e.getMessage()));
        }
    }

    private void rulePenetration(EfficiencyRecord efficiencyRecord, String rulePenetration, StringBuffer sb) {
        try {
            efficiencyRecord.setRulePenetration(validation.rulePenetration(rulePenetration));
        } catch (EmfException e) {
            sb.append(format(e.getMessage()));
        }
    }

    private void equationType(EfficiencyRecord efficiencyRecord, String eqType) {
        efficiencyRecord.setEquationType(eqType);
    }

    private void capitalRecoveryFactor(EfficiencyRecord efficiencyRecord, String factor, StringBuffer sb) {
        try {
            efficiencyRecord.setCapRecFactor(validation.capRecFactor(factor));
        } catch (EmfException e) {
            sb.append(format(e.getMessage()));
        }
    }

    private void discountFactor(EfficiencyRecord efficiencyRecord, String factor, StringBuffer sb) {
        String ds = (factor.indexOf('%') != -1) ? factor.split("%")[0] : factor;
        try {
            efficiencyRecord.setDiscountRate(validation.discountRate(ds));
        } catch (EmfException e) {
            sb.append(format(e.getMessage()));
        }
    }

    private void details(EfficiencyRecord efficiencyRecord, String details) {
        efficiencyRecord.setDetail(details);
    }

    private String[] modify(Record record, StringBuffer sb, int lineNo) {
        int sizeDiff = fileFormat.cols().length - record.getTokens().length;
        if (sizeDiff == 0)
            return record.getTokens();

        if (sizeDiff > 0) {
            for (int i = 0; i < sizeDiff; i++) {
                record.add("");
            }
            return record.getTokens();
        }

        sb.append(format("The new record has extra tokens"));
        status.addStatus(lineNo, sb);
        return null;
    }

    private String format(String text) {
        return status.format(text);
    }

}
