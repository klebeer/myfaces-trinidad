/*
 * Copyright  2005,2006 The Apache Software Foundation.
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
package org.apache.myfaces.trinidadinternal.renderkit.core.xhtml;

import java.awt.Color;

import java.io.IOException;

import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.apache.myfaces.trinidad.util.ArrayMap;

import org.apache.myfaces.trinidadinternal.agent.TrinidadAgent;
import org.apache.myfaces.trinidadinternal.renderkit.RenderingContext;
import org.apache.myfaces.trinidadinternal.skin.icon.Icon;

/**
 * Utilities for miscellaneous HTML output.
 * @todo RENAME CLASS
 */
public class OutputUtils
{
  /**
   * Gets the character encoding of the output.
   */
  public static String getOutputEncoding(FacesContext context)
  {
    return context.getResponseWriter().getCharacterEncoding();
  }

  public static void renderHiddenField(
    FacesContext context,
    String       name,
    String       value) throws IOException
  {
    ResponseWriter rw = context.getResponseWriter();
    rw.startElement("input", null);
    rw.writeAttribute("type", "hidden", null);
    rw.writeAttribute("name", name, null);
    rw.writeAttribute("value", value, null);
    rw.endElement("input");
  }

  public static void renderLayoutTableAttributes(
    FacesContext        context,
    RenderingContext arc,
    Object              cellspacing,
    Object              tableWidth
    ) throws IOException
  {
    renderLayoutTableAttributes(context, arc, "0", cellspacing, tableWidth);
  }


  public static void renderLayoutTableAttributes(
    FacesContext        context,
    RenderingContext arc,
    Object              cellpadding,
    Object              cellspacing,
    Object              tableWidth
    ) throws IOException
  {
    renderLayoutTableAttributes(context, arc, cellpadding, cellspacing, "0",
                                tableWidth);
  }

  /**
   * All layout tables should call this method, so that a special summary tag
   * (which silences OAC) is rendered
   */
  public static void renderLayoutTableAttributes(
    FacesContext        context,
    RenderingContext arc,
    Object              cellpadding,
    Object              cellspacing,
    Object              border,
    Object              tableWidth
    ) throws IOException
  {
    renderLayoutTableAttributes(context, arc, cellpadding, cellspacing, border,
                                tableWidth, "" /* summary */ );
  }

  /**
   * all data tables should call this one, so that a summary tag is written
   * out
   */
  public static void renderLayoutTableAttributes(
    FacesContext        context,
    RenderingContext arc,
    Object              cellpadding,
    Object              cellspacing,
    Object              border,
    Object              tableWidth,
    Object              summary
    ) throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();
    writer.writeAttribute("cellpadding", cellpadding, null);
    writer.writeAttribute("cellspacing", cellspacing, null);
    writer.writeAttribute("border", border, null);
    writer.writeAttribute("width", tableWidth, null);

