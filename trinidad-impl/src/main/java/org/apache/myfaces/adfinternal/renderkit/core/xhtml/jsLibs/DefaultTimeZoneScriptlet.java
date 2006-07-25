/*
 * Copyright  2002-2006 The Apache Software Foundation.
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
package org.apache.myfaces.adfinternal.renderkit.core.xhtml.jsLibs;

import java.io.IOException;

import javax.faces.context.FacesContext;
import org.apache.myfaces.adfinternal.renderkit.AdfRenderingContext;

/**
 * Scriptlet for defaulting the time zone.

 * @version $Name:  $ ($Revision: adfrt/faces/adf-faces-impl/src/main/java/oracle/adfinternal/view/faces/renderkit/core/xhtml/jsLibs/DefaultTimeZoneScriptlet.java#0 $) $Date: 10-nov-2005.19:02:45 $
 * @author The Oracle ADF Faces Team
 */
class DefaultTimeZoneScriptlet extends Scriptlet
{
  static public final String DEFAULT_TIME_ZONE_KEY = "defaultTimeZone";

  static public Scriptlet sharedInstance()
  {
    return _sInstance;
  }

  private DefaultTimeZoneScriptlet()
  {
  }

  public Object getScriptletKey()
  {
    return DEFAULT_TIME_ZONE_KEY;
  }

  protected void outputScriptletImpl(
    FacesContext        context,
    AdfRenderingContext arc) throws IOException
  {
    outputDependency(context, arc, XhtmlScriptletFactory.COOKIES_LIB);
    super.outputScriptletImpl(context, arc);
  }

  protected void outputScriptletContent(
    FacesContext        context,
    AdfRenderingContext arc) throws IOException
  {
    context.getResponseWriter().writeText("_defaultTZ()", null);
  }

  static private final Scriptlet _sInstance =
    new DefaultTimeZoneScriptlet();
}
