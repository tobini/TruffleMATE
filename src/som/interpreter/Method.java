/**
 * Copyright (c) 2013 Stefan Marr, stefan.marr@vub.ac.be
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
package som.interpreter;

import som.interpreter.nodes.ExpressionNode;
import som.interpreter.nodes.SOMNode;

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;


public final class Method extends Invokable {

  private final LexicalScope currentLexicalScope;

  public Method(final SourceSection sourceSection,
                final ExpressionNode expressions,
                final LexicalScope currentLexicalScope,
                final ExpressionNode uninitialized,
                final DynamicObject method) {
    super(sourceSection, currentLexicalScope.getFrameDescriptor(),
        expressions, uninitialized, method);
    this.currentLexicalScope = currentLexicalScope;
    currentLexicalScope.setMethod(this);
  }

  @Override
  public String toString() {
    SourceSection ss = getSourceSection();
    final String id = String.format("%s:%d", ss.getSource().getName(),
        ss.getStartLine());
    return "Method " + id + "\t@" + Integer.toHexString(hashCode());
  }

  @Override
  public Invokable cloneWithNewLexicalContext(final LexicalScope outerScope) {
    FrameDescriptor inlinedFrameDescriptor = getFrameDescriptor().copy();
    LexicalScope    inlinedCurrentScope = new LexicalScope(
        inlinedFrameDescriptor, outerScope);
    ExpressionNode inlinedBody = SplitterForLexicallyEmbeddedCode.doInline(
        uninitializedBody, inlinedCurrentScope);
    Method clone = new Method(getSourceSection(), inlinedBody,
        inlinedCurrentScope, uninitializedBody, this.belongsToMethod);
    return clone;
  }

  public Invokable cloneAndAdaptToEmbeddedOuterContext(
      final InlinerForLexicallyEmbeddedMethods inliner) {
    LexicalScope currentAdaptedScope = new LexicalScope(
        getFrameDescriptor().copy(), inliner.getCurrentLexicalScope());
    ExpressionNode adaptedBody = InlinerAdaptToEmbeddedOuterContext.doInline(
        uninitializedBody, inliner, currentAdaptedScope);
    ExpressionNode uninitAdaptedBody = NodeUtil.cloneNode(adaptedBody);

    Method clone = new Method(getSourceSection(), adaptedBody,
        currentAdaptedScope, uninitAdaptedBody, this.belongsToMethod);
    return clone;
  }

  public Invokable cloneAndAdaptToSomeOuterContextBeingEmbedded(
      final InlinerAdaptToEmbeddedOuterContext inliner) {
    LexicalScope currentAdaptedScope = new LexicalScope(
        getFrameDescriptor().copy(), inliner.getCurrentLexicalScope());
    ExpressionNode adaptedBody = InlinerAdaptToEmbeddedOuterContext.doInline(
        uninitializedBody, inliner, currentAdaptedScope);
    ExpressionNode uninitAdaptedBody = NodeUtil.cloneNode(adaptedBody);

    Method clone = new Method(getSourceSection(),
        adaptedBody, currentAdaptedScope, uninitAdaptedBody, this.belongsToMethod);
    return clone;
  }

  @Override
  public void propagateLoopCountThroughoutLexicalScope(final long count) {
    assert count >= 0;
    currentLexicalScope.propagateLoopCountThroughoutLexicalScope(count);
    LoopNode.reportLoopCount(this, (count > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) count);
  }

  public SourceSection[] getDefinition() {
    // Should we include an special array of sourceSections for the method definition as in SOMns?
    return new SourceSection[]{this.getSourceSection()};
  }

  public SourceSection getRootNodeSource() {
    ExpressionNode root = SOMNode.unwrapIfNecessary(expressionOrSequence);
    assert root.isMarkedAsRootExpression();
    return root.getSourceSection();
  }

  @Override
  public Node deepCopy() {
    return cloneWithNewLexicalContext(currentLexicalScope.getOuterScopeOrNull());
  }

  public boolean isBlock() {
    // TODO: analyze the best way to implement this method properly
    return false;
  }

  public LexicalScope getLexicalScope() {
    return currentLexicalScope;
  }
}
