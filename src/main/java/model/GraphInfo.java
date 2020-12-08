package model;

import java.util.*;

public class GraphInfo {
    private final List<Node> graphNodes = new ArrayList<>();
    private Integer nodesCounter = 1;

    private Map<String,Set<Integer>> currentVersions;
    private Map<String,Set<Integer>> prevVersions;
    private final Versions versionsClass = new Versions();

    private final Set<Node> prevNodes = new HashSet<>();

    private final Flags flags = new Flags();

    private final GraphCollections collections = new GraphCollections();

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
        if (prevNode == null) {
            this.prevNodes.clear();
        } else {
            this.prevNodes.add(prevNode);
        }
    }

    public Set<Node> getPrevNode() {
        return prevNodes;
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

    public void setDeclaratorFlag() {
        flags.setDeclarator(true);
        prevVersions = new HashMap<>(currentVersions);
    }

    public void addBreaks(final Node node) {
        Breakable breakableNode;
        if (flags.isSwitchEndFlag()) {
            breakableNode = collections.removeLastSwitchNode();
        } else {
            breakableNode = collections.removeLastIterationNode();
        }
        collections.removeLastBreakableNode();
        Set<Node> breaks = breakableNode.getBreaks();
        breaks.iterator().forEachRemaining(breakNode ->
                addLink(breakNode,node));
    }

    public void addLink(final Node prevNode,final model.Node node) {
        Node.NodeLabel label;
        if (prevNode != null) {
            label = prevNode.getCurrentLabel();
            prevNode.addChild(label,node);
            node.addParent(prevNode);
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
            Set<Node> prevs = collections.getLastSelectionNode().getBranchesEnds();

            for (Node prevNode : prevs) {
                addLink(prevNode,node);
            }
            collections.removeLastSelectionNode();
            flags.setSelectionEndFlag(false);
        } else {
            for (Node prevNode : prevNodes) {
                addLink(prevNode, node);
            }
            prevNodes.clear();
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
        prevNodes.add(node);
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
        return versionsClass.setVersion(currentVersions,variable,flags.isIterationCounterFlag());
    }

    public Variable getVersion(final String variable) {
        Map<String,Set<Integer>> versionCollection;
        if (prevVersions != null) {
            versionCollection = prevVersions;
        } else {
            versionCollection = currentVersions;
        }

        Variable var = versionsClass.getVersion(versionCollection, variable);

        Variable tmpVar;
        if ((tmpVar = tryToAddVarToPhiCollection(var)) != null) {
            var = tmpVar;
        } else {
            if (var.getVersion().size() > 1) {
                var = addPhiNode(var);
            }
        }

        return var;
    }

    private Variable tryToAddVarToPhiCollection(Variable var) {
        // if program is in loop now
        if (collections.getIterationNodesSize() > 0 && flags.isIterationFlag()) {
            // get actual iteration node to add phi
            IterationNode iterationNode;
            if (flags.isIterationEndFlag()) {
                Iterator<IterationNode> iter = collections.getIterationNodesIterator();
                iter.next();
                iterationNode = iter.next();
            } else {
                iterationNode = collections.getLastIterationNode();
            }
            // get before loop versions
            Set<Integer> varGlobalVersions =
                    iterationNode.getGlobalVersions().get(var.getLabel());
            // if this var depends on before loop versions add to phi collection
            if (varGlobalVersions != null
                    && !Collections.disjoint(varGlobalVersions,var.getVersion())) {
                for (Integer version : varGlobalVersions) {
                    if (var.getVersion().contains(version)) {
                        var.removeVersion(version);
                    }
                }
                if (var.getVersion().size() > 0 ) {
                    iterationNode.addToPhiCollection(var);
                    var = addPhiNode(var);
                } else {
                    iterationNode.addToPhiCollection(var);
                }
                return var;
            }
        }
        return null;
    }

    public Set<Node> getPrevNodes() {
        Set<Node> nodes = new HashSet<>();
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
        prevNodes.addAll(nodes);
        return nodes;
    }

    public void addBoxNodeBeforeNode(Node nextNode,Node nodeToAdd, Set<Node> prevNodes) {
        for (Node parent : prevNodes) {
            parent.replaceChild(nextNode,nodeToAdd);
            nodeToAdd.addParent(parent);
        }
        nextNode.getParents().removeAll(prevNodes);
        nextNode.addParent(nodeToAdd);
        nodeToAdd.addChild(Node.NodeLabel.UNDEFINED,nextNode);
        graphNodes.add(nodeToAdd);
    }

    public void mergePhiCollections() {
        if (collections.getIterationNodesSize() > 1) {
            Iterator<IterationNode> iterator = collections.getIterationNodesIterator();
            IterationNode innerLoop = iterator.next();
            IterationNode outerLoop = iterator.next();
            outerLoop.mergePhiCollections(innerLoop.getPhiCollection(),innerLoop);
        }

    }

    private Variable addPhiNode(Variable var) {
        // add phi resolver as next node
        List<Variable> phiNodeVars = formPhiResolveVar(var);
        addNode(Node.State.BASIC,phiNodeVars,null);
        return phiNodeVars.get(0);
    }

    private List<Variable> formPhiResolveVar(Variable var) {
        List<Variable> phiNodeVars = new ArrayList<>();
        Variable phiVar = getDeclaratorVersion(var.getLabel());
        phiNodeVars.add(phiVar);
        phiNodeVars.add(new Variable("=", Variable.Type.OPERATOR));
        phiNodeVars.add(var);
        return phiNodeVars;
    }

    public Node addPhiResolveNode(final String var,
                                  final Node prevNode,
                                  final Set<Node> beforeLoopNodes) {
        // add phi resolver as node before prevNode
        Variable phiVar = versionsClass.getVersion(currentVersions,var);
        List<Variable> variables = formPhiResolveVar(phiVar);
        Node node = new Node(nodesCounter, Node.State.BASIC,variables);
        incrementNodesCounter();
        addBoxNodeBeforeNode(prevNode,node,beforeLoopNodes);

        // add to outer loop's phiCollection phi resolvers from inner loop
        if (collections.getIterationNodesSize() > 1) {
            Iterator<IterationNode> iter = collections.getIterationNodesIterator();
            iter.next();
            IterationNode outLoop = iter.next();
            Set<Integer> globalVersions = outLoop.getGlobalVersions().get(phiVar.getLabel());
            if (!Collections.disjoint(globalVersions,phiVar.getVersion())) {
                outLoop.addToPhiCollection(phiVar);
            }
        }
        return node;
    }

    public void replaceNode(Node prevNode, Node afterNode) {
        for (Node parent : prevNode.getParents()) {
            parent.replaceChild(prevNode,afterNode);
        }

        for (Node parent : afterNode.getParents()) {
            if (parent != prevNode) {
                parent.replaceChild(afterNode,prevNode);
            }
        }

        Node tmpNode = new Node();
        tmpNode.copyNode(afterNode);
        afterNode.copyNode(prevNode);
        afterNode.replaceChild(afterNode,prevNode);
        prevNode.copyNode(tmpNode);
        prevNode.replaceParent(prevNode,afterNode);
    }
}
