package gov.epa.emissions.framework.client.meta.qa;

public class QAStatus {

    public String[] list() {
        return new String[] { "Start", "Skipped", "In Progress", "Complete", "Failure" };
    }
}
