package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.client.EmfInternalFrame;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.DatasetTypeService;
import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.ui.DefaultViewLayout;
import gov.epa.emissions.framework.ui.ViewLayout;

import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;

import org.jmock.Mock;
import org.jmock.core.constraint.IsEqual;
import org.jmock.core.stub.ReturnStub;

public class DatasetTypesManagerWindowLauncher {

    public static void main(String[] args) throws Exception {
        DatasetTypesManagerWindowLauncher launcher = new DatasetTypesManagerWindowLauncher();

        JFrame frame = new JFrame();
        JDesktopPane desktop = new JDesktopPane();

        DatasetTypesManagerWindow view = new DatasetTypesManagerWindow(frame, desktop);
        DatasetType[] types = types();

        Mock datasetTypesServices = new Mock(DatasetTypeService.class);
        datasetTypesServices.stubs().method(new IsEqual("getDatasetTypes")).will(new ReturnStub(types));

        Mock interdataServices = new Mock(DataCommonsService.class);
        interdataServices.stubs().method(new IsEqual("getKeywords")).will(new ReturnStub(keywords()));

        Mock serviceLocator = new Mock(ServiceLocator.class);
        serviceLocator.stubs().method(new IsEqual("getDatasetTypesServices")).will(
                new ReturnStub(datasetTypesServices.proxy()));
        serviceLocator.stubs().method(new IsEqual("getInterDataServices")).will(
                new ReturnStub(interdataServices.proxy()));

        ViewLayout layout = new DefaultViewLayout(view);
        DatasetTypesManagerPresenter presenter = new DatasetTypesManagerPresenter(view, (ServiceLocator) serviceLocator
                .proxy(), layout);

        launcher.addAsInternalFrame(view, frame, desktop);

        frame.setSize(new Dimension(800, 600));
        frame.setLocation(new Point(400, 200));
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        presenter.doDisplay();
    }

    private static Keyword[] keywords() {
        return new Keyword[] { new Keyword("1"), new Keyword("2") };
    }

    private static DatasetType[] types() {
        DatasetType type1 = new DatasetType();
        type1.setName("type1");
        type1.setKeywords(new Keyword[] { new Keyword("1"), new Keyword("2"), new Keyword("3") });
        DatasetType type2 = new DatasetType();
        type2.setName("type2");
        type2.setKeywords(new Keyword[] { new Keyword("11"), new Keyword("12"), new Keyword("13") });
        DatasetType[] types = { type1, type2 };
        return types;
    }

    private void addAsInternalFrame(EmfInternalFrame console, JFrame frame, JDesktopPane desktop) {
        desktop.setName("EMF Console");
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        desktop.add(console);

        frame.setContentPane(desktop);
    }

}
