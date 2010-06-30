package gov.epa.emissions.framework.client.fast.run.tabs.inputs;

@SuppressWarnings("serial")
public abstract class FastRunInputWindow implements FastRunInputView {

    // public FastInputWindow(DesktopManager desktopManager, EmfSession session, EmfConsole parentConsole) {
    // super("Edit Fast Entity", desktopManager, session, parentConsole);
    // }
    //
    // protected void doLayout(FastEntity entity) {
    //
    // Container contentPane = getContentPane();
    // contentPane.removeAll();
    // contentPane.setLayout(new BorderLayout());
    // contentPane.add(this.getMessagePanel(), BorderLayout.PAGE_START);
    // contentPane.add(createTabbedPane(entity));
    // contentPane.add(createButtonsPanel(), BorderLayout.PAGE_END);
    // }
    //
    // private JTabbedPane createTabbedPane(FastEntity entity) {
    //
    // final JTabbedPane tabbedPane = new JTabbedPane();
    // tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    // tabbedPane.addTab("Summary", this.createSummaryTab(entity));
    // tabbedPane.addTab("Inputs", this.createInputsTab(entity));
    // tabbedPane.addTab("Control Strategies", this.createControlStrategiesTab(entity));
    // tabbedPane.addTab("Outputs", this.createOutputsTab(entity));
    // return tabbedPane;
    // }
    //
    // public void refresh(FastEntity entity) {
    // /*
    // * no-op
    // */
    // }
}