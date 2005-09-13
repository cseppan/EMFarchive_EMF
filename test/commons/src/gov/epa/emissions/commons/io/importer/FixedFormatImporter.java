package gov.epa.emissions.commons.io.importer;

import gov.epa.emissions.commons.db.DbServer;

/**
 * This class contains all the common features for importing files with a fixed
 * format.
 */
public abstract class FixedFormatImporter extends FormattedImporter {
    protected FixedFormatImporter(TableTypes tableTypes, DbServer dbServer) {
        super(tableTypes, dbServer);
    }

    /**
     * this method takes a line and column widths as arguments and then splits
     * the line into strings accordingly
     * 
     * @param line
     *            the line to be split
     * @param widths
     *            the widths of each column
     * @return String[] a list of the split up strings
     */
    public String[] breakUpLine(String line, int[] widths) throws Exception {
        // create a list to hold the split up strings
        String[] stringlets = new String[widths.length];
        // need to remember the last split index
        int lastIndex = 0;
        int lineLength = line.length();
        int nextIndex = 0;
        for (int i = 0; i < widths.length; i++) {
            // get a split from the last index plus the width of the current
            // column
            nextIndex = lastIndex + widths[i];
            if (nextIndex > lineLength) {
                nextIndex = lineLength;
            }
            // check for index out of bounds
            checkForIndexOutOfBounds(i, stringlets, line);
            stringlets[i] = line.substring(lastIndex, nextIndex).trim();
            // increment the last split index by the current column width
            lastIndex = nextIndex;
        }// for int i
        return stringlets;
    }
}
