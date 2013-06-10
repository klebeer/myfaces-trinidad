/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.trinidad.webapp;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

import javax.el.MethodExpression;
import javax.el.ValueExpression;

import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.webapp.UIComponentClassicTagBase;
import javax.faces.webapp.UIComponentELTag;

import javax.servlet.jsp.JspException;

import org.apache.myfaces.trinidad.bean.FacesBean;
import org.apache.myfaces.trinidad.bean.PropertyKey;
import org.apache.myfaces.trinidad.component.UIXComponent;
import org.apache.myfaces.trinidad.context.RequestContext;
import org.apache.myfaces.trinidad.logging.TrinidadLogger;


/**
 * Subclass of UIComponentTag to add convenience methods,
 * and optimize where appropriate.
 */
abstract public class UIXComponentELTag extends UIComponentELTag
{
  /**
   * @see #checkChildTagExecution
   */
  protected enum CheckExecutionResult
  {
    /** Execute the child tag and its body */
    ACCEPT,

    /** Skip both the creation of the component, and do not execute the child's body */
    REJECT
  }

  public UIXComponentELTag()
  {
  }

  public void setAttributeChangeListener(MethodExpression attributeChangeListener)
  {
    _attributeChangeListener = attributeChangeListener;
  }

  @Override
  public int doStartTag() throws JspException
  {
    Map<Object, Object> facesContextAttributes = getFacesContext().getAttributes();

    // Only support skipping the body of a tag when not iterating. This is due to the fact that
    // skipping tag execution usually relies on component attribute state. Since stamping causes
    // component state to change per-stamp, it is not reliable enough to try to determine if
    // a component tag should process its body based on the non-stamped component state. As such,
    // we always execute the tag body when inside a stamping component tag
    if (!_isProcessingStampingComponentTag(facesContextAttributes))
    {
      UIComponentClassicTagBase parentTagBase = getParentUIComponentClassicTagBase(pageContext);
      if (parentTagBase instanceof UIXComponentELTag)
      {
        UIXComponentELTag parentTag = (UIXComponentELTag)parentTagBase;
        String facetName = getFacetName();

        // Check if the component should be created
        if (parentTag.checkChildTagExecution(this, facetName) ==
          CheckExecutionResult.REJECT)
        {
          _skipEndTagSuperCall = true;
          return SKIP_BODY;
        }
      }
    }

    _skipEndTagSuperCall = false;
    int retVal = super.doStartTag();

    // There could have been some validation error during property setting
    // on the bean, this is the closest opportunity to burst out.
    if (_validationError != null)
      throw new JspException(_validationError);

    _checkStartingStampingTag(facesContextAttributes);

    return retVal;
  }

  @Override
  public int doEndTag()
    throws JspException
  {
    if (_skipEndTagSuperCall)
    {
      _skipEndTagSuperCall = false;
      return EVAL_PAGE;
    }
    else
    {
      _checkEndingStampingTag();
      return super.doEndTag();
    }
  }

  /**
   * Check if a tag that creates components that stamp their children (tag that creates a component
   * that stamps out its contents, potentially rendering the children multiple times)
   * is currently executing (between doStartTag and doEndTag).
   */
  protected boolean isProcessingStampingComponentTag()
  {
    return _isProcessingStampingComponentTag(getFacesContext().getAttributes());
  }

  /**
   * Check if this tag is a stamping tag (tag that creates a component that stamps out its
   * contents, potentially rendering the children multiple times).
   */
  protected boolean isStampingTag()
  {
    return false;
  }

