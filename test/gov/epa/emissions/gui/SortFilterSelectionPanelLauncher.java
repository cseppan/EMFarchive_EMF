package gov.epa.emissions.gui;

import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JFrame;

public class SortFilterSelectionPanelLauncher {

    public static void main(String[] args) {
        JFrame frame = new JFrame();

        String[] jill = { "Jill Row", "28" };
        String[] matt = {"Matt Fischer", "47"};
        String[][] data = new String[][]{jill, matt};
        String[] headers = { "Name", "Age" };
        
        SortFilterSelectModel selectModel = new SortFilterSelectModel(data, headers);
        SortFilterSelectionPanel panel = new SortFilterSelectionPanel(frame, selectModel);

        frame.getContentPane().add(panel);
        
        frame.setSize(new Dimension(500, 200));
        frame.setLocation(new Point(400, 200));

        frame.show();
    }
}
