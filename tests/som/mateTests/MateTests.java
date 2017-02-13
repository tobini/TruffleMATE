/**
 * Copyright (c) 2015 Guido Chari, gchari@dc.uba.ar
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
package som.mateTests;

import java.util.Arrays;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import som.tests.SomTests;

@RunWith(Parameterized.class)
public class MateTests extends SomTests {

  public MateTests(String testName) {
    super(testName);
  }

  @Parameters
  public static Iterable<Object[]> data() {
    return Arrays.asList(new Object[][] {
        {"Immutability"},
        {"Layout"},
      });
  }

  protected String[] getArguments() {
    String[] arg = {
        "--mate",
        "-activateMate",
        "-cp",
        "Smalltalk:Smalltalk/Mate:Smalltalk/Mate/MOP:Smalltalk/FileSystem/Core:Smalltalk/FileSystem/Disk:TestSuite:TestSuite/MateMOPSuite:Examples/Benchmarks:Examples/Benchmarks/Mate/Immutability:Examples/Benchmarks/Mate/Immutability/Handles:Examples/Benchmarks/Mate/Immutability/DelegationProxies:Examples/Benchmarks/Mate/Layout:TestSuite/FileSystem",
        "TestHarness",
        testName};
    return arg;
  }
}
