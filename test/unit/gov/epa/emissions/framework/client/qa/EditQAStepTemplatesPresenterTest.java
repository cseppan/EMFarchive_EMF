package gov.epa.emissions.framework.client.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.services.EmfException;

import org.jmock.Mock;

public class EditQAStepTemplatesPresenterTest extends EmfMockObjectTestCase {

    public void testShouldObserveViewOnDisplay() {
        Mock view = mock(EditQAStepTemplatesView.class);

        EditQAStepTemplatesPresenter presenter = new EditQAStepTemplatesPresenter(null, (EditQAStepTemplatesView) view
                .proxy());
        expectsOnce(view, "observe", presenter);

        presenter.display();
    }

    public void testShouldRunEditQAStepTemplateWindow() throws EmfException {
        Mock view = mock(EditQAStepTemplatesView.class);

        DatasetType type = new DatasetType();
        EditQAStepTemplatesPresenter presenter = new EditQAStepTemplatesPresenter(type, (EditQAStepTemplatesView) view
                .proxy());

        Mock editor = mock(EditQAStepTemplateView.class);
        expects(editor, "loadTemplate");
        expects(view, "refresh");

        presenter.doEdit_WRONG_PRESENTER((EditQAStepTemplateView) editor.proxy());
    }

    public void testShouldDisplayEditQAStepTemplateWindow() {
        Mock view = mock(EditQAStepTemplatesView.class);

        DatasetType type = new DatasetType();
        EditQAStepTemplatesPresenter presenter = new EditQAStepTemplatesPresenter(type, (EditQAStepTemplatesView) view
                .proxy());

        QAStepTemplate template = new QAStepTemplate();
        Mock editor = mock(EditQAStepTemplateView.class);
        expects(editor, "display");
        expects(editor, 1, "observe", same(presenter));
        expects(editor, 1, "display", same(template));

        presenter.doEdit((EditQAStepTemplateView) editor.proxy(), template);
    }

}
