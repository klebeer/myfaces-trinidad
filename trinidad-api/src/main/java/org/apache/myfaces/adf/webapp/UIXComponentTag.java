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
package org.apache.myfaces.adf.webapp;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.myfaces.adf.logging.ADFLogger;
import org.apache.myfaces.adf.bean.FacesBean;
import org.apache.myfaces.adf.bean.PropertyKey;
import org.apache.myfaces.adf.change.AddComponentChange;
import org.apache.myfaces.adf.change.AttributeComponentChange;
import org.apache.myfaces.adf.change.ComponentChange;
import org.apache.myfaces.adf.component.UIXComponent;
import org.apache.myfaces.adf.context.AdfFacesContext;
import org.apache.myfaces.adf.event.AttributeChangeEvent;

/**
 * Subclass of UIComponentTag to add convenience methods,
 * and optimize where appropriate.
 */
abstract public class UIXComponentTag extends UIComponentTag
{
  public UIXComponentTag()
  {
  }

  public void setId(String id)
  {
    _id = id;
  }

  public void setRendered(String rendered)
  {
    _rendered = rendered;
  }

  public void setAttributeChangeListener(String attributeChangeListener)
  {
    _attributeChangeListener = attributeChangeListener;
  }

  public int doStartTag() throws JspException
  {
    _parentELContext = (ELContextTag)
       TagSupport.findAncestorWithClass(this, ELContextTag.class);

    // Transform "rendered" on behalf of the UIComponentTag
    String rendered = _rendered;
    if (rendered != null)
    {
      if ((_parentELContext != null) && isValueReference(rendered))
        rendered = _parentELContext.transformExpression(rendered);

      super.setRendered(rendered);
    }


    String id = _id;
    if (id != null)
    {
      if (_parentELContext != null)
        id = _parentELContext.transformId(id);

      super.setId(id);
    }

    int retVal = super.doStartTag();

    //pu: There could have been some validation error during property setting
    //  on the bean, this is the closest opportunity to burst out.
    if (_validationError != null)
      throw new JspException(_validationError);

    return retVal;
  }

  public int doEndTag() throws JspException
  {
    UIComponent component = getComponentInstance();
    if (isSuppressed())
      _applyChanges(getFacesContext(), component, getCreated());
    return super.doEndTag();
  }

  protected void encodeBegin() throws java.io.IOException
  {
    UIComponent component = getComponentInstance();
    if (!isSuppressed())
      _applyChanges(getFacesContext(), component, getCreated());
    super.encodeBegin();
  }

  protected final void setProperties(UIComponent component)
  {
    super.setProperties(component);

    UIXComponent uixComponent = (UIXComponent) component;

    if (_attributeChangeListener != null)
    {
      MethodBinding mb =
        createMethodBinding(_attributeChangeListener,
                            new Class[]{AttributeChangeEvent.class});
      uixComponent.setAttributeChangeListener(mb);
    }


    setProperties(uixComponent.getFacesBean());
  }

  protected void setProperty(
    FacesBean   bean,
    PropertyKey key,
    String      value)
  {
    if (value == null)
      return;

    if (isValueReference(value))
    {
      bean.setValueBinding(key, createValueBinding(value));
    }
    else
    {
      bean.setProperty(key, value);
    }
  }

  /**
   * Set a property of type java.lang.Boolean.  If the value
   * is an EL expression, it will be stored as a ValueBinding.
   * Otherwise, it will parsed with Integer.valueOf().
   * Null values are ignored.
   */
  protected void setBooleanProperty(
    FacesBean   bean,
    PropertyKey key,
    String      value)
  {
    if (value == null)
      return;

    if (isValueReference(value))
    {
      bean.setValueBinding(key, createValueBinding(value));
    }
    else
    {
      bean.setProperty(key, Boolean.valueOf(value));
    }
  }


