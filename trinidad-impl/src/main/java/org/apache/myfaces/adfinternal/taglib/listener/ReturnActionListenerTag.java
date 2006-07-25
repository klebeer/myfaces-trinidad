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
package org.apache.myfaces.adfinternal.taglib.listener;

import org.apache.myfaces.adfinternal.taglib.util.TagUtils;

import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspException;

import javax.faces.application.Application;
import javax.faces.component.ActionSource;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.webapp.UIComponentTag;

import org.apache.myfaces.adf.webapp.ELContextTag;


/**
 * creates a ReturnActionListener on the parent ActionSource component.
 * @author The Oracle ADF Faces Team
 */
public class ReturnActionListenerTag extends TagSupport
{
  public void setValue(String value)
  {
    _value = value;
  }


  public int doStartTag() throws JspException
  {
    UIComponentTag tag = UIComponentTag.getParentUIComponentTag(pageContext);
    if (tag == null)
    {
      throw new JspException(
        "returnActionListener must be inside of a UIComponent tag.");
    }

    // Only run on the first time the tag executes
    if (!tag.getCreated())
      return SKIP_BODY;

    UIComponent component = tag.getComponentInstance();
    if (!(component instanceof ActionSource))
    {
      throw new JspException(
        "returnActionListener must be inside of an ActionSource component.");
    }

    ELContextTag parentELContext = (ELContextTag)
       findAncestorWithClass(this, ELContextTag.class);

    Application application =
      FacesContext.getCurrentInstance().getApplication();

    ReturnActionListener listener = new ReturnActionListener();
    if (_value != null)
    {
      String value = _value;
      if (TagUtils.isValueReference(value))
      {
        if (parentELContext != null)
          value = parentELContext.transformExpression(value);

        listener.setValueBinding(ReturnActionListener.VALUE_KEY,
                                 application.createValueBinding(value));
      }
      else
      {
        listener.setValue(value);
      }
    }
    ((ActionSource) component).addActionListener(listener);

    return super.doStartTag();
  }

  public void release()
  {
    super.release();
    _value = null;
  }

  private String _value;
}
