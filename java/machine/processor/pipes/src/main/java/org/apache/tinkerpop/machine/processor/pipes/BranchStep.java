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
import org.apache.tinkerpop.machine.function.BranchFunction;
import org.apache.tinkerpop.machine.traverser.Traverser;
import org.apache.tinkerpop.machine.util.EmptyIterator;
import org.apache.tinkerpop.machine.util.MultiIterator;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
final class BranchStep<C, S, E, M> extends AbstractStep<C, S, E> {

    private final Map<Compilation<C, S, ?>, List<Compilation<C, S, E>>> branches;
    private final List<Compilation<C, S, E>> defaultBranches;
    private Iterator<Traverser<C, E>> nextTraversers = EmptyIterator.instance();

    BranchStep(final Step<C, ?, S> previousStep, final BranchFunction<C, S, E> branchFunction) {
        super(previousStep, branchFunction);
        this.branches = branchFunction.getBranches();
        this.defaultBranches = this.branches.getOrDefault(null, Collections.emptyList());
        this.branches.remove(null);
    }

    @Override
    public boolean hasNext() {
        this.stageOutput();
        return this.nextTraversers.hasNext();
    }

    @Override
    public Traverser<C, E> next() {
        this.stageOutput();
        return this.nextTraversers.next();
    }

    private void stageOutput() {
        while (!this.nextTraversers.hasNext() && this.previousStep.hasNext()) {
            boolean found = false;
            this.nextTraversers = new MultiIterator<>();
            final Traverser<C, S> traverser = this.previousStep.next();
            for (final Map.Entry<Compilation<C, S, ?>, List<Compilation<C, S, E>>> entry : this.branches.entrySet()) {
                if (entry.getKey().filterTraverser(traverser)) {
                    found = true;
                    for (final Compilation<C, S, E> branch : entry.getValue()) {
                        ((MultiIterator<Traverser<C, E>>) this.nextTraversers).addIterator(branch.getProcessor().iterator(traverser.clone()));
                    }
                }
            }
            if (!found) {
                for (final Compilation<C, S, E> defaultBranch : this.defaultBranches) {
                    ((MultiIterator<Traverser<C, E>>) this.nextTraversers).addIterator(defaultBranch.getProcessor().iterator(traverser.clone()));
                }
            }
        }
    }

    @Override
    public void reset() {
        this.nextTraversers = EmptyIterator.instance();
    }
}