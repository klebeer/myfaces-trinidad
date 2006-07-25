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
package org.apache.myfaces.adfinternal.renderkit.core.xhtml;

import java.awt.Color;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import org.apache.myfaces.adf.logging.ADFLogger;

import org.apache.myfaces.adf.bean.FacesBean;
import org.apache.myfaces.adf.bean.PropertyKey;
import org.apache.myfaces.adf.component.core.input.CoreSelectInputColor;
import org.apache.myfaces.adf.context.AdfFacesContext;
import org.apache.myfaces.adf.event.ReturnEvent;

import org.apache.myfaces.adfinternal.agent.AdfFacesAgent;
import org.apache.myfaces.adfinternal.convert.ColorConverter;
import org.apache.myfaces.adfinternal.renderkit.AdfRenderingContext;
import org.apache.myfaces.adfinternal.renderkit.core.CoreRendererUtils;
import org.apache.myfaces.adfinternal.renderkit.core.pages.GenericEntry;
import org.apache.myfaces.adfinternal.renderkit.core.xhtml.jsLibs.AliasedScriptlet;
import org.apache.myfaces.adfinternal.renderkit.core.xhtml.jsLibs.ColorFieldInfoScriptlet;
import org.apache.myfaces.adfinternal.renderkit.core.xhtml.jsLibs.ConfigurationScriptlet;

import org.apache.myfaces.adfinternal.skin.icon.Icon;
import org.apache.myfaces.adfinternal.style.util.CSSUtils;
import org.apache.myfaces.adfinternal.util.IntegerUtils;


/**
 * @todo Resolve ALT keys
 * @todo is AF_SELECT_INPUT_COLOR_SWATCH_OVERLAY_ICON_STYLE_CLASS used anywhere?
 */