  /**
   * Set a property of type java.lang.Integer.  If the value
   * is an EL expression, it will be stored as a ValueBinding.
   * Otherwise, it will parsed with Integer.valueOf().
   * Null values are ignored.
   */
  protected void setIntegerProperty(
    FacesBean   bean,
    PropertyKey key,
    String      value)
  {
    if (value == null)
      return;

    if (isValueReference(value))
    {
      bean.setValueBinding(key, createValueBinding(value));
    }
    else
    {
      bean.setProperty(key, Integer.valueOf(value));
    }
  }


  /**
   * Set a property of type java.lang.Character.  If the value
   * is an EL expression, it will be stored as a ValueBinding.
   * Otherwise, its first character will be stored (unless
   * it is an empty string, in which case it will be ignored).
   * Null values are ignored.
   */
  protected void setCharacterProperty(
    FacesBean   bean,
    PropertyKey key,
    String      value)
  {
    if (value == null)
      return;

    if (isValueReference(value))
    {
      bean.setValueBinding(key, createValueBinding(value));
    }
    else
    {
      if (value.length() >= 1)
        bean.setProperty(key, new Character(value.charAt(0)));
    }
  }




  /**
   * Set a property of type java.lang.Long.  If the value
   * is an EL expression, it will be stored as a ValueBinding.
   * Otherwise, it will parsed with Long.valueOf().
   * Null values are ignored.
   */
  protected void setLongProperty(
    FacesBean   bean,
    PropertyKey key,
    String      value)
  {
    if (value == null)
      return;

    if (isValueReference(value))
    {
      bean.setValueBinding(key, createValueBinding(value));
    }
    else
    {
      bean.setProperty(key, Long.valueOf(value));
    }
  }



  /**
   * Set a property of type java.lang.String[].  If the value
   * is an EL expression, it will be stored as a ValueBinding.
   * Otherwise, it will parsed as a whitespace-separated series
   * of strings.
   * Null values are ignored.
   */
  protected void setStringArrayProperty(
    FacesBean   bean,
    PropertyKey key,
    String      value)
  {
    if (value == null)
      return;

    if (isValueReference(value))
    {
      bean.setValueBinding(key, createValueBinding(value));
    }
    else
    {
      bean.setProperty(key, _parseNameTokens(value));
    }
  }

  /**
   * Set a property of type int[].  If the value
   * is an EL expression, it will be stored as a ValueBinding.
   * Otherwise, it will parsed as a whitespace-separated series
   * of ints.
   * Null values are ignored.
   */
  protected void setIntArrayProperty(
    FacesBean   bean,
    PropertyKey key,
    String      value)
  {
    if (value == null)
      return;

    if (isValueReference(value))
    {
      bean.setValueBinding(key, createValueBinding(value));
    }
    else
    {
      String[] strings = _parseNameTokens(value);
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
          _LOG.severe("Could not convert:"+value+" into int[]", e);
          return;
        }
      }
      else
        ints = null;

