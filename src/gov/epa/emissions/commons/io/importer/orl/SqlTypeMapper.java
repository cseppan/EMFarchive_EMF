package gov.epa.emissions.commons.io.importer.orl;

public interface SqlTypeMapper {

    String getSqlType(String name, String genericType, int width);

}
