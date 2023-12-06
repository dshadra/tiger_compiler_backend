import java.util.*;

public class CFGBuilderListener extends IRBaseListener {
    int nodeNameSequence;
    String currentFunctionName;
    List<String> mipsCode = new ArrayList<String>();
    Map<String, String> errors = new HashMap<String, String>();

    Map<String, Node> labelToNodeMap;
    Map<String, List<Node>> functionNodeMap = new HashMap<String, List<Node>>();
    Map<String, List<Edge>> functionEdgeMap = new HashMap<String, List<Edge>>();

    //(a) first statement in the function
    //(b) any statement that is the target of a branch
    //(c) any statement that immediately follows a branch or return statement
    private List<Node> cfgList = new ArrayList<Node>();

    private Node currentCFGRoot;

    private Node currentCFGNode;

    public List<Node> getCfgList() {
        return cfgList;
    }

    public void setCfgList(List<Node> cfgList) {
        this.cfgList = cfgList;
    }

    public Map<String, List<Node>> getFunctionNodeMap() {
        return functionNodeMap;
    }

    public void setFunctionNodeMap(Map<String, List<Node>> functionNodeMap) {
        this.functionNodeMap = functionNodeMap;
    }

    public Map<String, List<Edge>> getFunctionEdgeMap() {
        return functionEdgeMap;
    }

    public void setFunctionEdgeMap(Map<String, List<Edge>> functionEdgeMap) {
        this.functionEdgeMap = functionEdgeMap;
    }

    public List<String> getMipsCode() {
        return mipsCode;
    }

    public void setMipsCode(List<String> mipsCode) {
        this.mipsCode = mipsCode;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }

    @Override public void enterProgram(IRParser.ProgramContext ctx) {
        mipsCode.add(". data # static data section");
        mipsCode.add(". text # code section");


//        . data # static data section
//        # EXAMPLE GLOBAL DATA
//        _a: . word 10 # int initialized to 10
//        _b: . word 65, 66, 67 # array of three ints
//        _c: . float 3.14 # float initialized to 3.14
//        _d: . float 1.0 , 2.0 , 3.0 # array of three floats
//        _e: . space 400 # space (in bytes ) for int [100]
//        _f: . asciiz " Hello   World \n" # null terminated string
//        . text # code section
//        . globl main # used for linking
//        main : # Entry point from SPIM
//        # YOUR CODE HERE
//        jr $ra # return from main

    }

    @Override public void enterFunction(IRParser.FunctionContext ctx) {
        currentFunctionName = ctx.ID().getText();
        currentCFGRoot = new Node(currentFunctionName + nodeNameSequence++);
        currentCFGNode = currentCFGRoot;
        currentCFGRoot.setStartLine(ctx.getStart().getLine() + 3);
        cfgList.add(currentCFGRoot);
        labelToNodeMap = new HashMap<String, Node>();
        functionNodeMap.put(currentFunctionName, new ArrayList<Node>());
        functionEdgeMap.put(currentFunctionName, new ArrayList<Edge>());
    }

    @Override public void enterStat_condition(IRParser.Stat_conditionContext ctx) {
//        currentCFGNode.setEndLine(ctx.getStart().getLine());
        Node labelNode = new Node(currentFunctionName + "-" + ctx.label.getText() + "-" + nodeNameSequence++);
//        currentCFGNode.addEdge(labelNode);
        if(labelToNodeMap.get(ctx.label.getText()) == null) {
            labelToNodeMap.put(ctx.label.getText(), labelNode);
        }
        currentCFGNode.addEdge(labelToNodeMap.get(ctx.label.getText()));
        functionEdgeMap.get(currentFunctionName).add(new Edge(currentCFGNode, labelNode));

        // fall through
//        Node n = new Node(currentFunctionName + nodeNameSequence++);
//        currentCFGNode.addEdge(n);
//        currentCFGNode = n;
//        currentCFGNode.setStartLine(ctx.getStart().getLine()+1);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterStat_label(IRParser.Stat_labelContext ctx) {
        Node labelNode = labelToNodeMap.get(ctx.label.getText());
        if(labelNode == null) {
            labelNode = new Node(currentFunctionName + "-" + ctx.label.getText() + "-" + nodeNameSequence++);
            labelToNodeMap.put(ctx.label.getText(), labelNode);
        }
        labelNode.setStartLine(ctx.getStart().getLine());
        if(currentCFGNode != null) {
            currentCFGNode.setEndLine(ctx.getStart().getLine()-1);
            functionNodeMap.get(currentFunctionName).add(currentCFGNode);
            currentCFGNode.addEdge(labelNode);
            functionEdgeMap.get(currentFunctionName).add(new Edge(currentCFGNode, labelNode));
        }
        currentCFGNode = labelNode;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterStat_goto(IRParser.Stat_gotoContext ctx) {
        currentCFGNode.setEndLine(ctx.getStart().getLine());
        Node n = labelToNodeMap.get(ctx.label.getText());
        if(n == null) {
            n = new Node(currentFunctionName + "-" + ctx.label.getText() + "-" + nodeNameSequence++);
            labelToNodeMap.put(ctx.label.getText(), n);
        }
        currentCFGNode.addEdge(n);
        functionNodeMap.get(currentFunctionName).add(currentCFGNode);
        functionEdgeMap.get(currentFunctionName).add(new Edge(currentCFGNode, n));
        currentCFGNode = null;
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterStat_return(IRParser.Stat_returnContext ctx) {
        currentCFGNode.setEndLine(ctx.getStart().getLine());
        functionNodeMap.get(currentFunctionName).add(currentCFGNode);
    }
}
