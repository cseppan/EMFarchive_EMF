package gov.epa.emissions.commons.io.importer.orl;

import gov.epa.emissions.commons.db.SqlTypeMapper;
import gov.epa.emissions.commons.io.ColumnType;
import gov.epa.emissions.commons.io.importer.DataFormat;

public abstract class ORLDataFormat extends DataFormat {
    public static final int NO_WIDTH = -1;

    public static final String CAS_NAME = "CAS";

    public static final int CAS_WIDTH = 10;

    public static final ColumnType CAS_TYPE = ColumnType.CHAR;

    public static final String ANNUAL_EMISSIONS_NAME = "ANN_EMIS";

    public static final int ANNUAL_EMISSIONS_WIDTH = NO_WIDTH;

    public static final ColumnType ANNUAL_EMISSIONS_TYPE = ColumnType.REAL;

    public static final String AVERAGE_DAY_EMISSIONS_NAME = "AVD_EMIS";// optional

    public static final int AVERAGE_DAY_EMISSIONS_WIDTH = NO_WIDTH;

    public static final ColumnType AVERAGE_DAY_EMISSIONS_TYPE = ColumnType.REAL;

    protected boolean extendedFormat;

    ORLDataFormat(SqlTypeMapper sqlTypeMapper, boolean extendedFormat) {
        super(sqlTypeMapper);
        this.extendedFormat = extendedFormat;
    }
}
