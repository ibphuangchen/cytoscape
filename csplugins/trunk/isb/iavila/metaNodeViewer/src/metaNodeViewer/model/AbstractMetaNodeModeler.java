/**  Copyright (c) 2003 Institute for Systems Biology
 **  This program is free software; you can redistribute it and/or modify
 **  it under the terms of the GNU General Public License as published by
 **  the Free Software Foundation; either version 2 of the License, or
 **  any later version.
 **
 **  This program is distributed in the hope that it will be useful,
 **  but WITHOUT ANY WARRANTY; without even the implied warranty of
 **  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  The software and
 **  documentation provided hereunder is on an "as is" basis, and the
 **  Institute for Systems Biology has no obligations to provide maintenance, 
 **  support, updates, enhancements or modifications.  In no event shall the
 **  Institute for Systems Biology be liable to any party for direct, 
 **  indirect, special,incidental or consequential damages, including 
 **  lost profits, arising out of the use of this software and its 
 **  documentation, even if the Institute for Systems Biology 
 **  has been advised of the possibility of such damage. See the
 **  GNU General Public License for more details.
 **   
 **  You should have received a copy of the GNU General Public License
 **  along with this program; if not, write to the Free Software
 **  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **/
/**
 * @author Iliana Avila-Campillo iavila@systemsbiology.org, iliana.avila@gmail.com
 * @version %I%, %G%
 * @since 2.0
 * 
 * This is a class that knows how to collapse/expand meta-nodes recursively.
 */

package metaNodeViewer.model;
import java.util.*;
import giny.model.*;
import cytoscape.CyNetwork;
import metaNodeViewer.data.MetaNodeAttributesHandler;
import cern.colt.list.IntArrayList;
import cern.colt.map.OpenIntObjectHashMap;
import cern.colt.list.ObjectArrayList;
import cern.colt.map.AbstractIntIntMap;
import cern.colt.map.OpenIntIntHashMap;

/**
 * TODO: Comment better (add class description)
 * TODO: Implements an interface and GraphPerspectiveChangeListener (?)
 * TODO: Optimize more
 */
public class AbstractMetaNodeModeler {

  protected static final boolean DEBUG = false;
  /**
   * A Map from CyNetworks to MetaNodeAttributesHandlers that are used to
   * assign names and attribute values to meta-nodes. 
   * If a network has not been assigned a MetaNodeAttributesHandler, 
   * then defaultAttributesHandler will be used.
   */
  protected Map networkToAttsHandler;
  /**
   * The default MetaNodeAttributesHandler.
   */
  protected MetaNodeAttributesHandler defaultAttributesHandler;
  /**
   * The RootGraph of all CyNetworks for which this modeler will be applied.
   */
  protected RootGraph rootGraph;
  
  // A map from node RootGraph indeces to IntArrayList objects
  // Each node index is the index of a meta-node, and the IntArrayList contains
  // edge RootGraph indices of edges that have been percolated through the RootGraph
  // to model an "abstract" model
  private OpenIntObjectHashMap metaNodeToProcessedEdges;
  
  // A list of root-graph indices of edges that we have created that are connected to at least
  // one meta-node
  private IntArrayList metaEdgesRindices;
  
  // A Map from CyNetworks to IntArrayLists that contain RootGraph indices of nodes
  // that are contained in the corresponding network
  private Map networkToNodes;
    
  /**
   * Since there should only be one AbstractMetaNodeModeler per/Cytoscape, the constructor
   * is protected; use <code>MetaNodeModelerFactory.getCytoscapeAbstractMetaNodeModeler();</code>
   * to get an instance.
   * 
   * @param root_graph the <code>RootGraph</code> of the <code>CyNetworks</code>s
   * that will be modeled
   */
  protected AbstractMetaNodeModeler (RootGraph root_graph){
    this.rootGraph = root_graph;
    this.metaNodeToProcessedEdges = new OpenIntObjectHashMap();
    this.metaEdgesRindices = new IntArrayList();
    this.networkToNodes = new HashMap();
    this.networkToAttsHandler = new HashMap();
    this.defaultAttributesHandler = MetaNodeModelerFactory.DEFAULT_MN_ATTRIBUTES_HANDLER;
  }//constructor
  
  /**
   * Sets the default MetaNodeAttributesHandler, by default, this handler is 
   * MetaNodeModelerFactory.DEFAULT_MN_ATTRIBUTES_HANDLER.
   * 
   * @param handler the handler to be used by default
   */
  public void setDefaultAttributesHandler (MetaNodeAttributesHandler handler){
  	this.defaultAttributesHandler = handler;
  }//setDefaultAttributesHandler

