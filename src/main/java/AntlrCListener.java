import guru.nidi.graphviz.model.MutableGraph;
import model.*;
import util.Util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static guru.nidi.graphviz.model.Factory.mutGraph;

public class AntlrCListener extends CBaseListener {
    private MutableGraph graph;
    private final List<Node> graphNodes = new ArrayList<>();
    private Integer nodesCounter = 1;

    private Map<String,Set<Integer>> currentVersions;
    private Map<String,Set<Integer>> prevVersions;
    private final Versions versionsClass = new Versions();

    private Node prevNode;

    private final Deque<Node.nodeLabel> labels = new ArrayDeque<>();

    private final Deque<SelectionNode> selectionNodes = new ArrayDeque<>();
    private final Deque<IterationNode> iterationNodes = new ArrayDeque<>();
    private final Deque<SwitchNode> switchNodes = new ArrayDeque<>();
    private final Deque<Breakable> breakableNodes = new ArrayDeque<>();

    private boolean functionDeclarationFlag = false,
            functionParametersFlag = false,
            selectionEndFlag = false,
            iterationFlag = false,
            iterationCounterFlag = false,
            iterationStartFlag = false,
            iterationEndFlag = false,
            switchStartFlag = false,
            switchEndFlag = false;
    private final List<Variable> currentExpression = new ArrayList<>();
    private boolean isDeclarator = false;

    public MutableGraph getGraph() {
        graphNodes.forEach(node -> node.addLinks(graph));
        return graph;
    }

    private void addLink(final model.Node prevNode, final model.Node node) {
        model.Node.nodeLabel label;
        if (prevNode != null) {
            if (prevNode.isSelectionNode()) {
                label = labels.removeLast();
            } else {
                label = model.Node.nodeLabel.UNDEFINED;
            }
            prevNode.addChild(label,node);
            if (iterationEndFlag) {
                iterationEndFlag = false;
                addBreaks(node);
            }
        }
    }

    private void addBreaks(final Node node) {
        Breakable breakableNode;
        if (switchEndFlag) {
            breakableNode = switchNodes.removeLast();
        } else {
            breakableNode = iterationNodes.removeLast();
        }
        breakableNodes.removeLast();
        List<Node> breaks = breakableNode.getBreaks();
        breaks.iterator().forEachRemaining(breakNode -> addLink(breakNode,node));
    }

    private void addLinkToPrevNode(final Node node) {
        if (switchEndFlag) {
            addBreaks(node);
            switchEndFlag = false;
        }
        if (selectionEndFlag) {
            List<Node> prevs = selectionNodes.getLast().getBranchesEnds();

            for (Node prevNode : prevs) {
                addLink(prevNode,node);
            }
            selectionNodes.removeLast();
            selectionEndFlag = false;
        } else {
            addLink(prevNode,node);
            prevNode = null;
        }
    }

    private Node addNode(final Node.State state, final List<Variable> expression,
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
        if (iterationStartFlag) {
            Iterator<IterationNode> iter = iterationNodes.descendingIterator();
            IterationNode iterationNode;
            while (iter.hasNext()
                    && (iterationNode = iter.next()).getIterationNode() == null) {
                if (iterationNode.isDirect()) {
                    iterationNode.setIterationNodes(node);
                } else {
                    iterationNode.setIterationNodes(node, new Node());
                }
            }
            iterationStartFlag = false;
        }
        return node;
    }

    private void addBoxNodeWithCurExpr() {
        addNode(Node.State.BASIC,currentExpression,null);
        currentExpression.clear();
    }

    private Node addSelectionNode(List<Variable> expression) {
        final Node node = addNode(Node.State.SELECTION,expression,null);
        selectionNodes.addLast(new SelectionNode(node,currentVersions));
        return node;
    }

    private Variable getDeclaratorVersion(final String variable) {
        return versionsClass.setVersion(currentVersions,variable);
    }

