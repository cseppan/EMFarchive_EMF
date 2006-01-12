package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.VersionedImporter;
import gov.epa.emissions.commons.io.orl.ORLOnRoadImporter;
import gov.epa.emissions.commons.io.temporal.TemporalProfileImporter;
import gov.epa.emissions.framework.services.EmfDataset;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class VersionedImporterFactoryTest extends MockObjectTestCase {

    public void testShouldBeAbleCreateOrlImporterr() throws Exception {
        Mock types = mock(SqlDataTypes.class);
        types.stubs().method(ANYTHING).withAnyArguments().will(returnValue(""));

        VersionedImporterFactory factory = new VersionedImporterFactory(null, (SqlDataTypes) types.proxy());

        DatasetType datasetType = new DatasetType();
        datasetType.setImporterClassName(ORLOnRoadImporter.class.getName());
        EmfDataset dataset = new EmfDataset();
        dataset.setDatasetType(datasetType);
        dataset.setName("name");

        Importer importer = factory.create(dataset, null, "file");

        assertEquals(VersionedImporter.class.getName(), importer.getClass().getName());
    }

    public void testShouldBeAbleCreateTemporalProfileImporter() throws Exception {
        Mock types = mock(SqlDataTypes.class);
        types.stubs().method(ANYTHING).withAnyArguments().will(returnValue(""));

        VersionedImporterFactory factory = new VersionedImporterFactory(null, (SqlDataTypes) types.proxy());

        DatasetType datasetType = new DatasetType();
        datasetType.setImporterClassName(TemporalProfileImporter.class.getName());
        EmfDataset dataset = new EmfDataset();
        dataset.setDatasetType(datasetType);

        Importer exporter = factory.create(dataset, null, "file");

        assertEquals(VersionedImporter.class.getName(), exporter.getClass().getName());
    }
}
