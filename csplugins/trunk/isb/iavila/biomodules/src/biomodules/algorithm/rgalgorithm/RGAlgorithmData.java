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
 * An object that bundles the data structures and objects that <code>RGAlgorithm</code>
 * creates for a <code>CyNetwork</code>. This object can be used as a "client-object" that
 * can be accessed through <code>CyNetwork's putClientData()</code> 
 * and <code>getClientData()</code> methods.
 *
 * @author Iliana Avila-Campillo iavila@systemsbiology.org
 * @version %I%, %G%
 * @since 2.0
 */

package biomodules.algorithm.rgalgorithm;

import java.util.*;
import cytoscape.*;
import common.algorithms.hierarchicalClustering.*;
import biomodules.algorithm.rgalgorithm.gui.*;
import filter.cytoscape.CsFilter;
import filter.model.*;

public class RGAlgorithmData {
  
  /**
   * The network for which this <code>RGAlgorithmData</code> stores algorithm data.
   */
  CyNetwork network;
  /**
   * A list of <code>CyNode</code> objects whose order corresponds to
   * the order of rows in the APSP and Manhattan-Distances tables.
   */
  ArrayList orderedNodes;
  /**
   * The All-Pairs-Shortest-Paths table.
   */
  int [][] apsp;
  /**
   * The Manhattan-Distances of the APSP table.
   */
  double [][] manhattanDistances;
  /**
   * The <code>HierarchicalClustering</code> of the nodes in the <code>CyNetwork</code>
   * to which this <code>RGAlgorithmData</code> belongs.
   */
  HierarchicalClustering hClustering;
  /**
   * An object that provides the rules for joining two nodes when hierarchically
   * clustering the network. TODO: static?
   */
  EisenClustering.JoiningCondition jCondition;
  /**
   * An object that provides the rules for deciding whether or not a subtree of
   * the hierarchical tree is a 'cluster' (biomodule in this case) or not.
   * TODO: static?
   */
  EisenClustering.ClusterCondition cCondition;
  /**
   * Minimum number of members that each cluster (biomodule) should have, 3 by default.
   */
  int minNumMembers = 3;
  /**
   * The join number at which to 'cut' the hierarchical-tree for creating biomodules.
   * The value of this field is -1 if it has not been set.
   */
  int cutJoinNumber = -1;
  /**
   * The <code>RGAlgorithmGui</code> that shows a dialog for this object.
   */
  RGAlgorithmGui gui;
  /**
   * A Map from String objects to CyNode[] arrays. The String objects are the biomodule
   * identifiers (for example, the CANONICAL_NAME attribute of meta-nodes, if meta-nodes
   * are used) and the CyNode [] arrays are the member nodes of the biomodules.
   */
  Map bioIdentifierToMembers;
  /**
   * Whether or not intermediary data should be saved in memory or not. 
   * False by default (saves memory).
   */
  boolean saveInterData;
  /**
   * Whether the molecule type of the nodes in the network are known.
   * False by default.
   */
  boolean knownMoleculeType;
  /**
   * A nodes filter to use before calculating biomodules, nodes that
   * pass the filter get ignored while calculating biomodules, null by default
   */
  Filter nodesFilter;
  /**
   * An edges filter to use before calculating biomodules, edges that
   * pass the filter get ignored while calculating biomodules, null by default
   */
  Filter edgesFilter;
  
  /**
   * Constructor.
   */
  public RGAlgorithmData (){
  	this.saveInterData = false;
  }//RGAlgorithmData

  /**
   * Constructor with argument to set network.
   *
   * @param network the <code>CyNetwork</code> for to which this data belongs
   */
  public RGAlgorithmData (CyNetwork network){
    setNetwork(network);
    this.saveInterData = false;
  }//RGAlgorithmData
  
  /**
   * Sets <code>this.apsp</code> and <code>this.manhattanDistances</code> to empty 2D arrays.
   */
  public void clearIntermediaryData (){
  	this.apsp = new int[0][0];
  	this.manhattanDistances = new double[0][0];
  	System.gc(); // run garbage collector!
  }//clearIntermediaryData
  
  /**
   * Sets the <code>CyNetwork</code> for which this <code>RGAlgorithmData</code> bundles
   * data.
   *
   * @param net the <code>CyNetwork</code>
   */
  public void setNetwork (CyNetwork net){
    this.network = net;
  }//setNetwork
  
  /**
   * Sets the list of ordered nodes that are ordered so that node at index 'i' 
   * corresponds to row entries with index 'i' in the apsp and Manhattan Distances tables
   *
   * @param ordered_nodes the list of the ordered <code>CyNode</code> objects
   */
  public void setOrderedNodes (ArrayList ordered_nodes){
    this.orderedNodes = ordered_nodes;
  }//setOrderedNodes

  /**
   * Sets the APSP table.
   *
   * @param distances the 2D int array of distances
   */
  public void setAPSP (int [][] distances){
    this.apsp = distances;
  }//setAPSP
  
  /**
   * Sets the Manhattan-Distances
   *
   * @param m_distances the 2D double array of manhattan distances
   */
  public void setManhattanDistances (double [][] m_distances){
    this.manhattanDistances = m_distances;
  }//setManhattanDistances

