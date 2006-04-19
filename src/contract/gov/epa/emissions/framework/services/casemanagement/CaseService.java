package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.framework.services.EmfException;

public interface CaseService {

    Case[] getCases() throws EmfException;

    Abbreviation[] getAbbreviations() throws EmfException;

    AirQualityModel[] getAirQualityModels() throws EmfException;
}
