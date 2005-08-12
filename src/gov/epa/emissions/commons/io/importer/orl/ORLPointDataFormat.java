package gov.epa.emissions.commons.io.importer.orl;

import gov.epa.emissions.commons.db.SqlTypeMapper;
import gov.epa.emissions.commons.io.ColumnType;
import gov.epa.emissions.commons.io.importer.FileColumnsMetadata;

/**
 * ORL Mobile Source Toxics Inventory (list-format)
 */
public final class ORLPointDataFormat extends ORLDataFormat {
    // FIPS -> FIPS
    public static final String PLANT_ID_CODE_NAME = "PLANTID";

    private static final int PLANT_ID_CODE_WIDTH = 15;

    private static final ColumnType PLANT_ID_CODE_TYPE = ColumnType.CHAR;

    public static final String POINT_ID_CODE_NAME = "POINTID";// recommended

    private static final int POINT_ID_CODE_WIDTH = 15;

    private static final ColumnType POINT_ID_CODE_TYPE = ColumnType.CHAR;

    public static final String STACK_ID_CODE_NAME = "STACKID";// recommended

    private static final int STACK_ID_CODE_WIDTH = 15;

    private static final ColumnType STACK_ID_CODE_TYPE = ColumnType.CHAR;

    public static final String DOE_PLANT_ID_NAME = "SEGMENT";// recommended

    private static final int DOE_PLANT_ID_WIDTH = 15;

    private static final ColumnType DOE_PLANT_ID_TYPE = ColumnType.CHAR;

    public static final String PLANT_NAME_NAME = "PLANT";// recommended

    private static final int PLANT_NAME_WIDTH = 40;

    private static final ColumnType PLANT_NAME_TYPE = ColumnType.CHAR;

    public static final String SOURCE_CLASSIFICATION_CODE_NAME = "SCC";

    private static final int SOURCE_CLASSIFICATION_CODE_WIDTH = /**/10;// */8;

    private static final ColumnType SOURCE_CLASSIFICATION_CODE_TYPE = ColumnType.CHAR;

    public static final String EMISSIONS_RELEASE_POINT_TYPE_NAME = "ERPTYPE";// not
                                                                                // used
                                                                                // by
                                                                                // SMOKE;
                                                                                // 01 =
                                                                                // fugitive;
                                                                                // 02 =
                                                                                // vertical
                                                                                // stack;
                                                                                // 03 =
                                                                                // horiozontal
                                                                                // stack;
                                                                                // 04 =
                                                                                // goose
                                                                                // neck;
                                                                                // 05 =
                                                                                // vertical
                                                                                // with
                                                                                // rain
                                                                                // cap;
                                                                                // 06 =
                                                                                // downward-facing
                                                                                // vent

    private static final int EMISSIONS_RELEASE_POINT_TYPE_WIDTH = 2;

    private static final ColumnType EMISSIONS_RELEASE_POINT_TYPE_TYPE = ColumnType.CHAR;

    private static final String SOURCE_TYPE_NAME = "SRCTYPE";// 01 = major;
                                                                // 02 = Section
                                                                // 12 area
                                                                // source

    private static final int SOURCE_TYPE_WIDTH = 2;

    private static final ColumnType SOURCE_TYPE_TYPE = ColumnType.CHAR;

    public static final String STACK_HEIGHT_NAME = "STKHGT";

    private static final int STACK_HEIGHT_WIDTH = NO_WIDTH;

    private static final ColumnType STACK_HEIGHT_TYPE = ColumnType.REAL;

    public static final String STACK_DIAMETER_NAME = "STKDIAM";

    private static final int STACK_DIAMETER_WIDTH = NO_WIDTH;

    private static final ColumnType STACK_DIAMETER_TYPE = ColumnType.REAL;

    public static final String STACK_GAS_EXIT_TEMPERATURE_NAME = "STKTEMP";

    private static final int STACK_GAS_EXIT_TEMPERATURE_WIDTH = NO_WIDTH;

    private static final ColumnType STACK_GAS_EXIT_TEMPERATURE_TYPE = ColumnType.REAL;

    public static final String STACK_GAS_FLOW_RATE_NAME = "STKFLOW";// optional;
                                                                    // automatically
                                                                    // calculated
                                                                    // by
                                                                    // Smkinven
                                                                    // from
                                                                    // velocity
                                                                    // and
                                                                    // diameter
                                                                    // if not
                                                                    // given

    private static final int STACK_GAS_FLOW_RATE_WIDTH = NO_WIDTH;

    private static final ColumnType STACK_GAS_FLOW_RATE_TYPE = ColumnType.REAL;

