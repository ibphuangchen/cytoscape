import cern.colt.map.OpenIntIntHashMap;
import cern.colt.map.OpenIntObjectHashMap;
import cern.colt.list.IntArrayList;
import cern.colt.list.ObjectArrayList;

import giny.model.RootGraph;

import cytoscape.util.GinyFactory;
import cytoscape.data.mRNAMeasurement;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import java.io.IOException;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * A bipartite graph of Variable and Factor nodes that represents
 * constraints on paths in a physical interaction network that
 * explain knockout effects.
 *
 * <p>
 * Ref: Kschischang, Frey, Loeliger. IEEE Trans Inf Theory. Feb 2001
 * 
 * 
 *  TODO: Separate Max-Product code from factor graph construction code
 * 
 */
public class FactorGraph
{
    // used to compute the a priori probabilities of
    // knockout nodes
    private static double koinv = 1e-20;

    // used to compute likelihood ratios
    private static double NUM_REPLICATES = 3;
    private static double lnN = Math.log(NUM_REPLICATES);

    // the FactorGraph
    protected RootGraph _g;
    
    // map node index to a VariableNode or FactorNode object
    protected OpenIntObjectHashMap _nodeMap;

    // map path number to path-factor node index
    private OpenIntIntHashMap _pathMap; 

    protected IntArrayList _pathActive;
    protected IntArrayList _vars;
    protected IntArrayList _factors;
    protected IntArrayList _sign;
    
    // map interaction edge index to AnnotatedEdge
    protected OpenIntObjectHashMap _edgeMap;

    // map edge presence variable index to AnnotatedEdge object
    protected OpenIntObjectHashMap _x2aeMap;

    // map edge direction variable index to AnnotatedEdge object
    protected OpenIntObjectHashMap _d2aeMap;
    
    // map edge sign variable index to AnnotatedEdge object
    protected OpenIntObjectHashMap _s2aeMap;
    
    // the InteractionGraph that this FactorGraph is based on
    protected InteractionGraph _ig;

    // the PathResults that this FactorGraph is based on
    protected PathResult _paths;
    
    // adjaceny list of edge messages
    // maps a factor graph node index to the List of EdgeMessages
    // that connect that node to its neighbors in the factor graph.
    // Used for MaxProduct algorithm.
    protected IntListMap _adjacencyMap;

    // dependency relationships between nodes
    //protected DependencyGraph _dependencyGraph;

    protected List _submodels;
    
    /**
     * So the PrintableFactorGraph subclass can use
     * a different type of underlying graph
     */
    protected RootGraph _newRootGraph()
    {
        //return new DummyRootGraph();
        //return GinyFactory.createRootGraph();

        return new AdjacencyListRootGraph();
    }


    protected FactorGraph(InteractionGraph ig, PathResult pathResults)
    {
        _ig = ig;
        _paths = pathResults;
        _g = _newRootGraph();

        //_dependencyGraph = new DependencyGraph(_g.getNodeCount());

        _submodels = new ArrayList();
        
        IntListMap edge2path = pathResults.getEdge2PathMap();

        int nC = 0;
        int nN = 0; // number of nodes
        int nE = 0; // number of edges
        
        IntArrayList kos = pathResults.getKOs();
        IntArrayList edges = edge2path.keys();
        
        // number of nodes in factor graph =
        //     2 * #knockouts
        //   + 2*number-of-paths 
        //   + 3 * number of unique edges used on paths
        nN = 2 * kos.size() + 2 * pathResults.getPathCount() + 3 * edges.size();
        
        // number of edges: 3 edges connect path-factor node to sigma, k, and OR
        // 3 edges connect path-factor to edge, dir, sign variables
        // 
        // lower bound = 3 * pathcount + 3 * 1 * pathCount (1 edge)
        // upper bound = 3 * pathcount + 3 * 5 * pathCount (5 edge)
        //
        // use 4 edges per path = (3 + 12 = 15) * pathcount
        nE = 12 * pathResults.getPathCount();

        System.out.println("Initializing factor graph. " + nN + " nodes, " +
                           nE + " estimated edges.");
        
        _g.ensureCapacity(nN, nE);

        _nodeMap = new OpenIntObjectHashMap(nN);
        _pathMap = new OpenIntIntHashMap(pathResults.getPathCount());

        _edgeMap = new OpenIntObjectHashMap(edges.size());
        _x2aeMap = new OpenIntObjectHashMap(edges.size());
        _d2aeMap = new OpenIntObjectHashMap(edges.size());
        _s2aeMap = new OpenIntObjectHashMap(edges.size());

        _adjacencyMap = new IntListMap(nN);
            
        _pathActive = new IntArrayList(pathResults.getPathCount());
        _factors = new IntArrayList(kos.size() + pathResults.getPathCount());
        _vars = new IntArrayList(nN - kos.size() - pathResults.getPathCount());

        _sign = new IntArrayList(edges.size());
    }

