/*
 * Copyright  2003-2006 The Apache Software Foundation.
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

package org.apache.myfaces.trinidadinternal.validator;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;

import java.util.HashSet;
import java.util.Set;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.apache.myfaces.trinidad.logging.TrinidadLogger;
import org.apache.myfaces.trinidad.util.MessageFactory;

import org.apache.myfaces.trinidadinternal.ui.laf.base.xhtml.XhtmlLafUtils;

/**
 * <p>Enables byte length validation at the client side. </p>
 *
 * @author The Oracle ADF Faces Team
 */
public class ByteLengthValidator
              extends org.apache.myfaces.trinidad.validator.ByteLengthValidator
              implements InternalClientValidator
{
  public ByteLengthValidator()
  {
    super();
  }

  /**
   * {@inheritDoc}
   */
  public String getLibKey(
    FacesContext context,
    UIComponent component
    )
  {

    switch (_getType(getEncoding()))
    {
      case _SINGLE_BYTE_TYPE:
        return "SBFormat()";
      case _CJK_TYPE:
        return "CjkFormat()";
      case _UTF8_TYPE:
        return "Utf8Format()";

      case _UNSUPPORTED_TYPE:
      default:
        return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  public String getClientScript(
    FacesContext context,
    UIComponent component
    )
  {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public String getClientValidation(
    FacesContext context,
    UIComponent component
    )
  {

    int encodingType = _getType(getEncoding());

    //to create the constructor.
    StringBuffer constr = new StringBuffer(150);
    switch(encodingType)
    {
      case _SINGLE_BYTE_TYPE :
      {
        constr.append("new SBFormat(");
        break;
      }
      case _CJK_TYPE :
      {
        constr.append("new CjkFormat(");
        break;
      }
      case _UTF8_TYPE :
      {
        constr.append("new Utf8Format(");
        break;
      }
      // encoding supported by JVM but not by us at the client side
      case _UNSUPPORTED_TYPE :
      {
        return null;
      }
      default :
      {
        assert false : "Unexpected Encoding type " + encodingType +
                       " Encoding is " + getEncoding();
      }
    }

    String lengthFailedMessage
      = _getEscapedJsMaximumMessageDetail(context);

    String maxLength = String.valueOf(getMaximum());

    constr.append(maxLength);

    constr.append(",{LF:'");
    constr.append(XhtmlLafUtils.escapeJS(lengthFailedMessage));
    constr.append( "'})");

    return constr.toString();
  }

  static private int _getType(String encoding)
  {

    // After null check, this should be the first check to see that the
    // encoding is supported. It is possible that encodings which are listed
    // as being part of _cjkEncodings and singlByteEncodings might not be
    // supported by the JVM
    if ( !Charset.isSupported(encoding))
      throw new IllegalCharsetNameException("Encoding: " + encoding +
                                         " is unsupported by JVM");

    encoding = encoding.toLowerCase();

    if ("utf-8".equals(encoding))
      return _UTF8_TYPE;
    else if (_cjkEncodings.contains(encoding))
      return _CJK_TYPE;
    else if (_singleByteEncodings.contains(encoding))
      return _SINGLE_BYTE_TYPE;

   /**
    * When we come to this point, it means that the JVM supports the given
    * encoding but we don't support it. So we log a message and render no
    * script to the client side, hence no client side validation will be
    * performed for component associated with this validator.
    */
    if (_LOG.isWarning())
    {
      _LOG.warning("Encoding " + encoding +
                   " is not supported at the client side. " +
                   "This will skip client side validation." );
    }
    return _UNSUPPORTED_TYPE;
  }

  private String _getEscapedJsMaximumMessageDetail(
    FacesContext context)
  {
    String maxMsgDetail = getMaximumMessageDetail();
    String maxLength = String.valueOf(getMaximum());
    String label = "{0}";    // this will get substituted on the client

    Object[] params = new Object[] {label, "{1}", maxLength};

    String detailMsg
      = MessageFactory.getMessage(context,
                                  ByteLengthValidator.MAXIMUM_MESSAGE_ID,
                                  maxMsgDetail,
                                  params).getDetail();


    return detailMsg;
  }


  // Type where all characters are single byte
  static private final int _SINGLE_BYTE_TYPE = 0;

  // Standard set of CJK characters (but _not_ GB18030  or EUC)
  static private final int _CJK_TYPE         = 1;

  static private final int _UTF8_TYPE        = 2;

  // Unsupported type - perform validation on the server only
  static private final int _UNSUPPORTED_TYPE = 3;

  static private Set<String> _cjkEncodings = new HashSet<String>();

  static private Set<String> _singleByteEncodings = new HashSet<String>();

  static
  {
    // List of CJK encoding we support - note that this _does not_
    // include GB18030 or the EUC encodings, as these have very
    // different byte length tables.
    String[] cjkArray = new String[] {
      "shift_jis",
      "ms_kanji",
      "csshiftjis",
      "windows-31j",
      "cswindows31j",
      "ks_c_5601-1987",
      "iso-ir-149",
      "ks_c_5601-1989",
      "ksc_5601",
      "korean",
      "csksc56011987",
      "euc-kr",
      "cseuckr",
      "windows-949",
      "gb2312",
      "csgb2312",
      "chinese",
      "csiso58gb231280",
      "hz-gb-2312",
      "gbk",
      "cp936",
      "ms936",
      "windows-936",
      "big5",
      "csbig5",
      "windows-950"
    };

    for (int i = 0; i < cjkArray.length; i++)
      _cjkEncodings.add(cjkArray[i]);

    // List of known Java single-byte character sets (lowercased)
    String[] singleByteArray = new String[] {
      "ascii",
      "iso646-us",
      "iso_8859-1:1987",
      "iso-ir-100",
      "iso_8859-1",
      "iso-8859-1",
      "latin1",
      "l1",
      "ibm819",
      "cp819",
      "csisolatin1",
      "iso_8859-2:1987",
      "iso-ir-101",
      "iso_8859-2",
      "iso-8859-2",
      "latin2",
      "l2",
      "csisolatin2",
      "iso_8859-3:1988",
      "iso-ir-109",
      "iso_8859-3",
      "iso-8859-3",
      "latin3",
      "l3",
      "csisolatin3",
      "iso_8859-4:1988",
      "iso-ir-110",
      "iso_8859-4",
      "iso-8859-4",
      "latin4",
      "l4",
      "csisolatin4",
      "iso_8859-5:1988",
      "iso-ir-144",
      "iso_8859-5",
      "iso-8859-5",
      "cyrillic",
      "csisolatincyrillic",
      "iso_8859-6:1987",
      "iso-ir-127",
      "iso_8859-6",
      "iso-8859-6",
      "ecma-114",
      "asmo-708",
      "arabic",
      "csisolatinarabic",
      "iso_8859-7:1987",
      "iso-ir-126",
      "iso_8859-7",
      "iso-8859-7",
      "elot_928",
      "ecma-118",
      "greek",
      "greek8",
      "csisolatingreek",
      "iso_8859-8:1988",
      "iso-ir-138",
      "iso_8859-8",
      "iso-8859-8",
      "hebrew",
      "csisolatinhebrew",
      "iso-ir-148",
      "iso_8859-9",
      "iso-8859-9",
      "latin5",
      "l5",
      "csisolatin5",
      "iso-8859-13",
      "iso-8859-15",
      "iso_8859-15",
      "ibm037",
      "cp037",
      "ibm273",
      "cp273",
      "ibm277",
      "ibm278",
      "cp278",
      "278",
      "ibm280",
      "cp280",
      "ibm284",
      "cp284",
      "ibm285",
      "cp285",
      "ibm297",
      "cp297",
      "ibm420",
      "cp420",
      "420",
      "ibm424",
      "cp424",
      "ibm437",
      "cp437",
      "437",
      "cspc8codepage437",
      "ibm500",
      "cp500",
      "ibm775",
      "cp775",
      "ibm850",
      "cp850",
      "850",
      "cspc850multilingual",
      "ibm852",
      "cp852",
      "852",
      "cspcp852",
      "ibm855",
      "cp855",
      "855",
      "cspcp855",
      "ibm857",
      "cp857",
      "857",
      "csibm857",
      "ibm860",
      "cp860",
      "860",
      "csibm860",
      "ibm861",
      "cp861",
      "cp-is",
      "861",
      "csibm861",
      "ibm862",
      "cp862",
      "862",
      "cspc862latinhebrew",
      "ibm863",
      "cp863",
      "863",
      "csibm863",
      "ibm864",
      "cp864",
      "csibm864",
      "ibm865",
      "cp865",
      "865",
      "csibm865",
      "ibm866",
      "cp866",
      "866",
      "csibm866",
      "ibm868",
      "cp868",
      "ibm869",
      "cp869",
      "869",
      "cp-gr",
      "csibm869",
      "ibm870",
      "cp870",
      "ibm871",
      "cp871",
      "ibm918",
      "cp918",
      "ibm1026",
      "cp1026",
      "windows-1250",
      "windows-1251",
      "windows-1252",
      "windows-1253",
      "windows-1254",
      "windows-1255",
      "windows-1256",
      "windows-1257",
      "windows-1258",
      "koi8-r",
      "cskoi8r",
    };

    for (int i = 0; i < singleByteArray.length; i++)
      _singleByteEncodings.add(singleByteArray[i]);
  }

 private static final TrinidadLogger _LOG =  TrinidadLogger.createTrinidadLogger(ByteLengthValidator.class);

}
