import guru.nidi.graphviz.model.MutableGraph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DoTest extends BasicTest{

    @Test
    void testSimpleDo() {
        MutableGraph resultGraph = getResult("doLoopTestCodes/simple_do.c");
        MutableGraph expectedGraph = getExpectedResult("expectedResults/expectedDo/simpleDo.dot");

        assertEquals(sortedGraphNodesToString(expectedGraph.nodes()),
                sortedGraphNodesToString(resultGraph.nodes()));
    }

    @Test
    void testBreakContinue() {
        MutableGraph resultGraph = getResult("doLoopTestCodes/break_do.c");
        MutableGraph expectedGraph = getExpectedResult("expectedResults/expectedDo/breakDo.dot");

        assertEquals(sortedGraphNodesToString(expectedGraph.nodes()),
                sortedGraphNodesToString(resultGraph.nodes()));
    }

    @Test
    void testInnerDo() {
        MutableGraph resultGraph = getResult("doLoopTestCodes/inner_do.c");
        MutableGraph expectedGraph = getExpectedResult("expectedResults/expectedDo/innerDo.dot");

        assertEquals(sortedGraphNodesToString(expectedGraph.nodes()),
                sortedGraphNodesToString(resultGraph.nodes()));
    }

    @Test
    void testInnerBreakDo() {
        MutableGraph resultGraph = getResult("doLoopTestCodes/inner_break_do.c");
        MutableGraph expectedGraph =
                getExpectedResult("expectedResults/expectedDo/innerBreakDo.dot");

        assertEquals(sortedGraphNodesToString(expectedGraph.nodes()),
                sortedGraphNodesToString(resultGraph.nodes()));
    }
}