package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.data.DatasetsBrowserView;
import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.ExImServices;
import gov.epa.emissions.framework.services.User;

/**
 * updates the Datasets Browser once View is closed.
 */
public class DatasetsBrowserAwareImportPresenter extends ImportPresenter {

    private DataServices dataServices;

    private DatasetsBrowserView browser;

    public DatasetsBrowserAwareImportPresenter(User user, ExImServices eximServices, DataServices dataServices,
            DatasetsBrowserView browser) {
        super(user, eximServices);
        this.dataServices = dataServices;
        this.browser = browser;
    }

    public void doDone() {
        try {
            browser.refresh(dataServices.getDatasets());
        } catch (EmfException e) {
            browser.showError("Could not refresh Datasets");
            return;
        }

        super.doDone();
    }

}
