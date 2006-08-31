package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.services.data.EmfDateFormat;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.RowSource;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class QAStepRowSource implements RowSource {

    private QAStep source;

    private DateFormat dateFormat;

    public QAStepRowSource(QAStep source) {
        this.source = source;
        dateFormat = new SimpleDateFormat(EmfDateFormat.format());
    }

    public Object[] values() {
        String comments = source.getComments();
        if(comments != null && comments.length() > 50)
            comments = comments.substring(0, 45) + "  ...";
        
        return new Object[] { new Integer(source.getVersion()), source.getName(), Boolean.valueOf(source.isRequired()),
                new Float(source.getOrder()), source.getStatus(), format(source.getDate()), source.getWho(),
                comments, source.getProgram(), source.getProgramArguments(), source.getConfiguration() };
    }

    private Object format(Date date) {
        return date == null ? "N/A" : dateFormat.format(date);
    }

    public Object source() {
        return source;
    }

    public void validate(int rowNumber) {// No Op
    }

    public void setValueAt(int column, Object val) {// No Op
    }
}