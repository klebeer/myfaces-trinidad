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


// External variables used:
//  _cfTrans    - the localized text for transparent colors

var _cfTrans;

function _rgbColorFormat(
  formatColor)
{
  //pu: Return undefined string for an undefined Color.
  if (formatColor == (void 0))
    return (void 0);

  // return localized transparent text for transparent color
  if (formatColor.alpha == 0)
    return _cfTrans;

  var stringHolder = new Object();
  stringHolder.value ="";
  
  var pattern = this._pattern;
  if (typeof pattern != "string")
    pattern = pattern[0];
    
  _cfoDoClumping(pattern,
              _cfoSubformat,
              formatColor,
              stringHolder);
  
  return stringHolder.value;
}


/**
 * Parses a String into a Color using the current object's pattern.  If the
 * parsing fails, undefined will be returned.
 */
function _rgbColorParse(
  parseString)
{
  // return transparent color for localized transparent text
  if (this._allowsTransparent && _cfTrans == parseString)
    return new Color(0,0,0,0);
    
  var pattern = this._pattern;
  if (typeof pattern == "string")
  {
    return _rgbColorParseImpl(parseString,
                              pattern);
  }
  else
  { 
    var i;
    for (i = 0; i < pattern.length; i++)
    {
      var color = _rgbColorParseImpl(parseString,
                                     pattern[i]);
      if (color != (void 0))
        return color;
    }
  }
}

function _rgbColorParseImpl(
  parseString,
  parsePattern)
{
  var parseContext = new Object();
  parseContext.currIndex = 0;
  parseContext.parseString = parseString;
  parseContext.parseException = new ConverterException();

  var parsedColor = new Color(0x00, 0x00, 0x00);

  // parse the color
  if (_cfoDoClumping(parsePattern,
                  _cfoSubParse,
                  parseContext,
                  parsedColor))
  {
    if (parseString.length != parseContext.currIndex)
    {
      return (void 0);
    }

    return parsedColor;
  }
  else
  {
    // failure
    return (void 0);
  }
}


/**
 * Clump up similar runs of pattern characters from the format patter and
 * call the subfunction for each result.  Return whether the clumping
 * succeeded.
 */
function _cfoDoClumping(
  formatPattern,
  subFunction,
  param,
  outValue
  )
{  
  var formatLength = formatPattern.length;
  var inQuote      = false;
  var kindCount    = 0;
  var lastChar     = void 0;
  var startIndex   = 0;
  
  for (var i = 0; i < formatLength; i++)
  {
    var currChar = formatPattern.charAt(i);
    
    if (inQuote)
    {
      if (currChar == "\'")
      {
        inQuote = false;
        
        // handle to single quotes in a row as escaping the quote
        // by not skipping it when outputting
        if (kindCount != 1)
        {
          startIndex++;
          kindCount--;
        }

        // output the quoted text
        if (!subFunction(formatPattern,
                         "\'",
                         startIndex,
                         kindCount,
                         param,
                         outValue))
        {
          // failure
          // alert("failure at " + startIndex + " with " + lastChar);
          return false;
        }
        
        kindCount = 0;
        lastChar  = void 0;
      }
      else
      {
        // keep adding characters to the escaped String
        kindCount++;
      }
    }
    else
    {
      // the characters that we are collecting have changed
      if (currChar != lastChar)
      {
        if (kindCount != 0)
        {       
          // output the previously collected string
          if (!subFunction(formatPattern,
                           lastChar,
                           startIndex,
                           kindCount,
                           param,
                           outValue))
          {
            // failure
            //alert("failure at " + startIndex + " with " + lastChar);
            return false;
          }
          
          kindCount = 0;
          lastChar  = void 0;
        }
        
        if (currChar == '\'')
        {
          inQuote = true;
        }
  
        startIndex = i;      
        lastChar = currChar;
      }

      // keep collecting this kind of character together
      kindCount++;
    }   
  }
  
  // output any left over substring being collected
  if (kindCount != 0)
  {
    if (!subFunction(formatPattern,
                     lastChar,
                     startIndex,
                     kindCount,
                     param,
                     outValue))
    {
      // failure
      //alert("failure at " + startIndex + " with " + lastChar);
      return false;
    }
  }
  
  // success
  return true;
}


