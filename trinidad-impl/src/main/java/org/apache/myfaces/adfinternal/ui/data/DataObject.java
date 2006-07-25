/*
 * Copyright  1999-2006 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.myfaces.adfinternal.ui.data;

import org.apache.myfaces.adfinternal.ui.RenderingContext;

/**
 * The DataObject interface provides an extremely simple API for
 * retrieving arbitrarily structured data.  All "queries"
 * are based simply on a selection string.  It is entirely up to an
 * implementation of this interface to define the syntax for these
 * strings.
 * <p>
 * @version $Name:  $ ($Revision: adfrt/faces/adf-faces-impl/src/main/java/oracle/adfinternal/view/faces/ui/data/DataObject.java#0 $) $Date: 10-nov-2005.18:56:30 $
 * @author The Oracle ADF Faces Team
 */
public interface DataObject
{
  /**
   * Given a select string, returns the value matching that
   * selection.  It is legal to return null if the select key
   * doesn't match anything.
   * <p>
   * @param context the current rendering context
   * @param select a select criterion, syntax as defined by the data object
   */
  public Object selectValue(RenderingContext context, Object select);
}

