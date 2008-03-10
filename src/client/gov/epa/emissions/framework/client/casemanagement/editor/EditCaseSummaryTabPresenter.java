package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.CaseObjectManager;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Abbreviation;
import gov.epa.emissions.framework.services.casemanagement.AirQualityModel;
import gov.epa.emissions.framework.services.casemanagement.CaseCategory;
import gov.epa.emissions.framework.services.casemanagement.EmissionsYear;
import gov.epa.emissions.framework.services.casemanagement.Grid;
import gov.epa.emissions.framework.services.casemanagement.GridResolution;
import gov.epa.emissions.framework.services.casemanagement.MeteorlogicalYear;
import gov.epa.emissions.framework.services.casemanagement.ModelToRun;
import gov.epa.emissions.framework.services.casemanagement.Speciation;

public class EditCaseSummaryTabPresenter {
    
    private CaseObjectManager caseObjectManager = null;
    
    public EditCaseSummaryTabPresenter(EmfSession session) {
        this.caseObjectManager = CaseObjectManager.getCaseObjectManager(session);
    }

    public Sector[] getAllSectors() throws EmfException {
        return caseObjectManager.getSectors();
    }
    
    public CaseCategory[] getCaseCategories() throws EmfException {
        return caseObjectManager.getCaseCategories();
    }
    
    public CaseCategory getCaseCategory(Object selected) throws EmfException {
        return caseObjectManager.getOrAddCaseCategory(selected);
    }
    
    public Abbreviation[] getAbbreviations() throws EmfException {
        return caseObjectManager.getAbbreviations();
    }
    
    public Abbreviation getAbbreviation(Object selected) throws EmfException {
        return caseObjectManager.getOrAddAbbreviation(selected);
    }
    
    public Project[] getProjects() throws EmfException {
        return caseObjectManager.getProjects();
    }
    
    public Project getProject(Object selected) throws EmfException {
        return caseObjectManager.getOrAddProject(selected);
    }
    
    public Region[] getRegions() throws EmfException {
        return caseObjectManager.getRegions();
    }

    public ModelToRun[] getModelToRuns() throws EmfException {
        return caseObjectManager.getModelToRuns();
    }
    
    public ModelToRun getModelToRun(Object selected) throws EmfException {
        return caseObjectManager.getOrAddModelToRun(selected);
    }
    
    public AirQualityModel[] getAirQualityModels() throws EmfException {
        return caseObjectManager.getAirQualityModels();
    }
    
    public AirQualityModel getAirQualityModel(Object selected) throws EmfException {
        return caseObjectManager.getOrAddAirQualityModel(selected);
    }
    
    public EmissionsYear[] getEmissionsYears() throws EmfException {
        return caseObjectManager.getEmissionsYears();
    }
    
    public EmissionsYear getEmissionsYear(Object selected) throws EmfException {
        return caseObjectManager.getOrAddEmissionsYear(selected);
    }
    
    public MeteorlogicalYear[] getMeteorlogicalYears() throws EmfException {
        return caseObjectManager.getMeteorlogicalYears();
    }
    
    public MeteorlogicalYear getMeteorlogicalYear(Object selected) throws EmfException {
        return caseObjectManager.getOrAddMeteorlogicalYear(selected);
    }
    
    public Speciation[] getSpeciations() throws EmfException {
        return caseObjectManager.getSpeciations();
    }
    
    public Speciation getSpeciation(Object selected) throws EmfException {
        return caseObjectManager.getOrAddSpeciation(selected);
    }
    
    public GridResolution[] getGridResolutions() throws EmfException {
        return caseObjectManager.getGridResolutions();
    }
    
    public GridResolution getGridResolutionl(Object selected) throws EmfException {
        return caseObjectManager.getOrAddGridResolution(selected);
    }
    
    public Grid[] getGrids() throws EmfException {
        return caseObjectManager.getGrids();
    }
    
    public Grid getGrid(Object selected) throws EmfException {
        return caseObjectManager.getOrAddGrid(selected);
    }
    
    

}
