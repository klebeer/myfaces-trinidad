/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.trinidad.util.ref;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

/**
 * <p>
 * PseudoReferenceFactory for creating SoftReference PseudoReferences to Objects.
 * </p>
 */
public final class SoftPseudoReferenceFactory<T> extends PseudoReferenceFactory<T>
{
  /**
   * Creates and returns a StrongPseudoReference to the referent
   * @param referent
   * @param queue Optional attribute specifying the ReferenceQueue to use for the created
   * PseudoReference.  If the PseudoReference doesn't support ReferenceQueues, this attribute
   * will be ignored
   * @return the created PseudoReference
   */
  @Override
  public PseudoReference<T> create(T referent, ReferenceQueue<? super T> queue)
  {
    return new ReferencePseudoReference<T>(new SoftReference<T>(referent, queue));
  }
}
