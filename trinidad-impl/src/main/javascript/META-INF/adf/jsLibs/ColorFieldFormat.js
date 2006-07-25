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
function _getColorFieldFormat(
  colorField)
{
  var name = colorField.name;
  if (name && _cfs)
  {
    var format = _cfs[name];
    var trans  = _cfts[name];
    if (format || trans)
      return new RGBColorFormat(format, trans);
  }

  return new RGBColorFormat();
}

function _fixCFF(
  colorField)
{
  var format = _getColorFieldFormat(colorField);

  if (colorField.value != "")
  {
    var value = format.getAsObject(colorField.value);

    if (value != (void 0))
      colorField.value = format.getAsString(value);
  }
}

