package gov.epa.emissions.commons.io.importer.orl;

import gov.epa.emissions.commons.db.SqlTypeMapper;
import gov.epa.emissions.commons.io.ColumnType;
import gov.epa.emissions.commons.io.importer.FileColumnsMetadata;

/**
 * ORL Mobile Source Toxics Inventory (list-format)
 */
public final class ORLMobileDataFormat extends ORLDataFormat {
    // FIPS -> FIPS (extendedFormat)
    private static final String STATE_CODE_NAME = "STATE";

    private static final int STATE_CODE_WIDTH = 2;

    private static final ColumnType STATE_CODE_TYPE = ColumnType.INT;

    private static final String COUNTY_CODE_NAME = "COUNTY";

    private static final int COUNTY_CODE_WIDTH = 3;

    private static final ColumnType COUNTY_CODE_TYPE = ColumnType.INT;

    public static final String SOURCE_CLASSIFICATION_CODE_NAME = "SCC";

    private static final int SOURCE_CLASSIFICATION_CODE_WIDTH = 10;

    private static final ColumnType SOURCE_CLASSIFICATION_CODE_TYPE = ColumnType.CHAR;

    // CAS -> CAS
    // ANNUAL_EMISSIONS -> ANN_EMIS
    // AVERAGE_DAY_EMISSIONS -> AVD_EMIS

    public ORLMobileDataFormat(SqlTypeMapper sqlTypeMapper, boolean extendedFormat) {
        super(sqlTypeMapper, extendedFormat);
    }

    public FileColumnsMetadata getFileImportDetails() {
        FileColumnsMetadata details = new FileColumnsMetadata("", super.sqlTypeMapper);

        // once per line
        if (extendedFormat) {
            addDetail(details, ORLDataFormat.FIPS_NAME, ORLDataFormat.FIPS_TYPE, ORLDataFormat.FIPS_WIDTH);
        } else {
            addDetail(details, STATE_CODE_NAME, STATE_CODE_TYPE, STATE_CODE_WIDTH);
            addDetail(details, COUNTY_CODE_NAME, COUNTY_CODE_TYPE, COUNTY_CODE_WIDTH);
        }
        addDetail(details, SOURCE_CLASSIFICATION_CODE_NAME, SOURCE_CLASSIFICATION_CODE_TYPE,
                SOURCE_CLASSIFICATION_CODE_WIDTH);
        addDetail(details, CAS_NAME, CAS_TYPE, CAS_WIDTH);
        addDetail(details, ANNUAL_EMISSIONS_NAME, ANNUAL_EMISSIONS_TYPE, ANNUAL_EMISSIONS_WIDTH);
        addDetail(details, AVERAGE_DAY_EMISSIONS_NAME, AVERAGE_DAY_EMISSIONS_TYPE, AVERAGE_DAY_EMISSIONS_WIDTH);

        return details;
    }
}