    public static FactorGraph create(InteractionGraph ig, PathResult pathResults)
    {
        FactorGraph fg = new FactorGraph(ig, pathResults);
        fg.buildGraph(ig, pathResults);
        return fg;
    }
    
    /**
     * Create a FactorGraph from an InteractionGraph and the
     * results of finding paths in that InteractionGraph.
     *
     * @param pathResults a DFSPath results object
     * @param ig an interaction graph.  The methods setExpressionData
     * and setEdgeData must have been called on the interaction graph.
     */
    protected void buildGraph(InteractionGraph ig, PathResult pathResults)
    {
        IntListMap edge2path = pathResults.getEdge2PathMap();
        IntArrayList kos = pathResults.getKOs();
        IntArrayList edges = edge2path.keys();

        int cN = 0;
        int cE = 0;
        
        for(int x=0, N = kos.size(); x < N; x++)
        {
            int knockedOut =  kos.get(x);
            Target2PathMap target2path = pathResults.getTarget2PathMap(knockedOut);

            IntArrayList targets = target2path.keys();

            for(int i=0, Nt = targets.size(); i < Nt; i++)
            {
                int t = targets.get(i);
                int ko = createKO(knockedOut, t);
                int or = createOR(knockedOut, t);

                IntArrayList paths = (IntArrayList) target2path.get(t);

                cN += 2;

                for(int j=0, Np =paths.size(); j < Np; j++)
                {
                    int path = paths.get(j);
                    int pa = createPathActive(path);
                    int pf = createPathFactor(path);

                    cN += 2;
                    cE += 3;
                    
                    connect(pa, pf);
                    connect(ko, pf);
                    connect(pa, or);
                }
            }
        }
        
        System.out.println("processed ko, OR, path nodes. cN=" + cN + ", cE=" + cE);
        
        for(int i=0, Ne = edges.size(); i < Ne; i++)
        {
            int edge = edges.get(i);

            AnnotatedEdge aEdge = createAnnotatedEdge(edge);
            cN += 3;
            
            List pathIntervals = edge2path.get(edge);
            
            for(int j=0, Np =pathIntervals.size(); j < Np; j++)
            {
                PathResult.Interval paths = (PathResult.Interval) pathIntervals.get(j);
                for(int k=paths.getStart(), m=paths.getEnd(); k < m; k++)
                {
                    int pf = getPathFactorIndex(k);
                    connect(aEdge.fgIndex, pf);
                    connect(aEdge.dirIndex, pf, paths.getDir());
                    connect(aEdge.signIndex, pf);                

                    cE += 3;
                }
            }
        }

        System.out.println("processed edges. total cN=" + cN + ", cE=" + cE);
    }

    /**
     * @return the RootGraph index of the PathFactorNode that corresponds to
     * pathNumber or 0 if pathNumber is not a known path.
     */
    private int getPathFactorIndex(int pathNumber)
    {
        return _pathMap.get(pathNumber);
    }

    /**
     * Create an undirected edge between a variable node and factor node.
     * This version of connect is called if varNode is a Direction variable node.
     *
     * @param dir the State (PLUS|MINUS) that the direction variable must take
     * for it to be consistent with a specific path.
     * 
     * @return the RootGraph index of the edge
     */
    private int connect(int varNode, int facNode, State dir)
    {
        /*
        EdgeMessage em = new EdgeMessage((VariableNode) _nodeMap.get(varNode),
                                         (FactorNode) _nodeMap.get(facNode),
                                         dir);
        */

        EdgeMessage em;
        if(dir != null)
        {
            em = new EdgeMessage((VariableNode) _nodeMap.get(varNode),
                                 varNode, facNode, dir);
        }
        else
        {
            em = new EdgeMessage((VariableNode) _nodeMap.get(varNode),
                                 varNode, facNode);
        }
        
        _adjacencyMap.add(varNode, em);
        _adjacencyMap.add(facNode, em);
        
        return _g.createEdge(varNode, facNode, false);
    }
    
