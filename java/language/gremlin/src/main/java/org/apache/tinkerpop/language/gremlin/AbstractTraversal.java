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
package org.apache.tinkerpop.language.gremlin;

import org.apache.tinkerpop.machine.Machine;
import org.apache.tinkerpop.machine.bytecode.Bytecode;
import org.apache.tinkerpop.machine.coefficient.Coefficient;
import org.apache.tinkerpop.machine.traverser.Traverser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class AbstractTraversal<C, S, E> implements Traversal<C, S, E> {

    private final Machine machine;
    protected final Bytecode<C> bytecode;
    protected Coefficient<C> currentCoefficient;
    private Iterator<Traverser<C, E>> traversers = null;
    private boolean executed = false;

    // iteration helpers
    private long lastCount = 0L;
    private E lastObject = null;

    public AbstractTraversal(final Machine machine, final Bytecode<C> bytecode, final Coefficient<C> unity) {
        this.machine = machine;
        this.bytecode = bytecode;
        this.currentCoefficient = unity.clone();
    }

    ///////

    protected final <A, B> Traversal<C, A, B> addInstruction(final String op, final Object... args) {
        if (this.executed)
            throw new IllegalStateException("The traversal has already been submitted and can no longer be mutated");
        this.bytecode.addInstruction(this.currentCoefficient, null, op, args);
        this.currentCoefficient.unity();
        return (Traversal<C, A, B>) this;
    }

    private void prepareTraversal() {
        if (!this.executed) {
            this.executed = true;
            this.traversers = this.machine.submit(this.bytecode);
        }
    }

    public Traverser<C, E> nextTraverser() {
        this.prepareTraversal();
        return this.traversers.next(); // TODO: interaction with hasNext/next and counts
    }

    @Override
    public boolean hasNext() {
        this.prepareTraversal();
        return this.lastCount > 0 || this.traversers.hasNext();
    }

    @Override
    public E next() {
        this.prepareTraversal();
        if (this.lastCount > 0) {
            this.lastCount--;
            return this.lastObject;
        } else {
            final Traverser<C, E> traverser = this.traversers.next();
            if (traverser.coefficient().count() > 1) {
                this.lastObject = traverser.object();
                this.lastCount = traverser.coefficient().count() - 1L;
            }
            return traverser.object();
        }
    }

    @Override
    public List<E> toList() {
        final List<E> list = new ArrayList<>();
        while (this.hasNext()) {
            list.add(this.next());
        }
        return list;
    }

    @Override
    public void iterate() {
        try {
            while (true) {
                this.nextTraverser();
            }
        } catch (final NoSuchElementException e) {
            // do nothing
        }
    }

    @Override
    public String toString() {
        return this.bytecode.toString();
    }
}
