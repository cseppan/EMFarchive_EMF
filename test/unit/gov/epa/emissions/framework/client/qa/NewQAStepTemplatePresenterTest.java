package gov.epa.emissions.framework.client.qa;

import org.jmock.Mock;
import org.jmock.core.Constraint;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.framework.EmfMockObjectTestCase;

public class NewQAStepTemplatePresenterTest extends EmfMockObjectTestCase {

    public void testShouldAddQAStepTemplateToViewOnAdd() {
        Mock view = mock(QAStepTemplatesPanelView.class);
        Mock dialog = mock(NewQAStepTemplateView.class);
        QAStepTemplate stepTemplate = new QAStepTemplate();
        expectsOnce(view, "add", stepTemplate);
        
        NewQAStepTemplatePresenter presenter = new NewQAStepTemplatePresenter(
                (QAStepTemplatesPanelView) view.proxy(), (NewQAStepTemplateView)dialog.proxy());

        presenter.addNew(stepTemplate);
    }
    
    public void testShouldDisplayView() {
        Mock view = mock(QAStepTemplatesPanelView.class);

        DatasetType type = new DatasetType();
        QAProgram[] programs = {};
        Mock dialog = mock(NewQAStepTemplateView.class);
        expects(dialog, 1, "display", new Constraint[]{same(type),same(programs)});

        NewQAStepTemplatePresenter presenter = new NewQAStepTemplatePresenter(
                (QAStepTemplatesPanelView) view.proxy(), (NewQAStepTemplateView)dialog.proxy());

        expects(dialog, 1, "observe", same(presenter));
        
        presenter.display(type,programs);
    }
}
