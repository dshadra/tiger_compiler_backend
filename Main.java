import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String args[]) throws IOException {
        boolean REGISTER_ALLOCATION_NAIVE = false;
        boolean REGISTER_ALLOCATION_LOCAL = false;
        boolean REGISTER_ALLOCATION_GLOBAL = false;
        boolean outputGraph = false;
        boolean outputLiveness = false;
        String irFileName = "";
        String tigerFileName = "";

        int i = 0;
        List<String> argsList = (Arrays.stream(args).toList());
        System.out.println("Main called with args " + argsList);
        if (argsList.contains("-r")) {
            irFileName = argsList.get(argsList.indexOf("-r") + 1);
        }
        if (argsList.contains("-f")) {
            tigerFileName = argsList.get(argsList.indexOf("-f") + 1);
        }
        if (irFileName != null || tigerFileName != null) {
            for (String arg : argsList) {
                if (arg.equalsIgnoreCase("-n")) {
                    REGISTER_ALLOCATION_NAIVE = true;
                } else if (arg.equalsIgnoreCase("-l")) {
                    REGISTER_ALLOCATION_LOCAL = true;
                } else if (arg.equalsIgnoreCase("-g")) {
                    REGISTER_ALLOCATION_GLOBAL = true;
                }
            }

            IRLexer lexer = new IRLexer(new ANTLRFileStream(irFileName));
            CommonTokenStream tokens = new CommonTokenStream((TokenSource) lexer);
            IRParser parser = new IRParser((TokenStream) tokens);
            ParseTree p = parser.program();
            ParseTreeWalker walker = new ParseTreeWalker();

            if(REGISTER_ALLOCATION_GLOBAL) {
                System.out.println("Global Register Allocation");
                CFGBuilderListener cfgBuilderListener = new CFGBuilderListener();
                walker.walk(cfgBuilderListener, p);
                Map<String, String> errors = cfgBuilderListener.getErrors();
                if (errors != null && !errors.isEmpty()) {
                    for (String key : errors.keySet()) {
                        System.out.println(key + " : " + errors.get(key));
                    }
                } else {
                    for (String key : cfgBuilderListener.getFunctionNodeMap().keySet()) {
                        List<Node> nodes = cfgBuilderListener.getFunctionNodeMap().get(key);
                        for (Node n : nodes) {
                            System.out.println("Node(" + n.getName() + ") [ " + n.getStartLine() + " , " + n.getEndLine() + " ] ");
                        }
                    }

                    for (String key : cfgBuilderListener.getFunctionEdgeMap().keySet()) {
                        List<Edge> edges = cfgBuilderListener.getFunctionEdgeMap().get(key);
                        for (Edge e : edges) {
                            System.out.println("Edge(" + e.getFrom().getName() + ", " + e.getTo().getName() + " ) ");
                        }
                    }
                }
            }

            if (REGISTER_ALLOCATION_NAIVE) {
                System.out.println("Naive Register Allocation");
                Files.lines(new File(irFileName).toPath())
                        .map(s -> s.trim())
                        .forEach(System.out::println);
                System.out.println("**********");

                NaiveMipsCodeGeneratorListener mipsCodeGeneratorListener = new NaiveMipsCodeGeneratorListener();
                walker.walk(mipsCodeGeneratorListener, p);
                Map<String, String> errors = mipsCodeGeneratorListener.getErrors();
                if (errors != null && !errors.isEmpty()) {
                    for (String key : errors.keySet()) {
                        System.out.println(key + " : " + errors.get(key));
                    }
                } else {
                    List<String> s = mipsCodeGeneratorListener.getMipsCode();
                    String output = "";
                    for (String line : s) {
                        output = output + line + "\n";
                    }
                    String outputFileName = "";
                    if (irFileName.indexOf(".") >= 0) {
                        outputFileName = irFileName.substring(0, irFileName.lastIndexOf(".")) + ".naive.s";
                    } else {
                        outputFileName = irFileName + ".naive.s";
                    }
                    Files.write(Paths.get(outputFileName),
                            output.getBytes(StandardCharsets.UTF_8),
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING);
                    System.out.println("File " + outputFileName + " created.");

                }
            }
        }
    }
}
