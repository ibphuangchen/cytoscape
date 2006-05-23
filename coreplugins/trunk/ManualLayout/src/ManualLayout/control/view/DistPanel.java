package ManualLayout.control.view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import ManualLayout.control.actions.dist.*;
import cytoscape.view.*;

public class DistPanel extends JPanel {

  public DistPanel ( ) {

    ImageIcon hari =  new ImageIcon( getClass().getResource("/H_DIST_RIGHT.gif") );
    ImageIcon haci =  new ImageIcon( getClass().getResource("/H_DIST_CENTER.gif") );
    ImageIcon hali =  new ImageIcon( getClass().getResource("/H_DIST_LEFT.gif") );
    ImageIcon vati =  new ImageIcon( getClass().getResource("/V_DIST_TOP.gif") );
    ImageIcon vaci =  new ImageIcon( getClass().getResource("/V_DIST_CENTER.gif") );
    ImageIcon vabi =  new ImageIcon( getClass().getResource("/V_DIST_BOTTOM.gif") );

    HDist har = new HDist( hari );
    HDist hac = new HDist( haci );
    HDist hal = new HDist( hali );

    VDist vat = new VDist( vati );
    VDist vac = new VDist( vaci );
    VDist vab = new VDist( vabi );

    JPanel h_panel = new JPanel();
    h_panel.add( createJButton( har, "Horizontal Right") );
    h_panel.add( createJButton( hac, "Horizontal Center") );
    h_panel.add( createJButton( hal, "Horizontal Left") );
    JPanel v_panel = new JPanel();
    v_panel.add( createJButton( vat, "Vertical Top") );
    v_panel.add( createJButton( vac, "Vertical Center") );
    v_panel.add( createJButton( vab, "Vertical Bottom") );

    setLayout( new BorderLayout() );
    add( h_panel, BorderLayout.EAST );
    add( v_panel, BorderLayout.WEST );

    setBorder( new TitledBorder( "Distribute" ) );

  }

  protected JButton createJButton ( Action a, String tt ) {
    JButton b = new JButton( a );
    b.setToolTipText( tt );
    b.setPreferredSize( new Dimension( 27, 18 ) );
    return b;
  }


}
    

