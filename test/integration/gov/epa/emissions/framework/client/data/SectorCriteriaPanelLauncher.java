package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.SectorCriteria;

import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JFrame;

public class SectorCriteriaPanelLauncher {

    public static void main(String[] args) throws Exception {
        SectorCriteria criteria = new SectorCriteria();
        criteria.setType("type1");
        criteria.setCriteria("crit1");

        SectorCriteriaTableData tableData = new SectorCriteriaTableData(new SectorCriteria[] { criteria });
        SectorCriteriaPanel panel = new SectorCriteriaPanel(tableData);

        JFrame frame = new JFrame();
        frame.getContentPane().add(panel);

        frame.setSize(new Dimension(800, 600));
        frame.setLocation(new Point(400, 200));
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.show();
    }

}
