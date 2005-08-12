package gov.epa.emissions.commons.io.importer.orl;

public abstract class ORLDataFormat extends DataFormat {
    protected static final int NO_WIDTH = -1;

    protected static final String CAS_NAME = "CAS";

    protected static final int CAS_WIDTH = 10;

    protected static final ColumnType CAS_TYPE = ColumnType.CHAR;

    protected static final String ANNUAL_EMISSIONS_NAME = "ANN_EMIS";

    protected static final int ANNUAL_EMISSIONS_WIDTH = NO_WIDTH;

    protected static final ColumnType ANNUAL_EMISSIONS_TYPE = ColumnType.REAL;

    protected static final String AVERAGE_DAY_EMISSIONS_NAME = "AVD_EMIS";// optional

    protected static final int AVERAGE_DAY_EMISSIONS_WIDTH = NO_WIDTH;

    protected static final ColumnType AVERAGE_DAY_EMISSIONS_TYPE = ColumnType.REAL;

    protected boolean extendedFormat;

    ORLDataFormat(SqlTypeMapper sqlTypeMapper, boolean extendedFormat) {
        super(sqlTypeMapper);
        this.extendedFormat = extendedFormat;
    }
}
