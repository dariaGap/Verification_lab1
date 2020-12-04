package model;

import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static guru.nidi.graphviz.model.Factory.mutNode;

public class Node {
    public enum nodeLabel {
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
        SELECTION, BASIC;
    }

    private Map<Node, nodeLabel> children = new HashMap<>();
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

    public void copyNode(final Node node) {
        this.node = node.node;
        this.variables = node.variables;
        this.children.putAll(node.children);
        this.number = node.number;
        this.state = node.state;
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

    public void addChild(final nodeLabel label, final Node node) {
        if (node != null)
            children.put(node,label);
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
        String text = "";
        for (Variable variable : variables) {
            text += variable.getText();
        }
        return text;
    }
}
