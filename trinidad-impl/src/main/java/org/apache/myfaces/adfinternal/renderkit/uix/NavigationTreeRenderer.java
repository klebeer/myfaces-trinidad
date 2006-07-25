/*
 * Copyright  2003-2006 The Apache Software Foundation.
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
package org.apache.myfaces.adfinternal.renderkit.uix;

import java.util.Collections;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.apache.myfaces.adfinternal.renderkit.core.xhtml.table.TreeUtils;
import org.apache.myfaces.adfinternal.ui.UIConstants;
import org.apache.myfaces.adfinternal.uinode.UINodeRendererBase;

/**
 * Renderer for tree
 * <p>
 * @version $Name:  $ ($Revision: adfrt/faces/adf-faces-impl/src/main/java/oracle/adfinternal/view/faces/renderkit/uix/NavigationTreeRenderer.java#0 $) $Date: 10-nov-2005.19:00:30 $
 * @author The Oracle ADF Faces Team
 */
public class NavigationTreeRenderer extends UINodeRendererBase
{


  /**
   * @todo do not mess with selection here. queue an event.
   */
  public void decode(
    FacesContext context, 
    UIComponent component)
  {
    Map parameters = context.getExternalContext().getRequestParameterMap();
    String source = (String) parameters.get(UIConstants.SOURCE_PARAM);

    if (!component.getClientId(context).equals(source))
      return;

    TreeUtils.decodeExpandEvents(parameters, component, Collections.EMPTY_LIST);
   
  }  
}
