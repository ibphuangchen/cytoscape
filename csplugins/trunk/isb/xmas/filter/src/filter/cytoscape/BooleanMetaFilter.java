package filter.cytoscape;

import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import filter.model.*;
import javax.swing.border.*;
import java.beans.*;
import javax.swing.event.SwingPropertyChangeSupport;

import cytoscape.*;
import cytoscape.data.*;
import cytoscape.CyNetwork;
import giny.model.*;

import ViolinStrings.Strings;

/**
 * This is a Cytoscape specific filter that will pass nodes if
 * a selected attribute matches a specific value.
 */
public class BooleanMetaFilter
  implements Filter  {
  
  //----------------------------------------//
  // Filter specific properties 
  //----------------------------------------//
		protected Object [] filters;
		protected String comparison;
		public static String AND = "ALL";
		public static String OR = "AT LEAST ONE";
		public static String XOR = "ONLY ONE";
		
  public static String FILTER_NAME_EVENT = "FILTER_NAME_EVENT";
  public static String FILTER_ID = "BooleanMetaFilter";
		public static String COMPARISON_EVENT = "COMPARISON_EVENT";
		public static String FILTER_EVENT = "FILTER_EVENT";	
	 public static String FILTER_BOX_EVENT = "FILTER_BOX";	

  //----------------------------------------//
  // Needed Variables
  //----------------------------------------//
  protected String identifier = "default";
  protected SwingPropertyChangeSupport pcs = new SwingPropertyChangeSupport(this);
  
  
  //---------------------------------------//
  // Constructor
  //----------------------------------------//

  /**
   * Creates a new BooleanMetaFilter
   */  
  public BooleanMetaFilter (Object [] filters, 
                            String comparison, String identifier ) {
    
    if ( filters.length > 0 && filters[0] instanceof String ) {
      // read from file, get the Filters
      Object[] actual_filters = new Object[filters.length];
      for ( int i = 0; i < filters.length; ++i ) {
        actual_filters[i] = FilterManager.defaultManager().getFilter( ( String )filters[i] );
      }
      this.filters = actual_filters;
    } else
      this.filters = filters;

    this.comparison = comparison;
    this.identifier =identifier;
  }
  
  /**
   * Creates a new BooleanMetaFilter
   */  
  public BooleanMetaFilter ( String filter_strings, 
                             String comparison, 
                             String identifier ) {
    

    String[] filters = filter_strings.split(":");
    Object[] actual_filters = new Object[filters.length];
    for ( int i = 0; i < filters.length; ++i ) {
      actual_filters[i] = FilterManager.defaultManager().getFilter( filters[i] );
    }
     
    this.filters = actual_filters;

    this.comparison = comparison;
    this.identifier =identifier;
  }
  
 
  
  //----------------------------------------//
  // Implements Filter
  //----------------------------------------//

  /**
   * Returns the name for this Filter
   */
  public String toString () {
    return identifier;
  }

  /**
   * sets a new name for this filter
   */
  public void setIdentifier ( String new_id ) {
    FilterManager.defaultManager().renameFilter( identifier, new_id );
    this.identifier = new_id;
  }

  /**
   * This is usually the same as the class name
   */
  public String getFilterID () {
    return FILTER_ID;
  }

  /**
   * An Object Passes this Filter if its "toString" method
   * matches any of the Text from the TextField
   */
  public boolean passesFilter ( Object object ) {
				if(filters.length == 0){
								return false;
				}
				int count = 0;
				for(int idx=0;idx<filters.length;idx++){
          //System.out.println(""+filters[idx]);
								boolean filterResult = ((Filter)(filters[idx])).passesFilter(object);
								if(comparison == AND && !filterResult){
												return false;
								}
								if(comparison == OR && filterResult){
												return true;
								}
								if(comparison == XOR && filterResult){
												if(++count>1){
																return false;
												}
								}
				}
				if(comparison == XOR && count == 1){
								return true;
				}
				if(comparison == AND){
								return true;
				}
				else{
								return false;
				}
		}

  public Class[] getPassingTypes () {
    return null;
  }
  
  public boolean equals ( Object other_object ) {
    //if ( other_object instanceof BooleanMetaFilter ) {
    //  if ( ( ( BooleanMetaFilter )other_object).getSearchNumber().equals( getSearchNumber() ) ) {
    //    return true;
    //  }
    //}
    //return false;
				return super.equals(other_object);
  }
  
  public Object clone () {
    return new BooleanMetaFilter ( filters, comparison,identifier+"_new" );
  }
  
  public SwingPropertyChangeSupport getSwingPropertyChangeSupport() {
    return pcs;
  }

  //----------------------------------------//
  // BooleanMetaFilter methods
  //----------------------------------------//

  public void propertyChange ( PropertyChangeEvent e ) {
    if ( e.getPropertyName() == FILTER_NAME_EVENT ) {
								setIdentifier( ( String )e.getNewValue() );
    } else if (e.getPropertyName() == FILTER_BOX_EVENT)  {
								setFilters((Object [])e.getNewValue());
				} else if (e.getPropertyName() == COMPARISON_EVENT){
								setComparison((String)e.getNewValue());
				}
  }
  

		public void setComparison(String comparison){
						this.comparison = comparison;
						pcs.firePropertyChange(COMPARISON_EVENT,null,comparison);	
		}

		public String getComparison(){
						return comparison;
		}

		public void setFilters(Object [] filters){
						this.filters = filters;
						pcs.firePropertyChange(FILTER_BOX_EVENT,null,filters);
		}
		public Object [] getFilters(){
						return filters;
		}
		
  
  


  //----------------------------------------//
  // IO
  //----------------------------------------//

  public String output () {
    StringBuffer buffer = new StringBuffer();
    buffer.append( "filter.cytoscape.BooleanMetaFilter,");
    for ( int i = 0; i < filters.length; ++i ) {
      buffer.append(filters[i].toString());
      if ( i != filters.length - 1 ) 
        buffer.append(":");
    }
    buffer.append( ","+getComparison()+"," );
    buffer.append( toString() );
    return buffer.toString();
  }
  
  public Filter input ( String desc ) {
    return null;
  }

}

