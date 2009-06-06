package org.jogre.common;

import nanoxml.XMLElement;

/**
 * Utility class, used to concatenate a set of xml-elements (given as strings)
 * into a single entity that can be parsed back on the receiving side.
 * The final result can be retrieved by using the toString() method.
 * This object is not threadsafe!
 * 
 * @author Jens Scheffler
 *
 */
public class PayloadBuilder {
  
  /**
   * Name of the (optional) meta-attribute of the outer xml tag
   */
  public static final String META = "meta";
  
  /**
   * Name of the outer tag (payload)
   */
  public static final String TAG = "payload";
  
  private final StringBuilder root = new StringBuilder();
  private int size = 0;
  
  /**
   * Constructor
   * @param meta an arbitrary string with meta-information that should be included in the outer tag
   */
  public PayloadBuilder(String meta) {
    reset(meta);
  }
  
  /**
   * Resets the builder, so that it can be reused
   */
  public PayloadBuilder reset(String newMeta) {
    root.delete(0, root.length());
    XMLElement elem = new XMLElement(TAG);
    if (newMeta != null) {
      elem.setAttribute(META, newMeta);
    }    
    root.append(elem.toString());
    root.deleteCharAt(root.length() - 2);
    size = 0;
    return this;
  }

  /**
   * Creates a new builder.
   * @param meta an arbitrary string with meta-information that should be included in the outer tag
   */
  public static PayloadBuilder payload(String meta) {
    return new PayloadBuilder(meta);
  }
  
  /**
   * Adds an xml node, but does not validate if the xml is valid (faster)
   * @return this builder
   */
  public PayloadBuilder addChildNoparse(String xmlData) {
    root.append(xmlData);
    size++;
    return this;
  }
  
  /**
   * Adds a valid xml node
   * @return this builder
   * @throws nanoxml.XMLParseException
   *     If an error occured while parsing the string.
   */
  public PayloadBuilder addChild(String xmlData) {
    XMLElement elem = new XMLElement();
    elem.parseString(xmlData);
    return addChildNoparse(elem.toString());
  }
  
  /**
   * Returns the amount of elements the builder currently contains.
   */
  public int size() {
    return size;
  }

  /**
   * Builds the result and returns it as a string
   */
  public String toString() {
    return root.toString() + "</payload>";
  }
}