    public static final String STACK_GAS_EXIT_VELOCITY_NAME = "STKVEL";

    private static final int STACK_GAS_EXIT_VELOCITY_WIDTH = NO_WIDTH;

    private static final ColumnType STACK_GAS_EXIT_VELOCITY_TYPE = ColumnType.REAL;

    private static final String STANDARD_INDUSTRIAL_CODE_NAME = "SIC";// recommended

    private static final int STANDARD_INDUSTRIAL_CODE_WIDTH = NO_WIDTH;

    private static final ColumnType STANDARD_INDUSTRIAL_CODE_TYPE = ColumnType.INT;

    private static final String MACT_NAME = "MACT";// optional

    private static final int MACT_WIDTH = 6;

    private static final ColumnType MACT_TYPE = ColumnType.CHAR;

    private static final String NAICS_NAME = "NAICS";// optional

    private static final int NAICS_WIDTH = 6;

    private static final ColumnType NAICS_TYPE = ColumnType.CHAR;

    public static final String COORDINATE_SYSTEM_TYPE_NAME = "CTYPE";// U =
                                                                        // Universal
                                                                        // Transverse
                                                                        // Mercator,
                                                                        // L =
                                                                        // Latitude/Longitude

    private static final int COORDINATE_SYSTEM_TYPE_WIDTH = 1;

    private static final ColumnType COORDINATE_SYSTEM_TYPE_TYPE = ColumnType.CHAR;

    public static final String X_LOCATION_NAME = "XLOC";// if CTYPE = U, Easting
                                                        // value (meters); if
                                                        // CTYPE = L, Longitude
                                                        // (decimal degrees)

    private static final int X_LOCATION_WIDTH = NO_WIDTH;

    private static final ColumnType X_LOCATION_TYPE = ColumnType.REAL;

    public static final String Y_LOCATION_NAME = "YLOC";// if CTYPE = U,
                                                        // Northing value
                                                        // (meters); if CTYPE =
                                                        // L, Latitude (decimal
                                                        // degrees)

    private static final int Y_LOCATION_WIDTH = NO_WIDTH;

    private static final ColumnType Y_LOCATION_TYPE = ColumnType.REAL;

    public static final String UTM_ZONE_NAME = "UMTZ";// required if CTYPE = U

    private static final int UTM_ZONE_WIDTH = NO_WIDTH;

    private static final ColumnType UTM_ZONE_TYPE = ColumnType.INT;

    // CAS -> CAS
    // ANNUAL_EMISSIONS -> ANN_EMIS
    // AVERAGE_DAY_EMISSIONS -> AVD_EMIS
    private static final String CONTROL_EFFICIENCY_NAME = "CEFF";// recommended:
                                                                    // SMOKE
                                                                    // default
                                                                    // value ==
                                                                    // 0

    private static final int CONTROL_EFFICIENCY_WIDTH = NO_WIDTH;

    private static final ColumnType CONTROL_EFFICIENCY_TYPE = ColumnType.REAL;

    private static final String RULE_EFFECTIVENESS_NAME = "REFF";// recommended:
                                                                    // SMOKE
                                                                    // default
                                                                    // value ==
                                                                    // 100

    private static final int RULE_EFFECTIVENESS_WIDTH = NO_WIDTH;

    private static final ColumnType RULE_EFFECTIVENESS_TYPE = ColumnType.REAL;

    private static final String PRIMARY_CONTROL_EQUIPMENT_CODE_NAME = "CPRI";// not
                                                                                // used
                                                                                // by
                                                                                // SMOKE

    private static final int PRIMARY_CONTROL_EQUIPMENT_CODE_WIDTH = NO_WIDTH;

    private static final ColumnType PRIMARY_CONTROL_EQUIPMENT_CODE_TYPE = ColumnType.INT;

    private static final String SECONDARY_CONTROL_EQUIPMENT_CODE_NAME = "CSEC";// not
                                                                                // used
                                                                                // by
                                                                                // SMOKE

    private static final int SECONDARY_CONTROL_EQUIPMENT_CODE_WIDTH = NO_WIDTH;

    private static final ColumnType SECONDARY_CONTROL_EQUIPMENT_CODE_TYPE = ColumnType.INT;

    public ORLPointDataFormat(SqlTypeMapper sqlTypeMapper, boolean extendedFormat) {
        super(sqlTypeMapper, extendedFormat);
    }

