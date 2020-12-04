import guru.nidi.graphviz.model.MutableGraph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SwitchTest extends BasicTest{

    @Test
    void testSimpleSwitch() {
        MutableGraph resultGraph = getResult("switchTestCodes/simple_switch.c");
        MutableGraph expectedGraph =
                getExpectedResult("expectedResults/expectedSwitch/simpleSwitch.dot");

        assertEquals(sortedGraphNodesToString(expectedGraph.nodes()),
                sortedGraphNodesToString(resultGraph.nodes()));
    }

    @Test
    void testInnerSwitch() {
        MutableGraph resultGraph = getResult("switchTestCodes/inner_switch.c");
        MutableGraph expectedGraph =
                getExpectedResult("expectedResults/expectedSwitch/innerSwitch.dot");
        assertEquals(sortedGraphNodesToString(expectedGraph.nodes()),
                sortedGraphNodesToString(resultGraph.nodes()));
    }
}