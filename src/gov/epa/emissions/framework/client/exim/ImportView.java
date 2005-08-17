package gov.epa.emissions.framework.client.exim;

public interface ImportView {
    void register(ImportPresenter presenter);

    void close();

    void display();
}
