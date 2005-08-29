package gov.epa.emissions.framework.client.exim;

public interface ExportView {
    void register(ExportPresenter presenter);

    void close();
}
