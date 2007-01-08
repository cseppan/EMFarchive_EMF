package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.framework.services.data.EmfDateFormat;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.RowSource;

import java.util.Date;

public class QAStepRowSource implements RowSource {

    private QAStep source;

    public QAStepRowSource(QAStep source) {
        this.source = source;
    }

    public Object[] values() {
        String comments = source.getComments();
        if (comments != null && comments.length() > 50)
            comments = comments.substring(0, 45) + "  ...";

        return new Object[] { new Integer(source.getVersion()), source.getName(), Boolean.valueOf(source.isRequired()),
                new Float(source.getOrder()), source.getStatus(), format(source.getDate()), source.getWho(), comments,
                program(source.getProgram()), source.getProgramArguments(), source.getConfiguration() };
    }

    private String program(QAProgram program) {
        return (program != null) ? program.getName() : "";
    }

    private Object format(Date date) {
        return date == null ? "N/A" : EmfDateFormat.format_YYYY_MM_DD_HH_MM(date);
    }

    public Object source() {
        return source;
    }

    public void validate(int rowNumber) {// No Op
    }

    public void setValueAt(int column, Object val) {// No Op
    }
}