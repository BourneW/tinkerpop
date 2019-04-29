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
package org.apache.tinkerpop.machine.function.initial;

import org.apache.tinkerpop.machine.bytecode.Instruction;
import org.apache.tinkerpop.machine.coefficient.Coefficient;
import org.apache.tinkerpop.machine.function.AbstractFunction;
import org.apache.tinkerpop.machine.function.InitialFunction;
import org.apache.tinkerpop.machine.structure.table.TDatabase;
import org.apache.tinkerpop.machine.util.StringFactory;

import java.util.Iterator;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public final class DbInitial<C> extends AbstractFunction<C> implements InitialFunction<C, TDatabase> {

    private TDatabase database;

    private DbInitial(final Coefficient<C> coefficient, final String label, final TDatabase database) {
        super(coefficient, label);
        this.database = database;
    }


    @Override
    public Iterator<TDatabase> get() {
        return List.of(this.database).iterator();
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ this.database.hashCode();
    }

    @Override
    public String toString() {
        return StringFactory.makeFunctionString(this, this.database);
    }

    public static <C> DbInitial<C> compile(final Instruction<C> instruction) {
        return new DbInitial<>(instruction.coefficient(), instruction.label(), (TDatabase) instruction.args()[0]);
    }
}