    private Variable getVersion(final String variable) {
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

        if (iterationNodes.size() > 0 && iterationFlag) {
            IterationNode iterationNode;
            if (iterationEndFlag) {
                Iterator<IterationNode> iter = iterationNodes.descendingIterator();
                iter.next();
                iterationNode = iter.next();
            } else {
                iterationNode = iterationNodes.getLast();
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

    private void setDeclaratorFlag() {
        isDeclarator = true;
        prevVersions = new HashMap<>(currentVersions);
    }

    private List<Node> getPrevNodes() {
        List<Node> nodes = new ArrayList<>();
        if (selectionEndFlag) {
            SelectionNode selectionNode = selectionNodes.removeLast();
            nodes.addAll(selectionNode.getBranchesEnds());
            selectionEndFlag = false;
        }
        if (switchEndFlag) {
            SwitchNode switchNode = switchNodes.removeLast();
            breakableNodes.removeLast();
            nodes.addAll(switchNode.getBreaks());
            switchEndFlag = false;
        }
        if (iterationEndFlag) {
            IterationNode iterationNode = iterationNodes.removeLast();
            breakableNodes.removeLast();
            nodes.addAll(iterationNode.getBreaks());
            iterationEndFlag = false;
        }
        return nodes;
    }

    public void enterCompilationUnit(CParser.CompilationUnitContext ctx) {
        graph = mutGraph("gr").setDirected(true);
        currentVersions = new HashMap<>();
    }

    public void enterFunctionDeclarator(CParser.FunctionDeclaratorContext ctx) {
        functionDeclarationFlag = true;
    }

    public void exitFunctionDeclarator(CParser.FunctionDeclaratorContext ctx) {
        functionDeclarationFlag = false;
        functionParametersFlag = false;
        if (currentExpression.size()>2) {
            currentExpression.remove(currentExpression.size()-1);
        }
        currentExpression.add(new Variable(")", Variable.Type.CONSTANT));
        addBoxNodeWithCurExpr();
    }

    public void enterParameterTypeList(CParser.ParameterTypeListContext ctx) {
        functionParametersFlag = true;
    }

    public void enterDirectDeclarator(CParser.DirectDeclaratorContext ctx) {
        if (functionDeclarationFlag && ctx.children.size() == 1) {
            if (functionParametersFlag) {
                currentExpression.add(getDeclaratorVersion(ctx.Identifier().getText()));
                currentExpression.add(new Variable(",", Variable.Type.CONSTANT));
            } else {
                currentExpression.add(new Variable(ctx.Identifier().getText(), Variable.Type.CONSTANT));
                currentExpression.add(new Variable("(", Variable.Type.CONSTANT));
            }
        }
    }

    public void enterInitDeclarator(CParser.InitDeclaratorContext ctx) {
        if (ctx.initializer() != null) {
            Variable declarator = getDeclaratorVersion(
                    ctx.declarator().directDeclarator().getChild(0).getText());
            currentExpression.add(declarator);
            currentExpression.add(new Variable("=", Variable.Type.OPERATOR));
            isDeclarator = false;
            ctx.children.remove(0);
        }
    }

    public void exitInitDeclarator(CParser.InitDeclaratorContext ctx) {
        if (ctx.initializer() != null) {
            addBoxNodeWithCurExpr();
        }
    }

    public void enterPrimaryExpression(CParser.PrimaryExpressionContext ctx) {
        if (ctx.Identifier() != null) {
            Variable variable = isDeclarator
                    ? getDeclaratorVersion(ctx.Identifier().getText())
                    : getVersion(ctx.Identifier().getText());
            currentExpression.add(variable);
            isDeclarator = false;
        } else {
            currentExpression.add(new Variable(ctx.getText(),Variable.Type.CONSTANT));
        }
        if (functionParametersFlag) {
            currentExpression.add(new Variable(",", Variable.Type.CONSTANT));
        }
    }

    public void enterPostfixExpression(CParser.PostfixExpressionContext ctx) {
        if (ctx.LeftParen() != null) {
            currentExpression.add(
                    new Variable(ctx.children.get(0).getText()+"(", Variable.Type.CONSTANT));
            ctx.children.remove(0);
            functionParametersFlag = true;
        }
        if (ctx.PlusPlus() != null || ctx.MinusMinus() != null) {
            Variable prev = getVersion(ctx.postfixExpression().getText());
            Variable cur = getDeclaratorVersion(ctx.postfixExpression().getText());
            currentExpression.add(cur);
            currentExpression.add(new Variable("=",Variable.Type.OPERATOR));
            currentExpression.add(prev);
            if (ctx.MinusMinus() != null) {
                currentExpression.add(new Variable("-", Variable.Type.OPERATOR));
            }
            else {
                currentExpression.add(new Variable("+", Variable.Type.OPERATOR));
            }
            currentExpression.add(new Variable("1", Variable.Type.CONSTANT));
            ctx.children.clear();
            if (iterationCounterFlag) {
                Set<Integer> varVersions = new HashSet<>(cur.getVersion());
                varVersions.addAll(prev.getVersion());
                currentVersions.put(cur.getLabel(),varVersions);
            }
        }
    }

    public void exitPostfixExpression(CParser.PostfixExpressionContext ctx) {
        if (ctx.RightParen() != null) {
            currentExpression.remove(currentExpression.size()-1);
            currentExpression.add(
                    new Variable(")", Variable.Type.CONSTANT));
            functionParametersFlag = false;
        }
    }

    public void enterExpressionStatement(CParser.ExpressionStatementContext ctx) {
        if (ctx.expression().assignmentExpression().assignmentOperator() != null) {
            setDeclaratorFlag();
        }
    }

    public void exitExpressionStatement(CParser.ExpressionStatementContext ctx) {
        addBoxNodeWithCurExpr();
        if (iterationCounterFlag) {
            currentVersions.putAll(Util.mergeVersions(currentVersions,prevVersions));
        }
        prevVersions = null;
    }

    public void enterSelectionStatement(CParser.SelectionStatementContext ctx) {
        if (ctx.Switch() != null) {
            switchStartFlag = true;
        }
    }

    public void exitSelectionStatement(CParser.SelectionStatementContext ctx) {
        List<Node> prev = getPrevNodes();
        if (ctx.Switch() != null) {
            switchEndFlag = true;
            currentVersions = switchNodes.getLast().getResultVersions();
        } else {
            selectionNodes.getLast().addBranchEnd(prevNode);
            prevNode = null;
            selectionNodes.getLast().addBranchEnd(prev);

            currentVersions = selectionNodes.getLast().getResultVersions();
            selectionEndFlag = true;
            /*prevVersions = new HashMap<>(currentVersions);
            for (Map.Entry<String, Set<Integer>> entry : currentVersions.entrySet()) {
                String label = entry.getKey();
                Set<Integer> versions = entry.getValue();
                if (versions.size() > 1) {
                    currentExpression.add(getDeclaratorVersion(label));
                    currentExpression.add(new Variable("=", Variable.Type.OPERATOR));
                    currentExpression.add(getVersion(label));
                    addBoxNodeWithCurExpr();
                    currentExpression.clear();
                }
            }
            prevVersions = null;*/
        }
        currentExpression.clear();
    }

    public void enterIfStatement(CParser.IfStatementContext ctx) {
        addSelectionNode(currentExpression);
        currentExpression.clear();
        labels.addLast(Node.nodeLabel.YES);
        Map<String,Set<Integer>> ifVersions = new HashMap<>(currentVersions);
        currentVersions = ifVersions;
        selectionNodes.getLast().setIfVersions(ifVersions);
    }

    public void exitIfStatement(CParser.IfStatementContext ctx) {
        List<Node> prev = getPrevNodes();
        selectionNodes.getLast().addBranchEnd(prev);
        labels.addLast(Node.nodeLabel.NO);
        selectionNodes.getLast().addBranchEnd(prevNode);
        prevNode = null;
        prevNode = selectionNodes.getLast().getSelectionNode();
        selectionNodes.getLast().setIfVersions(currentVersions);
        currentVersions = new HashMap<>(selectionNodes.getLast().getGlobalVersions());
        selectionNodes.getLast().setElseVersions(currentVersions);
    }

    public void exitElseStatement(CParser.ElseStatementContext ctx) {
        selectionNodes.getLast().setElseVersions(currentVersions);
    }

    public void enterLabeledStatement(CParser.LabeledStatementContext ctx) {
        if (ctx.Identifier() != null) {
            currentExpression.add(
                    new Variable(ctx.Identifier().getText() + ": ", Variable.Type.CONSTANT));
        }
        if (ctx.Case() != null) {
            SwitchNode switchNode;
            if (switchStartFlag) {
                switchNode = new SwitchNode(currentExpression);
            } else {
                switchNode = switchNodes.getLast();
            }
            currentExpression.clear();

            List<Variable> expression = switchNode.getExpression();
            expression.add(new Variable("==", Variable.Type.OPERATOR));
            expression.add(new Variable(
                    ctx.constantExpression().getText(),
                    Variable.Type.CONSTANT));
            ctx.children.remove(1);

            Node selectionNode = addSelectionNode(expression);
            labels.addLast(Node.nodeLabel.YES);

            if (switchEndFlag) {
                addBreaks(selectionNode);
                switchEndFlag = false;
            }
            if (switchStartFlag) {
                switchStartFlag = false;
                switchNodes.addLast(switchNode);
                breakableNodes.addLast(switchNode);
            }
            currentVersions = new HashMap<>(currentVersions);
            selectionNodes.getLast().setIfVersions(currentVersions);
        }
    }

    public void exitLabeledStatement(CParser.LabeledStatementContext ctx) {
        if (ctx.Case() != null) {
            labels.addLast(Node.nodeLabel.NO);
            prevNode = selectionNodes.getLast().getSelectionNode();
            currentVersions = selectionNodes.removeLast().getGlobalVersions();
        }
    }


    public void enterIterationStatement(CParser.IterationStatementContext ctx) {
        if (ctx.Do() != null) {
            iterationStartFlag = true;
            Map<String, Set<Integer>> loopVersions = new HashMap<>(currentVersions);
            IterationNode iterationNode = new IterationNode(
                    currentVersions,loopVersions, IterationNode.LoopType.INVERSE);
            iterationNodes.addLast(iterationNode);
            breakableNodes.addLast(iterationNode);
            currentVersions = loopVersions;
            iterationFlag = true;
        }
    }

    public void exitIterationStatement(CParser.IterationStatementContext ctx) {
        List<Node> prev = getPrevNodes();
        Node continueNode;
        final Node selectionNode = iterationNodes.getLast().getConditionalNode();

        if (ctx.forCondition() != null
                && iterationNodes.getLast().getIterationCounter() != null) {
            Node counterNode = iterationNodes.getLast().getIterationCounter();
            continueNode = counterNode;
            addLinkToPrevNode(counterNode);
            prevNode = counterNode;
        } else {
            continueNode = selectionNode;
        }

        for (Node node : prev) {
            addLink(node,continueNode);
        }
        final Node iterationNode = iterationNodes.getLast().getIterationNode();
        addLinkToPrevNode(iterationNode);
        labels.addLast(Node.nodeLabel.NO);
        prevNode = selectionNode;
        iterationEndFlag = true;

        if (iterationNodes.size() == 1) {
            iterationFlag = false;
        }

        iterationNodes.getLast().resolvePhiCollection(versionsClass);

        if (iterationNodes.size() > 1) {
            Iterator<IterationNode> iterator = iterationNodes.descendingIterator();
            IterationNode innerLoop = iterator.next();
            IterationNode outerLoop = iterator.next();
            outerLoop.mergePhiCollections(innerLoop.getPhiCollection());
        }

        if (ctx.Do() != null) {
            currentVersions = iterationNodes.getLast().getDoLoopResultGlobalVersions();
        } else {
            currentVersions = iterationNodes.getLast().getResultGlobalVersions();
        }
    }

    public void enterIterationConditionExpression(
            CParser.IterationConditionExpressionContext ctx) {
        if (!currentExpression.isEmpty()) {
            addBoxNodeWithCurExpr();
        }

        Map<String, Set<Integer>> loopVersions = new HashMap<>(currentVersions);
        IterationNode iterationNode =
                new IterationNode(currentVersions,loopVersions,IterationNode.LoopType.DIRECT);
        iterationNodes.addLast(iterationNode);
        breakableNodes.addLast(iterationNode);
        currentVersions = loopVersions;
        iterationFlag = true;
    }

    public void exitIterationConditionExpression(
            CParser.IterationConditionExpressionContext ctx) {
        final Node conditionNode = addNode(Node.State.SELECTION,currentExpression,null);
        currentExpression.clear();
        labels.addLast(Node.nodeLabel.YES);
        iterationNodes.getLast().setIterationNodes(conditionNode);
    }

    public void enterForIteratorExpression(CParser.ForIteratorExpressionContext ctx) {
        iterationCounterFlag = true;
    }

    public void exitForIteratorExpression(CParser.ForIteratorExpressionContext ctx) {
        final Node directNode = new Node(nodesCounter, Node.State.BASIC,
                currentExpression);
        graphNodes.add(directNode);
        nodesCounter++;
        iterationNodes.getLast().setIterationCounter(directNode);
        currentExpression.clear();
        iterationCounterFlag = false;
    }

    public void enterPostfixIterationConditionExpression(
            CParser.PostfixIterationConditionExpressionContext ctx) {
        if (!currentExpression.isEmpty()) {
            addBoxNodeWithCurExpr();
        }

        List<Node> prev = getPrevNodes();
        final Node selectionNode = iterationNodes.getLast().getConditionalNode();
        for (Node node : prev) {
            addLink(node,selectionNode);
        }
        iterationNodes.getLast().getResultLoopVersions();
    }

    public void exitPostfixIterationConditionExpression(
            CParser.PostfixIterationConditionExpressionContext ctx) {
        addNode(Node.State.SELECTION,currentExpression,iterationNodes.getLast().getConditionalNode());
        currentExpression.clear();
        labels.addLast(Node.nodeLabel.YES);
    }

    public void enterJumpStatement(CParser.JumpStatementContext ctx) {
        List<Node> prev;
        switch (ctx.start.getText()) {
            case "continue":
                if (!iterationFlag) {
                    throw new IllegalArgumentException("Operator \"continue\" outside loop.");
                }
                prev = getPrevNodes();
                Node node = iterationNodes.getLast().getIterationCounter();
                if (node == null) {
                    node = iterationNodes.getLast().getConditionalNode();
                }
                for (Node prevNode : prev) {
                    addLink(prevNode,node);
                }
                addLinkToPrevNode(node);
                iterationNodes.getLast().addContinueVersions(currentVersions);
                break;
            case "break":
                prev = getPrevNodes();
                breakableNodes.getLast().addBreak(prev,currentVersions);
                breakableNodes.getLast().addBreak(prevNode, currentVersions);
                prevNode = null;
                break;
            case "goto":
                List<Variable> expr = new ArrayList<>();
                expr.add(new Variable(ctx.Goto().getText() + " " + ctx.Identifier(),
                        Variable.Type.CONSTANT));
                addNode(Node.State.BASIC,expr,null);
                prevNode = null;
                break;
            case "return":
                currentExpression.add(new Variable("return ", Variable.Type.CONSTANT));
                break;
            default:
                break;
        }
    }

    public void exitJumpStatement(CParser.JumpStatementContext ctx) {
        if (ctx.start.getText().equals("return")) {
            addBoxNodeWithCurExpr();
            prevNode = null;
        }
        currentVersions.clear();
    }

    public void enterAssignmentOperator(CParser.AssignmentOperatorContext ctx) {
        if (ctx.Assign() != null) {
            currentExpression.add(
                    new Variable(ctx.getText(), Variable.Type.OPERATOR));
        } else {
            String operator = "";
            String initializer =
                    currentExpression.get(currentExpression.size()-1).getLabel();
            currentExpression.add(
                    new Variable("=", Variable.Type.OPERATOR));
            currentExpression.add(getVersion(initializer));
            if (ctx.AndAssign() != null) {
                operator = "&";
            }
            if (ctx.DivAssign() != null) {
                operator = "/";
            }
            if (ctx.MinusAssign() != null) {
                operator = "-";
            }
            if (ctx.LeftShiftAssign() != null) {
                operator = "<<";
            }
            if (ctx.ModAssign() != null) {
                operator = "%";
            }
            if (ctx.OrAssign() != null) {
                operator = "|";
            }
            if (ctx.PlusAssign() != null) {
                operator = "+";
            }
            if (ctx.StarAssign() != null) {
                operator = "*";
            }
            if (ctx.XorAssign() != null) {
                operator = "^";
            }
            if (ctx.RightShiftAssign() != null) {
                operator = ">>";
            }
            currentExpression.add(
                    new Variable(operator, Variable.Type.OPERATOR));
        }
    }

    public void enterAssignmentExpression(CParser.AssignmentExpressionContext ctx) {
        if (ctx.assignmentOperator() != null && ctx.assignmentOperator().Assign() != null) {
            setDeclaratorFlag();
        }
    }

    public void exitAssignmentExpression(CParser.AssignmentExpressionContext ctx) {
        if (ctx.assignmentOperator() != null && ctx.assignmentOperator().Assign() != null) {
            if (iterationCounterFlag) {
                currentVersions.putAll(Util.mergeVersions(currentVersions,prevVersions));
            }
            prevVersions = null;
        }
    }

    public void enterAdditiveOperator(CParser.AdditiveOperatorContext ctx) {
        currentExpression.add(new Variable(ctx.getText(), Variable.Type.OPERATOR));
    }

    public void enterMultiplicativeOperator(CParser.MultiplicativeOperatorContext ctx) {
        currentExpression.add(new Variable(ctx.getText(), Variable.Type.OPERATOR));
    }

    public void enterShiftOperator(CParser.ShiftOperatorContext ctx) {
        currentExpression.add(new Variable(ctx.getText(), Variable.Type.OPERATOR));
    }

    public void enterRelationalOperator(CParser.RelationalOperatorContext ctx) {
        currentExpression.add(new Variable(ctx.getText(), Variable.Type.OPERATOR));
    }

    public void enterEqualityOperator(CParser.EqualityOperatorContext ctx) {
        currentExpression.add(new Variable(ctx.getText(), Variable.Type.OPERATOR));
    }

    public void enterAndOperator(CParser.AndOperatorContext ctx) {
        currentExpression.add(new Variable(ctx.getText(), Variable.Type.OPERATOR));
    }

    public void enterExclusiveOrOperator(CParser.ExclusiveOrOperatorContext ctx) {
        currentExpression.add(new Variable(ctx.getText(), Variable.Type.OPERATOR));
    }

    public void enterInclusiveOrOperator(CParser.InclusiveOrOperatorContext ctx) {
        currentExpression.add(new Variable(ctx.getText(), Variable.Type.OPERATOR));
    }

    public void enterLogicalAndOperator(CParser.LogicalAndOperatorContext ctx) {
        currentExpression.add(new Variable(ctx.getText(), Variable.Type.OPERATOR));
    }

    public void enterLogicalOrOperator(CParser.LogicalOrOperatorContext ctx) {
        currentExpression.add(new Variable(ctx.getText(), Variable.Type.OPERATOR));
    }
}
