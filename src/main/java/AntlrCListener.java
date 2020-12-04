import guru.nidi.graphviz.model.MutableGraph;
import model.*;
import util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static guru.nidi.graphviz.model.Factory.mutGraph;

public class AntlrCListener extends CBaseListener {
    private MutableGraph graph;
    private final List<Variable> currentExpression = new ArrayList<>();
    private GraphInfo graphInfo = new GraphInfo();

    public MutableGraph getGraph() {
        graphInfo.getGraphNodes().forEach(node -> node.addLinks(graph));
        return graph;
    }

    public void addBoxNodeWithCurExpr() {
        graphInfo.addNode(Node.State.BASIC,currentExpression,null);
        currentExpression.clear();
    }

    public void enterCompilationUnit(CParser.CompilationUnitContext ctx) {
        graph = mutGraph("gr").setDirected(true);
        graphInfo.setCurrentVersions(new HashMap<>());
    }

    public void enterFunctionDeclarator(CParser.FunctionDeclaratorContext ctx) {
        graphInfo.getFlags().setFunctionDeclarationFlag(true);
    }

    public void exitFunctionDeclarator(CParser.FunctionDeclaratorContext ctx) {
        graphInfo.getFlags().setFunctionDeclarationFlag(false);
        graphInfo.getFlags().setFunctionParametersFlag(false);
        if (currentExpression.size()>2) {
            currentExpression.remove(currentExpression.size()-1);
        }
        currentExpression.add(new Variable(")", Variable.Type.CONSTANT));
        addBoxNodeWithCurExpr();
    }

    public void enterParameterTypeList(CParser.ParameterTypeListContext ctx) {
        graphInfo.getFlags().setFunctionParametersFlag(true);
    }

    public void enterDirectDeclarator(CParser.DirectDeclaratorContext ctx) {
        if (graphInfo.getFlags().isFunctionDeclarationFlag() && ctx.children.size() == 1) {
            if (graphInfo.getFlags().isFunctionParametersFlag()) {
                currentExpression.add(
                        graphInfo.getDeclaratorVersion(ctx.Identifier().getText()));
                currentExpression.add(
                        new Variable(",", Variable.Type.CONSTANT));
            } else {
                currentExpression.add(
                        new Variable(ctx.Identifier().getText(),Variable.Type.CONSTANT));
                currentExpression.add(
                        new Variable("(", Variable.Type.CONSTANT));
            }
        }
    }

    public void enterInitDeclarator(CParser.InitDeclaratorContext ctx) {
        if (ctx.initializer() != null) {
            Variable declarator = graphInfo.getDeclaratorVersion(
                    ctx.declarator().directDeclarator().getChild(0).getText());
            currentExpression.add(declarator);
            currentExpression.add(new Variable("=", Variable.Type.OPERATOR));
            graphInfo.getFlags().setDeclarator(false);
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
            Variable variable = graphInfo.getFlags().isDeclarator()
                    ? graphInfo.getDeclaratorVersion(ctx.Identifier().getText())
                    : graphInfo.getVersion(ctx.Identifier().getText());
            currentExpression.add(variable);
            graphInfo.getFlags().setDeclarator(false);
        } else {
            currentExpression.add(new Variable(ctx.getText(),Variable.Type.CONSTANT));
        }
        if (graphInfo.getFlags().isFunctionParametersFlag()) {
            currentExpression.add(new Variable(",", Variable.Type.CONSTANT));
        }
    }

    public void enterPostfixExpression(CParser.PostfixExpressionContext ctx) {
        if (ctx.LeftParen() != null) {
            currentExpression.add(
                    new Variable(ctx.children.get(0).getText()+"(",
                            Variable.Type.CONSTANT));
            ctx.children.remove(0);
            graphInfo.getFlags().setFunctionParametersFlag(true);
        }
        if (ctx.PlusPlus() != null || ctx.MinusMinus() != null) {
            Variable prev = graphInfo.getVersion(ctx.postfixExpression().getText());
            Variable cur = graphInfo
                    .getDeclaratorVersion(ctx.postfixExpression().getText());
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
            if (graphInfo.getFlags().isIterationCounterFlag()) {
                Set<Integer> varVersions = new HashSet<>(cur.getVersion());
                varVersions.addAll(prev.getVersion());
                graphInfo.getCurrentVersions().put(cur.getLabel(),varVersions);
            }
        }
    }

    public void exitPostfixExpression(CParser.PostfixExpressionContext ctx) {
        if (ctx.RightParen() != null) {
            currentExpression.remove(currentExpression.size()-1);
            currentExpression.add(
                    new Variable(")", Variable.Type.CONSTANT));
            graphInfo.getFlags().setFunctionParametersFlag(false);
        }
    }

