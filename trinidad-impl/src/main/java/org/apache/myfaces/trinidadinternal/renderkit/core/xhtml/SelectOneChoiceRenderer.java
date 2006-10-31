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

import org.apache.myfaces.trinidad.bean.FacesBean;
import org.apache.myfaces.trinidad.component.core.input.CoreSelectOneChoice;


public class SelectOneChoiceRenderer extends InputLabelAndMessageRenderer
{

  public SelectOneChoiceRenderer()
  {
    super(CoreSelectOneChoice.TYPE);
    
  }  

  @Override
  protected void findTypeConstants(FacesBean.Type type)
  {
    super.findTypeConstants(type);
    _simpleSelectOneChoice = new SimpleSelectOneChoiceRenderer(type);
  }
  
  @Override
  protected String getRootStyleClass(FacesBean bean)  
  {
    return "af|selectOneChoice";
  }
  
  @Override
  protected FormInputRenderer getFormInputRenderer()
  {
    return _simpleSelectOneChoice;
  }  

  private SimpleSelectOneChoiceRenderer _simpleSelectOneChoice;
}