/**
 * Format a clump of pattern elements using the specified time.
 */
function _cfoSubformat(
  inString,
  formatType,
  startIndex,
  charCount,
  color,
  stringHolder
  )
{    
  // string to append to the toString
  var appendString = null;
  
  if ((formatType >= 'A') && (formatType <= 'Z') ||
      (formatType >= 'a') && (formatType <= 'z'))
  {
    switch (formatType)
    {
      case 'r': // decimal red component (0-255)
        appendString = _cfoGetPaddedNumber(color.red, charCount, 3, 10);
        break;
      
      case 'g': // decimal green component (0-255)
        appendString = _cfoGetPaddedNumber(color.green, charCount, 3, 10);
        break;
      
      case 'b': // decimal blue component (0-255)
        appendString = _cfoGetPaddedNumber(color.blue, charCount, 3, 10);
        break;
      
      case 'a': // decimal alpha component (0-255)
        appendString = _cfoGetPaddedNumber(color.alpha, charCount, 3, 10);
        break;
      
      case 'R': // hex red component (0x00-0xff)
        appendString = 
          _cfoGetPaddedNumber(color.red, charCount, 2, 16).toUpperCase();
        break;
      
      case 'G': // hex green component (0x00-0xff)
        appendString = 
          _cfoGetPaddedNumber(color.green, charCount, 2, 16).toUpperCase();
        break;
      
      case 'B': // hex blue component (0x00-0xff)
        appendString = 
          _cfoGetPaddedNumber(color.blue, charCount, 2, 16).toUpperCase();
        break;
      
      case 'A': // hex alpha component (0x00-0xff)
        appendString = 
          _cfoGetPaddedNumber(color.alpha, charCount, 2, 16).toUpperCase();
        break;
      
      default:
        // do nothing rather than throw an exception
        appendString = "";
    }
  }
  else
  {
    // all other results are literal
    appendString = inString.substring(startIndex, startIndex + charCount);
  }
  
  stringHolder.value += appendString;
  
  // formatting should never fail
  return true;
}


/**
 * Parse a substring using a clump of format elements.
 */
function _cfoSubParse(
  inString,
  formatType,
  startIndex,
  charCount,
  parseContext,
  parsedColor
  )
{
  // Start index of the string being parsed (as opposed
  // to startIndex, which is the index on the format mask)
  var inStartIndex = parseContext.currIndex;


  if ((formatType >= 'A') && (formatType <= 'Z') ||
      (formatType >= 'a') && (formatType <= 'z'))
  {
    switch (formatType)
    {
      case 'r': // decimal red component (0-255)
        parsedColor.red = _cfoAccumulateNumber(parseContext, charCount, 3, 10);
        if (parsedColor.red == (void 0))
        {
          return false;
        }
        break;
      
      case 'g': // decimal green component (0-255)
        parsedColor.green = _cfoAccumulateNumber(parseContext, charCount, 3, 10);
        if (parsedColor.green == (void 0))
        {
          return false;
        }
        break;
      
      case 'b': // decimal blue component (0-255)
        parsedColor.blue = _cfoAccumulateNumber(parseContext, charCount, 3, 10);
        if (parsedColor.blue == (void 0))
        {
          return false;
        }
        break;
      
      case 'a': // decimal alpha component (0-255)
        parsedColor.alpha = _cfoAccumulateNumber(parseContext, charCount, 3, 10);
        if (parsedColor.alpha == (void 0))
        {
          return false;
        }
        break;
      
      case 'R': // hex red component (0x00-0xff)
        parsedColor.red = _cfoAccumulateNumber(parseContext, charCount, 2, 16);
        if (parsedColor.red == (void 0))
        {
          return false;
        }
        break;
      
      case 'G': // hex green component (0x00-0xff)
        parsedColor.green = _cfoAccumulateNumber(parseContext, charCount, 2, 16);
        if (parsedColor.green == (void 0))
        {
          return false;
        }
        break;
      
      case 'B': // hex blue component (0x00-0xff)
        parsedColor.blue = _cfoAccumulateNumber(parseContext, charCount, 2, 16);
        if (parsedColor.blue == (void 0))
        {
          return false;
        }
        break;
      
      case 'A': // hex alpha component (0x00-0xff)
        parsedColor.alpha = _cfoAccumulateNumber(parseContext, charCount, 2, 16);
        if (parsedColor.alpha == (void 0))
        {
          return false;
        }
        break;
      
      default:
    }
  }
  else
  {
    // consume constants
    return _cfoMatchText(parseContext,
                      inString.substring(startIndex, startIndex + charCount));
  }
  
  // match succeeded
  return true;
}


