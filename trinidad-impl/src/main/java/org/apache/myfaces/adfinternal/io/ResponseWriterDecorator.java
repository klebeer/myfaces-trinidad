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
package org.apache.myfaces.adfinternal.io;

import java.io.IOException;
import java.io.Writer;

import javax.faces.component.UIComponent;
import javax.faces.context.ResponseWriter;

/**
 * ResponseWriter abstract base used to decorate another ResponseWriter.
 * 
 * @author The Oracle ADF Faces Team
 */
abstract public class ResponseWriterDecorator extends ResponseWriter
{
  /**
   * Create a ResponseWriterDecorator.
   * 
   * @param decorated the decorated ResponseWriter
   */
  public ResponseWriterDecorator(ResponseWriter decorated)
  {
    _decorated = decorated;
  }

  /**
   * Return the decorated ResponseWriter.
   */
  protected ResponseWriter getResponseWriter()
  {
    return _decorated;
  }

  public String getCharacterEncoding()
  {
    return getResponseWriter().getCharacterEncoding();
  }

  public String getContentType()
  {
    return getResponseWriter().getContentType();
  }

  public void startDocument() throws IOException
  {
    getResponseWriter().startDocument();
  }


  public void endDocument() throws IOException
  {
    getResponseWriter().endDocument();
  }

  public void flush() throws IOException
  {
    getResponseWriter().flush();
  }


  public void close()throws IOException
  {
    getResponseWriter().close();
  }

  public void startElement(String name,
                           UIComponent component) throws IOException
  {
    getResponseWriter().startElement(name, component);
  }

  
  public void endElement(String name) throws IOException
  {
    getResponseWriter().endElement(name);
  }


  public void writeAttribute(String name,
                             Object value,
                             String componentPropertyName)
        throws IOException
  {
    getResponseWriter().writeAttribute(name, value, componentPropertyName);
  }


  public void writeURIAttribute(String name,
                                Object value,
                                String componentPropertyName)
    throws IOException
  {
    getResponseWriter().writeURIAttribute(name, value, componentPropertyName);
  }

  public void writeComment(Object comment) throws IOException
  {
    getResponseWriter().writeComment(comment);
  }

  
  public void writeText(Object text, String componentPropertyName) throws IOException
  {
    getResponseWriter().writeText(text, componentPropertyName);
  }


  public void writeText(char text[], int off, int len)
        throws IOException
  {
    getResponseWriter().writeText(text, off, len);
  }

  public void write(char cbuf[], int off, int len) throws IOException
  {
    getResponseWriter().write(cbuf, off, len);
  }

  public void write(String str) throws IOException
  {
    getResponseWriter().write(str);
  }

  public void write(int c) throws IOException
  {
    getResponseWriter().write((char) c);
  }

  public void write(char[] cbuf)
    throws IOException
  {
    getResponseWriter().write(cbuf);
  }

  public void write(String str, int off, int len)
    throws IOException
  {
    getResponseWriter().write(str, off, len);
  }

  public ResponseWriter cloneWithWriter(Writer writer)
  {
    return getResponseWriter().cloneWithWriter(writer);
  }

  private final ResponseWriter _decorated;
}
