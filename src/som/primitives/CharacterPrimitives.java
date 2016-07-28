/**
 * Copyright (c) 2013 Stefan Marr,   stefan.marr@vub.ac.be
 * Copyright (c) 2009 Michael Haupt, michael.haupt@hpi.uni-potsdam.de
 * Software Architecture Group, Hasso Plattner Institute, Potsdam, Germany
 * http://www.hpi.uni-potsdam.de/swa/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package som.primitives;

import som.primitives.CharacterPrimsFactory.AsDigitCharPrimFactory;
import som.primitives.CharacterPrimsFactory.AsIntegerCharPrimFactory;
import som.primitives.CharacterPrimsFactory.AsLowercaseCharPrimFactory;
import som.primitives.CharacterPrimsFactory.AsUppercaseCharPrimFactory;
import som.primitives.CharacterPrimsFactory.CompareCharsPrimFactory;
import som.primitives.CharacterPrimsFactory.IsAlphaNumericCharPrimFactory;
import som.primitives.CharacterPrimsFactory.IsDigitCharPrimFactory;
import som.primitives.CharacterPrimsFactory.IsLetterCharPrimFactory;
import som.primitives.CharacterPrimsFactory.IsLowercaseCharPrimFactory;
import som.primitives.CharacterPrimsFactory.IsUppercaseCharPrimFactory;
import som.primitives.CharacterPrimsFactory.NewCharPrimFactory;

public final class CharacterPrimitives extends Primitives {
  public CharacterPrimitives(final boolean displayWarning) { super(displayWarning); }

  @Override
  public void installPrimitives() {
    installInstancePrimitive("asInteger",      AsIntegerCharPrimFactory.getInstance());
    installInstancePrimitive("asString",       AsStringPrimFactory.getInstance());
    installInstancePrimitive("asDigitBase:",   AsDigitCharPrimFactory.getInstance());
    installInstancePrimitive("isDigit",        IsDigitCharPrimFactory.getInstance());
    installInstancePrimitive("isLetter",       IsLetterCharPrimFactory.getInstance());
    installInstancePrimitive("isAlphaNumeric", IsAlphaNumericCharPrimFactory.getInstance());
    installInstancePrimitive("asUppercase",    AsUppercaseCharPrimFactory.getInstance());
    installInstancePrimitive("asLowercase",    AsLowercaseCharPrimFactory.getInstance());
    installInstancePrimitive("isUppercase",    IsUppercaseCharPrimFactory.getInstance());
    installInstancePrimitive("isLowercase",    IsLowercaseCharPrimFactory.getInstance());
    installInstancePrimitive("compareWith:",   CompareCharsPrimFactory.getInstance());
    installClassPrimitive("new:",              NewCharPrimFactory.getInstance());
  }
}
