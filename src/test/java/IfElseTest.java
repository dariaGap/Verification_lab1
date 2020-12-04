import static org.junit.jupiter.api.Assertions.*;

import guru.nidi.graphviz.model.MutableGraph;
import org.junit.jupiter.api.Test;

class IfElseTest extends BasicTest{

    /**
     * Test if/else construction without inner if/else, with if and else branches and only with if branch.
     */
    @Test
    void testSimpleIf() {
        MutableGraph resultGraph = getResult("ifElseTestCodes/simple_if.c");
        MutableGraph expectedGraph = getExpectedResult("expectedResults/expectedIf/simpleIf.dot");

        assertEquals(sortedGraphNodesToString(expectedGraph.nodes()),
                sortedGraphNodesToString(resultGraph.nodes()));
    }

    @Test
    void testInnerIf() {
        MutableGraph resultGraph = getResult("ifElseTestCodes/inner_if.c");
        MutableGraph expectedGraph = getExpectedResult("expectedResults/expectedIf/innerIf.dot");

        assertEquals(sortedGraphNodesToString(expectedGraph.nodes()),
                sortedGraphNodesToString(resultGraph.nodes()));
    }
}