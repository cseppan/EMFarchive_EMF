package gov.epa.emissions.framework.client.fast.run.edit;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.fast.run.AbstractFastRunWindow;
import gov.epa.emissions.framework.services.fast.FastRun;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JTabbedPane;

@SuppressWarnings("serial")
public class FastRunEditorWindow extends AbstractFastRunWindow {

    public FastRunEditorWindow(DesktopManager desktopManager, EmfSession session, EmfConsole parentConsole) {
        super("Edit Fast Run", desktopManager, session, parentConsole);
    }

    protected void doLayout(FastRun run) {

        Container contentPane = getContentPane();
        contentPane.removeAll();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(this.getMessagePanel(), BorderLayout.PAGE_START);
        contentPane.add(createTabbedPane(run));
        contentPane.add(createButtonsPanel(), BorderLayout.PAGE_END);
    }

    private JTabbedPane createTabbedPane(FastRun run) {

        final JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.addTab("Summary", this.createSummaryTab(run));
        tabbedPane.addTab("Configuration", this.createConfigurationTab(run));
        tabbedPane.addTab("Inventories", this.createInventoriesTab(run));
        tabbedPane.addTab("Outputs", this.createOutputsTab(run));
        return tabbedPane;
    }

    public void refresh(FastRun run) {
        /*
         * no-op
         */
    }
}