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
 * A graphical user interface for the algorithm implemented in <code>RGAlgorithm</code>.
 *
 * @author Iliana Avila-Campillo iavila@systemsbiology.org
 * @version %I%, %G%
 * @since 2.0
 */
package biomodules.algorithm.rgalgorithm.gui;

import biomodules.algorithm.rgalgorithm.*;
import biomodules.view.ViewUtils;
import common.algorithms.hierarchicalClustering.*;
import cytoscape.*;
import cytoscape.view.*;
import cytoscape.data.*;
import cytoscape.util.SwingWorker;
import metaNodeViewer.GPMetaNodeFactory;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import utils.*;

public class RGAlgorithmGui extends JDialog {
  
  protected final static String BIOMODS_PLOT = "Tree node index vs. Num biomods";
  protected final static String DISTANCES_PLOT = "Tree node index vs. Join distance";
  protected final static int BOUNDS_ERROR = -1;
  protected final static int NOT_A_NUM_ERROR = -2;
  protected final static int UNKNOWN_BOUNDS = -3;
  protected final static boolean DEFAULT_VIEW_DATA = false;
  protected final static int APSP_TABLE = 0;
  protected final static int MD_TABLE = 1;
    
  protected RGAlgorithmData algorithmData;
  protected JTextField minSizeField;
  protected JPanel plotPanel;
  protected HCPlot numBiomodulesPlot;
  protected HCPlot distancesPlot;
  protected JLabel yLabel;
  protected JLabel yField;
  protected JTextField xField;
  protected PlotListener numBiomodulesPlotListener;
  protected PlotListener distancesPlotListener;
  protected JRadioButton abstractRbutton;
  protected DataTable apspTable;
  protected DataTable mdTable;

  /**
   * Constructor, calls <code>create()</code>.
   *
   * @param network the <code>CyNetwork</code> for which this dialog displays options.
   */
  public RGAlgorithmGui (CyNetwork network){
    super();
    setTitle("R&G Biomodules Calculator");
    this.algorithmData = RGAlgorithm.getClientData(network);
    create();
  }//RGAlgorithmGui

  /**
   * Creates the dialog.
   */
  protected void create (){
    
    Container mainPanel = getContentPane();
    
    JTabbedPane tabbedPane = new JTabbedPane();
    
    JPanel paramsPanel = createParamsPanel();
    tabbedPane.add("Parameters", paramsPanel);
    
    JPanel visPanel = createVisualizationPanel();
    tabbedPane.add("Visualization", visPanel);
    
    JPanel dataPanel = createDataPanel();
    tabbedPane.add("Data", dataPanel);

    JPanel buttonsPanel = createButtonsPanel();

    mainPanel.add(tabbedPane,BorderLayout.CENTER);
    mainPanel.add(buttonsPanel,BorderLayout.PAGE_END);
    
  }//create

