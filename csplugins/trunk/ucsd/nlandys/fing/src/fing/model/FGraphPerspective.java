package fing.model;

import cytoscape.graph.dynamic.DynamicGraph;
import cytoscape.graph.dynamic.util.DynamicGraphFactory;
import cytoscape.util.intr.IntArray;
import cytoscape.util.intr.IntEnumerator;
import cytoscape.util.intr.IntIterator;
import cytoscape.util.intr.IntIntHash;
import cytoscape.util.intr.MinIntHeap;

import giny.filter.Filter;
import giny.model.Edge;
import giny.model.GraphPerspective;
import giny.model.GraphPerspectiveChangeListener;
import giny.model.Node;
import giny.model.RootGraph;
import giny.model.RootGraphChangeEvent;
import giny.model.RootGraphChangeListener;

import java.util.Iterator;
import java.util.NoSuchElementException;

// Package visible class.
class FGraphPerspective implements GraphPerspective
{

  public void addGraphPerspectiveChangeListener
    (GraphPerspectiveChangeListener listener)
  { // This method is not thread safe; synchronize on an object to make it so.
    m_lis[0] = GraphPerspectiveChangeListenerChain.add(m_lis[0], listener);
  }

  public void removeGraphPerspectiveChangeListener
    (GraphPerspectiveChangeListener listener)
  { // This method is not thread safe; synchronize on an object to make it so.
    m_lis[0] = GraphPerspectiveChangeListenerChain.remove(m_lis[0], listener);
  }

  // The object returned shares the same RootGraph with this object.
  public Object clone()
  {
    final IntEnumerator nativeNodes = m_graph.nodes();
    final IntEnumerator rootGraphNodeInx = new IntEnumerator() {
        public int numRemaining() { return nativeNodes.numRemaining(); }
        public int nextInt() {
          return m_nativeToRootNodeInxMap.getIntAtIndex
            (nativeNodes.nextInt()); } };
    final IntEnumerator nativeEdges = m_graph.edges();
    final IntEnumerator rootGraphEdgeInx = new IntEnumerator() {
        public int numRemaining() { return nativeEdges.numRemaining(); }
        public int nextInt() {
          return m_nativeToRootEdgeInxMap.getIntAtIndex
            (nativeEdges.nextInt()); } };
    return new FGraphPerspective(m_root, rootGraphNodeInx, rootGraphEdgeInx);
  }

  public RootGraph getRootGraph()
  {
    return m_root;
  }

  public int getNodeCount()
  {
    return m_graph.nodes().numRemaining();
  }

  public int getEdgeCount()
  {
    return m_graph.edges().numRemaining();
  }

  public Iterator nodesIterator()
  {
    final IntEnumerator nodes = m_graph.nodes();
    return new Iterator() {
        public void remove() {
          throw new UnsupportedOperationException(); }
        public boolean hasNext() {
          return nodes.numRemaining() > 0; }
        public Object next() {
          if (!hasNext()) throw new NoSuchElementException();
          return m_root.getNode
            (m_nativeToRootNodeInxMap.getIntAtIndex(nodes.nextInt())); } };
  }

  // This method has been marked deprecated in the Giny API.
  public java.util.List nodesList() {
    final int nodeCount = getNodeCount();
    final java.util.ArrayList returnThis = new java.util.ArrayList(nodeCount);
    Iterator iter = nodesIterator();
    for (int i = 0; i < nodeCount; i++) returnThis.add(iter.next());
    return returnThis; }

  // This method has been marked deprecated in the Giny API.
  public int[] getNodeIndicesArray()
  {
    IntEnumerator nodes = m_graph.nodes();
    // According to the API spec, we must return an array with 0 at index 0.
    final int[] returnThis = new int[nodes.numRemaining() + 1];
    for (int i = 1; i < returnThis.length; i++)
      returnThis[i] = m_nativeToRootNodeInxMap.getIntAtIndex(nodes.nextInt());
    return returnThis;
  }

