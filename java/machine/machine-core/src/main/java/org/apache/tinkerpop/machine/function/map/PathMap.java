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
package org.apache.tinkerpop.machine.function.map;

import org.apache.tinkerpop.machine.bytecode.Instruction;
import org.apache.tinkerpop.machine.bytecode.compiler.Compilation;
import org.apache.tinkerpop.machine.bytecode.compiler.CompilationCircle;
import org.apache.tinkerpop.machine.coefficient.Coefficient;
import org.apache.tinkerpop.machine.function.AbstractFunction;
import org.apache.tinkerpop.machine.function.MapFunction;
import org.apache.tinkerpop.machine.traverser.Traverser;
import org.apache.tinkerpop.machine.traverser.path.BasicPath;
import org.apache.tinkerpop.machine.traverser.path.Path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public final class PathMap<C, S> extends AbstractFunction<C> implements MapFunction<C, S, Path> {

    private final List<String> pathLabels;
    private CompilationCircle<C, Object, Object> byCompilations;
    private final boolean hasPathLabels;
    private final boolean hasByCompilations;


    private PathMap(final Coefficient<C> coefficient, final String label, final List<String> pathLabels, final List<Compilation<C, Object, Object>> byCompilations) {
        super(coefficient, label);
        this.pathLabels = pathLabels;
        this.byCompilations = new CompilationCircle<>(byCompilations);
        this.hasPathLabels = !pathLabels.isEmpty();
        this.hasByCompilations = !this.byCompilations.isEmpty();
    }

    @Override
    public Path apply(final Traverser<C, S> traverser) {
        if (!this.hasPathLabels && !this.hasByCompilations)
            return traverser.path();
        else {
            final Path oldPath = traverser.path();
            final Path newPath = new BasicPath();
            if (this.hasPathLabels) {
                for (final String label : this.pathLabels) {
                    newPath.add(label, this.byCompilations.processObject(oldPath.get(Path.Pop.last, label)));
                }
            } else {
                for (int i = 0; i < oldPath.size(); i++) {
                    newPath.add(oldPath.label(i), this.byCompilations.processObject(oldPath.object(i)));
                }
            }
            return newPath;
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ this.pathLabels.hashCode() ^ this.byCompilations.hashCode();
    }

    @Override
    public PathMap<C, S> clone() {
        final PathMap<C, S> clone = (PathMap<C, S>) super.clone();
        clone.byCompilations = this.byCompilations.clone();
        return clone;
    }

    public static <C, S, E> MapFunction<C, S, E> compile(final Instruction<C> instruction) {
        if (Arrays.stream(instruction.args()).anyMatch("|"::equals)) {
            final List<String> labels = new ArrayList<>();
            final List<Compilation<C, Object, Object>> compilations = new ArrayList<>();
            boolean processingLabels = true;
            for (final Object arg : instruction.args()) {
                if (arg.equals("|")) {
                    processingLabels = false;
                    continue;
                }
                if (processingLabels)
                    labels.add((String) arg);
                else
                    compilations.add(Compilation.compile(arg));
            }
            return (MapFunction<C, S, E>) new PathMap<>(instruction.coefficient(), instruction.label(), labels, compilations);
        } else {
            return PathObjectMap.compile(instruction);
        }

    }
}
