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
package org.apache.myfaces.trinidad.component;

import java.io.IOException;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import java.util.Map;
import java.util.Set;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PhaseId;

import javax.faces.render.Renderer;
import org.apache.myfaces.trinidad.bean.FacesBean;
import org.apache.myfaces.trinidad.bean.PropertyKey;
import org.apache.myfaces.trinidad.component.visit.VisitCallback;
import org.apache.myfaces.trinidad.component.visit.VisitContext;
import org.apache.myfaces.trinidad.component.visit.VisitResult;
import org.apache.myfaces.trinidad.context.RenderingContext;
import org.apache.myfaces.trinidad.model.CollectionModel;
import org.apache.myfaces.trinidad.model.LocalRowKeyIndex;
import org.apache.myfaces.trinidad.model.ModelUtils;
import org.apache.myfaces.trinidad.render.ClientRowKeyManager;
import org.apache.myfaces.trinidad.util.ComponentUtils;


/**
 * This component iterates over some given data.
 * Each child is repeatedly stamped as many times as necessary.
 * Iteration is done starting at the index given by {@link #getFirst()}
 * for as many indices as specified by {@link #getRows()}.
 * If {@link #getRows()} returns 0, then the iteration continues until
 * there are no more elements in the underlying data.
 */
public abstract class UIXIteratorTemplate extends UIXCollection implements FlattenedComponent, LocalRowKeyIndex
{

/**/  abstract public int getFirst();
/**/  abstract public void setFirst(int first);
/**/  abstract public int getRows();

  /**
   * Override to return true.
   */
  @Override
  public boolean getRendersChildren()
  {
    return true;
  }

  /**
   * Sets up the iteration context for each child and processes it
   */
  public <S> boolean processFlattenedChildren(
    final FacesContext context,
    ComponentProcessingContext cpContext,
    final ComponentProcessor<S> childProcessor,
    final S callbackContext) throws IOException
  {
    // Mimic what would normally happen in the non-flattening case for encodeBegin():
    __processFlattenedChildrenBegin();

    Runner runner = new Runner(cpContext)
    {
      @Override
      protected void process(UIComponent kid, ComponentProcessingContext cpContext) throws IOException
      {
        childProcessor.processComponent(context, cpContext, kid, callbackContext);
      }
    };
    boolean processedChildren = runner.run();
    Exception exp = runner.exception;
    if (exp != null)
    {
      if (exp instanceof RuntimeException)
        throw (RuntimeException) exp;

      if (exp instanceof IOException)
        throw (IOException) exp;
      throw new IllegalStateException(exp);
    }

    return processedChildren;
  }

  /**
   * Returns <code>true</code> if this FlattenedComponent is currently flattening its children
   * @param context FacesContext
   * @return <code>true</code> if this FlattenedComponent is currently flattening its children
   */
  public boolean isFlatteningChildren(FacesContext context)
  {
    // if we don't have a Renderer, then we're flattening
    return (getRendererType() == null);
  }

  /**
   * Repeatedly render the children as many times as needed.
   */
  @Override
  public void encodeChildren(final FacesContext context)
    throws IOException
  {
    if (!isRendered())
      return;

    // if this is the table there will be a rendererType:
    if (getRendererType() != null)
    {
      Renderer renderer = getRenderer(context);
      if (renderer != null)
      {
        renderer.encodeChildren(context, this);
      }
    }
    else // this is not the table. it must be the iterator
    {
      Runner runner = new Runner()
      {
        @Override
        protected void process(
          UIComponent                kid,
          ComponentProcessingContext cpContext
          ) throws IOException
        {
          kid.encodeAll(context);
        }
      };
      runner.run();
      Exception exp = runner.exception;
      if (exp != null)
      {
        if (exp instanceof RuntimeException)
          throw (RuntimeException) exp;

        if (exp instanceof IOException)
          throw (IOException) exp;
        throw new IllegalStateException(exp);
      }
    }
  }

