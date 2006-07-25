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
package org.apache.myfaces.adfinternal.renderkit.core.xhtml.table;

import java.util.Map;

import javax.faces.component.UIComponent;

import org.apache.myfaces.adf.logging.ADFLogger;
import org.apache.myfaces.adf.bean.PropertyKey;
import org.apache.myfaces.adf.component.core.data.CoreTable;

/**
 * @version $Name:  $ ($Revision: adfrt/faces/adf-faces-impl/src/main/java/oracle/adfinternal/view/faces/renderkit/core/xhtml/table/BandingData.java#0 $) $Date: 10-nov-2005.19:02:33 $
 * @author The Oracle ADF Faces Team
 */
public final class BandingData
{
  /**
   * @param context gets the appropriate banding data from this context.
   */
  public static BandingData create(TableRenderingContext context)
  {
    // using the variable name "hgrid" so that we don't forget that
    // the table instance might infact be an hgrid:
    UIComponent hgrid = context.getTable();
    Map attrs = hgrid.getAttributes();
    int row = _getInterval(attrs, CoreTable.ROW_BANDING_INTERVAL_KEY);
    int col = _getInterval(attrs, CoreTable.COLUMN_BANDING_INTERVAL_KEY);
    return new BandingData(col, row);
  }

  private static int _getInterval(Map attrs, PropertyKey key)
  {
    Number num = (Number) attrs.get(key.getName());
    int interval = (num != null) ? num.intValue() : 0;
    if (interval < 0)
    {
      _LOG.warning("Illegal value:{0} for {1}", new Object[] {interval, key});
      interval = 0;
    }
    return interval;
  }

  /**
   * computes the banding shade to use for a table cell. Either the cell's row
   * or column(s) will be used depending on the type of this banding.
   * @param row the rowIndex of the table cell.
   * @param physicalColumn the actual column of the table cell.
   * @param logicalColumn the index (of this cell) into the column band
   * DataObjectList on the context.
   * @return the banding color to use. There are only two colors, so a boolean
   * is returned
   */
  public boolean getBand(TableRenderingContext context,
                         int row,
                         int physicalColumn,
                         int logicalColumn)
  {
    boolean band = false;
    if (_rowBanding > 0)
    {
      band = ((row / _rowBanding) % 2) == 1;
    }
    if (_colBanding > 0)
    {
      band |= (((physicalColumn / _colBanding) % 2) == 1);
    }
    return band;
  }

  private BandingData(int colBanding, int rowBanding)
  {
    _rowBanding = rowBanding;
    _colBanding = colBanding;
  }

  private final int _rowBanding, _colBanding;

  private static final ADFLogger _LOG = ADFLogger.createADFLogger(BandingData.class);
}
