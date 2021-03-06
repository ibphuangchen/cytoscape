//AttributeMapper.java
//----------------------------------------------------------------------------
// $Revision$
// $Date$
// $Author$
//----------------------------------------------------------------------------
package cytoscape.vizmap;
//----------------------------------------------------------------------------
import java.util.Map;
import java.util.HashMap;
//----------------------------------------------------------------------------
/**
 * This class stores, for a set of range attributes, the name of the
 * domain attribute and the ValueMapper object to use to compute a
 * value for the range attribute. Given a Map of domain attribute names
 * and values, it uses these structures to get the domain value and
 * pass it to the correct ValueMapper object.
 *
 * This class maintains a Map of default values for the range attributes
 * in case a domain value or mapper is unavailable. Additionally, one
 * can specify that default values should always be used by calling the
 * setUseDefaultsOnly method with an argument of "true".
 */
public class AttributeMapper {

    protected Map rangeToDomainName;
    protected Map rangeToValueMapper;
    private Map defaultRangeValues;
    private boolean useDefaultsOnly;


    public AttributeMapper(Map defaultValues) {
	rangeToDomainName = new HashMap();
	rangeToValueMapper = new HashMap();
	this.setDefaultValues(defaultValues);
	this.setUseDefaultsOnly(false);
    }

    //-------------------------------------------------------------------

    /**
     * Associates the given name of a domain attribute and the supplied
     * ValueMapper object with the range attribute identified by the
     * first argument. If any of the arguments is null, does nothing.
     */
    public void setAttributeMapEntry(Integer rangeAttribute,
				     String domainAttributeName,
				     ValueMapper mapper) {
	if (rangeAttribute != null && domainAttributeName != null
	    && mapper != null) {
	    rangeToDomainName.put(rangeAttribute,domainAttributeName);
	    rangeToValueMapper.put(rangeAttribute,mapper);
	}
    }

    /** removes an attribute map entry */
    public void removeAttributeMapEntry(Integer rangeAttribute) {
	if (rangeAttribute != null) {
	    rangeToDomainName.remove(rangeAttribute);
	    rangeToValueMapper.remove(rangeAttribute);
	}
    }

    public String getControllingDomainAttributeName(Integer rangeAttribute) {
	return (String)rangeToDomainName.get(rangeAttribute);
    }

    public ValueMapper getValueMapper(Integer rangeAttribute) {
	return (ValueMapper)rangeToValueMapper.get(rangeAttribute);
    }

    //-------------------------------------------------------------------

    /**
     * Given a Map of attribute names to values and the desired range
     * attribute specified by the second argument, returns a matching
     * range value.
     */
    public Object getRangeValue(Map attrBundle, Integer rangeAttribute) {
	/* get the default value for this rangeAttribute */
	Object defaultVal = this.getDefaultValue(rangeAttribute);
	if ( useDefaultsOnly || (attrBundle == null)
	     || rangeAttribute == null) {
	    return defaultVal;
	}

	/* extract name of the domain attribute controlling this
	   rangeAttribute */
	Object attrKey = rangeToDomainName.get(rangeAttribute);
	if (attrKey == null) {return defaultVal;}

	/* get the domain attribute value from the supplied Map */
	Object attr = attrBundle.get(attrKey);
	if (attr == null) {return defaultVal;}

	/* get the object that maps domain values to range values for this
	   rangeAttribute */
	ValueMapper mapper =
	    (ValueMapper)rangeToValueMapper.get(rangeAttribute);
	if (mapper == null) {return defaultVal;}

	/* get the range value appropriate to this domain value */
	Object mVal = mapper.getRangeValue(attr);
	if (mVal == null) {return defaultVal;} else {return mVal;}
    }

    //------------------------------------------------------------------

    public Object getDefaultValue(Integer rangeAttribute) {
	return defaultRangeValues.get(rangeAttribute);
    }
    public Object setDefaultValue(Integer rangeAttribute, Object o) {
	if (rangeAttribute == null) {
	    return null;
	} else {
	    //caller is responsible for ensuring proper type of argument
	    return defaultRangeValues.put(rangeAttribute,o);
	}
    }

    public Map getDefaultValues() {return defaultRangeValues;}
    public void setDefaultValues(Map defaults) {
	if (defaults == null) {
	    defaultRangeValues = new HashMap();
	} else {
	    defaultRangeValues = defaults;
	}
    }

    public boolean getUseDefaultsOnly() {return useDefaultsOnly;}
    public void setUseDefaultsOnly(boolean flag) {
	useDefaultsOnly = flag;
    }
}
