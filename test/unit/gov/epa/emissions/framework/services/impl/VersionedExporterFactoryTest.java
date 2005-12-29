package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Exporter;
import gov.epa.emissions.commons.io.orl.ORLOnRoadExporter;
import gov.epa.emissions.commons.io.temporal.TemporalProfileExporter;
import gov.epa.emissions.framework.services.EmfDataset;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class VersionedExporterFactoryTest extends MockObjectTestCase {

    public void testShouldBeAbleCreateOrlExporter() throws Exception {
        VersionedExporterFactory factory = new VersionedExporterFactory(null, null);

        DatasetType datasetType = new DatasetType();
        datasetType.setExporterClassName(ORLOnRoadExporter.class.getName());
        EmfDataset dataset = new EmfDataset();
        dataset.setDatasetType(datasetType);

        Exporter exporter = factory.create(dataset);

        assertEquals(datasetType.getExporterClassName(), exporter.getClass().getName());
    }

    public void testShouldBeAbleCreateTemporalProfileExporter() throws Exception {
        Mock types = mock(SqlDataTypes.class);
        types.stubs().method("intType").withNoArguments().will(returnValue("integer"));

        VersionedExporterFactory factory = new VersionedExporterFactory(null, (SqlDataTypes) types.proxy());

        DatasetType datasetType = new DatasetType();
        datasetType.setExporterClassName(TemporalProfileExporter.class.getName());
        EmfDataset dataset = new EmfDataset();
        dataset.setDatasetType(datasetType);

        Exporter exporter = factory.create(dataset);

        assertEquals(datasetType.getExporterClassName(), exporter.getClass().getName());
    }
}
