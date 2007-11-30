/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.myfaces.trinidadinternal.style.xml.parse;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

/**
 * Test the hashCode and equals methods on the StyleSheetNode object and the objects
 * that make up the StyleSheetNode. The hashcode of the StyleSheetNode object is used
 * to create a StyleSheetDocument id.
 */
public class StyleSheetNodeEqualsTest extends TestCase
{
  public StyleSheetNodeEqualsTest(String name)
  {
    super(name);
  }

  /**
   * test the IncludePropertyNode hashCode and equals
   */
  public void testIncludePropertyNodeEquals() throws Exception
  {
    IncludePropertyNode node = 
      new IncludePropertyNode(null, "AFDefaultColor", "background-color", "color");
    IncludePropertyNode anotherNode = 
      new IncludePropertyNode(null, "AFDefaultColor", "background-color", "color");
    IncludePropertyNode unequalNode = 
      new IncludePropertyNode(null, "AFDefaultColor", "background-color", "border-color");

    assertEquals(node.hashCode() == anotherNode.hashCode(), true);   
    assertEquals(node.equals(anotherNode), true);
    assertEquals(anotherNode.equals(node), true);
    assertEquals(node.equals(unequalNode), false);
    assertEquals(node.equals("Hello"), false);
  }

  /**
   * test the IncludeStyleNode hashCode and equals
   */
  public void testIncludeStyleNodeEquals() throws Exception
  {
    IncludeStyleNode node = 
      new IncludeStyleNode("name", null);
    IncludeStyleNode anotherNode = 
      new IncludeStyleNode("name", null);      
    IncludeStyleNode unequalNode = 
      new IncludeStyleNode(null, "anotherSelector");      
    
    assertEquals(node.hashCode() == anotherNode.hashCode(), true);   
    assertEquals(node.equals(anotherNode), true);
    assertEquals(anotherNode.equals(node), true);
    assertEquals(node.equals(unequalNode), false);
    assertEquals(node.equals("Hello"), false);

  }  
  
  /**
   * test the StyleNode hashCode and equals
   */
  public void testStyleNodeEquals() throws Exception
  {

    StyleNode afDefaultFontNode = getAfDefaultFontStyleNode();
                    
    StyleNode afDefaultFontNodeReset = getAfDefaultFontResetStyleNode();                    
                    
    StyleNode afOutputLabelNode = getOutputLabelStyleNode();
     
    StyleNode anotherOutputLabelNode = getAnotherOutputLabelStyleNode();
     
    assertEquals(anotherOutputLabelNode.hashCode() == afOutputLabelNode.hashCode(), true);   
     
    assertEquals(anotherOutputLabelNode.equals(afOutputLabelNode), true);
    assertEquals(afOutputLabelNode.equals(anotherOutputLabelNode), true);
     
    assertEquals(afDefaultFontNode.equals(anotherOutputLabelNode), false);
     
    assertEquals(afDefaultFontNode.equals(afDefaultFontNodeReset), false);
    assertEquals(afDefaultFontNodeReset.equals(afDefaultFontNode), false);
    assertEquals(afDefaultFontNodeReset.equals("Hello"), false);

  }
  
  public void testStyleSheetNodeEquals() throws Exception
  {
    StyleNode[] styleSheetOneNodes = {getAfDefaultFontStyleNode(), 
                                      getAfDefaultFontResetStyleNode(),
                                      getOutputLabelStyleNode(),
                                      getAnotherOutputLabelStyleNode()};
    StyleNode[] anotherStyleSheetOneNodes = 
                                      {getAfDefaultFontStyleNode(), 
                                      getAfDefaultFontResetStyleNode(),
                                      getOutputLabelStyleNode(),
                                      getAnotherOutputLabelStyleNode()};

    // create locales arrays
    Locale[] localesArray = getLocalesArray();
    Locale[] anotherLocalesArray = getAnotherLocalesArray();
    Locale[] diffOrderLocalesArray = getDiffOrderLocalesArray();
     
    // create a browsers array
    int[] browsers = {1, 2};
    int[] anotherBrowsers = {1, 2};
    int[] anotherBrowsersDiffOrder = {2, 1};
    
    int[] versions = {6};
    int[] anotherVersions = {6};

    int[] platforms = {2, 3, 4};
    int[] anotherPlatforms = {2, 3, 4};
    int[] differentOrderPlatforms = {2, 4, 3};

    
    // The constructor takes these arguments:
    // StyleNode[] styles,
    // Locale[] locales,
    // int direction,
    // int[] browsers,
    // int[] versions,
    // int[] platforms,
    // int mode
    
    StyleSheetNode styleSheetNode = 
      new StyleSheetNode(styleSheetOneNodes, 
                         localesArray, 
                         0, 
                         browsers, 
                         versions, 
                         platforms, 
                         0);
    StyleSheetNode anotherStyleSheetNode = 
      new StyleSheetNode(anotherStyleSheetOneNodes, 
                         anotherLocalesArray,
                         0, 
                         anotherBrowsersDiffOrder, 
                         anotherVersions,
                         anotherPlatforms, 
                         0);
    StyleSheetNode sameDiffOrderStyleSheetNode = 
      new StyleSheetNode(anotherStyleSheetOneNodes, 
                         diffOrderLocalesArray,
                         0, 
                         anotherBrowsersDiffOrder, 
                         anotherVersions,
                         anotherPlatforms, 
                         0);                         
      
    // these should be equal
    assertEquals(styleSheetNode.hashCode() == anotherStyleSheetNode.hashCode(), true);   
    assertEquals(anotherStyleSheetNode.equals(styleSheetNode), true);
    assertEquals(styleSheetNode.equals(anotherStyleSheetNode), true); 
    assertEquals(sameDiffOrderStyleSheetNode.equals(anotherStyleSheetNode), true); 
    assertEquals(styleSheetNode.equals(sameDiffOrderStyleSheetNode), true);
    
    // these should be false
    assertEquals(styleSheetNode.equals(null), false);
    assertEquals(styleSheetNode.equals(localesArray), false);
    
    /* Test styleSheetNode's toString */
    /*
    System.out.println(sameLocaleDiffOrderStyleSheetNode.toString());
    */
    
  }
  
