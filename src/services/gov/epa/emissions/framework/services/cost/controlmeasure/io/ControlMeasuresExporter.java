package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.io.Exporter;
import gov.epa.emissions.commons.io.ExporterException;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ControlMeasuresExporter implements Exporter {

    private ControlMeasure[] controlMeasures;

    public ControlMeasuresExporter(File folder, ControlMeasure[] controlMeasures, User user, HibernateSessionFactory factory) {
        this.controlMeasures = controlMeasures;
    }

    public void run() throws ExporterException {
        //
        throw new ExporterException("Export control measures is under construction...");
    }
    
    public void export(File file) throws ExporterException {
        // NOTE Auto-generated method stub
        throw new ExporterException("Under construction...");
    }
    
    public int[] getCMIds() {
        List idList = new ArrayList();
        
        for (int i = 0; i < controlMeasures.length; i++)
            idList.add(new Integer(controlMeasures[i].getId()));
        
        int[] ids = new int[idList.size()];
        
        for (int j = 0; j < ids.length; j++)
            ids[j] = Integer.parseInt(idList.get(j).toString());
            
        
        return ids;
    }

}
