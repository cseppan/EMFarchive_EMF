package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.Sector;

public class ViewableSectorPresenterImpl implements ViewableSectorPresenter {

    private ViewableSectorView view;

    private Sector sector;

    public ViewableSectorPresenterImpl(ViewableSectorView view, Sector sector) {
        this.view = view;
        this.sector = sector;
    }

    public void doDisplay() {
        view.observe(this);
        view.display(sector);
    }

    public void doClose() {
        view.close();
    }

}
