import guru.nidi.graphviz.model.MutableGraph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WhileTest extends BasicTest{

    @Test
    void testSimpleWhile() {
        MutableGraph resultGraph = getResult("whileLoopTestCodes/simple_while.c");
        MutableGraph expectedGraph = getExpectedResult("expectedResults/expectedWhile/simpleWhile.dot");

        assertEquals(sortedGraphNodesToString(expectedGraph.nodes()),
                sortedGraphNodesToString(resultGraph.nodes()));
    }

    @Test
    void testBreakContinue() {
        MutableGraph resultGraph = getResult("whileLoopTestCodes/break_while.c");
        MutableGraph expectedGraph = getExpectedResult("expectedResults/expectedWhile/breakWhile.dot");

        assertEquals(sortedGraphNodesToString(expectedGraph.nodes()),
                sortedGraphNodesToString(resultGraph.nodes()));
    }

    @Test
    void testInnerWhile() {
        MutableGraph resultGraph = getResult("whileLoopTestCodes/inner_while.c");
        MutableGraph expectedGraph = getExpectedResult("expectedResults/expectedWhile/innerWhile.dot");

        assertEquals(sortedGraphNodesToString(expectedGraph.nodes()),
                sortedGraphNodesToString(resultGraph.nodes()));
    }

    @Test
    void testInnerBreakWhile() {
        MutableGraph resultGraph =
                getResult("whileLoopTestCodes/inner_break_while.c");
        MutableGraph expectedGraph =
                getExpectedResult("expectedResults/expectedWhile/innerBreakWhile.dot");

        assertEquals(sortedGraphNodesToString(expectedGraph.nodes()),
                sortedGraphNodesToString(resultGraph.nodes()));
    }
}