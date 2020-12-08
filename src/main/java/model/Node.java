package model;

import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;

import java.util.*;

import static guru.nidi.graphviz.model.Factory.mutNode;

public class Node {
    public enum NodeLabel {
        YES, NO, UNDEFINED;

        @Override
        public String toString() {
            switch (this) {
                case NO:
                    return "No";
                case YES:
                    return "Yes";
                default:
                    return "";
            }
        }
    }

    public enum State {
        SELECTION, BASIC
    }

    private Map<Node, NodeLabel> children = new HashMap<>();
    private Set<Node> parents = new HashSet<>();
    private Integer number;
    private State state;
    private List<Variable> variables;
    private MutableNode node;

    public Node(final int number,
                final State state,
                final List<Variable> variables){
        this.number = number;
        this.state = state;
        this.variables = new ArrayList<>(variables);
    }

    public Node(){

    }

    public void setParameters(final Integer number,
                              final State state,
                              final List<Variable> variables) {
        this.number = number;
        this.state = state;
        this.variables = new ArrayList<>(variables);
    }

    public NodeLabel getCurrentLabel() {
        if (isSelectionNode()) {
            return children.size() == 0 ? NodeLabel.YES : NodeLabel.NO;
        }
        return NodeLabel.UNDEFINED;
    }

    public void copyNode(final Node node) {
        this.node = node.node;
        this.variables = node.variables;
        this.children = node.getChildren();
        this.parents = node.getParents();
        this.number = node.number;
        this.state = node.state;
    }

    public Map<Node,NodeLabel> getChildren() {
        return children;
    }

    public MutableNode getNode(final MutableGraph graph) {
        if (node == null) {
            node = createNode(graph);
        }
        return node;
    }

    public boolean isSelectionNode() {
        return this.state == State.SELECTION;
    }

    public void addChild(final NodeLabel label, final Node node) {
        if (node != null)
            children.put(node,label);
    }

    public void replaceChild(final Node oldChild, final Node newChild) {
        NodeLabel label = children.remove(oldChild);
        children.put(newChild,label);
    }

    public void replaceParent(final Node oldParent, final Node newParent) {
        parents.remove(oldParent);
        parents.add(newParent);
    }

    public void addParent(final Node node) {
        if (node != null)
            parents.add(node);
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public Set<Node> getParents() {
        return parents;
    }

    public MutableNode createNode(final MutableGraph graph) {
        final String text = getText();
        final MutableNode directNode = mutNode(number.toString())
                .attrs()
                .add("label", text);
        switch (state) {
            case BASIC:
                directNode.add(guru.nidi.graphviz.attribute.Shape.BOX);
                break;
            case SELECTION:
                directNode.add(guru.nidi.graphviz.attribute.Shape.DIAMOND);
                break;
            default:
                break;
        }
        graph.add(directNode);
        node = directNode;
        return directNode;
    }

    public void addLinks(final MutableGraph graph){
        children.forEach((childNode,label) ->
             this.getNode(graph).addLink(
                    this.getNode(graph).linkTo(childNode.getNode(graph))
                        .with(Label.of(label.toString()))));
    }

    private String getText() {
        StringBuilder text = new StringBuilder();
        for (Variable variable : variables) {
            text.append(variable.getText());
        }
        return text.toString();
    }

    public boolean equals(Node node) {
        return this.number.equals(node.number);
    }
}
