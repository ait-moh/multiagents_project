package project.inference;

public class Variable {
    private String name;
    private String value;
    
    public Variable(String name) {
        this.name = name;
        this.value = "";
    }

    public String getName() {
        return this.name;
    }
    public String getValue() {
        return this.value;
    }
    public void setValue(String value) {
        this.value = value;
    }
}
