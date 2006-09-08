package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.data.QAProgram;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class QAPrograms {

    private List list;

    public QAPrograms(QAProgram[] programs) {
        this.list = new ArrayList(Arrays.asList(programs));
    }

    public QAProgram get(String name) {
        if (name == null || name.trim().length() == 0)
            return null;

        name = name.trim();
        for (int i = 0; i < list.size(); i++) {
            QAProgram item = ((QAProgram) list.get(i));
            if (item.getName().equalsIgnoreCase(name))
                return item;
        }
        QAProgram p = new QAProgram();
        p.setName(name);
        return p;
    }

    public String[] names() {
        List names = new ArrayList();
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            QAProgram element = (QAProgram) iter.next();
            names.add(element.getName());
        }

        return (String[]) names.toArray(new String[0]);
    }
}