  public Iterator edgesIterator()
  {
    final IntEnumerator edges = m_graph.edges();
    return new Iterator() {
        public void remove() {
          throw new UnsupportedOperationException(); }
        public boolean hasNext() {
          return edges.numRemaining() > 0; }
        public Object next() {
          if (!hasNext()) throw new NoSuchElementException();
          return m_root.getEdge
            (m_nativeToRootEdgeInxMap.getIntAtIndex(edges.nextInt())); } };
  }

  // This method has been marked deprecated in the Giny API.
  public java.util.List edgesList() {
    final int edgeCount = getEdgeCount();
    final java.util.ArrayList returnThis = new java.util.ArrayList(edgeCount);
    Iterator iter = edgesIterator();
    for (int i = 0; i < edgeCount; i++) returnThis.add(iter.next());
    return returnThis; }

  // This method has been marked deprecated in the Giny API.
  public int[] getEdgeIndicesArray()
  {
    IntEnumerator edges = m_graph.edges();
    // According to the API spec, we must return an array with 0 at index 0.
    final int[] returnThis = new int[edges.numRemaining() + 1];
    for (int i = 1; i < returnThis.length; i++)
      returnThis[i] = m_nativeToRootEdgeInxMap.getIntAtIndex(edges.nextInt());
    return returnThis;
  }

  // This method has been marked deprecated in the Giny API.
  public int[] getEdgeIndicesArray(int rootGraphFromNodeInx,
                                   int rootGraphToNodeInx,
                                   boolean undirectedEdges,
                                   boolean bothDirections)
  {
    if (!(rootGraphFromNodeInx < 0 && rootGraphToNodeInx < 0)) return null;
    final int nativeFromNodeInx =
      m_rootToNativeNodeInxMap.get(~rootGraphFromNodeInx);
    final int nativeToNodeInx =
      m_rootToNativeNodeInxMap.get(~rootGraphToNodeInx);
    final IntIterator connectingEdges;
    try {
      connectingEdges = m_graph.connectingEdges
        (nativeFromNodeInx, nativeToNodeInx, true, bothDirections,
         undirectedEdges); }
    catch (IllegalArgumentException e) { return null; }
    if (connectingEdges == null) return null;
    m_heap.empty();
    final MinIntHeap edgeBucket = m_heap;
    while (connectingEdges.hasNext())
      edgeBucket.toss(m_nativeToRootNodeInxMap.getIntAtIndex
                      (connectingEdges.nextInt()));
    final int[] returnThis = new int[edgeBucket.size()];
    edgeBucket.copyInto(returnThis, 0);
    return returnThis;
  }

  public Node hideNode(Node node)
  {
    if (node.getRootGraph() == m_root &&
        hideNode(node.getRootGraphIndex()) != 0) return node;
    else return null;
  }

  public int hideNode(int rootGraphNodeInx)
  {
    return m_weeder.hideNode(this, rootGraphNodeInx);
  }

  public java.util.List hideNodes(java.util.List nodes)
  {
    final java.util.ArrayList returnThis = new java.util.ArrayList();
    for (int i = 0; i < nodes.size(); i++)
      if (hideNode((Node) nodes.get(i)) != null)
        returnThis.add(nodes.get(i));
    return returnThis;
  }

  public int[] hideNodes(int[] rootGraphNodeInx)
  {
    return m_weeder.hideNodes(this, rootGraphNodeInx);
  }

  public Node restoreNode(Node node)
  {
    if (node.getRootGraph() == m_root &&
        restoreNode(node.getRootGraphIndex()) != 0) return node;
    else return null;
  }

  public int restoreNode(int rootGraphNodeInx)
  {
    final int returnThis = _restoreNode(rootGraphNodeInx);
    if (returnThis != 0) {
      final GraphPerspectiveChangeListener listener = m_lis[0];
      if (listener != null) {
        listener.graphPerspectiveChanged
          (new GraphPerspectiveNodesRestoredEvent
           (this, new int[] { rootGraphNodeInx })); } }
    return returnThis;
  }