    public void enterExpressionStatement(CParser.ExpressionStatementContext ctx) {
        if (ctx.expression().assignmentExpression().assignmentOperator() != null) {
            graphInfo.setDeclaratorFlag();
        }
    }

    public void exitExpressionStatement(CParser.ExpressionStatementContext ctx) {
        addBoxNodeWithCurExpr();
        if (graphInfo.getFlags().isIterationCounterFlag()) {
            graphInfo.getCurrentVersions().putAll(
                    Util.mergeVersions(graphInfo
                            .getCurrentVersions(),graphInfo.getPrevVersions()));
        }
        graphInfo.setPrevVersions(null);
    }

    public void enterSelectionStatement(CParser.SelectionStatementContext ctx) {
        if (ctx.Switch() != null) {
            graphInfo.getFlags().setSwitchStartFlag(true);
        }
    }

    public void exitSelectionStatement(CParser.SelectionStatementContext ctx) {
        List<Node> prev = graphInfo.getPrevNodes();
        if (ctx.Switch() != null) {
            graphInfo.getFlags().setSwitchEndFlag(true);
            graphInfo.setCurrentVersions(
                    graphInfo.getCollections()
                            .getLastSwitchNode().getResultVersions());
        } else {
            graphInfo.getCollections()
                    .getLastSelectionNode()
                    .addBranchEnd(graphInfo.getPrevNode());
            graphInfo.setPrevNode(null);
            graphInfo.getCollections()
                    .getLastSelectionNode().addBranchEnd(prev);

            graphInfo.setCurrentVersions(
                    graphInfo.getCollections()
                            .getLastSelectionNode().getResultVersions());
            graphInfo.getFlags().setSelectionEndFlag(true);
        }
        currentExpression.clear();
    }

    public void enterIfStatement(CParser.IfStatementContext ctx) {
        graphInfo.addSelectionNode(currentExpression);
        currentExpression.clear();
        graphInfo.getLabels().addLast(Node.nodeLabel.YES);
        Map<String,Set<Integer>> ifVersions =
                new HashMap<>(graphInfo.getCurrentVersions());
        graphInfo.setCurrentVersions(ifVersions);
        graphInfo.getCollections().getLastSelectionNode().setIfVersions(ifVersions);
    }

    public void exitIfStatement(CParser.IfStatementContext ctx) {
        List<Node> prev = graphInfo.getPrevNodes();
        graphInfo.getCollections().getLastSelectionNode().addBranchEnd(prev);
        graphInfo.getLabels().addLast(Node.nodeLabel.NO);
        graphInfo.getCollections().getLastSelectionNode()
                .addBranchEnd(graphInfo.getPrevNode());
        graphInfo.setPrevNode(null);
        graphInfo.setPrevNode(graphInfo.getCollections()
                .getLastSelectionNode().getSelectionNode());
        graphInfo.getCollections().getLastSelectionNode()
                .setIfVersions(graphInfo.getCurrentVersions());
        graphInfo.setCurrentVersions(new HashMap<>(
                graphInfo.getCollections().getLastSelectionNode().getGlobalVersions()));
        graphInfo.getCollections().getLastSelectionNode()
                .setElseVersions(graphInfo.getCurrentVersions());
    }

    public void exitElseStatement(CParser.ElseStatementContext ctx) {
        graphInfo.getCollections()
                .getLastSelectionNode()
                .setElseVersions(graphInfo.getCurrentVersions());
    }

    public void enterLabeledStatement(CParser.LabeledStatementContext ctx) {
        if (ctx.Identifier() != null) {
            currentExpression.add(
                    new Variable(ctx.Identifier().getText() + ": ",
                            Variable.Type.CONSTANT));
        }
        if (ctx.Case() != null) {
            SwitchNode switchNode;
            if (graphInfo.getFlags().isSwitchStartFlag()) {
                switchNode = new SwitchNode(currentExpression);
            } else {
                switchNode = graphInfo.getCollections().getLastSwitchNode();
            }
            currentExpression.clear();

            List<Variable> expression = switchNode.getExpression();
            expression.add(new Variable("==", Variable.Type.OPERATOR));
            expression.add(new Variable(
                    ctx.constantExpression().getText(),
                    Variable.Type.CONSTANT));
            ctx.children.remove(1);

            Node selectionNode = graphInfo.addSelectionNode(expression);
            graphInfo.getLabels().addLast(Node.nodeLabel.YES);

            if (graphInfo.getFlags().isSwitchEndFlag()) {
                graphInfo.addBreaks(selectionNode);
                graphInfo.getFlags().setSwitchEndFlag(false);
            }
            if (graphInfo.getFlags().isSwitchStartFlag()) {
                graphInfo.getFlags().setSwitchStartFlag(false);
                graphInfo.getCollections().setLastSwitchNode(switchNode);
                graphInfo.getCollections().setLastBreakableNode(switchNode);
            }
            graphInfo.setCurrentVersions(
                    new HashMap<>(graphInfo.getCurrentVersions()));
            graphInfo.getCollections().getLastSelectionNode()
                    .setIfVersions(graphInfo.getCurrentVersions());
        }
    }

