package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.framework.EmfException;


public interface InterDataServices {
    //Keywords
    EmfKeyword[] getKeywords() throws EmfException;
    
    //Countries
    Country[] getCountries() throws EmfException;
    void addCountry(Country country) throws EmfException;
    void updateCountry(Country country) throws EmfException;

    //Sectors
    Sector[] getSectors() throws EmfException;
    void addSector(Sector sector) throws EmfException;
    void updateSector(Sector sector) throws EmfException;

}