  /**
   * Check if a child component tag should execute or not. Allows the parent tag to control if
   * child components are created for optimization purposes. Only called if the current tag is
   * not in a stamping tag.
   * <p>
   *   Called from the doStartTag of the child tag to see if the parent tag wishes to prevent
   *   the execution of the child tag. This is called before the child tag creates its component.
   * </p>
   * <p>
   *   This may be overridden by a tag to check if a child tag's body should be executed. The
   *   framework will call this method when the child tag is executing.
   * </p>
   * <p>
   *   If inside of a stamping container this code is not executed as component state
   *   may change per stamp and therefore the tag will not have access to that state since the
   *   component does not stamp during tag execution. Therefore, it is best to avoid trying to
   *   defer child execution when component state is not known.
   * </p>
   * <p>
   *   This method is called by the framework where the {@link #checkChildTagExecution(UIComponent)}
   *   function is called by sub-classes of {@link UIXComponentELTag}.
   * </p>
   *
   * @param childTag The child tag
   * @param facetName The facet, if any, for the child tag
   * @return if the child tag body should be executed or not
   */
  protected CheckExecutionResult checkChildTagExecution(
    @SuppressWarnings("unused") UIComponentELTag childTag,
    @SuppressWarnings("unused") String           facetName)
  {
    return CheckExecutionResult.ACCEPT;
  }

  /**
   * Allows a child tag to check if its children should be executed based on the grand-parent.
   * Used for components where a parent-child relationship has been established. For example,
   * allows the show detail item to ask the parent panelTabbed tag if the show detail item should
   * allow children components of the show detail item to be created.
   * <p>
   *   This method is not called by the framework, but may be called by a child tag on the
   *   parent tag. The parent tag should override this method to determine if a child tag should
   *   execute its children tags. In the above example, the show detail item tag should call this
   *   method on the panelTabbed tag to see if the show detail item's children tags should be
   *   executed.
   * </p>
   * <p>
   *   This method is called by sub-classes and is not called by the framework at any time.
   *   The framework will invoke the {@link #checkChildTagExecution(UIComponentELTag, String)}
   *   method during the execution of the start tag.
   * </p>
   *
   * @param childComponent The child component
   * @return if the children tags of the child component should execute their tag bodies
   */
  public CheckExecutionResult checkChildTagExecution(
    @SuppressWarnings("unused") UIComponent childComponent)
  {
    return CheckExecutionResult.ACCEPT;
  }

  protected final void setProperties(UIComponent component)
  {
    if (component instanceof UIViewRoot)
    {
      throw new IllegalStateException(
         "<f:view> was not present on this page; tag " + this +
         "encountered without an <f:view> being processed.");
    }

    super.setProperties(component);

    UIXComponent uixComponent = (UIXComponent) component;

    if (_attributeChangeListener != null)
    {
      uixComponent.setAttributeChangeListener(_attributeChangeListener);
    }

    setProperties(uixComponent.getFacesBean());
  }

  protected void setProperty(
    FacesBean   bean,
    PropertyKey key,
    ValueExpression expression)
  {
    if (expression == null)
      return;

    if (expression.isLiteralText())
    {
      bean.setProperty(key, expression.getValue(null));
    }
    else
    {
      bean.setValueExpression(key, expression);
    }
  }

  /**
   * Set a property of type java.lang.String[].  If the value
   * is an EL expression, it will be stored as a ValueExpression.
   * Otherwise, it will parsed as a whitespace-separated series
   * of strings.
   * Null values are ignored.
   */
  protected void setStringArrayProperty(
    FacesBean       bean,
    PropertyKey     key,
    ValueExpression expression)
  {
    if (expression == null)
      return;

    if (expression.isLiteralText())
    {
      bean.setProperty(key, TagUtils.parseNameTokens(expression.getValue(null)));
    }
    else
    {
      // Support coercion from a string to a string array
      expression = new StringArrayValueExpression(expression);
      bean.setValueExpression(key, expression);
    }
  }

  /**
   * Set a property of type java.util.List&lt;java.lang.String>.  If the value
   * is an EL expression, it will be stored as a ValueExpression.
   * Otherwise, it will parsed as a whitespace-separated series
   * of strings.
   * Null values are ignored.
   */
  protected void setStringListProperty(
    FacesBean       bean,
    PropertyKey     key,
    ValueExpression expression)
  {
    if (expression == null)
      return;

    if (expression.isLiteralText())
    {
      bean.setProperty(key, TagUtils.parseNameTokensAsList(expression.getValue(null)));
    }
    else
    {
      bean.setValueExpression(key, expression);
    }
  }