    public void exitLabeledStatement(CParser.LabeledStatementContext ctx) {
        if (ctx.Case() != null) {
            graphInfo.getLabels().addLast(Node.nodeLabel.NO);
            graphInfo.setPrevNode(
                    graphInfo.getCollections()
                            .getLastSelectionNode().getSelectionNode());
            graphInfo.setCurrentVersions(
                    graphInfo.getCollections()
                            .removeLastSelectionNode().getGlobalVersions());
        }
    }


    public void enterIterationStatement(CParser.IterationStatementContext ctx) {
        if (ctx.Do() != null) {
            graphInfo.getFlags().setIterationStartFlag(true);
            Map<String, Set<Integer>> loopVersions =
                    new HashMap<>(graphInfo.getCurrentVersions());
            IterationNode iterationNode = new IterationNode(
                    graphInfo.getCurrentVersions(),loopVersions,
                    IterationNode.LoopType.INVERSE);
            graphInfo.getCollections().setLastIterationNode(iterationNode);
            graphInfo.getCollections().setLastBreakableNode(iterationNode);
            graphInfo.setCurrentVersions(loopVersions);
            graphInfo.getFlags().setIterationFlag(true);
        }
    }

    public void exitIterationStatement(CParser.IterationStatementContext ctx) {
        List<Node> prev = graphInfo.getPrevNodes();
        Node continueNode;

        graphInfo.getCollections()
                .getLastIterationNode()
                .resolvePhiCollection(graphInfo.getVersionsClass());

        final Node selectionNode =
                graphInfo.getCollections()
                        .getLastIterationNode().getConditionalNode();

        if (ctx.forCondition() != null
                && graphInfo.getCollections()
                .getLastIterationNode()
                .getIterationCounter() != null) {
            Node counterNode =
                    graphInfo.getCollections()
                            .getLastIterationNode()
                            .getIterationCounter();
            continueNode = counterNode;
            graphInfo.addLinkToPrevNode(counterNode);
            graphInfo.setPrevNode(counterNode);
        } else {
            continueNode = selectionNode;
        }

        for (Node node : prev) {
            graphInfo.addLink(node,continueNode);
        }
        final Node iterationNode =
                graphInfo.getCollections().getLastIterationNode().getIterationNode();
        graphInfo.addLinkToPrevNode(iterationNode);
        graphInfo.getLabels().addLast(Node.nodeLabel.NO);
        graphInfo.setPrevNode(selectionNode);
        graphInfo.getFlags().setIterationEndFlag(true);

        if (graphInfo.getCollections().getIterationNodesSize() == 1) {
            graphInfo.getFlags().setIterationFlag(false);
        }

        if (graphInfo.getCollections().getIterationNodesSize() > 1) {
            Iterator<IterationNode> iterator = graphInfo
                    .getCollections()
                    .getIterationNodesIterator();
            IterationNode innerLoop = iterator.next();
            IterationNode outerLoop = iterator.next();
            outerLoop.mergePhiCollections(innerLoop.getPhiCollection());
        }

        if (ctx.Do() != null) {
            graphInfo.setCurrentVersions(
                    graphInfo.getCollections()
                            .getLastIterationNode()
                            .getDoLoopResultGlobalVersions());
        } else {
            graphInfo.setCurrentVersions(
                    graphInfo.getCollections()
                            .getLastIterationNode()
                            .getResultGlobalVersions());
        }
    }

    public void enterIterationConditionExpression(
            CParser.IterationConditionExpressionContext ctx) {
        if (!currentExpression.isEmpty()) {
            addBoxNodeWithCurExpr();
        }

        Map<String, Set<Integer>> loopVersions =
                new HashMap<>(graphInfo.getCurrentVersions());
        IterationNode iterationNode =
                new IterationNode(
                        graphInfo.getCurrentVersions(),
                        loopVersions,IterationNode.LoopType.DIRECT);
        graphInfo.getCollections().setLastIterationNode(iterationNode);
        graphInfo.getCollections().setLastBreakableNode(iterationNode);
        graphInfo.setCurrentVersions(loopVersions);
        graphInfo.getFlags().setIterationFlag(true);
    }

