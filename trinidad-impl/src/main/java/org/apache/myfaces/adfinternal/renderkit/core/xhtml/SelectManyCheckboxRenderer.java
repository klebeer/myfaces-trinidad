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

import org.apache.myfaces.adf.bean.FacesBean;
import org.apache.myfaces.adf.bean.PropertyKey;
import org.apache.myfaces.adf.component.core.input.CoreSelectManyCheckbox;


public class SelectManyCheckboxRenderer extends InputLabelAndMessageRenderer
{

  public SelectManyCheckboxRenderer()
  {
    super(CoreSelectManyCheckbox.TYPE);
  }
  
  protected void findTypeConstants(FacesBean.Type type)
  {
    super.findTypeConstants(type);
    _layoutKey = type.findKey("layout");
  } 
  
  protected String getRootStyleClass(FacesBean bean)  
  {
    return "af|selectManyCheckbox";
  }
  
  protected String getLayout(FacesBean bean)
  {
    return toString(bean.getProperty(_layoutKey));
  }

  protected String getDefaultLabelValign(FacesBean bean)
  {
    // Don't top-align for horizontal radio buttons.
    if (CoreSelectManyCheckbox.LAYOUT_HORIZONTAL.equals(getLayout(bean)))
      return super.getDefaultLabelValign(bean);

    return "top";
  }

  /**
   * selectManyCheckbox should not render a &lt;label&gt; on itself.
   */ 
  protected boolean hasOwnLabel(FacesBean bean)
  {
    return true;
  }
  
  protected boolean showAccessKeyOnLabel(FacesBean bean)
  {
    return true;
  }

  protected FormInputRenderer getFormInputRenderer()
  {
    return _simpleSelectManyCheckbox;
  }  

  private PropertyKey _layoutKey;

  private SimpleSelectManyCheckboxRenderer _simpleSelectManyCheckbox =
     new SimpleSelectManyCheckboxRenderer();
}
