package gov.epa.emissions.framework.services.cost.data;

import java.io.Serializable;
import java.util.Date;

public class EfficiencyRecord implements Serializable {

    private int id;

    private int existingDevCode;
    
    private String pollutant;

    private String existingMeasureAbbr;
    
    private String locale;
    
    private String equationType;

    private String detail;
    
    private float percentReduction;

    private int costYear;
    
    private float costPerTon;
    
    private float ruleEffectiveness;
    
    private float rulePenetration;
    
    private float capRecFactor;
    
    private float discountRate;

    private float efficiency;
    
    private Date effectiveDate;

    public float getCapRecFactor() {
        return capRecFactor;
    }

    public void setCapRecFactor(float capRecFactor) {
        this.capRecFactor = capRecFactor;
    }

    public float getCostPerTon() {
        return costPerTon;
    }

    public void setCostPerTon(float costPerTon) {
        this.costPerTon = costPerTon;
    }

    public int getCostYear() {
        return costYear;
    }

    public void setCostYear(int costYear) {
        this.costYear = costYear;
    }

    public float getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(float discountRate) {
        this.discountRate = discountRate;
    }

    public float getEfficiency() {
        return efficiency;
    }

    public void setEfficiency(float efficiency) {
        this.efficiency = efficiency;
    }

    public String getEquationType() {
        return equationType;
    }

    public void setEquationType(String equationType) {
        this.equationType = equationType;
    }

    public int getExistingDevCode() {
        return existingDevCode;
    }

    public void setExistingDevCode(int existingDevCode) {
        this.existingDevCode = existingDevCode;
    }

    public String getExistingMeasureAbbr() {
        return existingMeasureAbbr;
    }

    public void setExistingMeasureAbbr(String existingMeasureAbbr) {
        this.existingMeasureAbbr = existingMeasureAbbr;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public float getPercentReduction() {
        return percentReduction;
    }

    public void setPercentReduction(float percentReduction) {
        this.percentReduction = percentReduction;
    }

    public String getPollutant() {
        return pollutant;
    }

    public void setPollutant(String pollutant) {
        this.pollutant = pollutant;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public float getRuleEffectiveness() {
        return ruleEffectiveness;
    }

    public void setRuleEffectiveness(float ruleEffectiveness) {
        this.ruleEffectiveness = ruleEffectiveness;
    }

    public float getRulePenetration() {
        return rulePenetration;
    }

    public void setRulePenetration(float rulePenetration) {
        this.rulePenetration = rulePenetration;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

}