  /**
   * Creates and returns a panel for inputing parameters to the
   * <code>RGAlgorithm</code>.
   * 
   * @return a <code>JPanel</code>
   */
  protected JPanel createParamsPanel (){
    
    JPanel paramsPanel = new JPanel();
    paramsPanel.setLayout(new BoxLayout(paramsPanel, BoxLayout.Y_AXIS));
    
    // ------- Min number of members -------- //
    // TODO: Option for min number of proteins
    JPanel sizePanel = new JPanel();
    sizePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
    JLabel minSizeLabel = new JLabel("Minimum biomodule size:");
    this.minSizeField = new JTextField(
                            Integer.toString(this.algorithmData.getMinNumMembers()),
                            3);
    minSizeField.addActionListener(
                                   new AbstractAction (){
                                     public void actionPerformed (ActionEvent e){
                                       if(readInput(minSizeField) < 0){
                                         showErrorMessageDialog("Enter a positive integer.");
                                       }
                                     }
                                   }
                                   );
    sizePanel.add(minSizeLabel);
    sizePanel.add(Box.createHorizontalStrut(3));
    sizePanel.add(minSizeField);
    paramsPanel.add(sizePanel);
    
    // --------- Plots ---------- //
    JPanel optionsPanel = new JPanel();
    optionsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
    JLabel plotOpsLabel = new JLabel("Plot:");
    String [] plotOps = {BIOMODS_PLOT,DISTANCES_PLOT};
    JComboBox plotsOptions = new JComboBox(plotOps);
    plotsOptions.addActionListener(new PlotsOptionsListener());
    optionsPanel.add(plotOpsLabel);
    optionsPanel.add(Box.createHorizontalStrut(3));
    optionsPanel.add(plotsOptions);
    
    paramsPanel.add(optionsPanel);

    this.plotPanel = new JPanel();
    this.plotPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
    this.numBiomodulesPlot = createNumBiomodulesPlot();
    this.numBiomodulesPlotListener = new PlotListener();
    this.numBiomodulesPlot.addJoinBarListener(this.numBiomodulesPlotListener);
    this.distancesPlot = createDistancesPlot();
    this.distancesPlotListener = new PlotListener();
    this.distancesPlot.addJoinBarListener(this.distancesPlotListener);
    this.plotPanel.add(this.numBiomodulesPlot.getContentPane());
    
    paramsPanel.add(this.plotPanel);
    
    JPanel xyPanel = new JPanel();
    xyPanel.setLayout(new BoxLayout(xyPanel, BoxLayout.Y_AXIS));
    JLabel treeNodeLabel = new JLabel("Tree node index:");
    int selectedJoinNumber = this.algorithmData.getCutJoinNumber();
    this.xField = new JTextField(Integer.toString(selectedJoinNumber),4);
    this.xField.addActionListener(new XFieldListener());
    JPanel xPanel = new JPanel();
    xPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
    xPanel.add(treeNodeLabel);
    xPanel.add(Box.createHorizontalStrut(3));
    xPanel.add(this.xField);
    xyPanel.add(xPanel);
    
    this.yLabel = new JLabel("Num biomodules:");
    this.yField = new JLabel("N/A");
    JPanel yPanel = new JPanel();
    yPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
    yPanel.add(this.yLabel);
    yPanel.add(Box.createHorizontalStrut(3));
    yPanel.add(this.yField);
    xyPanel.add(yPanel);
    
    paramsPanel.add(xyPanel);
    
    return paramsPanel;
  }//createParamsPanel

  /**
   * Creates and returns a <code>HCPlot</code> for number of biomodules at each join in the
   * hierarchical-tree contained in <code>this.algorithmData</code>. 
   */
  protected HCPlot createNumBiomodulesPlot (){
    HierarchicalClustering hClustering = this.algorithmData.getHierarchicalClustering();
    
    double [] numBiomodulesAtJoin;
    double minNumBiomods = 0;
    double maxNumBiomods = 0;
    int cutJoin = 0;
    int [] numClustersPerJoin = null;
    
    if(hClustering != null){
      numClustersPerJoin = hClustering.getNumClustersAtIterations();
    }
      
    if(numClustersPerJoin == null || numClustersPerJoin.length == 0){
      int numIterations = this.algorithmData.getNetwork().getNodeCount() - 1;
      if(numIterations <= 0){
        // HCPlot crashes if this is 0, so make it 1
        numIterations = 1;
      }
      numBiomodulesAtJoin = new double[numIterations];
      Arrays.fill(numBiomodulesAtJoin,0);
    }else{
      numBiomodulesAtJoin = new double[numClustersPerJoin.length];
      for(int i = 0; i < numClustersPerJoin.length; i++){
        numBiomodulesAtJoin[i] = (double)numClustersPerJoin[i];
      }//for i
      minNumBiomods = (double)hClustering.getMinNumClusters();
      maxNumBiomods = (double)hClustering.getMaxNumClusters();
      cutJoin = this.algorithmData.getCutJoinNumber();
    }
    
    return new HCPlot( 
                      numBiomodulesAtJoin,
                      minNumBiomods,
                      maxNumBiomods,
                      cutJoin,
                      "Tree_node_index",
                      "Num_Biomods"
                      );
  }//createNumBiomodulesPlot