    /**
     * Create an undirected edge between a variable node and factor node
     * @return the RootGraph index of the edge
     */
    private int connect(int varNode, int facNode)
    {
        return connect(varNode, facNode, null);
    }

    /**
     * @return the newly created index of the node in this FactorGraph's
     * RootGraph
     */
    private AnnotatedEdge createAnnotatedEdge(int interactionEdgeIndex)
    {
        AnnotatedEdge ae = new AnnotatedEdge(interactionEdgeIndex);

        ae.fgIndex = createEdge(interactionEdgeIndex);
        ae.dirIndex = createDir(interactionEdgeIndex);
        ae.signIndex = createSign(interactionEdgeIndex);
        ae.label = _ig.edgeLabel(interactionEdgeIndex);
        
        _edgeMap.put(interactionEdgeIndex, ae);
        _x2aeMap.put(ae.fgIndex, ae);
        _d2aeMap.put(ae.dirIndex, ae);
        _s2aeMap.put(ae.signIndex, ae);

        return ae;
    }

    /**
     * Create an edge variable node in the factor graph and initialize
     * its a priori probability using pvalues (if the edge is protein-DNA)
     * or probabilities (if the edge is protein-protein).
     * 
     * @return the newly created index of the node in this FactorGraph's
     * RootGraph
     */
    protected int createEdge(int interactionEdgeIndex)
    {
        int node = _g.createNode();

        _vars.add(node);

        VariableNode vn = VariableNode.createEdge(interactionEdgeIndex);
        _nodeMap.put(node, vn);

        StateSet ss = vn.stateSet();
        double[] prob = new double[ss.size()];

        double val = _ig.getEdgeValue(interactionEdgeIndex);

        if(_ig.isProteinDNA(interactionEdgeIndex))
        {
            prob[ss.getIndex(State.ZERO)] = 1;
            prob[ss.getIndex(State.ONE)] = likelihoodRatio(val);
        }
        else
        {
            prob[ss.getIndex(State.ZERO)] = 1 - val;
            prob[ss.getIndex(State.ONE)] = val;
        }

        vn.setDefaultProbs(prob);

        return node;
    }


    /**
     * @return the newly created index of the node in this FactorGraph's
     * RootGraph
     */
    protected int createSign(int interactionEdgeIndex)
    {
        int node = _g.createNode();
        _sign.add(node);
        _vars.add(node);
        _nodeMap.put(node, VariableNode.createSign(interactionEdgeIndex));
        return node;
    }

    /**
     * If the edge is a protein-DNA edge, fix its direction.
     * 
     * @return the newly created index of the node in this FactorGraph's
     * RootGraph
     */
    protected int createDir(int interactionEdgeIndex)
    {
        int node = _g.createNode();

        _vars.add(node);
        
        VariableNode vn = VariableNode.createDirection(interactionEdgeIndex);

        if(_ig.isProteinDNA(interactionEdgeIndex))
        {
            vn.fixState(_ig.getFixedDir(interactionEdgeIndex));
        }
        
        _nodeMap.put(node, vn);
        return node;
    }

    /**
     * @return the newly created index of the node in this FactorGraph's
     * RootGraph
     */
    protected int createPathActive(int pathNumber)
    {
        int node = _g.createNode();

        _pathActive.add(node);
        _vars.add(node);
        _nodeMap.put(node, VariableNode.createPathActive(pathNumber));
        return node;
    }


