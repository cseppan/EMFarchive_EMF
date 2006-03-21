package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.info.InfoTabPresenter;
import gov.epa.emissions.framework.client.meta.info.InfoTabView;
import gov.epa.emissions.framework.client.meta.keywords.KeywordsTabPresenter;
import gov.epa.emissions.framework.client.meta.keywords.KeywordsTabView;
import gov.epa.emissions.framework.client.meta.logs.LogsTabPresenter;
import gov.epa.emissions.framework.client.meta.logs.LogsTabView;
import gov.epa.emissions.framework.client.meta.notes.NotesTabPresenter;
import gov.epa.emissions.framework.client.meta.notes.NotesTabView;
import gov.epa.emissions.framework.client.meta.qa.QATabPresenter;
import gov.epa.emissions.framework.client.meta.qa.ViewableQATabView;
import gov.epa.emissions.framework.client.meta.revisions.RevisionsTabPresenter;
import gov.epa.emissions.framework.client.meta.revisions.RevisionsTabView;
import gov.epa.emissions.framework.client.meta.summary.SummaryTabPresenter;
import gov.epa.emissions.framework.client.meta.summary.SummaryTabView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class PropertiesViewPresenter {

    private EmfDataset dataset;

    private PropertiesView view;

    private EmfSession session;

    public PropertiesViewPresenter(EmfDataset dataset, EmfSession session) {
        this.dataset = dataset;
        this.session = session;
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

    public void set(InfoTabView view) {
        InfoTabPresenter presenter = new InfoTabPresenter(view, dataset);
        presenter.doDisplay();
    }

    public void set(DataTabView view) {
        DataTabPresenter presenter = new DataTabPresenter(view, dataset, session);
        presenter.doDisplay();
    }

    public void set(LogsTabView view) throws EmfException {
        LogsTabPresenter presenter = new LogsTabPresenter(view, dataset, session.loggingService());
        presenter.display();
    }

    public void set(NotesTabView view) throws EmfException {
        NotesTabPresenter presenter = new NotesTabPresenter(dataset, session.dataCommonsService());
        presenter.display(view);
    }

    public void set(RevisionsTabView view) throws EmfException {
        RevisionsTabPresenter presenter = new RevisionsTabPresenter(dataset, session.dataCommonsService());
        presenter.display(view);
    }

    public void set(ViewableQATabView view) throws EmfException {
        QATabPresenter presenter = new QATabPresenter(view, dataset, session.qaService());
        presenter.display();
    }

}