  private int _restoreNode(int rootGraphNodeInx)
  {
    if (!(rootGraphNodeInx < 0)) return 0;
    int nativeNodeInx = m_rootToNativeNodeInxMap.get(~rootGraphNodeInx);
    if (!(nativeNodeInx < 0 || nativeNodeInx == Integer.MAX_VALUE)) return 0;
    nativeNodeInx = m_graph.createNode();
    m_rootToNativeNodeInxMap.put(~rootGraphNodeInx, nativeNodeInx);
    m_nativeToRootNodeInxMap.setIntAtIndex(rootGraphNodeInx, nativeNodeInx);
    return rootGraphNodeInx;
  }

  public java.util.List restoreNodes(java.util.List nodes)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public java.util.List restoreNodes(java.util.List nodes,
                                     boolean restoreIncidentEdges)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public int[] restoreNodes(int[] perspNodeInx,
                            boolean restoreIncidentEdges)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public int[] restoreNodes(int[] rootGraphNodeInx)
  {
    m_heap.empty();
    final MinIntHeap successes = m_heap;
    final int[] returnThis = new int[rootGraphNodeInx.length];
    for (int i = 0; i < rootGraphNodeInx.length; i++) {
      returnThis[i] = _restoreNode(rootGraphNodeInx[i]);
      if (returnThis[i] != 0) successes.toss(i); }
    if (successes.size() > 0) {
      final GraphPerspectiveChangeListener listener = m_lis[0];
      if (listener != null) {
        final int[] successArr = new int[successes.size()];
        final IntEnumerator enum = successes.elements();
        int index = -1;
        while (enum.numRemaining() > 0)
          successArr[++index] = rootGraphNodeInx[enum.nextInt()];
        listener.graphPerspectiveChanged
          (new GraphPerspectiveNodesRestoredEvent(this, successArr)); } }
    return returnThis;
  }

  public Edge hideEdge(Edge edge)
  {
    if (edge.getRootGraph() == m_root &&
        hideEdge(edge.getRootGraphIndex()) != 0) return edge;
    else return null;
  }

  public int hideEdge(int rootGraphEdgeInx)
  {
    return m_weeder.hideEdge(this, rootGraphEdgeInx);
  }

  // This methods has been marked deprecated in the Giny API.
  public java.util.List hideEdges(java.util.List edges)
  {
    final java.util.ArrayList returnThis = new java.util.ArrayList();
    for (int i = 0; i < edges.size(); i++)
      if (hideEdge((Edge) edges.get(i)) != null)
        returnThis.add(edges.get(i));
    return returnThis;
  }

  public int[] hideEdges(int[] rootGraphEdgeInx)
  {
    return m_weeder.hideEdges(this, rootGraphEdgeInx);
  }

  public Edge restoreEdge(Edge edge)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public int restoreEdge(int rootGraphEdgeInx)
  {
    final int returnThis = _restoreEdge(rootGraphEdgeInx);
    if (returnThis != 0) {
      final GraphPerspectiveChangeListener listener = m_lis[0];
      if (listener != null) {
        listener.graphPerspectiveChanged
          (new GraphPerspectiveEdgesRestoredEvent
           (this, new int[] { rootGraphEdgeInx })); } }
    return returnThis;
  }

  private int _restoreEdge(int rootGraphEdgeInx)
  {
    return -1;
  }