/**
 * Match the specified text in a case insensitive manner,
 * returning true and updating the
 * <code>parseContext</code> if the match succeeded.
 */
function _cfoMatchText(
  parseContext,
  text
  )
{
  // if no text to match then match will fail
  if (!text)
    return false;

  // get the length of the text to match
  var textLength  = text.length;

  var currIndex   = parseContext.currIndex;
  var parseString = parseContext.parseString;
  
  // determine whether we have enough of the parseString left to match
  if (textLength > parseString.length - currIndex)
  {
    return false;
  }

  var parseText = parseString.substring(currIndex, currIndex + textLength);
  
  if (parseText != text)
    return false;
    
  // update the current parseContext
  parseContext.currIndex += textLength;
  
  return true;
}
 

/**
 * Accumlates and returns a number at this location or undefined, if
 * there is no number.
 */
function _cfoAccumulateNumber(
  parseContext,
  minDigits,
  maxDigits,
  base)
{
  var startIndex  = parseContext.currIndex;
  var currIndex   = startIndex;
  var parseString = parseContext.parseString;
  var parseLength = parseString.length;
  if (parseLength > currIndex + maxDigits)
    parseLength = currIndex + maxDigits;

  var currValue = 0;

  // gather up all of the digits
  while (currIndex < parseLength)
  {
    var currDigit = parseInt(parseString.charAt(currIndex), base);

    if (!isNaN(currDigit))
    {
      // add on the digit and shift over the results
      currValue *= base;
      currValue += currDigit;

      currIndex++;
    }
    else
    {
      break;
    }
  }

  if (startIndex != currIndex && 
      (currIndex - startIndex) >= minDigits)
  {
    // update the current parseContext
    parseContext.currIndex = currIndex;

    // return the numeric version
    return currValue;
  }
  else
  {
    // no number at this location
    return (void 0);
  }
}


/**
 * Pad out a number with leading 0's to meet the minDigits digits or
 * truncate to meet the maxDigits.
 */
function _cfoGetPaddedNumber(
  number,
  minDigits,
  maxDigits,
  base)
{  
  var stringNumber = number.toString(base);
  
  //
  // pad out any number strings that are too short
  //
  if (minDigits != (void 0))
  {    
    var addedDigits = minDigits - stringNumber.length;
  
    while (addedDigits > 0)
    {
      stringNumber = "0" + stringNumber;
      addedDigits--;
    }
  }
  
  //
  // truncate any number strings that are too long
  //
  if (maxDigits != (void 0))
  {
    var extraDigits = stringNumber.length - maxDigits;
    
    if (extraDigits > 0)
    {
      stringNumber = stringNumber.substring(extraDigits,
                                            extraDigits + maxDigits);
    }
  }
  
  return stringNumber;
}

/**
 * Construct a RGBColorFormat with the specifed color pattern.
 */
function RGBColorFormat(
  pattern,
  allowsTransparent)
{
  // for debugging
  this._class = "RGBColorFormat";
  this._allowsTransparent = allowsTransparent;
  
  if (pattern != (void 0))
  {
    if (typeof(pattern) == "string" ) 
      pattern = [pattern];
  }

  this._pattern = pattern;
}

RGBColorFormat.prototype = new Converter();
RGBColorFormat.prototype.getAsString = _rgbColorFormat;
RGBColorFormat.prototype.getAsObject  = _rgbColorParse;

function Color(
  red,
  green,
  blue,
  alpha)
{
  // for debugging
  this._class = "Color";
  
  if (alpha == (void 0))
    alpha = 0xff;

  this.red   = (red & 0xff);
  this.green = (green & 0xff);
  this.blue  = (blue & 0xff);
  this.alpha = (alpha & 0xff);
}

function _Color_toString()
{
  return "rgba(" + this.red + 
         "," + this.green + 
         "," + this.blue + 
         "," + this.alpha + ")";
}

Color.prototype.toString = _Color_toString;