    /**
     * Create a knockout variable node in the factor graph and initialize
     * its probabilities using its expression pvalue and logratio.
     * 
     * @return the newly created index of the node in this FactorGraph's
     * RootGraph
     */
    protected int createKO(int koNodeIndex, int targetNodeIndex)
    {
        int node = _g.createNode();

        _vars.add(node);
        
        VariableNode vn = VariableNode.createKO(koNodeIndex, targetNodeIndex);
        _nodeMap.put(node, vn);

        mRNAMeasurement m = _ig.getExpression(koNodeIndex, targetNodeIndex);
        if(m == null)
        {
            System.err.println("null mRNAMeasurement for " + koNodeIndex + ", " 
                               + targetNodeIndex);
            return node;
        }

        StateSet ss = vn.stateSet();
        double[] prob = new double[ss.size()];
        prob[ss.getIndex(State.ZERO)] = 1;

        if(m.getRatio() > 0)
        {
            prob[ss.getIndex(State.PLUS)] = likelihoodRatio(m.getSignificance());;
            prob[ss.getIndex(State.MINUS)] = koinv;
        }
        else
        {
            prob[ss.getIndex(State.PLUS)] = koinv;
            prob[ss.getIndex(State.MINUS)] = likelihoodRatio(m.getSignificance());
        }
        vn.setDefaultProbs(prob);

        return node;
    }

    private double likelihoodRatio(double pval)
    {
        return Math.exp(0.5* (ChiSquaredDistribution.inverseCDFMinus1(pval) - lnN));
    }
    
    /**
     * @return the newly created index of the node in this FactorGraph's
     * RootGraph
     */
    protected int createOR(int koNodeIndex, int targetNodeIndex)
    {
        int node = _g.createNode();

        _factors.add(node);
        _nodeMap.put(node, OrFactorNode.getInstance());
        return node;
    }

    /**
     * @return the newly created index of the node in this FactorGraph's
     * RootGraph
     */
    protected int createPathFactor(int pathNumber)
    {
        int node = _g.createNode();

        _factors.add(node);
        _nodeMap.put(node, CachedPathFactorNode.getInstance());
        _pathMap.put(pathNumber, node);

        return node;
    }

    /**
     * @return if nodeIndex refers to a variable node, return the max state
     * of the node if it has a unique max.  Return null of nodeIndex is a
     * factor node or if the variable does not have a unique max.
     */ 
    private State maxState(int nodeIndex)
    {
        Object o = _nodeMap.get(nodeIndex);

        if(o instanceof VariableNode)
        {
            ProbTable pt = ((VariableNode) o).getProbs();

            if(pt.hasUniqueMax())
            {
                return pt.maxState();
            }
        }
        
        return null;
    }

    /**
     * Update the InteractionGraph associated with this factor graph
     * with the edges that are active.  Active edges are determined by calling
     * runMaxProduct, or if runMaxProduct has not been called, active
     * edges will be determinied by their a priori probabilities.
     * <p>
     * An edge is active if its max state is ONE and it is in at least
     * one active path.
     */
    public void updateInteractionGraph()
    {
        int[] activePaths = getActivePaths();

        ObjectArrayList aes = _edgeMap.values();
        
        List activeEdges = new ArrayList();
        
        for(int n=0, N =aes.size(); n < N; n++)
        {
            AnnotatedEdge ae = (AnnotatedEdge) aes.get(n);

            if((ae.maxState == State.ONE)
               && _paths.isEdgeOnPath(ae.interactionIndex, activePaths)
                && ae.maxSign != null)
            {
                activeEdges.add(ae);
            }
        }
        
        _ig.setActiveEdges(activeEdges);

        System.out.println("Found " + activePaths.length + " active paths");
        System.out.println("Found " + activeEdges.size() + " active edges");
    }

    int[] getActivePaths()
    {
        IntArrayList l = new IntArrayList();
        
        for(int s=0, N=_pathActive.size(); s < N; s++)
        {
            VariableNode vn = (VariableNode) _nodeMap.get(_pathActive.get(s));
            ProbTable pt = vn.getProbs();

            if(pt.hasUniqueMax() && (pt.maxState() == State.ONE))
            {
                l.add(vn.getId());
            }
        }
        l.trimToSize();
        
        return l.elements();
    }


    /**
     * Run the max product algorithm.
     *
     * FIX: termination condition. decompose degenerate networks.
     */
    public void runMaxProduct()  throws AlgorithmException
    {
        _runMaxProduct();
        updateEdgeAnnotation();
    }
    
    private void _runMaxProduct()  throws AlgorithmException
    {
        _vars.trimToSize();
        _factors.trimToSize();
        
        int[] v = _vars.elements();
        int[] f = _factors.elements();

        initVar2Factor(v);
        
        int N = 2  * _paths.getMaxPathLength();

        System.out.println("Running max product " + N + " iterations");
        
        for(int x=0; x < N; x++)
        {
            computeFactor2Var(f);
            computeVar2Factor(v);
        }
    }