  /**
   * Sets the MetaNodeAttributesHandler that should be used from now on 
   * to transfer node and edge attributes from children nodes and edges to 
   * meta-nodes in the given CyNetwork
   *
   * @param cy_network the CyNetwork for which the handler should be used
   * @param MetaNodeAttributesHandler the handler
   */
  public void setNetworkAttributesHandler (CyNetwork cy_network, MetaNodeAttributesHandler handler){
    this.networkToAttsHandler.put(cy_network,handler);
  }//setNetworkAttributesHandler

   /**
    * Gets the MetaNodeAttributesHandler that is being used to transfer node 
    * and edge attributes from children nodes and edges to meta-nodes for the given
    * CyNetwork
    * 
    * @param cy_network the network for which to return the MetaNodeAttributesHandler in use
    */
  public MetaNodeAttributesHandler getNetworkAttributesHandler (CyNetwork cy_network){
  	MetaNodeAttributesHandler handler = (MetaNodeAttributesHandler)this.networkToAttsHandler.get(cy_network);
  	if(handler == null){
  		return this.defaultAttributesHandler;
  	}
  	return handler;
  }//getNetworkAttributesHandler

  /**
   * Sets the RootGraph whose CyNetworks will be changed
   * so that their meta-nodes can be collapsed and expanded.
   * Clears internal data-structures for the current RootGraph.
   */
  // Should in theory not be needed, since Cytoscape only has ONE RootGraph throughout execution.
  public void setRootGraph (RootGraph new_root_graph){
    this.rootGraph = new_root_graph;
    this.metaNodeToProcessedEdges.clear();
    this.metaEdgesRindices.clear();
    this.networkToNodes.clear();
    this.networkToAttsHandler.clear();
  }//setRootGraph
  
  /**
   * @return the RootGraph that this AbstractMetaNodeModeler models
   */
  public RootGraph getRootGraph (){
    return this.rootGraph;
  }//getRootGraph

  /**
   * Applies the model to the <code>GraphPerspective</code>.
   *
   * @return false if the model could not be applied, maybe because the model
   * had already been applied previously, true otherwise
   * TODO: IMPLEMENT!, also, why does this method not take a GP for an argument???
   */
  public boolean applyModel (){
    return false;
  }//applyModel

  /**
   * Undos the changes made to <code>graphPerspective</code> in order to apply the model.
   *
   * @param temporary_undo whether or not the "undo" is temporary, if not temporary, then, the
   * <code>graphPerspective</code>'s <code>RootGraph</code> will also be modified so that it is
   * in the same state as it was before any calls to <code>applyModel()</code>
   *
   * @return false if the undo was not successful, maybe because the model
   * was already undone for <code>graphPerspective</code>, true otherwise
   * TODO: IMPLEMENT! also, why does this method not take a GP for an argument?
   */
  public boolean undoModel (boolean temporary_undo){
    return false;
  }//undoModel

  /**
   * Applies the model to the <code>Node</code> with the given <code>RootGraph</code> or 
   * <code>GraphPerspective</code> index. 
   * Calls <code>applyModel (cy_network,node_index,getDescendants(node_index))</code>
   *
   * @param cy_network the <code>CyNetwork</code> that will be modified
   * @param node_index the index of the <code>Node</code>, if the node is not contained in
   * <code>cy_network</code> (and is contained in <code>cy_network</code>'s 
   * <code>RootGraph</code>) then the <code>RootGraph</code> index should be given
   *
   * @return true if the node was successfuly abstracted, false otherwise (maybe the node 
   * is not a meta-node, or the node has already been abstracted)
   */
  public boolean applyModel (
                             CyNetwork cy_network,
                             int node_index
                             ){
    int rootNodeIndex = 0;
    if(node_index > 0){
      rootNodeIndex = cy_network.getRootGraphNodeIndex(node_index);
    }else if(node_index < 0){
      rootNodeIndex = node_index;
    }
    
    if(rootNodeIndex == 0){
      if(DEBUG){
        System.err.println("----- applyModel (CyNetwork,"+node_index+",descendants) returning"+
                           " false because root index is 0 -----"); 
      }
      return false;
    }
    IntArrayList d = new IntArrayList();
    getDescendants(rootNodeIndex,d);
    d.trimToSize();
    int [] descendants = d.elements();
    return applyModel(cy_network,node_index,descendants);
  }//applyModel
  
