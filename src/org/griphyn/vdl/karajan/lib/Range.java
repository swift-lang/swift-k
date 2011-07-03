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

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.RootArrayDataNode;
import org.griphyn.vdl.mapping.RootDataNode;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;

public class Range extends VDLFunction {
	public static final SwiftArg PA_FROM = new SwiftArg.Positional("from");
	public static final SwiftArg PA_TO = new SwiftArg.Positional("to");
	public static final SwiftArg OA_STEP = new SwiftArg.Optional("step", new Double(1), Types.FLOAT);

	static {
		setArguments(Range.class, new Arg[] { PA_FROM, PA_TO, OA_STEP });
	}

	public Object function(final VariableStack stack) throws ExecutionException {
		// TODO: deal with expression
		final Type type = PA_FROM.getType(stack);
		final double start = PA_FROM.getDoubleValue(stack);
		final double stop = PA_TO.getDoubleValue(stack);
		final double incr = OA_STEP.getDoubleValue(stack);

		// only deal with int and float
		try {
			final AbstractDataNode handle;

			handle = new RootArrayDataNode(type.arrayType()) {
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
						int index = Integer.parseInt(path.getFirst());
						DSHandle value = new RootDataNode(type);
						value.init(null);
						value.setValue(new Double(start + incr * index));
						value.closeShallow();
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
														value = new RootDataNode(type);
														value.init(null);
														value.setValue(new Double(crt));
														value.closeShallow();
														key = index;
													}

													public Comparable<?> getKey() {
														return new Double(key);
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
			return handle;
		}
		catch (Exception e) {
			throw new ExecutionException(e);
		}
	}
}
