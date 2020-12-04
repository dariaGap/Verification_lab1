package model;

import model.Node;
import util.Util;

import java.util.*;

public class Breakable {
    protected List<Node> breaks = new ArrayList<>();
    protected Map<String, Set<Integer>> breakVersions = new HashMap<>();

    public void addBreak(final Node breakNode,
                         final Map<String,Set<Integer>> breakVersions) {
        breaks.add(breakNode);
        this.breakVersions.putAll(Util.mergeVersions(this.breakVersions,breakVersions));
    }

    public void addBreak(final List<Node> breakNodes,
                         final Map<String,Set<Integer>> breakVersions) {
        breaks.addAll(breakNodes);
        this.breakVersions.putAll(Util.mergeVersions(this.breakVersions,breakVersions));
    }

    public List<Node> getBreaks() {
        return breaks;
    }

    public void setBreakVersions(Map<String, Set<Integer>> breakVersions) {
        this.breakVersions.putAll(Util.mergeVersions(this.breakVersions,breakVersions));
    }
}