  /**
   * Applies the model to the <code>Node</code> with the given <code>RootGraph</code> or 
   * <code>GraphPerspective</code> index. Use this method instead of 
   * <code>applyModel(cy_network,node_index)</code> if the descendants of the node are
   * known.
   *
   * @param cy_network the <code>CyNetwork</code> that will be modified
   * @param node_index the index of the <code>Node</code>, if the node is not contained in
   * <code>cy_network</code> (and is contained in <code>cy_network</code>'s 
   * <code>RootGraph</code>) then the <code>RootGraph</code> index should be given
   * @param descendants the <code>RootGraph</code> indices of the descendants of node 
   * with index node_index, considerably faster than calling <code>applyModel(cy_network,node_index)</code>
   *
   * @return true if the node was successfuly abstracted, false otherwise (maybe the node 
   * is not a meta-node, or the node has already been abstracted)
   * 
   * NOTE: Descendant nodes are all the nodes that are contained in the tree rooted at node with index node_index
   * (not only the leaves).
   */
  public boolean applyModel (
                             CyNetwork cy_network,
                             int node_index,
                             int [] descendants
                             ){
    
    if(DEBUG){
      System.err.println("----- applyModel (cy_network,"+node_index+") -----");
    }
    
    if(cy_network.getRootGraph() != this.rootGraph){
      // This should not happen
      if(DEBUG){
        System.err.println("----- applyModel (CyNetwork,"+node_index+
                           ") leaving, graphPerspective's RootGraph != this.rootGraph -----");
      }
      return false;
    }
    
    int rootNodeIndex = 0;
    if(node_index > 0){
      rootNodeIndex = cy_network.getRootGraphNodeIndex(node_index);
    }else if(node_index < 0){
      rootNodeIndex = node_index;
    }
    
    if(rootNodeIndex == 0){
      if(DEBUG){
        System.err.println("----- applyModel (CyNetwork,"+node_index+") returning" +
                           " false because root index is 0 -----"); 
      }
      return false;
    }
    
    IntArrayList cnNodes = (IntArrayList)this.networkToNodes.get(cy_network);
    if(cnNodes == null){
      // I know that this is the first time that applyModel is called for the
      // given network
      cnNodes = new IntArrayList(getNodeRindicesInGP(cy_network));
      this.networkToNodes.put(cy_network,cnNodes);
    }
    
    if(!prepareNodeForModel(cy_network,rootNodeIndex)){
      if(DEBUG){
        System.err.println("----- applyModel (CyNetwork,"+node_index+") returning, "+
                           "prepareNodeForModel returned false, returning false -----");
      }
      return false;
    }
        
    // Restore the meta-node, in case that it is not showing
    int restoredNodeIndex =cy_network.restoreNode(rootNodeIndex);
    // The restored meta-node is now contained in the network, so remember this:
    cnNodes.add(restoredNodeIndex);
    
    if(DEBUG){
      System.err.println("Restored node w/rindex " + rootNodeIndex + " and got back index " +
                         restoredNodeIndex);
    }
    
    // Hide the meta-node's descendants and connected edges
    
    // This method is too slow:
    //int [] descendants = this.rootGraph.getNodeMetaChildIndicesArray(rootNodeIndex,true);
    
    int [] hiddenNodes = cy_network.hideNodes(descendants);
    if(DEBUG){
      System.err.println("Hid " + hiddenNodes.length + " descendant nodes of node " + 
                         rootNodeIndex + ", from a total of " + descendants.length + 
                         " descendants");
    }
    // Restore the edges connecting the meta-node and nodes that are in cyNetwork
    int [] gpNodes = cy_network.getNodeIndicesArray();
    if(DEBUG){
      System.err.println("graphPerspective has " + gpNodes.length + " nodes");
    }
    int numRestoredEdges = 0;
    for(int node_i = 0; node_i < gpNodes.length; node_i++){
      
      // Get edges in BOTH directions and restore them
      int [] connectingEdgesRindices1 = 
        this.rootGraph.getEdgeIndicesArray(rootNodeIndex,gpNodes[node_i],true);
      
      // ...other direction
      int [] connectingEdgesRindices2 = 
        this.rootGraph.getEdgeIndicesArray(gpNodes[node_i],rootNodeIndex,true);
     
      int [] connectingEdgesRindices;
      if(connectingEdgesRindices1 != null && connectingEdgesRindices2 != null){
        int size = connectingEdgesRindices1.length + connectingEdgesRindices2.length;
        connectingEdgesRindices = new int[size];
        System.arraycopy(connectingEdgesRindices1, 
                         0, 
                         connectingEdgesRindices, 
                         0, 
                         connectingEdgesRindices1.length);
        System.arraycopy(connectingEdgesRindices2,
                         0,
                         connectingEdgesRindices,
                         connectingEdgesRindices1.length,
                         connectingEdgesRindices2.length);
      }else if(connectingEdgesRindices1 != null){
        connectingEdgesRindices = connectingEdgesRindices1;
      }else if(connectingEdgesRindices2 != null){
        connectingEdgesRindices = connectingEdgesRindices2;
      }else{
        connectingEdgesRindices = null;
      }
            
      if(connectingEdgesRindices == null || connectingEdgesRindices.length == 0){
        if(DEBUG){
          System.err.println("There are no connecting edges between nodes " 
                             + rootNodeIndex + " and " +
                             gpNodes[node_i]);
        }
        continue;
      }
      if(DEBUG){
        System.err.println("There are " + connectingEdgesRindices.length +
                           " edges between nodes " + rootNodeIndex + " and " +
                           gpNodes[node_i]);
      }
      int [] restoredRindices = cy_network.restoreEdges(connectingEdgesRindices);
      if(DEBUG){
        System.err.println("Restored " + restoredRindices.length + "/" + 
                           connectingEdgesRindices.length + " edges connecting " + 
                           rootNodeIndex + " and " + gpNodes[node_i]);
      }
      numRestoredEdges += restoredRindices.length;
    }//for node_i

    if(restoredNodeIndex != 0 || hiddenNodes.length > 0 || numRestoredEdges > 0){
      if(DEBUG){
        System.err.println("----- applyModel (CyNetwork,"+node_index+") returning true -----");
      }
      return true;
    }
    if(DEBUG){
      System.err.println("----- applyModel (CyNetwork,"+node_index+") returning false -----");
    }
    return false;
    
  }//applyModel
  