    if (!XhtmlRenderer.isInaccessibleMode(arc))
    {
      writer.writeAttribute("summary", summary, null);
    }
  }
  /**
  * Renders only the alt attribute
  * if that can be used as a tooltip on an image.
  * Otherwise it renders both the alt and the title attributes.
  */
  public static void renderAltAndTooltipForImage(
     FacesContext        context,
     RenderingContext afc,
     Object              textValue
     ) throws IOException
  {
    if (textValue == null)
      return;

    ResponseWriter writer = context.getResponseWriter();
    boolean wroteTitle = false;

    if (!supportsAltRendersTooltipOnImage(afc))
    {
      if (!"".equals(textValue))
      {
        writer.writeAttribute("title", textValue, null);
        wroteTitle = true;
      }
    }

    // only write out both title and alt if
    // we really need both
    if (!wroteTitle || !XhtmlRenderer.isInaccessibleMode(afc))
    {
      writer.writeAttribute("alt", textValue, null);
    }
  }

  /**
   * Returns true if the agent supports alt as a tooltip on images
   */
  public static boolean supportsAltRendersTooltipOnImage(
    RenderingContext     afc
    )
  {
    return Boolean.TRUE.equals(afc.getAgent().getCapability(
                        TrinidadAgent.CAP_ALT_RENDERS_TOOLTIP_ON_IMAGE));
  }

  /**
   * Returns the valign vAlign value for aligning image icons
   * vertically with text on the same line.
   */
  public static String getMiddleIconAlignment(
    RenderingContext arc)
  {
    // =-= AEW I haven't been able to find an image alignment
    // that works well for all browsers.  "absmiddle" looks
    // great in IE, but that's a nonstandard hack. "middle"
    // should work OK everywhere, but looks terrible in both
    // IE and Netscape (but OK in Mozilla) "top"'s OK in Netscape.
    // For now, "top" in Netscape, "absmiddle" everywhere else

    int agentApplication = arc.getAgent().getAgentApplication();

    if (agentApplication == TrinidadAgent.APPLICATION_NETSCAPE)
    {
      return XhtmlConstants.V_ALIGN_TOP;
    }
    else
    {
      // Previously we used "middle" for all other browsers except
      // for Safari, where "absmiddle" was required for reasonable
      // results.  However, as far as I can tell, for images which
      // are evenly padded on top/bottom, absmiddle also looks
      // best on IE and Mozilla.  So, let's use absmiddle for
      // these browsers too.
      // =-= MLL Update: to address Bug # 3426092, alignment has been set to
      // "middle" to comply with HTML 4.01 Transitional Spec.

      return XhtmlConstants.V_ALIGN_MIDDLE;
    }
  }

  /**
   * Renders the specified Icon with the provided attributes.
   */
  public static void renderIcon(
    FacesContext        context,
    RenderingContext arc,
    Icon                icon,
    Object              shortDesc,
    Object              align
    ) throws IOException
  {
    renderIcon(context, arc, icon, shortDesc, align, false);
  }

  /**
   * Renders the specified Icon with the provided attributes.
   */
  public static void renderIcon(
    FacesContext        context,
    RenderingContext arc,
    Icon                icon,
    Object              shortDesc,
    Object              align,
    boolean             embedded
    ) throws IOException
  {
    if (icon.isNull())
      return;

    Map attrs = null;

    if ((shortDesc != null)  ||
        (align != null)      ||
         embedded)
    {
      attrs = new ArrayMap(3);
      attrs.put(Icon.SHORT_DESC_KEY, shortDesc);
      attrs.put(Icon.ALIGN_KEY, align);
      attrs.put(Icon.EMBEDDED_KEY, Boolean.valueOf(embedded));
    }

    icon.renderIcon(context, arc, attrs);
  }

  /**
   * Renders an image tag.
   */
  static public void renderImage(
    FacesContext        context,
    RenderingContext arc,
    Object              absoluteUri,
    Object              width,
    Object              height,
    Object              id,
    Object              altText
    ) throws IOException
  {
    renderImage(context, arc, absoluteUri,
                width, height, id, altText, null);
  }

  static public void renderImage(
    FacesContext        context,
    RenderingContext arc,
    Object              absoluteUri,
    Object              width,
    Object              height,
    Object              id,
    Object              altText,
    UIComponent         comp
    ) throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();

    writer.startElement("img", comp);
    writer.writeAttribute("id", id, null);
    // TODO: writeURIAttribute here
    writer.writeAttribute("src", absoluteUri, null);

    renderAltAndTooltipForImage(context, arc, altText);

    if (width != null)
    {
      writer.writeAttribute("width", width, null);
    }

    if (height != null)
    {
      writer.writeAttribute("height", height, null);
    }

    writer.endElement("img");
  }

  /**
   * @todo Add real mechanism for getting the background color
   * if needed.
   */
  static public Color getBackgroundColor(RenderingContext arc)
  {
    return Color.WHITE;
  }
}
