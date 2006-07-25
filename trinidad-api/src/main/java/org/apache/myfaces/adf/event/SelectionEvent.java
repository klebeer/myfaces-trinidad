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

package org.apache.myfaces.adf.event;
import javax.faces.component.UIComponent;
import javax.faces.event.FacesListener;
import org.apache.myfaces.adf.model.RowKeySet;

/**
 * Event that is generated when the selection of a component changes.
 * @author The Oracle ADF Faces Team
 */
public class SelectionEvent extends RowKeySetChangeEvent
{
  /**
   * Creates a new SelectionEvent
   * @param unselected the set of rowKeys that have just been unselected.
   * @param selected the set of rowKeys that have just been selected.
   */
  public SelectionEvent(UIComponent source, RowKeySet unselected, RowKeySet selected)
  {
    super(source, unselected, selected);
  }

  /**
   * Creates a new SelectionEvent
   * @param oldSet the set of rowKeys before any changes.
   * @param newSet the set of rowKeys after any changes.
   */
  public SelectionEvent(RowKeySet oldSet, RowKeySet newSet, UIComponent source)
  {
    super(oldSet, newSet, source);
  }

  public void processListener(FacesListener listener)
  {
    ((SelectionListener) listener).processSelection(this);
  }

  public boolean isAppropriateListener(FacesListener listener)
  {
    return (listener instanceof SelectionListener);
  }  
}