  /**
   * It undos the model for the <code>Node</code> with the given index.
   *
   * @param cy_network the <code>CyNetwork</code> whose <code>RootGraph</code>
   * should be the one set in the constructor (or through <code>setRootGraph</code>)and in which the 
   * <code>Node</code> with index <code>node_index</code> should reside
   * @param node_index the <code>RootGraph</code> or <code>GraphPerspective</code> index
   * of the <code>Node</code>
   * @param recursive_undo whether or not any existing meta-nodes inside the meta-node being
   * undone should also be undone
   * @param temporary_undo whether or not the "undo" is temporary, if not temporary, then, the
   * <code>cy_network</code>'s <code>RootGraph</code> will also be modified so that it is
   * in the same state as it was before any calls to <code>applyModel(node_index)</code>
   */
  public boolean undoModel (CyNetwork cy_network, 
                            int node_index, 
                            boolean recursive_undo,
                            boolean temporary_undo){

    if(DEBUG){
      System.err.println("----- undoModel (CyNetwork," + node_index + "," + recursive_undo + 
                         "," + temporary_undo + ") -----");
    }

    // Get the RootIndex and make sure it is not 0
    int metaNodeRindex = 0;
    if(node_index < 0){
      metaNodeRindex = node_index;
    }else if(node_index > 0){
      metaNodeRindex = cy_network.getRootGraphNodeIndex(node_index);
    }
    if(metaNodeRindex == 0){
      if(DEBUG){
        System.err.println("----- undoModel (CyNetwork," + node_index + "," + recursive_undo + 
                           ") returning false since the root index of " +
                           node_index + " is zero -----");
      }
      return false;
    }
    
    int [] childrenRindices = null;
    
    if(recursive_undo){

      if(!temporary_undo){
        // Remove edges that *we* created in the RootGraph for this meta-node and 
        // its descendant meta-nodes, it also removes the meta-nodes from this networks client data
      	// available through MetaNodeFactory.METANODES_IN_NETWORK
        removeMetaNode(cy_network,metaNodeRindex, true);
      }
      
      // Get the descendants with no children of this meta-node, since they will
      // be displayed after this call to undo
      // TODO: Check performance of this method:
      // COULD IMPROVE BY USING this.networkToNodes ??
      childrenRindices = this.rootGraph.getChildlessMetaDescendants(metaNodeRindex);
      
      if(DEBUG){
        if(childrenRindices != null){
          System.err.println("childless-descendants of node " + metaNodeRindex + " are: ");
          for(int cd = 0; cd < childrenRindices.length; cd++){
            System.err.println(childrenRindices[cd]);
          }//for cd
        }else{
          System.err.println("node " + metaNodeRindex + " has no childless descendants");
        }
      }// if DEBUG
    
    }else{
      // not recursive
      if(!temporary_undo){
        // Remove edges that *we* created in the RootGraph for this meta-node (only)
        removeMetaNode(cy_network,metaNodeRindex, false);
        
      }
      // Not recursive, so just get the immediate children
      childrenRindices = this.rootGraph.getNodeMetaChildIndicesArray(metaNodeRindex);
    }
     
    if(childrenRindices == null || childrenRindices.length == 0){
      if(DEBUG){
        System.err.println("----- undoModel (CyNetwork," + node_index + "," + recursive_undo + 
                           ") returning false since the given node is not a " +
                           " meta-node -----");
      }
      return false;
    }
    
    // Restore the children nodes and their adjacent edges that connect to other nodes
    // currently contained in CyNetwork
    int [] restoredNodeRindices = cy_network.restoreNodes(childrenRindices, true);
    if(DEBUG){
      System.err.println("Restored " + restoredNodeRindices.length + " children nodes of meta-node" 
                         + metaNodeRindex + " in cy_network");
    }
    // Hide the meta-node and adjacent edges
    int hiddenNodeRindex = cy_network.hideNode(metaNodeRindex);
    // The meta-node is no longer in the network, so remember this:
    IntArrayList cnNodes = (IntArrayList)this.networkToNodes.get(cy_network);
    if(cnNodes != null){
      cnNodes.delete(metaNodeRindex);
    }
    
    if(DEBUG){
      System.err.println("Hid node " + metaNodeRindex + 
                         " in graphPerspective and got back node index " + hiddenNodeRindex);
    }
    
    if(restoredNodeRindices.length > 0 || hiddenNodeRindex != 0){
      if(DEBUG){
        System.err.println("----- undoModel (CyNetwork," + node_index + "," + recursive_undo + 
                           ") returning true -----");
      }
      return true;
    }
    
    if(DEBUG){
      System.err.println("----- undoModel (CyNetwork," + node_index + "," + recursive_undo + 
                         ") returning false -----");
    }
    return false;
  }//undoModel

