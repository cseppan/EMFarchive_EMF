package gov.epa.emissions.framework.ui;

import gov.epa.emissions.commons.gui.SelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class SelectableSortFilterWrapper extends JPanel implements SelectModel {

    private EmfConsole parentConsole;

    private SortFilterSelectModel selectModel;

    private SortCriteria sortCriteria;

    private SortFilterSelectionPanel sortFilterSelectionpanel;

    public SelectableSortFilterWrapper(EmfConsole parentConsole, TableData tableData, SortCriteria criteria) {
        this.setLayout(new BorderLayout());
        this.parentConsole = parentConsole;
        this.sortCriteria = criteria;
        this.add(setLayout(parentConsole, tableData));
    }

    private JScrollPane setLayout(EmfConsole parentConsole, TableData tableData) {
        EmfTableModel model = new EmfTableModel(tableData);
        //get the old sort & filter criteria before creating new SortFilterSelectionPanel
        SortFilterSelectModel selectModel = new SortFilterSelectModel(model);
        this.selectModel = selectModel;
        selectModel.refresh();
        return sortFilterPane(parentConsole, selectModel);
    }

    private JScrollPane sortFilterPane(EmfConsole parentConsole, SortFilterSelectModel selectModel) {
        sortFilterSelectionpanel = new SortFilterSelectionPanel(parentConsole, selectModel);
        sortFilterSelectionpanel.sort(sortCriteria);
        sortFilterSelectionpanel.setPreferredSize(new Dimension(450, 120));

        return new JScrollPane(sortFilterSelectionpanel);
    }

    public void refresh(TableData tableData) {
        this.removeAll();
        this.add(setLayout(parentConsole, tableData));
        repaint();
    }

    public List<?> selected() {
        int[] selectedIndexes = topModelSelectedIndexes();
        
        return selectModel.selected(selectedIndexes);
    }

    private int[] topModelSelectedIndexes() {
        int[] selectedIndexes = sortFilterSelectionpanel.getSelectedIndexes();
        return selectedIndexes;
    }

    public int getSelectedCount() {
//        return selectModel.getSelectedCount(topModelSelectedIndexes());
        return topModelSelectedIndexes().length;
    }
    
    //update()
    
    //add()
    
    //remove();
    
    //notes
    
    //1. Before recreates the new model, get the sort criteria and filter criteria from the overall table model
        //SortFilterSelctionPanel extends SortFilterPanel
        //SortFilterPanel has OverallTableModel
        //OverallTableModel has SortTableModel and FilterTableModel
     //2 Recreates the new SortFilterSelectionPanel and then set the old sort and filter 
        //criteria to the new model

}
