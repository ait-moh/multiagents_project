package project.inference;

public class Clause {
    private Variable variable;
    private Condition condition;
    private String value;

    public Clause(Variable variable, Condition condition, String value) {
        this.variable = variable;
        this.condition = condition;
        this.value = value;
    }

    public boolean isTrue() {
        return condition.test(this.variable.getValue(), this.value);
    }

    public Variable getVariable() {
        return this.variable;
    }
    public String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.variable.getName() 
            + " " + this.condition.toString() 
            + " " + this.value;
    }
}
