package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.framework.services.EmfException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface RecordGenerator {

    Record getRecord(ResultSet resultSet, MaxEmsRedControlMeasure maxCM) throws SQLException, EmfException;
    
    double reducedEmission();
    
    void calculateEmissionReduction(ResultSet resultSet, MaxEmsRedControlMeasure maxMeasure) throws SQLException;
    
    List tokens(ResultSet resultSet, MaxEmsRedControlMeasure maxCM) throws SQLException, EmfException;
}