  /**
   * Sets the <code>HierarchicalClustering</code> object that was used
   * to cluster the nodes in the <code>CyNetwork</code> to which this 
   * <code>RGAlgorithmData</code> belongs.
   *
   * @param h_clustering the <code>HierarchicalClustering</code>
   */
  public void setHierarchicalClustering (HierarchicalClustering h_clustering){
    this.hClustering = h_clustering;
  }//setHierarchicalClustering

  /**
   * Sets the joining condition that provides the rules for joining two nodes when 
   * hierarchically clustering the network
   *
   * @param j_cond the <code>EisenClustering.JoiningCondition</code> to be set
   */
  public void setJoiningCondition (EisenClustering.JoiningCondition j_cond){
    this.jCondition = j_cond;
  }//setJoiningCondition

  /**
   * Sets the cluster condition that provides the rules for deciding whether or not a 
   * subtree of the hierarchical tree is a 'cluster' (biomodule in this case) or not.
   *
   * @param c_condition the <code>EisenClustering.ClusterCondition</code> to be set
   */
  public void setClusterCondition (EisenClustering.ClusterCondition c_cond){
    this.cCondition = c_cond;
  }//setClusterCondition

  /**
   * Sets the minimum number of proteins that a subtree in the hierarchical-tree must have
   * to be considered a cluster (biomodule). If no molecule type infomation is attached to
   * the nodes in the network, then this is the minimum number of leaves that the subtree
   * must have.
   *
   * @param min_num_members an int that will be the new minimum
   */
  public void setMinNumMembers (int min_num_members){
    this.minNumMembers = min_num_members;
  }//setMinNumMembers

  /**
   * Sets the join number of the hierarchical-tree node at which it will be 'cut' for
   * creating biomodules.
   *
   * @param join_number an int that is assigned by the <code>HierarchicalClustering</code>
   * object to each node in the hierarchical-tree that it calcualtes
   */
  public void setCutJoinNumber (int join_number){
    this.cutJoinNumber = join_number;
  }//setCutJoinNumber

  /**
   * Sets the <code>RGAlgorithmGui</code> that offers user-input for this data.
   *
   * @param gui a <code>RGAlgorithmGui</code>
   */
  public void setGUI (RGAlgorithmGui gui){
    this.gui = gui;
  }//setGUI

  /**
   * Sets the calculated biomodules and their identifiers for the network stored in 
   * this <code>RGAlgorithmData</code>.
   *
   * @param bioIdentifier_to_members a Map from String object that identify the 
   * biomodule to CyNode [] objects that contain the members of the biomodule
   */
  public void setBiomodules (Map bioIdentifier_to_members){
    this.bioIdentifierToMembers = bioIdentifier_to_members;
  }//setBiomodules
  
  /**
   * Sets whether or not the intermediary data (APSP, Manhattan-Distances) should be kept in memory or not, false by default.
   * 
   * @param save true or false
   */
  public void setSaveIntermediaryData (boolean save){
  	this.saveInterData = save;
  }//setSaveIntermediaryData

  /**
   *
   */
  public void setMoleculeTypeKnown (boolean known){
    this.knownMoleculeType = known;
  }//setMoleculeTypeKnown

  /**
   * Sets the filter for nodes, nodes that pass this filter will be ignored
   * for biomodule calculations
   */
  public void setNodeFilter (Filter node_filter){
    this.nodesFilter = node_filter;
  }//setNodeFilter

  /**
   * Sets the filter for edges, edges that pass this filter will be ignored
   * for biomodule calculations
   */
  public void setEdgeFilter (Filter edge_filter){
    this.edgesFilter = edge_filter;
  }//setEdgeFilter
  
  /**
   * @return whether or not the intermediary data (APSP, Manhattan-Distances) will be kept in memory next time
   * it is calculated.
   */
  public boolean getSaveIntermediaryData (){
  	return this.saveInterData;
  }//getSaveIntermediaryData
  
  /**
   * Gets the <code>CyNetwork</code> for which this <code>RGAlgorithmData</code> bundles data
   *
   * @return a <code>CyNetwork</code>
   */
  public CyNetwork getNetwork(){
    return this.network;
  }//getNetwork

  /**
   * Gets the ordered nodes.
   *
   * @return an ArrayList of <code>CyNodes</code> that are ordered to match
   * the row orderin APSP and Manhattan-Distances table
   */
  public ArrayList getOrderedNodes (){
    return this.orderedNodes;
  }//getOrderedNodes
  
  /**
   * Gets the APSP table.
   *
   * @return the 2D int array of distances
   */
  public int[][] getAPSP (){
    return this.apsp;
  }//getAPSP
  
  /**
   * Gets the Manhattan-Distances
   *
   * @return the 2D double array of manhattan distances
   */
  public double[][] getManhattanDistances (){
    return this.manhattanDistances;
  }//getManhattanDistances

