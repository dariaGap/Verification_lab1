package model;
import util.Util;

import java.util.*;

public class IterationNode extends Breakable {

    public enum LoopType {
        DIRECT, INVERSE
    }

    private Node iterationNode;
    private Node conditionalNode;
    private Node iterationCounter;
    private final Set<Node> beforeLoopNodes = new HashSet<>();
    private final Map<String, Set<Integer>> continueVersions = new HashMap<>();
    private final Set<Node> continues = new HashSet<>();
    private final Map<String, Set<Integer>> loopVersions;
    private final Map<String,Set<Integer>> globalVersions;
    private final List<Variable> phiCollection = new ArrayList<>();
    private final LoopType type;

    public IterationNode(final Map<String, Set<Integer>> globalVersions,
                         final Map<String,Set<Integer>> loopVersions,
                         final LoopType type,
                         final Set<Node> prevNodes) {
        this.globalVersions = globalVersions;
        this.loopVersions = loopVersions;
        this.type = type;
        this.beforeLoopNodes.addAll(prevNodes);
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

    public void setIterationNode(final Node iterationNode) {
        this.iterationNode = iterationNode;
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

    public boolean isInPhi(final String var) {
        for(Variable variable: phiCollection) {
            if (variable.getLabel().equals(var)) {
                return true;
            }
        }
        return false;
    }

    public void getResultLoopVersions(GraphInfo graphInfo) {
        for (Map.Entry<String, Set<Integer>> entry : continueVersions.entrySet()) {
            String var = entry.getKey();
            Set<Integer> versions = entry.getValue();

            if (globalVersions.get(var) != null
                    && isInPhi(var)
                    && !Collections.disjoint(globalVersions.get(var), versions)
                    && Util.mergeVersions(globalVersions, loopVersions).get(var).size() > 1
                    && !loopVersions.get(var).containsAll(versions)) {
                versions.add(graphInfo.getVersionsClass().getLastVersion(var) + 1);
                continueVersions.put(var, versions);
            }
        }
        loopVersions.putAll(Util.mergeVersions(loopVersions,continueVersions));
        continueVersions.clear();
    }

    public void addToPhiCollection(final Variable variable) {
        this.phiCollection.add(variable);
    }

    public void resolvePhiCollection(final GraphInfo graphInfo) {
        Map<String, Set<Integer>> basicVersions = new HashMap<>(globalVersions);
        getResultLoopVersions(graphInfo);

        for (Map.Entry<String, Set<Integer>> e : loopVersions.entrySet()) {
            String var = e.getKey();
            Set<Integer> versions = e.getValue();
            if (globalVersions.get(var) != null
                    && isInPhi(var)
                    && !Collections.disjoint(globalVersions.get(var), versions)
                    && Util.mergeVersions(globalVersions, loopVersions).get(var).size() > 1) {
                versions.add(graphInfo.getVersionsClass().getLastVersion(var) + 1);
                loopVersions.put(var, versions);
            }
        }

        Map<String, Set<Integer>> copyGlobalVersions = new HashMap<>(globalVersions);
        globalVersions.putAll(Util.mergeVersions(globalVersions,loopVersions));

        Node prevIterationNode = iterationNode;

        for (Variable var : phiCollection) {
            Set<Integer> varVersions = globalVersions.get(var.getLabel());
            if (var.getVersion().containsAll(varVersions)
                    && varVersions.containsAll(var.getVersion())) {
                continue;
            }
            if (var.getVersion().size() > 0) {
                for (Integer version : copyGlobalVersions.get(var.getLabel())) {
                    var.removeVersion(version);
                }
            }
            if (varVersions.size() > 1
                    && !(basicVersions.get(var.getLabel()).containsAll(varVersions)
                    && graphInfo.getCollections().getIterationNodesSize() > 1)
                    && !var.getVersion().containsAll(varVersions)) {
                Node node = graphInfo.addPhiResolveNode(var.getLabel(),
                        prevIterationNode,
                        beforeLoopNodes);
                beforeLoopNodes.clear();
                if (iterationNode.equals(prevIterationNode)) {
                    graphInfo.replaceNode(node, iterationNode);
                    if (conditionalNode == iterationNode) {
                        conditionalNode = node;
                    }
                    prevIterationNode = node;
                    beforeLoopNodes.add(iterationNode);
                } else {
                    beforeLoopNodes.add(node);
                }


            }
            var.addVersion(globalVersions.get(var.getLabel()));
        }
        if (type == LoopType.INVERSE) {
            for (Map.Entry<String, Set<Integer>> entry : new HashSet<>(loopVersions.entrySet())) {
                String varLabel = entry.getKey();
                Set<Integer> version = entry.getValue();
                Set<Integer> newVersions = new HashSet<>(version);
                if (version.size() > 1) {
                    for (Integer v : version) {
                        if (basicVersions.get(varLabel).contains(v)) {
                            newVersions.remove(v);
                            newVersions.addAll(globalVersions.get(varLabel));
                        }
                    }
                }
                loopVersions.put(varLabel,newVersions);
            }
            globalVersions.putAll(loopVersions);
        }
        globalVersions.putAll(Util.mergeVersions(globalVersions,breakVersions));
    }

    public void mergePhiCollections(List<Variable> innerPhiCollection,
                                    IterationNode innerLoop) {
        List<Variable> innerPhiCopy = new ArrayList<>(innerPhiCollection);
        for (Variable variable : innerPhiCopy) {
            Set<Integer> varGlobalVersions = this.globalVersions.get(variable.getLabel());
            Set<Integer> varLoopVersions = innerLoop.getGlobalVersions().get(variable.getLabel());
            if (varGlobalVersions != null && varLoopVersions != null
                    && !Collections.disjoint(varGlobalVersions,varLoopVersions)) {
                this.phiCollection.add(variable);
                innerPhiCollection.remove(variable);
            }
        }
    }

    public List<Variable> getPhiCollection() {
        return phiCollection;
    }

    public boolean isDirect() {
       return this.type.equals(LoopType.DIRECT);
    }

    public void addContinues(Set<Node> continues) {
        this.continues.addAll(continues);
    }

    public Set<Node> getContinues() {
        return continues;
    }

    public Set<Node> getBeforeLoopNodes() {
        return beforeLoopNodes;
    }
}
