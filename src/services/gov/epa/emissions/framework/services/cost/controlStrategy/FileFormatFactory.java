package gov.epa.emissions.framework.services.cost.controlStrategy;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.orl.ORLNonPointFileFormat;
import gov.epa.emissions.commons.io.orl.ORLNonRoadFileFormat;
import gov.epa.emissions.commons.io.orl.ORLOnRoadFileFormat;
import gov.epa.emissions.commons.io.orl.ORLPointFileFormat;
import gov.epa.emissions.commons.io.temporal.VersionedTableFormat;
import gov.epa.emissions.framework.services.EmfDbServer;

public class FileFormatFactory {

    private SqlDataTypes types;
    private DbServer dbServer;

    public FileFormatFactory() throws Exception {
        sqlTypes();
    }

    public FileFormatFactory(DbServer dbServer) throws Exception {
        this.dbServer = dbServer;
        this.types = dbServer.getSqlDataTypes();
    }

    private void sqlTypes() throws Exception {
//        EmfDbServer dbServer = new EmfDbServer();
        dbServer = new EmfDbServer();
        dbServer.disconnect();
        this.types = dbServer.getSqlDataTypes();
    }

    public TableFormat tableFormat(DatasetType type) throws Exception {
        if (type.getName().startsWith("ORL Nonpoint"))
            return new VersionedTableFormat(new ORLNonPointFileFormat(types), types);

        if (type.getName().startsWith("ORL Nonroad"))
            return new VersionedTableFormat(new ORLNonRoadFileFormat(types), types);

        if (type.getName().startsWith("ORL Onroad"))
            return new VersionedTableFormat(new ORLOnRoadFileFormat(types), types);

        if (type.getName().startsWith("ORL Point"))
            return new VersionedTableFormat(new ORLPointFileFormat(types), types);

        throw new Exception("The dataset type '" + type.getName() + "' is not supported for inventory output");
    }

}
