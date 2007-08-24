package gov.epa.emissions.framework.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CaseJobRunManager extends RunManager {
    private static Log log = LogFactory.getLog(CaseJobRunManager.class);

    public CaseJobRunManager() {
        super();
        log.info("CaseJobRunManager constructor");
    }

    public static RunManager getCaseJobRunManager() {
        if (ref == null)
            ref = new CaseJobRunManager();
        return ref;
    }

    


}
