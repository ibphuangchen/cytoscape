/*
 * Created on Jul 5, 2005
 *
 */
package cytoscape.editor.impl;

import cytoscape.editor.CytoscapeEditor;
import cytoscape.editor.CytoscapeEditorFactory;
import cytoscape.editor.CytoscapeEditorManager;
import cytoscape.editor.InvalidEditorException;

import cytoscape.editor.event.NetworkEditEventAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 *
 * builds new instances of editors and network edit event adapters.
 * Before an editor and its network edit event adapter can be built, the editor first needs to be
 * registered with the CytoscapeEditorManager.
 *
 * @author Allan Kuchinsky, Agilent Technologies
 * @version 1.0
 *
 * @see CytoscapeEditorManager
 */
public class CytoscapeEditorFactoryImpl implements CytoscapeEditorFactory {
    private Collection<String> editorTypes = new ArrayList<String>();

    /**
     * mapping of editor types to editors
     */
    private Map<String, CytoscapeEditor> editors = new HashMap<String, CytoscapeEditor>();

    /**
     * get the Cytoscape editor for the specified type
     * @param editorType the type of the editor
     * @param args an arbitrary list of arguments
     * @return the Cytoscape editor for the specified editor type
     * @throws InvalidEditorException
     */
    public CytoscapeEditor getEditor(String editorType, List args)
                              throws InvalidEditorException {
        Class           editorClass;
        CytoscapeEditor cyEditor = null;

        Object cyEditObj = editors.get(editorType);

        if (cyEditObj != null) {
            cyEditor = (CytoscapeEditor) cyEditObj;

            return cyEditor;
        }

        try {
            editorClass = Class.forName("cytoscape.editor.editors." +
                                        editorType);
            editorTypes.add(editorType);
            cyEditor = (CytoscapeEditor) editorClass.newInstance();
            editors.put(editorType, cyEditor);
            cyEditor.setEditorName(editorType);
        } catch (ClassNotFoundException e) {
            String                 msg = "Cannot create editor of type: " +
                                         editorType;
            InvalidEditorException ex = new InvalidEditorException(msg,
                                                                   new Throwable(
                "type not found"));

            //			ex.printStackTrace();
            throw ex;
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }

        CytoscapeEditorManager.setCurrentEditor(cyEditor);

        return cyEditor;
    }

    public CytoscapeEditor getEditor(String editorType)
                              throws InvalidEditorException {
        return getEditor(editorType, null);
    }

    /**
     * Get the set of valid editor types
     *
     * @return non null collection of editor types (String)
     */
    public Collection getEditorTypes() {
        return this.editorTypes;
    }

    // implements CytoscapeEditorFactory interface:
    public Iterator<String> getEditorNames ()
    {
    	return Collections.unmodifiableCollection(this.editorTypes).iterator();
    }

    /**
     * adds a new editorType to the collection of editor types
     *
     * @param editorType
     */
    public void addEditorType(String editorType) {
        editorTypes.add(editorType);
    }

    /**
     * gets an instance of the NetworkEditEventAdaptor associated with the input editor
     * The NetworkEditEventAdapter handles events that are associated with user input to the
     * editor, such as mouse actions, drag/drop, keystrokes.  Each NetworkEditEventAdapter is specialized
     * for the editor that is is associated with.  This is written by the developer and is at the heart of
     * the specialized behaviour of the editor.
     * @param editor
     * @return the NetworkEditEventAdapter that is assigned to the editor
     *
     */
    public NetworkEditEventAdapter getNetworkEditEventAdapter(CytoscapeEditor editor) {
        return editor.getNetworkEditEventAdapter();
    }
}