  /**
   * Creates and returns a <code>HCPlot</code> for distances between children of
   * hierarchical-tree nodes in <code>this.algorithmData</code>
   */
  protected HCPlot createDistancesPlot (){
    HierarchicalClustering hClustering = this.algorithmData.getHierarchicalClustering();
    
    double [] distancesAtJoin = null;
    // The smallest distance with which two nodes in the hierarchical-tree were joined
    double minJoiningValue = 0;
    // The largest one
    double maxJoiningValue = 0;
    int cutJoin = 0;
      
    if(hClustering != null){
      distancesAtJoin = hClustering.getJoinDistances();
    }
      
    if(distancesAtJoin == null || distancesAtJoin.length == 0){
      int numIterations = this.algorithmData.getNetwork().getNodeCount() - 1;
      if(numIterations <= 0){
        // HCPlot crashes if this is 0, so make it 1
        numIterations = 1;
      }
      distancesAtJoin = new double[numIterations];
      Arrays.fill(distancesAtJoin,0);
    }else{
      minJoiningValue = hClustering.getMinimumRowJoiningValue();
      maxJoiningValue = hClustering.getMaximumRowJoiningValue();
      cutJoin = this.algorithmData.getCutJoinNumber();
    }
    
    return new HCPlot( 
                      distancesAtJoin,
                      minJoiningValue,
                      maxJoiningValue,
                      cutJoin,
                      "Tree_node_index",
                      "Join_Distance"
                      );
  }//createDistancesPlot

  /**
   * Creates and returns a panel with settings for visualizing biomodules.
   *
   * @return a <code>JPanel</code>
   */
  protected JPanel createVisualizationPanel (){
    JPanel visPanel = new JPanel();
    visPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
    
    this.abstractRbutton = new JRadioButton("Abstract Biomodules");
    String netID = this.algorithmData.getNetwork().getIdentifier();
    CyNetworkView netView = Cytoscape.getNetworkView(netID);
    if(netView != null){
      this.abstractRbutton.setSelected(true);
    }
    visPanel.add(this.abstractRbutton);
    return visPanel;
  }//createVisualizationPanel

  /**
   * Creates and returns a panel for showing the data that the <code>RGAlgorithm</code>
   * produces.
   *
   * @return a <code>JPanel</code>
   */
  protected JPanel createDataPanel (){
  	JPanel dataPanel = new JPanel();
  	dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
  	
  	JRadioButton viewDataRadioButton = new JRadioButton("View data after it is calculated", this.algorithmData.getSaveIntermediaryData());
  	dataPanel.add(viewDataRadioButton);
  	
  	JButton apspButton = new JButton("Display All-Pairs-Shortest-Paths");
  	apspButton.addActionListener(new DisplayTableAction(APSP_TABLE));
  	
  	JButton distButton = new JButton("Display Manhattan Distances");
  	distButton.addActionListener(new DisplayTableAction(MD_TABLE));
  	
  	JButton biomodsButton = new JButton("Display Biomodules Table");
  	
  	dataPanel.add(apspButton);
  	dataPanel.add(distButton);
  	dataPanel.add(biomodsButton);
  	
    return dataPanel;
  }//createDataPanel

  /**
   * Creates and returns a panel with buttons (Calculate, Dismiss, etc).
   *
   * @return a <code>JPanel</code>
   */
  protected JPanel createButtonsPanel (){
    JPanel buttonsPanel = new JPanel();
    buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
    
    JButton calculateBiomodsButton = new JButton("Calculate");
    calculateBiomodsButton.addActionListener(
                                             new AbstractAction (){
                                               public void actionPerformed (ActionEvent e){
                                                 //SwingWorker w = new SwingWorker(){
                                                 //  public Object construct (){
                                                 calculateBiomodules();
                                                 //    return null;
                                                 //}
                                                 //};
                                                 //}
                                               }//actionPerformed
                                             }//AbstractAction
                                             );
    buttonsPanel.add(calculateBiomodsButton);
    
    JButton closeWindowButton = new JButton("Dismiss");
    closeWindowButton.addActionListener(
                                        new AbstractAction (){
                                          public void actionPerformed (ActionEvent e){
                                            RGAlgorithmGui.this.dispose();
                                          }
                                        }
                                        );
    
    
    buttonsPanel.add(closeWindowButton);
    return buttonsPanel;
  }//createButtonsPanel

