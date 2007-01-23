/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.myfaces.trinidadinternal.image.cache;

import java.util.Map;

import org.apache.myfaces.trinidadinternal.image.ImageContext;

/**
 * CacheKeyFactory implementation for composite button keys.
 *
 * @version $Name:  $ ($Revision: adfrt/faces/adf-faces-impl/src/main/java/oracle/adfinternal/view/faces/image/cache/CompositeButtonKeyFactory.java#0 $) $Date: 10-nov-2005.19:06:03 $
 * @author The Oracle ADF Faces Team
 */
public class CompositeButtonKeyFactory implements CacheKeyFactory
{
  /**
   * Implementation of CacheKeyFactory.getCacheKey().
   */
  public Object getCacheKey(ImageContext context, Map<Object, Object> properties)
  {
    return new CompositeButtonKey(context, properties);
  }
}

