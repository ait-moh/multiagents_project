package project.inference;

import java.util.ArrayList;

public class Rule {
    private String label;
    private ArrayList<Clause> antecedents;
    private Clause consequent;
    private boolean fired;

    public Rule(String label, ArrayList<Clause> antecedents, Clause consequent) {
        this.label = label;
        this.antecedents = antecedents;
        this.consequent = consequent;
    }

    public boolean isTriggered() {
        for (Clause clause : this.antecedents) {
            if (!clause.isTrue())
                return false;
        }
        return true;
    }

    public void fire() {
        this.consequent.getVariable().setValue(this.consequent.getValue());
        this.fired = true;
    }

    public String getLabel() {
        return this.label;
    }
    public boolean isFired() {
        return this.fired;
    }
    public int getNumberOfAntecedents() {
        return this.antecedents.size();
    }
    public String getAntecedentsString() {
        ArrayList<String> clauses = new ArrayList<String>();
        for (Clause clause : this.antecedents) {
            clauses.add(clause.toString());
        }
        return String.join(" AND ", clauses);
    }
    public String getConsequentString() {
        return this.consequent.toString();
    }
    public String getFiredString() {
        return Boolean.toString(this.fired);
    }
}