  /**
   * Creates edges between the Node with index node_rindex and other nodes so that
   * when the Node is collapsed (applyModel) or expanded (undoModel) the needed edges (between meta-nodes and
   * non-meta-nodes, or meta-nodes and meta-nodes), will be there for hiding or unhiding
   * as necessary.
   *
   * @param cy_network edges will be created in the RootGraph of the cy_network
   * @param node_rindex the RootGraph index of the Node to be prepared
   * @return false if the node does not have any descendatns in graph_perspective
   */
  protected boolean prepareNodeForModel (
                                         CyNetwork cy_network,
                                         int node_rindex
                                         ){
    if(DEBUG){
      System.err.println("----- prepareNodeForModel (CyNetwork,"+node_rindex+") -----");
    }
    
    IntArrayList cnNodes = (IntArrayList)this.networkToNodes.get(cy_network);
    if(cnNodes == null){
      // Should not get here
      throw new IllegalStateException ("this.networkToNodes.get(cy_network) returned null");
    }

    if(!isMetaNode(node_rindex)){
      boolean returnBool;
      if(cnNodes.contains(node_rindex)){
        returnBool = true;
      }else{
        returnBool = false;
      }
      if(DEBUG){
        System.err.println("----- prepareNodeForModel (CyNetwork,"+node_rindex+
                           ") leaving, node is not a meta-node, returning is in graphPerspective = " 
                           + returnBool + "-----");
      }
      return returnBool;
    }// !isMetaNode
    
    int [] childrenRindices = this.rootGraph.getNodeMetaChildIndicesArray(node_rindex);
    if(DEBUG){
      if(childrenRindices != null){
        System.err.println("Meta-node " + node_rindex + " has " + childrenRindices.length +
                           " children, recursively calling prepareNodeForModel for each one of them");
      }else{
        System.err.println("Meta-node " + node_rindex + " has no children.");
      }
    }
    
    boolean hasDescendantInGP = false;
    if(childrenRindices != null){
      // Recursively prepare each child node
      for(int i = 0; i < childrenRindices.length; i++){
        boolean temp = prepareNodeForModel(cy_network,childrenRindices[i]);
        hasDescendantInGP = hasDescendantInGP || temp;
      }//for i
    }
    
    // If the meta-node does not have a single descendant in graphPerspective,
    // then skip it
    if(!hasDescendantInGP){
      if(DEBUG){
        System.err.println("----- prepareNodeForModel (CyNetwork,"+node_rindex+
                           ") leaving, meta-node does not have a descendant contained "+
                           "in graphPerspective, returning false -----");
      }
      return false;
    }
    
    // Add edges to the meta-node in this.rootGraph, respect directionality
    int numNewEdges = 0;
    // For the attributes handler:
    AbstractIntIntMap metaEdgeToChildEdge = new OpenIntIntHashMap();
    for(int child_i = 0; child_i < childrenRindices.length; child_i++){
      
      int childNodeRindex = childrenRindices[child_i];
            
      if(DEBUG){
        System.err.println("Child rindex is "+childNodeRindex);
      }
      
      int[] adjacentEdgeRindices = 
        this.rootGraph.getAdjacentEdgeIndicesArray(childNodeRindex,true, true, true);
      
      if(DEBUG){
        System.err.println("Child node "+childNodeRindex+" has "+adjacentEdgeRindices.length+
                           " total adjacent edges in this.rootGraph");
      }
      // Process each edge by creating edges in RootGraph that reflect connections of meta-node
      // children
      for(int edge_i = 0; edge_i < adjacentEdgeRindices.length; edge_i++){
        
        int childEdgeRindex = adjacentEdgeRindices[edge_i];
               
        // See if we already processed this edge before, and if so, skip it
        IntArrayList processedEdgeRindices = 
          (IntArrayList)this.metaNodeToProcessedEdges.get(node_rindex);
        if(processedEdgeRindices != null && processedEdgeRindices.contains(childEdgeRindex)){
          if(DEBUG){
            System.out.println("Edge " + childEdgeRindex + " has already been processed before for "+
                               " meta-node " + node_rindex + ", skipping it.");
          }
          continue;
        }
        if(processedEdgeRindices == null){
          // This List will be used later, so create it if null
          // Also, note that if the IntArrayList for a meta-node is not null, we know that 
          // prepareNodeForModel(GraphPerspective, meta-node_index) has been called before
          // for that node (which is useful for later)
          processedEdgeRindices = new IntArrayList();
        }

        // If the edge connects two descendants of the meta-node, then ignore it, and remember this
        // processed edge
        if(edgeConnectsDescendants(node_rindex,childEdgeRindex)){
          if(DEBUG){
            System.out.println("Edge " + childEdgeRindex + 
                               " connects descendants of node " + node_rindex + ", skipping it.");
          }
          processedEdgeRindices.add(childEdgeRindex);
          this.metaNodeToProcessedEdges.put(node_rindex,processedEdgeRindices);
          continue;
        }

        // Identify the node on the other end of the edge, and determine whether the meta-node
        // is the source or not
        int otherNodeRindex = 0;
        int targetRindex = this.rootGraph.getEdgeTargetIndex(childEdgeRindex);
        int sourceRindex = this.rootGraph.getEdgeSourceIndex(childEdgeRindex);
        boolean metaNodeIsSource = false;
        if(targetRindex == childNodeRindex){
          otherNodeRindex = sourceRindex;
        }else if(sourceRindex == childNodeRindex){
          metaNodeIsSource = true;
          otherNodeRindex = targetRindex;
        }
        
        // Create an edge in rootGraph respecting directionality and remember that 
        // we processed childEdgeRindex
        int newEdgeRindex = 0;
        boolean directedEdge = this.rootGraph.isEdgeDirected(childEdgeRindex);
        if(metaNodeIsSource){
          newEdgeRindex = 
            this.rootGraph.createEdge(node_rindex,
                                      otherNodeRindex,
                                      directedEdge);
        }else{
          newEdgeRindex = 
            this.rootGraph.createEdge(otherNodeRindex,
                                      node_rindex,
                                      directedEdge);
        }
        processedEdgeRindices.add(childEdgeRindex);
        this.metaNodeToProcessedEdges.put(node_rindex,processedEdgeRindices);
        metaEdgeToChildEdge.put(newEdgeRindex, childEdgeRindex);
        numNewEdges++;
        // Remember that *we* created this edge, so that later, we can reset RootGraph
        // to its original state if needed by removing edges we created
        this.metaEdgesRindices.add(newEdgeRindex);
        
        // If otherNodeRindex has parents, and the parents have been processed before,
        // then mark newEdgeRindex as a processed edge for the parents
        // This is so that if prepareNodeForModel(GraphPerspective,otherNodeRindex) is called
        // after this call, a duplicate edge from otherNodeRindex to node_rindex won't be created
        int[] otherNodeParentsRindices = this.rootGraph.getNodeMetaParentIndicesArray(otherNodeRindex);
        if(otherNodeParentsRindices != null){
          for(int otherParent_i = 0; 
              otherParent_i < otherNodeParentsRindices.length; 
              otherParent_i++){
            processedEdgeRindices = 
              (IntArrayList)this.metaNodeToProcessedEdges.get(otherNodeParentsRindices[otherParent_i]);
            if(processedEdgeRindices != null){
              // We know that this parent has been processed before
              processedEdgeRindices.add(newEdgeRindex);
            }
          }// for each otherNodeRindex parent 
        }// if otherNodeRindex has parents
        
        if(DEBUG){
          System.err.println("New edge " + newEdgeRindex + 
                             " created in this.rootGraph:");
          System.err.println( "Source is " + this.rootGraph.getEdgeSourceIndex(newEdgeRindex)+
                              " Target is " + this.rootGraph.getEdgeTargetIndex(newEdgeRindex) );
        }        
        
      }//for each adjacent edge to child node
      
    }// for each child of node_rindex
    
    // Transfer node and edge attributes to the meta-node as needed
    metaEdgeToChildEdge.trimToSize();
    MetaNodeAttributesHandler attributesHandler = getNetworkAttributesHandler(cy_network);
    boolean attributesSet = attributesHandler.setAttributes(cy_network,
                                                            node_rindex,
                                                            childrenRindices,
                                                            metaEdgeToChildEdge);
    if(!attributesSet){
      if(DEBUG){
        System.out.println("----- prepareNodeForModel (CyNetwork," + node_rindex + 
                           "): error,  attributesHandler.setAttributes returned false!!! -----");
      }
    }
    if(DEBUG){
      System.out.println("----- prepareNodeForModel (CyNetwork," + node_rindex + 
                         ") returning true -----");
    }
    return true;
    
  }//prepareNodeForModel