      bean.setProperty(key, ints);
    }
  }

  /**
   * Set a property of type java.util.Date.  If the value
   * is an EL expression, it will be stored as a ValueBinding.
   * Otherwise, it will parsed as an ISO 8601 date (yyyy-MM-dd).
   * Null values are ignored.
   */
  protected void setDateProperty(
    FacesBean   bean,
    PropertyKey key,
    String      value)
  {
    if (value == null)
      return;

    if (isValueReference(value))
    {
      bean.setValueBinding(key, createValueBinding(value));
    }
    else
    {
      bean.setProperty(key, _parseISODate(value));
    }
  }


  /**
   * @todo Handle syntax exceptions gracefully?
   */
  protected final ValueBinding createValueBinding(String string)
  {
    if (_parentELContext != null)
      string = _parentELContext.transformExpression(string);

    return getFacesContext().getApplication().createValueBinding(string);
  }

  /**
   * @todo Handle syntax exceptions gracefully?
   */
  protected final MethodBinding createMethodBinding(
    String   string,
    Class [] types)
  {
    if (_parentELContext != null)
      string = _parentELContext.transformExpression(string);

    return getFacesContext().getApplication().createMethodBinding(string,
                                                                  types);
  }

  protected void setProperties(FacesBean bean)
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
   * Parse a string into a java.util.Date object.  The
   * string must be in ISO 9601 format (yyyy-MM-dd).
   */
  static private final Date _parseISODate(String stringValue)
  {
    try
    {
      return _ISO_DATE_FORMAT.parse(stringValue);
    }
    catch (ParseException pe)
    {
      _LOG.info("Could not parse value {0} into a Date using " +
                "pattern \"yyyy-MM-dd\";  ignoring.", stringValue);
      return null;
    }
  }

  /**
   * Parses a whitespace separated series of name tokens.
   * @param stringValue the full string
   * @return an array of each constituent value, or null
   *  if there are no tokens (that is, the string is empty or
   *  all whitespace)
   * @todo Move to utility function somewhere (ADF Share?)
   */
  static private final String[] _parseNameTokens(String stringValue)
  {
    if (stringValue == null)
      return null;

    ArrayList list = new ArrayList(5);

    int     length = stringValue.length();
    boolean inSpace = true;
    int     start = 0;
    for (int i = 0; i < length; i++)
    {
      char ch = stringValue.charAt(i);

      // We're in whitespace;  if we've just departed
      // a run of non-whitespace, append a string.
      // Now, why do we use the supposedly deprecated "Character.isSpace()"
      // function instead of "isWhitespace"?  We're following XML rules
      // here for the meaning of whitespace, which specifically
      // EXCLUDES general Unicode spaces.
      if (Character.isWhitespace(ch))
      {
        if (!inSpace)
        {
          list.add(stringValue.substring(start, i));
          inSpace = true;
        }
      }
      // We're out of whitespace;  if we've just departed
      // a run of whitespace, start keeping track of this string
      else
      {
        if (inSpace)
        {
          start = i;
          inSpace = false;
        }
      }
    }

    if (!inSpace)
      list.add(stringValue.substring(start));

    if (list.isEmpty())
      return null;

    return (String[]) list.toArray(new String[list.size()]);
  }

  private static void _applyChanges(
    FacesContext facesContext,
    UIComponent uiComponent,
    boolean isCreated)
  {
    AdfFacesContext afc = AdfFacesContext.getCurrentInstance();
    Iterator changeIter =
                  afc.getChangeManager().getComponentChanges(facesContext, uiComponent);

    if (changeIter == null)
      return;
    while (changeIter.hasNext())
    {
      ComponentChange change = (ComponentChange)changeIter.next();

      //pu: If we did not create the component during tag execution, do not
      //  apply any AttributeChange. This is because we do not have enough
      //  mechanism to take care of such cases for now. Users could always apply
      //  such Changes explicitly in their backing bean after creating component.
      boolean isChangeApplicable =
        ( (change instanceof AttributeComponentChange) && !isCreated) ? false:true;

      if (isChangeApplicable)
      {
        change.changeComponent(uiComponent);
      }

      //pu: In case this Change has added a new component/facet, the added
      //  component could have its own Changes, that may need to be applied here.
      if (change instanceof AddComponentChange)
      {
        UIComponent newAddedComponent =
          ( (AddComponentChange)change ).getComponent();

        if (newAddedComponent != null)
        {
          _applyChanges(facesContext, newAddedComponent, true);
        }
      }
    }
  }

  private String       _rendered;
  private String       _id;
  private String       _attributeChangeListener;
  private String       _validationError;
  private ELContextTag _parentELContext;

  // We rely strictly on ISO 8601 formats
  private static DateFormat  _ISO_DATE_FORMAT =
    new SimpleDateFormat("yyyy-MM-dd");

  static private final ADFLogger _LOG = ADFLogger.createADFLogger(UIXComponentTag.class);
}
