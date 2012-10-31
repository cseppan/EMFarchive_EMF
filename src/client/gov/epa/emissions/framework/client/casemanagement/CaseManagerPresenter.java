package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorView;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseViewerView;
import gov.epa.emissions.framework.client.casemanagement.sensitivity.SensitivityView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseCategory;

public interface CaseManagerPresenter {

    void display() throws EmfException;
    
    String checkParentCase(Case caseObj) throws EmfException;

    void doRemove(Case caseObj) throws EmfException;
    
    void doEdit(CaseEditorView caseView, Case caseObj) throws EmfException; 
    
    void doQA(int[] ids, CompareCaseView view) throws EmfException; 

    //void doRefresh(String nameContains) throws EmfException;
    
    //void doRefresh() throws EmfException;

    void doClose();

    void doNew(NewCaseView view);
    
    //void doSaveCopiedCase(Case newCase, String templateused) throws EmfException;
    
    void addNewCaseToTableData(Case newCase);

    void refreshWithLastCategory() throws EmfException;
    
    void doSensitivity(SensitivityView view, Case case1);
    
    void doCopyCases(int[] caseIds) throws EmfException;
    
    void doView(CaseViewerView caseView, Case caseObj) throws EmfException;
    
    CaseCategory[] getCategories() throws EmfException;
    
    CaseCategory getSelectedCategory();
    
    Case[] getCases(CaseCategory category) throws EmfException;
    
    Case[] getCases(CaseCategory category, String nameContains) throws EmfException;

    void viewCaseComparisonResult(int[] caseIds, String exportDir) throws EmfException;
    
    void viewCaseQaReports(int[] caseIds, String gridName, String sector, String repType, String exportDir) throws EmfException;
}