package model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Variable {
    public enum Type {
        VAR, PHI, OPERATOR, CONSTANT, DECLARATOR
    }

    private String label;
    private Set<Integer> version = new HashSet<>();
    private Type type;

    public Variable(final String label, final int version) {
        this.label = label;
        this.version.add(version);
        this.type = Type.VAR;
    }

    public Variable(final String label, final int version, final Type type) {
        this.label = label;
        this.version.add(version);
        this.type = type;
    }

    public Variable(final String label, final Set<Integer> version) {
        this.label = label;
        this.version = new HashSet<>(version);
        if (version.size() > 1)
            this.type = Type.PHI;
        else
            this.type = Type.VAR;
    }

    public Variable(final String label, final Type type) {
        this.label = label;
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setVersion(Set<Integer> version) {
        this.version = new HashSet<>(version);
    }

    public Set<Integer> getVersion() {
        //return new HashSet<>(version);
        return version;
    }

    public void removeVersion(final Integer version) {
        this.version.remove(version);
        if (this.version.size() <= 1) {
            type = Type.VAR;
        }
    }

    public void addVersion(Set<Integer> version) {
        this.version.addAll(new HashSet<>(version));
        if (this.version.size() > 1)
            this.type = Type.PHI;
    }

    public void setType(Type type) {
        this.type = type;
    }

    private String resolvePhi(){
        String result = "phi(";
        for (Integer vers : version) {
            result += label + "." + vers + ",";
        }
        result = result.substring(0,result.length()-1) + ")";
        return result;
    }

    public Type getType() {
        return this.type;
    }

    public String getText() {
        switch (type) {
            case OPERATOR:
                return " " + label + " ";
            case CONSTANT:
                return label;
            case PHI:
                return resolvePhi();
            case VAR:
            case DECLARATOR:
                return label + "." + version.iterator().next();
            default:
                return "";
        }
    }

}
