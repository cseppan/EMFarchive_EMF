package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.client.data.datasettype.EditQAStepTemplatesView;

import org.jmock.Mock;

public class EditQAStepTemplatesPresenterTest extends EmfMockObjectTestCase {

    public void testShouldObserveViewOnDisplay() {
        Mock view = mock(EditQAStepTemplatesView.class);

        EditQAStepTemplatesPresenter presenter = new EditQAStepTemplatesPresenter(null, (EditQAStepTemplatesView) view
                .proxy());
        expectsOnce(view, "observe", presenter);

        presenter.display();
    }

    public void testShouldAddQAStepTemplateToViewOnAdd() {
        Mock view = mock(EditQAStepTemplatesView.class);

        DatasetType type = new DatasetType();
        EditQAStepTemplatesPresenter presenter = new EditQAStepTemplatesPresenter(type, (EditQAStepTemplatesView) view
                .proxy());

        Mock dialog = mock(NewQAStepTemplateView.class);
        expects(dialog, 1, "display", same(type));
        stub(dialog, "shouldCreate", Boolean.TRUE);
        QAStepTemplate stepTemplate = new QAStepTemplate();
        stub(dialog, "template", stepTemplate);

        expectsOnce(view, "add", stepTemplate);

        presenter.doAdd((NewQAStepTemplateView) dialog.proxy());
    }
    
    public void testShouldNotAddQAStepTemplateToViewIfUserCancelsNewDialogOnAdd() {
        Mock view = mock(EditQAStepTemplatesView.class);
        
        DatasetType type = new DatasetType();
        EditQAStepTemplatesPresenter presenter = new EditQAStepTemplatesPresenter(type, (EditQAStepTemplatesView) view
                .proxy());
        
        Mock dialog = mock(NewQAStepTemplateView.class);
        expects(dialog, 1, "display", same(type));
        stub(dialog, "shouldCreate", Boolean.FALSE);
        
        presenter.doAdd((NewQAStepTemplateView) dialog.proxy());
    }

}
