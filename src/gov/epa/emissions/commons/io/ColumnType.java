package gov.epa.emissions.commons.io;


/**
 * The type of columns to insert into FileImportDetails.
 * 
 * @author Keith Lee, CEP UNC
 * @version $Id: ColumnType.java,v 1.1 2005/08/05 13:14:28 rhavaldar Exp $
 */
public final class ColumnType extends Enum
{
    public static final ColumnType INT = new ColumnType("I");
    public static final ColumnType REAL = new ColumnType("N");
    public static final ColumnType CHAR = new ColumnType("C");

    private ColumnType(String name)
    {
        super(name);
    }
}

