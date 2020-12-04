package model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public class GraphCollections {
    private final Deque<SelectionNode> selectionNodes = new ArrayDeque<>();
    private final Deque<IterationNode> iterationNodes = new ArrayDeque<>();
    private final Deque<SwitchNode> switchNodes = new ArrayDeque<>();
    private final Deque<Breakable> breakableNodes = new ArrayDeque<>();

    public SelectionNode getLastSelectionNode() {
        return selectionNodes.getLast();
    }

    public void setLastSelectionNode(final SelectionNode selectionNode) {
        selectionNodes.addLast(selectionNode);
    }

    public SelectionNode removeLastSelectionNode() {
        return selectionNodes.removeLast();
    }

    public Breakable getLastBreakableNode() {
        return breakableNodes.getLast();
    }

    public void setLastBreakableNode(final Breakable breakableNode) {
        breakableNodes.addLast(breakableNode);
    }

    public Breakable removeLastBreakableNode() {
        return breakableNodes.removeLast();
    }

    public SwitchNode getLastSwitchNode() {
        return switchNodes.getLast();
    }

    public void setLastSwitchNode(final SwitchNode switchNode) {
        switchNodes.addLast(switchNode);
    }

    public SwitchNode removeLastSwitchNode() {
        return switchNodes.removeLast();
    }

    public IterationNode getLastIterationNode() {
        return iterationNodes.getLast();
    }

    public void setLastIterationNode(final IterationNode iterationNode) {
        iterationNodes.addLast(iterationNode);
    }

    public IterationNode removeLastIterationNode() {
        return iterationNodes.removeLast();
    }

    public Iterator<IterationNode> getIterationNodesIterator() {
        return iterationNodes.descendingIterator();
    }

    public int getIterationNodesSize() {
        return iterationNodes.size();
    }
}
