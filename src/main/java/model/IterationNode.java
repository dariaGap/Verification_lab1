package model;

import model.Breakable;
import model.Node;
import util.Util;

import java.util.*;

public class IterationNode extends Breakable {
    public enum LoopType {
        DIRECT, INVERSE;
    }

    private Node iterationNode;
    private Node conditionalNode;
    private Node iterationCounter;
    private Map<String, Set<Integer>> continueVersions = new HashMap<>();
    private Map<String, Set<Integer>> loopVersions;
    private Map<String,Set<Integer>> globalVersions;
    private List<Variable> phiCollection = new ArrayList<>();
    private LoopType type;

    public IterationNode(final Map<String, Set<Integer>> globalVersions,
                         final Map<String,Set<Integer>> loopVersions,
                         final LoopType type) {
        this.globalVersions = globalVersions;
        this.loopVersions = loopVersions;
        this.type = type;
    }

    public void setIterationCounter(final Node iterationCounter) {
        this.iterationCounter = iterationCounter;
    }

    public void setIterationNodes(final Node iterationNode, final Node conditionalNode) {
        this.iterationNode = iterationNode;
        this.conditionalNode = conditionalNode;
    }

    public void setIterationNodes(final Node iterationNode) {
        this.iterationNode = iterationNode;
        this.conditionalNode = iterationNode;
    }

    public Node getIterationCounter() {
        return iterationCounter;
    }

    public Node getIterationNode() {
        return iterationNode;
    }

    public Node getConditionalNode() {
        return conditionalNode;
    }

    public Map<String,Set<Integer>> getGlobalVersions() {
        return globalVersions;
    }

    public void addContinueVersions(final Map<String, Set<Integer>> versions) {
        continueVersions.putAll(Util.mergeVersions(continueVersions,versions));
    }

    public Map<String, Set<Integer>> getResultLoopVersions() {
        loopVersions.putAll(Util.mergeVersions(loopVersions,continueVersions));
        return loopVersions;
    }

    public Map<String, Set<Integer>> getResultGlobalVersions() {
        globalVersions.putAll(Util.mergeVersions(globalVersions,loopVersions));
        globalVersions.putAll(Util.mergeVersions(globalVersions,breakVersions));
        return globalVersions;
    }

    public Map<String, Set<Integer>> getDoLoopResultGlobalVersions() {
        globalVersions.putAll(Util.mergeVersions(loopVersions,breakVersions));
        return globalVersions;
    }

    public void addToPhiCollection(final Variable variable) {
        this.phiCollection.add(variable);
    }

    public void resolvePhiCollection(final Versions versions) {
        getResultLoopVersions();
        for (int i = 0; i<phiCollection.size(); i++) {
            Variable var = phiCollection.get(i);
            var.addVersion(versions.getVersion(loopVersions,var.getLabel()).getVersion());
        }
    }

    public void mergePhiCollections(List<Variable> innerPhiCollection) {
        for (Variable variable : innerPhiCollection) {
            Set<Integer> varGlobalVersions = this.globalVersions.get(variable.getLabel());
            Set<Integer> varLoopVersions = this.loopVersions.get(variable.getLabel());
            if (varGlobalVersions != null && varLoopVersions != null
                    && !Collections.disjoint(varGlobalVersions,varLoopVersions)) {
                this.phiCollection.add(variable);
            }
        }
    }

    public List<Variable> getPhiCollection() {
        return phiCollection;
    }

    public boolean isDirect() {
        if (this.type.equals(LoopType.DIRECT)) {
            return true;
        }
        return false;
    }
}
