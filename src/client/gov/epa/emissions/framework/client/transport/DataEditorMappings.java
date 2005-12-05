package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.VersionedRecord;
import gov.epa.emissions.framework.services.EditToken;

import javax.xml.namespace.QName;

import org.apache.axis.client.Call;

public class DataEditorMappings extends Mappings {

    public void register(Call call) {
        mapper.registerBeanMapping(call, Page.class, page());
        mapper.registerArrayMapping(call, Page[].class, pages());

        mapper.registerBeanMapping(call, VersionedRecord.class, record());
        mapper.registerArrayMapping(call, VersionedRecord[].class, records());

        mapper.registerBeanMapping(call, Version.class, version());
        mapper.registerArrayMapping(call, Version[].class, versions());

        mapper.registerBeanMapping(call, EditToken.class, editToken());
        mapper.registerBeanMapping(call, ChangeSet.class, changeset());

        mapper.registerMappingForTable(call);
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

    public QName editToken() {
        return qname("EditToken");
    }

    public QName changeset() {
        return qname("ChangeSet");
    }

}
