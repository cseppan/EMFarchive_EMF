package gov.epa.emissions.commons.io.importer.orl;

import gov.epa.emissions.commons.db.SqlTypeMapper;
import gov.epa.emissions.commons.io.ColumnType;
import gov.epa.emissions.commons.io.importer.FileColumnsMetadata;

public abstract class ORLDataFormat {
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

    protected SqlTypeMapper sqlTypeMapper;

    public static final String FIPS_NAME = "FIPS";

    protected static final ColumnType FIPS_TYPE = ColumnType.INT;

    protected static final int FIPS_WIDTH = 5;

    ORLDataFormat(SqlTypeMapper sqlTypeMapper, boolean extendedFormat) {
        this.sqlTypeMapper = sqlTypeMapper;
        this.extendedFormat = extendedFormat;
    }

    /** TODO should this be moved to FileColumnsMetadata ? */
    public final void addDetail(FileColumnsMetadata details, String name, ColumnType type, int width) {
        details.addColumnName(name);
        try {
            details.setType(name, type.getName());
            details.setWidth(name, String.valueOf(width));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract FileColumnsMetadata getFileImportDetails();
}
