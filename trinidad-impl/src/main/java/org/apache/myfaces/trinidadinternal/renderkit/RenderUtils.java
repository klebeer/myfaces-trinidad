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
package org.apache.myfaces.trinidadinternal.renderkit;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 * Stub for temporary backwards compatibility.
 *
 * @author The Oracle ADF Faces Team
 * @deprecated use public API
 */
public class RenderUtils
{
  private RenderUtils()
  {
  }


  /**
   * Encodes a component and all of its children.
   */
  @SuppressWarnings("unchecked")
  static public void encodeRecursive(FacesContext context,
                                     UIComponent component)
    throws IOException
  {
    org.apache.myfaces.trinidad.render.RenderUtils.encodeRecursive(context,
                                                                   component);
  }

  /**
   *  Retrieves id of the form the component is contained in.
   *
   * @param context the faces context object
   * @param component UIComponent whose container form id is to be retuirned
   *
   * @return String id of the form if one exists in component's hierarchy,
   *                otherwise null
   */
  public static String getFormId(
    FacesContext context,
    UIComponent  component)
  {
    return org.apache.myfaces.trinidad.render.RenderUtils.getFormId(context,
                                                                    component);
  }
}