  /**
   * Calls <code>RGAlgorithm.calculateBiomodules()</code> and updates the plots.
   */
  protected void calculateBiomodules (){
    
    // Calculate the biomodules
    
    HierarchicalClustering hClustering = this.algorithmData.getHierarchicalClustering();
    CyNode [][] biomodules = null;
    if(hClustering == null){
      biomodules = RGAlgorithm.calculateBiomodules(this.algorithmData.getNetwork());
      updatePlots();
    }else{
      biomodules = RGAlgorithm.createBiomodules(this.algorithmData);
    }
    this.algorithmData.setBiomodules(biomodules);
    
    // Visualize them if necessary
    
    String netID = this.algorithmData.getNetwork().getIdentifier();
    CyNetworkView netView = Cytoscape.getNetworkView(netID);
  
    if(this.abstractRbutton.isSelected() && netView != null){
      // Remove existent meta-nodes
      int [] oldMetaNodeRindices = this.algorithmData.getMetaNodeRindices();
      ViewUtils.removeMetaNodes(this.algorithmData.getNetwork(),oldMetaNodeRindices,false);
      // Create new meta-nodes
      int [] metaNodeRindices =  
        ViewUtils.abstractBiomodules(this.algorithmData.getNetwork(),biomodules);
      this.algorithmData.setMetaNodeRindices(metaNodeRindices);
    }
  
  }//calculateBiomodules

  /**
   * Updates the plots.
   */
  protected void updatePlots (){
    HCPlot newBiomodsPlot = createNumBiomodulesPlot();
    HCPlot newDistancesPlot = createDistancesPlot();
    String currentPlotID = getCurrentPlot();
    
    this.plotPanel.removeAll();
    
    if(currentPlotID.equals(BIOMODS_PLOT)){
      this.plotPanel.add(newBiomodsPlot.getContentPane());
    }else if(currentPlotID.equals(DISTANCES_PLOT)){
      this.plotPanel.add(newDistancesPlot.getContentPane());
    }
    
    this.numBiomodulesPlot.removeJoinBarListener(this.numBiomodulesPlotListener);
    this.distancesPlot.removeJoinBarListener(this.distancesPlotListener);
    this.numBiomodulesPlot = newBiomodsPlot;
    this.numBiomodulesPlot.addJoinBarListener(this.numBiomodulesPlotListener);
    this.distancesPlot = newDistancesPlot;
    this.distancesPlot.addJoinBarListener(this.distancesPlotListener);
    this.plotPanel.validate();
    updatePlotLabels();
  }//updatePlots

  /**
   * Updates the yLabel and the yField for the current plot.
   */
  public void updatePlotLabels (){
    
    String currentPlotID = getCurrentPlot();
    
    if(currentPlotID.equals(BIOMODS_PLOT)){
      this.yLabel.setText("Num biomodules:");
    }else if(currentPlotID.equals(DISTANCES_PLOT)){
      this.yLabel.setText("Join distance:");
    }
    
    HierarchicalClustering hClustering = this.algorithmData.getHierarchicalClustering();
    if(hClustering == null){
      return;
    }
    
    int joinNumber = this.algorithmData.getCutJoinNumber();
    
    if(currentPlotID.equals(BIOMODS_PLOT)){
    
      int [] numClustersPerJoin = hClustering.getNumClustersAtIterations();
      if(numClustersPerJoin == null || numClustersPerJoin.length == 0){
        return;
      }
      int numClusters = numClustersPerJoin[joinNumber];
      RGAlgorithmGui.this.yField.setText(Integer.toString(numClusters));
    
    }else if(currentPlotID.equals(DISTANCES_PLOT)){
      
      double [] distances = hClustering.getJoinDistances();
      if(distances == null || distances.length == 0){
        return;
      }
      double distance = distances[joinNumber];
      NumberFormat nf = NumberFormat.getInstance();
      nf.setMaximumFractionDigits(2);
      String text = nf.format(distance);
      RGAlgorithmGui.this.yField.setText(text);
    
    }
  
  }//updatePlotLabels

  /**
   * @return the currently displayed plot identifier (one of BIOMODS_PLOT or DISTANCES_PLOT),
   * or null if neither plot is displayed
   */
  public String getCurrentPlot (){
    Component pane = this.plotPanel.getComponent(0);
    if(pane == this.numBiomodulesPlot.getContentPane()){
      // The biomodules plot is already being displayed
      return BIOMODS_PLOT;
    }
    if(pane == this.distancesPlot.getContentPane()){
      return DISTANCES_PLOT;
    }
    System.err.println("getCurrentPlot() returning null");
    return null;
  }//getCurrentPlot

