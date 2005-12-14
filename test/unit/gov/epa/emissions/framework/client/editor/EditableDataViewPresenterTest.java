package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.EditToken;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.Constraint;
import org.jmock.core.constraint.HasPropertyWithValue;
import org.jmock.core.constraint.IsInstanceOf;

public class EditableDataViewPresenterTest extends MockObjectTestCase {

    public void testShouldLoadTablesOfDatasetOnDisplay() throws Exception {
        Version version = new Version();
        String table = "table";
        Mock service = mock(DataEditorService.class);
        DataEditorService serviceProxy = (DataEditorService) service.proxy();

        Mock view = mock(EditableDataView.class);
        view.expects(once()).method("display").with(eq(version), eq(table), same(serviceProxy));

        EditableDataViewPresenter p = new EditableDataViewPresenter(version, table, (EditableDataView) view.proxy(),
                serviceProxy);
        view.expects(once()).method("observe").with(same(p));

        p.display();
    }

    public void testShouldCloseViewAndCloseDataEditSessionOnClose() throws Exception {
        Mock view = mock(EditableDataView.class);
        view.expects(once()).method("close").withNoArguments();

        Mock services = mock(DataEditorService.class);

        EditableDataViewPresenter p = new EditableDataViewPresenter(null, null, (EditableDataView) view.proxy(),
                (DataEditorService) services.proxy());

        p.doClose();
    }

    public void testShouldDiscardChangesOnDiscard() throws Exception {
        Version version = new Version();
        String table = "table";

        Mock services = mock(DataEditorService.class);
        Constraint propertyConstraint = and(new HasPropertyWithValue("version", same(version)),
                new HasPropertyWithValue("table", eq(table)));
        Constraint constraint = and(new IsInstanceOf(EditToken.class), propertyConstraint);
        services.expects(once()).method("discard").with(constraint);

        EditableDataViewPresenter p = new EditableDataViewPresenter(version, table, null, (DataEditorService) services
                .proxy());

        p.doDiscard();
    }

    public void testShouldSaveChangesOnSave() throws Exception {
        Version version = new Version();
        String table = "table";

        Mock services = mock(DataEditorService.class);
        Constraint propertyConstraint = and(new HasPropertyWithValue("version", same(version)),
                new HasPropertyWithValue("table", eq(table)));
        Constraint constraint = and(new IsInstanceOf(EditToken.class), propertyConstraint);
        services.expects(once()).method("save").with(constraint);

        EditableDataViewPresenter p = new EditableDataViewPresenter(version, table, null, (DataEditorService) services
                .proxy());

        p.doSave();
    }

    public void testShouldMarkVersionAsFinalChangesOnMarkFinal() throws Exception {
        Version version = new Version();

        Mock services = mock(DataEditorService.class);
        services.expects(once()).method("markFinal").with(same(version)).will(returnValue(null));

        Mock view = mock(EditableDataView.class);
        view.expects(once()).method("close").withNoArguments();

        EditableDataViewPresenter p = new EditableDataViewPresenter(version, null, (EditableDataView) view.proxy(),
                (DataEditorService) services.proxy());

        p.doMarkFinal();
    }

}
