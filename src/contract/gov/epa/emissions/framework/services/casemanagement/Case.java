package gov.epa.emissions.framework.services.casemanagement;

public class Case implements Comparable {

    private int id;

    private String name;

    private Abbreviation abbreviation;

    private AirQualityModel airQualityModel;

    private CaseCategory caseCategory;

    private EmissionsYear emissionsYear;

    private Grid grid;

    private MeteorlogicalYear meteorlogicalYear;

    private Speciation speciation;

    private String description;

    /*
     * Default constructor needed for hibernate and axis serialization
     */
    public Case() {
        super();
    }

    public Case(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean equals(Object other) {
        if (other == null || !(other instanceof Case))
            return false;

        return ((Case) other).name.equals(this.name);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        return getName();
    }

    public int compareTo(Object other) {
        return name.compareTo(((Case) other).getName());
    }

    public void setAbbreviation(Abbreviation abbreviation) {
        this.abbreviation = abbreviation;
    }

    public Abbreviation getAbbreviation() {
        return abbreviation;
    }

    public void setAirQualityModel(AirQualityModel airQualityModel) {
        this.airQualityModel = airQualityModel;
    }

    public AirQualityModel getAirQualityModel() {
        return airQualityModel;
    }

    public void setCaseCategory(CaseCategory caseCategory) {
        this.caseCategory = caseCategory;
    }

    public CaseCategory getCaseCategory() {
        return caseCategory;
    }

    public void setEmissionsYear(EmissionsYear emissionsYear) {
        this.emissionsYear = emissionsYear;
    }

    public EmissionsYear getEmissionsYear() {
        return emissionsYear;
    }

    public void setGrid(Grid grid) {
        this.grid = grid;
    }

    public Grid getGrid() {
        return grid;
    }

    public void setMeteorlogicalYear(MeteorlogicalYear meteorlogicalYear) {
        this.meteorlogicalYear = meteorlogicalYear;
    }

    public MeteorlogicalYear getMeteorlogicalYear() {
        return meteorlogicalYear;
    }

    public void setSpeciation(Speciation speciation) {
        this.speciation = speciation;
    }

    public Speciation getSpeciation() {
        return speciation;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