    private void partitionFactorGraph()
    {
        
        /*
        // divide the factor graph into connected components
        DFS dfs = new DFS(_g);
        GraphForrest components = dfs.traverse();

        System.out.println("Found " + components.size() + " trees by DFS");

        // separate the sign variables from non-sign vars in each
        // connected components.
        GraphForrest signs = new GraphForrest();
        GraphForrest others = new GraphForrest();
                
        for(Iterator it = components.iterator(); it.hasNext();)
        {
            IntArrayList nodes = (IntArrayList) it.next();
            System.out.println("tree has " + nodes.size() + " nodes.");

            IntArrayList s= signs.newTree();
            IntArrayList o= others.newTree();

            for(int x=0; x < nodes.size(); x++)
            {
                int v = nodes.get(x);
                FGNode fgn =  (FGNode) _nodeMap.get(v);   
                if(!fgn.isType(NodeType.FACTOR))
                {
                    if(fgn.isType(NodeType.SIGN))
                    {
                        // inefficient because fixDegenerateVar must
                        // translate the int "v" back into a Node object
                        s.add(v);
                    }
                    else
                    {
                        
                        o.add(v);
                    }
                }
            }

            System.out.println("   " + s.size() + " signs");
            System.out.println("   " + o.size() + " other");
        }
        
        // sort the sign nodes in each component by degree
        List sortedSigns = new ArrayList();
        for(Iterator it = signs.iterator(); it.hasNext();)
        {
            IntArrayList l = sortNodesByDegree((IntArrayList) it.next());
            sortedSigns.add(l);
        }
        */

    }
    
    /**
     * Run the max product algorithm.
     *
     * FIX: termination condition. decompose degenerate networks.
     */
    public void runMaxProductAndDecompose() throws AlgorithmException
    {
        _submodels.clear();

        _runMaxProduct();
        Submodel invariant = fixUniqueVars();
        invariant.setInvariant(true);
        _submodels.add(invariant);

        System.out.println("added invariant submodel with "
                           + invariant.size() + " vars");
        
        // annotate invariant edges in the interaction graph
        updateEdgeAnnotation();
        
        //partitionFactorGraph();
        
        IntArrayList allSortedSign = sortNodesByDegree(_sign);

        //System.out.println(printAdj());
        
        // first fix degenerate sign variables
        int x=0;
        x += recursivelyFixVars(_sign);
        
        // now fix any other non-sign variables that are degenerate
        x += recursivelyFixVars(_vars);
        
        System.out.println("Called max product method " + x + " times");

        System.out.println("Generated " + _submodels.size()
                           + " submodels");
        
        // update the edge annotations now that all variables are fixed.
        updateEdgeAnnotation();

        /*
        System.out.println("### Dependency Graph");
        System.out.println(CyUtil.toString(_dependencyGraph.getRootGraph(),
                                           _dependencyGraph.getDep2InteractionMap())
                           );
        */
        // break dependecy graph into connected components


        // send connected components to interaction graph
    }


    private int recursivelyFixVars(IntArrayList varNodes)
        throws AlgorithmException
    {
        int x=0;
        while(!allFixed(varNodes))
        {
            x++;

            // at each iteration, fix one sign variable in each connected
            // component of the factor graph
            System.out.println("### fix var loop: " + x);
           
            /*for(int y=0; y < sortedSigns.size(); y++)
            {
                int var = chooseDegenerateVar((IntArrayList) sortedSigns.get(y));

                if(var < 0)
                {
                    fixVar(var);
                }
            }
            */

            int var = chooseDegenerateVar(varNodes);
            
            if(var < 0)
            {
                fixVar(var);
            }
            
            _runMaxProduct();
            Submodel model = fixUniqueVars(var);
            _submodels.add(model);
            System.out.println("added submodel with "
                               + model.size() + " vars");
        }
        
        return x;
    }
    
    /**
     * @return the list of nodes sorted by degree
     */
    private IntArrayList sortNodesByDegree(IntArrayList nodes)
    {
        OpenIntIntHashMap map = new OpenIntIntHashMap(nodes.size());
        
        for(int x=0; x < nodes.size(); x++)
        {
            int v = nodes.get(x);

            List messages = _adjacencyMap.get(v);

            map.put(v, messages.size());
        }

        IntArrayList sortedNodes = new IntArrayList(nodes.size());
        map.keysSortedByValue(sortedNodes);
        return sortedNodes;
    }

