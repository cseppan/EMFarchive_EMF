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
        step.setRequired(true);
        step.setOrder((float)823.2);
        step.setResult("result");
        step.setStatus("status");

        QAStepRowSource source = new QAStepRowSource(step);

        Object[] values = source.values();
        assertEquals(9, values.length);
        assertEquals(new Long(step.getVersion()), values[0]);
        assertEquals(step.getName(), values[1]);
        assertEquals(step.getWho(), values[2]);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        assertEquals(dateFormat.format(step.getWhen()), values[3]);

        assertEquals(step.getProgram(), values[4]);
        assertEquals(step.isRequired(), ((Boolean) values[5]).booleanValue());
        assertEquals(step.getOrder(), 0.0, ((Float) values[6]).floatValue());
        assertEquals(step.getResult(), values[7]);
        assertEquals(step.getStatus(), values[8]);
    }

    public void testShouldTrackOriginalSource() {
        QAStep step = new QAStep();
        QAStepRowSource source = new QAStepRowSource(step);

        assertEquals(step, source.source());
    }
}
