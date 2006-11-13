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
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.ui.RefreshObserver;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ControlMeasuresManagerPresenter implements RefreshObserver {

    private ControlMeasuresManagerView view;

    private EmfSession session;

    public ControlMeasuresManagerPresenter(EmfSession session) {
        this.session = session;
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
        view.refresh(service().getMeasures());
    }

    private ControlMeasureService service() {
        return session.controlMeasureService();
    }

    public void doEdit(EmfConsole parent, ControlMeasure measure, DesktopManager desktopManager) throws EmfException {
        
        ControlMeasureView editor = new EditControlMeasureWindow(parent, session, desktopManager);
        ControlMeasurePresenter presenter = new EditorControlMeasurePresenterImpl(
                measure, editor, session, this);
        presenter.doDisplay();
    }

    public void doCreateNew(EmfConsole parent, ControlMeasure measure, DesktopManager desktopManager) throws EmfException {
        ControlMeasureView window = new NewControlMeasureWindow(parent, session, desktopManager);
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

    public void doExport(ControlMeasure[] measures, DesktopManager desktopManager, int totalMeasuers) {
        CMExportView exportView = new CMExportWindow(measures, desktopManager, totalMeasuers);
        CMExportPresenter exportPresenter = new CMExportPresenter(session);
        exportPresenter.display(exportView);
    }

    public void doSaveCopiedControlMeasure(ControlMeasure coppied, ControlMeasure original) throws EmfException {
        coppied.setAbbreviation(getRandomString());
        
        if (isDuplicate(coppied))
            throw new EmfException("A control measure with the same name or abbreviation already exists.");

        coppied.setCreator(session.user());
        coppied.setLastModifiedTime(new Date());
        service().addMeasure(coppied, getSCCs(original));
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
    
    public Scc[] getSCCs(ControlMeasure cm) throws EmfException {
        return service().getSccs(cm);
    }

    public ControlMeasure[] getControlMeasures(Pollutant pollutant) throws EmfException {
        if (pollutant.getName().equalsIgnoreCase("Select one"))
            return new ControlMeasure[0];
        
        if (pollutant.getName().equals("ALL"))
            return service().getMeasures();
        
        return service().getMeasures(pollutant);
    }

}
