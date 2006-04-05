package gov.epa.emissions.framework.client.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.framework.EmfMockObjectTestCase;

import org.jmock.Mock;
import org.jmock.core.constraint.IsInstanceOf;

public class QAStepTemplatesPanelPresenterTest extends EmfMockObjectTestCase {

    public void testShouldObserveViewOnDisplay() {
        Mock view = mock(QAStepTemplatesPanelView.class);

        QAStepTemplatesPanelPresenter presenter = new QAStepTemplatesPanelPresenter(null, (QAStepTemplatesPanelView) view
                .proxy());
        expectsOnce(view, "observe", presenter);

        presenter.display();
    }

    public void testShouldDisplayEditQAStepTemplateWindow() {
        Mock view = mock(QAStepTemplatesPanelView.class);

        DatasetType type = new DatasetType();
        QAStepTemplatesPanelPresenter presenter = new QAStepTemplatesPanelPresenter(type, (QAStepTemplatesPanelView) view
                .proxy());

        QAStepTemplate template = new QAStepTemplate();
        Mock editor = mock(EditQAStepTemplateView.class);
        
        expects(editor, 1, "observe", new IsInstanceOf(EditQAStepTemplatesPresenter.class));
        expects(editor, 1, "display", same(type));
        expects(editor, 1, "populateFields", same(template));
        
        presenter.doEdit((EditQAStepTemplateView) editor.proxy(), template);
    }

}
