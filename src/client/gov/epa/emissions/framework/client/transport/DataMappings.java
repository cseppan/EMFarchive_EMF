package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.data.Country;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.ExternalSource;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.data.SectorCriteria;
import gov.epa.emissions.commons.data.SourceGroup;
import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.VersionedRecord;
import gov.epa.emissions.commons.io.ColumnMetaData;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.basic.AccessLog;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureClass;
import gov.epa.emissions.framework.services.cost.ControlMeasureEquation;
import gov.epa.emissions.framework.services.cost.ControlMeasureMonth;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.ControlStrategyMeasure;
import gov.epa.emissions.framework.services.cost.EquationType;
import gov.epa.emissions.framework.services.cost.EquationTypeVariable;
import gov.epa.emissions.framework.services.cost.LightControlMeasure;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyConstraint;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.cost.data.ControlStrategyResultsSummary;
import gov.epa.emissions.framework.services.cost.data.ControlTechnology;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import gov.epa.emissions.framework.services.cost.data.SumEffRec;
import gov.epa.emissions.framework.services.data.DatasetNote;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.IntendedUse;
import gov.epa.emissions.framework.services.data.Note;
import gov.epa.emissions.framework.services.data.NoteType;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.services.editor.DataAccessToken;
import gov.epa.emissions.framework.services.editor.Revision;

import javax.xml.namespace.QName;

import org.apache.axis.client.Call;

public class DataMappings extends Mappings {

    public void register(Call call) {
        registerBeans(call);
        registerArrays(call);
    }

    private void registerBeans(Call call) {
        bean(call, User.class, user());

        bean(call, EmfDataset.class, dataset());
        bean(call, DatasetType.class, datasetType());

        bean(call, InternalSource.class, "InternalSource");
        bean(call, ExternalSource.class, "ExternalSource");

        bean(call, Keyword.class, keyword());
        bean(call, KeyVal.class, "KeyVal");

        bean(call, Country.class, country());
        bean(call, Project.class, project());
        bean(call, Region.class, region());
        bean(call, IntendedUse.class, intendeduse());

        bean(call, Sector.class, sector());
        bean(call, SectorCriteria.class, "SectorCriteria");

        bean(call, Page.class, page());
        bean(call, VersionedRecord.class, record());
        bean(call, Version.class, version());
        bean(call, DataAccessToken.class, dataAccessToken());
        bean(call, ChangeSet.class, changeset());

        bean(call, Status.class, status());
        bean(call, AccessLog.class, log());

        bean(call, Note.class, note());
        bean(call, DatasetNote.class, datasetNote());
        bean(call, NoteType.class, notetype());
        bean(call, Revision.class, revision());
        bean(call, Pollutant.class, pollutant());
        bean(call, SourceGroup.class, sourceGroup());

        bean(call, QAStepTemplate.class, qaStepTemplate());
        bean(call, QAStep.class, qaStep());
        bean(call, QAProgram.class, program());
        bean(call, QAStepResult.class, qaStepResult());

        bean(call, TableMetadata.class, tablemetadata());
        bean(call, ColumnMetaData.class, columnmetadata());

        bean(call, EmfFileInfo.class, emfFileInfo());

        controlBeans(call);
    }

    private void controlBeans(Call call) {
        bean(call, ControlMeasure.class, controlMeasure());
        bean(call, ControlStrategyMeasure.class, controlStrategyMeasure());
        bean(call, LightControlMeasure.class, lightControlMeasure());
        bean(call, ControlTechnology.class, controlTechnology());
        bean(call, ControlStrategy.class, controlStrategy());
        bean(call, ControlStrategyInputDataset.class, controlStrategyInputDataset());
        bean(call, StrategyType.class, strategyType());
        bean(call, ControlStrategyResult.class, controlStrategyResult());
        bean(call, EfficiencyRecord.class, efficiencyRecord());
        bean(call, SumEffRec.class, sumEffRec());
        bean(call, Scc.class, scc());
        bean(call, ControlStrategyResultsSummary.class, controlStrategyResultsSummary());
        bean(call, StrategyResultType.class, strategyResultType());
        bean(call, CostYearTable.class, costYearTable());
        bean(call, ControlMeasureClass.class, controlMeasureClass());
        bean(call, ControlStrategyConstraint.class, controlStrategyConstraint());
        bean(call, EquationType.class, equationType());
        bean(call, EquationTypeVariable.class, equationTypeVariable());
        bean(call, ControlMeasureEquation.class, controlMeasureEquation());
        bean(call, ControlMeasureMonth.class, controlMeasureMonth());
    }

