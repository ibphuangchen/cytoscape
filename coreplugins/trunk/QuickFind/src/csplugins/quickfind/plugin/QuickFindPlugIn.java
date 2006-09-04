package csplugins.quickfind.plugin;

import csplugins.widgets.autocomplete.index.Hit;
import csplugins.widgets.autocomplete.index.TextIndex;
import csplugins.widgets.autocomplete.index.IndexFactory;
import csplugins.widgets.autocomplete.view.ComboBoxFactory;
import csplugins.widgets.autocomplete.view.TextIndexComboBox;
import csplugins.test.quickfind.test.TaskMonitorBase;
import csplugins.quickfind.util.QuickFind;
import csplugins.quickfind.util.QuickFindFactory;
import csplugins.quickfind.util.QuickFindListener;
import csplugins.quickfind.view.QuickFindConfigDialog;
import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.CyNode;
import cytoscape.ding.DingNetworkView;
import cytoscape.plugin.CytoscapePlugin;
import cytoscape.util.CytoscapeToolBar;
import cytoscape.view.CyMenus;
import cytoscape.view.CyNetworkView;
import cytoscape.view.CytoscapeDesktop;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;

import ding.view.DGraphView;
import ding.view.InnerCanvas;
import giny.view.NodeView;

/**
 * Quick Find PlugIn.
 *
 * @author Ethan Cerami.
 */
public class QuickFindPlugIn extends CytoscapePlugin
        implements PropertyChangeListener, QuickFindListener {
    private TextIndexComboBox comboBox;
    private JButton configButton;

    /**
     * Constructor.
     */
    public QuickFindPlugIn() {
        initListeners();
        initToolBar();
    }

    /**
     * Initializes All Cytoscape Listeners.
     */
    private void initListeners() {
        // to catch network creation / destruction events
        Cytoscape.getSwingPropertyChangeSupport().
                addPropertyChangeListener(this);

        // to catch network selection / focus events
        Cytoscape.getDesktop().getNetworkViewManager().
                getSwingPropertyChangeSupport().addPropertyChangeListener(this);

        QuickFind quickFind = QuickFindFactory.getGlobalQuickFindInstance();
        quickFind.addQuickFindListener(this);
    }

    /**
     * Initalizes Tool Bar.
     */
    private void initToolBar() {
        CytoscapeDesktop desktop = Cytoscape.getDesktop();
        CyMenus cyMenus = desktop.getCyMenus();
        CytoscapeToolBar toolBar = cyMenus.getToolBar();

        TextIndex textIndex = IndexFactory.createDefaultTextIndex();
        try {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
            panel.setBorder(new EmptyBorder(0, 0, 0, 0));
            comboBox = ComboBoxFactory.createTextIndexComboBox
                    (textIndex, 1.5);
            comboBox.setEnabled(false);
            ActionListener listener = new UserSelectionListener(comboBox);
            comboBox.addFinalSelectionListener(listener);
            //  Set Size of ComboBox Display, based on # of specific chars
            comboBox.setPrototypeDisplayValue("01234567890");
            comboBox.setToolTipText("Please select or load a network to "
                    + "activate search functionality.");

            URL configIconUrl = QuickFindPlugIn.class.getResource
                    ("resources/config.png");
            ImageIcon configIcon = new ImageIcon(configIconUrl,
                    "Configure search options");
            configButton = new JButton(configIcon);
            configButton.setToolTipText("Configure search options");
            configButton.setEnabled(false);
            configButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    QuickFindConfigDialog dialog = new QuickFindConfigDialog();
                }
            });
            configButton.setBorderPainted(false);

            JLabel label = new JLabel("Search:  ");
            label.setForeground(Color.GRAY);
            panel.add(label);
            panel.add(comboBox);
            panel.add(configButton);
            toolBar.add(panel);
        } catch (Exception e) {
        }
    }

    /**
     * Property change listener - to get network/network view destroy events.
     *
     * @param event PropertyChangeEvent
     */
    public void propertyChange(PropertyChangeEvent event) {
        final QuickFind quickFind =
                QuickFindFactory.getGlobalQuickFindInstance();
        if (event.getPropertyName() != null) {
            if (event.getPropertyName().equals
                    (CytoscapeDesktop.NETWORK_VIEW_CREATED)) {
                final CyNetwork cyNetwork = Cytoscape.getCurrentNetwork();
                //  Run Indexer in separate background daemon thread.
                Thread thread = new Thread() {
                    public void run() {
                        quickFind.addNetwork(cyNetwork, new TaskMonitorBase());
                    }
                };
                thread.start();
            } else if (event.getPropertyName().equals
                    (CytoscapeDesktop.NETWORK_VIEW_DESTROYED)) {
                CyNetworkView networkView = (CyNetworkView) event.getNewValue();
                CyNetwork cyNetwork = networkView.getNetwork();
                quickFind.removeNetwork(cyNetwork);
                swapCurrentNetwork(quickFind);
            } else if (event.getPropertyName().equals
                    (CytoscapeDesktop.NETWORK_VIEW_FOCUSED)) {
                swapCurrentNetwork(quickFind);
            }
        }
    }

    /**
     * Determine which network view now has focus.
     * If no network view has focus, disable quick find.
     */
    private void swapCurrentNetwork(QuickFind quickFind) {
        CyNetworkView networkView = Cytoscape.getCurrentNetworkView();
        CyNetwork cyNetwork;
        boolean networkHasFocus = false;
        if (networkView != null) {
            if (networkView.getNetwork() != null) {
                cyNetwork = networkView.getNetwork();
                TextIndex textIndex = quickFind.getTextIndex(cyNetwork);
                if (textIndex != null) {
                    comboBox.setTextIndex(textIndex);
                    networkHasFocus = true;
                }
            }
        }
        if (!networkHasFocus) {
            disableAllQuickFindButtons();
            comboBox.setToolTipText("Please select or load a network");
        }
    }

    /**
     * Event:  Network Added to Index.
     *
     * @param network CyNetwork Object.
     */
    public void networkAddedToIndex(CyNetwork network) {
        //  No-op
    }

    /**
     * Event:  Network Removed from Index.
     *
     * @param network CyNetwork Object.
     */
    public void networkRemovedfromIndex(CyNetwork network) {
        //  No-op
    }

    /**
     * Indexing started.
     */
    public void indexingStarted() {
        disableAllQuickFindButtons();
        comboBox.setToolTipText("Indexing network.  Please wait...");
    }

    /**
     * Disables all Quick Find Buttons.
     */
    public void disableAllQuickFindButtons() {
        comboBox.removeAllText();
        comboBox.setEnabled(false);
        configButton.setEnabled(false);
    }

    /**
     * Enables all Quick Find Buttons.
     */
    public void enableAllQuickFindButtons() {
        comboBox.setEnabled(true);
        configButton.setEnabled(true);
    }

    /**
     * Indexing ended.
     */
    public void indexingEnded() {
        QuickFind quickFind = QuickFindFactory.getGlobalQuickFindInstance();
        CyNetwork cyNetwork = Cytoscape.getCurrentNetwork();
        TextIndex textIndex = quickFind.getTextIndex(cyNetwork);
        comboBox.setTextIndex(textIndex);
        enableAllQuickFindButtons();
        comboBox.setToolTipText("Enter search string");
    }
}