  /**
   * Pops-up an error dialog with the given message.
   *
   * @param message the message that will be displayed
   */
  protected void showErrorMessageDialog (String message){
    JOptionPane.showMessageDialog(this, 
                                  message, 
                                  "Error", 
                                  JOptionPane.ERROR_MESSAGE);
  }//showErrorMessageDialog

  /**
   * @return a negative number if there was an error:
   * <code>BOUNDS_ERROR</code> if the number entered violates its possible bounds
   * <code>NOT_A_NUM_ERROR</code> if the input is not numeric
   * <code>UNKNOWN_BOUNDS</code> can't asses whether the entered number meets bounds
   * or not (because the object to do this is null)
   * a positive number that represents the input otherwise.
   */
  protected int readInput (JTextField text_field){
    
    String text = text_field.getText();
    int input = -99;
    try{
      input = Integer.parseInt(text);
    }catch(NumberFormatException ex){
      return NOT_A_NUM_ERROR;
    }
    if(input < 0){
      return BOUNDS_ERROR;
    }
    if(text_field == this.minSizeField){
      return input;
    }else if(text_field == this.xField){
      HierarchicalClustering hClustering = this.algorithmData.getHierarchicalClustering();
      if(hClustering == null){
        return UNKNOWN_BOUNDS;
      }
      int numIntNodes = 
        hClustering.getNumInternalNodes(EisenClustering.ROWS);
      if(input < 0 || input > (numIntNodes - 1)){
        return BOUNDS_ERROR;
      }
    }
    return input;
  
  }//readInput

  //------------ Internal classes -----------//
  /**
   * Gets called when the user switches displayed plot using the combo-box.
   */
  protected class PlotsOptionsListener extends AbstractAction {
    PlotsOptionsListener (){
      super();
    }

    public void actionPerformed (ActionEvent event){
      Object source = event.getSource();
      if(!(source instanceof JComboBox)){
        return;
      }
      JComboBox comboBox = (JComboBox)source;
      String option = (String)comboBox.getSelectedItem();
      String currentPlotID = getCurrentPlot();
      if(currentPlotID == null){
        return;
      }
      
      if(option.equals(BIOMODS_PLOT) && !currentPlotID.equals(BIOMODS_PLOT)){
        
        // Show biomodules plot
        RGAlgorithmGui.this.plotPanel.removeAll();
        RGAlgorithmGui.this.plotPanel.add(
                              RGAlgorithmGui.this.numBiomodulesPlot.getContentPane());
        int joinNumber = RGAlgorithmGui.this.distancesPlot.getSelectedJoinNumber();
        if(joinNumber != RGAlgorithmGui.this.numBiomodulesPlot.getSelectedJoinNumber()){
          RGAlgorithmGui.this.numBiomodulesPlot.moveVerticalBarTo(joinNumber);
        }// if the selected join numbers are not the same
        RGAlgorithmGui.this.plotPanel.validate();
        updatePlotLabels();
        return;
      }

      if(option.equals(DISTANCES_PLOT) && !currentPlotID.equals(DISTANCES_PLOT)){
      
        // Show distances plot
        RGAlgorithmGui.this.plotPanel.removeAll();
        RGAlgorithmGui.this.plotPanel.add(RGAlgorithmGui.this.distancesPlot.getContentPane());
        int joinNumber = RGAlgorithmGui.this.numBiomodulesPlot.getSelectedJoinNumber();
        if(joinNumber != RGAlgorithmGui.this.distancesPlot.getSelectedJoinNumber()){
          RGAlgorithmGui.this.distancesPlot.moveVerticalBarTo(joinNumber);
        }// if the selected join numbers are not the same
        RGAlgorithmGui.this.plotPanel.validate();
        updatePlotLabels();
        return;
      }
      
    }//actionPerformed
  
  }//PlotsOptionsListener

  /**
   * Listens to changes in the vertical bar of a HCPlot.
   */
  protected class PlotListener extends AbstractAction{
    PlotListener (){
      super();
    }
    
