package filter.view;

import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import filter.model.*;
import javax.swing.border.*;
import java.beans.*;
import javax.swing.event.SwingPropertyChangeSupport;

import ViolinStrings.Strings;

/**
 * Provides a tabbed Interface for creating filters of all available 
 * filter editors that have been provided.
 */
public class FilterEditorPanel 
  extends JPanel 
  implements PropertyChangeListener,
             ActionListener {

  JTabbedPane editorTabs;
  Map editorIndexMap;
  int editorCount;
  protected JButton addButton, removeButton, resetButton;

  public FilterEditorPanel () {
    super();
    initialize();

  }

  public void initialize() {

  
    editorIndexMap = new HashMap();
    editorCount = 0;

   //  JPanel editorPanel = new JPanel();
//     editorPanel.setPreferredSize( new Dimension( 500, 100 ) );
//     editorPanel.setBorder( new BevelBorder( BevelBorder.RAISED ) );

    JPanel controlPanel = new JPanel();
    controlPanel.setBorder( new TitledBorder( "Filter Control" ) );
    controlPanel.setLayout( new GridLayout( 0, 1 ) );

   //  DefaultFilterEditor dfe = new DefaultFilterEditor();
//     dfe.setPreferredSize( new Dimension( 500, 100 ) );
    editorTabs = new JTabbedPane();
//     editorTabs.addTab( dfe.toString(), dfe );

    //editorPanel.add( editorTabs );


    addButton = new JButton( "Add" );
    removeButton = new JButton( "Remove" );
    resetButton = new JButton( "Reset" );
    addButton.addActionListener( this );
    removeButton.addActionListener( this );
    resetButton.addActionListener( this );
    controlPanel.add( addButton );
    controlPanel.add( removeButton );
    controlPanel.add( resetButton );

    add( editorTabs );
    add( controlPanel );

    FilterManager.defaultManager().getSwingPropertyChangeSupport().addPropertyChangeListener( this );

  }
  
  public void addEditor ( FilterEditor new_editor ) {
    editorIndexMap.put( new_editor.toString() , new Integer( editorCount ) );
    editorTabs.insertTab( new_editor.toString(), null, new_editor, new_editor.toString(), editorCount );
    editorCount++;
  }


  public void setEditorActive ( String name ) {
    int index = ( ( Integer )editorIndexMap.get( name ) ).intValue();
    editorTabs.setSelectedIndex( index );
  }
  
  public FilterEditor getSelectedEditor () {
    return ( FilterEditor )editorTabs.getSelectedComponent();
  }

//   protected void addEditor ( FilterEditor fe ) {
//     Set editors = FilterManager.defaultManager().getEditors();
    
//   }

  public void propertyChange ( PropertyChangeEvent e ) {
    // get notified when new Filter is up for Editing
    //System.out.println( "PCS:::: "+e.getPropertyName() );
    if ( e.getPropertyName() == FilterManager.EDITOR_ADDED ) {
      //System.out.println( "FilterEditorPanel received EDITOR_ADDED Event" );
      addEditor( ( FilterEditor )e.getNewValue() );
    } else if ( e.getPropertyName() == FilterListPanel.FILTER_SELECTED ) {
      Filter f = FilterManager.defaultManager().getFilter( ( String )e.getNewValue() );
      if ( f == null ) {
        return;
      }
      setEditorActive( f.getEditorName() );
      FilterEditor fe = ( FilterEditor )editorTabs.getSelectedComponent();
      //FilterEditor fe = FilterManager.defaultManager().getEditor( f.getEditorName() );
      fe.editFilter( f );
      // System.out.println( "Editing Filter: "+f+" with editor:"+fe );
      //  System.out.println( "FilterEditor is editing: "+fe.getFilter() );
      //fe.editFilter( f );
      //editorTabs.setSelectedComponent( fe );
      //setEditorActive

    }
   
  } 

  public void actionPerformed ( ActionEvent e ) {
    if ( e.getSource() == addButton ) {
      //System.out.println( "Adding Filter from selected editor: "+getSelectedEditor() );
      FilterManager.defaultManager().addFilter( getSelectedEditor().getFilter() );
    }
    if ( e.getSource() == removeButton ) {
      FilterManager.defaultManager().removeFilter( getSelectedEditor().getFilter() );
      getSelectedEditor().clear();
    }
     if ( e.getSource() == resetButton ) {
      FilterManager.defaultManager().removeFilter( getSelectedEditor().getFilter() );
      getSelectedEditor().reset();
    }
  }



}
    

