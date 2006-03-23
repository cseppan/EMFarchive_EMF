package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.data.QAStep;

import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

public class QAStepRowSourceTest extends TestCase {

    public void testShouldFillValuesInAppropriatePositions() {
        QAStep step = new QAStep();
        step.setVersion(2);
        step.setName("step");
        User user = new User();
        user.setUsername("username");
        step.setWho(user);
        step.setWhen(new Date());
        step.setProgram("program");
        step.setProgramArguments("args");
        step.setRequired(true);
        step.setOrder((float) 823.2);
        step.setResult("result");
        step.setStatus("status");

        QAStepRowSource source = new QAStepRowSource(step);

        Object[] values = source.values();
        assertEquals(10, values.length);
        assertEquals(new Integer(step.getVersion()), values[0]);
        assertEquals(step.getName(), values[1]);
        assertEquals(step.isRequired(), ((Boolean) values[2]).booleanValue());
        assertEquals(step.getOrder() + "", values[3]);
        assertEquals(step.getStatus(), values[4]);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        assertEquals(dateFormat.format(step.getWhen()), values[5]);

        assertEquals(step.getWho(), values[6]);
        assertEquals(step.getResult(), values[7]);
        assertEquals(step.getProgram(), values[8]);
        assertEquals(step.getProgramArguments(), values[9]);
    }

    public void testShouldTrackOriginalSource() {
        QAStep step = new QAStep();
        QAStepRowSource source = new QAStepRowSource(step);

        assertEquals(step, source.source());
    }
}
