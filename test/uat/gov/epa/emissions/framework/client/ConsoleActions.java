package gov.epa.emissions.framework.client;

public class ConsoleActions {

    private UserAcceptanceTestCase testcase;

    private EmfConsole console;

    public ConsoleActions(UserAcceptanceTestCase testcase) {
        this.testcase = testcase;
    }

    public EmfConsole open() throws Exception {
        console = testcase.openConsole();
        return console;
    }

}
