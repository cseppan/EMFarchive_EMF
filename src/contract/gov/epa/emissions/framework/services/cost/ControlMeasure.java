package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.data.Mutex;
import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.data.SourceGroup;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.cost.data.AggregatedPollutantEfficiencyRecord;
import gov.epa.emissions.framework.services.cost.data.ControlTechnology;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ControlMeasure implements Lockable, Serializable {

    private int id;

    private String name;

    private String description;

    private int deviceCode;

    private int costYear;

    private float equipmentLife;

    private Pollutant majorPollutant;

    private User creator;

    private float annualizedCost;

    private ControlMeasureClass cmClass;

    private String abbreviation;

    private ControlTechnology controlTechnology;

    private SourceGroup sourceGroup;

    private String dataSouce;

    private Date dateReviewed;

    private Date lastModifiedTime;

    private Mutex lock;

    private List sccs;

    private List efficiencyRecords;
    private List aggregatedPollutantEfficiencyRecords;

    private List sectors;
    private String lastModifiedBy;

    private Double ruleEffectiveness;

    private Double rulePenetration;

    private Double applyOrder;

    private List equationTypes;

//    private EquationType equationType;

    public ControlMeasure() {
        this.lock = new Mutex();
        this.sccs = new ArrayList();
        this.efficiencyRecords = new ArrayList();
        this.aggregatedPollutantEfficiencyRecords = new ArrayList();
        this.sectors = new ArrayList();
        this.equationTypes = new ArrayList();
        ControlMeasureEquationType controlMeasureEquationType = new ControlMeasureEquationType(new EquationType("Type 5"));
        ControlMeasureEquationTypeVariable[] controlMeasureEquationTypeVariables = new ControlMeasureEquationTypeVariable[] 
                { new ControlMeasureEquationTypeVariable(new EquationTypeVariable("Scaling Factor"), 2.5),
                    new ControlMeasureEquationTypeVariable(new EquationTypeVariable("Scaling Factor2"), 5.5)};
        controlMeasureEquationType.setControlMeasureEquationTypeVariables(controlMeasureEquationTypeVariables);
        this.equationTypes.add(controlMeasureEquationType);
        controlMeasureEquationType = new ControlMeasureEquationType(new EquationType("Type 4"));
        controlMeasureEquationTypeVariables = new ControlMeasureEquationTypeVariable[] 
                { new ControlMeasureEquationTypeVariable(new EquationTypeVariable("Scaling Factor"), 2.5),
                    new ControlMeasureEquationTypeVariable(new EquationTypeVariable("Scaling Factor2"), 5.5)};
        controlMeasureEquationType.setControlMeasureEquationTypeVariables(controlMeasureEquationTypeVariables);
        this.equationTypes.add(controlMeasureEquationType);
    }

    public ControlMeasure(String name) {
        this();
        this.name = name;
    }

    public float getAnnualizedCost() {
        return annualizedCost;
    }

    public void setAnnualizedCost(float annualizedCost) {
        this.annualizedCost = annualizedCost;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(int deviceCode) {
        this.deviceCode = deviceCode;
    }

    public float getEquipmentLife() {
        return equipmentLife;
    }

    public void setEquipmentLife(float equipmentLife) {
        this.equipmentLife = equipmentLife;
    }

    public Pollutant getMajorPollutant() {
        return majorPollutant;
    }

    public void setMajorPollutant(Pollutant majorPollutant) {
        this.majorPollutant = majorPollutant;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getLockDate() {
        return lock.getLockDate();
    }

    public void setLockDate(Date lockDate) {
        lock.setLockDate(lockDate);
    }

    public String getLockOwner() {
        return lock.getLockOwner();
    }

    public void setLockOwner(String owner) {
        lock.setLockOwner(owner);
    }

    public boolean isLocked(String owner) {
        return lock.isLocked(owner);
    }

    public boolean isLocked(User user) {
        return lock.isLocked(user);
    }

    public boolean isLocked() {
        return lock.isLocked();
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ControlMeasure)) {
            return false;
        }

        ControlMeasure other = (ControlMeasure) obj;

        return (id == other.getId() || name.equals(other.getName()));
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public ControlMeasureClass getCmClass() {
        return cmClass;
    }

    public void setCmClass(ControlMeasureClass cmClass) {
        this.cmClass = cmClass;
    }

    public Date getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(Date lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public int getCostYear() {
        return costYear;
    }

    public void setCostYear(int costYear) {
        this.costYear = costYear;
    }

    public EfficiencyRecord[] getEfficiencyRecords() {
        return (EfficiencyRecord[]) efficiencyRecords.toArray(new EfficiencyRecord[0]);
    }

    public void setEfficiencyRecords(EfficiencyRecord[] efficiencyRecords) {
        this.efficiencyRecords = Arrays.asList(efficiencyRecords);
    }

    public ControlTechnology getControlTechnology() {
        return controlTechnology;
    }

    public void setControlTechnology(ControlTechnology controlTechnology) {
        this.controlTechnology = controlTechnology;
    }

    public String getDataSouce() {
        return dataSouce;
    }

    public void setDataSouce(String dataSouce) {
        this.dataSouce = dataSouce;
    }

    public Date getDateReviewed() {
        return dateReviewed;
    }

    public void setDateReviewed(Date dateReviewed) {
        this.dateReviewed = dateReviewed;
    }

    public SourceGroup getSourceGroup() {
        return sourceGroup;
    }

    public void setSourceGroup(SourceGroup sourceGroup) {
        this.sourceGroup = sourceGroup;
    }

    public Scc[] getSccs() {
        return (Scc[]) sccs.toArray(new Scc[0]);
    }

    public void setSccs(Scc[] sccs) {
        this.sccs = Arrays.asList(sccs);
    }

    public Sector[] getSectors() {
        return (Sector[]) sectors.toArray(new Sector[0]);
    }

    public void setSectors(Sector[] sectors) {
        this.sectors = Arrays.asList(sectors);
    }

    public ControlMeasureEquationType[] getEquationTypes() {
        return (ControlMeasureEquationType[]) equationTypes.toArray(new ControlMeasureEquationType[0]);
    }

    public void setEquationTypes(ControlMeasureEquationType[] equationTypes) {
        this.equationTypes = Arrays.asList(equationTypes);
    }
    
    public void addEfficiencyRecord(EfficiencyRecord efficiencyRecord) {
        this.efficiencyRecords.add(efficiencyRecord);
    }

    public void addScc(Scc scc) {
        this.sccs.add(scc);
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public AggregatedPollutantEfficiencyRecord[] getAggregatedPollutantEfficiencyRecords() {
        return (AggregatedPollutantEfficiencyRecord[]) aggregatedPollutantEfficiencyRecords.toArray(new AggregatedPollutantEfficiencyRecord[0]);
    }

    public void setAggregatedPollutantEfficiencyRecords(AggregatedPollutantEfficiencyRecord[] aggregatedPollutantEfficiencyRecords) {
        this.aggregatedPollutantEfficiencyRecords = Arrays.asList(aggregatedPollutantEfficiencyRecords);
    }

    public void addAggregatedPollutantEfficiencyRecord(AggregatedPollutantEfficiencyRecord aggregatedPollutantEfficiencyRecord) {
        aggregatedPollutantEfficiencyRecords.add(aggregatedPollutantEfficiencyRecord);
    }

    public void addSector(Sector sector) {
        sectors.add(sector);
    }

    //these properties will overide the efficiency record settings...
    public void setRuleEffectiveness(Double ruleEffectiveness) {
        this.ruleEffectiveness = ruleEffectiveness;
    }

    public Double getRuleEffectiveness() {
        return ruleEffectiveness;
    }

    public void setRulePenetration(Double rulePenetration) {
        this.rulePenetration = rulePenetration;
    }

    public Double getRulePenetration() {
        return rulePenetration;
    }

    public void setApplyOrder(Double applyOrder) {
        this.applyOrder = applyOrder;
    }

    public Double getApplyOrder() {
        return applyOrder;
    }
    
//    public void setEquationType(EquationType equationType) {
//        this.equationType = equationType;
//    }
//
//    public EquationType getEquationType() {
//        return equationType;
//    }
}