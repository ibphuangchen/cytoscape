package fing.model.test;

import fing.model.FingRootGraphFactory;
import giny.model.Edge;
import giny.model.Node;
import giny.model.RootGraph;

import java.util.Iterator;

public final class AllRootGraphMethodsTest
{

  // No constructor.
  private AllRootGraphMethodsTest() { }

  public static final void main(String[] args)
    throws ClassNotFoundException, InstantiationException,
           IllegalAccessException
  {
    final RootGraph root = getRootGraph(args);
    addNodesAndEdges(root);
    Iterator nodesIter = root.nodesIterator();
    Iterator edgesIter = root.edgesIterator();
    Node[] twoNodes = new Node[] { (Node) nodesIter.next(),
                                   (Node) nodesIter.next() };
    Edge[] twoEdges = new Edge[] { (Edge) edgesIter.next(),
                                   (Edge) edgesIter.next() };
    if (root.createGraphPerspective(twoNodes, null).getNodeCount() != 2)
      throw new IllegalStateException
        ("GraphPerspective does not have two nodes");
    if (root.createGraphPerspective(null, twoEdges).getEdgeCount() != 2)
      throw new IllegalStateException
        ("GraphPerspective does not have two edges");
    if (root.createGraphPerspective(twoNodes, twoEdges).getNodeCount() < 2)
      throw new IllegalStateException
        ("GraphPerspective has less than two nodes");
    if (root.createGraphPerspective(twoNodes, twoEdges).getEdgeCount() < 2)
      throw new IllegalStateException
        ("GraphPerspective has less than two edges");
  }

  private static final RootGraph getRootGraph(String[] mainArgs)
    throws ClassNotFoundException, InstantiationException,
           IllegalAccessException {
    if (mainArgs.length > 0 && mainArgs[0].equalsIgnoreCase("luna"))
      return (RootGraph) Class.forName("luna.LunaRootGraph").newInstance();
    else return FingRootGraphFactory.instantiateRootGraph(); }

  private static final void addNodesAndEdges(RootGraph root) {
    int[] nodeInx = new int[3];
    for (int i = 0; i < nodeInx.length; i++) nodeInx[i] = root.createNode();
    root.createEdge(nodeInx[0], nodeInx[1], true);
    root.createEdge(nodeInx[1], nodeInx[2], false);
    root.createEdge(nodeInx[2], nodeInx[0], true);
    root.createEdge(nodeInx[0], nodeInx[0], true);
    root.createEdge(nodeInx[1], nodeInx[1], false); }

}
