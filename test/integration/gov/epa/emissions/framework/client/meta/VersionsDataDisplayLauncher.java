package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.ScrollableTable;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class VersionsDataDisplayLauncher {

    public static void main(String[] args) throws Exception {
        Version version = new Version();
        version.setName("name");
        version.setPath("0,1");
        version.setVersion(2);
        version.setDate(new Date());

        VersionsTableData tableData = new VersionsTableData(new Version[] { version });
        JScrollPane table = new ScrollableTable(new EmfTableModel(tableData));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(table, BorderLayout.CENTER);

        JFrame frame = new JFrame();
        frame.getContentPane().add(panel);

        frame.setSize(new Dimension(800, 600));
        frame.setLocation(new Point(400, 200));
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.show();
    }

}
