package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.EmfDataset;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class VersionsEditorPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() {
        Mock view = mock(VersionsEditorView.class);
        EmfDataset dataset = new EmfDataset();
        DataEditorService service = (DataEditorService) mock(DataEditorService.class).proxy();

        view.expects(once()).method("display").with(same(dataset), same(service));

        VersionsEditorPresenter p = new VersionsEditorPresenter(dataset, service);
        view.expects(once()).method("observe").with(same(p));

        p.display((VersionsEditorView) view.proxy());
    }

    public void testShouldCloseViewOnClose() {
        Mock view = mock(VersionsEditorView.class);
        VersionsEditorPresenter p = displayPresenter(view);

        view.expects(once()).method("close").withNoArguments();
        p.doClose();
    }

    private VersionsEditorPresenter displayPresenter(Mock view) {
        EmfDataset dataset = new EmfDataset();
        DataEditorService service = (DataEditorService) mock(DataEditorService.class).proxy();

        view.expects(once()).method("display").with(same(dataset), same(service));

        VersionsEditorPresenter p = new VersionsEditorPresenter(dataset, service);
        view.expects(once()).method("observe").with(same(p));

        p.display((VersionsEditorView) view.proxy());

        return p;
    }
}
