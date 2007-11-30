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
package org.apache.myfaces.trinidadinternal.ui.laf.base.xhtml;

import junit.framework.TestCase;

/**
 * Unit tests for XhtmlLafUtils.
 *
 * @version $Name:  $ ($Revision: adfrt/faces/adf-faces-impl/src/test/java/oracle/adfinternal/view/faces/ui/laf/base/xhtml/XhtmlLafUtilsTest.java#1 $) $Date: 16-aug-2005.15:15:42 $
 */
public class XhtmlLafUtilsTest extends TestCase
{
  public XhtmlLafUtilsTest(
    String testName)
  {
    super(testName);
  }

  /**
   * Tests JavaScript strings are escaped inside single quotation marks.
   */
  public void testEscapeInQuoteJS()
  {
    String raw = "a'b";
    StringBuilder escaped = new StringBuilder();
    XhtmlLafUtils.escapeJS(escaped, raw, true);
    assertEquals("a\\\'b", escaped.toString());
  }

  /**
   * Tests JavaScript strings are double escaped inside single quotation marks.
   */
  public void testDoubleEscapeInQuoteJS()
  {
    String raw = "a'b";
    StringBuilder escaped = new StringBuilder();
    XhtmlLafUtils.escapeJS(escaped, raw, true, 2);
    assertEquals("a\\\\\\\'b", escaped.toString());
  }
}