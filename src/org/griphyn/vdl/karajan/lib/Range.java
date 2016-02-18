/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import k.rt.ExecutionException;
import k.rt.Stack;
import k.thr.LWThread;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.mapping.nodes.NodeFactory;
import org.griphyn.vdl.mapping.nodes.RootFutureArrayDataNode;
import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;

public class Range extends SwiftFunction {
    private ArgRef<DSHandle> from;
    private ArgRef<DSHandle> to;
    private ArgRef<DSHandle> step;
    
	@Override
    protected Signature getSignature() {
        return new Signature(params("from", "to", optional("step", NodeFactory.newRoot(Field.GENERIC_FLOAT, 1))));
    }

	@Override
	public Object function(Stack stack) {
		// TODO: deal with expression
	    DSHandle from = this.from.getValue(stack);
	    DSHandle to = this.to.getValue(stack);
	    DSHandle step = this.step.getValue(stack);
	    
	    from.waitFor(this);
	    to.waitFor(this);
	    step.waitFor(this);
	    
		final Type type = from.getType();
		final double start = ((Number) from.getValue()).doubleValue();
		final double stop = ((Number) to.getValue()).doubleValue();
		final double incr = ((Number) step.getValue()).doubleValue();

		// only deal with int and float
		try {
			final AbstractDataNode handle;
			final Field valueField = Field.Factory.getImmutableField("rangeItem", type);

			handle = new RootFutureArrayDataNode(Field.Factory.getImmutableField("range", type.arrayType()), null) {
				final DSHandle h = this;				
				{
				    closeShallow();
				}
				
				public Collection<DSHandle> getFields(Path path)
						throws InvalidPathException {
					if (path.size() > 1) {
						throw new InvalidPathException(path, this);
					}
					else if (path.equals(Path.EMPTY_PATH)) {
						return Collections.singletonList((DSHandle) this);
					}
					else {
						int index = (Integer) path.getFirst();
						DSHandle value = NodeFactory.newRoot(valueField, new Double(start + incr * index));
						return Collections.singletonList(value);
					}
				}

				public Map<Comparable<?>, DSHandle> getArrayValue() {
					return new AbstractMap<Comparable<?>, DSHandle>() {
						public Set<Map.Entry<Comparable<?>, DSHandle>> entrySet() {
							return new AbstractSet<Map.Entry<Comparable<?>, DSHandle>>() {
								public Iterator<Map.Entry<Comparable<?>, DSHandle>> iterator() {
									return new Iterator<Map.Entry<Comparable<?>, DSHandle>>() {
										private double crt = start;
										private int index = 0;
										
										public boolean hasNext() {
											return crt <= stop;
										}

										public Map.Entry<Comparable<?>, DSHandle> next() {
											try {
												Map.Entry<Comparable<?>, DSHandle> e = new Map.Entry<Comparable<?>, DSHandle>() {
													private DSHandle value;
													private int key;
													
													{
														if (type == Types.INT) {
														    value = NodeFactory.newRoot(Field.GENERIC_INT, (int) crt);
														}
														else {
														    value = NodeFactory.newRoot(Field.GENERIC_FLOAT, Double.valueOf(crt));
														}
														key = index;
													}

													public Comparable<?> getKey() {
														return Integer.valueOf(key);
													}

													public DSHandle getValue() {
														return value;
													}

													public DSHandle setValue(DSHandle value) {
														throw new UnsupportedOperationException();
													}
												};
												index++;
												crt += incr;
												if (crt > stop) {
												    h.closeShallow();
												}
												return e;
											}
											catch (Exception e) {
												throw new RuntimeException(e);
											}
										}

										public void remove() {
											throw new UnsupportedOperationException();
										}
									};
								}

								public int size() {
									return (int) ((stop - start) / incr);
								}								
							};
						}
					};
				}				
			};
			
			if (PROVENANCE_ENABLED) {
			    String thread = SwiftFunction.getThreadPrefix(LWThread.currentThread());
			    logger.info("ARRAYRANGE thread=" + thread + " array=" + handle.getIdentifier() + " from=" + 
			            from.getIdentifier() + " to=" + to.getIdentifier() + " step=" + step.getIdentifier());
			}
			
			return handle;
		}
		catch (Exception e) {
			throw new ExecutionException(this, e);
		}
	}
}
