package gov.epa.emissions.framework.client.meta.qa;

public class QAProperties {

    public static String[] status() {
        return new String[] { initialStatus(), "Skipped", "In Progress", "Complete", "Failed" };
    }

    public static String[] programs() {
        return new String[] { "EmisView", "Smkreport", "Smkinven" };
    }

    public static String initialStatus() {
        return "Not Started";
    }
}
