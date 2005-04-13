package cytoscape.data.attr;

import java.util.Enumeration;

/**
 * This API is one that plugin developers have visibility into - the ability
 * to define, delete, or change a node's [canonical] name is not included in
 * this API.
 */
public interface CyNodeData
{

  public final byte ATTR_TYPE_BOOLEAN = 1;
  public final byte ATTR_TYPE_DOUBLE = 2;
  public final byte ATTR_TYPE_LONG = 3;
  public final byte ATTR_TYPE_STRING = 4;
  public final byte ATTR_TYPE_MULTI = 5;

  /**
   * Defines an attribute domain.  To create multi-attribute domains
   * use defineMultiNodeAttribute().
   * @exception IllegalStateException if attrName specified already defines
   *   an existing attribute domain.
   * @exception IllegalArgumentException if attrType specified is not one of
   *   the ATTR_TYPE_* constants or if it is equal to ATTR_TYPE_MULTI.
   */
  public void defineNodeAttribute(String attrName, byte attrType);

  /**
   * This method defines a multi-attribute domain.  The input variables
   * dimNames and attrTypes must be of the same length -- the first entry
   * in dimNames defines the human-readable name of the first dimension in
   * this multi-attribute, and the first entry in attrTypes defines
   * the primitive attribute type of the first dimension.  And so on, until
   * we get to the last entry in both arrays.  The last entry of dimNames
   * is special because it becomes the name of this attribute domain, and
   * the last entry of attrTypes is special because it defines the type of
   * value stored for this attribute domain.  The way that multi-attributes
   * work is that for any given node, an assigned multi-attribute value
   * is uniquely defined (or missing) as a function of N-1 ordered keys,
   * where N is the dimensionality of the multi-attribute definition (the
   * length of the input arrays).  The N-1 ordered keys are representatives
   * from the first N-1 dimension of this multi-attribute definition.<p>
   * Let's walk through a concete example so that we can better understand
   * how to use multi-attributes to our advantage.  Suppose that our nodes
   * are proteins.  Experiments were performed at two different labs, the
   * Ideker lab and the Salk lab, and these experiments measure all possible
   * locations of these proteins within a cell over some period of time (e.g.,
   * "nucleus", "membrane", "disneyland").
   * For a given experiment (Ideker or Salk) and for a given protein, the
   * experiment results are defined to be a list of all observed locations of
   * that protein.<p>
   * Let's analyze how we would represent this data with multi-attributes.
   * The final values we're interested in are locations, which will be
   * represented as strings.  So our final dimension becomes a string type,
   * whose name shall be "location".  We secretly adopt the policy that
   * the range of values in this third dimension is exactly
   * {"nucleus", "membrane", "disneyland"}.
   * A key into such a set of location values
   * is an experiment.  We have exactly two named experiments, "Ideker" and
   * "Salk".  We will define the first dimension of our multi-attribute
   * definition to be a string type, representing the experiment from which
   * data came.  We will call this first dimension "experiment".  We will
   * secretly adopt the policy that the range of values in this first
   * dimension is exactly {"Ideker", "Salk"}.  Now all
   * that we have not solved is how to represent multiple values -- that is,
   * for a given experiment and a given protein, there can be zero, one, or
   * more locations observed.  In order to represent this, we define our second
   * dimension to be of type integer, and we adopt the policy that an integer
   * in the second dimension stands for the offset into the "array" of
   * multiple locations.  So, the key pair ("Ideker", 0) would uniquely
   * define (if present) the first location oberved by Ideker experiment.  We
   * shall be careful not to assign, for a given protein node, a value for
   * ("Ideker", 0) and ("Ideker", 2), but not ("Ideker", 1), because in
   * "skipping over 1" we are going against our own policy of encoding array
   * offsets in a multi-attribute dimension.
   * @exception IllegalArgumentException if dimNames and attrTypes don't
   *   match in length or if any one of the values in attrTypes is not
   *   one of the four primitive ATTR_TYPE_* types.
   * @exception NullPointerException if any part of the input parameters is
   *   null (one of the arrays or any entry of dimNames).
   * @exception IllegalStateException if dimNames[dimNames.length - 1] already
   *   defines an existing attribute domain.
   */
  public void defineMultiNodeAttribute(String[] dimNames, byte[] attrTypes);

  /**
   * @return an enumeration of java.lang.String, the set of strings returned
   *   is a list of unique node attribute names that are currently defined;
   *   null is never returned.
   */
  public Enumeration definedNodeAttributes();

  public byte nodeAttributeType(String attrName);

  /**
   * The last entry in the returned array is the string attrName (the input
   * parameter).
   */
  public String[] multiNodeAttributeDimensionNames(String attrName);

  public byte[] multiNodeAttributeType(String attrName);

  /**
   * The "un"-define of an attribute node.
   * @exception UnsupportedOperationException if the specified attribute domain
   *   exists but permission to delete it is not granted; for example, the
   *   "nodeName" attribute domain always exists and can never be deleted.
   */
  public void undefineNodeAttribute(String attrName);

  // Actual attribute value set and get methods.

  /**
   * This method enables the notion of "duplicate node".  I've thought about
   * adding finer-grain linking control (for example, linking node1.color ->
   * node2.color or even linking node1.color -> foo, where foo -> blue) but
   * decided that it is too much overhead both for understanding the API and
   * for implementing the API.  The one feature that people ask for
   * consistently is duplication of nodes.<p>
   * When this method is called, all attribute values that have been assigned
   * to fromNode go away.
   * @exception IllegalStateException if a loop of links is detected.
   * @exception IllegalStateException if toNode is linked as well.
   */
  public void linkNode(String fromNode, String toNode);

  /**
   * Returns null if fromNode does not link to another node.
   */
  public String getLinkDestination(String fromNode);

  // Exceptions are thrown if we try to modify attributes on a node which
  // links to another node.

  // Exceptions are thrown if we try to delete a node which has links pointing
  // to it.  This is like freeing memory in C which is still being used.

  /**
   * Returns an non-null [but possibly empty] enumeration of java.lang.String.
   */
  public Enumeration getLinkSources(String toNode);

}
