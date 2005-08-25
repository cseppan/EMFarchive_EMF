package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.io.Dataset;

public interface DataServices {

	public Dataset[] getDatasets(User user);
	
}
