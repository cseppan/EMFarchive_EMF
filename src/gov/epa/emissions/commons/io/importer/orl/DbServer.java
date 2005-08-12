package gov.epa.emissions.commons.io.importer.orl;

import java.sql.SQLException;

public interface DbServer {

    Datasource getAnalysisDatasource();

    Datasource getEmissionsDatasource();

    Datasource getReferenceDatasource();

    SqlTypeMapper getTypeMapper();

    /**
     * @return wraps a db-specific function around ascii column to convert it to
     *         a number w/ specified precision
     */
    String asciiToNumber(String asciiColumn, int precision);

    void createAnalysisDatasource() throws SQLException;

    void createEmissionsDatasource() throws SQLException;

    void createReferenceDatasource() throws SQLException;
}