  /**
   * Override of UIXComponent visitTree to visit the stamped children of the UIXIterator
   * with correct currency
   */
  @Override
  public boolean visitTree(
    final VisitContext visitContext,
    final VisitCallback callback)
  {
    if (!isVisitable(visitContext))
      return false;

    // invoke the callback for this component
    VisitResult result = visitContext.invokeVisitCallback(this, callback);

    if (result == VisitResult.COMPLETE)
      return true;
    else if (result == VisitResult.ACCEPT)
    {
      FacesContext context = visitContext.getFacesContext();
      PhaseId phaseId = visitContext.getPhaseId();
      RenderingContext rc = (PhaseId.RENDER_RESPONSE == phaseId)
                              ? RenderingContext.getCurrentInstance()
                              : null;
      Collection<String> idsToVisit = visitContext.getSubtreeIdsToVisit(this);
      boolean visitAllIds = idsToVisit == VisitContext.ALL_IDS;
      if (idsToVisit.isEmpty())
        return false;

      try
      {
        if (PhaseId.RENDER_RESPONSE == phaseId)
        {
          setUpEncodingContext(context, rc);
        }
        else
        {
          setupVisitingContext(context);
        }

        if (visitAllIds)
        {
          if (_visitAllChildren(visitContext, callback))
          {
            return true;
          }
        }
        else
        {
          if (_visitSubIds(context, visitContext, idsToVisit, callback))
          {
            return true;
          }
        }
      }
      finally
      {
        // tear down the context we set up in order to visit our children
        if (PhaseId.RENDER_RESPONSE == phaseId)
        {
          tearDownEncodingContext(context, rc);
        }
        else
        {
          tearDownVisitingContext(context);
        }
      }
    }
    // if we got this far, we're not done
    return false;
  }

  /**
   * Enhances the varStatusMap created by the super class to include:<ul>
   * <li>begin - the index of the first row being rendered
   * <li>first - true if the current row is the first row
   * <li>count - indicates which iteration this is. This always starts at one,
   * and increases (by one) as the loop progresses.
   * <li>step - this is always one.
   * </ul>
   */
  @Override
  protected Map<String, Object> createVarStatusMap()
  {
    final Map<String, Object> map = super.createVarStatusMap();
    return new AbstractMap<String, Object>()
    {
      @Override
      public Object get(Object key)
      {
        // some of these keys are from <c:forEach>, ie:
        // javax.servlet.jsp.jstl.core.LoopTagStatus
        if ("begin".equals(key)) // from jstl
        {
          return Integer.valueOf(getFirst());
        }
        if ("first".equals(key)) // from jstl
        {
          boolean isFirst = (getFirst() == getRowIndex());
          return Boolean.valueOf(isFirst);
        }
        if ("count".equals(key)) // from jstl
        {
          int count = getRowIndex() - getFirst() + 1;
          return Integer.valueOf(count);
        }
        if ("step".equals(key)) // from jstl
        {
          return Integer.valueOf(1);
        }
        return map.get(key);
      }

      @Override
      public Set<Map.Entry<String, Object>> entrySet()
      {
        return map.entrySet();
      }
    };
  }

  @Override
  protected CollectionModel createCollectionModel(
    CollectionModel current,
    Object value)
  {
    CollectionModel model = ModelUtils.toCollectionModel(value);
    // initialize to -1. we need to do this incase some application logic
    // changed this index. Also, some JSF1.0 RI classes were initially starting
    // with a rowIndex of 0.
    // we need this to be -1 because of name-transformation.
    model.setRowIndex(-1);
    assert model.getRowIndex() == -1 : "RowIndex did not reset to -1";
    return model;
  }

  @Override
  protected void processFacetsAndChildren(
    final FacesContext context,
    final PhaseId phaseId)
  {
    Runner runner = new Runner()
    {
      @Override
      protected void process(UIComponent kid, ComponentProcessingContext cpContext)
      {
        UIXIterator.this.processComponent(context, kid, phaseId);
      }
    };
    runner.run();
  }

  private boolean _visitAllChildren(
    final VisitContext  visitContext,
    final VisitCallback visitCallback)
  {
    Runner runner = new Runner()
    {
      @Override
      protected void process(
        UIComponent                kid,
        ComponentProcessingContext cpContext
        ) throws Exception
      {
        if (UIXComponent.visitTree(visitContext, kid, visitCallback))
        {
          throw new VisitingCompleteException();
        }
      }
    };
    runner.run();

    if (runner.exception != null)
    {
      if (runner.exception instanceof VisitingCompleteException)
      {
        return true;
      }
      else if (runner.exception instanceof RuntimeException)
      {
        throw (RuntimeException)runner.exception;
      }
      else
      {
        throw new FacesException(runner.exception);
      }
    }

    return false;
  }