  // returns a StyleNode for "AFDefaultFont" name, null selector, resetProperties = false  
  private StyleNode getAfDefaultFontStyleNode()
  {
    PropertyNode fontSize = new PropertyNode("font-size", "10pt");
    PropertyNode fontWeight = new PropertyNode("font-weight", "normal");    
    PropertyNode[] defaultFontPropertyNodes = { fontSize, fontWeight };

    IncludeStyleNode[] defaultFontIncludeStyles = 
      { new IncludeStyleNode("AFDefaultFontFamily", null) };
      
    IncludePropertyNode[] defaultFontIncludeProperty = 
      { new IncludePropertyNode("AFVeryDarkBackground", null, "background-color", "color") };

    return
      new StyleNode("AFDefaultFont", null, defaultFontPropertyNodes, 
                    defaultFontIncludeStyles, defaultFontIncludeProperty, null, false);
    
  }
  
  // returns a StyleNode for "AFDefaultFont" name, null selector, resetProperties = true
  private StyleNode getAfDefaultFontResetStyleNode()
  {
    PropertyNode fontSize = new PropertyNode("font-size", "10pt");
    PropertyNode fontWeight = new PropertyNode("font-weight", "normal");    
    PropertyNode[] defaultFontPropertyNodes = { fontSize, fontWeight };

    IncludeStyleNode[] defaultFontIncludeStyles = 
      { new IncludeStyleNode("AFDefaultFontFamily", null) };
      
    IncludePropertyNode[] defaultFontIncludeProperty = 
      { new IncludePropertyNode("AFVeryDarkBackground", null, "background-color", "color") };

    return
      new StyleNode("AFDefaultFont", null, defaultFontPropertyNodes, 
                    defaultFontIncludeStyles, defaultFontIncludeProperty, null, true);
    
  }  
  
  // returns a StyleNode for "af|outputLabel:error" selector
  private StyleNode getOutputLabelStyleNode()
  {
    // build up another StyleNode
    IncludeStyleNode[] labelIncludeStyles = {new IncludeStyleNode("AFLabel", null)};
    PropertyNode[] labelPropertyNodes = {new PropertyNode("color", "red")};
    
    Set<String> labelInhibitedProperties = new HashSet<String>();
    labelInhibitedProperties.add("font-size");
    labelInhibitedProperties.add("background-color");

      
    return
        new StyleNode(null, "af|outputLabel:error", labelPropertyNodes, labelIncludeStyles, 
                      null, labelInhibitedProperties, false);
  }
  
  // returns a StyleNode that matches the outputLabelStyleNode
  // the inhibited properties are added in a different order, that's all.
  private StyleNode getAnotherOutputLabelStyleNode()
  {
    IncludeStyleNode[] anotherIncludeStyles = {new IncludeStyleNode("AFLabel", null)};
    PropertyNode[] anotherPropertyNodes = {new PropertyNode("color", "red")};
    Set<String> anotherInhibitedProperties = new HashSet<String>();
    anotherInhibitedProperties.add("background-color");
    anotherInhibitedProperties.add("font-size");
    
    return  new StyleNode(null, "af|outputLabel:error", anotherPropertyNodes, anotherIncludeStyles, 
                          null, anotherInhibitedProperties, false);
  }
  
  
  private Set<Locale> getLocalesSet()
  {
    Set<Locale> set = new HashSet<Locale>();
    set.add(new Locale("zh", "TW"));
    set.add(new Locale("zh", "CN"));
    return set; 
  }  
  
  private Set<Locale> getDiffOrderLocalesSet()
  {
    Set<Locale> set = new HashSet<Locale>();
    set.add(new Locale("zh", "CN"));
    set.add(new Locale("zh", "TW"));

    return set;  
  }
  
  // same as above
  private Locale[] getLocalesArray()
  {
    return new Locale[] {new Locale("tw", "TW"), new Locale("zh", "CN")};  
  }  
  
  private Locale[] getAnotherLocalesArray()
  {
    return new Locale[] {new Locale("tw", "TW"), new Locale("zh", "CN")};  
  }  
  
  // same as above, different order
  private Locale[] getDiffOrderLocalesArray()
  {
    return new Locale[] {new Locale("zh", "CN"), new Locale("tw", "TW")};  
  }
  
}
