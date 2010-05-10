package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.admin.AddAdminOption;
import gov.epa.emissions.framework.client.admin.UpdateUserPresenter;
import gov.epa.emissions.framework.client.admin.UpdateUserPresenterImpl;
import gov.epa.emissions.framework.client.admin.UpdateUserWindow;
import gov.epa.emissions.framework.client.admin.UserView;
import gov.epa.emissions.framework.client.admin.UsersManager;
import gov.epa.emissions.framework.client.admin.UsersManagerView;
import gov.epa.emissions.framework.client.admin.ViewUserWindow;
import gov.epa.emissions.framework.client.casemanagement.CaseManagerView;
import gov.epa.emissions.framework.client.casemanagement.CaseManagerWindow;
import gov.epa.emissions.framework.client.cost.controlmeasure.ControlMeasuresManagerView;
import gov.epa.emissions.framework.client.cost.controlmeasure.ControlMeasuresManagerWindow;
import gov.epa.emissions.framework.client.cost.controlprogram.ControlProgramManagerView;
import gov.epa.emissions.framework.client.cost.controlprogram.ControlProgramManagerWindow;
import gov.epa.emissions.framework.client.cost.controlstrategy.ControlStrategyManagerView;
import gov.epa.emissions.framework.client.cost.controlstrategy.ControlStrategyManagerWindow;
import gov.epa.emissions.framework.client.data.dataset.DatasetsBrowserView;
import gov.epa.emissions.framework.client.data.dataset.DatasetsBrowserWindow;
import gov.epa.emissions.framework.client.data.datasettype.DatasetTypesManagerView;
import gov.epa.emissions.framework.client.data.datasettype.DatasetTypesManagerWindow;
import gov.epa.emissions.framework.client.data.sector.SectorsManagerView;
import gov.epa.emissions.framework.client.data.sector.SectorsManagerWindow;
import gov.epa.emissions.framework.client.sms.sectorscenario.SectorScenarioManagerView;
import gov.epa.emissions.framework.client.sms.sectorscenario.SectorScenarioManagerWindow;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class ManageMenu extends JMenu implements ManageMenuView {

    private EmfConsolePresenter emfConsolePresenter;

    private EmfSession session;

    private EmfConsole parent;

    private DesktopManager desktopManager;

    private ManageMenuPresenter presenter;

    // FIXME: where's the associated Presenter ?
    public ManageMenu(EmfSession session, EmfConsole parent, MessagePanel messagePanel) {
        super("Manage");
        super.setName("manage");

        this.session = session;
        this.parent = parent;

        super.add(createDatasets(parent, messagePanel));
        super.add(createCases(parent, messagePanel));
        super.addSeparator();
        super.add(createDatasetTypes(parent, messagePanel));
        super.add(createSectors(parent, messagePanel));
        super.addSeparator();
        super.add(createControlMeasures(parent, messagePanel));
        super.add(createControlStrategies(parent, messagePanel));
        super.add(createControlPrograms(parent, messagePanel));
        super.addSeparator();
        super.add(createSectorScenario(parent, messagePanel));
        super.addSeparator();

        manageUsers(session.user(), messagePanel);
        super.add(createMyProfile(session, messagePanel));
    }

    public ManageMenu(EmfSession session, EmfConsole parent, MessagePanel messagePanel, DesktopManager desktopManager) {
        this(session, parent, messagePanel);
        this.desktopManager = desktopManager;
    }

    private JMenuItem createMyProfile(final EmfSession session, final MessagePanel messagePanel) {
        JMenuItem menuItem = new JMenuItem("My Profile");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                displayMyProfile(session, messagePanel);
            }
        });

        return menuItem;
    }

    private void manageUsers(User user, final MessagePanel messagePanel) {
        //if (user.isAdmin()) {
            JMenuItem users = new JMenuItem("Users");
            users.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    doManagerUsers(messagePanel);
                }
            });

            super.add(users);
        //}
    }

    private JMenuItem createDatasets(final EmfConsole parent, final MessagePanel messagePanel) {
        JMenuItem menuItem = new JMenuItem("Datasets");
        menuItem.setName("datasets");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                doDisplayDatasets(parent, messagePanel);
            }
        });

        return menuItem;
    }

    // FIXME: each of the menu-item and it's handles are similar. Refactor ?
    private JMenuItem createDatasetTypes(final EmfConsole parent, final MessagePanel messagePanel) {
        JMenuItem menuItem = new JMenuItem("Dataset Types");
        menuItem.setName("datasetTypes");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                doManageDatasetTypes(parent, messagePanel);
            }
        });

        return menuItem;
    }

    private JMenuItem createSectors(final EmfConsole parent, final MessagePanel messagePanel) {
        JMenuItem menuItem = new JMenuItem("Sectors");
        menuItem.setName("sectors");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                doManageSectors(parent, messagePanel);
            }
        });

        return menuItem;
    }

    private JMenuItem createCases(final EmfConsole parent, final MessagePanel messagePanel) {
        JMenuItem menuItem = new JMenuItem("Cases");
        menuItem.setName("cases");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                doManageCases(parent, messagePanel);
            }
        });

        return menuItem;
    }

    private JMenuItem createControlMeasures(final EmfConsole parent, final MessagePanel messagePanel) {
        JMenuItem menuItem = new JMenuItem("Control Measures");
        menuItem.setName("controlMeasures");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    doDisplayControlMeasures(parent);
                } catch (EmfException e) {
                    messagePanel.setError("Can't display control measures: " + e.getMessage());
                }
            }
        });

        return menuItem;
    }

    private JMenuItem createControlStrategies(final EmfConsole parent, final MessagePanel messagePanel) {
        JMenuItem menuItem = new JMenuItem("Control Strategies");
        menuItem.setName("controlStrategies");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                doManageControlStrategies(parent, messagePanel);
            }
        });

        return menuItem;
    }

    private JMenuItem createControlPrograms(final EmfConsole parent, final MessagePanel messagePanel) {
        JMenuItem menuItem = new JMenuItem("Control Programs");
        menuItem.setName("controlPrograms");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                doManageControlPrograms(parent, messagePanel);
            }
        });

        return menuItem;
    }
    
    private JMenuItem createSectorScenario(final EmfConsole parent, final MessagePanel messagePanel) {
        JMenuItem menuItem = new JMenuItem("Sector Scenario");
        menuItem.setName("SectorScenario");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                doManageSectorScenario(parent, messagePanel);
            }
        });

        return menuItem;
    }

    private void displayMyProfile(EmfSession session, MessagePanel messagePanel) {
        UpdateUserWindow updatable = new UpdateUserWindow(new AddAdminOption(false), desktopManager);
        UserView viewable = new ViewUserWindow(desktopManager);

        UpdateUserPresenter presenter = new UpdateUserPresenterImpl(session, session.user(), session.userService());
        try {
            presenter.display(updatable, viewable);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    public void displayUserManager() throws EmfException {
        UsersManagerView view = new UsersManager(parent, desktopManager);
        presenter.doDisplayUserManager(view);
    }

    public void observe(EmfConsolePresenter presenter) {
        this.emfConsolePresenter = presenter;
    }

    public void observe(ManageMenuPresenter presenter) {
        this.presenter = presenter;
    }

    private void doManagerUsers(final MessagePanel messagePanel) {
        try {
            emfConsolePresenter.notifyManageUsers();
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private void doDisplayDatasets(final EmfConsole parent, final MessagePanel messagePanel) {
        try {
            DatasetsBrowserView datasetsBrowserView = new DatasetsBrowserWindow(session, parent, desktopManager);
            presenter.doDisplayDatasetsBrowser(datasetsBrowserView);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private void doManageDatasetTypes(final EmfConsole parent, final MessagePanel messagePanel) {
        try {
            DatasetTypesManagerView view = new DatasetTypesManagerWindow(session, parent, desktopManager);
            presenter.doDisplayDatasetTypesManager(view);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private void doManageSectors(final EmfConsole parent, final MessagePanel messagePanel) {
        try {
            SectorsManagerView view = new SectorsManagerWindow(parent, desktopManager);
            presenter.doDisplaySectors(view);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private void doManageCases(final EmfConsole parent, final MessagePanel messagePanel) {
        CaseManagerView view = new CaseManagerWindow(session, parent, desktopManager);
        presenter.doDisplayCases(view);
    }

    private void doDisplayControlMeasures(final EmfConsole parent) throws EmfException {
        ControlMeasuresManagerView controlMeasuresManagerView = new ControlMeasuresManagerWindow(session, parent,
                desktopManager);
        presenter.doDisplayControlMeasuresManager(controlMeasuresManagerView);
    }

    private void doManageControlStrategies(final EmfConsole parent, final MessagePanel messagePanel) {
        ControlStrategyManagerView view = new ControlStrategyManagerWindow(parent, session, desktopManager);
        try {
            presenter.doDisplayControlStrategies(view);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private void doManageControlPrograms(final EmfConsole parent, final MessagePanel messagePanel) {
        ControlProgramManagerView view = new ControlProgramManagerWindow(parent, session, desktopManager);
        try {
            presenter.doDisplayControlPrograms(view);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }       
    }
    
    private void doManageSectorScenario(final EmfConsole parent, final MessagePanel messagePanel) {
        SectorScenarioManagerView view = new SectorScenarioManagerWindow(parent, session, desktopManager);
        try {
            presenter.doDisplaySectorScenarios(view);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

}
