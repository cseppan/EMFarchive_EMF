package gov.epa.emissions.framework.client.meta.QA;

import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.List;

public class QAStepTemplateTableData extends AbstractTableData {
    private List rows;

    public QAStepTemplateTableData(QAStepTemplate[] templates) {
        this.rows = createRows(templates);
    }

    public String[] columns() {
        return new String[] { "Name", "Program", "Arguments", "Required", "Order" };
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }

    private List createRows(QAStepTemplate[] templates) {
        List rows = new ArrayList();
        for (int i = 0; i < templates.length; i++)
            rows.add(row(templates[i]));

        return rows;
    }

    private ViewableRow row(QAStepTemplate template) {
        return new ViewableRow(template, new Object[] { template.getName(), template.getProgram(),
                template.getProgramArguments(), Boolean.valueOf(template.isRequired()), template.getOrder() });
    }

    public Class getColumnClass(int col) {
        return String.class;
    }

}
