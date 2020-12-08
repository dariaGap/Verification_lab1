package model;

import util.Util;

import java.util.*;

public class Breakable {
    protected Set<Node> breaks = new HashSet<>();
    protected Map<String, Set<Integer>> breakVersions = new HashMap<>();

    public void addBreak(final Node breakNode,
                         final Map<String,Set<Integer>> breakVersions) {
        breaks.add(breakNode);
        this.breakVersions.putAll(Util.mergeVersions(this.breakVersions,breakVersions));
    }

    public void addBreak(final Set<Node> breakNodes,
                         final Map<String,Set<Integer>> breakVersions) {
        breaks.addAll(breakNodes);
        this.breakVersions.putAll(Util.mergeVersions(this.breakVersions,breakVersions));
    }

    public Set<Node> getBreaks() {
        return breaks;
    }
}
