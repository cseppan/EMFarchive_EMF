package gov.epa.emissions.commons.io.importer;

import gov.epa.emissions.commons.db.SqlTypeMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FieldDefinitionsFileReader {
    private static final String NAMES = "names";

    private static final String TYPES = "types";

    private static final String WIDTHS = "widths";

    /** the delimiter used in the field defs file * */
    private static final String DELIMITER = ", ";

    /** the buffered reader to be used for reading the field defs file * */
    private BufferedReader reader = null;

    /** if multiple details objects are required * */
    private Map detailsMap = null;

    /**
     * @TODO fill out this constructor later
     */
    public FieldDefinitionsFileReader(File file, SqlTypeMapper sqlTypeMapper) throws IOException {
        try {
            reader = new BufferedReader(new FileReader(file));
            detailsMap = new HashMap();
            String[] buffer = new String[3];
            FileColumnsMetadata details = null;

            while (readFileSection(reader, buffer)) {
                details = readTableDetails(buffer, sqlTypeMapper);
                detailsMap.put(details.getTableName(), details);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    /**
     * given a stringbuffer and an import file type (i.e. nei_area_em)
     * 
     * @param buffer
     *            the string buffer containing the details
     * 
     */
    private FileColumnsMetadata readTableDetails(String[] buffer, SqlTypeMapper sqlTypeMapper) {
        String importFileType = buffer[0].substring(0, buffer[0].indexOf("_names"));
        // initialize the details object
        FileColumnsMetadata details = new FileColumnsMetadata(importFileType, sqlTypeMapper);

        // the string array will contain column information in the following
        // format
        // --------------------------------------
        // *******_names: A, B, C, D
        // *******_types: W, X, Y, Z
        // *******_widths: 1, 2, 3, 4
        // --------------------------------------

        // break up the stringarray into individual lines
        // e.g. namesLine will be [******_names: A, B, C, D]
        String namesLine = buffer[0];
        String typesLine = buffer[1];
        String widthsLine = buffer[2];

        // remove the headers from each of the lines to they can be easily
        // parsed
        // e.g. the namesLine will now be [A, B, C, D]
        namesLine = namesLine.replaceFirst(importFileType + "_" + NAMES + ": ", "");
        typesLine = typesLine.replaceFirst(importFileType + "_" + TYPES + ": ", "");
        widthsLine = widthsLine.replaceFirst(importFileType + "_" + WIDTHS + ": ", "");

        // now split up the strings and put them into the details object
        // for our example the details object should look like this
        // --- the different elements are separated by | ----
        // namesVector: [A | B | C | D]
        // nameTypeMap: [A:W | B:X | C:Y | D:Z]
        // nameWidthMap: [A:1 | B:2 | C:3 | D:4]
        String[] nameSplitString = namesLine.split(DELIMITER);
        for (int i = 0; i < nameSplitString.length; i++) {
            details.addColumnName(nameSplitString[i]);
        }

        String[] typeSplitString = typesLine.split(DELIMITER);
        for (int i = 0; i < typeSplitString.length; i++) {
            try {
                details.setType(nameSplitString[i], typeSplitString[i]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String[] widthSplitString = widthsLine.split(DELIMITER);
        for (int i = 0; i < widthSplitString.length; i++) {
            try {
                details.setWidth(nameSplitString[i], widthSplitString[i]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return details;
    }

    private boolean readFileSection(BufferedReader reader, String[] buffer) throws IOException {
        String line = null;
        // keep running through the file
        while ((line = reader.readLine()) != null) {
            if (!line.startsWith("#") && line.trim().length() > 0) {
                // if there are
                buffer[0] = line;
                buffer[1] = reader.readLine();
                buffer[2] = reader.readLine();
                return true;
            }
        }
        return false;
    }

    public FileColumnsMetadata getFileColumnsMetadata(String fileImportType) throws Exception {
        if (!detailsMap.containsKey(fileImportType))
            throw new Exception("The fileimportype " + fileImportType + " was not found in the field definitions file");
        return (FileColumnsMetadata) detailsMap.get(fileImportType);
    }

}
