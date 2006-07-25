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
package org.apache.myfaces.adfdemo;

import javax.faces.component.UIComponent;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;

import org.apache.myfaces.adf.component.UIXOutput;
import org.apache.myfaces.adf.context.AdfFacesContext;

public class PartialDemoUtilBean
{
  public void action(ActionEvent action)
  {
    // Just update the string which says when the last update was.
    _status.setLinkUpdate();
  }

  // This is called for the resetButton
  public void reset(ActionEvent action)
  {
    _status.reset();
    _resetList();
  }

  public void valueChanged(ValueChangeEvent vce)
  {
    Object newValue = vce.getNewValue();
    UIComponent component = vce.getComponent();

    String rendererType = component.getRendererType();

    // For these first components the listeners have registered themselves
    // by setting the partialTriggers attribute. So we just update the model.
    if (rendererType.equals("org.apache.myfaces.adf.Checkbox"))
    {
      _status.setChecked((Boolean) newValue);
      _status.incrementCheckBoxUpdateCount();
    }
    else if (rendererType.equals("org.apache.myfaces.adf.Radio"))
    {
      if (Boolean.TRUE.equals(newValue))
      {
        String text = (String) component.getAttributes().get("text");
        _status.setSelectBooleanState(text);
      }
      else if (newValue instanceof String)
        _status.setSelectOneState((String) newValue);
    }
    else if (rendererType.equals("org.apache.myfaces.adf.Text"))
    {
      if (newValue instanceof String)
        _status.setTextValue((String) newValue);
    }
    else if (rendererType.equals("org.apache.myfaces.adf.Choice"))
    {
      if (newValue instanceof String)
        _status.setChoiceInt((String) newValue);
    }

    // This component illustrates a method of dynamically adding a
    // partialTarget (i.e. without setting the partialTriggers attribute). It
    // updates a component binding and adds the updated component directly to
    // the list of partial targets.
    else if (rendererType.equals("org.apache.myfaces.adf.Listbox"))
    {
      _listUpdate.setValue(component.getAttributes().get("value"));
      _addTarget(_listUpdate);
    }
  }

  public UIXOutput getListUpdate()
  {
    return _listUpdate;
  }

  public void setListUpdate(UIXOutput listUpdate)
  {
    _listUpdate = listUpdate;
  }

  public PartialDemoStatusBean getStatus()
  {
    return _status;
  }

  public void setStatus(PartialDemoStatusBean status)
  {
    _status = status;
  }

  private void _resetList()
  {
    _listUpdate.setValue("nothing yet.");
    _addTarget(_listUpdate);
  }

  private void _addTarget(UIComponent target)
  {
    AdfFacesContext adfContext = AdfFacesContext.getCurrentInstance();
    adfContext.addPartialTarget(target);
  }

  private PartialDemoStatusBean _status;
  private UIXOutput _listUpdate;
}
