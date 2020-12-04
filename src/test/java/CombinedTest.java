import guru.nidi.graphviz.model.MutableGraph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CombinedTest extends BasicTest{

    @Test
    void testCombined() {
        MutableGraph resultGraph = getResult("combinedStructuresTestCodes/combined.c");
        MutableGraph expectedGraph =
                getExpectedResult("expectedResults/expectedCombined/combined.dot");

        assertEquals(sortedGraphNodesToString(expectedGraph.nodes()),
                sortedGraphNodesToString(resultGraph.nodes()));
    }

    @Test
    void testCombinedWhileIf() {
        MutableGraph resultGraph = getResult("combinedStructuresTestCodes/combined_while_if.c");
        MutableGraph expectedGraph =
                getExpectedResult("expectedResults/expectedCombined/combinedWhileIf.dot");

        assertEquals(sortedGraphNodesToString(expectedGraph.nodes()),
                sortedGraphNodesToString(resultGraph.nodes()));
    }

    @Test
    void testCombinedIfSwitch() {
        MutableGraph resultGraph = getResult("combinedStructuresTestCodes/combined_if_switch.c");
        MutableGraph expectedGraph =
                getExpectedResult("expectedResults/expectedCombined/combinedIfSwitch.dot");

        assertEquals(sortedGraphNodesToString(expectedGraph.nodes()),
                sortedGraphNodesToString(resultGraph.nodes()));
    }

    @Test
    void testCombinedSwitchIf() {
        MutableGraph resultGraph = getResult("combinedStructuresTestCodes/combined_switch_if.c");
        MutableGraph expectedGraph =
                getExpectedResult("expectedResults/expectedCombined/combinedSwitchIf.dot");

        assertEquals(sortedGraphNodesToString(expectedGraph.nodes()),
                sortedGraphNodesToString(resultGraph.nodes()));
    }
}