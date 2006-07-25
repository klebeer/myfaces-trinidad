/*
 * Copyright  2001-2006 The Apache Software Foundation.
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
package org.apache.myfaces.adfinternal.ui.laf.base.xhtml;

import java.awt.Color;
import org.apache.myfaces.adfinternal.ui.laf.base.Icon;
import org.apache.myfaces.adfinternal.ui.laf.base.IconKey;

import org.apache.myfaces.adfinternal.image.ImageContext;
import org.apache.myfaces.adfinternal.image.ImageProviderRequest;

import org.apache.myfaces.adfinternal.share.io.NameResolver;
import org.apache.myfaces.adfinternal.ui.RenderingContext;

/**
 * Abstracts out the retrieval of ImageProviderResponses for
 * dual ramp colorization.
 * <p>
 * Icons are kept as array and index is accessed using
 * the iconIndex provided in IconKey.
 * 
 * @version $Name:  $ ($Revision: adfrt/faces/adf-faces-impl/src/main/java/oracle/adfinternal/view/faces/ui/laf/base/xhtml/IconArrayLafIconProvider.java#0 $) $Date: 10-nov-2005.18:53:56 $
 * @author The Oracle ADF Faces Team
 */
public final class IconArrayLafIconProvider extends AccentedLafIconProvider
{
  /**
  * @param iconInfo array containing Icons and their corresponding IconKeys.
  * Array should contain icon1key,icon1, icon2key, icon2, etc.
  * @see Icon
  * @see IconKey
  */
  public IconArrayLafIconProvider(
    Object[] iconInfo
    )
  {
    int maxIndex = IconKey.getKeyCount();
    _icons = new Icon[maxIndex];

    _setIcons( iconInfo );

  }

 /**
  * @param iconProvider another IconArrayLafIconProvider, the contents
  *     of which is copied.
  * @param iconInfo array containing Icons and their corresponding IconKeys.
  *     These take precedence over icons copied from the iconProvider parameter.
  *     Array should contain icon1key,icon1, icon2key, icon2, etc.
  * @see Icon
  * @see IconKey
  */
  public IconArrayLafIconProvider(
    IconArrayLafIconProvider iconProvider,
    Object[] iconInfo

    )
  {
    int maxIndex = IconKey.getKeyCount();
    _icons = new Icon[maxIndex];

    // copy the icon array of the iconProvider passed in
    System.arraycopy(iconProvider._icons,
                     0,
                     _icons,
                     0 ,
                     iconProvider._icons.length);

    // override these with the icons passed in.
    _setIcons( iconInfo );

  }

 /**
  * @param iconProvider another IconArrayLafIconProvider, the contents
  *     of which is copied.
  */
  public IconArrayLafIconProvider(
    IconArrayLafIconProvider iconProvider
    )
  {
    int maxIndex = iconProvider._icons.length;
    _icons = new Icon[maxIndex];

    
    // copy the icon array of the iconProvider passed in
    System.arraycopy(iconProvider._icons,
                     0,
                     _icons,
                     0 ,
                     maxIndex);

  }

  /*
  * icons are put into array of icons at the index specified by the
  * corresponding iconKey.
  */

  private void _setIcons(
    Object[] iconInfo
    )
  {
    int numIcons =  iconInfo.length / 2;

    IconKey[] keys = new IconKey[ numIcons ];
    Icon[] icons = new Icon[ numIcons ];

    for (int i = 0; i < keys.length;  i ++)
    {
      keys[i] = (IconKey)iconInfo[i * 2];
      icons[i] = (Icon)iconInfo[(i * 2) + 1];
    }

    //
    // Assign the values
    //
    for (int i = 0; i < numIcons; i++)
    {
      if ( keys[i] != null )
        _icons[ keys[i].getKeyIndex()] = icons[i];
    }
  }

  /**
   * Returns the URI to the icon indentified by the icon key
   */
  public String getIconURI(
    RenderingContext context,
    IconKey          iconKey
    )
  {
    return getColorizedIconURI( context, iconKey);
  }


  /**
   * Returns the icon, given its key.
   */
  protected Icon getIcon(
    IconKey iconKey
    )
  {
    Icon icon =   _icons[ iconKey.getKeyIndex()];

    return icon;
  }

  
  protected ImageProviderRequest
    createCoreIconRequest(
      ImageContext context, 
      String       source,
      Class        lookAndFeel,
      int          direction,
      Color        color,
      Color        surroundingColor,
      NameResolver resolver
      )
  {
    return new CoreIconRequest(context,
                               source,
                               lookAndFeel,
                               direction,
                               color,
                               surroundingColor,
                               resolver);
  }

  protected ImageProviderRequest
    createAccentIconRequest(
      ImageContext context, 
      String       source,
      Class        lookAndFeel,
      int          direction,
      Color        color,
      Color        surroundingColor,
      NameResolver resolver
      )
  {
    return new AccentIconRequest(context,
                               source,
                               lookAndFeel,
                               direction,
                               color,
                               surroundingColor,
                               resolver);
  }

  private Icon[]       _icons;

}
