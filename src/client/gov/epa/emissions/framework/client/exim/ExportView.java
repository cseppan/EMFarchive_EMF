package gov.epa.emissions.framework.client.exim;

public interface ExportView {

    void observe(ExportPresenter presenter);

    void setMostRecentUsedFolder(String mostRecentUsedFolder);

    void close();

    void display();
}
