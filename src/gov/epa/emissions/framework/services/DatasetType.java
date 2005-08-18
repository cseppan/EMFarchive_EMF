package gov.epa.emissions.framework.services;

import java.io.Serializable;

public class DatasetType implements Serializable{
    
	/**
	 * Generated serialVersionUID
	 */
	private static final long serialVersionUID = 4694789007596096797L;
	private long id;
    private String name;
    private String description;
    private int minfiles;
    private int maxfiles;
    
    public DatasetType(){
    	super();
    }
    
    public DatasetType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getMaxfiles() {
		return maxfiles;
	}

	public void setMaxfiles(int maxfiles) {
		this.maxfiles = maxfiles;
	}

	public int getMinfiles() {
		return minfiles;
	}

	public void setMinfiles(int minfiles) {
		this.minfiles = minfiles;
	}

	public void setName(String name) {
		this.name = name;
	}

}
