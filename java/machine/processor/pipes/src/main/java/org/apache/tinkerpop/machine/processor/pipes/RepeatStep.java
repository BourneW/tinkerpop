/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tinkerpop.machine.processor.pipes;

import org.apache.tinkerpop.machine.bytecode.compiler.Compilation;
import org.apache.tinkerpop.machine.function.branch.RepeatBranch;
import org.apache.tinkerpop.machine.traverser.Traverser;
import org.apache.tinkerpop.machine.traverser.TraverserSet;
import org.apache.tinkerpop.machine.util.IteratorUtils;

import java.util.Collections;
import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
final class RepeatStep<C, S> extends AbstractStep<C, S, S> {

    private final RepeatBranch<C, S> repeatBranch;
    private final int untilLocation;
    private final int emitLocation;
    private final Compilation<C, S, ?> untilCompilation;
    private final Compilation<C, S, ?> emitCompilation;
    private final Compilation<C, S, S> repeat;
    private Iterator<Traverser<C, S>> repeatIterator = Collections.emptyIterator();
    private TraverserSet<C, S> repeatStarts = new TraverserSet<>();
    private TraverserSet<C, S> outputTraversers = new TraverserSet<>();
    private TraverserSet<C, S> inputTraversers = new TraverserSet<>();
    private final boolean hasStartPredicates;
    private final boolean hasEndPredicates;

    RepeatStep(final Step<C, ?, S> previousStep, final RepeatBranch<C, S> repeatBranch) {
        super(previousStep, repeatBranch);
        this.repeatBranch = repeatBranch;
        this.untilCompilation = repeatBranch.getUntil();
        this.emitCompilation = repeatBranch.getEmit();
        this.repeat = repeatBranch.getRepeat();
        this.untilLocation = repeatBranch.getUntilLocation();
        this.emitLocation = repeatBranch.getEmitLocation();
        this.hasStartPredicates = repeatBranch.hasStartPredicates();
        this.hasEndPredicates = repeatBranch.hasEndPredicates();
    }

    @Override
    public boolean hasNext() {
        this.stageOutput();
        return !this.outputTraversers.isEmpty();
    }

    @Override
    public Traverser<C, S> next() {
        this.stageOutput();
        return this.outputTraversers.remove();
    }

    private boolean stageInput() {
        if (!this.inputTraversers.isEmpty() || this.previousStep.hasNext()) {
            final Traverser<C, S> traverser = this.inputTraversers.isEmpty() ? this.previousStep.next() : this.inputTraversers.remove();
            if (this.hasStartPredicates) {
                if (1 == this.untilLocation) {
                    if (this.untilCompilation.filterTraverser(traverser)) {
                        this.outputTraversers.add(traverser);
                    } else if (2 == this.emitLocation && this.emitCompilation.filterTraverser(traverser)) {
                        this.outputTraversers.add(traverser.repeatDone(this.repeatBranch));
                        this.repeatStarts.add(traverser);
                    } else
                        this.repeatStarts.add(traverser);
                } else if (1 == this.emitLocation) {
                    if (this.emitCompilation.filterTraverser(traverser))
                        this.outputTraversers.add(traverser.repeatDone(this.repeatBranch));
                    if (2 == this.untilLocation && this.untilCompilation.filterTraverser(traverser))
                        this.outputTraversers.add(traverser.repeatDone(this.repeatBranch));
                    else
                        this.repeatStarts.add(traverser);
                }
            } else {
                this.repeatStarts.add(traverser);
            }
            return true;
        }
        return false;
    }

    private void stageOutput() {
        while (this.outputTraversers.isEmpty() && (this.repeatIterator.hasNext() || this.stageInput())) {
            if (this.repeatIterator.hasNext()) {
                final Traverser<C, S> traverser = this.repeatIterator.next().repeatLoop(this.repeatBranch);
                if (this.hasEndPredicates) {
                    if (3 == this.untilLocation) {
                        if (this.untilCompilation.filterTraverser(traverser)) {
                            this.outputTraversers.add(traverser.repeatDone(this.repeatBranch));
                        } else if (4 == this.emitLocation && this.emitCompilation.filterTraverser(traverser)) {
                            this.outputTraversers.add(traverser.repeatDone(this.repeatBranch));
                            this.inputTraversers.add(traverser);
                        } else
                            this.inputTraversers.add(traverser);
                    } else if (3 == this.emitLocation) {
                        if (this.emitCompilation.filterTraverser(traverser))
                            this.outputTraversers.add(traverser.repeatDone(this.repeatBranch));
                        if (4 == this.untilLocation && this.untilCompilation.filterTraverser(traverser))
                            this.outputTraversers.add(traverser.repeatDone(this.repeatBranch));
                        else
                            this.inputTraversers.add(traverser);
                    }
                } else {
                    this.inputTraversers.add(traverser);
                }
            } else {
                this.repeatIterator = this.repeat.getProcessor().iterator(IteratorUtils.removeOnNext(this.repeatStarts.iterator()));
            }
        }
    }

    @Override
    public void reset() {
        this.inputTraversers.clear();
        this.outputTraversers.clear();
    }
}

