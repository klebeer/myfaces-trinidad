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
package org.apache.myfaces.adfinternal.renderkit.core.pages;

import java.io.IOException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.apache.myfaces.adf.context.AdfFacesContext;
import org.apache.myfaces.adf.component.core.CoreImportScript;
import org.apache.myfaces.adf.component.core.output.CoreOutputText;
import org.apache.myfaces.adf.component.html.HtmlHtml;
import org.apache.myfaces.adf.component.html.HtmlFrame;
import org.apache.myfaces.adf.component.html.HtmlFrameBorderLayout;
import org.apache.myfaces.adf.component.html.HtmlScript;

import org.apache.myfaces.adfinternal.context.PageFlowScopeProviderImpl;
import org.apache.myfaces.adfinternal.renderkit.RenderUtils;
import org.apache.myfaces.adfinternal.renderkit.core.CoreRenderKit;
import org.apache.myfaces.adfinternal.share.url.EncoderUtils;

import org.apache.myfaces.adfinternal.renderkit.AdfRenderingContext;

/**
 * Entry point for the "fred" JSP.
 * <p>
 * Parameters:
 * <ul>
 * <li>redirect: the full path to an external JSP
 * <li>_red: the name of an internal JSP to redirect to
 * <li>enc: the HTML character set encoding to use
 * <li>configName: the name of a configuration object
 * <li>contextURI: the context URI
 * </ul>
 * <p>
 * @version $Name:  $ ($Revision: adfrt/faces/adf-faces-impl/src/main/java/oracle/adfinternal/view/faces/renderkit/core/pages/FredJSP.java#0 $) $Date: 10-nov-2005.19:03:34 $
 * @author The Oracle ADF Faces Team
 */
public class FredJSP
{
  /**
   * Create an URL that will point to the frame redirect page.
   * @param context the FacesContext
   * @param viewRoot the UIViewRoot that will subsequently displayed.
   *    The viewRoot itself is not saved, but values like the locale
   *    and viewId are saved.
   * @param savedPageFlowScope the process scope to use for the dialog
   * @param minWidth the minimum width for the target page.
   * @param minHeight the minimum height for the target page.
   */
  static public String getRedirectURL(FacesContext context,
                                      UIViewRoot   viewRoot,
                                      String       minWidth,
                                      String       minHeight)
  {
    String baseURL = GenericEntry.getGenericEntryURL(
                         context,
                         GenericEntry.NEW_FRAME_REDIRECT_ENTRY);

    String[] args = new String[]
    {
      _VIEW_ID_REDIRECT_PARAM,
      viewRoot.getViewId(),
      "loc",
      viewRoot.getLocale().toString().replace('_', '-'),
      _MIN_WIDTH_PARAM,
      minWidth,
      _MIN_HEIGHT_PARAM,
      minHeight
    };


    return EncoderUtils.appendURLArguments(baseURL, args);
  }


