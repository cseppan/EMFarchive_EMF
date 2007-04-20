package gov.epa.emissions.framework.services.cost.controlmeasure;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDateFormat;

import java.util.Date;

//FIXME: merge this validation with EfficiencyRecordWindow
public class EfficiencyRecordValidation {

    public int existingDevCode(String code) throws EmfException {
        code = code.trim();
        if (code.length() == 0)
            return 0;
        return parseInteger("existing device code", code);
    }

    public Date effectiveDate(String date) throws EmfException {
        date = date.trim();
        if (date.length() == 0) {
            return null;
        }
        try {
            return EmfDateFormat.parse_MMddyyyy(date);
        } catch (Exception e) {
            throw new EmfException("effective date format should be MM/dd/yyyy, value =" + date);
        }
    }

    public float discountRate(String rate) throws EmfException {
        if (rate.length() == 0)
            return 0;

        float value = parseFloat("discount rate", rate);
        if (value < 0 || value > 20)
            throw new EmfException("discount rate should be a percent between 0 and 20 - e.g., 1 = 1%, 0.01 = 0.01%");
        return value;
    }

    public float capRecFactor(String factor) throws EmfException {
        if (factor.length() == 0)
            return 0;

        return parseFloat("cap. recovery factor", factor);
    }

    public float rulePenetration(String penetration) throws EmfException {
        float value = parseFloat("rule penetration", penetration);
        if (value <= 0 || value > 100)
            throw new EmfException("rule penetration should be a percent between 0 and 100 - e.g., 1 = 1%, 0.01 = 0.01%, value = "
                    + penetration);
        return value;
    }

    public float ruleEffectiveness(String effectiveness) throws EmfException {
        float value = parseFloat("rule effectiveness", effectiveness);
        if (value <= 0 || value > 100)
            throw new EmfException("rule effectiveness should be a percent between 0 and 100 - e.g., 1 = 1%, 0.01 = 0.01%, value ="
                    + effectiveness);
        return value;
    }

    public String locale(String locale) throws EmfException {
        locale = locale.trim();
        if (locale.length() == 0) {
            return "";
        }
        parseInteger("locale", locale);
        // put this in to handle when Excel truncates leading zeros
        if (locale.length() == 1)
            return "0"+locale;
        
        if (locale.length() == 2 || locale.length() == 5 || locale.length() == 6)
            return locale;

        throw new EmfException("locale must be a two, five, or six digit integer, value = " + locale);
    }

    public float costPerTon(String costperTon) throws EmfException {
        if (costperTon.trim().length() == 0) return Float.MIN_VALUE;
        float value = parseFloat("cost per ton", costperTon);
        // This is not actually the case per Greg Stella - some controls have cost benefits
        //if (value < 0)
        //    throw new EmfException("cost per ton should be >= 0, value = " + costperTon);
        return value;
    }

    public int costYear(String costYear) throws EmfException {
        YearValidation validation = new YearValidation("Cost Year");
        return validation.value(costYear);
    }

    public float efficiency(String efficiency) throws EmfException {
        float value = parseFloat("control efficiency", efficiency);
        if (value <= 0)
            throw new EmfException("control efficiency should be a percentage (e.g., 90%, or -10% for a disbenefit), value = "
                    + efficiency);
        return value;
    }

    public int parseInteger(String msgPrefix, String value) throws EmfException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new EmfException(msgPrefix + " should be an integer, value = " + value);
        }
    }

    public float parseFloat(String msgPrefix, String value) throws EmfException {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            throw new EmfException(msgPrefix + " should be a float, value = " + value);
        }
    }

    public double parseDouble(String msgPrefix, String value) throws EmfException {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new EmfException(msgPrefix + " should be a double, value = " + value);
        }
    }

}
