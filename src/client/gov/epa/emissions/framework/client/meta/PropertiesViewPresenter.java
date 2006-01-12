package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.client.meta.keywords.KeywordsTabPresenter;
import gov.epa.emissions.framework.client.meta.keywords.KeywordsTabView;
import gov.epa.emissions.framework.client.meta.summary.SummaryTabPresenter;
import gov.epa.emissions.framework.client.meta.summary.SummaryTabView;
import gov.epa.emissions.framework.services.EmfDataset;

public class PropertiesViewPresenter {

    private EmfDataset dataset;

    private PropertiesView view;

    public PropertiesViewPresenter(EmfDataset dataset) {
        this.dataset = dataset;
    }

    public void doDisplay(PropertiesView view) {
        this.view = view;
        view.observe(this);

        view.display(dataset);
    }

    public void doClose() {
        view.close();
    }

    public void set(SummaryTabView summary) {
        SummaryTabPresenter summaryPresenter = new SummaryTabPresenter();
        summaryPresenter.display();
    }

    public void set(KeywordsTabView keywordsView) {
        KeywordsTabPresenter keywordsPresenter = new KeywordsTabPresenter(keywordsView, dataset);
        keywordsPresenter.display();
    }

}