  public java.util.List restoreEdges(java.util.List edges)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public int[] restoreEdges(int[] perspEdgeInx)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public boolean containsNode(Node node)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public boolean containsNode(Node node, boolean recurse)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public boolean containsEdge(Edge edge)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public boolean containsEdge(Edge edge, boolean recurse)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public GraphPerspective join(GraphPerspective persp)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public GraphPerspective createGraphPerspective(Node[] nodes, Edge[] edges)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public GraphPerspective createGraphPerspective(int[] perspNodeInx,
                                                 int[] perspEdgeInx)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public GraphPerspective createGraphPerspective(Filter filter)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public java.util.List neighborsList(Node node)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public int[] neighborsArray(int perspNodeInx)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public boolean isNeighbor(Node aNodel, Node anotherNode)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public boolean isNeighbor(int perspNodeInx, int perspAnotherNodeInx)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public boolean edgeExists(Node from, Node to)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public boolean edgeExists(int perspFromNodeInx, int perspToNodeInx)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public int getEdgeCount(Node from, Node to, boolean countUndirectedEdges)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public int getEdgeCount(int perspFromNodeInx, int perspToNodeInx,
                          boolean countUndirectedEdges)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public java.util.List edgesList(Node from, Node to)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public java.util.List edgesList(int perspFromNodeInx,
                                  int perspToNodeInx,
                                  boolean includeUndirectedEdges)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public int[] getEdgeIndicesArray(int perspFromNodeInx,
                                   int perspToNodeInx,
                                   boolean includeUndirectedEdges)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public int getInDegree(Node node)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public int getInDegree(int perspNodeInx)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public int getInDegree(Node node, boolean countUndirectedEdges)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public int getInDegree(int perspNodeInx, boolean countUndirectedEdges)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public int getOutDegree(Node node)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public int getOutDegree(int perspNodeInx)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public int getOutDegree(Node node, boolean countUndirectedEdges)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public int getOutDegree(int perspNodeInx, boolean countUndirectedEdges)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public int getDegree(Node node)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public int getDegree(int perspNodeInx)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public int getIndex(Node node)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public int getNodeIndex(int rootGraphNodeInx)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public int getRootGraphNodeIndex(int perspNodeInx)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public Node getNode(int perspNodeInx)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public int getIndex(Edge edge)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public int getEdgeIndex(int rootGraphEdgeInx)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public int getRootGraphEdgeIndex(int perspEdgeInx)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public Edge getEdge(int perspEdgeInx)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public int getEdgeSourceIndex(int perspEdgeInx)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public int getEdgeTargetIndex(int perspEdgeInx)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public boolean isEdgeDirected(int perspEdgeInx)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public boolean isMetaParent(Node child, Node parent)
  {
    throw new UnsupportedOperationException("meta nodes not yet supported");
  }

  public boolean isNodeMetaParent(int perspChildNodeInx,
                                  int perspParentNodeInx)
  {
    throw new UnsupportedOperationException("meta nodes not yet supported");
  }

  public java.util.List metaParentsList(Node node)
  {
    throw new UnsupportedOperationException("meta nodes not yet supported");
  }

  public java.util.List nodeMetaParentsList(int perspNodeInx)
  {
    throw new UnsupportedOperationException("meta nodes not yet supported");
  }

  public int[] getNodeMetaParentIndicesArray(int perspNodeInx)
  {
    throw new UnsupportedOperationException("meta nodes not yet supported");
  }

  public boolean isMetaChild(Node parent, Node child)
  {
    throw new UnsupportedOperationException("meta nodes not yet supported");
  }

  public boolean isNodeMetaChild(int perspNodeInx, int perspChildInx)
  {
    throw new UnsupportedOperationException("meta nodes not yet supported");
  }

  public java.util.List nodeMetaChildrenList(Node node)
  {
    throw new UnsupportedOperationException("meta nodes not yet supported");
  }

  public java.util.List nodeMetaChildrenList(int perspParentInx)
  {
    throw new UnsupportedOperationException("meta nodes not yet supported");
  }

  public int[] getNodeMetaChildIndicesArray(int perspNodeInx)
  {
    throw new UnsupportedOperationException("meta nodes not yet supported");
  }

  public boolean isMetaParent(Edge child, Node parent)
  {
    throw new UnsupportedOperationException("meta nodes not yet supported");
  }

  public boolean isEdgeMetaParent(int perspChildEdgeInx,
                                  int perspParentNodeInx)
  {
    throw new UnsupportedOperationException("meta nodes not yet supported");
  }

  public java.util.List metaParentsList(Edge edge)
  {
    throw new UnsupportedOperationException("meta nodes not yet supported");
  }

  public java.util.List edgeMetaParentsList(int perspEdgeInx)
  {
    throw new UnsupportedOperationException("meta nodes not yet supported");
  }

  public int[] getEdgeMetaParentIndicesArray(int perspEdgeInx)
  {
    throw new UnsupportedOperationException("meta nodes not yet supported");
  }