    private void registerArrays(Call call) {
        array(call, User[].class, users());

        array(call, EmfDataset[].class, datasets());
        array(call, DatasetType[].class, datasetTypes());

        array(call, ExternalSource[].class, "ExternalSources");
        array(call, InternalSource[].class, "InternalSources");

        array(call, Keyword[].class, keywords());
        array(call, KeyVal[].class, "KeyVals");

        array(call, Country[].class, countries());
        array(call, Project[].class, projects());
        array(call, Region[].class, regions());
        array(call, IntendedUse[].class, intendeduses());

        array(call, Sector[].class, sectors());
        array(call, SectorCriteria[].class, "SectorCriterias");

        array(call, Page[].class, pages());
        array(call, VersionedRecord[].class, records());
        array(call, Version[].class, versions());

        array(call, Status[].class, statuses());
        array(call, AccessLog[].class, logs());

        array(call, Note[].class, notes());
        array(call, DatasetNote[].class, datasetNotes());
        array(call, NoteType[].class, notetypes());
        array(call, Revision[].class, revisions());
        array(call, Pollutant[].class, pollutants());
        array(call, SourceGroup[].class, sourceGroups());
        array(call, ColumnMetaData[].class, columnmetadatas());

        array(call, QAStepTemplate[].class, qaStepTemplates());
        array(call, QAStep[].class, qaSteps());
        array(call, QAProgram[].class, programs());

        array(call, ControlMeasure[].class, controlMeasures());
        array(call, ControlStrategyMeasure[].class, controlStrategyMeasures());
        array(call, LightControlMeasure[].class, lightControlMeasures());
        
        array(call, ControlTechnology[].class, controlTechnologies());
        array(call, ControlStrategy[].class, controlStrategies());
        array(call, ControlStrategyInputDataset[].class, controlStrategyInputDatasets());
        array(call, StrategyType[].class, strategyTypes());
        array(call, ControlStrategyResult[].class, controlStrategyResults());
        array(call, EfficiencyRecord[].class, efficiencyRecords());
        array(call, SumEffRec[].class, sumEffRecs());
        array(call, Scc[].class, sccs());
        array(call, ControlMeasureClass[].class, controlMeasureClasses());
        array(call, EquationType[].class, equationTypes());
        array(call, EquationTypeVariable[].class, equationTypeVariables());
        array(call, ControlMeasureEquation[].class, controlMeasureEquations());
        array(call, ControlMeasureMonth[].class, controlMeasureMonths());
        
        array(call, EmfFileInfo[].class, emfFileInfos());
    }

    public QName logs() {
        return qname("AllAccessLogs");
    }

    public QName log() {
        return qname("AccessLog");
    }

    public QName datasetTypes() {
        return qname("DatasetTypes");
    }

    public QName sector() {
        return qname("Sector");
    }

    public QName sectors() {
        return qname("Sectors");
    }

    public QName region() {
        return qname("Region");
    }

    public QName intendeduse() {
        return qname("IntendedUse");
    }

    public QName project() {
        return qname("Project");
    }

    public QName intendeduses() {
        return qname("IntendedUses");
    }

    public QName regions() {
        return qname("Regions");
    }

    public QName projects() {
        return qname("Projects");
    }

    public QName statuses() {
        return qname("Statuses");
    }

    public QName status() {
        return qname("Status");
    }

    public QName user() {
        return qname("User");
    }

    public QName users() {
        return qname("Users");
    }

    public QName keywords() {
        return qname("Keywords");
    }

    public QName keyword() {
        return qname("Keyword");
    }

    public QName countries() {
        return qname("Countries");
    }

    public QName country() {
        return qname("Country");
    }

    public QName datasetType() {
        return qname("DatasetType");
    }

    public QName dataset() {
        return qname("EmfDataset");
    }

    public QName datasets() {
        return qname("EmfDatasets");
    }

    public QName page() {
        return qname("Page");
    }

    public QName record() {
        return qname("Record");
    }

    public QName pages() {
        return qname("Pages");
    }

    public QName records() {
        return qname("Records");
    }

    public QName version() {
        return qname("Version");
    }

    public QName versions() {
        return qname("Versions");
    }

    public QName dataAccessToken() {
        return qname("DataAccessToken");
    }

    public QName changeset() {
        return qname("ChangeSet");
    }

    public QName notetype() {
        return qname("NoteType");
    }

    public QName notetypes() {
        return qname("NoteTypes");
    }

