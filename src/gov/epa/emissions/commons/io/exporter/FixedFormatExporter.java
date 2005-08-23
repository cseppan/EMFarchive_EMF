package gov.epa.emissions.commons.io.exporter;

import gov.epa.emissions.commons.db.DbServer;

/**
 * This class contain elements to exporting common to all fixed format text files.
 * 
 * @author Keith Lee, CEP UNC
 * @version $Id: FixedFormatExporter.java,v 1.1 2005/08/23 20:28:27 rhavaldar Exp $
 */
public abstract class FixedFormatExporter implements Exporter
{
    protected DbServer dbServer;

    protected FixedFormatExporter(DbServer dbServer) {
        this.dbServer = dbServer;
    }
}
