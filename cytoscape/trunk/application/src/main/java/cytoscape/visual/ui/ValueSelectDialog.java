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
package cytoscape.visual.ui;

import static cytoscape.visual.VisualPropertyType.EDGE_SRCARROW_SHAPE;
import static cytoscape.visual.VisualPropertyType.EDGE_TGTARROW_SHAPE;
import static cytoscape.visual.VisualPropertyType.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.border.DropShadowBorder;

import cytoscape.Cytoscape;
import cytoscape.visual.NodeShape;
import cytoscape.visual.VisualPropertyType;
import cytoscape.visual.ui.icon.VisualPropertyIcon;

/**
 * Visual Property selector for discrete values
 * 
 * @author kono
 */
public class ValueSelectDialog extends JDialog {

	private static final long serialVersionUID = 1201804212862509435L;

	// Target visual property.
	private final VisualPropertyType type;

	// Map from actual value to its icon.
	private Map<Object, Icon> iconMap;

	private List orderedKeyList;

	private Object originalValue;

	private boolean canceled = false;

	private static final int WIDTH = 480;
	private final int height;

	private final String listTitle;

	private static final int ICON_CELL_HEIGHT = 140;

	/**
	 * Static method to show dialog and get a value from user.
	 * 
	 * @param type
	 * @param parent
	 * @return
	 */
	public static Object showDialog(final VisualPropertyType type,
			final Window parent) {

		final ValueSelectDialog dialog = new ValueSelectDialog(type, parent,
				true);

		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);
		return dialog.getValue();
	}

	private ValueSelectDialog(VisualPropertyType type, Window parent,
			boolean modal) {
		super(Cytoscape.getDesktop(), modal);
		final Toolkit toolkit = this.getToolkit();
		final Dimension screenSize = toolkit.getScreenSize();
		height = (int) (screenSize.getHeight() * 0.7);

		this.type = type;

		iconMap = this.type.getVisualProperty().getIconSet();
		initComponents();
		setList();

		// Special case handling
		// TODO: Create more user-friendly GUI only for Custom Graphics.
		if (type.equals(NODE_CUSTOM_GRAPHICS_1)) {
			this.iconList.setFixedCellHeight(ICON_CELL_HEIGHT);
			this.listTitle = "Custom Graphics";
		} else {
			this.listTitle = type.getName();
		}

		mainPanel.setTitle(listTitle);

		// get original value and set the selected item.
		if (type.isNodeProp())
			originalValue = Cytoscape.getVisualMappingManager()
					.getVisualStyle().getNodeAppearanceCalculator()
					.getDefaultAppearance().get(type);
		else
			originalValue = Cytoscape.getVisualMappingManager()
					.getVisualStyle().getEdgeAppearanceCalculator()
					.getDefaultAppearance().get(type);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */

	// <editor-fold defaultstate="collapsed" desc=" Generated Code ">
	private void initComponents() {
		mainPanel = new org.jdesktop.swingx.JXTitledPanel();
		iconListScrollPane = new javax.swing.JScrollPane();
		iconList = new JXList(true);

		applyButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Select New Value");

		mainPanel.setTitle(listTitle);
		mainPanel.setTitleFont(new java.awt.Font("SansSerif", 1, 14));

		iconList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		iconListScrollPane.setViewportView(iconList);

		applyButton.setText("Apply");
		applyButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				applyButtonActionPerformed(evt);
			}
		});

		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cancelButtonActionPerformed(evt);
			}
		});
		// Currently not implemented
		cancelButton.setVisible(true);

		org.jdesktop.layout.GroupLayout mainPanelLayout = new org.jdesktop.layout.GroupLayout(
				mainPanel.getContentContainer());
		mainPanel.getContentContainer().setLayout(mainPanelLayout);
		mainPanelLayout
				.setHorizontalGroup(mainPanelLayout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(org.jdesktop.layout.GroupLayout.TRAILING,
								mainPanelLayout
										.createSequentialGroup()
										.addContainerGap(128, Short.MAX_VALUE)
										.add(cancelButton)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(applyButton).addContainerGap())
						.add(iconListScrollPane,
								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
								WIDTH, Short.MAX_VALUE));
		mainPanelLayout
				.setVerticalGroup(mainPanelLayout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(org.jdesktop.layout.GroupLayout.TRAILING,
								mainPanelLayout
										.createSequentialGroup()
										.add(iconListScrollPane,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												height, Short.MAX_VALUE)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(mainPanelLayout
												.createParallelGroup(
														org.jdesktop.layout.GroupLayout.BASELINE)
												.add(applyButton)
												.add(cancelButton))
										.addContainerGap()));

		org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(
				getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(
				org.jdesktop.layout.GroupLayout.LEADING).add(mainPanel,
				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
		layout.setVerticalGroup(layout.createParallelGroup(
				org.jdesktop.layout.GroupLayout.LEADING).add(mainPanel,
				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
		pack();
	} // </editor-fold>

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
		dispose();
		canceled = true;
	}

	private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) {
		dispose();
	}

	// Variables declaration - do not modify
	private javax.swing.JButton applyButton;
	private javax.swing.JButton cancelButton;
	private JXList iconList;
	private javax.swing.JScrollPane iconListScrollPane;
	private org.jdesktop.swingx.JXTitledPanel mainPanel;
	private DefaultListModel model;

	// End of variables declaration
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Object getValue() {

		if (canceled == true) {
			return originalValue;
		}

		final int selectedIndex = iconList.getSelectedIndex();
		if ((0 <= selectedIndex) && (selectedIndex < orderedKeyList.size()))
			return orderedKeyList.get(selectedIndex);
		else
			return originalValue;
	}

	/*
	 * Key SHOULD be enum.
	 */
	private void setList() {
		orderedKeyList = new ArrayList();

		model = new DefaultListModel();
		iconList.setModel(model);

		VisualPropertyIcon icon;

		final Map<String, Icon> name2icon = new HashMap<String, Icon>();
		final Map<String, Object> name2value = new HashMap<String, Object>();

		for (Object key : iconMap.keySet()) {
			icon = (VisualPropertyIcon) iconMap.get(key);

			if (type == EDGE_SRCARROW_SHAPE || type == EDGE_TGTARROW_SHAPE) {
				icon.setIconWidth(icon.getIconWidth() * 3);
			}
			if (type.equals(NODE_SHAPE)
					&& ((NodeShape) key).isSupported() == false) {
				// Filter shapes not supported by current rendering engine.
				// Maybe supported in future versions...
				continue;
			}

			final String keyName = icon.getName();
			if (name2icon.containsKey(keyName)) {
				name2icon.put(keyName + key.hashCode(), icon);
				name2value.put(keyName + key.hashCode(), key);
			} else {
				name2icon.put(keyName, icon);
				name2value.put(keyName, key);
			}
		}

		TreeSet<String> sortedSet = new TreeSet<String>(name2icon.keySet());

		for (String key : sortedSet) {
			orderedKeyList.add(name2value.get(key));
			model.addElement(name2icon.get(key));
		}

		iconList.setCellRenderer(new IconCellRenderer());
		iconList.repaint();
	}

	private static final class IconCellRenderer extends JLabel implements ListCellRenderer {

		private static final long serialVersionUID = -7235212695832080213L;

		private final Font SELECTED_FONT = new Font("SansSerif", Font.ITALIC,
				18);
		private final Font NORMAL_FONT = new Font("SansSerif", Font.BOLD, 14);
		private final Color SELECTED_COLOR = new Color(30, 30, 80, 25);
		private final Color SELECTED_FONT_COLOR = new Color(0, 150, 255, 120);

		private final Border DROPHSADOW = new DropShadowBorder();

		public IconCellRenderer() {
			setOpaque(true);
		}

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			final VisualPropertyIcon icon = (VisualPropertyIcon) value;

			if (value != null)
				setText(icon.getName());

			if (icon != null) {
				setIcon(icon);
				setFont(isSelected ? SELECTED_FONT : NORMAL_FONT);
				icon.setLeftPadding(30);
				setPreferredSize(new Dimension(icon.getIconWidth() + 300,
						icon.getIconHeight() + 20));
			}
			
			this.setVerticalTextPosition(SwingConstants.CENTER);
			this.setVerticalAlignment(SwingConstants.CENTER);
			this.setIconTextGap(60);

			setBackground(isSelected ? SELECTED_COLOR : list.getBackground());
			setForeground(isSelected ? SELECTED_FONT_COLOR : list
					.getForeground());

			this.setBorder(DROPHSADOW);
			return this;
		}
	}
}
