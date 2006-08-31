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
            throw new EmfException("effective date format(MM/dd/yyyy)-" + date);
        }
    }

    public float discountRate(String rate) throws EmfException {
        if (rate.length() == 0)
            return 0;

        float value = parseFloat("discount rate", rate);
        if (value < 0 || value > 20)
            throw new EmfException("discount rate as a percent between 0 and 20. Eg: 1 = 1%.  0.01 = 0.01%");
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
            throw new EmfException("rule penetration as a percent between 0 and 100. Eg: 1 = 1%.  0.01 = 0.01% - "
                    + penetration);
        return value;
    }

    public float ruleEffectiveness(String effectiveness) throws EmfException {
        float value = parseFloat("rule effectiveness", effectiveness);
        if (value <= 0 || value > 100)
            throw new EmfException("rule effectiveness as a percent between 0 and 100. Eg: 1 = 1%.  0.01 = 0.01% -"
                    + effectiveness);
        return value;
    }

    public String locale(String locale) throws EmfException {
        locale = locale.trim();
        if (locale.length() == 0) {
            return "";
        }
        parseInteger("locale", locale);
        if (locale.length() == 2 || locale.length() == 5 || locale.length() == 6)
            return locale;

        throw new EmfException("locale must be a two, five, or six digit integer-" + locale);
    }

    public float costPerTon(String costperTon) throws EmfException {
        float value = parseFloat("cost per ton", costperTon);
        if (value <= 0)
            throw new EmfException("cost per ton > 0 - " + costperTon);
        return value;
    }

    public int costYear(String costYear) throws EmfException {
        YearValidation validation = new YearValidation("Cost Year");
        return validation.value(costYear);
    }

    public float efficiency(String efficiency) throws EmfException {
        float value = parseFloat("control efficiency", efficiency);
        if (value <= 0)
            throw new EmfException("control efficiency as a percentage (e.g., 90%, or -10% for a disbenefit) - "
                    + efficiency);
        return value;
    }

    public int parseInteger(String msgPrefix, String value) throws EmfException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new EmfException(msgPrefix + " should be an integer-" + value);
        }
    }

    public float parseFloat(String msgPrefix, String value) throws EmfException {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            throw new EmfException(msgPrefix + " should be a float-" + value);
        }
    }

}
