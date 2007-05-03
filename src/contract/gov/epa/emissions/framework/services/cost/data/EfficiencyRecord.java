package gov.epa.emissions.framework.services.cost.data;

import gov.epa.emissions.commons.data.Pollutant;

import java.io.Serializable;
import java.util.Date;

public class EfficiencyRecord implements Serializable {

    private int existingDevCode;

    private Pollutant pollutant;

    private String existingMeasureAbbr;

    private String locale;

    private String equationType;

    private String detail;

    private int costYear;

    private Double costPerTon;

    private float ruleEffectiveness;

    private float rulePenetration;

    private float capRecFactor;

    private float discountRate;

    private float efficiency;

    private Date effectiveDate;

    private int recordId;

    private int id;

    private int controlMeasureId;

    private String lastModifiedBy;
    private Date lastModifiedTime;

    private Double refYrCostPerTon;

    public float getCapRecFactor() {
        return capRecFactor;
    }

    public void setCapRecFactor(float capRecFactor) {
        this.capRecFactor = capRecFactor;
    }

    public Double getCostPerTon() {
        return costPerTon;
    }

    public void setCostPerTon(Double costPerTon) {
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

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public Pollutant getPollutant() {
        return pollutant;
    }

    public void setPollutant(Pollutant pollutant) {
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

    public boolean equals(Object other) {
        if (other == null || !(other instanceof EfficiencyRecord))
            return false;

        final EfficiencyRecord record = (EfficiencyRecord) other;
        boolean equal = record.getPollutant().equals(pollutant) && record.getLocale().equals(locale)
                && compareEffectiveDate(record.getEffectiveDate(), effectiveDate)
                && record.getExistingMeasureAbbr().equals(existingMeasureAbbr);

        return equal;
    }

    private boolean compareEffectiveDate(Date date1, Date date2) {
        if (date1 == null && date2 == null)
            return true;
        // if either one is null =>not equal
        if (date1 == null || date2 == null)
            return false;

        return date1.equals(date2);
    }

    public void setRecordId(int noOfEfficiencyRecords) {
        this.recordId = noOfEfficiencyRecords;
    }

    public int getRecordId() {
        return recordId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public void setControlMeasureId(int controlMeasureId) {
        this.controlMeasureId = controlMeasureId;
    }

    public int getControlMeasureId() {
        return controlMeasureId;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedTime(Date lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public Date getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setRefYrCostPerTon(Double refYrCostPerTon) {
        this.refYrCostPerTon = refYrCostPerTon;
    }

    public Double getRefYrCostPerTon() {
        return refYrCostPerTon;
    }

}
