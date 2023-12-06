import java.util.ArrayList;
import java.util.List;

public class Node {
    private String name;

    List<Node> edges;

    public Node() {
        this.edges = new ArrayList<Node>();
    }
    public Node(String name) {
        this.name = name;
        this.edges = new ArrayList<Node>();
    }
    public Node(List<Node> edges) {
        this.edges = edges;
    }

    public void addEdge(Node n) {
        edges.add(n);
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Node> getEdges() {
        return edges;
    }

    public void setEdges(List<Node> edges) {
        this.edges = edges;
    }

    public int getStartLine() {
        return startLine;
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public List<String> getCode() {
        return code;
    }

    public void setCode(List<String> code) {
        this.code = code;
    }

    int startLine;
    int endLine;

    List<String> code = new ArrayList<String>();


}
