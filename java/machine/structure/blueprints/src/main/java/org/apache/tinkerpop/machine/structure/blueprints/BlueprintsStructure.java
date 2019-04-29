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
package org.apache.tinkerpop.machine.structure.blueprints;

import org.apache.tinkerpop.machine.bytecode.compiler.BytecodeCompiler;
import org.apache.tinkerpop.machine.strategy.Strategy;
import org.apache.tinkerpop.machine.structure.Structure;
import org.apache.tinkerpop.machine.structure.StructureFactory;
import org.apache.tinkerpop.machine.structure.blueprints.bytecode.compiler.BlueprintsCompiler;
import org.apache.tinkerpop.machine.structure.blueprints.strategy.provider.BlueprintsVerticesStrategy;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class BlueprintsStructure implements StructureFactory {

    private static final List<BytecodeCompiler> COMPILERS = List.of(BlueprintsCompiler.instance());

    public BlueprintsStructure(final Map<String, Object> configuration) {

    }

    @Override
    public Structure mint() {
        return new Blueprints();
    }

    @Override
    public Set<Strategy<?>> getStrategies() {
        return Set.of(new BlueprintsVerticesStrategy());
    }

    @Override
    public List<BytecodeCompiler> getCompilers() {
        return COMPILERS;
    }

}
