package gov.epa.emissions.framework.client.cost.controlmeasure;


public class SCCSelectionPresenter {

    private SCCTableData tableData;

    private CMSCCTab parentView;

    public SCCSelectionPresenter(CMSCCTab parentView, SCCSelectionView view) {
        this.parentView = parentView;
    }

    public void display(SCCSelectionView view) throws Exception {
        Sccs sccs = new Sccs();
        SccFileReader reader = new SccFileReader("config/ref/delimited/scc.txt", sccs);
        reader.read();
        view.observe(this);
        this.tableData = new SCCTableData(sccs.getSccs());
        view.display(tableData);

    }

    public void doAdd(Scc[] sccs) {
        parentView.add(sccs);
    }

}