  public boolean isMetaChild(Node parent, Edge child)
  {
    throw new UnsupportedOperationException("meta nodes not yet supported");
  }

  public boolean isEdgeMetaChild(int perspParentNodeInx,
                                 int perspChildEdgeInx)
  {
    throw new UnsupportedOperationException("meta nodes not yet supported");
  }

  public java.util.List edgeMetaChildrenList(Node node)
  {
    throw new UnsupportedOperationException("meta nodes not yet supported");
  }

  public java.util.List edgeMetaChildrenList(int perspParentNodeInx)
  {
    throw new UnsupportedOperationException("meta nodes not yet supported");
  }

  public int[] getEdgeMetaChildIndicesArray(int perspParentNodeInx)
  {
    throw new UnsupportedOperationException("meta nodes not yet supported");
  }

  public java.util.List getAdjacentEdgesList(Node node,
                                             boolean undirected,
                                             boolean incoming,
                                             boolean outgoing)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public int[] getAdjacentEdgeIndicesArray(int perspNodeInx,
                                           boolean undirected,
                                           boolean incoming,
                                           boolean outgoing)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public java.util.List getConnectingEdges(java.util.List nodes)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public int[] getConnectingEdgeIndicesArray(int[] perspNodeInx)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public int[] getConnectingNodeIndicesArray(int[] perspEdgeInx)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public GraphPerspective createGraphPerspective(int[] perspNodeInx)
  {
    throw new IllegalStateException("not implemented yet");
  }

  public void finalize()
  {
    m_root.removeRootGraphChangeListener(m_changeSniffer);
  }

  // Nodes and edges in this graph are called "native indices" throughout
  // this class.
  private final DynamicGraph m_graph;

  private final FRootGraph m_root;

  // This is an array of length 1 - we need an array as an extra reference
  // to a reference because some other inner classes need to know what the
  // current listener is.
  private final GraphPerspectiveChangeListener[] m_lis;

  // RootGraph indices are negative in these arrays.
  private final IntArray m_nativeToRootNodeInxMap;
  private final IntArray m_nativeToRootEdgeInxMap;

  // RootGraph indices are ~ (complements) of the real RootGraph indices
  // in these hashtables.
  private final IntIntHash m_rootToNativeNodeInxMap;
  private final IntIntHash m_rootToNativeEdgeInxMap;

  // This is a utilitarian heap that is used as a bucket of ints.
  // Don't forget to empty() it before using it.
  private final MinIntHeap m_heap;

  private final GraphWeeder m_weeder;

  // We need to remove this listener from the RootGraph during finalize().
  private final RootGraphChangeSniffer m_changeSniffer;

  // Package visible constructor.  rootGraphNodeInx
  // must contain all endpoint nodes corresponding to edges in
  // rootGraphEdgeInx.  All indices must correspond to existing nodes
  // and edges.  The indices lists must be non-repeating.
  FGraphPerspective(FRootGraph root,
                    IntEnumerator rootGraphNodeInx,
                    IntEnumerator rootGraphEdgeInx)
  {
    m_graph = DynamicGraphFactory.instantiateDynamicGraph();
    m_root = root;
    m_lis = new GraphPerspectiveChangeListener[1];
    m_nativeToRootNodeInxMap = new IntArray();
    m_nativeToRootEdgeInxMap = new IntArray();
    m_rootToNativeNodeInxMap = new IntIntHash();
    m_rootToNativeEdgeInxMap = new IntIntHash();
    m_heap = new MinIntHeap();
    m_weeder = new GraphWeeder(m_root, m_graph,
                               m_nativeToRootNodeInxMap,
                               m_nativeToRootEdgeInxMap,
                               m_rootToNativeNodeInxMap,
                               m_rootToNativeEdgeInxMap, m_lis, m_heap);
    m_changeSniffer = new RootGraphChangeSniffer(m_weeder);
    while (rootGraphNodeInx.numRemaining() > 0) {
      final int rootNodeInx = rootGraphNodeInx.nextInt();
      final int nativeNodeInx = m_graph.createNode();
      m_nativeToRootNodeInxMap.setIntAtIndex(rootNodeInx, nativeNodeInx);
      m_rootToNativeNodeInxMap.put(~rootNodeInx, nativeNodeInx); }
    while (rootGraphEdgeInx.numRemaining() > 0) {
      final int rootEdgeInx = rootGraphEdgeInx.nextInt();
      final int rootEdgeSourceInx = m_root.getEdgeSourceIndex(rootEdgeInx);
      final int rootEdgeTargetInx = m_root.getEdgeTargetIndex(rootEdgeInx);
      final boolean rootEdgeDirected = m_root.isEdgeDirected(rootEdgeInx);
      final int nativeEdgeSourceInx =
        m_rootToNativeNodeInxMap.get(~rootEdgeSourceInx);
      final int nativeEdgeTargetInx =
        m_rootToNativeNodeInxMap.get(~rootEdgeTargetInx);
      final int nativeEdgeInx =
        m_graph.createEdge(nativeEdgeSourceInx, nativeEdgeTargetInx,
                           rootEdgeDirected);
      m_nativeToRootEdgeInxMap.setIntAtIndex(rootEdgeInx, nativeEdgeInx);
      m_rootToNativeEdgeInxMap.put(~rootEdgeInx, nativeEdgeInx); }
    m_root.addRootGraphChangeListener(m_changeSniffer);
  }

