package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.data.Country;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.ExternalSource;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.data.SectorCriteria;
import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.VersionedRecord;
import gov.epa.emissions.commons.io.ColumnMetaData;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.basic.AccessLog;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.cost.data.ControlMeasureCost;
import gov.epa.emissions.framework.services.cost.data.ControlMeasureEfficiency;
import gov.epa.emissions.framework.services.cost.data.CostRecord;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.IntendedUse;
import gov.epa.emissions.framework.services.data.Note;
import gov.epa.emissions.framework.services.data.NoteType;
import gov.epa.emissions.framework.services.data.QAStep;
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
        bean(call, NoteType.class, notetype());
        bean(call, Revision.class, revision());

        bean(call, QAStepTemplate.class, qaStepTemplate());
        bean(call, QAStep.class, qaStep());

        bean(call, TableMetadata.class, tablemetadata());
        bean(call, ColumnMetaData.class, columnmetadata());

        controlBeans(call);
    }

    private void controlBeans(Call call) {
        bean(call, ControlMeasure.class, controlMeasure());
        bean(call, ControlStrategy.class, controlStrategy());
        bean(call, ControlMeasureCost.class, controlMeasureCost());
        bean(call, CostRecord.class, costRecord());
        bean(call, ControlMeasureEfficiency.class, controlMeasureEfficiency());
        bean(call, EfficiencyRecord.class, efficiencyRecord());
        bean(call, Scc.class, scc());
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
        array(call, NoteType[].class, notetypes());
        array(call, Revision[].class, revisions());
        array(call, ColumnMetaData[].class, columnmetadatas());

        array(call, QAStepTemplate[].class, qaStepTemplates());
        array(call, QAStep[].class, qaSteps());

        array(call, ControlMeasure[].class, controlMeasures());
        array(call, ControlStrategy[].class, controlStrategies());
        array(call, CostRecord[].class, costRecords());
        array(call, ControlMeasureCost[].class, controlMeasureCosts());
        array(call, EfficiencyRecord[].class, efficiencyRecords());
        array(call, ControlMeasureEfficiency[].class, controlMeasureEfficiencys());
        array(call, Scc[].class, sccs());
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

    public QName notes() {
        return qname("Notes");
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

    public QName controlMeasures() {
        return qname("ControlMeasures");
    }

    public QName controlStrategy() {
        return qname("ControlStrategy");
    }

    public QName controlStrategies() {
        return qname("ControlStrategies");
    }

    public QName costRecord() {
        return qname("CostRecord");
    }

    public QName costRecords() {
        return qname("CostRecords");
    }

    public QName controlMeasureCost() {
        return qname("ControlMeasureCost");
    }

    private QName controlMeasureCosts() {
        return qname("ControlMeasureCosts");
    }

    private QName efficiencyRecord() {
        return qname("EfficiencyRecord");
    }

    private QName efficiencyRecords() {
        return qname("EfficiencyRecords");
    }

    private QName controlMeasureEfficiency() {
        return qname("ControlMeasureEfficiency");
    }

    private QName controlMeasureEfficiencys() {
        return qname("ControlMeasureEfficiencys");
    }

    public QName scc() {
        return qname("scc");
    }

    public QName sccs() {
        return qname("sccs");
    }

}
