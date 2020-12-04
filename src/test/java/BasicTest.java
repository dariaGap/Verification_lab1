import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import guru.nidi.graphviz.parse.Parser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

abstract class BasicTest {
    Logger log = LoggerFactory.getLogger(BasicTest.class);

    public MutableGraph getExpectedResult(final String filename) {
        InputStream dot = getClass().getResourceAsStream(filename);
        try {
            MutableGraph g = new Parser().read(dot);
            return g;
        } catch (IOException e) {
            log.error("Can not parse expected result", e);
            return null;
        }
    }

    public MutableGraph getResult(final String filename) {
        InputStream inputStream = IfElseTest.class.getResourceAsStream(filename);
        CLexer lexer = null;
        try {
            lexer = new CLexer(CharStreams.fromStream(inputStream));
        } catch (IOException e) {
            log.error("Can not create lexer", e);
        }
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        CParser parser = new CParser(tokenStream);
        CParser.CompilationUnitContext context = parser.compilationUnit();
        AntlrCListener listener = new AntlrCListener();
        ParseTreeWalker.DEFAULT.walk(listener, context);
        return listener.getGraph();
    }

    public String sortedGraphNodesToString (Collection<MutableNode> graphNodes) {
        MutableNode[] nodes;
        nodes = graphNodes.toArray(MutableNode[]::new);
        Arrays.sort(nodes, (o1, o2) -> o1.name().toString().compareTo(o2.name().toString()));

        String result = "";
        for (int i = 0; i<nodes.length; i++) {
            nodes[i].links().sort((l1,l2) -> l1.name().toString().compareTo(l2.name().toString()));
            result += nodes[i].toString() + "\n";
        }
        return result;
    }
}
