package org.cytoscape.model;


import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.impl.CyNetworkImpl;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.lang.RuntimeException;
import java.util.List;

public class CyNetworkTest extends TestCase {

	private CyNetwork net;

	public static Test suite() {
		return new TestSuite(CyNetworkTest.class);
	}

	public void setUp() {
		net = new CyNetworkImpl("test");
	}

	public void tearDown() {
		net = null;
	}

	public void testAddNode() {
		CyNode n = net.addNode();
		assertNotNull("node is not null",n);
		assertTrue("node index >= 0", n.getIndex() >= 0);
		assertTrue("node index < num node", n.getIndex() < net.getNodeCount() );
	}

	public void testRemoveNode() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();

		// remove 1
		assertEquals("num nodes == 3",3,net.getNodeCount());
		boolean rem1 = net.removeNode( n1 );
		assertTrue("remove node 1 success",rem1);
		assertEquals("num nodes == 2",2,net.getNodeCount());

		// create a dummy node and try removing that
		CyNode n4 = new DummyCyNode(10);
		boolean rem4 = net.removeNode( n4 );
		assertFalse("remove dummy node 4 failure",rem4);
		assertEquals("num nodes == 2",2,net.getNodeCount());

		// add another node
		CyNode n5 = net.addNode();
		assertEquals("num nodes == 3",3,net.getNodeCount());

		// remove the rest of the nodes
		boolean rem5 = net.removeNode( n5 );
		assertTrue("remove node 5 success",rem5);
		assertEquals("num nodes == 2",2,net.getNodeCount());

		boolean rem3 = net.removeNode( n3 );
		assertTrue("remove node 3 success",rem3);
		assertEquals("num nodes == 1",1,net.getNodeCount());

		boolean rem2 = net.removeNode( n2 );
		assertTrue("remove node 2 success",rem2);
		assertEquals("num nodes == 0",0,net.getNodeCount());