    public FileColumnsMetadata getFileImportDetails() {
        FileColumnsMetadata details = new FileColumnsMetadata("", super.sqlTypeMapper);

        // once per line
        addDetail(details, ORLDataFormat.FIPS_NAME, ORLDataFormat.FIPS_TYPE, ORLDataFormat.FIPS_WIDTH);
        addDetail(details, PLANT_ID_CODE_NAME, PLANT_ID_CODE_TYPE, PLANT_ID_CODE_WIDTH);
        addDetail(details, POINT_ID_CODE_NAME, POINT_ID_CODE_TYPE, POINT_ID_CODE_WIDTH);
        addDetail(details, STACK_ID_CODE_NAME, STACK_ID_CODE_TYPE, STACK_ID_CODE_WIDTH);
        addDetail(details, DOE_PLANT_ID_NAME, DOE_PLANT_ID_TYPE, DOE_PLANT_ID_WIDTH);
        addDetail(details, PLANT_NAME_NAME, PLANT_NAME_TYPE, PLANT_NAME_WIDTH);
        addDetail(details, SOURCE_CLASSIFICATION_CODE_NAME, SOURCE_CLASSIFICATION_CODE_TYPE,
                SOURCE_CLASSIFICATION_CODE_WIDTH);
        addDetail(details, EMISSIONS_RELEASE_POINT_TYPE_NAME, EMISSIONS_RELEASE_POINT_TYPE_TYPE,
                EMISSIONS_RELEASE_POINT_TYPE_WIDTH);
        addDetail(details, SOURCE_TYPE_NAME, SOURCE_TYPE_TYPE, SOURCE_TYPE_WIDTH);
        addDetail(details, STACK_HEIGHT_NAME, STACK_HEIGHT_TYPE, STACK_HEIGHT_WIDTH);
        addDetail(details, STACK_DIAMETER_NAME, STACK_DIAMETER_TYPE, STACK_DIAMETER_WIDTH);
        addDetail(details, STACK_GAS_EXIT_TEMPERATURE_NAME, STACK_GAS_EXIT_TEMPERATURE_TYPE,
                STACK_GAS_EXIT_TEMPERATURE_WIDTH);
        addDetail(details, STACK_GAS_FLOW_RATE_NAME, STACK_GAS_FLOW_RATE_TYPE, STACK_GAS_FLOW_RATE_WIDTH);
        addDetail(details, STACK_GAS_EXIT_VELOCITY_NAME, STACK_GAS_EXIT_VELOCITY_TYPE, STACK_GAS_EXIT_VELOCITY_WIDTH);
        addDetail(details, STANDARD_INDUSTRIAL_CODE_NAME, STANDARD_INDUSTRIAL_CODE_TYPE, STANDARD_INDUSTRIAL_CODE_WIDTH);
        addDetail(details, MACT_NAME, MACT_TYPE, MACT_WIDTH);
        addDetail(details, NAICS_NAME, NAICS_TYPE, NAICS_WIDTH);
        addDetail(details, COORDINATE_SYSTEM_TYPE_NAME, COORDINATE_SYSTEM_TYPE_TYPE, COORDINATE_SYSTEM_TYPE_WIDTH);
        addDetail(details, X_LOCATION_NAME, X_LOCATION_TYPE, X_LOCATION_WIDTH);
        addDetail(details, Y_LOCATION_NAME, Y_LOCATION_TYPE, Y_LOCATION_WIDTH);
        addDetail(details, UTM_ZONE_NAME, UTM_ZONE_TYPE, UTM_ZONE_WIDTH);
        addDetail(details, CAS_NAME, CAS_TYPE, CAS_WIDTH);
        addDetail(details, ANNUAL_EMISSIONS_NAME, ANNUAL_EMISSIONS_TYPE, ANNUAL_EMISSIONS_WIDTH);
        addDetail(details, AVERAGE_DAY_EMISSIONS_NAME, AVERAGE_DAY_EMISSIONS_TYPE, AVERAGE_DAY_EMISSIONS_WIDTH);
        addDetail(details, CONTROL_EFFICIENCY_NAME, CONTROL_EFFICIENCY_TYPE, CONTROL_EFFICIENCY_WIDTH);
        addDetail(details, RULE_EFFECTIVENESS_NAME, RULE_EFFECTIVENESS_TYPE, RULE_EFFECTIVENESS_WIDTH);
        addDetail(details, PRIMARY_CONTROL_EQUIPMENT_CODE_NAME, PRIMARY_CONTROL_EQUIPMENT_CODE_TYPE,
                PRIMARY_CONTROL_EQUIPMENT_CODE_WIDTH);
        addDetail(details, SECONDARY_CONTROL_EQUIPMENT_CODE_NAME, SECONDARY_CONTROL_EQUIPMENT_CODE_TYPE,
                SECONDARY_CONTROL_EQUIPMENT_CODE_WIDTH);

        return details;
    }
}