    public QName note() {
        return qname("Note");
    }

    public QName datasetNote() {
        return qname("DatasetNote");
    }

    public QName notes() {
        return qname("Notes");
    }

    public QName datasetNotes() {
        return qname("DatasetNotes");
    }

    public QName revision() {
        return qname("Revision");
    }

    public QName revisions() {
        return qname("Revisions");
    }

    public QName qaStepTemplate() {
        return qname("QAStepTemplate");
    }

    public QName qaStepTemplates() {
        return qname("QAStepTemplates");
    }

    public QName qaStep() {
        return qname("QAStep");
    }

    public QName qaSteps() {
        return qname("QASteps");
    }

    public QName tablemetadata() {
        return qname("TableMetadata");
    }

    public QName columnmetadata() {
        return qname("ColumnMetaData");
    }

    public QName columnmetadatas() {
        return qname("ColumnMetaDatas");
    }

    public QName controlMeasure() {
        return qname("ControlMeasure");
    }

    public QName controlStrategyMeasure() {
        return qname("ControlStrategyMeasure");
    }

    public QName lightControlMeasure() {
        return qname("LightControlMeasure");
    }

    public QName controlMeasures() {
        return qname("ControlMeasures");
    }

    public QName controlStrategyMeasures() {
        return qname("ControlStrategyMeasures");
    }
    
    public QName lightControlMeasures() {
        return qname("LightControlMeasures");
    }

    public QName controlMeasureClass() {
        return qname("ControlMeasureClass");
    }
    
    public QName controlMeasureClasses() {
        return qname("ControlMeasureClasses");
    }

    public QName equationTypes() {
        return qname("EquationTypes");
    }

    public QName equationType() {
        return qname("EquationType");
    }

    public QName equationTypeVariables() {
        return qname("EquationTypeVariables");
    }

    public QName equationTypeVariable() {
        return qname("EquationTypeVariable");
    }

    public QName controlMeasureEquations() {
        return qname("ControlMeasureEquations");
    }

    public QName controlMeasureEquation() {
        return qname("ControlMeasureEquation");
    }

    public QName controlMeasureMonths() {
        return qname("ControlMeasureMonths");
    }

    public QName controlMeasureMonth() {
        return qname("ControlMeasureMonth");
    }

    public QName controlStrategy() {
        return qname("ControlStrategy");
    }

    public QName controlStrategies() {
        return qname("ControlStrategies");
    }

    public QName controlStrategyInputDataset() {
        return qname("ControlStrategyInputDataset");
    }

    public QName controlStrategyInputDatasets() {
        return qname("ControlStrategyInputDatasets");
    }

    public QName strategyType() {
        return qname("StrategyType");
    }

    public QName strategyTypes() {
        return qname("StrategyTypes");
    }

    public QName efficiencyRecord() {
        return qname("EfficiencyRecord");
    }

    public QName efficiencyRecords() {
        return qname("EfficiencyRecords");
    }

    public QName sumEffRec() {
        return qname("SumEffRec");
    }

    public QName sumEffRecs() {
        return qname("SumEffRecs");
    }

    public QName scc() {
        return qname("scc");
    }

    public QName sccs() {
        return qname("sccs");
    }

    public QName controlStrategyResult() {
        return qname("ControlStrategyResult");
    }

    public QName controlStrategyConstraint() {
        return qname("ControlStrategyConstraint");
    }

    public QName controlStrategyResults() {
        return qname("ControlStrategyResults");
    }

    public QName controlStrategyResultsSummary() {
        return qname("ControlStrategyResultsSummary");
    }

    public QName strategyResultType() {
        return qname("StrategyResultType");
    }

    public QName pollutant() {
        return qname("Pollutant");
    }

    public QName pollutants() {
        return qname("Pollutants");
    }

    public QName sourceGroup() {
        return qname("SourceGroup");
    }

    public QName sourceGroups() {
        return qname("SourceGroups");
    }

    public QName controlTechnology() {
        return qname("ControlTechnology");
    }

    public QName controlTechnologies() {
        return qname("ControlTechnologies");
    }

    public QName costYearTable() {
        return qname("CostYearTable");
    }

    public QName program() {
        return qname("QAProgram");
    }

    public QName programs() {
        return qname("QAPrograms");
    }

    public QName qaStepResult() {
        return qname("QAStepResult");
    }
    
    public QName emfFileInfo() {
        return qname("EmfFileInfo");
    }
    
    public QName emfFileInfos() {
        return qname("EmfFileInfos");
    }
    
}
