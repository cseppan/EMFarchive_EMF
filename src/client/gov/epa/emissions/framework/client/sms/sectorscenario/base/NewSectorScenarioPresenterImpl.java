package gov.epa.emissions.framework.client.sms.sectorscenario.base;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.sms.sectorscenario.SectorScenarioManagerPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.SectorScenarioService;
import gov.epa.emissions.framework.services.sms.SectorScenario;

import java.util.Date;

public class NewSectorScenarioPresenterImpl implements NewSectorScenarioPresenter{

    protected EmfSession session;

    private NewSectorScenarioView view;
    
    protected SectorScenarioManagerPresenter managerPresenter;
    
    public NewSectorScenarioPresenterImpl(NewSectorScenarioView view, EmfSession session, 
            SectorScenarioManagerPresenter managerPresenter) {
        this.session = session;
        this.view = view;
        this.managerPresenter = managerPresenter;
    }
    
    public NewSectorScenarioPresenterImpl(EmfSession session, 
            SectorScenarioManagerPresenter managerPresenter) {
        this.session = session;
        this.managerPresenter = managerPresenter;
    }

    public void display() throws EmfException {
        view.observe(this);
        view.display();
    }
    
    public EmfSession getSession(){
        return session; 
    }
    
    public void addSectorScenario(SectorScenario sectorScenario) throws EmfException {
        validateNameAndAbbre(sectorScenario);
        sectorScenario.setLastModifiedDate(new Date());
        service().addSectorScenario(sectorScenario);
        //SectorScenario loaded = service().getById(id);
        managerPresenter.addNewSSToTableData(sectorScenario);
    }
       
    protected void validateNameAndAbbre(SectorScenario sectorScenario) throws EmfException {
        // emptyName
        String name = sectorScenario.getName();
        String abbre = sectorScenario.getAbbreviation();
        if (name.trim().equals(""))
            throw new EmfException("Empty string is not allowed for the name.");

        if (abbre.trim().equals(""))
            throw new EmfException("Empty string is not allowed for the abbre.");

        if (isDuplicate(name))
            throw new EmfException("A Control Strategy named '" + name + "' already exists.");
    }

    protected boolean isDuplicate(String name) throws EmfException {
        int id = service().isDuplicateName(name);
        return (id != 0);
    }
    
    protected SectorScenarioService service(){    
        return session.sectorScenarioService();
    }
}