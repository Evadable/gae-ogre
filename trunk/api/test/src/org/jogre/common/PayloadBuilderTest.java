package org.jogre.common;

import nanoxml.XMLParseException;
import junit.framework.TestCase;

/**
 * Unit tests for the PayloadBuilder
 * 
 * @author Jens Scheffler
 *
 */
public class PayloadBuilderTest extends TestCase {
  
  public void testEmptyBuilder() {
    assertEquals("<payload></payload>", PayloadBuilder.payload(null).toString());
    assertEquals("<payload meta=\"foo\"></payload>", PayloadBuilder.payload("foo").toString());
  }
  
  public void testNonvalidatedXml() {
    assertEquals(
        "<payload><a/></payload>", 
        PayloadBuilder.payload(null).addChildNoparse("<a/>").toString());
    assertEquals(
        "<payload><a></payload>", 
        PayloadBuilder.payload(null).addChildNoparse("<a>").toString());
    assertEquals(
        "<payload meta=\"foo\"><a></payload>", 
        PayloadBuilder.payload("foo").addChildNoparse("<a>").toString());
  }
  
  public void testValidatedXml() {
    assertEquals(
        "<payload><a/></payload>", 
        PayloadBuilder.payload(null).addChild("<a/>").toString());
    try {
      PayloadBuilder.payload(null).addChild("<a>");
      fail("Excpected: XMLParseException!");
    }
    catch (XMLParseException expected) {
      // fall through
    }
  }
}
