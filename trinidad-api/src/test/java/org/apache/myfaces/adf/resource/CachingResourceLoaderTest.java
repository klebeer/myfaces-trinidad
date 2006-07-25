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
package org.apache.myfaces.adf.resource;

import java.io.IOException;
import java.net.URL;

public class CachingResourceLoaderTest extends ResourceLoaderTestCase
{
  public CachingResourceLoaderTest(
    String testName)
  {
    super(testName);
  }

  public void testContentLength() throws IOException
  {
    // test twice to make sure both codepaths work
    // 1. initial load
    // 2. cached content
    URL url = findTestResource();
    doTestContentLength(url);
    doTestContentLength(url);
  }

  protected URL findTestResource() throws IOException
  {
    ResourceLoader loader = 
      new CachingResourceLoader(new LocalResourceLoader());
    return loader.getResource("test.xml");
  }
}
