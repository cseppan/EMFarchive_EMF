package gov.epa.emissions.commons.io;

import java.io.Serializable;

public class DatasetType implements Serializable{
    
	/**
	 * Generated serialVersionUID
	 */
	private static final long serialVersionUID = 4694789007596096797L;
	private long datasettypeid;
    private String name;
    private String description;
    private int minfiles;
    private int maxfiles;
    private String uid=null;
    
    /**
     * @return Returns the uid.
     */
    public String getUid() {
        return uid;
    }
    /**
     * @param uid The uid to set.
     */
    public void setUid(String uid) {
        this.uid = uid;
    }
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

    public String toString() {
        return getName();
    }
	public long getDatasettypeid() {
		return datasettypeid;
	}
	public void setDatasettypeid(long datasettypeid) {
		this.datasettypeid = datasettypeid;
	}
}