		// try redundant remove
		rem2 = net.removeNode( n2 );
		assertFalse("remove node 2 again fails",rem2);
		assertEquals("num nodes == 0",0,net.getNodeCount());
	}

	public void testRemoveNodeWithEdges() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();
		CyNode n4 = net.addNode();
		CyNode n5 = net.addNode();

		CyEdge e1 = net.addEdge(n1,n2,true);
		CyEdge e2 = net.addEdge(n2,n3,true);
		CyEdge e3 = net.addEdge(n4,n2,true);
		CyEdge e4 = net.addEdge(n4,n1,true);
		CyEdge e5 = net.addEdge(n5,n2,false);

		assertEquals("num nodes",5,net.getNodeCount());
		assertEquals("num edges",5,net.getNodeCount());

		boolean rem1 = net.removeNode(n1);
		assertTrue("successfully removed", rem1);
		assertEquals("num nodes",4,net.getNodeCount());
		assertEquals("num edges",3,net.getEdgeCount());

		boolean rem2 = net.removeNode(n2);
		assertTrue("successfully removed", rem2);
		assertEquals("num nodes",3,net.getNodeCount());
		assertEquals("num edges",0,net.getEdgeCount());
	}

	public void testAddEdge() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();

		// add a directed edge
		CyEdge e1 = net.addEdge(n1,n2,true);
		assertNotNull("edge is not null",e1);
		assertTrue("edge index >= 0", e1.getIndex() >= 0);
		assertTrue("edge index < num edge", e1.getIndex() < net.getEdgeCount() );
		assertEquals("edge count",1,net.getEdgeCount());

		// add an undirected edge
		CyEdge e2 = net.addEdge(n2,n3,false);
		assertNotNull("edge is not null",e2);
		assertTrue("edge index >= 0", e2.getIndex() >= 0);
		assertTrue("edge index < num edge", e2.getIndex() < net.getEdgeCount() );
		assertEquals("edge count",2,net.getEdgeCount());

		// try to add a bad edge
		CyNode n4 = new DummyCyNode(10);
		try {
			CyEdge e3 = net.addEdge(n2,n4,false);
			fail("successfully added an invalid edge");
		} catch (RuntimeException e) {
			assertEquals("edge count",2,net.getEdgeCount());
		}

		// and again
		try {
			CyEdge e3 = net.addEdge(n4,n2,true);
			fail("successfully added an invalid edge");
		} catch (RuntimeException e) {
			assertEquals("edge count",2,net.getEdgeCount());
		}

		// try to add a null edge
		try {
			CyEdge e3 = net.addEdge(n2,null,false);
			fail("successfully added a null edge");
		} catch (RuntimeException e) {
			assertEquals("edge count",2,net.getEdgeCount());
		}

		// and again
		try {
			CyEdge e3 = net.addEdge(null,n2,false);
			fail("successfully added a null edge");
		} catch (RuntimeException e) {
			assertEquals("edge count",2,net.getEdgeCount());
		}

		// add multiple edges
		CyEdge e3 = net.addEdge(n1,n2,true);
		assertNotNull("edge is not null",e3);
		assertEquals("edge count",3,net.getEdgeCount());

		CyEdge e4 = net.addEdge(n1,n2,false);
		assertNotNull("edge is not null",e4);
		assertEquals("edge count",4,net.getEdgeCount());

		// add self edge
		CyEdge e5 = net.addEdge(n1,n1,false);
		assertNotNull("edge is not null",e5);
		assertEquals("edge count",5,net.getEdgeCount());

		CyEdge e6 = net.addEdge(n1,n1,true);
		assertNotNull("edge is not null",e6);
		assertEquals("edge count",6,net.getEdgeCount());
	}

	public void testRemoveEdge() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();

		CyEdge e1 = net.addEdge(n1,n2,true);
		CyEdge e2 = net.addEdge(n2,n3,false);
		CyEdge e3 = net.addEdge(n1,n3,false);

		assertEquals("num edges == 3",3,net.getEdgeCount());

		// basic remove
		boolean rem3 = net.removeEdge(e3);
		assertTrue("remove edge 3 success",rem3);
		assertEquals("num edges == 2",2,net.getEdgeCount());

	
		// try to remove dummy edge
		CyEdge e4 = new DummyCyEdge(n1,n2,true,10);
		boolean rem4 = net.removeEdge(e4);
		assertFalse("remove edge 4 failure",rem4);
		assertEquals("num edges == 2",2,net.getEdgeCount());

		// try to remove null edge
		boolean remn = net.removeEdge(null);
		assertFalse("remove null edge failure",remn);
		assertEquals("num edges == 2",2,net.getEdgeCount());

		// add another edge
		CyEdge e5 = net.addEdge(n1,n1,false);
		assertEquals("num nodes == 3",3,net.getNodeCount());

		// remove the rest
		boolean rem5 = net.removeEdge(e5);
		assertTrue("remove edge 5 success",rem5);
		assertEquals("num edges == 2",2,net.getEdgeCount());

		boolean rem2 = net.removeEdge(e2);
		assertTrue("remove edge 2 success",rem2);
		assertEquals("num edges == 1",1,net.getEdgeCount());

		boolean rem1 = net.removeEdge(e1);
		assertTrue("remove edge 1 success",rem1);
		assertEquals("num edges == 0",0,net.getEdgeCount());

		// try redundant remove
		rem1 = net.removeEdge(e1);
		assertFalse("remove edge 1 again fails",rem1);
		assertEquals("num edges == 0",0,net.getEdgeCount());
	}

	// this is functionality is tested elsewhere too
	public void testGetNodeCount() {
		assertEquals("num nodes == 0",0,net.getNodeCount());

		CyNode n1 = net.addNode();
		assertEquals("num nodes == 1",1,net.getNodeCount());

		CyNode n2 = net.addNode();
		assertEquals("num nodes == 2",2,net.getNodeCount());

		CyNode n3 = net.addNode();
		assertEquals("num nodes == 3",3,net.getNodeCount());

		boolean rem3 = net.removeNode(n3);
		assertTrue("successfully removed node 3",rem3);
		assertEquals("num nodes == 2",2,net.getNodeCount());

		rem3 = net.removeNode(n3);
		assertFalse("unsuccessfully removed node 3 again",rem3);
		assertEquals("num nodes == 2",2,net.getNodeCount());

		CyNode n4 = net.addNode();
		assertEquals("num nodes == 3",3,net.getNodeCount());
	}

	// this is functionality is tested elsewhere too
	public void testGetEdgeCount() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();

		CyEdge e1 = net.addEdge(n1,n2,true);
		CyEdge e2 = net.addEdge(n2,n3,false);
		CyEdge e3 = net.addEdge(n1,n3,false);

		assertEquals("num edges == 3",3,net.getEdgeCount());

		// basic remove
		boolean rem3 = net.removeEdge(e3);
		assertTrue("remove edge 3 success",rem3);
		assertEquals("num edges == 2",2,net.getEdgeCount());
	}

	public void testGetNodeList() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();
	
		// check list
		List<CyNode> l = net.getNodeList();
		assertEquals("list size",3,l.size());
		assertTrue("contains node 1",l.contains(n1));
		assertTrue("contains node 2",l.contains(n2));
		assertTrue("contains node 3",l.contains(n3));

		// remove a node and check again
		boolean rem2 = net.removeNode(n2);
		l = net.getNodeList();
		assertEquals("list size",2,l.size());
		assertTrue("contains node 1",l.contains(n1));
		assertFalse("contains node 2",l.contains(n2));
		assertTrue("contains node 3",l.contains(n3));

		// remove a dummy node and check again
		CyNode n4 = new DummyCyNode(10);
		boolean rem4 = net.removeNode(n4);
		l = net.getNodeList();
		assertEquals("list size",2,l.size());
	}

	public void testGetEdgeList() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();

		CyEdge e1 = net.addEdge(n1,n2,true);
		CyEdge e2 = net.addEdge(n2,n3,false);
		CyEdge e3 = net.addEdge(n1,n3,false);

		// check list
		List<CyEdge> l = net.getEdgeList();
		assertEquals("edge list size",3,l.size());
		assertTrue("contains edge 1",l.contains(e1));
		assertTrue("contains edge 2",l.contains(e2));
		assertTrue("contains edge 3",l.contains(e3));

		// add an edge and check again
		CyEdge e4 = net.addEdge(n1,n1,false);
		l = net.getEdgeList();
		assertEquals("edge list size",4,l.size());
		assertTrue("contains edge 4",l.contains(e4));

		// remove an edge and check again
		boolean rem3 = net.removeEdge(e3);
		l = net.getEdgeList();
		assertEquals("edge list size",3,l.size());
		assertFalse("contains edge 3",l.contains(e3));

		// remove a dummy edge and check again
		CyEdge e5 = new DummyCyEdge(n1,n2,true,10);
		boolean rem5 = net.removeEdge(e5);
		l = net.getEdgeList();
		assertFalse("remove dummy edge 5 failure",rem5);
		assertEquals("edge list size",3,l.size());
	}

	public void testIsNode() {
		CyNode n1 = net.addNode();
		CyNode n2 = new DummyCyNode(20);
		assertTrue("node 1 is good", net.contains(n1));
		assertFalse("node 2 is not", net.contains(n2));
	}

	public void testIsEdgeFromEdge() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();

		CyEdge e1 = net.addEdge(n1,n2,true);
		CyEdge e2 = new DummyCyEdge(n1,n2,true,10);

		assertTrue("edge 1 is good",net.contains(e1));
		assertFalse("edge 2 is not",net.contains(e2));
	}

	public void testIsEdgeFromNodes() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();

		CyEdge e1 = net.addEdge(n1,n2,true);

		assertTrue("edge 1 is good",net.contains(n1,n2));
		assertFalse("not and edge",net.contains(n3,n2));
		assertFalse("not and edge",net.contains(n1,n1));
		assertFalse("not and edge",net.contains(n2,n3));
	}

	public void testBasicGetNeighborList() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();
		CyNode n4 = net.addNode();

		CyEdge e1 = net.addEdge(n1,n2,false);
		CyEdge e2 = net.addEdge(n2,n3,false);

		// one neighbor
		List<CyNode> l = net.getNeighborList(n1,CyNetwork.ANY_EDGE);
		assertEquals("one neighbor",1,l.size());
		assertTrue("contains node 2",l.contains(n2));

		// two neighbors
		l = net.getNeighborList(n2,CyNetwork.ANY_EDGE);
		assertEquals("two neighbors",2,l.size());
		assertTrue("contains node 1",l.contains(n1));
		assertTrue("contains node 3",l.contains(n3));
		assertFalse("contains node 4",l.contains(n4));

		// no neighbors
		l = net.getNeighborList(n4,CyNetwork.ANY_EDGE);
		assertEquals("no neighbors",0,l.size());

		// whoa!  what about self edges?
		// TODO
		CyEdge e3 = net.addEdge(n4,n4,false);
		l = net.getNeighborList(n4,CyNetwork.ANY_EDGE);
		assertEquals("one neighbor?",1,l.size());
	}

	public void testUndirectedGetNeighborList() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();
		CyNode n4 = net.addNode();
		CyNode n5 = net.addNode();

		CyEdge e1 = net.addEdge(n1,n2,false);
		CyEdge e2 = net.addEdge(n2,n3,false);
		CyEdge e3 = net.addEdge(n4,n2,false);
		CyEdge e4 = net.addEdge(n4,n1,false);

		List<CyNode> l = net.getNeighborList(n1,CyNetwork.UNDIRECTED_EDGE);
		assertEquals("node 1 neighbors",2,l.size());
		assertTrue("contains node 2",l.contains(n2));
		assertTrue("contains node 4",l.contains(n4));
	
		l = net.getNeighborList(n2,CyNetwork.UNDIRECTED_EDGE);
		assertEquals("node 2 neighbors",3,l.size());
		assertTrue("contains node 1",l.contains(n1));
		assertTrue("contains node 3",l.contains(n3));
		assertTrue("contains node 4",l.contains(n4));

		l = net.getNeighborList(n2,CyNetwork.ANY_EDGE);
		assertEquals("node 2 neighbors",3,l.size());
		assertTrue("contains node 1",l.contains(n1));
		assertTrue("contains node 3",l.contains(n3));
		assertTrue("contains node 4",l.contains(n4));

		l = net.getNeighborList(n2,CyNetwork.INCOMING_EDGE);
		assertEquals("node 2 neighbors",0,l.size());

		l = net.getNeighborList(n2,CyNetwork.OUTGOING_EDGE);
		assertEquals("node 2 neighbors",0,l.size());

		l = net.getNeighborList(n2,CyNetwork.DIRECTED_EDGE);
		assertEquals("node 2 neighbors",0,l.size());
	}

	public void testDirectedGetNeighborList() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();
		CyNode n4 = net.addNode();
		CyNode n5 = net.addNode();

		CyEdge e1 = net.addEdge(n1,n2,true);
		CyEdge e2 = net.addEdge(n2,n3,true);
		CyEdge e3 = net.addEdge(n4,n2,true);
		CyEdge e4 = net.addEdge(n4,n1,true);
		CyEdge e5 = net.addEdge(n5,n2,false);

		List<CyNode> l = net.getNeighborList(n1,CyNetwork.DIRECTED_EDGE);
		assertEquals("node 1 neighbors directed",2,l.size());
		assertTrue("contains node 2",l.contains(n2));
		assertTrue("contains node 4",l.contains(n4));
	
		l = net.getNeighborList(n1,CyNetwork.INCOMING_EDGE);
		assertEquals("node 1 neighbors incoming",1,l.size());
		assertTrue("contains node 4",l.contains(n4));

		l = net.getNeighborList(n1,CyNetwork.OUTGOING_EDGE);
		assertEquals("node 1 neighbors outgoing",1,l.size());
		assertTrue("contains node 2",l.contains(n2));

		l = net.getNeighborList(n2,CyNetwork.UNDIRECTED_EDGE);
		assertEquals("node 2 neighbors undirected",1,l.size());
		assertTrue("contains node 5",l.contains(n5));

		l = net.getNeighborList(n2,CyNetwork.DIRECTED_EDGE);
		assertEquals("node 2 neighbors directed",3,l.size());
		assertTrue("contains node 1",l.contains(n1));
		assertTrue("contains node 3",l.contains(n3));
		assertTrue("contains node 4",l.contains(n4));

		l = net.getNeighborList(n2,CyNetwork.INCOMING_EDGE);
		assertEquals("node 2 neighbors incoming",2,l.size());
		assertTrue("contains node 1",l.contains(n1));
		assertTrue("contains node 4",l.contains(n4));

		l = net.getNeighborList(n2,CyNetwork.OUTGOING_EDGE);
		assertEquals("node 2 neighbors outgoing",1,l.size());
		assertTrue("contains node 3",l.contains(n3));
	}

	public void testBasicGetAdjacentEdgeList() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();
		CyNode n4 = net.addNode();

		CyEdge e1 = net.addEdge(n1,n2,false);
		CyEdge e2 = net.addEdge(n2,n3,false);

		// one edge
		List<CyEdge> l = net.getAdjacentEdgeList(n1,CyNetwork.ANY_EDGE);
		assertEquals("one adjacent edge",1,l.size());
		assertTrue("contains edge 1",l.contains(e1));

		// two edge
		l = net.getAdjacentEdgeList(n2,CyNetwork.ANY_EDGE);
		assertEquals("two adjacent edges",2,l.size());
		assertTrue("contains edge 1",l.contains(e1));
		assertTrue("contains edge 2",l.contains(e2));

		// no adjacent edges
		l = net.getAdjacentEdgeList(n4,CyNetwork.ANY_EDGE);
		assertEquals("no edges",0,l.size());

		// whoa!  what about self edges?
		// TODO
		CyEdge e3 = net.addEdge(n4,n4,false);
		l = net.getAdjacentEdgeList(n4,CyNetwork.ANY_EDGE);
		assertEquals("one adjacent edge?",1,l.size());
	}

	public void testUndirectedGetAdjacentEdgeList() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();
		CyNode n4 = net.addNode();
		CyNode n5 = net.addNode();

		CyEdge e1 = net.addEdge(n1,n2,false);
		CyEdge e2 = net.addEdge(n2,n3,false);
		CyEdge e3 = net.addEdge(n4,n2,false);
		CyEdge e4 = net.addEdge(n4,n1,false);

		List<CyEdge> l = net.getAdjacentEdgeList(n1,CyNetwork.UNDIRECTED_EDGE);
		assertEquals("node 1 adjacent edges",2,l.size());
		assertTrue("contains edge 1",l.contains(e1));
		assertTrue("contains edge 4",l.contains(e4));
	
		l = net.getAdjacentEdgeList(n2,CyNetwork.UNDIRECTED_EDGE);
		assertEquals("node 2 adjacent edges",3,l.size());
		assertTrue("contains edge 1",l.contains(e1));
		assertTrue("contains edge 2",l.contains(e2));
		assertTrue("contains edge 3",l.contains(e3));

		l = net.getAdjacentEdgeList(n2,CyNetwork.ANY_EDGE);
		assertEquals("node 2 adjacent edges",3,l.size());
		assertTrue("contains edge 1",l.contains(e1));
		assertTrue("contains edge 2",l.contains(e2));
		assertTrue("contains edge 3",l.contains(e3));

		l = net.getAdjacentEdgeList(n2,CyNetwork.INCOMING_EDGE);
		assertEquals("node 2 adjacent edges",0,l.size());

		l = net.getAdjacentEdgeList(n2,CyNetwork.OUTGOING_EDGE);
		assertEquals("node 2 adjacent edges",0,l.size());

		l = net.getAdjacentEdgeList(n2,CyNetwork.DIRECTED_EDGE);
		assertEquals("node 2 adjacent edges",0,l.size());
	}

	public void testDirectedGetAdjacentEdgeList() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();
		CyNode n4 = net.addNode();
		CyNode n5 = net.addNode();

		CyEdge e1 = net.addEdge(n1,n2,true);
		CyEdge e2 = net.addEdge(n2,n3,true);
		CyEdge e3 = net.addEdge(n4,n2,true);
		CyEdge e4 = net.addEdge(n4,n1,true);
		CyEdge e5 = net.addEdge(n5,n2,false);

		List<CyEdge> l = net.getAdjacentEdgeList(n1,CyNetwork.DIRECTED_EDGE);
		assertEquals("node 1 adjacent edges directed",2,l.size());
		assertTrue("contains edge 1",l.contains(e1));
		assertTrue("contains edge 4",l.contains(e4));
	
		l = net.getAdjacentEdgeList(n1,CyNetwork.INCOMING_EDGE);
		assertEquals("node 1 adjacent edges incoming",1,l.size());
		assertTrue("contains edge 4",l.contains(e4));

		l = net.getAdjacentEdgeList(n1,CyNetwork.OUTGOING_EDGE);
		assertEquals("node 1 adjacent edges outgoing",1,l.size());
		assertTrue("contains edge 1",l.contains(e1));

		l = net.getAdjacentEdgeList(n2,CyNetwork.UNDIRECTED_EDGE);
		assertEquals("node 2 adjacent edges undirected",1,l.size());
		assertTrue("contains edge 5",l.contains(e5));

		l = net.getAdjacentEdgeList(n2,CyNetwork.DIRECTED_EDGE);
		assertEquals("node 2 adjacent edges directed",3,l.size());
		assertTrue("contains edge 1",l.contains(e1));
		assertTrue("contains edge 2",l.contains(e2));
		assertTrue("contains edge 3",l.contains(e3));

		l = net.getAdjacentEdgeList(n2,CyNetwork.INCOMING_EDGE);
		assertEquals("node 2 adjacent edges incoming",2,l.size());
		assertTrue("contains edge 1",l.contains(e1));
		assertTrue("contains edge 3",l.contains(e3));

		l = net.getAdjacentEdgeList(n2,CyNetwork.OUTGOING_EDGE);
		assertEquals("node 2 adjacent edges outgoing",1,l.size());
		assertTrue("contains edge 2",l.contains(e2));
	}

	public void testBasicGetConnectingEdgeList() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();

		CyEdge e1 = net.addEdge(n1,n2,false);
		CyEdge e2 = net.addEdge(n2,n3,false);
		CyEdge e3 = net.addEdge(n1,n2,false);
		CyEdge e4 = net.addEdge(n1,n2,false);

		// between node 1 and 2
		List<CyEdge> l = net.getConnectingEdgeList(n1,n2,CyNetwork.ANY_EDGE);
		assertEquals("connecting edges",3,l.size());
		assertTrue("contains edge 1",l.contains(e1));
		assertTrue("contains edge 3",l.contains(e1));
		assertTrue("contains edge 4",l.contains(e4));

		// between node 2 and 3
		l = net.getConnectingEdgeList(n3,n2,CyNetwork.ANY_EDGE);
		assertEquals("connecting edges",1,l.size());
		assertTrue("contains edge 2",l.contains(e2));

		// between node 2 and 3 after adding an edge
		CyEdge e5 = net.addEdge(n3,n2,false);
		l = net.getConnectingEdgeList(n2,n3,CyNetwork.ANY_EDGE);
		assertEquals("connecting edges",2,l.size());
		assertTrue("contains edge 2",l.contains(e2));
		assertTrue("contains edge 5",l.contains(e5));

		// between node 2 and 3 after deleting an edge
		boolean rem5 = net.removeEdge(e5);
		assertTrue("removed successfully",rem5);
		l = net.getConnectingEdgeList(n2,n3,CyNetwork.ANY_EDGE);
		assertEquals("connecting edges",1,l.size());
		assertTrue("contains edge 2",l.contains(e2));
	}

	public void testUndirectedBasicGetConnectingEdgeList() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();

		CyEdge e1 = net.addEdge(n1,n2,false);
		CyEdge e2 = net.addEdge(n2,n3,false);
		CyEdge e3 = net.addEdge(n1,n2,false);
		CyEdge e4 = net.addEdge(n1,n2,true);

		// between node 1 and 2
		List<CyEdge> l = net.getConnectingEdgeList(n1,n2,CyNetwork.UNDIRECTED_EDGE);
		assertEquals("connecting edges",2,l.size());
		assertTrue("contains edge 1",l.contains(e1));
		assertTrue("contains edge 3",l.contains(e3));
	}

	public void testDirectedBasicGetConnectingEdgeList() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();

		CyEdge e1 = net.addEdge(n1,n2,true);
		CyEdge e2 = net.addEdge(n2,n3,true);
		CyEdge e3 = net.addEdge(n1,n2,true);
		CyEdge e4 = net.addEdge(n2,n1,true);
		CyEdge e5 = net.addEdge(n2,n1,false);

		// between node 1 and 2
		List<CyEdge> l = net.getConnectingEdgeList(n1,n2,CyNetwork.DIRECTED_EDGE);
		assertEquals("connecting edges",3,l.size());
		assertTrue("contains edge 1",l.contains(e1));
		assertTrue("contains edge 3",l.contains(e3));
		assertTrue("contains edge 4",l.contains(e4));
	}

	public void testGetNode() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();

		assertEquals("nodes are equivalent",n1,net.getNode( n1.getIndex() ));
		assertEquals("nodes are equivalent",n2,net.getNode( n2.getIndex() ));
		assertEquals("nodes are equivalent",n3,net.getNode( n3.getIndex() ));
		assertNull("node is null ",net.getNode(72));
	}

	public void testGetEdge() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();

		CyEdge e1 = net.addEdge(n1,n2,true);
		CyEdge e2 = net.addEdge(n2,n3,true);

		assertEquals("edges are equivalent",e1,net.getEdge( e1.getIndex() ));
		assertEquals("edges are equivalent",e2,net.getEdge( e2.getIndex() ));
		assertNull("edge is null ",net.getEdge(72));
	}

	private class DummyCyNode implements CyNode {
		int ind;
		DummyCyNode( int x ) {
			ind = x;
		}
		public int getIndex() {
			return ind;
		}
	}

	private class DummyCyEdge implements CyEdge {
	
		CyNode source;
		CyNode target;
		int index;
		boolean directed;

		DummyCyEdge(CyNode src, CyNode tgt, boolean dir, int ind) {
			source = src;
			target = tgt;
			directed = dir;
			index = ind;
		}

		public int getIndex() {
			return index;
		}
		public CyNode getSource() {
			return source;
		}

		public CyNode getTarget() {
			return target;
		}

		public boolean isDirected() {
			return directed;
		}
	}
}
