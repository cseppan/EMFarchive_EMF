package gov.epa.emissions.framework.client.transport;

import javax.xml.namespace.QName;

import gov.epa.emissions.framework.services.casemanagement.Abbreviation;
import gov.epa.emissions.framework.services.casemanagement.AirQualityModel;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseCategory;
import gov.epa.emissions.framework.services.casemanagement.EmissionsYear;
import gov.epa.emissions.framework.services.casemanagement.Grid;
import gov.epa.emissions.framework.services.casemanagement.GridResolution;
import gov.epa.emissions.framework.services.casemanagement.MeteorlogicalYear;
import gov.epa.emissions.framework.services.casemanagement.Speciation;

import org.apache.axis.client.Call;

public class CaseMappings extends Mappings {

    public void register(Call call) {
        beans(call);
        arrays(call);
    }

    private void beans(Call call) {
        bean(call, Case.class, caseObject());
        bean(call, Abbreviation.class, abbreviation());
        bean(call, AirQualityModel.class, airQualityModel());
        bean(call, CaseCategory.class, caseCategory());
        bean(call, EmissionsYear.class, emissionsYear());
        bean(call, Grid.class, grid());
        bean(call, GridResolution.class, gridResolution());
        bean(call, MeteorlogicalYear.class, meteorlogicalYear());
        bean(call, Speciation.class, speciation());
    }

    private void arrays(Call call) {
        array(call, Case[].class, cases());
        array(call, Abbreviation[].class, abbreviations());
        array(call, AirQualityModel[].class, airQualityModels());
        array(call, CaseCategory[].class, caseCategories());
        array(call, EmissionsYear[].class, emissionsYears());
        array(call, Grid[].class, grids());
        array(call, GridResolution[].class, gridResolutions());
        array(call, MeteorlogicalYear[].class, meteorlogicalYears());
        array(call, Speciation[].class, speciations());
    }

    public QName caseObject() {
        return qname("Case");
    }

    public QName cases() {
        return qname("Cases");
    }

    public QName abbreviation() {
        return qname("Abbreviation");
    }

    public QName abbreviations() {
        return qname("Abbreviations");
    }

    public QName airQualityModel() {
        return qname("AirQualityModel");
    }

    public QName airQualityModels() {
        return qname("AirQualityModels");
    }

    public QName caseCategory() {
        return qname("CaseCategory");
    }

    public QName caseCategories() {
        return qname("CaseCategories");
    }

    public QName emissionsYear() {
        return qname("EmissionsYear");
    }

    public QName emissionsYears() {
        return qname("EmissionsYears");
    }

    public QName grid() {
        return qname("Grid");
    }

    public QName grids() {
        return qname("Grids");
    }

    public QName gridResolution() {
        return qname("GridResolution");
    }
    
    public QName gridResolutions() {
        return qname("GridResolutions");
    }
    
    public QName meteorlogicalYear() {
        return qname("MeteorlogicalYear");
    }

    public QName meteorlogicalYears() {
        return qname("MeteorlogicalYears");
    }

    public QName speciation() {
        return qname("Speciation");
    }

    public QName speciations() {
        return qname("Speciations");
    }

}