    /**
     *
     *
     * @param nodes a list of root graph node indicies
     * @return true if all of the nodes in "nodes" are fixed.  
     */
    private boolean allFixed(IntArrayList nodes)
    {
        for(int x=0; x < nodes.size(); x++)
        {
            int v = nodes.get(x);

            if(!isVarFixed(v))
            {
                return false;
            }
        }

        return true;
    }
    
    /**
     * Fix all variables that have a unique max
     * @return a Submodel containing the variables that were fixed
     */
    private Submodel fixUniqueVars()
    {
        Submodel model = new Submodel();
        
        for(int x=0; x < _vars.size(); x++)
        {
            int v = _vars.get(x);
            VariableNode vn = (VariableNode) _nodeMap.get(v);

            ProbTable pt = vn.getProbs();

            if(!vn.isFixed())
            {
                if(pt.hasUniqueMax())
                {
                    model.addVar(v);
                    vn.fixState(pt.maxState());
                }
            }
        }

        System.out.println("fixed " + model.size()
                           + " variables of " + _vars.size());

        return model;
    }

    /**
     * 1. Fix all variables that have a unique max.
     *
     * 2. Establish dependency relationship from fixedNode to
     * the newly fixed nodes.
     */
    private Submodel fixUniqueVars(int fixedNode)
    {
        Submodel model = new Submodel();
        model.setIndependentVar(fixedNode);
        int ct = 0;
        
        for(int x=0; x < _vars.size(); x++)
        {
            int v = _vars.get(x);
            VariableNode vn = (VariableNode) _nodeMap.get(v);

            ProbTable pt = vn.getProbs();

            if(!vn.isFixed())
            {
                if(pt.hasUniqueMax())
                {
                    // v is a dependent node
                    model.addVar(v);

                    // add variables that influence the
                    // potential functions that determine v.
                    addInferredVars(model, fixedNode, v,
                                    pt.maxState());

                    vn.fixState(pt.maxState());
                    ct++;
                }
            }
        }

        System.out.println("fixed " + ct + " variables of " + _vars.size());

        return model;
    }

    /**
     * Record a dependency established by fixing "fixedNode"
     * 1. find factor nodes that contain the fixedNode and the dependent node
     * 2. keep factor node only if all of the vars connected to it are
     *    either invariant, previously manually fixed, or newly determined.
     * 3. update the dependency graph.  
     *    The dependent node depends on each variable connected to the
     *    factor node.
     *
     * @param fixedNode the degerate variable that was manually fixed
     * @param node a variable that was uniquely determined by the last
     *             run of the max product algorith,
     */
    private void addInferredVars(Submodel model,
                                 int fixedNode, int node, State max)
    {
        System.out.println("addInferredVars: " + fixedNode
                           + ", " + node
                           + " max=" + max);

        // identify variables that are connected to the factor nodes
        // that are connected to this variable
        // criteria:
        // 1. all child vars of the factor node must be fixed

        List messages = _adjacencyMap.get(node);
        for(int m=0, M=messages.size(); m < M; m++)
        {
            EdgeMessage em = (EdgeMessage) messages.get(m);

            int fac = em.getFactorIndex();

            List fmsg = _adjacencyMap.get(fac);
            for(int x=0, N=fmsg.size(); x < N; x++)
            {

                int y = ((EdgeMessage) fmsg.get(x)).getVariableIndex();
                
                if(y != node && isVarFixed(y))
                {
                    // FIX THIS
                    System.out.println("adding var: " + y
                                       + " to model "
                                       + model.getIndependentVar());
                    model.addVar(y);
                    //                    updateDepGraph(y, node);
                }
            }

        }

    }

    private boolean isInvariant(int node)
    {
        FGNode fgn = (FGNode) _nodeMap.get(node);

        if(fgn.isType(NodeType.EDGE))
        {
            return ((AnnotatedEdge) _x2aeMap.get(node)).invariant;
        }
        else if(fgn.isType(NodeType.SIGN))
        {
            return ((AnnotatedEdge)_s2aeMap.get(node)).invariant;
        }
        else if(fgn.isType(NodeType.DIR))
        {
            return ((AnnotatedEdge)_d2aeMap.get(node)).invariant;
        }

        return false;
    }
    
