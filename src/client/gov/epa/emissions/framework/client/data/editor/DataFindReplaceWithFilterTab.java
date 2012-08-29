package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;

public class DataFindReplaceWithFilterTab extends JPanel implements DataFindReplaceView{

    private SingleLineMessagePanel messagePanel;

    private FindReplaceViewPresenter presenter;

    private TextArea filter;
    
    private JLabel filterLabel;

    private TextArea replaceWith;
    
    private String table;
    
    private Version version;

    private JTextArea sortOrder;
    
    private ManageChangeables listOfChangeables;

    public DataFindReplaceWithFilterTab(String table, Version version, JLabel filterLabel, 
            JTextArea sortOrder, ManageChangeables listOfChangeables, SingleLineMessagePanel messagePanel){
        super.setName("Advanced");
         
        this.table = table;
        this.version = version;
        this.listOfChangeables = listOfChangeables;
        this.filterLabel = filterLabel;
        this.sortOrder = sortOrder;
        this.messagePanel = messagePanel;
    }
    
    public void display(){
        setLayout();
    }

    private void setLayout() {
        super.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); 
        panel.add(selectionPanel());
        super.add(panel, BorderLayout.CENTER);
    }
    
    private JPanel selectionPanel() {
        JPanel panel = new JPanel(new SpringLayout());
//        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Make Selections",
//                0, 0, Font.decode(""), Color.BLUE));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Row Filter ", filterLabel, panel);

        filter = new TextArea("filterValue", "", 40,3);
        filter.setToolTipText("<html>SQL Filter"
                + "<br/><br/>This is a SQL WHERE clause that is used to filter the current dataset."
                + "<br/>The expressions in the WHERE clause must contain valid column(s). "
                + "<br/><br/>Sample SQL Filter:"
                + "<br/><br/>For example to filter on a certain state and scc codes,<br/>substring(fips,1,2) = '37' and SCC_code in ('10100202','10100203')<br/>or<br/>fips like '37%' and  and SCC_code like '101002%'</html>");
        ScrollableComponent scrollableFilter = ScrollableComponent.createWithVerticalScrollBar(this.filter);
        scrollableFilter.setPreferredSize(new Dimension(350, 105));
        layoutGenerator.addLabelWidgetPair("SQL Filter ", scrollableFilter, panel);

        //        ScrollableComponent scrollableFilter = ScrollableComponent.createWithVerticalScrollBar(this.filter);
//        scrollableFilter.setPreferredSize(new Dimension(300, 105));
//        layoutGenerator.addLabelWidgetPair("SQL Filter  ", scrollableFilter, panel);

        replaceWith = new TextArea("setValues", "", 40,3);
        replaceWith.setToolTipText("<html>Set Values"
                + "<br/><br/>This is a SQL SET clause that is used to set column(s) values of the current dataset. " 
                + "<br/>The expressions in the SET clause must contain valid column(s) and separated by ','"
                + "<br/><br/>Sample SQL Filter:"
                + "<br/>set fips = '37001', <br/>ann_emis = 0.5 <html>" );
        ScrollableComponent scrollableSet = ScrollableComponent.createWithVerticalScrollBar(this.replaceWith);
        scrollableSet.setPreferredSize(new Dimension(350, 105));
        layoutGenerator.addLabelWidgetPair("Set SQL  ", scrollableSet, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 5);// xPad, yPad

        return panel;
    }
    
    public void apply() throws EmfException {
        clearMsgPanel();

        if (!validateFields())
            return;

        String findFilter = (filter.getText() == null) ?  "": filter.getText().trim();
        String setString = (replaceWith.getText() == null) ? "" : replaceWith.getText().trim();
        String rowFilter = (filterLabel.getText().equals("NO FILTER")) ? "" : filterLabel.getText().trim();

        presenter.replaceValues(table, findFilter, setString, version, rowFilter);
        presenter.applyConstraint(rowFilter, sortOrder.getText().trim());
        resetDataeditorRevisionField();
         
    }
    
    public void observe(FindReplaceViewPresenter presenter) {
        this.presenter = presenter;
    }
    
    private boolean validateFields() throws EmfException {
        String filterString = (filter.getText() == null) ?  "": filter.getText();
        String replaceString = (replaceWith.getText() == null) ? "" : replaceWith.getText().trim();
        
        if (filterString.trim().isEmpty())
            throw new EmfException("Please specify the filter value.");
        if (replaceString.isEmpty()){
            String message = "Replace field is empty, would you like to continue?";
            int selection = JOptionPane.showConfirmDialog( this, message, "Warning", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (selection == JOptionPane.NO_OPTION)   
                return false;
        }
        return true; 
    }
    
    private void resetDataeditorRevisionField() {
        boolean nofilter = filterLabel.getText().equals("NO FILTER");
        ((DataEditor)listOfChangeables).setHasReplacedValues(true);
        ((DataEditor)listOfChangeables).append2WhatField("Replaced '" + filter.getText() + "' with '" + replaceWith.getText()
                + (nofilter ? "" : " using filter '" + filterLabel.getText() + "'"));
    }
  
    private void clearMsgPanel() {
        messagePanel.clear();
    }
    
    public void setErrorMsg(String errorMsg) {
        messagePanel.setError(errorMsg);
    }

    private void setMsg(String msg) {
        messagePanel.setMessage(msg);
    }
    
}

    