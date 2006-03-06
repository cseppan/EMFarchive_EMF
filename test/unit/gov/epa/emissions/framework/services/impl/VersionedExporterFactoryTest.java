package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Exporter;
import gov.epa.emissions.commons.io.orl.ORLOnRoadExporter;
import gov.epa.emissions.commons.io.temporal.TemporalProfileExporter;
import gov.epa.emissions.framework.services.EmfDataset;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class VersionedExporterFactoryTest extends MockObjectTestCase {

    public void testShouldBeAbleCreateOrlExporter() throws Exception {
        VersionedExporterFactory factory = new VersionedExporterFactory(dbServer(), null);

        DatasetType datasetType = new DatasetType();
        datasetType.setExporterClassName(ORLOnRoadExporter.class.getName());
        EmfDataset dataset = new EmfDataset();
        dataset.setDatasetType(datasetType);

        Version version = new Version();
        version.setVersion(0);

        Exporter exporter = factory.create(dataset, version);

        assertEquals(datasetType.getExporterClassName(), exporter.getClass().getName());
    }

    public void testShouldBeAbleCreateTemporalProfileExporter() throws Exception {
        Mock types = mock(SqlDataTypes.class);
        types.stubs().method("intType").withNoArguments().will(returnValue("integer"));

        VersionedExporterFactory factory = new VersionedExporterFactory(dbServer(), (SqlDataTypes) types.proxy());

        DatasetType datasetType = new DatasetType();
        datasetType.setExporterClassName(TemporalProfileExporter.class.getName());
        EmfDataset dataset = new EmfDataset();
        dataset.setDatasetType(datasetType);

        Version version = new Version();
        version.setVersion(0);

        Exporter exporter = factory.create(dataset, version);

        assertEquals(datasetType.getExporterClassName(), exporter.getClass().getName());
    }
    
    private DbServer dbServer() {
        Mock datasource = mock(Datasource.class);
        Mock dbServer = mock(DbServer.class);
        dbServer.stubs().method("getEmissionsDatasource").withAnyArguments().will(returnValue(datasource.proxy()));
        return (DbServer) dbServer.proxy();
    }
}
