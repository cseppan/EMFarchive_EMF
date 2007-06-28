package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.framework.services.EmfException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface RecordGenerator {

    Record getRecord(ResultSet resultSet, BestMeasureEffRecord maxCM, double originalEmissions, boolean displayOriginalEmissions, boolean displayFinalEmissions) throws SQLException, EmfException;
    
    double reducedEmission();
    
    void calculateEmissionReduction(ResultSet resultSet, BestMeasureEffRecord maxMeasure) throws SQLException;
    
    List tokens(ResultSet resultSet, BestMeasureEffRecord maxCM, double originalEmissions, boolean displayOriginalEmissions, boolean displayFinalEmissions) throws SQLException, EmfException;
}