  /**
   * Removes the edges that were created by <code>prepareNodeForModel()</code> that are
   * connected to the <code>Node</code> with index <code>meta_node_index</code> and their
   * attributes in the given CyNetwork. It also remembers that the meta-node is no
   * longer a meta-node for cy_net, and if recursive, it also remembers this for other meta-nodes
   * in the path.
   *
   * @param cy_net the CyNetwork that contains the meta-node
   * @param recursive if true, edges for meta-node descendants of the given node are 
   * also removed (except for those connected to the descendatns with no children)
   */
  protected void removeMetaNode (CyNetwork cy_net, int meta_node_index, boolean recursive){
    
    if(DEBUG){
      System.err.println("----- removeMetaEdges (" + meta_node_index + "," + recursive + ")-----");
    }
    
    if(meta_node_index >= 0){
      if(DEBUG){
        System.err.println("----- removeMetaEdges returning, meta_node_index is  positive -----");
      }
      return;
    }
    
    // If not a meta-node, then return
    if(!isMetaNode(meta_node_index)){
      if(DEBUG){
        System.err.println("----- removeMetaEdges returning, node " + meta_node_index + 
                           " is not a meta node -----");
      }
      return;
    }
    
    // It is a meta-node, remember that it is no longer a meta-node of cy_net
    IntArrayList metaNodesForNetwork = (IntArrayList)cy_net.getClientData(MetaNodeFactory.METANODES_IN_NETWORK);
    if(metaNodesForNetwork != null){
    	metaNodesForNetwork.delete(meta_node_index);
    	metaNodesForNetwork.trimToSize();
    }
  
    // If recursive, get all the descendants of this meta-node, and remove their meta-edges
    if(recursive){
      // This method is too slow:
      //int [] descendants = this.rootGraph.getNodeMetaChildIndicesArray(meta_node_index,true);
      IntArrayList d = new IntArrayList();
      getDescendants(meta_node_index, d);
      d.trimToSize();
      int [] descendants = d.elements();
      for(int i = 0; i < descendants.length; i++){
        removeMetaNode(cy_net, descendants[i], false);
      }// for i
    }// if recursive
    
    IntArrayList adjacentEdgeRindices = 
      new IntArrayList(this.rootGraph.getAdjacentEdgeIndicesArray(meta_node_index,true, true,true));
    adjacentEdgeRindices.trimToSize();
    
    // Remember that this meta-node has no processed edges anymore so that if applyModel
    // is called, new edges are created once more
    if(DEBUG){
      System.err.println("metaNodeToProcessedEdges.containsKey(" + meta_node_index + ") = " +
                         this.metaNodeToProcessedEdges.containsKey(meta_node_index));
    }
    this.metaNodeToProcessedEdges.removeKey(meta_node_index);
    if(DEBUG){
      System.err.println("after removal, metaNodeToProcessedEdges.containsKey(" + meta_node_index + 
                         ") = " +
                         this.metaNodeToProcessedEdges.containsKey(meta_node_index));
    }
    
    // Make sure we are not going to remove edges that were there originally
    adjacentEdgeRindices.retainAll(this.metaEdgesRindices);
    adjacentEdgeRindices.trimToSize();
    
    if(adjacentEdgeRindices.size() == 0){
      if(DEBUG){
        System.err.println("----- removeMetaEdges returning, node " + meta_node_index +
                           " has no adjacent edges -----");
      }
      return;
    } 
    
    // Remove the edges that are connected to this meta-node and that *we* created
    if(DEBUG){
      System.err.println("before removing edges, num e = " + this.rootGraph.getEdgeCount());
    }
    this.rootGraph.removeEdges(adjacentEdgeRindices.elements());
    if(DEBUG){
      System.err.println("after removing edges, num e = " + this.rootGraph.getEdgeCount());
    }
    // Update metaEdgesRindices
    if(DEBUG){
      System.err.println("metaEdgesRindices.size = " + this.metaEdgesRindices.size());
    }
    this.metaEdgesRindices.removeAll(adjacentEdgeRindices);
    if(DEBUG){
      System.err.println("after removing, metaEdgesRindices.size = " + this.metaEdgesRindices.size());
    }
    // Remove the edges from the lists of processed edges
    ObjectArrayList lists = this.metaNodeToProcessedEdges.values();
    for(int i = 0; i < lists.size(); i++){
      IntArrayList processedEdges = (IntArrayList)lists.get(i);
      if(processedEdges == null){
        if(DEBUG){
          System.err.println("processedEdges is null");
        }
        continue;
      }// processedEdges == null
      processedEdges.removeAll(adjacentEdgeRindices);
    }// for i
    
    // And finally, remove the attributes for these edges
    MetaNodeAttributesHandler attributesHandler = getNetworkAttributesHandler(cy_net);
    boolean r = attributesHandler.removeMetaEdgesFromAttributes(cy_net,
                                                                meta_node_index,
                                                                adjacentEdgeRindices.elements());
    if(!r){
      if(DEBUG){
        System.err.println("----- AbstractMetaNodeModeler.removeMetaEdges: error, could not remove" +
                           " attributes for meta-edges");
      }
    }
  }//removeMetaEdges

