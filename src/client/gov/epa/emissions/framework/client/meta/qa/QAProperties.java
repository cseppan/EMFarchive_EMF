package gov.epa.emissions.framework.client.meta.qa;

public class QAProperties {

    public String[] status() {
        return new String[] { initialStatus(), "Skipped", "In Progress", "Complete", "Failure" };
    }

    public String[] programs() {
        return new String[] { "EmisView", "Smkreport", "Smkinven" };
    }

    public String initialStatus() {
        return "Not Started";
    }
}