public class SimpleSelectInputColorRenderer
  extends SimpleSelectInputTextRenderer
{
  public SimpleSelectInputColorRenderer()
  {
    super(CoreSelectInputColor.TYPE);
  }

  protected void findTypeConstants(FacesBean.Type type)
  {
    super.findTypeConstants(type);
    _compactKey = type.findKey("compact");
    _chooseIdKey = type.findKey("chooseId");
  }

  protected void queueActionEvent(FacesContext context, UIComponent component)
  {
    FacesBean bean = getFacesBean(component);
    // If there's a non-default action, then just launch away
    if (getAction(bean) != null)
    {
      super.queueActionEvent(context, component);
    }
    // Otherwise, we'll fall back to launching the default dialog
    // (This should only happen on devices without support for
    // custom windows - everything else would have just launched
    // a calendar window with the _ldp JS function)
    else
    {
      Object submittedValue = (String) getSubmittedValue(bean);
      Color color = null;
      try
      {
        Object converted = getConvertedValue(context,
                                             component,
                                             submittedValue);
        if (converted instanceof Color)
          color = (Color) converted;
      }
      // Not a big deal;  just means that an invalid value was entered,
      // so we'll launch the dialog showing nothing
      catch (ConverterException ce)
      {
        _LOG.fine(ce);
      }


      AdfFacesContext afContext = AdfFacesContext.getCurrentInstance();

      // =-=AEW Parameters?  Shouldn't we pass in the color?
      Map parameters = null;
      afContext.launchDialog(GenericEntry.getGenericEntryViewRoot(context),
                             parameters,
                             component,
                             true,
                             null);
    }
  }


  /**
   * Give subclasses a chance to override the ReturnEvent.
   */
  protected void queueReturnEvent(
    FacesContext context,
    UIComponent  component,
    ReturnEvent  event)
  {
    Object returnValue = event.getReturnValue();

    // If we got passed a Color object, send it back to String
    // land (where it needs to be for submitted values).
    if (returnValue instanceof Color)
    {
      FacesBean bean = getFacesBean(component);
      Converter converter = getConverter(bean);
      if (converter == null)
        converter = getDefaultConverter(context, bean);

      if (converter != null)
      {
        returnValue = converter.getAsString(context,
                                            component,
                                            returnValue);
      }
      else
      {
        returnValue = returnValue.toString();
      }

      event = new ReturnEvent(component,
                              returnValue,
                              event.getReturnParameters());
    }

    event.queue();
  }


  protected void encodeAllAsElement(
    FacesContext        context,
    AdfRenderingContext arc,
    UIComponent         component,
    FacesBean           bean) throws IOException
  {
    String chooseId = _computeChooseId(context, component, bean);
    arc.getProperties().put(_CACHED_CHOOSE_ID, chooseId);

    // Add the scriptlets required by the color field
    XhtmlUtils.addLib(context, arc, "_fixCFF()");
    XhtmlUtils.addLib(context, arc, _COLOR_FIELD_LIB);
    super.encodeAllAsElement(context, arc, component, bean);

    if (!getDisabled(bean))
    {
      // =-=AEW addOnSubmitConverterValidators() when compact???
      _renderFirstColorFieldScript(context, arc, component, _getChooseId(arc));
    }

    arc.getProperties().remove(_CACHED_CHOOSE_ID);
  }

  protected void renderTextField(
    FacesContext        context,
    AdfRenderingContext arc,
    UIComponent         component,
    FacesBean           bean) throws IOException
  {
    if (!isCompact(bean) ||
        !_supportsSwatchAndChooser(arc))
    {
      super.renderTextField(context, arc, component, bean);
    }
    else
    {
      if (isAutoSubmit(bean))
        AutoSubmitUtils.writeDependencies(context, arc);
      addOnSubmitConverterValidators(context, arc, component, bean);

      // In compact mode, write out a hidden field that is
      // the stub
      ResponseWriter writer = context.getResponseWriter();
      writer.startElement("input", component);
      writer.writeAttribute("type", "hidden", null);
      writer.writeAttribute("value",
                            getConvertedString(context,
                                               component,
                                               bean),
                            "value");
      String id = arc.getCurrentClientId();
      writer.writeAttribute("id", id, null);
      writer.writeAttribute("name", id, null);
      writer.endElement("input");
    }
  }

  protected void renderContent(
    FacesContext        context,
    AdfRenderingContext arc,
    UIComponent         component,
    FacesBean           bean,
    boolean             renderAsElement,
    boolean             isTextArea) throws IOException
  {
    // Hook here to make sure we're inside the <span>
    assert(!isTextArea);
    super.renderContent(context, arc, component, bean,
                        renderAsElement, isTextArea);
    if (!renderAsElement)
    {
      renderAfterTextField(context, arc, component, bean);
    }
  }

  protected void renderAfterTextField(
    FacesContext        context,
    AdfRenderingContext arc,
    UIComponent         component,
    FacesBean           bean) throws IOException
  {
    if (!_supportsSwatchAndChooser(arc))
      return;

    if (!isCompact(bean))
    {
      // =-=AEW TODO: Make spacer a property?
      renderSpacer(context, arc, "8", "1");
      renderIcon(context, arc, component, bean);
    }
    else
    {
      renderIcon(context, arc, component, bean);
    }
  }

  protected void renderIcon(
    FacesContext        context,
    AdfRenderingContext arc,
    UIComponent         component,
    FacesBean           bean) throws IOException
  {
    boolean isEditable = ((_getChooseId(arc) == null) &&
                          !getDisabled(bean) &&
                          !getReadOnly(context, bean));
    if (isEditable)
    {
      XhtmlUtils.addLib(context,
                        arc,
                        ConfigurationScriptlet.sharedInstance().getScriptletKey());
    }

    _renderColorSwatch(context, arc, component, bean, isEditable);
  }

  /**
   * Return a default converter.
   */
  protected Converter getDefaultConverter(
    FacesContext context,
    FacesBean    bean)
  {
    return _DEFAULT_CONVERTER;
  }


  protected String getLaunchOnclick(
    FacesContext        context,
    AdfRenderingContext arc,
    UIComponent         component,
    FacesBean           bean) throws IOException
  {
    // If the field has an action, use the default behavior.  Or,
    // if the field doesn't support launching a window at all,
    // use the default behavior.
    if ((getAction(bean) != null) ||
        !Boolean.TRUE.equals(
            arc.getAgent().getCapability(AdfFacesAgent.CAP_MULTIPLE_WINDOWS)))
      return super.getLaunchOnclick(context, arc, component, bean);

    String id = arc.getCurrentClientId();
    if ((id == null) || (arc.getFormData() == null))
      return null;

    // we want something big enough
    StringBuffer onClickBuffer = new StringBuffer(100);

    onClickBuffer.append("_lcp('");
    onClickBuffer.append(arc.getFormData().getName());
    onClickBuffer.append("','");

    onClickBuffer.append(id);
    onClickBuffer.append("'); return false");

    return onClickBuffer.toString();
  }

  protected String getOnfocus(FacesBean bean)
  {
    String onfocus = super.getOnfocus(bean);
    AdfRenderingContext arc = AdfRenderingContext.getCurrentInstance();
    if (!_supportsSwatchAndChooser(arc))
      return onfocus;

    String chooseId = _getChooseId(arc);
    // The special _dff handler is only needed for date fields
    // connected to a chooser;  the blur handler is needed all the time.
    if (chooseId != null)
    {
      int length = _FOCUS_PREFIX.length() + _FOCUS_SUFFIX.length() + chooseId.length();

      StringBuffer buffer = new StringBuffer(length);
      buffer.append(_FOCUS_PREFIX);
      buffer.append(chooseId);
      buffer.append(_FOCUS_SUFFIX);

      return XhtmlUtils.getChainedJS(buffer.toString(), onfocus, false);
    }
    else
    {
      return onfocus;
    }
  }

  protected String getOnblur(FacesBean bean)
  {
    String onblur = super.getOnblur(bean);
    if (!_supportsSwatchAndChooser(AdfRenderingContext.getCurrentInstance()))
      return XhtmlUtils.getChainedJS(_AUTO_FORMAT_SCRIPT,
                                     onblur, false);


    return XhtmlUtils.getChainedJS(_AUTO_FORMAT_SCRIPT + _AUTO_SWATCH_SCRIPT,
                                   onblur, false);
  }



  protected Integer getDefaultColumns(AdfRenderingContext arc, FacesBean bean)
  {
    Converter converter = getConverter(bean);

    // Ignoring the "default" converter code is intentional;  we'll just
    // fall through to _DEFAULT_COLUMNS here to save time
    if (converter instanceof ColorConverter)
    {
      int columns = ((ColorConverter) converter).getColumns(FacesContext.getCurrentInstance());
      return IntegerUtils.getInteger(columns);
    }

    return _DEFAULT_COLUMNS;
  }

  protected Number getMaximumLength(FacesBean bean)
  {
    // Not supported for selectInputColor
    // =-=AEW We could have a good default
    return null;
  }


  protected String getButtonIconName()
  {
    return XhtmlConstants.AF_SELECT_INPUT_COLOR_LAUNCH_ICON_NAME;
  }

  protected String getChooseId(FacesBean bean)
  {
    return toString(bean.getProperty(_chooseIdKey));
  }

  protected boolean isCompact(FacesBean bean)
  {
    Object o = bean.getProperty(_compactKey);
    if (o == null)
      o = _compactKey.getDefault();

    return Boolean.TRUE.equals(o);
  }

  protected String getRootStyleClass(FacesBean bean)
  {
    return "af|selectInputColor";
  }

  protected String getContentStyleClass(FacesBean bean)
  {
    return "af|selectInputColor::content";
  }

  private String _getChooseId(AdfRenderingContext arc)
  {
    return (String) arc.getProperties().get(_CACHED_CHOOSE_ID);
  }

  private String _computeChooseId(
    FacesContext context,
    UIComponent  component,
    FacesBean    bean)
  {
    return CoreRendererUtils.getRelativeId(context,
                                           component,
                                           getChooseId(bean));

  }

  // On PDAs, we only support a simple text field
  private boolean _supportsSwatchAndChooser(AdfRenderingContext arc)
  {
    return (arc.getAgent().getAgentType() != AdfFacesAgent.TYPE_PDA);
  }

  protected String getSearchDesc(FacesBean bean)
  {
    AdfRenderingContext arc = AdfRenderingContext.getCurrentInstance();
    if (isInaccessibleMode(arc))
      return null;

    return arc.getTranslatedString(_LAUNCH_PICKER_TIP_KEY);
  }

  private String _getColorSwatchId(AdfRenderingContext arc)
  {
    return arc.getCurrentClientId() + "$sw";
  }


  private void _renderColorSwatch(
    FacesContext        context,
    AdfRenderingContext arc,
    UIComponent         component,
    FacesBean           bean,
    boolean             editable) throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();
    if (editable)
    {
      writer.startElement("a", component);
      writer.writeAttribute("onclick",
                            getLaunchOnclick(context, arc, component, bean),
                            null);
      // @todo Restore
      //      writer.writeURIAttribute("href", "#", null);
      writer.writeAttribute("href", "#", null);
    }

    writer.startElement("img", component);

    String id = _getColorSwatchId(arc);

    // Render the ID directly on the swatch image.
    writer.writeAttribute("id", id, null);

    // Render other image attrs
    writer.writeAttribute("align",
                          isScreenReaderMode(arc) ? "middle" : "absmiddle",
                          null);
    writer.writeAttribute("width", _CELL_SIZE, null);
    writer.writeAttribute("height", _CELL_SIZE, null);

    Color color = _getColorValue(bean);

    // Render the style attributes
    String inlineStyle = _getInlineStyleForColor(color);
    renderStyleClass(context, arc, XhtmlConstants.COLOR_FIELD_SWATCH_STYLE_CLASS);
    writer.writeAttribute("style", inlineStyle, null);

    Object altText = null;

    if (color != null && color.getAlpha() == 0)
    {
      Icon icon = arc.getIcon(XhtmlConstants.COLOR_PALETTE_TRANSPARENT_ICON_NAME);
      if (icon != null)
      {
        writer.writeAttribute("src", icon.getImageURI(context, arc), null);
      }

      String key = editable ?
                    "af_selectInputColor.LAUNCH_PICKER_TIP" :
                    "af_chooseColor.TRANSPARENT";
      altText = arc.getTranslatedString(key);
    }
    else
    {
      String transparentURI = getBaseImageUri(context, arc) + TRANSPARENT_GIF;
      writer.writeAttribute("src", transparentURI, null);

      if (editable)
        altText = arc.getTranslatedString("af_selectInputColor.LAUNCH_PICKER_TIP");
      else
        altText = "";
    }

    OutputUtils.renderAltAndTooltipForImage(context, arc, altText);

    writer.endElement("img");

    if (editable)
    {
      Icon overlay = arc.getIcon(XhtmlConstants.AF_SELECT_INPUT_COLOR_SWATCH_OVERLAY_ICON_NAME);
      if (overlay != null)
        OutputUtils.renderIcon(context, arc, overlay, "", "middle");
      writer.endElement("a");
    }
  }


  // @todo Should this deal with submittedValue, and String values?
  private Color _getColorValue(FacesBean bean)
  {
    Object value = getValue(bean);
    if (value instanceof Color)
      return ((Color) value);
    return null;
  }

  // Checks to see whether this is the first color field for
  // a given chooseId, and if so renders a script
  private void _renderFirstColorFieldScript(
    FacesContext        context,
    AdfRenderingContext arc,
    UIComponent         component,
    String              chooseId
    ) throws IOException
  {
    if (chooseId == null)
      return;

    if (!_supportsSwatchAndChooser(arc))
      return;

    if (arc.getFormData() == null)
      return;

    String id = getClientId(context, component);


    Map chooseColorIds = (Map)
      arc.getProperties().get(_CHOOSE_COLOR_IDS_KEY);

    if (chooseColorIds == null)
    {
      chooseColorIds = new HashMap();
      arc.getProperties().put(_CHOOSE_COLOR_IDS_KEY, chooseColorIds);
    }

    // The first dateField that is rendered for each inlineDatePicker
    // is the "active" dateField.  Check to see if we already
    // have an active dateField for this inlineDatePicker.
    if (chooseColorIds.get(chooseId) == null)
    {
      // We don't already have an active selectInputColor, so
      // this one is it.  Render the script to activate
      // this selectInputColor.
      ResponseWriter writer = context.getResponseWriter();
      writer.startElement("script", component);
      renderScriptTypeAttribute(context, arc);
      renderScriptDeferAttribute(context, arc);

      writer.writeText("_cfBus['", null);
      writer.writeText(chooseId, null);
      writer.writeText("']=document.forms['", null);
      writer.writeText(arc.getFormData().getName(), null);
      writer.writeText("']['", null);
      writer.writeText(id, null);
      writer.writeText("'];", null);
      writer.endElement("script");

      // Mark the ChooseColor as having its script
      chooseColorIds.put(chooseId, Boolean.TRUE);
    }
  }

  private static String _getInlineStyleForColor(Color color)
  {
    if (color != null && color.getAlpha() > 0)
      return "background-color:" + CSSUtils.getColorValue(color);

    return null;
  }

  private PropertyKey _chooseIdKey;
  private PropertyKey _compactKey;

  private static final String _COLOR_FIELD_LIB = "ColorField";

  static
  {
    ColorFieldInfoScriptlet.sharedInstance().registerSelf();
    (new AliasedScriptlet(_COLOR_FIELD_LIB, null,
                          new String[]{"openWindow()",
                                       "_getColorFieldFormat()",
                                       ColorFieldInfoScriptlet.COLOR_FIELD_INFO_KEY
                                       })).registerSelf();
  }


  private static final Integer _DEFAULT_COLUMNS = IntegerUtils.getInteger(11);
  // @todo 7 seems better than 11 - fix?
  //  private static final Integer _DEFAULT_COLUMNS = IntegerUtils.getInteger(7);

  // AdfRenderingContext property key for the Map which tracks whether
  // a ChooseColor id has been encountered
  private static final Object _CHOOSE_COLOR_IDS_KEY = new Object();

  private static final String _LAUNCH_PICKER_TIP_KEY =
    "af_selectInputColor.SELECT_PICKER_ALT";

  // Script for onblur auto-formatting
  private static final String _AUTO_SWATCH_SCRIPT = "_cfsw(this);";
  private static final String _AUTO_FORMAT_SCRIPT = "_fixCFF(this);";

  // Script for onfocus hookup with the chooser
  private static final String _FOCUS_PREFIX = "_cfBus['";
  private static final String _FOCUS_SUFFIX = "']=this;";

  private static final String _CELL_SIZE = "15";

  private static final Converter _DEFAULT_CONVERTER = new ColorConverter();

  // Key for remembering the cached chooseId
  private static final Object _CACHED_CHOOSE_ID = new Object();

  private static final ADFLogger _LOG =
    ADFLogger.createADFLogger(SimpleSelectInputColorRenderer.class);

}
