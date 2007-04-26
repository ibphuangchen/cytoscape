package cytoscape.visual.ui.editors.continuous;

import cytoscape.Cytoscape;

import cytoscape.data.CyAttributes;

import cytoscape.data.attr.CountedIterator;
import cytoscape.data.attr.MultiHashMap;

import cytoscape.visual.VisualPropertyType;

import cytoscape.visual.calculators.Calculator;

import cytoscape.visual.mappings.ContinuousMapping;
import cytoscape.visual.mappings.continuous.ContinuousMappingPoint;

import org.jdesktop.swingx.JXMultiThumbSlider;
import org.jdesktop.swingx.multislider.Thumb;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * DOCUMENT ME!
 *
 * @author $author$
  */
public abstract class ContinuousMappingEditorPanel extends JDialog {
    protected VisualPropertyType type;
    protected Calculator calculator;
    protected ContinuousMapping mapping;
    protected double maxValue;
    protected double minValue;
    protected double valRange;
    protected ArrayList<ContinuousMappingPoint> allPoints;
    private SpinnerNumberModel spinnerModel;
    protected Object below;
    protected Object above;
    protected static ContinuousMappingEditorPanel editor;

    /** Creates new form ContinuousMapperEditorPanel */
    public ContinuousMappingEditorPanel(VisualPropertyType type) {
        this.type = type;
        initComponents();
        setVisualPropLabel();

        setAttrComboBox();
        setSpinner();
    }

    protected void setSpinner() {
        spinnerModel = new SpinnerNumberModel(0.0d, Float.NEGATIVE_INFINITY,
                Float.POSITIVE_INFINITY, 0.001d);
        spinnerModel.addChangeListener(new SpinnerChangeListener());
        valueSpinner.setModel(spinnerModel);
    }

    protected void setVisualPropLabel() {
        this.visualPropertyLabel.setText("Visual Property: " + type.getName());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */

    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">
    private void initComponents() {
        JPanel mainPanel = new JPanel();

        abovePanel = new BelowAndAbovePanel(type, Color.yellow, false);
        abovePanel.setName("abovePanel");
        belowPanel = new BelowAndAbovePanel(type, Color.white, true);
        belowPanel.setName("belowPanel");

        rangeSettingPanel = new javax.swing.JPanel();
        pivotLabel = new javax.swing.JLabel();
        addButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();

        colorButton = new javax.swing.JButton();
        rangeEditorPanel = new javax.swing.JPanel();
        slider = new org.jdesktop.swingx.JXMultiThumbSlider();
        attrNameLabel = new javax.swing.JLabel();
        iconPanel = new YValueLegendPanel(type);
        visualPropertyLabel = new javax.swing.JLabel();

        valueSpinner = new JSpinner();

        valueSpinner.setEnabled(false);

        rotaryEncoder = new JXMultiThumbSlider();

        iconPanel.setPreferredSize(new Dimension(60, 1));

        mainPanel.setBorder(
            javax.swing.BorderFactory.createTitledBorder(
                null,
                "Continuous Mapping for " + type.getName(),
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new java.awt.Font("SansSerif", Font.BOLD, 12),
                new java.awt.Color(0, 0, 0)));

        rangeSettingPanel.setBorder(
            javax.swing.BorderFactory.createTitledBorder(
                null,
                "Range Setting",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new java.awt.Font("SansSerif", 1, 10),
                new java.awt.Color(0, 0, 0)));
        pivotLabel.setFont(new java.awt.Font("SansSerif", 1, 12));
        pivotLabel.setForeground(java.awt.Color.darkGray);
        pivotLabel.setText("Pivot:");

        addButton.setText("Add");
        addButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        addButton.addActionListener(
            new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    addButtonActionPerformed(evt);
                }
            });

