package gov.epa.emissions.commons.io.importer.orl;

import gov.epa.emissions.commons.db.SqlTypeMapper;
import gov.epa.emissions.commons.io.ColumnType;
import gov.epa.emissions.commons.io.importer.FileColumnsMetadata;

/**
 * ORL Nonpoint Source Toxics Inventory (list-format)
 * 
 * @author Keith Lee, CEP UNC
 * @version $Id: ORLAreaNonpointDataFormat.java,v 1.3 2005/08/12 15:46:42 rhavaldar Exp $
 */
public final class ORLAreaNonpointDataFormat extends ORLAreaDataFormat
{
    //FIPS -> FIPS
    public static final String SOURCE_CLASSIFICATION_CODE_NAME = "SCC";
    private static final int SOURCE_CLASSIFICATION_CODE_WIDTH = 10;
    private static final ColumnType SOURCE_CLASSIFICATION_CODE_TYPE = ColumnType.CHAR;
    private static final String STANDARD_INDUSTRIAL_CODE_NAME = "SIC";//recommended
    private static final int STANDARD_INDUSTRIAL_CODE_WIDTH = NO_WIDTH;
    private static final ColumnType STANDARD_INDUSTRIAL_CODE_TYPE = ColumnType.INT;
    private static final String MACT_NAME = "MACT";//recommended
    private static final int MACT_WIDTH = 6;
    private static final ColumnType MACT_TYPE = ColumnType.CHAR;
    private static final String SOURCE_TYPE_NAME = "SRCTYPE";
    private static final int SOURCE_TYPE_WIDTH = 2;
    private static final ColumnType SOURCE_TYPE_TYPE = ColumnType.CHAR;
    private static final String NAICS_NAME = "NAICS";//optional
    private static final int NAICS_WIDTH = 6;
    private static final ColumnType NAICS_TYPE = ColumnType.CHAR;
    //CAS -> CAS
    //ANNUAL_EMISSIONS -> ANN_EMIS
    //AVERAGE_DAY_EMISSIONS -> AVD_EMIS
    //CONTROL_EFFICIENCY -> CEFF
    //RULE_EFFECTIVENESS -> REFF
    //RULE_PENETRATION -> RPEN

    public ORLAreaNonpointDataFormat(SqlTypeMapper sqlTypeMapper, boolean extendedFormat)
    {
        super(sqlTypeMapper, extendedFormat);
    }

    public FileColumnsMetadata getFileImportDetails()
    {
        FileColumnsMetadata details = new FileColumnsMetadata("", super.sqlTypeMapper);

        // once per line
        addDetail(details, FIPS_NAME, FIPS_TYPE, FIPS_WIDTH);
        addDetail(details, SOURCE_CLASSIFICATION_CODE_NAME, SOURCE_CLASSIFICATION_CODE_TYPE, SOURCE_CLASSIFICATION_CODE_WIDTH);
        addDetail(details, STANDARD_INDUSTRIAL_CODE_NAME, STANDARD_INDUSTRIAL_CODE_TYPE, STANDARD_INDUSTRIAL_CODE_WIDTH);
        addDetail(details, MACT_NAME, MACT_TYPE, MACT_WIDTH);
        addDetail(details, SOURCE_TYPE_NAME, SOURCE_TYPE_TYPE, SOURCE_TYPE_WIDTH);
        addDetail(details, NAICS_NAME, NAICS_TYPE, NAICS_WIDTH);
        addDetail(details, CAS_NAME, CAS_TYPE, CAS_WIDTH);
        addDetail(details, ANNUAL_EMISSIONS_NAME, ANNUAL_EMISSIONS_TYPE, ANNUAL_EMISSIONS_WIDTH);
        addDetail(details, AVERAGE_DAY_EMISSIONS_NAME, AVERAGE_DAY_EMISSIONS_TYPE, AVERAGE_DAY_EMISSIONS_WIDTH);
        addDetail(details, CONTROL_EFFICIENCY_NAME, CONTROL_EFFICIENCY_TYPE, CONTROL_EFFICIENCY_WIDTH);
        addDetail(details, RULE_EFFECTIVENESS_NAME, RULE_EFFECTIVENESS_TYPE, RULE_EFFECTIVENESS_WIDTH);
        addDetail(details, RULE_PENETRATION_NAME, RULE_PENETRATION_TYPE, RULE_PENETRATION_WIDTH);

        return details;
    }//getFileImportDetails()
}