  // Cannot have any recursize reference to a FGraphPerspective in this
  // object instance - we want to allow garbage collection of unused
  // GraphPerspective objects.
  private final static class RootGraphChangeSniffer
    implements RootGraphChangeListener
  {

    private final GraphWeeder m_weeder;

    private RootGraphChangeSniffer(GraphWeeder weeder)
    {
      m_weeder = weeder;
    }

    public final void rootGraphChanged(RootGraphChangeEvent evt)
    {
      if ((evt.getType() & RootGraphChangeEvent.NODES_REMOVED_TYPE) != 0)
        m_weeder.hideNodes(evt.getSource(), evt.getRemovedNodes());
      if ((evt.getType() & RootGraphChangeEvent.EDGES_REMOVED_TYPE) != 0)
        m_weeder.hideEdges(evt.getSource(), evt.getRemovedEdges());
    }

  }

  // An instance of this class cannot have any recursive reference to a
  // FGraphPerspective object.  The idea behind this class is to allow
  // garbage collection of unused GraphPerspective objects.  This class
  // is used by the RootGraphChangeSniffer to remove nodes/edges from
  // a GraphPerspective; this class is also used by this GraphPerspective
  // implementation itself.
  private final static class GraphWeeder
  {

    private final RootGraph m_root;
    private final DynamicGraph m_graph;
    private final IntArray m_nativeToRootNodeInxMap;
    private final IntArray m_nativeToRootEdgeInxMap;
    private final IntIntHash m_rootToNativeNodeInxMap;
    private final IntIntHash m_rootToNativeEdgeInxMap;

    // This is an array of length 1 - we need an array as an extra reference
    // to a reference because the surrounding GraphPerspective will be
    // modifying the entry at index 0 in this array.
    private final GraphPerspectiveChangeListener[] m_lis;

    // This is a utilitarian heap that is used as a bucket of ints.
    // Don't forget to empty() it before using it.
    private final MinIntHeap m_heap;

    private GraphWeeder(RootGraph root,
                        DynamicGraph graph,
                        IntArray nativeToRootNodeInxMap,
                        IntArray nativeToRootEdgeInxMap,
                        IntIntHash rootToNativeNodeInxMap,
                        IntIntHash rootToNativeEdgeInxMap,
                        GraphPerspectiveChangeListener[] listener,
                        MinIntHeap heap)
    {
      m_root = root;
      m_graph = graph;
      m_nativeToRootNodeInxMap = nativeToRootNodeInxMap;
      m_nativeToRootEdgeInxMap = nativeToRootEdgeInxMap;
      m_rootToNativeNodeInxMap = rootToNativeNodeInxMap;
      m_rootToNativeEdgeInxMap = rootToNativeEdgeInxMap;
      m_lis = listener;
      m_heap = heap;
    }