/**
 * Listens for Final Selection from User.
 *
 * @author Ethan Cerami.
 */
class UserSelectionListener implements ActionListener {
    private TextIndexComboBox comboBox;
    private static final int NODE_SIZE_MULTIPLER = 10;

    /**
     * Constructor.
     *
     * @param comboBox TextIndexComboBox.
     */
    public UserSelectionListener(TextIndexComboBox comboBox) {
        this.comboBox = comboBox;
    }

    /**
     * User has made final selection.
     *
     * @param e ActionEvent Object.
     */
    public void actionPerformed(ActionEvent e) {

        //  Get Current Network
        final CyNetwork currentNetwork = Cytoscape.getCurrentNetwork();

        //  Get Current User Selection
        //  If we have a hit, select matching nodes and fit content.
        Object o = comboBox.getSelectedItem();
        if (o != null && o instanceof Hit) {
            Hit hit = (Hit) comboBox.getSelectedItem();
            currentNetwork.unselectAllNodes();
            currentNetwork.unselectAllEdges();
            final Object graphObjects[] = hit.getAssociatedObjects();

            final ArrayList list = new ArrayList();
            for (int i = 0; i < graphObjects.length; i++) {
                list.add(graphObjects[i]);
            }

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    currentNetwork.setFlaggedNodes(list, true);
                    ((DingNetworkView)
                            Cytoscape.getCurrentNetworkView()).fitSelected();
                    //  If only one node is selected, auto-adjust zoom factor
                    //  so that node does not take up whole screen.
                    if (graphObjects.length == 1) {
                        if (graphObjects[0] instanceof CyNode) {
                            CyNode node = (CyNode) graphObjects[0];

                            //  Obtain dimensions of current InnerCanvas
                            DGraphView graphView = (DGraphView)
                                    Cytoscape.getCurrentNetworkView();
                            InnerCanvas innerCanvas = graphView.getCanvas();

                            NodeView nodeView = Cytoscape.
                                    getCurrentNetworkView().getNodeView(node);

                            double width = nodeView.getWidth()
                                    * NODE_SIZE_MULTIPLER;
                            double height = nodeView.getHeight()
                                    * NODE_SIZE_MULTIPLER;
                            double scaleFactor = Math.min
                                    (innerCanvas.getWidth() / width,
                                    (innerCanvas.getHeight() / height));
                            Cytoscape.getCurrentNetworkView().setZoom
                                    (scaleFactor);
                        }
                    }
                    Cytoscape.getCurrentNetworkView().updateView();
                }
            }
            );
        }
    }
}