  /**
   * Gets the <code>HierarchicalClustering</code> object that was used
   * to cluster the nodes in the <code>CyNetwork</code> to which this 
   * <code>RGAlgorithmData</code> belongs.
   *
   * @return the <code>HierarchicalClustering</code>
   */
  public HierarchicalClustering getHierarchicalClustering (){
    return this.hClustering;
  }//getHierarchicalClustering
  
  /**
   * Gets the joining condition that provides the rules for joining two nodes when 
   * hierarchically clustering the network
   *
   * @return the <code>EisenClustering.JoiningCondition</code>
   */
  public EisenClustering.JoiningCondition  getJoiningCondition (){
    return this.jCondition ;
  }//getJoiningCondition
  
  /**
   * Gets the cluster condition that provides the rules for deciding whether or not a 
   * subtree of the hierarchical tree is a 'cluster' (biomodule in this case) or not.
   *
   * @return the <code>EisenClustering.ClusterCondition</code>
   */
  public EisenClustering.ClusterCondition getClusterCondition (){
    return this.cCondition;
  }//getClusterCondition

   /**
   * Gets the minimum number of proteins that a subtree in the hierarchical-tree must have
   * to be considered a cluster (biomodule). If no molecule type infomation is attached to
   * the nodes in the network, then this is the minimum number of leaves that the subtree
   * must have.
   *
   * @return an int
   */
  public int getMinNumMembers (){
    return this.minNumMembers;
  }//getMinNumMembers
  
  /**
   * Gets the join number of the hierarchical-tree node at which it will be 'cut' for
   * creating biomodules.
   *
   * @return an int that is assigned by the <code>HierarchicalClustering</code>
   * object to each node in the hierarchical-tree that it calcualtes, or -1 if
   * this field has not been set (join numbers are > 0)
   */
  public int getCutJoinNumber (){
    return this.cutJoinNumber;
  }//getCutJoinNumber

  /**
   * Gets the <code>RGAlgorithmGui</code> that offers user-input for this data.
   *
   * @return a <code>RGAlgorithmGui</code>
   */
  public RGAlgorithmGui getGUI (){
    return this.gui;
  }//getGUI

  /**
   * Gets the bimodules and their identifiers that were set through <code>setBiomodules()</code>.
   *
   * @return a Map with String keys that represent the biomodule identifiers, and CyNode[] objects as values
   * that contain the biomodule members
   */
  public Map getBiomodules (){
  	if(this.bioIdentifierToMembers == null){
  		this.bioIdentifierToMembers = new HashMap();
  	}
    return this.bioIdentifierToMembers;
  }//getBiomodules

  /**
   * @return true if the molecule type attribute is known for the network, false otherwise
   */
  public boolean getMoleculeTypeKnown (){
    return this.knownMoleculeType;
  }//getMoleculeTypeKnown
  
   /**
   * Gets the filter for nodes, nodes that pass this filter will be ignored
   * for biomodule calculations
   */
  public Filter getNodeFilter (){
    return this.nodesFilter;
  }//getNodeFilter
  
  /**
   * Gets the filter for edges, edges that pass this filter will be ignored
   * for biomodule calculations
   */
  public Filter getEdgeFilter (){
    return this.edgesFilter;
  }//getEdgeFilter
 
  /**
   * @return a String description of this <code>RGAlgorithmData</code>
   */
  public String toString (){
    
    String nl = System.getProperty("line.separator");
    
    String desc = "---------- RGAlgorithmData ----------" + nl;
    
    if(this.orderedNodes == null){
      desc = desc + "ordered nodes list is null" + nl;
    }else{
      desc = desc + "num ordered nodes = " + this.orderedNodes.size() + nl;
      //CyNode [] nodes = 
      //(CyNode[])this.orderedNodes.toArray(new CyNode[this.orderedNodes.size()]);
      //for(int i = 0; i < nodes.length; i++){
      //desc = desc + nodes[i] + nl;
      //}//for i
    }
    
    if(this.apsp == null){
      desc = desc + "apsp is null" + nl;
    }else{
      desc = desc + "apsp has " + this.apsp.length + " rows" + nl;
    }

    if(this.manhattanDistances == null){
      desc = desc + "manhattan distances is null" + nl;
    }else{
      desc = desc + "manhattan distances has "+this.manhattanDistances.length+" rows" +nl;
    }

    if(this.hClustering == null){
      desc = desc + "hClustering is null" + nl;
    }else{
      desc = desc + "hClustering is not null" + nl;
    }

    if(this.jCondition == null){
      desc = desc + "joining condition is null" + nl;
    }else{
      desc = desc + "joining condition is not null" + nl;
    }

    if(this.cCondition == null){
      desc = desc + "cluster condition is null" + nl;
    }else{
      desc = desc + "cluster condition is not null" + nl;
    }

    desc = desc + "min num members = " + this.minNumMembers + nl;
    desc = desc + "join number = " + this.cutJoinNumber + nl;
    if(this.bioIdentifierToMembers != null){
      desc = desc + "num biomodules = " + this.bioIdentifierToMembers.size() + nl;
    }else{
      desc = desc + "num biomodules = NOT SET" + nl;
    }
    
    desc = desc + "--------------------------------------";
    
    return desc;
  }//toString

}//class RGAlgorithmData
