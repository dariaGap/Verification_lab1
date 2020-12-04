import guru.nidi.graphviz.model.MutableGraph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ForTest extends BasicTest{

    @Test
    void testSimpleFor() {
        MutableGraph resultGraph = getResult("forLoopTestCodes/simple_for.c");
        MutableGraph expectedGraph = getExpectedResult("expectedResults/expectedFor/simpleFor.dot");

        assertEquals(sortedGraphNodesToString(expectedGraph.nodes()),
                sortedGraphNodesToString(resultGraph.nodes()));
    }

    @Test
    void testBreakContinue() {
        MutableGraph resultGraph = getResult("forLoopTestCodes/break_continue_for.c");
        MutableGraph expectedGraph = getExpectedResult("expectedResults/expectedFor/breakContinueFor.dot");

        assertEquals(sortedGraphNodesToString(expectedGraph.nodes()),
                sortedGraphNodesToString(resultGraph.nodes()));
    }

    @Test
    void testInnerFor() {
        MutableGraph resultGraph = getResult("forLoopTestCodes/inner_for.c");
        MutableGraph expectedGraph = getExpectedResult("expectedResults/expectedFor/innerFor.dot");

        assertEquals(sortedGraphNodesToString(expectedGraph.nodes()),
                sortedGraphNodesToString(resultGraph.nodes()));
    }

    @Test
    void testInnerBreakFor() {
        MutableGraph resultGraph = getResult("forLoopTestCodes/inner_break_for.c");
        MutableGraph expectedGraph = getExpectedResult("expectedResults/expectedFor/innerBreakFor.dot");

        assertEquals(sortedGraphNodesToString(expectedGraph.nodes()),
                sortedGraphNodesToString(resultGraph.nodes()));
    }
}