    public void actionPerformed (ActionEvent event){
      
      HCPlot sourcePlot = (HCPlot)event.getSource();
      int joinNumber = sourcePlot.getSelectedJoinNumber();
      HierarchicalClustering hClustering = 
        RGAlgorithmGui.this.algorithmData.getHierarchicalClustering();
      int numIntNodes;
      if(hClustering != null){
        numIntNodes = 
          hClustering.getNumInternalNodes(EisenClustering.ROWS);
      }else{
        return;
      }
            
      if(joinNumber < 0){
        joinNumber = 0;
      }else if(joinNumber > (numIntNodes - 1) ){
        joinNumber = numIntNodes - 1; // node indeces start at 0, not 1
      }
      RGAlgorithmGui.this.xField.setText(Integer.toString(joinNumber));
      if(hClustering == null){
        return;
      }
      RGAlgorithmGui.this.algorithmData.setCutJoinNumber(joinNumber);
      updatePlotLabels();
    
    }//actionperformed
  
  }//PlotListener
  
  /**
   * Listens to input to the xField.
   */
  protected class XFieldListener extends AbstractAction {
    public XFieldListener (){
      super("");
    }//XFieldListener
    
    public void actionPerformed (ActionEvent event){
      // The user hit the 'Enter' key for the xField (maybe entered new data)
      Object source = event.getSource();
      if(!(source instanceof JTextField)){
        System.err.println("ERROR: source is not a JTextField.");
        return;
      }
      int input = readInput((JTextField)source);
      if(input < 0){
        if(input == BOUNDS_ERROR){
          HierarchicalClustering hClustering = 
            RGAlgorithmGui.this.algorithmData.getHierarchicalClustering();
          if(hClustering != null){
            int numIntNodes = hClustering.getNumInternalNodes(EisenClustering.ROWS);
            showErrorMessageDialog("Enter a number between 0 and " + (numIntNodes -1) + ".");
          }
        }else if (input == UNKNOWN_BOUNDS){
          showErrorMessageDialog("Press \"Calculate\" button.");
        }else if (input == NOT_A_NUM_ERROR){
          showErrorMessageDialog("Please enter a number.");
        }
        return;
      }
      
      // The input is correct, move the vertical bar to the join-number
      RGAlgorithmGui.this.distancesPlot.removeJoinBarListener(
                                         RGAlgorithmGui.this.distancesPlotListener);
      RGAlgorithmGui.this.numBiomodulesPlot.removeJoinBarListener(
                                         RGAlgorithmGui.this.numBiomodulesPlotListener
                                         );
      RGAlgorithmGui.this.distancesPlot.moveVerticalBarTo(input);
      RGAlgorithmGui.this.numBiomodulesPlot.moveVerticalBarTo(input);
      RGAlgorithmGui.this.distancesPlot.addJoinBarListener(
                                     RGAlgorithmGui.this.distancesPlotListener);
      RGAlgorithmGui.this.numBiomodulesPlot.addJoinBarListener(
                                     RGAlgorithmGui.this.numBiomodulesPlotListener
                                     );
      
      RGAlgorithmGui.this.algorithmData.setCutJoinNumber(input);
      updatePlotLabels();
    }//actionPerformed
  
  }//XFieldListener
  
  protected class DisplayTableAction extends AbstractAction {
  	
  	protected int type;
  	
  	DisplayTableAction (int type){
  		this.type = type;
  	}
  	
  	public void actionPerformed (ActionEvent event){
  		if(this.type == APSP_TABLE){ 
  			if(RGAlgorithmGui.this.apspTable == null){
  				ArrayList orderedNodes = RGAlgorithmGui.this.algorithmData.getOrderedNodes();
  				String [] nodeNames = new String[orderedNodes.size()];
  				for(int i = 0; i < nodeNames.length; i++){
  					CyNode node = (CyNode)orderedNodes.get(i);
  					nodeNames[i] = (String)Cytoscape.getNodeAttributeValue(node, Semantics.CANONICAL_NAME);
  				}
  				int [][] apsp = RGAlgorithmGui.this.algorithmData.getAPSP();
  				RGAlgorithmGui.this.apspTable = new DataTable(nodeNames,nodeNames,apsp);
  			}//if apspTable = null
  			RGAlgorithmGui.this.apspTable.pack();
  			RGAlgorithmGui.this.apspTable.setVisible(true);
  		}//apsp table
  	}//actionPerformed
  
  }//DisplayTableAction
  
}//class RGAlgorithmGui
