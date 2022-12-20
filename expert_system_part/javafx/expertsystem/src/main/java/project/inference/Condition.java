package project.inference;

public enum Condition {
    EQUAL("="), LESS("<"), GREAT(">"), NOT_EQUAL("!=");

    private String label;

    private Condition(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label; 
    }

    public static Condition fromString(String label) {
        for (Condition condition : Condition.values())
            if (condition.toString().equals(label))
                return condition;
        return null;
    }

    public boolean test(String leftValue, String rightValue) {
        switch (this) {
            case EQUAL:
                return leftValue.equals(rightValue);
            case LESS:
                return leftValue.compareTo(rightValue) < 0;
            case GREAT:
                return leftValue.compareTo(rightValue) > 0;
            case NOT_EQUAL:
                return !leftValue.equals(rightValue);
            default:
                return false;
        }
    }
}
