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
package org.apache.myfaces.adf.config;
import java.util.Map;

/**
 * Manages region definitions.
 * This will parse the region metadata files to create and cache 
 * an instance of this object.
 * All resources with the name "/META-INF/region-metadata.xml" will be searched.
 * /WEB-INF/region-metadata.xml will also be searched.
 * @author The Oracle ADF Faces Team
 */
public abstract class RegionManager 
{
  protected RegionManager()
  {
  }
  
  /**
   * Gets all the registered regions.
   * @return An immutable Map. 
   *  Each key is a regionType String.
   *  Each value is of type {@link RegionConfig}.
   */
  public abstract Map getRegionConfigs();
  
  /**
   * Gets the RegionConfig for a particular regionType
   * @param regionType the component-type of the region 
   * @return null, if the region does not exist. 
   */
  public abstract RegionConfig getRegionConfig(String regionType);
}