  private boolean _visitSubIds(
    FacesContext       facesContext,
    VisitContext       visitContext,
    Collection<String> idsToVisit,
    VisitCallback      callback)
  {
    Object oldKey = getRowKey();
    try
    {
      ClientRowKeyManager keyMgr = this.getClientRowKeyManager();
      String thisClientId = getClientId(facesContext);
      for (String clientId : idsToVisit)
      {
        Object rowKey = null;

        // look for row key in client ids to visit and set currency before visiting children
        int index = clientId.indexOf(thisClientId);

        if (index >= 0)
        {
          index += thisClientId.length();
          if (index < clientId.length() && clientId.charAt(index) == UINamingContainer.SEPARATOR_CHAR)
          {
            int endIndex = clientId.indexOf(UINamingContainer.SEPARATOR_CHAR, index + 1);

            if (endIndex >= 0 && endIndex > index)
            {
              String clientRowKey = clientId.substring(index + 1, endIndex);
              rowKey = keyMgr.getRowKey(facesContext, this, clientRowKey);
            }
          }
        }

        // visit the children with correct currency
        setRowKey(rowKey);

        Iterator<UIComponent> kids = getFacetsAndChildren();
        while(kids.hasNext())
        {
          boolean done;
          UIComponent currChild = kids.next();

          if (currChild instanceof UIXComponent)
          {
            UIXComponent uixChild = (UIXComponent)currChild;

            // delegate to UIXComponent's visitTree implementation to allow
            // subclassses to modify the behavior
            done = uixChild.visitTree(visitContext, callback);
          }
          else
          {
            // use generic visit implementation
            done = visitTree(visitContext, currChild, callback);
          }

          // If any kid visit returns true, we are done.
          if (done)
          {
            return true;
          }
        }
      }

      return false;
    }
    finally
    {
      setRowKey(oldKey);
    }
  }

  private abstract class Runner implements ComponentProcessor<Object>
  {
    public Runner()
    {
      this(null);
    }

    public Runner(ComponentProcessingContext cpContext)
    {
      _cpContext = cpContext;
    }

    public final boolean run()
    {
      FacesContext context = FacesContext.getCurrentInstance();

      List<UIComponent> stamps = getStamps();
      int oldIndex = getRowIndex();
      int first = getFirst();
      int rows = getRows();
      int end = (rows <= 0) //show everything
        ? Integer.MAX_VALUE
        : first + rows;

      boolean processedChild = false;

      try
      {
        for(int i=first; i<end; i++)
        {
          setRowIndex(i);
          if (isRowAvailable())
          {
            // latch processedChild the first time we process a child
            processedChild |= (_cpContext != null)
              ? UIXComponent.processFlattenedChildren(context, _cpContext, this, stamps, null)
              : UIXComponent.processFlattenedChildren(context, this, stamps, null);
          }
          else
            break;
        }
      }
      catch (IOException e)
      {
        exception = e;
      }
      finally
      {
        setRowIndex(oldIndex);
      }

      return processedChild;
    }

    /**
     * Sets up the context for the child and processes it
     */
    public void processComponent(
      FacesContext context,
      ComponentProcessingContext cpContext,
      UIComponent component,
      Object callbackContext) throws IOException
    {
      try
      {
        process(component, cpContext);
      }
      catch (IOException ioe)
      {
        throw ioe;
      }
      catch (Exception e)
      {
        exception = e;
      }
    }

    protected abstract void process(UIComponent comp, ComponentProcessingContext cpContext)
      throws Exception;

    public Exception exception = null;

    private final ComponentProcessingContext _cpContext;
  }

  private static class VisitingCompleteException
    extends RuntimeException
  {

  }

  @Override
  void __encodeBegin(FacesContext context) throws IOException
  {
    _fixupFirst();
    super.__encodeBegin(context);
  }

  // make sure the current range exists on the model:
  // see bug 4143852:
  private void _fixupFirst()
  {
    int first = getFirst();
    // if we are starting from row zero then there is no problem:
    if (first == 0)
      return;

    // Negative "first" makes no sense. Given the logic below,
    // it forces iterator to scroll to the end unnecessarily.
    if (first < 0)
    {
      setFirst(0);
      return;
    }

    CollectionModel model = getCollectionModel();
    int oldIndex = model.getRowIndex();
    try
    {
      model.setRowIndex(first);
      // if the starting row doesn't exist then we need to scroll back:
      if (!model.isRowAvailable())
      {
        int size = model.getRowCount();
        int rows = getRows();
        // if the rowCount is unknown OR
        //    the blockSize is show all OR
        //    there are fewer rows than the blockSize on the table
        // then start from the beginning:
        if ((size <= 0) || (rows <= 0) || (size <= rows))
          first = 0;
        else
        {
          // scroll to the last page:
          first = size - rows;
          model.setRowIndex(first);
          // make sure the row is indeed available:
          if (!model.isRowAvailable())
          {
            // row is not available. this happens when getRowCount() lies.
            // Some DataModel implementations seem to have rowCount methods which
            // lie. see bug 4157186
            first = 0;
          }
        }
        setFirst(first);
      }
    }
    finally
    {
      model.setRowIndex(oldIndex);
    }
  }
}