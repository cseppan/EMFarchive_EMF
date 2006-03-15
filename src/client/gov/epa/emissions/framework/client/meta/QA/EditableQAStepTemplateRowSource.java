package gov.epa.emissions.framework.client.meta.QA;

import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.RowSource;

public class EditableQAStepTemplateRowSource implements RowSource {

    private QAStepTemplate source;

    private Boolean selected;

    public EditableQAStepTemplateRowSource(QAStepTemplate source) {
        this.source = source;
        this.selected = Boolean.FALSE;
    }

    public Object[] values() {
        return new Object[] { selected, source.getName(), source.getProgram(), 
                source.getProgramArguments(), Boolean.valueOf(source.isRequired()), source.getOrder() };
    }

    public void setValueAt(int column, Object val) {
        switch (column) {
        case 0:
            selected = (Boolean) val;
            break;
        case 1:
            source.setName((String)val);
            break;
        case 2:
            source.setProgram((String)val);
            break;
        case 3:
            source.setProgramArguments((String)val);
            break;
        case 4:
            source.setRequired(((Boolean)val).booleanValue());
            break;
        case 5:
            source.setOrder((String)val);
            break;
        default:
            throw new RuntimeException("invalid column - " + column);
        }
    }

    public Object source() {
        return source;
    }

    public boolean isSelected() {
        return selected.booleanValue();
    }

    public void validate(int rowNumber) throws EmfException {
        if (source.getName() == null || source.getName().trim().length() == 0)
            throw new EmfException("empty QAStepTemplate name at row " + rowNumber);
    }
}
