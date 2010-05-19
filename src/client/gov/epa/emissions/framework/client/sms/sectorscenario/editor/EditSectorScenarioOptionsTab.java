package gov.epa.emissions.framework.client.sms.sectorscenario.editor;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.sms.SectorScenario;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SpringLayout;

public class EditSectorScenarioOptionsTab extends JPanel implements EditSectorScenarioOptionsTabView {

//    private TextField countyFileTextField;

    private MessagePanel messagePanel;
    
    protected SectorScenario sectorScenario;
    
    protected JCheckBox annoInvEECS;
    
    //protected JCheckBox shouldDoubleCount;
    
    private JRadioButton addEECSCol;
    
    private JRadioButton useEECSFromInv;
    
    private JRadioButton fillMissEECS;
    
    private JRadioButton mappedToOne;
    
    private JRadioButton mappedToAll;
    
    private ButtonGroup buttonGroup;
    
    private ButtonGroup doubleButtonGroup;
    
    protected EmfSession session;
    
    protected EmfConsole parentConsole;

    //private ManageChangeables changeablesList;

    private EditSectorScenarioOptionsTabPresenter presenter;
    
    //private EditSectorScenarioPresenter editPresenter;
    

    public EditSectorScenarioOptionsTab(SectorScenario sectorScenario, 
            MessagePanel messagePanel, EmfConsole parentConsole, 
            EmfSession session, DesktopManager desktopManager){
        super.setName("sectorscenarioinputs");
        
        this.messagePanel = messagePanel;
        this.sectorScenario = sectorScenario;
        this.parentConsole = parentConsole;
        this.session = session;
        //this.editPresenter = editPresenter; 
    }


    public void observe(EditSectorScenarioOptionsTabPresenter presenter){
        this.presenter = presenter;
    }

    public void display() {
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(getBorderedPanel(createmMainSection(), ""), BorderLayout.NORTH);
        panel.add(doubleCount());
        
        setLayout(new BorderLayout(5, 5));
        add(panel,BorderLayout.NORTH);
        // mainPanel.add(buttonPanel(), BorderLayout.SOUTH);
        //mainPanel = new JPanel(new BorderLayout(10, 10));
        //buildSortFilterPanel();
        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.add(radioPanel(), BorderLayout.CENTER);
        this.add(buildListPanel(), BorderLayout.SOUTH);
        
    }
    
    private JPanel radioPanel() {
        addEECSCol = new JRadioButton("add/overwrite eecs column");
        useEECSFromInv = new JRadioButton("use eecs from inventory");
        fillMissEECS = new JRadioButton ("fill-in missing eecs within inventory");
        buttonGroup = new ButtonGroup();
        buttonGroup.add(addEECSCol);     
        buttonGroup.add(useEECSFromInv);
        buttonGroup.add(fillMissEECS);
        buttonGroup.setSelected(addEECSCol.getModel(), true);
        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS));
        radioPanel.add(new JLabel(" "));
        radioPanel.add(new JLabel("annotate eecs options"));
        radioPanel.add(addEECSCol);
        radioPanel.add(useEECSFromInv);
        radioPanel.add(fillMissEECS);
        
        return radioPanel;
    }
        
    private JPanel getBorderedPanel(JPanel component, String border) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new Border(border));
        panel.add(component);

        return panel;
    }
    
    private JPanel createmMainSection() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        JPanel panel = new JPanel(new SpringLayout());
        
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        
        layoutGenerator.addLabelWidgetPair("Annotate inventory with eecs:", annoInv(), panel);
        //layoutGenerator.addLabelWidgetPair("Double count options:", doubleCount(), panel);
        //layoutGenerator.addLabelWidgetPair("Annotate eecs options:", regions(), panel); 
        layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad
        mainPanel.add(panel);
        return mainPanel;
    }
    
    private JCheckBox annoInv() {
        annoInvEECS = new JCheckBox(" ", null, sectorScenario.getAnnotateInventoryWithEECS() != null ? sectorScenario.getAnnotateInventoryWithEECS() : true);
        return annoInvEECS;
    } 
    
    private JPanel doubleCount() {
        mappedToOne = new JRadioButton("emissions mapped to one and only one sector");
        mappedToAll = new JRadioButton("emissions mapped to all matching sectors (double count)");
        doubleButtonGroup = new ButtonGroup();
        doubleButtonGroup.add(mappedToOne);     
        doubleButtonGroup.add(mappedToAll);
        doubleButtonGroup.setSelected(mappedToOne.getModel(), true);
       
        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS));
        radioPanel.add(new JLabel(" "));
        radioPanel.add(new JLabel("Double count option: "));
        radioPanel.add(mappedToOne);
        radioPanel.add(mappedToAll);
        
        return radioPanel;
    } 

    private JPanel buildListPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        //panel.setBorder(new Border("Inventories to Process"));
        //SortFilterSelectionPanel sfpanel = sortFilterPanel();
        panel.add(listPanel(), BorderLayout.CENTER);
        presenter.getClass();
        
        messagePanel.clear();
        return panel; 
    }
    
    private JPanel listPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        return panel;
    }


    
    private void refresh(){
//        table.refresh(tableData);
//        panelRefresh();
    }
    
 
    public void save(SectorScenario sectorScenario){
        sectorScenario.setAnnotateInventoryWithEECS(annoInvEECS.isSelected());
        if (doubleButtonGroup.getSelection().equals(mappedToOne.getModel()))
            sectorScenario.setShouldDoubleCount(false);
        if (doubleButtonGroup.getSelection().equals(mappedToAll.getModel()))
            sectorScenario.setShouldDoubleCount(true);
        if (buttonGroup.getSelection().equals(addEECSCol.getModel())){
            Short choice = 1; 
            sectorScenario.setAnnotatingEecsOption(choice);
        }
        if (buttonGroup.getSelection().equals(useEECSFromInv.getModel())){
            Short choice = 2; 
            sectorScenario.setAnnotatingEecsOption(choice);
        }
        if (buttonGroup.getSelection().equals(fillMissEECS.getModel())){
            Short choice = 3; 
            sectorScenario.setAnnotatingEecsOption(choice);
        }
    }

    public void refresh(SectorScenario sectorScenario) {
        //tableData.add(sectorScenario.getInventories());
        refresh();
    }


}