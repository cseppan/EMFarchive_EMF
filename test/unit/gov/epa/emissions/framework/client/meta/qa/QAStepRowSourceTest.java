package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.services.data.EmfDateFormat;
import gov.epa.emissions.framework.services.data.QAStep;

import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

public class QAStepRowSourceTest extends TestCase {

    public void testShouldFillValuesInAppropriatePositions() {
        QAStep step = new QAStep();
        step.setVersion(2);
        step.setName("step");
        step.setWho("username");
        step.setDate(new Date());
        step.setProgram("program");
        step.setProgramArguments("args");
        step.setRequired(true);
        step.setOrder((float) 823.2);
        step.setComments("result");
        step.setStatus("status");
        step.setConfiguration("dataset one");

        QAStepRowSource source = new QAStepRowSource(step);

        Object[] values = source.values();
        assertEquals(11, values.length);
        assertEquals(new Integer(step.getVersion()), values[0]);
        assertEquals(step.getName(), values[1]);
        assertEquals(step.isRequired(), ((Boolean) values[2]).booleanValue());
        assertEquals(step.getOrder() + "", values[3] + "");
        assertEquals(step.getStatus(), values[4]);

        SimpleDateFormat dateFormat = new SimpleDateFormat(EmfDateFormat.format());
        assertEquals(dateFormat.format(step.getDate()), values[5]);

        assertEquals(step.getWho(), values[6]);
        assertEquals(step.getComments(), values[7]);
        assertEquals(step.getProgram(), values[8]);
        assertEquals(step.getProgramArguments(), values[9]);
        assertEquals(step.getConfiguration(), values[10]);
    }

    public void testShouldTrackOriginalSource() {
        QAStep step = new QAStep();
        QAStepRowSource source = new QAStepRowSource(step);

        assertEquals(step, source.source());
    }
}
