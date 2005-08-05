package gov.epa.emissions.commons.io.importer.orl;

import gov.epa.emissions.commons.io.ColumnType;

/**
 * @author Keith Lee, CEP UNC
 * @version $Id: ORLAreaDataFormat.java,v 1.1 2005/08/05 13:14:28 rhavaldar Exp $
 */
public abstract class ORLAreaDataFormat extends ORLDataFormat
{
    //CAS -> CAS
    //ANNUAL_EMISSIONS -> ANN_EMIS
    //AVERAGE_DAY_EMISSIONS -> AVD_EMIS
    protected static final String CONTROL_EFFICIENCY_NAME = "CEFF";//optional: SMOKE default value == 0
    protected static final int CONTROL_EFFICIENCY_WIDTH = NO_WIDTH;
    protected static final ColumnType CONTROL_EFFICIENCY_TYPE = ColumnType.REAL;
    protected static final String RULE_EFFECTIVENESS_NAME = "REFF";//optional: SMOKE default value == 100
    protected static final int RULE_EFFECTIVENESS_WIDTH = NO_WIDTH;
    protected static final ColumnType RULE_EFFECTIVENESS_TYPE = ColumnType.REAL;
    protected static final String RULE_PENETRATION_NAME = "RPEN";//optional: SMOKE default value == 100
    protected static final int RULE_PENETRATION_WIDTH = NO_WIDTH;
    protected static final ColumnType RULE_PENETRATION_TYPE = ColumnType.REAL;

    public ORLAreaDataFormat(boolean extendedFormat)
    {
        super(extendedFormat);
    }
}
