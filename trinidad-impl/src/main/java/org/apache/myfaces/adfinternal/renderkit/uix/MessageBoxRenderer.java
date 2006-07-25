/*
 * Copyright  2004-2006 The Apache Software Foundation.
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

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.apache.myfaces.adf.context.AdfFacesContext;

import org.apache.myfaces.adfinternal.uinode.UINodeRendererBase;

public class MessageBoxRenderer extends UINodeRendererBase
{
  public void encodeBegin(
    FacesContext context,
    UIComponent  component) throws IOException
  {
    // Force MessageBox to be re-rendered via PPR, since the set
    // of messages may have changed.
    AdfFacesContext afContext = AdfFacesContext.getCurrentInstance();
    if (afContext != null)
      afContext.addPartialTarget(component);

    super.encodeBegin(context, component);
  }
}