    /**
     * Update dependency graph with relationship: n2 depends on n1.
     *
     * @param n2 the dependent node
     * @param n1 the node that n2 is dependent on
     *
    private void updateDepGraph(int n1, int n2)
    {
        int edge1 = nodeIndex2InteractionIndex(n1);
        int edge2 = nodeIndex2InteractionIndex(n2);

        if(edge1 != 0 && edge2 != 0
           && edge1 != edge2
           && !isInvariant(n1))
        {
            //            System.out.println("isInvariant: " + edge1 + " " + isInvariant(n1));
            _dependencyGraph.add(edge1, edge2);
        }
    }
    */

    /**
     *
     *
     * @param node index of a node in the factor graph
     * @return if node is an edge presence, sign or direction variable, then
     * return the index of the edge in the interaction graph that corresponds
     * to the variable. Return 0 otherwise.
     */
    private int nodeIndex2InteractionIndex(int node)
    {
        FGNode fgn = (FGNode) _nodeMap.get(node);

        if(fgn.isType(NodeType.EDGE))
        {
            return ((AnnotatedEdge) _x2aeMap.get(node)).interactionIndex;
        }
        else if(fgn.isType(NodeType.SIGN))
        {
            return ((AnnotatedEdge)_s2aeMap.get(node)).interactionIndex;
        }
        else if(fgn.isType(NodeType.DIR))
        {
            return ((AnnotatedEdge)_d2aeMap.get(node)).interactionIndex;
        }

        return 0;
    }
    
    /**
     * @param sortedNodes nodes sorted by degree in ascending order
     * 
     * @return the root graph index of a degenerate variable, or zero if
     * all variables are fixed.
     */
    private int chooseDegenerateVar(IntArrayList sortedNodes)
    {
        for(int x=sortedNodes.size() - 1; x >= 0; x--)
        {
            int v = sortedNodes.get(x);

            if(!isVarFixed(v))
            {
                return v;
            }
        }

        return 0;
    }

    private boolean isVarFixed(int varIndex)
    {
        VariableNode vn = (VariableNode) _nodeMap.get(varIndex);

        return vn.isFixed();
    }
    
    /**
     * Fix "var" to a specific state.  Choose the state randomly
     *
     * @param var the root graph index of the variable to fix
     */
    private void fixVar(int var)
    {
        System.out.println("fixvar called on " + var);

        if(_nodeMap.containsKey(var))
        {
            VariableNode vn = (VariableNode) _nodeMap.get(var);

            if(vn.isFixed())
            {
                return;
            }
            
            ProbTable pt = vn.getProbs();
            if(pt.hasUniqueMax())
            {
                vn.fixState(pt.maxState());
            }
            else
            {
                StateSet ss = vn.stateSet();

                if(ss == StateSet.PATH_ACTIVE)
                {
                    vn.fixState(State.ZERO);
                    System.out.println("fixing path active for path "
                                       + vn.getId()
                                       + " to " + State.ZERO);
                                        
                }
                else if (ss == StateSet.EDGE)
                {
                    vn.fixState(State.ONE);

                    System.out.println("fixing edge value for "
                                       + _ig.edgeName(vn.getId())
                                       + " to " + State.ONE);
                
                }
                else if (ss == StateSet.SIGN)
                {
                    vn.fixState(State.MINUS);
                    
                    System.out.println("fixing sign for "
                                       + _ig.edgeName(vn.getId())
                                       + " to " + State.MINUS);
                                    
                }
                else if (ss == StateSet.DIR)
                {
                    vn.fixState(State.PLUS);

                    System.out.println("fixing dir for "
                                       + _ig.edgeName(vn.getId())
                                       + " to " + State.PLUS);
                        
                }
                else if(ss == StateSet.KO)
                {
                    System.out.println("fixing ko for "
                                       + _ig.node2Name(vn.getId())
                                       + " - "
                                       + _ig.node2Name(vn.getId2()));
                    
                    // P(+1) == P(-1) -> choose +1
                    if(Math.abs(pt.prob(State.PLUS) - pt.prob(State.MINUS)) > 1e-20)
                    {
                        vn.fixState(State.PLUS);
                    }
                    // P(0) == P(-1) -> choose -1
                    else if (Math.abs(pt.prob(State.ZERO) - pt.prob(State.MINUS)) > 1e-20)
                    {
                        vn.fixState(State.MINUS);
                    }
                    // last case: P(0) == P(+1) -> choose +1
                    else
                    {
                        vn.fixState(State.PLUS);
                    }
                }
                else
                {
                    // pick a state at random
                    Iterator it = ss.iterator();
                    if(it.hasNext())
                    {
                        vn.fixState((State) it.next());
                    }
                }
                
            }
        }
    }

