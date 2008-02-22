package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.cost.controlmeasure.io.CMExportPresenter;
import gov.epa.emissions.framework.client.cost.controlmeasure.io.CMExportView;
import gov.epa.emissions.framework.client.cost.controlmeasure.io.CMExportWindow;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureService;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.ui.RefreshObserver;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ControlMeasuresManagerPresenter implements RefreshObserver {

    private ControlMeasuresManagerView view;

    private EmfSession session;
    private CostYearTable costYearTable;

    public ControlMeasuresManagerPresenter(EmfSession session) {
        this.session = session;
        try {
            this.costYearTable = populateCostYearTable();
        } catch (EmfException e) {
            //
        }
    }

    public void doDisplay(ControlMeasuresManagerView view) throws EmfException {
        this.view = view;
        view.observe(this);

        view.display(new ControlMeasure[0]);
    }

    public void doClose() {
        view.disposeView();
    }

    public void doRefresh() throws EmfException {
        view.clearMessage();
        view.refresh(service().getSummaryControlMeasures(""));
    }

    private ControlMeasureService service() {
        return session.controlMeasureService();
    }
    
//View control measures
    
   public void doView(EmfConsole parent, ControlMeasure measure, DesktopManager desktopManager) throws EmfException {
        
        ControlMeasureView editor = new ViewControlMeasureWindow(parent, session, desktopManager, costYearTable);
        ControlMeasurePresenter presenter = new ViewControlMeasurePresenterImpl(
                measure, editor, session, this);
        presenter.doDisplay();
    }
    
    
    public void doEdit(EmfConsole parent, ControlMeasure measure, DesktopManager desktopManager) throws EmfException {
        
        ControlMeasureView editor = new EditControlMeasureWindow(parent, session, desktopManager, costYearTable);
        ControlMeasurePresenter presenter = new EditorControlMeasurePresenterImpl(
                measure, editor, session, this);
        presenter.doDisplay();
    }

    public void doCreateNew(EmfConsole parent, ControlMeasure measure, DesktopManager desktopManager) throws EmfException {
        ControlMeasureView window = new NewControlMeasureWindow(parent, session, desktopManager, costYearTable);
        ControlMeasurePresenter presenter = new NewControlMeasurePresenterImpl(
                measure, window, session, this);
        presenter.doDisplay();
    }
    
    public ControlMeasure[] doFilterEfficiencyAndCost(ControlMeasure[] measures) {
        //List filteredMeasures = new ArrayList();
        
        for(int i = 0; i < measures.length; i++) {
           // me
        }
        
        return null;
    }

    public void doExport(ControlMeasure[] measures, DesktopManager desktopManager, int totalMeasuers, EmfConsole parentConsole) {
        CMExportView exportView = new CMExportWindow(measures, desktopManager, totalMeasuers, session, parentConsole);
        CMExportPresenter exportPresenter = new CMExportPresenter(session);
        exportPresenter.display(exportView);
    }

    public void doSaveCopiedControlMeasure(ControlMeasure coppied, ControlMeasure original) throws EmfException {
        coppied.setAbbreviation(getRandomString());
        
        if (isDuplicate(coppied))
            throw new EmfException("A control measure with the same name or abbreviation already exists.");

        coppied.setCreator(session.user());
        coppied.setLastModifiedTime(new Date());
        service().addMeasure(coppied, getSCCs(original.getId()));
    }

    public void doSaveCopiedControlMeasure(int controlMeasureId) throws EmfException {
        service().copyMeasure(controlMeasureId, session.user());
    }

    private String getRandomString() {
        return Math.round(Math.random() * 1000000000) % 1000000000 + "";
    }
    
    private boolean isDuplicate(ControlMeasure newControlMeasure) throws EmfException {
        ControlMeasure[] controlMeasures = service().getMeasures();
        String[] cmAbbrevs = getAllAbbreviations(controlMeasures);
        for (int i = 0; i < controlMeasures.length; i++) {
            if (controlMeasures[i].getName().equals(newControlMeasure.getName()) 
                    || newControlMeasure.getAbbreviation().equals(cmAbbrevs[i]))
                return true;
        }

        return false;
    }

    private String[] getAllAbbreviations(ControlMeasure[] controlMeasures) {
        List abbrevs = new ArrayList();
        
        for (int i = 0; i < controlMeasures.length; i++)
            abbrevs.add(controlMeasures[i].getAbbreviation());
        
        return (String[])abbrevs.toArray(new String[0]);
    }
    
    public Scc[] getSCCs(int controlMeasureId) throws EmfException {
        return service().getSccs(controlMeasureId);
    }

    public ControlMeasure[] getControlMeasures(Pollutant pollutant, boolean getDetails) throws EmfException {
        if (pollutant.getName().equalsIgnoreCase("Select one"))
            return new ControlMeasure[0];

        if (pollutant.getName().equals("ALL"))
            return (getDetails ? service().getSummaryControlMeasures("") : service().getControlMeasures(""));

        return (getDetails ? service().getSummaryControlMeasures(pollutant.getId(), "") : service().getControlMeasures(pollutant.getId(), ""));
    }
    
    public ControlMeasure[] getControlMeasures(Pollutant pollutant, Scc[] sccs, boolean getDetails) throws EmfException {
        if (sccs.length==0 )
            return getControlMeasures(pollutant, getDetails);
        
        String scc="";
        for (int i=0; i<sccs.length-1; i++)
            scc +="'"+sccs[i].getCode()+"'" + ",";
        scc +="'"+sccs[sccs.length-1].getCode()+"'";
//        System.out.println(scc);
        
        String whereFilter = "cM.id in (select " + (!getDetails ? "controlMeasureId" : "control_measures_id") + " from " + (!getDetails ? "Scc" : "emf.control_measure_sccs") + " where " + (!getDetails ? "code" : "name") + " in (" + scc +")) " ;
        System.out.println(whereFilter);
        if (pollutant.getName().equals("ALL") || pollutant.getName().equalsIgnoreCase("Select one"))
            return (getDetails ? service().getSummaryControlMeasures(whereFilter) : service().getControlMeasures(whereFilter));
        return (getDetails ? service().getSummaryControlMeasures(pollutant.getId(), whereFilter) : service().getControlMeasures(pollutant.getId(), whereFilter));
        
    }

    public CostYearTable getCostYearTable() {
        return costYearTable;
    }

    private CostYearTable populateCostYearTable() throws EmfException {
        return session.controlMeasureService().getCostYearTable(2006);
    }
}
