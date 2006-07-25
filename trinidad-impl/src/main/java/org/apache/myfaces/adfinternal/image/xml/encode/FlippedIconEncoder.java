/*
 * Copyright  2000-2006 The Apache Software Foundation.
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

package org.apache.myfaces.adfinternal.image.xml.encode;

import java.io.PrintWriter;
import java.util.Map;
import org.apache.myfaces.adfinternal.image.ImageContext;

/**
 * Encoder for flipped icons
 *
 * @version $Name:  $ ($Revision: adfrt/faces/adf-faces-impl/src/main/java/oracle/adfinternal/view/faces/image/xml/encode/FlippedIconEncoder.java#0 $) $Date: 10-nov-2005.19:04:46 $
 * @author The Oracle ADF Faces Team
 */
public class FlippedIconEncoder extends AbstractXMLEncoder
{
  /**
   * Creates a FlippedIconEncoder
   */
  public FlippedIconEncoder()
  {
  }

  protected void encodeAttributes(
    ImageContext context,
    Map properties, 
    Map responseProperties,
    PrintWriter out
    )
  {
    super.encodeAttributes(context, properties, responseProperties, out);
    String source = (String)properties.get(SOURCE_KEY);
    encodeAttribute(SOURCE_ATTR, source, out);
  }

}