    // RootGraphChangeSniffer is not to call this method.  We rely on
    // the specified node still existing in the RootGraph in this method.
    private final int hideNode(GraphPerspective source, int rootGraphNodeInx)
    {
      final int returnThis = _hideNode(source, rootGraphNodeInx);
      if (returnThis != 0) {
        final GraphPerspectiveChangeListener listener = m_lis[0];
        if (listener != null) {
          final Node removedNode = m_root.getNode(rootGraphNodeInx);
          listener.graphPerspectiveChanged
            (new GraphPerspectiveNodesHiddenEvent
             (source, new Node[] { removedNode })); } }
      return returnThis;
    }

    // Don't call this method from outside this inner class.
    // Returns 0 if and only if hiding this node was unsuccessful.
    // Otherwise returns the input parameter, the root node index.
    private int _hideNode(Object source, int rootGraphNodeInx)
    {
      if (!(rootGraphNodeInx < 0)) return 0;
      final int nativeNodeIndex =
        m_rootToNativeNodeInxMap.get(~rootGraphNodeInx);
      final IntEnumerator edgeInxEnum;
      try { edgeInxEnum = m_graph.adjacentEdges
              (nativeNodeIndex, true, true, true); }
      catch (IllegalArgumentException e) { return 0; }
      if (edgeInxEnum == null) return 0;
      final Edge[] edgeRemoveArr = new Edge[edgeInxEnum.numRemaining()];
      for (int i = 0; i < edgeRemoveArr.length; i++) {
        final int rootGraphEdgeInx =
          m_nativeToRootEdgeInxMap.getIntAtIndex(edgeInxEnum.nextInt());
        // The edge returned by the RootGraph won't be null even if this
        // hideNode operation is triggered by a node being removed from
        // the underlying RootGraph - this is because when a node is removed
        // from an underlying RootGraph, all touching edges to that node are
        // removed first from that RootGraph, and corresponding edge removal
        // events are fired before the node removal event is fired.
        edgeRemoveArr[i] = m_root.getEdge(rootGraphEdgeInx); }
      hideEdges(source, edgeRemoveArr);
      // nativeNodeIndex tested for validity with adjacentEdges() above.
      if (m_graph.removeNode(nativeNodeIndex)) {
        m_rootToNativeNodeInxMap.put(~rootGraphNodeInx, Integer.MAX_VALUE);
        m_nativeToRootNodeInxMap.setIntAtIndex(0, nativeNodeIndex);
        return rootGraphNodeInx; }
      else throw new IllegalStateException
             ("internal error - node didn't exist, its adjacent edges did");
    }

    // RootGraphChangeSniffer is not to call this method.  We rely on
    // the specified nodes still existing in the RootGraph in this method.
    private final int[] hideNodes(GraphPerspective source, int[] rootNodeInx)
    {
      // We can't use m_heap here because it's use by every _hideNode().
      final MinIntHeap successes = new MinIntHeap();
      final int[] returnThis = new int[rootNodeInx.length];
      for (int i = 0; i < rootNodeInx.length; i++) {
        returnThis[i] = _hideNode(this, rootNodeInx[i]);
        if (returnThis[i] != 0) successes.toss(i); }
      if (successes.size() > 0) {
        final GraphPerspectiveChangeListener listener = m_lis[0];
        if (listener != null) {
          final Node[] successArr = new Node[successes.size()];
          final IntEnumerator enum = successes.elements();
          int index = -1;
          while (enum.numRemaining() > 0)
            successArr[++index] = m_root.getNode(rootNodeInx[enum.nextInt()]);
          listener.graphPerspectiveChanged
            (new GraphPerspectiveNodesHiddenEvent(source, successArr)); } }
      return returnThis;
    }

