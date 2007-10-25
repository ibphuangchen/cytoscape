/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

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

package csplugins.enhanced.search;

import cytoscape.CyNetwork;

import org.apache.lucene.store.RAMDirectory;

public interface EnhancedSearch {

	String INDEX_SET = "INDEX_SET";

	String REINDEX = "REINDEX";

	/**
	 * Removes the specified network from the global index. <p/> To free up
	 * memory, this method should be called whenever a network is destroyed.
	 * 
	 * @param network
	 *            CyNetwork Object.
	 */
	void removeNetworkIndex(CyNetwork network);

	/**
	 * Gets the index associated with the specified network.
	 * 
	 * @param network
	 *            Cytoscape Network.
	 * @return RAMDirectory Object.
	 */
	RAMDirectory getNetworkIndex(CyNetwork network);

	/**
	 * Gets the indexing statis of a specified network.
	 * 
	 * @param network
	 *            Cytoscape Network.
	 * @return status String Object.
	 */
	String getNetworkIndexStatus(CyNetwork network);

	/**
	 * Sets the index for the specified network.
	 * 
	 * @param network
	 *            Cytoscape Network.
	 * @param RAMDirectory
	 *            Object.
	 */
	void setNetworkIndex(CyNetwork network, RAMDirectory index);

	/**
	 * Sets the indexing status of the specified network.
	 * 
	 * @param network
	 *            Cytoscape Network.
	 * @param status
	 *            String Object.
	 */
	void setNetworkIndexStatus(CyNetwork network, String status);

}