    public void exitIterationConditionExpression(
            CParser.IterationConditionExpressionContext ctx) {
        final Node conditionNode = graphInfo.addNode(
                Node.State.SELECTION,currentExpression,null);
        currentExpression.clear();
        graphInfo.getLabels().addLast(Node.nodeLabel.YES);
        graphInfo.getCollections()
                .getLastIterationNode().setIterationNodes(conditionNode);
    }

    public void enterForIteratorExpression(CParser.ForIteratorExpressionContext ctx) {
        graphInfo.getFlags().setIterationCounterFlag(true);
    }

    public void exitForIteratorExpression(CParser.ForIteratorExpressionContext ctx) {
        final Node directNode = new Node(graphInfo.getNodesCounter(), Node.State.BASIC,
                currentExpression);
        graphInfo.graphNodesAdd(directNode);
        graphInfo.incrementNodesCounter();
        graphInfo.getCollections().getLastIterationNode().setIterationCounter(directNode);
        currentExpression.clear();
        graphInfo.getFlags().setIterationCounterFlag(false);
    }

    public void enterPostfixIterationConditionExpression(
            CParser.PostfixIterationConditionExpressionContext ctx) {
        if (!currentExpression.isEmpty()) {
            addBoxNodeWithCurExpr();
        }

        List<Node> prev = graphInfo.getPrevNodes();
        final Node selectionNode =
                graphInfo.getCollections().getLastIterationNode().getConditionalNode();
        for (Node node : prev) {
            graphInfo.addLink(node,selectionNode);
        }
        graphInfo.getCollections().getLastIterationNode().getResultLoopVersions();
    }

    public void exitPostfixIterationConditionExpression(
            CParser.PostfixIterationConditionExpressionContext ctx) {
        graphInfo.addNode(Node.State.SELECTION,currentExpression,
                graphInfo.getCollections().getLastIterationNode().getConditionalNode());
        currentExpression.clear();
        graphInfo.getLabels().addLast(Node.nodeLabel.YES);
    }

    public void enterJumpStatement(CParser.JumpStatementContext ctx) {
        List<Node> prev;
        switch (ctx.start.getText()) {
            case "continue":
                if (!graphInfo.getFlags().isIterationFlag()) {
                    throw new IllegalArgumentException(
                            "Operator \"continue\" outside loop.");
                }
                prev = graphInfo.getPrevNodes();
                Node node = graphInfo
                        .getCollections()
                        .getLastIterationNode()
                        .getIterationCounter();
                if (node == null) {
                    node = graphInfo
                            .getCollections()
                            .getLastIterationNode()
                            .getConditionalNode();
                }
                for (Node prevNode : prev) {
                    graphInfo.addLink(prevNode,node);
                }
                graphInfo.addLinkToPrevNode(node);
                graphInfo.getCollections()
                        .getLastIterationNode()
                        .addContinueVersions(graphInfo.getCurrentVersions());
                break;
            case "break":
                prev = graphInfo.getPrevNodes();
                graphInfo.getCollections()
                        .getLastBreakableNode()
                        .addBreak(prev,graphInfo.getCurrentVersions());
                graphInfo.getCollections()
                        .getLastBreakableNode()
                        .addBreak(graphInfo.getPrevNode(),graphInfo.getCurrentVersions());
                graphInfo.setPrevNode(null);
                break;
            case "goto":
                List<Variable> expr = new ArrayList<>();
                expr.add(new Variable(ctx.Goto().getText() + " " + ctx.Identifier(),
                        Variable.Type.CONSTANT));
                graphInfo.addNode(Node.State.BASIC,expr,null);
                graphInfo.setPrevNode(null);
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
            graphInfo.setPrevNode(null);
        }
        graphInfo.getCurrentVersions().clear();
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
            currentExpression.add(graphInfo.getVersion(initializer));
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
            graphInfo.setDeclaratorFlag();
        }
    }

    public void exitAssignmentExpression(CParser.AssignmentExpressionContext ctx) {
        if (ctx.assignmentOperator() != null && ctx.assignmentOperator().Assign() != null) {
            if (graphInfo.getFlags().isIterationCounterFlag()) {
                graphInfo.getCurrentVersions().putAll(
                                Util.mergeVersions(
                                        graphInfo.getCurrentVersions(),
                                        graphInfo.getPrevVersions()));
            }
            graphInfo.setPrevVersions(null);
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
