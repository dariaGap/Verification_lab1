package model;

import model.Node;
import util.Util;

import java.util.*;

public class SelectionNode {
    private Node selectionNode;
    private List<Node> branchesEnds = new ArrayList<>();
    private Map<String, Set<Integer>> ifVersions = new HashMap<>();
    private Map<String, Set<Integer>> elseVersions = new HashMap<>();
    private Map<String,Set<Integer>> globalVersions;

    public SelectionNode(final Node selectionNode,
                         final Map<String,Set<Integer>> globalVersions) {
        this.selectionNode = selectionNode;
        this.globalVersions = globalVersions;
    }

    public Node getSelectionNode() {
        return selectionNode;
    }

    public void addBranchEnd(final Node node) {
        branchesEnds.add(node);
    }

    public void addBranchEnd(final List<Node> nodes) {
        branchesEnds.addAll(nodes);
    }

    public List<Node> getBranchesEnds() {
        return branchesEnds;
    }

    public void setElseVersions(final Map<String,Set<Integer>> elseVersions) {
        this.elseVersions = elseVersions;
    }

    public void setIfVersions(final Map<String,Set<Integer>> ifVersions) {
        this.ifVersions = ifVersions;
    }

    public Map<String,Set<Integer>> getResultVersions() {
        globalVersions.putAll(Util.mergeVersions(ifVersions,elseVersions));
        return globalVersions;
    }

    public Map<String,Set<Integer>> getGlobalVersions() {
        return globalVersions;
    }
}