  /**
   * Set a property of type java.util.Set&lt;java.lang.String>.  If the value
   * is an EL expression, it will be stored as a ValueExpression.
   * Otherwise, it will parsed as a whitespace-separated series
   * of strings.
   * Null values are ignored.
   */
  protected void setStringSetProperty(
    FacesBean       bean,
    PropertyKey     key,
    ValueExpression expression)
  {
    if (expression == null)
      return;

    if (expression.isLiteralText())
    {
      bean.setProperty(key, TagUtils.parseNameTokensAsSet(expression.getValue(null)));
    }
    else
    {
      bean.setValueExpression(key, expression);
    }
  }

  /**
   * Set a property of type java.lang.Number.  If the value
   * is an EL expression, it will be stored as a ValueBinding.
   * Otherwise, it will parsed with Integer.valueOf() or Double.valueOf() .
   * Null values are ignored.
   */
  protected void setNumberProperty(
    FacesBean   bean,
    PropertyKey key,
    ValueExpression expression)
  {
    if (expression == null)
      return;

    if (expression.isLiteralText())
    {
      Object value = expression.getValue(null);
      if (value != null)
      {
        if (value instanceof Number)
        {
          bean.setProperty(key, value);
        }
        else
        {
          String valueStr = value.toString();
          if(valueStr.indexOf('.') == -1)
            bean.setProperty(key, Integer.valueOf(valueStr));
          else
            bean.setProperty(key, Double.valueOf(valueStr));
        }
      }
    }
    else
    {
      bean.setValueExpression(key, expression);
    }
  }

  /**
   * Set a property of type int[].  If the value
   * is an EL expression, it will be stored as a ValueExpression.
   * Otherwise, it will parsed as a whitespace-separated series
   * of ints.
   * Null values are ignored.
   */
  protected void setIntArrayProperty(
    FacesBean   bean,
    PropertyKey key,
    ValueExpression expression)
  {
    if (expression == null)
      return;

    if (expression.isLiteralText())
    {
      Object value = expression.getValue(null);
      if (value != null)
      {
        String[] strings = TagUtils.parseNameTokens(value);
        final int[] ints;
        if (strings != null)
        {
          try
          {
            ints = new int[strings.length];
            for(int i=0; i<strings.length; i++)
            {
              int j = Integer.parseInt(strings[i]);
              ints[i] = j;
            }
          }
          catch (NumberFormatException e)
          {
            _LOG.severe("CANNOT_CONVERT_INTO_INT_ARRAY",value);
            _LOG.severe(e);
            return;
          }
        }
      }
    }
    else
    {
      bean.setValueExpression(key, expression);
    }
  }

  /**
   * Set a property of type java.util.Date.  If the value
   * is an EL expression, it will be stored as a ValueExpression.
   * Otherwise, it will parsed as an ISO 8601 date (yyyy-MM-dd).
   * Null values are ignored.
   */
  protected void setDateProperty(
    FacesBean   bean,
    PropertyKey key,
    ValueExpression expression)
  {
    if (expression == null)
      return;

    if (expression.isLiteralText())
    {
      bean.setProperty(key, _parseISODate(expression.getValue(null)));
    }
    else
    {
      bean.setValueExpression(key, expression);
    }
  }

  /**
   * Set a property of type java.util.Date.  If the value
   * is an EL expression, it will be stored as a ValueBinding.
   * Otherwise, it will parsed as an ISO 8601 date (yyyy-MM-dd)
   * and the time components (hour, min, second, millisecond) maximized.
   * Null values are ignored.
   */
    protected void setMaxDateProperty(
    FacesBean   bean,
    PropertyKey key,
    ValueExpression expression)
  {
    if (expression == null)
      return;

    if (expression.isLiteralText())
    {
      Date d = _parseISODate(expression.getValue(null));
      Calendar c = Calendar.getInstance();
      TimeZone tz = RequestContext.getCurrentInstance().getTimeZone();
      if (tz != null)
        c.setTimeZone(tz);
      c.setTime(d);
      // Original value had 00:00:00 for hours,mins, seconds now maximize those
      // to get the latest time value for the date supplied.
      c.set (Calendar.HOUR_OF_DAY, 23);
      c.set (Calendar.MINUTE, 59);
      c.set (Calendar.SECOND, 59);
      c.set (Calendar.MILLISECOND, 999);
      bean.setProperty(key, c.getTime());
    }
    else
    {
      bean.setValueExpression(key, expression);
    }
  }

