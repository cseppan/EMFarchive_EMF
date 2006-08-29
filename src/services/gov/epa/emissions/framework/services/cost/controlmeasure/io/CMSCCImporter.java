package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.io.importer.ImporterException;

import java.io.File;
import java.util.Map;

public class CMSCCImporter{

    private File file;
    
    private CMSCCRecordReader sccReader;
    
    public CMSCCImporter(File file, CMSCCsFileFormat fileFormat) {
        this.file = file;
        this.sccReader = new CMSCCRecordReader(fileFormat);
    }

    public void run(Map controlMeasures) throws ImporterException {
        CMCSVFileReader reader = new CMCSVFileReader(file);
        try {
            for (Record record = reader.read(); !record.isEnd(); record = reader.read()) {
                sccReader.parse(controlMeasures,record, reader.lineNumber());
            }
        } catch (CMImporterException e) {
            throw new ImporterException(e.getMessage());// FIXME:quit import or not
        }


    }

}
