/*
 Copyright (c) 2006, 2011, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.io.internal.read.xgmml.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.cytoscape.equations.Equation;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.io.internal.read.SUIDUpdater;
import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.cytoscape.io.internal.util.ReadCache;
import org.cytoscape.io.internal.util.session.SessionUtil;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

public class ReadDataManager {

	public static final String GROUP_STATE_ATTRIBUTE = "__groupState";
	public static final String EXTERNAL_EDGE_ATTRIBUTE = "__externalEdges.SUID";

	protected static final String XLINK = "http://www.w3.org/1999/xlink";
	
	/* RDF Data */
	protected String RDFDate;
	protected String RDFTitle;
	protected String RDFIdentifier;
	protected String RDFDescription;
	protected String RDFSource;
	protected String RDFType;
	protected String RDFFormat;

	/* Set of created networks */
	private Set<CyNetwork> publicNetworks;
	
	/* Stack of original network IDs */
	private Stack<Object> networkStack;
	/* Stack of nodes that have a nested graph*/
	private Stack<CyNode> compoundNodeStack;
	
	/* Attribute values */
	protected ParseState attState = ParseState.NONE;
	protected String currentAttributeID;

	/** Edge handle list */
	protected List<String> handleList;
	/** X handle */
	protected String edgeBendX;
	/** Y handle */
	protected String edgeBendY;

	protected List<Object> listAttrHolder;
	
	/** The graph-global directedness, which will be used as default directedness of edges. */
	protected boolean currentNetworkIsDirected = true;

	private Map<Long/*network suid*/, Map<String/*att name*/, String/*att value*/>> networkGraphics;
	private Map<Long/*node suid*/, Map<String/*att name*/, String/*att value*/>> nodeGraphics;
	private Map<Long/*edge suid*/, Map<String/*att name*/, String/*att value*/>> edgeGraphics;
	
	private Map<CyRow, Map<String/*column name*/, String/*equation*/>> equations;

	protected int graphCount;
	protected int graphDoneCount;
	
	private boolean viewFormat;
	private double documentVersion;
	private CyRootNetwork parentNetwork;
	private CyNetwork currentNetwork;
	private CyNode currentNode;
	private CyEdge currentEdge;
	private CyIdentifiable currentElement;
	private CyRow currentRow;
	
	// Network view format properties
	private Object networkViewId;
	private Object networkId;
	private String visualStyleName;
	private String rendererName;
	private Object currentElementId; // node/edge/network old id
	private Map<Object/*old model id*/, Map<String/*att name*/, String/*att value*/>> viewGraphics;
	private Map<Object/*old model id*/, Map<String/*att name*/, String/*att value*/>> viewLockedGraphics;
	
	private final ReadCache cache;
	private final SUIDUpdater suidUpdater;
	private final EquationCompiler equationCompiler;
	private final CyNetworkFactory networkFactory;
	private final CyRootNetworkManager rootNetworkManager;

	private static final Logger logger = LoggerFactory.getLogger(ReadDataManager.class);

	public ReadDataManager(final ReadCache cache,
						   final SUIDUpdater suidUpdater,
						   final EquationCompiler equationCompiler,
						   final CyNetworkFactory networkFactory,
						   final CyRootNetworkManager rootNetworkManager) {
		this.cache = cache;
		this.suidUpdater = suidUpdater;
		this.equationCompiler = equationCompiler;
		this.networkFactory = networkFactory;
		this.rootNetworkManager = rootNetworkManager;
		init();
	}

	public void init() {
		if (!SessionUtil.isReadingSessionFile()) {
			cache.init();
			suidUpdater.init();
		}
		
		viewFormat = false;
		graphCount = 0;
		graphDoneCount = 0;
		documentVersion = 0;
		
		/* RDF Data */
		RDFDate = null;
		RDFTitle = null;
		RDFIdentifier = null;
		RDFDescription = null;
		RDFSource = null;
		RDFType = null;
		RDFFormat = null;

		currentElement = null;
		currentNode = null;
		currentEdge = null;
		currentNetwork = null;
		parentNetwork = null;
		currentNetworkIsDirected = true;
		currentRow = null;

		attState = ParseState.NONE;
		currentAttributeID = null;

		/* Edge handle list */
		handleList = null;

		edgeBendX = null;
		edgeBendY = null;
		
		networkStack = new Stack<Object>();
		compoundNodeStack = new Stack<CyNode>();
		
		publicNetworks = new LinkedHashSet<CyNetwork>();
		equations = new Hashtable<CyRow, Map<String, String>>();
		
		networkGraphics = new LinkedHashMap<Long, Map<String, String>>();
		nodeGraphics = new LinkedHashMap<Long, Map<String, String>>();
		edgeGraphics = new LinkedHashMap<Long, Map<String, String>>();
		
		networkViewId = null;
		networkId = null;
		visualStyleName = null;
		rendererName = null;
		viewGraphics = new LinkedHashMap<Object, Map<String,String>>();
		viewLockedGraphics = new LinkedHashMap<Object, Map<String,String>>();
	}
	
	public void dispose() {
		// At least it should get rid of references to CyNodes, CyNodes and CyEdges!
		publicNetworks = null;
		equations = null;
		compoundNodeStack = null;
		currentElement = null;
		currentNetwork = null;
		currentNode = null;
		currentEdge = null;
		currentRow = null;
		
		// Important: graphics related maps and lists cannot be disposed here,
		// because they may be necessary when creating the network views.
		
		if (!SessionUtil.isReadingSessionFile()) {
			cache.dispose();
			suidUpdater.dispose();
		}
	}
	
	public double getDocumentVersion() {
		return documentVersion;
	}
	
	public void setDocumentVersion(String documentVersion) {
		this.documentVersion = XGMMLParseUtil.parseDocumentVersion(documentVersion);
	}

	public boolean isSessionFormat() {
		return SessionUtil.isReadingSessionFile();
	}
	
	public boolean isViewFormat() {
		return viewFormat;
	}

	public void setViewFormat(boolean viewFormat) {
		this.viewFormat = viewFormat;
	}

	public Set<CyNetwork> getPublicNetworks() {
		return publicNetworks;
	}

	public ReadCache getCache() {
		return cache;
	}
	
	public SUIDUpdater getSUIDUpdater() {
		return suidUpdater;
	}

	/**
	 * @param element A CyNode or CyEdge
	 * @param attName The name of the attribute
	 * @param attValue The value of the attribute
	 */
	protected void addGraphicsAttribute(CyIdentifiable element, String attName, String attValue) {
		if (!ignoreGraphicsAttribute(element, attName)) {
			Map<Long, Map<String, String>> graphics = null;

			if (element instanceof CyNode)
				graphics = nodeGraphics;
			else if (element instanceof CyEdge)
				graphics = edgeGraphics;
			else if (element instanceof CyNetwork)
				graphics = networkGraphics;

			Map<String, String> attributes = graphics.get(element.getSUID());

			if (attributes == null) {
				attributes = new HashMap<String, String>();
				graphics.put(element.getSUID(), attributes);
			}

			attributes.put(attName, attValue);
		}
	}
	
	/**
	 * Used only when reading Cy3 view format XGMML. Because there is no network yet, we use the old model Id as
	 * mapping key.
	 * @param oldModelId The original ID of the CyNode, CyEdge or CyNetwork.
	 * @param attName
	 * @param attValue
	 * @param locked
	 */
	protected void addViewGraphicsAttribute(Object oldModelId, String attName, String attValue, boolean locked) {
		Map<Object, Map<String, String>> graphics = locked ? viewLockedGraphics : viewGraphics;
		Map<String, String> attributes = graphics.get(oldModelId);

		if (attributes == null) {
			attributes = new HashMap<String, String>();
			graphics.put(oldModelId, attributes);
		}

		attributes.put(attName, attValue);
	}

	protected void addGraphicsAttributes(CyIdentifiable element, Attributes atts) {
		if (element != null) {
			final int attrLength = atts.getLength();

			for (int i = 0; i < attrLength; i++) {
				if (element instanceof CyNode)
					addGraphicsAttribute((CyNode) element, atts.getLocalName(i), atts.getValue(i));
				else if (element instanceof CyEdge)
					addGraphicsAttribute((CyEdge) element, atts.getLocalName(i), atts.getValue(i));
				else if (element instanceof CyNetwork)
					addGraphicsAttribute((CyNetwork) element, atts.getLocalName(i), atts.getValue(i));
			}
		}
	}
	
	protected void addViewGraphicsAttributes(Object oldModelId, Attributes atts, boolean locked) {
		if (oldModelId != null) {
			final int attrLength = atts.getLength();
			
			for (int i = 0; i < attrLength; i++)
				addViewGraphicsAttribute(oldModelId, atts.getLocalName(i), atts.getValue(i), locked);
		}
	}

	public Map<String, String> getGraphicsAttributes(CyIdentifiable element) {
		if (element instanceof CyNetwork) return networkGraphics.get(element.getSUID());
		if (element instanceof CyNode)    return nodeGraphics.get(element.getSUID());
		if (element instanceof CyEdge)    return edgeGraphics.get(element.getSUID());

		return null;
	}
	
	public <T extends CyIdentifiable> Map<String, String> getViewGraphicsAttributes(Object oldId, boolean locked) {
		return locked ? viewLockedGraphics.get(oldId) : viewGraphics.get(oldId);
	}

	public void setParentNetwork(CyRootNetwork parent) {
		this.parentNetwork = parent;
	}
	
	public CyRootNetwork getParentNetwork() {
		return this.parentNetwork;
	}

	/**
	 * Just stores all the equation strings per CyIdentifiable and column name.
	 * It does not create the real Equation objects yet.
	 * @param row The network/node/edge row
	 * @param columnName The name of the column
	 * @param formula The equation formula
	 */
	public void addEquationString(CyRow row, String columnName, String formula) {
		Map<String, String> colEquationMap = equations.get(row);
		
		if (colEquationMap == null) {
			colEquationMap = new HashMap<String, String>();
			equations.put(row, colEquationMap);
		}
		
		colEquationMap.put(columnName, formula);
	}
	
	/**
	 * Should be called only after all XGMML attributes have been read.
	 */
	protected void parseAllEquations() {
		for (Map.Entry<CyRow, Map<String, String>> entry : equations.entrySet()) {
			CyRow row = entry.getKey();
			Map<String, String> colEquationMap = entry.getValue();
			
			Map<String, Class<?>> colNameTypeMap = new Hashtable<String, Class<?>>();
			Collection<CyColumn> columns = row.getTable().getColumns();
			
			for (CyColumn col : columns) {
				colNameTypeMap.put(col.getName(), col.getType());
			}
			
			for (Map.Entry<String, String> colEqEntry : colEquationMap.entrySet()) {
				String columnName = colEqEntry.getKey();
				String formula = colEqEntry.getValue();

				if (equationCompiler.compile(formula, colNameTypeMap)) {
					Equation equation = equationCompiler.getEquation();
					row.set(columnName, equation);
				} else {
					logger.error("Error parsing equation \"" + formula + "\": " + equationCompiler.getLastErrorMsg());
				}
			}
		}
	}
	
	protected void setCurrentNetwork(CyNetwork network) {
		this.currentNetwork = network;
	}

	protected CyNetwork getCurrentNetwork() {
		return this.currentNetwork;
	}
	
	/**
	 * @return Stack of network IDs (XGMML IDs).
	 */
	protected Stack<Object> getNetworkIDStack() {
		return networkStack;
	}
	
	public Stack<CyNode> getCompoundNodeStack() {
		return compoundNodeStack;
	}

	protected CyRootNetwork createRootNetwork() {
		final CyNetwork baseNet = networkFactory.createNetwork();
		final CyRootNetwork rootNetwork = rootNetworkManager.getRootNetwork(baseNet);
		
		return rootNetwork;
	}
	
	protected CyRootNetwork getRootNetwork() {
		return (currentNetwork != null) ? rootNetworkManager.getRootNetwork(currentNetwork) : null;
	}
	
    protected CyNode createNode(Object oldId, String label) {
        if (oldId == null)
        	throw new NullPointerException("'oldId' is null.");
        
        CyNode node = null;
        
        if (this.getCurrentNetwork() instanceof CySubNetwork && this.getParentNetwork() != null) {
        	// Do not create the element again if the network is a sub-network!
	        node = cache.getNode(oldId);
	        
	        if (node != null) {
	        	((CySubNetwork) this.getCurrentNetwork()).addNode(node);
	        }
        }
        
        if (node == null) {
	        // OK, create it
	        node = this.getCurrentNetwork().addNode();
        }
        
        // Add to internal cache:
        cache.cache(oldId, node);
        cache.cacheNodeByName(label, node);
        
        mapSUIDs(oldId, node.getSUID());
        
        return node;
    }

	protected CyEdge createEdge(final CyNode source, final CyNode target, Object id, String label, boolean directed) {
		CyEdge edge = null;
		
		if (id == null) id = label;
        
        if (this.getCurrentNetwork() instanceof CySubNetwork && this.getParentNetwork() != null) {
        	// Do not create the element again if the network is a sub-network and the edge already exists!
	        edge = cache.getEdge(id);
	        
	        if (edge != null) {
	        	((CySubNetwork) this.getCurrentNetwork()).addEdge(edge);
	        }
        }
        
        if (edge == null) {
	        // OK, create it
        	CyNetwork net = getCurrentNetwork();
        	// But first get the actual source/target instances from the current network,
        	// because both node instances have to belong to the same root or sub-network.
        	CyNode actualSrc = net.getNode(source.getSUID());
        	CyNode actualTgt = net.getNode(target.getSUID());
        	
        	List<String> extEdgeIds = null; // For 2.x groups
        	
        	if ( (getDocumentVersion() < 3.0 || !isSessionFormat()) && (actualSrc == null || actualTgt == null) ) {
        		// The nodes might have been added to other sub-networks, but not to the current one.
        		// If that is the case, the root network should have both nodes,
        		// so let's just add the edge to the root.
				logger.warn("Cannot add edge \"" + id
						+ "\" to the expected sub-network, because it does not contain the source or target node." +
						" Will try to add the edge to the root-network instead.");
        		
        		net = getRootNetwork();
				
        		if (actualSrc == null)
        			actualSrc = net.getNode(source.getSUID());
        		if (actualTgt == null)
        			actualTgt = net.getNode(target.getSUID());
        		
        		// Does the current subnetwork belong to a 2.x group?
        		if (getDocumentVersion() < 3.0 && !compoundNodeStack.isEmpty() && networkStack.size() > 1) {
        			// Get the current compound node
        			final CyNode grNode = compoundNodeStack.peek();
        			// Get the network of that node (the current one is its network pointer)
        			final CyNetwork grNet = cache.getNetwork(networkStack.elementAt(networkStack.size() - 2));
        			
        			// Check for the group's metadata attribute
        			final CyRow grhRow = grNet.getRow(grNode, CyNetwork.HIDDEN_ATTRS);
        			
        			if (grhRow.isSet(GROUP_STATE_ATTRIBUTE)) { // It's a group!
        				// Add extra metadata for external edges, so that the information is not lost
        				final CyRow rnRow = getRootNetwork().getRow(grNode, CyNetwork.HIDDEN_ATTRS);
        				
        				if (rnRow.getTable().getColumn(EXTERNAL_EDGE_ATTRIBUTE) == null) {
        					rnRow.getTable().createListColumn(EXTERNAL_EDGE_ATTRIBUTE, String.class, false);
        					// These are already the new SUIDs. Let's tell the SUIDUpdater to ignore this column,
        					// in order to prevent it from replacing the correct list by an empty one.
        					suidUpdater.ignoreColumn(rnRow.getTable(), EXTERNAL_EDGE_ATTRIBUTE);
        				}
        				
        				extEdgeIds = rnRow.getList(EXTERNAL_EDGE_ATTRIBUTE, String.class);
        						
        				if (extEdgeIds == null) {
        					extEdgeIds = new ArrayList<String>();
        					rnRow.set(EXTERNAL_EDGE_ATTRIBUTE, extEdgeIds);
        				}
        			}
        		}
        	}
        	
        	edge = net.addEdge(actualSrc, actualTgt, directed);
        	
        	if (extEdgeIds != null)
        		extEdgeIds.add(id.toString());
        	
        	mapSUIDs(id, edge.getSUID());
        }
        
        // Add to internal cache:
        cache.cache(id, edge);

		return edge;
	}
	
	protected void addNetwork(Object oldId, CyNetwork net, boolean isRegistered) {
		if (net != null) {
			if (isRegistered && !(net instanceof CyRootNetwork))
				publicNetworks.add(net);
			
			cache.cache(oldId, net);
			mapSUIDs(oldId, net.getSUID());
		}
	}
	
	protected void addElementLink(String href, Class<? extends CyIdentifiable> clazz) {
		Map<CyNetwork, Set<Long>> map = null;
		Long id = XGMMLParseUtil.getIdFromXLink(href);
		
		if (clazz == CyNode.class)      map = cache.getNodeLinks();
		else if (clazz == CyEdge.class) map = cache.getEdgeLinks();
		
		CyNetwork net = getCurrentNetwork();
		
		if (map != null && net != null) {
			Set<Long> idSet = map.get(net);
			
			if (idSet == null) {
				idSet = new HashSet<Long>();
				map.put(net, idSet);
			}
			
			idSet.add(id);
		}
	}
	
	public Object getNetworkViewId() {
		return networkViewId;
	}

	protected void setNetworkViewId(Object networkViewId) {
		this.networkViewId = networkViewId;
	}

	public Object getNetworkId() {
		return networkId;
	}

	protected void setNetworkId(Object networkId) {
		this.networkId = networkId;
	}

	public String getVisualStyleName() {
		return visualStyleName;
	}

	protected void setVisualStyleName(String visualStyleName) {
		this.visualStyleName = visualStyleName;
	}

	public String getRendererName() {
		return rendererName;
	}

	protected void setRendererName(String rendererName) {
		this.rendererName = rendererName;
	}
	
	protected Object getCurrentElementId() {
		return currentElementId;
	}

	protected void setCurrentElementId(Object currentElementId) {
		this.currentElementId = currentElementId;
	}

	public CyNode getCurrentNode() {
		return currentNode;
	}

	public void setCurrentNode(CyNode currentNode) {
		this.currentNode = currentNode;
	}

	public CyEdge getCurrentEdge() {
		return currentEdge;
	}

	public void setCurrentEdge(CyEdge currentEdge) {
		this.currentEdge = currentEdge;
	}

	public CyRow getCurrentRow() {
		return currentRow;
	}
	
	public void setCurrentRow(CyRow row) {
		this.currentRow = row;
	}
	
	protected CyIdentifiable getCurrentElement() {
		return currentElement;
	}
	
	public void setCurrentElement(CyIdentifiable entry) {
		this.currentElement = entry;
		
		if (entry instanceof CyNetwork)
			setCurrentNetwork((CyNetwork) entry);
		if (entry instanceof CyNode)
			setCurrentNode((CyNode) entry);
		if (entry instanceof CyEdge)
			setCurrentEdge((CyEdge) entry);
	}

 	/**
 	 * Adds old->new SUID references to the SUID Updater 
 	 */
	private void mapSUIDs(final Object oldId, final Long newSUID) {
        if (oldId instanceof Long) // if String (probably Cy2), it has to be handled differently
        	suidUpdater.addSUIDMapping((Long)oldId, newSUID);
	}
	
	/**
	 * It controls which graphics attributes should be parsed.
	 * @param element The network, node or edge
	 * @param attName The name of the XGMML attribute
	 * @return
	 */
	private boolean ignoreGraphicsAttribute(final CyIdentifiable element, String attName) {
		boolean b = false;
		
		// When reading XGMML as part of a CYS file, these graphics attributes should not be parsed.
		if (isSessionFormat() && element != null && attName != null) {
			// Network
			b = b || (element instanceof CyNetwork && attName.matches("backgroundColor"));
			// Nodes or Edges (these are standard XGMML and 2.x <graphics> attributes, not 3.0 bypass properties)
			b = b ||
				((element instanceof CyNode || element instanceof CyEdge) && attName
						.matches("type|fill|w|h|size|width|outline|"
								 + "(cy:)?((node|edge)Transparency|(node|edge)LabelFont|(border|edge)LineType)|"
								 + "(cy:)?(source|target)Arrow(Color)?"));
		}

		return b;
	}
}