  protected void setProperties(
    @SuppressWarnings("unused")
    FacesBean bean)
  {
    // Could be abstract, but it's easier to *always* call super.setProperties(),
    // and perhaps we'll have something generic in here, esp. if we take
    // over "rendered" from UIComponentTag
  }

  /**
   * Sets any fatal validation error that could have happened during property
   *  setting. If this is set, tag execution aborts with a JspException at the
   *  end of doStartTag().
   * @param validationError
   */
  protected void setValidationError(String validationError)
  {
    _validationError = validationError;
  }

  /**
   * Checks if the current tag is a stamping tag. If so, a counter is incremented so that it can
   * be tracked when we are outside all stamping tags again.
   *
   * @see #_checkEndingStampingTag()
   * @see #isStampingTag()
   * @param facesContextAttributes
   */
  private void _checkStartingStampingTag(
    Map<Object, Object> facesContextAttributes)
  {
    if (isStampingTag())
    {
      AtomicInteger count = (AtomicInteger)facesContextAttributes.get(_STAMPING_COUNT_KEY);
      if (count == null)
      {
        // Use an atomic integer here so that we can increment and decrement the value without
        // having to store a new integer value into the map each time. This avoids the overhead
        // of the map.put operation.
        facesContextAttributes.put(_STAMPING_COUNT_KEY, new AtomicInteger(1));
      }
      else
      {
        // Only used on one thread, so use the safe methods for performance (only using the
        // atomic integer for higher performance than boxing int to Integer)
        count.set(count.get() + 1);
      }
    }
  }

  /**
   * Decrement the counter if this tag returns true from {@link #isStampingTag()}. Allows the code
   * to check if we are outside of all stamping component tags.
   *
   * @see #_checkStartingStampingTag(Map)
   */
  private void _checkEndingStampingTag()
  {
    if (isStampingTag())
    {
      Map<Object, Object> facesContextAttributes = getFacesContext().getAttributes();
      AtomicInteger count = (AtomicInteger)facesContextAttributes.get(_STAMPING_COUNT_KEY);
      if (count.get() == 1)
      {
        facesContextAttributes.remove(_STAMPING_COUNT_KEY);
      }
      else
      {
        count.set(count.get() - 1);
      }
    }
  }

  /**
   * Check if an iterating tag is currently executing (between doStartTag and doEndTag).
   * This is used to always process the tag body when stamping. This prevents issues when component
   * state is used to determine if a tag should execute and that state differs per stamp. Since
   * stamp state is not available during JSP tag execution, it is best to always execute the tag
   * body when inside a stamping tag.
   */
  private boolean _isProcessingStampingComponentTag(
    Map<Object, Object> facesContextAttributes)
  {
    return facesContextAttributes.containsKey(_STAMPING_COUNT_KEY);
  }

  /**
   * Parse a string into a java.util.Date object.  The
   * string must be in ISO 9601 format (yyyy-MM-dd).
   */
  static private final Date _parseISODate(Object o)
  {
    if (o == null)
      return null;

    String stringValue = o.toString();
    try
    {
      return _getDateFormat().parse(stringValue);
    }
    catch (ParseException pe)
    {
      _LOG.info("CANNOT_PARSE_VALUE_INTO_DATE", stringValue);
      return null;
    }
  }

  // We rely strictly on ISO 8601 formats
  private static DateFormat _getDateFormat()
  {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    TimeZone tz = RequestContext.getCurrentInstance().getTimeZone();
    if (tz != null)
      sdf.setTimeZone(tz);
    return sdf;
  }

  private static final TrinidadLogger _LOG =
    TrinidadLogger.createTrinidadLogger(UIXComponentELTag.class);

  /** @deprecated Not used any more in the session state manager */
  @Deprecated
  public static final String DOCUMENT_CREATED_KEY = "org.apache.myfaces.trinidad.DOCUMENTCREATED";

  private final static String _STAMPING_COUNT_KEY = UIXComponentELTag.class.getName() + ".STAMPING";

  private MethodExpression _attributeChangeListener;
  private String           _validationError;
  private boolean          _skipEndTagSuperCall = false;
}
