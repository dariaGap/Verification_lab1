import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizCmdLineEngine;
import guru.nidi.graphviz.model.MutableGraph;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SSA {
    public static void main(String[] args) {
        try {
            File file = new File(args[0]);
            InputStream inputStream = new FileInputStream(file);
            CLexer lexer = new CLexer(CharStreams.fromStream(inputStream));
            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            CParser parser = new CParser(tokenStream);
            CParser.CompilationUnitContext context = parser.compilationUnit();
            AntlrCListener listener = new AntlrCListener();
            ParseTreeWalker.DEFAULT.walk(listener, context);
            Graphviz.useEngine(new GraphvizCmdLineEngine());
            MutableGraph g = listener.getGraph();
            Graphviz.fromGraph(g).render(Format.PNG)
                    .toFile(new File("result\\SSA_result.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
