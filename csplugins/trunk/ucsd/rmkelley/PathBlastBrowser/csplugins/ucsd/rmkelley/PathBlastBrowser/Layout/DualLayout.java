package csplugins.ucsd.rmkelley.PathBlastBrowser.Layout;
import java.awt.event.ActionEvent;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JMenu;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.CytoscapeInit;
import cytoscape.data.readers.GMLTree;
import cytoscape.plugin.CytoscapePlugin;
import cytoscape.view.CyNetworkView;
//import giny.view.GraphView;
import cytoscape.actions.FitContentAction;

//for saving the view to an image
import java.io.File;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import phoebe.PGraphView;
import java.awt.Component;
import ding.view.DGraphView;
import ding.view.InnerCanvas;

/**
 * This is a plugin to separate a compatability graph into two
 * separate graphs, one for each species. It tries to lay the graphs
 * out such that homologous nodes are in a similar position in each graph.
 * In order to achieve this, it uses a force-directed layout, where the relevant
 * forces are repulsion between nodes, attraction between nodes connected by edge
 * and psuedo-attraction between homologous nodes (node will actuall be attracted to
 * that is "offset" away from the real node.
 */
public class DualLayout extends CytoscapePlugin{

  DualLayoutCommandLineParser parser;
  public static String NEW_TITLE = "Split Graph";
  /**
   * This constructor saves the cyWindow argument (the window to which this
   * plugin is attached) and adds an item to the operations menu.
   */
  public DualLayout() {
	JMenu topMenu = new JMenu("Dual Layout");
	//Cytoscape.getDesktop().getCyMenus().getOperationsMenu().add(new DualLayoutAction());
    Cytoscape.getDesktop().getCyMenus().getOperationsMenu().add(topMenu);
    topMenu.add(new DualLayoutAction() );
    topMenu.add(new AbstractAction("Edit Options"){
    	public void actionPerformed(ActionEvent ae){
    		DualLayoutOptionsDialog dialog = new DualLayoutOptionsDialog(parser);
    		dialog.show();
    	}
    });
    //parser = new DualLayoutCommandLineParser(Cytoscape.getCytoscapeObj().getConfiguration().getArgs());
    parser = new DualLayoutCommandLineParser(CytoscapeInit.getArgs());
   
    
    if(parser.run()){
      Thread t = new PluginTask(Cytoscape.getCurrentNetwork(),parser,Cytoscape.getCurrentNetwork().getTitle()+" - Split Graph"); 
      t.run();
    }
  }

  /**
   * This class gets attached to the menu item.
   */
  public class DualLayoutAction extends AbstractAction {

    /**
     * The constructor sets the text that should appear on the menu item.
     */
    public DualLayoutAction() {super("Dual Layout");}

    /**
     * Gives a description of this plugin.
     */
    public String describe() {
      StringBuffer sb = new StringBuffer();
      sb.append("Split a compatability graph and try to lay it out");
      return sb.toString();
    }

    /**
     * This method is called when the user selects the menu item.
     */
    public void actionPerformed(ActionEvent ae) {

      //inform listeners that we're doing an operation on the network
      Thread t = new PluginTask(Cytoscape.getCurrentNetwork(),parser,Cytoscape.getCurrentNetwork().getTitle()+" - Split"); 
      t.run();


    }
  }

  class PluginTask extends Thread{
    protected CyNetwork cyNetwork;
    protected DualLayoutCommandLineParser parser;
    protected String title;
    public PluginTask(CyNetwork cyNetwork, DualLayoutCommandLineParser parser, String title){
      this.cyNetwork = cyNetwork;
      this.parser = parser;
      this.title = title;
    }

    public void run(){
      CyNetwork splitGraph = Cytoscape.createNetwork(title);
      DualLayoutTask task = new DualLayoutTask(cyNetwork,parser);
      CyNetworkView view = null;
      
      if(!Cytoscape.viewExists(splitGraph.getIdentifier())){
	view = Cytoscape.createNetworkView(splitGraph);
      }
      else{
	view = Cytoscape.getNetworkView(splitGraph.getIdentifier());
      }
      task.splitNetwork(splitGraph);
      /*
       * If a view does not exist for this network
       * try to create one
       */
      
      //task.layoutNetwork((PGraphView)view);
      task.layoutNetwork(view);
      if(parser.addEdges()){
	//implement the edge addition stuff here
	task.addHomologyEdges(splitGraph);
      }
 					FitContentAction fitAction = new FitContentAction();							   
      fitAction.actionPerformed(new ActionEvent(this,0,""));								   
      //newWindow.getVizMapManager().applyAppearances();
      try {
									 InnerCanvas component = (InnerCanvas)((DGraphView)view).getComponent();
										component.reshape(0,0,1000,1000);
										BufferedImage out = new BufferedImage(component.getWidth(),component.getHeight(),BufferedImage.TYPE_INT_RGB);
										component.print(out.getGraphics());
										ImageIO.write(out,"png",new File("bill.png"));  
      } catch ( Exception e) {
										e.printStackTrace();
      } // end of try-catch
    
      if(parser.save()){
	String name = parser.getGMLname(); 
	try {
	  FileWriter fileWriter = new FileWriter(name);
	  GMLTree result = new GMLTree(view);
	  fileWriter.write(result.toString());
	  fileWriter.close();
	}
	catch (IOException ioe) {
	  System.err.println("Error while writing " + name);
	  ioe.printStackTrace();
	}
      }
      if(parser.exit()){
	System.exit(0);
      }
    }
  }
}
