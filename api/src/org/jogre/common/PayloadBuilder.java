package org.jogre.common;

import nanoxml.XMLElement;

/**
 * Utility class, used to concatenate a set of xml-elements (given as strings)
 * into a single entity that can be parsed back on the receiving side.
 * The final result can be retrieved by using the toString() method
 * 
 * @author Jens Scheffler
 *
 */
public class PayloadBuilder {
  
  private final StringBuilder root;
  
  /**
   * Constructor
   * @param meta an arbitrary string with meta-information that should be included in the outer tag
   */
  private PayloadBuilder(String meta) {
    XMLElement elem = new XMLElement("payload");
    if (meta != null) {
      elem.setAttribute("meta", meta);
    }
    
    // This assumes that the final element always ends with "/>".
    // It deletes the backslash, thus making the element an open tag
    root = new StringBuilder(elem.toString());
    assert root.charAt(root.length() - 2) == '/';
    root.deleteCharAt(root.length() - 2);
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
   * Builds the result and returns it as a string
   */
  public String toString() {
    return root.toString() + "</payload>";
  }
}
