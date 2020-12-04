package model;

import java.util.*;

public class GraphInfo {
    private final List<Node> graphNodes = new ArrayList<>();
    private Integer nodesCounter = 1;

    private Map<String,Set<Integer>> currentVersions;
    private Map<String,Set<Integer>> prevVersions;
    private final Versions versionsClass = new Versions();

    private Node prevNode;

    private final Deque<Node.nodeLabel> labels = new ArrayDeque<>();
    private final Flags flags = new Flags();

    private final GraphCollections collections = new GraphCollections();

    public void addBreaks(final Node node) {
        Breakable breakableNode;
        if (flags.isSwitchEndFlag()) {
            breakableNode = collections.removeLastSwitchNode();
        } else {
            breakableNode = collections.removeLastIterationNode();
        }
        collections.removeLastBreakableNode();
        List<Node> breaks = breakableNode.getBreaks();
        breaks.iterator().forEachRemaining(breakNode ->
                addLink(breakNode,node));
    }

    public void addLink(final model.Node prevNode,final model.Node node) {
        model.Node.nodeLabel label;
        if (prevNode != null) {
            if (prevNode.isSelectionNode()) {
                label = labels.removeLast();
            } else {
                label = model.Node.nodeLabel.UNDEFINED;
            }
            prevNode.addChild(label,node);
            if (flags.isIterationEndFlag()) {
                flags.setIterationEndFlag(false);
                addBreaks(node);
            }
        }
    }

    public void addLinkToPrevNode(final Node node) {
        if (flags.isSwitchEndFlag()) {
            addBreaks(node);
            flags.setSwitchEndFlag(false);
        }
        if (flags.isSelectionEndFlag()) {
            List<Node> prevs = collections.getLastSelectionNode().getBranchesEnds();

            for (Node prevNode : prevs) {
                addLink(prevNode,node);
            }
            collections.removeLastSelectionNode();
            flags.setSelectionEndFlag(false);
        } else {
            addLink(prevNode,node);
            prevNode = null;
        }
    }

    public Node addNode(final Node.State state, final List<Variable> expression,
                         Node node) {
        if (node == null) {
            node = new Node(nodesCounter, state, expression);
        } else {
            node.setParameters(nodesCounter,state,expression);
        }
        graphNodes.add(node);
        nodesCounter++;
        addLinkToPrevNode(node);
        prevNode = node;
        if (flags.isIterationStartFlag()) {
            Iterator<IterationNode> iter = collections.getIterationNodesIterator();
            IterationNode iterationNode;
            while (iter.hasNext()
                    && (iterationNode = iter.next()).getIterationNode() == null) {
                if (iterationNode.isDirect()) {
                    iterationNode.setIterationNodes(node);
                } else {
                    iterationNode.setIterationNodes(node, new Node());
                }
            }
            flags.setIterationStartFlag(false);
        }
        return node;
    }

    public Node addSelectionNode(List<Variable> expression) {
        final Node node = addNode(Node.State.SELECTION,expression,null);
        collections.setLastSelectionNode(new SelectionNode(node,currentVersions));
        return node;
    }

    public Variable getDeclaratorVersion(final String variable) {
        return versionsClass.setVersion(currentVersions,variable);
    }

    public Variable getVersion(final String variable) {
        Map<String,Set<Integer>> versionCollection;
        if (prevVersions != null) {
            versionCollection = prevVersions;
        } else {
            versionCollection = currentVersions;
        }

        Variable var = versionsClass.getVersion(versionCollection, variable);

        if (var.getType() == Variable.Type.PHI) {
            List<Variable> phiNode = new ArrayList<>();
            Variable phiVar = getDeclaratorVersion(var.getLabel());
            phiNode.add(phiVar);
            phiNode.add(new Variable("=", Variable.Type.OPERATOR));
            phiNode.add(var);
            addNode(Node.State.BASIC,phiNode,null);
            var = phiVar;
        }

        if (collections.getIterationNodesSize() > 0 && flags.isIterationFlag()) {
            IterationNode iterationNode;
            if (flags.isIterationEndFlag()) {
                Iterator<IterationNode> iter = collections.getIterationNodesIterator();
                iter.next();
                iterationNode = iter.next();
            } else {
                iterationNode = collections.getLastIterationNode();
            }
            Set<Integer> varGlobalVersions =
                    iterationNode.getGlobalVersions().get(variable);
            if (varGlobalVersions != null
                    && !Collections.disjoint(varGlobalVersions,var.getVersion())) {
                iterationNode.addToPhiCollection(var);
            }
        }

        return var;
    }

    public void setDeclaratorFlag() {
        flags.setDeclarator(true);
        prevVersions = new HashMap<>(currentVersions);
    }

    public List<Node> getPrevNodes() {
        List<Node> nodes = new ArrayList<>();
        if (flags.isSelectionEndFlag()) {
            SelectionNode selectionNode = collections.removeLastSelectionNode();
            nodes.addAll(selectionNode.getBranchesEnds());
            flags.setSelectionEndFlag(false);
        }
        if (flags.isSwitchEndFlag()) {
            SwitchNode switchNode = collections.removeLastSwitchNode();
            collections.removeLastBreakableNode();
            nodes.addAll(switchNode.getBreaks());
            flags.setSwitchEndFlag(false);
        }
        if (flags.isIterationEndFlag()) {
            IterationNode iterationNode = collections.removeLastIterationNode();
            collections.removeLastBreakableNode();
            nodes.addAll(iterationNode.getBreaks());
            flags.setIterationEndFlag(false);
        }
        return nodes;
    }

    public List<Node> getGraphNodes() {
        return graphNodes;
    }

    public Flags getFlags() {
        return flags;
    }

    public GraphCollections getCollections() {
        return collections;
    }

    public void setCurrentVersions(Map<String, Set<Integer>> currentVersions) {
        this.currentVersions = currentVersions;
    }

    public Map<String, Set<Integer>> getCurrentVersions() {
        return currentVersions;
    }

    public void setPrevVersions(Map<String, Set<Integer>> prevVersions) {
        this.prevVersions = prevVersions;
    }

    public Map<String, Set<Integer>> getPrevVersions() {
        return prevVersions;
    }

    public void setPrevNode(Node prevNode) {
        this.prevNode = prevNode;
    }

    public Node getPrevNode() {
        return prevNode;
    }

    public Deque<Node.nodeLabel> getLabels() {
        return labels;
    }

    public void graphNodesAdd(Node node) {
        graphNodes.add(node);
    }

    public void incrementNodesCounter() {
        nodesCounter++;
    }

    public int getNodesCounter() {
        return nodesCounter;
    }

    public Versions getVersionsClass() {
        return versionsClass;
    }
}
