package gov.epa.emissions.commons.io;

import gov.epa.emissions.commons.io.importer.FileImportDetails;

/**
 * Base DataFormat object used to construct FileImportDetails object.
 * 
 * @author Keith Lee, CEP UNC
 * @version $Id: DataFormat.java,v 1.1 2005/08/05 13:14:28 rhavaldar Exp $
 */
public abstract class DataFormat
{
    public static final String FIPS_NAME = "FIPS";
    protected static final ColumnType FIPS_TYPE = ColumnType.INT;
    protected static final int FIPS_WIDTH = 5;

    /** TODO should this be moved to FileImportDetails? */
    public final void addDetail(FileImportDetails details, String name, ColumnType type, int width)
    {
        details.add(name);
        try
        {
            details.setType(name, type.getName());
            details.setWidth(name, String.valueOf(width));
        }
        catch (Exception e)
        {
            System.err.println("Error adding details to data format: " + name);
        }
    }//addDetail(FileImportDetails, String, Type, String)

    public abstract FileImportDetails getFileImportDetails();
}