    /**
     * Set the max state, direction, and sign for each edge
     *
     */
    protected void updateEdgeAnnotation()
    {
        ObjectArrayList aes = _edgeMap.values();
        
        for(int x=0; x < aes.size(); x++)
        {
            AnnotatedEdge ae = (AnnotatedEdge) aes.get(x);

            ae.maxState = maxState(ae.fgIndex);
            ae.maxDir = maxState(ae.dirIndex);
            ae.maxSign = maxState(ae.signIndex);

            if(ae.maxState != null &&
               ae.maxDir != null &&
               ae.maxSign != null)
            {
                //                System.out.println("setting invariant to true for: " + ae.interactionIndex);
                ae.invariant = true;
            }
        }
    }

    /**
     * Compute variable to factor messages
     *
     * @param nodes the root graph indicies of variable nodes
     * @return false
     */
    protected boolean computeVar2Factor(int[] nodes)
    {
        boolean noChange = false;
        
        for(int x=0, N=nodes.length; x < N; x++)
        {
            int n = nodes[x];
            VariableNode vn = (VariableNode) _nodeMap.get(n);

            List messages = _adjacencyMap.get(n);
            vn.maxProduct(messages, false);

            /* no need since EdgeMessages have a ref to ProbTables?
             * is this unsafe?
            ProbTable pt = vn.getProbs();

            for(int m=0, M=messages.size(); m < M; m++)
            {
                EdgeMessage em = (EdgeMessage) messages.get(m);
                em.v2f(pt);
            }
            */
        }

        return noChange;
    }

    /**
     * Compute factor to variable messages
     *
     * @param nodes the root graph indicies of factor nodes
     * @throws AlgorithmException
     */
    protected void computeFactor2Var(int[] nodes) throws AlgorithmException
    {
        for(int x=0, N=nodes.length; x < N; x++)
        {
            int n = nodes[x];
            FactorNode fn = (FactorNode) _nodeMap.get(n);

            List messages = _adjacencyMap.get(n);
            for(int m=0, M=messages.size(); m < M; m++)
            {
                EdgeMessage em = (EdgeMessage) messages.get(m);

                em.f2v(fn.maxProduct(messages, m, em.getVariable()));
            }
        }
    }

    /**
     * Initialize messages from variable to factor nodes
     *
     * @param nodes root graph indices of variable nodes
     */
    protected void initVar2Factor(int[] nodes)
    {
        for(int x=0, N=nodes.length; x < N; x++)
        {
            int n = nodes[x];
            VariableNode vn = (VariableNode) _nodeMap.get(n);
            ProbTable pt = vn.getProbs();
            
            List messages = _adjacencyMap.get(n);
            for(int m=0, M=messages.size(); m < M; m++)
            {
                EdgeMessage em = (EdgeMessage) messages.get(m);
                em.v2f(pt);
            }
        }
    }

    /**
     * For debugging, print the adjacency list map containing
     * the most recent set of messages passed in the factor graph.
     */
    public String printAdj()
    {
        StringBuffer b = new StringBuffer();
        IntArrayList keys = _adjacencyMap.keys();
        for(int x=0; x < keys.size(); x++)
        {
            int k = keys.get(x);
            b.append(k);
            b.append("  {\n");

            List l = _adjacencyMap.get(k);
            for(int m=0; m < l.size(); m++)
            {
                EdgeMessage em = (EdgeMessage) l.get(m);
                b.append("    v2f ");
                b.append(em.v2f());
                b.append("\n");
                b.append("    f2v ");
                b.append(em.f2v());
                b.append("\n");
            }
            b.append("  }\n");
        }

        return b.toString();
    }

    public String toString()
    {
        return CyUtil.toString(_g);
    }

}
