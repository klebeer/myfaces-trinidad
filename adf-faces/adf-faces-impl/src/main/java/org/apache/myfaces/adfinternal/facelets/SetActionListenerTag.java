/*
* Copyright 2006 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.myfaces.adfinternal.facelets;

import javax.el.ELException;
import javax.el.ValueExpression;
import javax.faces.component.ActionSource;
import javax.faces.component.UIComponent;

import org.apache.myfaces.adfinternal.taglib.listener.SetActionListener;

import com.sun.facelets.FaceletContext;
import com.sun.facelets.FaceletException;
import com.sun.facelets.el.LegacyValueBinding;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.TagConfig;
import com.sun.facelets.tag.TagHandler;
import com.sun.facelets.tag.jsf.ComponentSupport;

/**
 * @author Emmanuel Pirsch
 */
public class SetActionListenerTag extends TagHandler
{
	
  public SetActionListenerTag(TagConfig tagConfig)
  {
    super(tagConfig);
    _from = getRequiredAttribute("from");
    _to   = getRequiredAttribute("to");
  }
  
  public void apply(FaceletContext faceletContext,
                    UIComponent parent) throws FaceletException, ELException
  {
    if (ComponentSupport.isNew(parent))
    {
      // =-=AEW Couldn't this be cached?
      ValueExpression fromExpression = _from.getValueExpression(faceletContext,
                                                                Object.class);
      ValueExpression toExpression=  _to.getValueExpression(faceletContext,
                                                            Object.class);
      ActionSource actionSource= (ActionSource) parent;
      SetActionListener listener = new SetActionListener();
      listener.setValueBinding(listener.FROM_KEY,
                               new LegacyValueBinding(fromExpression));
      listener.setValueBinding(listener.TO_KEY,
                               new LegacyValueBinding(toExpression));
      actionSource.addActionListener(listener);
    }
  }

  private final TagAttribute _from;
  private final TagAttribute _to;
}