  static void service(FacesContext context) throws IOException
  {
    AdfRenderingContext arc = AdfRenderingContext.getCurrentInstance();

    // Don't use HtmlHead or CoreDocument;  these add a stylesheet link,
    // which we don't need or want
    HtmlHtml root = new HtmlHtml();
    context.getViewRoot().getChildren().add(root);

    Map requestParameters = context.getExternalContext().getRequestParameterMap();
    // Save the return ID - and do so before generating the
    // link to the frames!
    String returnId = (String) requestParameters.get(_RETURN_ID_PARAM);
    if (returnId != null)
      CoreRenderKit.saveDialogPostbackValues(returnId);

    CoreOutputText headStart = new CoreOutputText();
    root.getChildren().add(headStart);
    headStart.setEscape(false);
    headStart.setValue("<head>");

    CoreImportScript cis = new CoreImportScript();
    cis.setNames(new String[]{"Core"});
    root.getChildren().add(cis);

    // The block-reload script only happens to work on IE, but may someday
    // work on Mozilla as well.
    HtmlScript script = new HtmlScript();
    script.setText(_BLOCK_RELOAD_TEXT + _FIX_DIALOG_TITLE);
    root.getChildren().add(script);

    CoreOutputText headEnd = new CoreOutputText();
    root.getChildren().add(headEnd);
    headStart.setEscape(false);
    headStart.setValue("</head>");

    HtmlFrame contentFrame = new HtmlFrame();
    String contentStr = arc.getTranslatedString(_FRAME_CONTENT);
    if (contentStr == null)
      contentStr = _DEFAULT_CONTENT_STRING;

    contentFrame.setShortDesc(contentStr); // for accessibility
    contentFrame.setLongDescURL("#");     // for accessibility
    contentFrame.setHeight("100%");
    contentFrame.setWidth("100%");

    // Get the query string.
    // trim out any "_t" parameter, which was only used to get here.
    // trim out any sizing parameters
    // trim out any redirect parameters
    String queryString = _getQueryString(
       context.getExternalContext().getRequestParameterValuesMap());

    // grab any sizing parameters
    String widthParam = (String) requestParameters.get(_MIN_WIDTH_PARAM);
    boolean gotWidth = (widthParam != null);

    String heightParam = (String) requestParameters.get(_MIN_HEIGHT_PARAM);
    boolean gotHeight = (heightParam != null);

    String viewIdRedirect = (String) requestParameters.get(_VIEW_ID_REDIRECT_PARAM);
    if (viewIdRedirect != null)
    {
      ViewHandler vh =
        context.getApplication().getViewHandler();
      // Prepend an extra slash to avoid re-prepending the context path
      String redirectString = "/" + vh.getActionURL(context,
                                                    viewIdRedirect);
      // if redirectString contains ?, append queryString with &,
      // otherwise append queryString with &
      char sep = (redirectString.indexOf('?') != -1) ? '&' : '?';
      contentFrame.setSource(redirectString + sep + queryString);
    }
    else
    {
      String internalRedirect = (String) requestParameters.get("_red");
      if (internalRedirect != null)
      {
        String path = GenericEntry.getGenericEntryURL(context,
                                                      internalRedirect);
        // Prepend an extra slash to avoid re-prepending the context path
        contentFrame.setSource("/" + path + "&" + queryString);
      }
    }

    HtmlFrameBorderLayout frameSet = new HtmlFrameBorderLayout();
    frameSet.setShortDesc(contentStr); // for accessibility
    frameSet.setCenter(contentFrame);
    // this border attribute is a "secret" attribute set to fix
    // 4339153 DIALOGS IN FIREFOX HAVE WHITE LINE AT THE BOTTOM
    frameSet.getAttributes().put("border", Boolean.FALSE);
    // see bug 3198336 apss accessibility violations
    CoreOutputText alternateContent = new CoreOutputText();
    alternateContent.setValue(arc.getTranslatedString("NO_FRAMES_MESSAGE"));
    frameSet.setAlternateContent(alternateContent);

    // Set the title to the title of the content, and then shrink (or expand)
    // the window to fit the content.  The 25 pixel fudge factor is purely
    // a hack to handle calendarDialog, which regularly needs to grow
    // a line or two.  A better method that accounts for font heights
    // would be good...

    // Bug #2464627: Allow support for forcing a minimum size
    StringBuffer onload = new StringBuffer(_FRAMESET_ONLOAD_TEXT.length()
                                           // space for the param plus the 'W:'
                                           + (gotWidth
                                              ? widthParam.length() + 2
                                              : 0)
                                           // space for the param plus the 'H:'
                                           + (gotHeight
                                              ? heightParam.length() + 2
                                              : 0)
                                           // space for commas, brackets,
                                           // and closing paren
                                           + 5);

    onload.append(_FRAMESET_ONLOAD_TEXT);
    if (gotWidth || gotHeight)
    {
      // open the parameter object
      onload.append(",{");
      if (gotWidth)
      {
        // add in the width parameter
        onload.append("W:");
        onload.append(widthParam);
      }
      if (gotHeight)
      {
        // If something preceded this param, separate with a comma
        if (gotWidth)
          onload.append(",");
        onload.append("H:");
        onload.append(heightParam);
      }
      // close the parameter object
      onload.append("}");
    }
    // close the handler
    onload.append(")");

    frameSet.setOnload(onload.toString());
    frameSet.setOnunload(_FRAMESET_ONUNLOAD_TEXT);
    root.getChildren().add(frameSet);
  }

  static private String _getQueryString(
    Map                parameters) throws IOException
  {
    // Bug #3419817 support request dispatch for Portal
    // build up the encoded query string from request parameters
    // this will work for both direct requests and include/forward requests
    StringBuffer buf = new StringBuffer();
    Iterator paramNames = parameters.keySet().iterator();
    boolean isFirst = true;

    // iterate through the request parameter names
    while (paramNames.hasNext())
    {
      String paramName = (String)paramNames.next();

      // add the parameter to the query string unless skipped
      if (!_SKIP_PARAMS.contains(paramName))
      {
        // the parameter might appear on the request more than once
        String[] paramValues = (String[]) parameters.get(paramName);

        for (int i=0; i < paramValues.length; i++)
        {
          if (!isFirst)
            buf.append("&");

          String paramValue = paramValues[i];
          EncoderUtils.appendUIXQueryParameter(buf, paramName);
          buf.append("=");
          EncoderUtils.appendUIXQueryParameter(buf, paramValue);

          isFirst = false;
        }
      }
    }

    return buf.toString();
  }

  static private final String _VIEW_ID_REDIRECT_PARAM = "_vir";
  static private final String _MIN_WIDTH_PARAM = "_minWidth";
  static private final String _MIN_HEIGHT_PARAM = "_minHeight";
  static private final String _RETURN_ID_PARAM = "_rtrnId";

  static private final String _BLOCK_RELOAD_TEXT =
    "document.onkeydown=_noReload;var _blockReload=1;";
  static private final String _FIX_DIALOG_TITLE =
    "function _fixDialogTitle(){window.document.title=window.frames[0].document.title};";

  // NOTE: There is NO CLOSING PAREN here. This will have the closing paren
  // added to it in the service method above (we may or may not add a window
  // sizing parameter object as a last argument).
  static private final String _FRAMESET_ONLOAD_TEXT =
    "_fixDialogTitle(); _sizeWin(window.frames[0],0,30";
  static private final String _FRAMESET_ONUNLOAD_TEXT =
    "_checkUnload(event)";

  static private final Collection _SKIP_PARAMS =
    Arrays.asList(new String[]
                  {
                    GenericEntry.__ENTRY_KEY_PARAM,
                    "redirect",
                    _MIN_WIDTH_PARAM,
                    _MIN_HEIGHT_PARAM,
                    "_red",
                    _VIEW_ID_REDIRECT_PARAM,
                    _RETURN_ID_PARAM,
                    PageFlowScopeProviderImpl.TOKEN_PARAMETER_NAME,
                  });

  static private final String _FRAME_CONTENT = "FRAME_CONTENT";
  static private final String _DEFAULT_CONTENT_STRING = "Content";
}