  /**
   * @return true iff the Node has at least one child
   */
  protected boolean isMetaNode (int node_rindex){
    int [] childrenIndices = this.rootGraph.getNodeMetaChildIndicesArray(node_rindex);
    if(childrenIndices == null || childrenIndices.length == 0){
      return false;
    }
    return true;
  }//isMetaNode

  /**
   * @return true if the edge with edge_rindex connects two descendants of node
   * node_rindex
   */
  protected boolean edgeConnectsDescendants ( int node_rindex, int edge_rindex){
    int [] childrenRindices = this.rootGraph.getNodeMetaChildIndicesArray(node_rindex);
    if(childrenRindices == null || childrenRindices.length == 0){
      return false;
    }
    int sourceRindex = this.rootGraph.getEdgeSourceIndex(edge_rindex);
    int targetRindex = this.rootGraph.getEdgeTargetIndex(edge_rindex);
    // If the source is the descendant of one of the children and so is the target,
    // then return true
    if(this.rootGraph.isNodeMetaChild(node_rindex, sourceRindex, true) && 
       this.rootGraph.isNodeMetaChild(node_rindex, targetRindex, true)){
      return true;
    }
    return false;
  }//edgeConnectsDescendants

  /**
   * @return an array of RootGraph node indices, each index corresponds to a Node that 
   * is contained in graph_perspective or that is contained in this.rootGraph and is 
   * a descendant of a Node contained in graph_perspective
   */
  // NOTE: This method is slow, avoid calling it as much as possible
  protected int [] getNodeRindicesInGP(GraphPerspective graph_perspective){
    if(DEBUG){
      System.err.println("--------- getNodeRindicesInGP (GraphPerspective) ---------");
    }
    int [] nodeRindices = graph_perspective.getNodeIndicesArray();
    IntArrayList gpNodeRindices = new IntArrayList(nodeRindices);
    // If graph_perspective contains meta-nodes, then their descendants are also
    // in this graph_perspective
    for(int i = 0; i < nodeRindices.length; i++){
      if(DEBUG){
        System.err.println("i = " + i);
      }
      // NOTE: This method takes a very long time to run:
      // int [] children = this.rootGraph.getNodeMetaChildIndicesArray(nodeRindices[i],true);
      IntArrayList descendants = new IntArrayList();
      getDescendants(nodeRindices[i],descendants);
      descendants.trimToSize();
      int [] children = descendants.elements();
      //TODO: Optimize
      for(int j = 0; j < children.length; j++){
        if(DEBUG){
          System.err.println("j = " + j + " child = " + children[j]);
        }
        gpNodeRindices.add(children[j]);
      }// for j
    }
    if(DEBUG){
      System.err.println("--------- leaving getNodeRindicesInGP (GraphPerspective) ---------");
    }
    return gpNodeRindices.elements();
  }//getNodeRindicesInGP

  /**
   * Stores in the given IntArrayList the RootGraph indices of the nodes that are descendants of
   * the node with the given RootGraphIndex, descendants are nodes that are direct children, or
   * children of children, etc. of the given node.
   * 
   * @param node_root_index the RootGraph index of the node for which descendants are being returned
   * @param descendatns an IntArrayList to which the RootGraph indices of the descendants will be added
   * 
   * This is faster than RootGraph.getNodeMetaChildIndicesArray(index,true).
   * TODO: Replace implementation in giny?
   */
  public void getDescendants (int node_root_index, IntArrayList descendants){
    if(descendants == null){
      descendants = new IntArrayList();
    }
    // Get all the immediate children
    int [] allNodes = this.rootGraph.getNodeIndicesArray();
    for(int i = 0; i < allNodes.length; i++){
      if(this.rootGraph.isNodeMetaParent(allNodes[i],node_root_index)){
        descendants.add(allNodes[i]);
        getDescendants(allNodes[i],descendants);
      }
    }
  }//getDescendants
  
}//class AbstractMetaNodeModeler