        deleteButton.setText("Delete");
        deleteButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        deleteButton.addActionListener(
            new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    deleteButtonActionPerformed(evt);
                }
            });

        rangeEditorPanel.setBorder(
            javax.swing.BorderFactory.createTitledBorder(
                null,
                "Range Editor",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new java.awt.Font("SansSerif", 1, 10),
                new java.awt.Color(0, 0, 0)));
        slider.setMaximumValue(100.0F);
        rotaryEncoder.setMaximumValue(100.0F);

        org.jdesktop.layout.GroupLayout sliderLayout = new org.jdesktop.layout.GroupLayout(slider);
        slider.setLayout(sliderLayout);
        sliderLayout.setHorizontalGroup(
            sliderLayout.createParallelGroup(
                org.jdesktop.layout.GroupLayout.LEADING).add(0, 486,
                Short.MAX_VALUE));
        sliderLayout.setVerticalGroup(
            sliderLayout.createParallelGroup(
                org.jdesktop.layout.GroupLayout.LEADING).add(0, 116,
                Short.MAX_VALUE));

        attrNameLabel.setFont(new java.awt.Font("SansSerif", 1, 14));
        attrNameLabel.setForeground(java.awt.Color.darkGray);
        attrNameLabel.setText("Attribute Name");

        //        org.jdesktop.layout.GroupLayout iconPanelLayout = new org.jdesktop.layout.GroupLayout(iconPanel);
        //        iconPanel.setLayout(iconPanelLayout);
        //        iconPanelLayout.setHorizontalGroup(
        //            iconPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
        //            .add(org.jdesktop.layout.GroupLayout.TRAILING, rotaryEncoder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)
        //        );
        //        iconPanelLayout.setVerticalGroup(
        //            iconPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
        //            .add(org.jdesktop.layout.GroupLayout.TRAILING, rotaryEncoder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
        //        );
        org.jdesktop.layout.GroupLayout jXMultiThumbSlider1Layout = new org.jdesktop.layout.GroupLayout(rotaryEncoder);
        rotaryEncoder.setLayout(jXMultiThumbSlider1Layout);
        jXMultiThumbSlider1Layout.setHorizontalGroup(
            jXMultiThumbSlider1Layout.createParallelGroup(
                org.jdesktop.layout.GroupLayout.LEADING).add(0, 84,
                Short.MAX_VALUE));
        jXMultiThumbSlider1Layout.setVerticalGroup(
            jXMultiThumbSlider1Layout.createParallelGroup(
                org.jdesktop.layout.GroupLayout.LEADING).add(0, 65,
                Short.MAX_VALUE));

        visualPropertyLabel.setFont(new java.awt.Font("SansSerif", 1, 14));
        visualPropertyLabel.setForeground(java.awt.Color.darkGray);

        org.jdesktop.layout.GroupLayout rangeSettingPanelLayout = new org.jdesktop.layout.GroupLayout(rangeSettingPanel);
        rangeSettingPanel.setLayout(rangeSettingPanelLayout);
        rangeSettingPanelLayout.setHorizontalGroup(
            rangeSettingPanelLayout.createParallelGroup(
                org.jdesktop.layout.GroupLayout.LEADING).add(
                rangeSettingPanelLayout.createSequentialGroup().addContainerGap().add(valueSpinner,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 67,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED,
                    118, Short.MAX_VALUE).add(addButton,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(deleteButton).add(10,
                    10, 10)));
        rangeSettingPanelLayout.setVerticalGroup(
            rangeSettingPanelLayout.createParallelGroup(
                org.jdesktop.layout.GroupLayout.LEADING).add(
                rangeSettingPanelLayout.createParallelGroup(
                    org.jdesktop.layout.GroupLayout.BASELINE).add(valueSpinner,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(deleteButton).add(addButton,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(mainPanel);
        mainPanel.setLayout(layout);

        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(rangeSettingPanel,
                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(
                layout.createSequentialGroup().add(iconPanel,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(belowPanel,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(slider,
                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 243,
                    Short.MAX_VALUE).add(abovePanel,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                layout.createSequentialGroup().add(
                    layout.createParallelGroup(
                        org.jdesktop.layout.GroupLayout.LEADING).add(org.jdesktop.layout.GroupLayout.TRAILING,
                        slider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        145, Short.MAX_VALUE).add(org.jdesktop.layout.GroupLayout.TRAILING,
                        iconPanel,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        Short.MAX_VALUE).add(org.jdesktop.layout.GroupLayout.TRAILING,
                        belowPanel,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        Short.MAX_VALUE).add(org.jdesktop.layout.GroupLayout.TRAILING,
                        abovePanel,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        Short.MAX_VALUE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(rangeSettingPanel,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));

        //        layout.setHorizontalGroup(
        //            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
        //                layout.createSequentialGroup().add(iconPanel,
        //                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
        //                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
        //                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(slider,
        //                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 269,
        //                    Short.MAX_VALUE).addContainerGap()).add(rangeSettingPanel,
        //                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
        //                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        //        layout.setVerticalGroup(
        //            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
        //                layout.createSequentialGroup().add(
        //                    layout.createParallelGroup(
        //                        org.jdesktop.layout.GroupLayout.TRAILING).add(slider,
        //                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 145,
        //                        Short.MAX_VALUE).add(iconPanel,
        //                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
        //                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
        //                        Short.MAX_VALUE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(rangeSettingPanel,
        //                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
        //                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
        //                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));

        // add the main panel to the dialog.
        this.getContentPane()
            .add(mainPanel);
        this.pack();
    } // </editor-fold>               

    abstract protected void deleteButtonActionPerformed(
        java.awt.event.ActionEvent evt);

    abstract protected void addButtonActionPerformed(
        java.awt.event.ActionEvent evt);

    private void setAttrComboBox() {
        final CyAttributes attr;

        if (type.isNodeProp()) {
            attr = Cytoscape.getNodeAttributes();
            calculator = Cytoscape.getVisualMappingManager()
                                  .getVisualStyle()
                                  .getNodeAppearanceCalculator()
                                  .getCalculator(type);
        } else {
            attr = Cytoscape.getEdgeAttributes();
            calculator = Cytoscape.getVisualMappingManager()
                                  .getVisualStyle()
                                  .getEdgeAppearanceCalculator()
                                  .getCalculator(type);
        }

        if (calculator == null)
            return;

        final String[] names = attr.getAttributeNames();

        byte attrType;

        for (String name : names) {
            attrType = attr.getType(name);

            //            if ((attrType == CyAttributes.TYPE_FLOATING) ||
            //                    (attrType == CyAttributes.TYPE_INTEGER))
            //               
        }

        // Assume this calc only returns cont. mapping.
        if (calculator.getMapping(0)
                          .getClass() == ContinuousMapping.class) {
            mapping = (ContinuousMapping) calculator.getMapping(0);

            final String controllingAttrName = mapping.getControllingAttributeName();

            final MultiHashMap mhm = attr.getMultiHashMap();

            final CountedIterator it = mhm.getObjectKeys(controllingAttrName);
            Object key;
            maxValue = Double.NEGATIVE_INFINITY;
            minValue = Double.POSITIVE_INFINITY;

            while (it.hasNext()) {
                key = it.next();

                Double val = Double.parseDouble(
                        mhm.getAttributeValue((String) key,
                            controllingAttrName, null).toString());

                if (val > maxValue)
                    maxValue = val;

                if (val < minValue)
                    minValue = val;

                //System.out.println("************ MHM OBJ= " + val);
            }

            System.out.println("----------- min max = " + minValue + ", " +
                maxValue);
            valRange = Math.abs(minValue - maxValue);
            allPoints = mapping.getAllPoints();
        }
    }

    protected void setSidePanelIconColor(Color below, Color above) {
        this.abovePanel.setColor(above);
        this.belowPanel.setColor(below);
        repaint();
    }

    // Variables declaration - do not modify
    protected javax.swing.JButton addButton;
    private javax.swing.JLabel attrNameLabel;

    //    private javax.swing.JComboBox attributeComboBox;
    protected javax.swing.JButton colorButton;
    protected javax.swing.JButton deleteButton;
    protected javax.swing.JPanel iconPanel;
    private javax.swing.JLabel pivotLabel;
    private javax.swing.JPanel rangeEditorPanel;
    private javax.swing.JPanel rangeSettingPanel;
    protected org.jdesktop.swingx.JXMultiThumbSlider slider;
    protected javax.swing.JSpinner valueSpinner;
    private javax.swing.JLabel visualPropertyLabel;
    protected JXMultiThumbSlider rotaryEncoder;

    /*
     * For Gradient panel only.
     */
    protected BelowAndAbovePanel abovePanel;
    protected BelowAndAbovePanel belowPanel;

    protected int getSelectedPoint(int selectedIndex) {
        final List<Thumb> thumbs = slider.getModel()
                                         .getSortedThumbs();
        Thumb selected = slider.getModel()
                               .getThumbAt(selectedIndex);
        int i;

        for (i = 0; i < thumbs.size(); i++) {
            if (thumbs.get(i) == selected) {
                System.out.println("=====Selected Color = " + i + ", " +
                    thumbs.get(i).getObject());

                return i;
            }
        }

        return -1;
    }

    protected void updateMap() {
        List<Thumb> thumbs = slider.getModel()
                                   .getSortedThumbs();

        //List<ContinuousMappingPoint> points = mapping.getAllPoints();
        Thumb t;
        Double newVal;

        System.out.println("Range = " + valRange + ", minVal = " + minValue);

        if(thumbs.size() == 1) {
        	System.out.println("Enter update code: " + mapping.getPointCount());
        	mapping.getPoint(0).getRange().equalValue = thumbs.get(0).getObject();
        	mapping.getPoint(0).getRange().lesserValue = below;
        	mapping.getPoint(0).getRange().greaterValue = above;
        	newVal = ((thumbs.get(0).getPosition() / 100) * valRange) + minValue;
        	mapping.getPoint(0)
            .setValue(newVal);
        	return;
        }
        
        for (int i = 0; i < thumbs.size(); i++) {
            t = thumbs.get(i);

            if (i == 0) {
                mapping.getPoint(i)
                       .getRange().lesserValue = below;

                mapping.getPoint(i)
                       .getRange().greaterValue = t.getObject();
            } else if (i == (thumbs.size() - 1)) {
                mapping.getPoint(i)
                       .getRange().greaterValue = above;

                mapping.getPoint(i)
                       .getRange().lesserValue = t.getObject();
            } else {
                mapping.getPoint(i)
                       .getRange().lesserValue = t.getObject();

                mapping.getPoint(i)
                       .getRange().greaterValue = t.getObject();
            }

            newVal = ((t.getPosition() / 100) * valRange) + minValue;
            mapping.getPoint(i)
                   .setValue(newVal);

            mapping.getPoint(i)
                   .getRange().equalValue = t.getObject();
//            System.out.println("Selected idx = " + selectedIndex +
//                ", new val = " + newVal + ", New obj = " + t.getObject() +
//                ", Pos = " + t.getPosition());
        }
    }

    // End of variables declaration
    protected class ThumbMouseListener extends MouseAdapter {
        //        public void mousePressed(MouseEvent e) {
        //            int selectedIndex = slider.getSelectedIndex();
        //
        //            if ((0 <= selectedIndex) &&
        //                    (slider.getModel()
        //                               .getThumbCount() > 1)) {
        //            	
        //            	Thumb t = slider.getModel().getThumbAt(selectedIndex);
        //                valueSpinner.setEnabled(true);
        //                valueSpinner.setValue(
        //                    ((t.getPosition() / 100) * valRange) + minValue);
        //            } else {
        //                valueSpinner.setEnabled(false);
        //                valueSpinner.setValue(0);
        //            }
        //        }
        public void mouseReleased(MouseEvent e) {
            int selectedIndex = slider.getSelectedIndex();

            System.out.println("T Count = " + slider.getModel().getThumbCount());
            
            if ((0 <= selectedIndex) &&
                    (slider.getModel()
                               .getThumbCount() > 0)) {
                Point location = slider.getSelectedThumb()
                                       .getLocation();
                valueSpinner.setEnabled(true);

                //                Double newVal = ((VizMapperTrackRenderer) slider.getTrackRenderer()).getSelectedThumbValue();
                Double newVal = ((slider.getModel()
                                        .getThumbAt(selectedIndex)
                                        .getPosition() / 100) * valRange) +
                    minValue;
                valueSpinner.setValue(newVal);

                
                updateMap();

                slider.repaint();
                repaint();

                System.out.println("\n\n");
                //mapping.getPoint(getSelectedPoint(selectedIndex)).setValue(newVal);
                Cytoscape.getVisualMappingManager()
                         .getNetworkView()
                         .redrawGraph(false, true);
            } else {
                valueSpinner.setEnabled(false);
                valueSpinner.setValue(0);
            }
        }
    }

    /**
     * Watching spinner
     *
     * @author kono
     *
     */
    class SpinnerChangeListener
        implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            Number newVal = spinnerModel.getNumber();
            int selectedIndex = slider.getSelectedIndex();

            if ((0 <= selectedIndex) &&
                    (slider.getModel()
                               .getThumbCount() > 1)) {
                Double newPosition = ((newVal.floatValue() - minValue) / valRange);

                slider.getModel()
                      .getThumbAt(selectedIndex)
                      .setPosition(newPosition.floatValue() * 100);
                slider.getSelectedThumb()
                      .setLocation((int) ((slider.getSize().width - 12) * newPosition),
                    0);
                
                
                updateMap();
                Cytoscape.getVisualMappingManager()
                .getNetworkView()
                .redrawGraph(false, true);
                
                slider.getSelectedThumb()
                      .repaint();
                slider.getParent()
                      .repaint();
                slider.repaint();

                /*
                 * Set continuous mapper value
                 */
            }
        }
    }
}