    // Entries in the nodes array may not be null.
    // This method is to be called by RootGraphChangeSniffer.
    private final void hideNodes(Object source, Node[] nodes)
    {
      // We can't use m_heap here because it's used be every _hideNode().
      final MinIntHeap successes = new MinIntHeap();
      for (int i = 0; i < nodes.length; i++) {
        if (_hideNode(source, nodes[i].getRootGraphIndex()) != 0)
          successes.toss(i); }
      if (successes.size() > 0) {
        final GraphPerspectiveChangeListener listener = m_lis[0];
        if (listener != null) {
          final Node[] successArr = new Node[successes.size()];
          final IntEnumerator enum = successes.elements();
          int index = -1;
          while (enum.numRemaining() > 0)
            successArr[++index] = nodes[enum.nextInt()];
          listener.graphPerspectiveChanged
            (new GraphPerspectiveNodesHiddenEvent(source, successArr)); } }
    }

    // RootGraphChangeSniffer is not to call this method.  We rely on
    // the specified edge still existing in the RootGraph in this method.
    private final int hideEdge(GraphPerspective source, int rootGraphEdgeInx)
    {
      final int returnThis = _hideEdge(rootGraphEdgeInx);
      if (returnThis != 0) {
        final GraphPerspectiveChangeListener listener = m_lis[0];
        if (listener != null) {
          final Edge removedEdge = m_root.getEdge(rootGraphEdgeInx);
          listener.graphPerspectiveChanged
            (new GraphPerspectiveEdgesHiddenEvent
             (source, new Edge[] { removedEdge })); } }
      return returnThis;
    }

    // Don't call this method from outside this inner class.
    // Returns 0 if and only if hiding this edge was unsuccessful.
    // Otherwise returns the input parameter, the root edge index.
    private int _hideEdge(int rootGraphEdgeInx)
    {
      if (!(rootGraphEdgeInx < 0)) return 0;
      final int nativeEdgeIndex =
        m_rootToNativeEdgeInxMap.get(~rootGraphEdgeInx);
      try {
        if (m_graph.removeEdge(nativeEdgeIndex)) {
          m_rootToNativeEdgeInxMap.put(~rootGraphEdgeInx, Integer.MAX_VALUE);
          m_nativeToRootEdgeInxMap.setIntAtIndex(0, nativeEdgeIndex);
          return rootGraphEdgeInx; } }
      catch (IllegalArgumentException e) { } // Thrown by m_graph.removeEdge().
      return 0;
    }

    // RootGraphChangeSniffer is not to call this method.  We rely on
    // the specified edges still existing in the RootGraph in this method.
    private final int[] hideEdges(GraphPerspective source, int[] rootEdgeInx)
    {
      m_heap.empty();
      final MinIntHeap successes = m_heap;
      final int[] returnThis = new int[rootEdgeInx.length];
      for (int i = 0; i < rootEdgeInx.length; i++) {
        returnThis[i] = _hideEdge(rootEdgeInx[i]);
        if (returnThis[i] != 0) successes.toss(i); }
      if (successes.size() > 0) {
        final GraphPerspectiveChangeListener listener = m_lis[0];
        if (listener != null) {
          final Edge[] successArr = new Edge[successes.size()];
          final IntEnumerator enum = successes.elements();
          int index = -1;
          while (enum.numRemaining() > 0)
            successArr[++index] = m_root.getEdge(rootEdgeInx[enum.nextInt()]);
          listener.graphPerspectiveChanged
            (new GraphPerspectiveEdgesHiddenEvent(source, successArr)); } }
      return returnThis;
    }

    // Entries in the edges array may not be null.
    // This method is to be called by RootGraphChangeSniffer.
    private final void hideEdges(Object source, Edge[] edges)
    {
      m_heap.empty();
      final MinIntHeap successes = m_heap;
      for (int i = 0; i < edges.length; i++)
        if (_hideEdge(edges[i].getRootGraphIndex()) != 0)
          successes.toss(i);
      if (successes.size() > 0) {
        final GraphPerspectiveChangeListener listener = m_lis[0];
        if (listener != null) {
          final Edge[] successArr = new Edge[successes.size()];
          final IntEnumerator enum = successes.elements();
          int index = -1;
          while (enum.numRemaining() > 0)
            successArr[++index] = edges[enum.nextInt()];
          listener.graphPerspectiveChanged
            (new GraphPerspectiveEdgesHiddenEvent(source, successArr)); } }
    }

  }

}
