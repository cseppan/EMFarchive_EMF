package gov.epa.emissions.framework.services.cost.controlmeasure.io;

public class CMEquationFileFormat implements CMFileFormat {

    private String[] cols;

    public CMEquationFileFormat() {
        this.cols = createCols();
    }

    private String[] createCols() {
        String[] cols = { "EquationType", "VariableName", "VariableValue", "Description" };
        return cols;
    }

    public String identify() {
        return "Control Measure Equation";
    }

    public String[] cols() {
        return cols;
    }

}
