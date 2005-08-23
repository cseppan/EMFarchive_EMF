package gov.epa.emissions.commons.io.exporter;

import gov.epa.emissions.commons.io.Dataset;

/**
 * The exporter interface for writing a table type to a text file.
 * @author Keith Lee
 * @version $Id: Exporter.java,v 1.1 2005/08/23 20:28:27 rhavaldar Exp $
 */
public interface Exporter
{
    public void exportTableToFile(String tableType, Dataset dataset, String fileName);
}
