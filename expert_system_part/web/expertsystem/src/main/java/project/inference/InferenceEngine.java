package project.inference;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class InferenceEngine {
    private HashMap<String, Variable> memory;
    private HashMap<String, Rule> knowledgeBase;
    private HashSet<String> conflictSet;

    public InferenceEngine(HashMap<String, Variable> memory, 
        HashMap<String, Rule> knowledgeBase) {
        this.memory = memory;
        this.knowledgeBase = knowledgeBase;
    }

    public String forwardPass() {
        // calculating the conflict set
        calculateConflictSet();
        if (getConflictSet().size() == 0)
            return null;
        return selectRule();
    }

    private void calculateConflictSet() {
        setConflictSet(new HashSet<String>());
        for (Map.Entry<String, Rule> entry : getKnowledgeBase().entrySet()) {
            if (!entry.getValue().isFired() && entry.getValue().isTriggered()) {
                getConflictSet().add(entry.getKey());
            }
        }
    }

    private String selectRule() {
        int max = 0;
        String selectedRule = null;
        for (String rule : getConflictSet()) {
            if (getKnowledgeBase().get(rule).getNumberOfAntecedents() > max) {
                max = getKnowledgeBase().get(rule).getNumberOfAntecedents();
                selectedRule = rule;
            }
        }
        return selectedRule;
    }

    private void setConflictSet(HashSet<String> conflictSet) {
        this.conflictSet = conflictSet;
    }
    public HashSet<String> getConflictSet() {
        return this.conflictSet;
    }

    public void setMemory(HashMap<String, Variable> memory) {
        this.memory = memory;
    }
    public void setKnowledgeBase(HashMap<String, Rule> knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
    }

    public HashMap<String, Variable> getMemory() {
        return this.memory;
    }

    public HashMap<String, Rule> getKnowledgeBase() {
        return this.knowledgeBase;
    }
}
