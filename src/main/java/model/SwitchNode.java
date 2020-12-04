package model;

import model.Breakable;

import java.util.*;

public class SwitchNode extends Breakable {
    private List<Variable> expression;

    public SwitchNode(final List<Variable> expression) {
        this.expression = new ArrayList<>(expression);
    }

    public void setExpression(final List<Variable> expression) {
        this.expression = new ArrayList<>(expression);
    }

    public List<Variable> getExpression() {
        return new ArrayList<>(expression);
    }

    public Map<String,Set<Integer>> getResultVersions() {
        return breakVersions;
    }
}
