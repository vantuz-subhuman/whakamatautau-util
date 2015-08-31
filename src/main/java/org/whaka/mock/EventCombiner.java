package org.whaka.mock;

import static org.whaka.util.UberStreams.*;
import static org.whaka.util.function.Tuple2.*;
import static org.whaka.util.function.Tuple3.*;
import static org.whaka.util.function.Tuple4.*;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.whaka.util.function.Consumer3;
import org.whaka.util.function.Consumer4;
import org.whaka.util.function.Consumer5;
import org.whaka.util.function.Tuple2;
import org.whaka.util.function.Tuple3;
import org.whaka.util.function.Tuple4;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * <p>Class represents an abstraction that performs a call to a stubbed mock method at one and, and combines
 * captured arguments into a single "event" at another end. Shall be used along with the {@link EventCollector}
 * 
 * <p>The main idea and a need for such an abstraction arise from the fact that {@link EventCollector} is abstract
 * enough to work with only a single "event" (moreover listeners with a single argument is 95% of use cases for
 * the collector). But methods with multiple argument shall be supported. Just in case.
 * 
 * <p>EventCombiner represents a call to a method with {@code N>0} arguments, and a combinator function that can combine
 * these {@code N} arguments into a single instance of some type: {@code <Event>}. So when user requests a creation
 * of the EventCombiner - he specifies how many arguments will be captured; as a result combiner generates specified
 * number of Mockito's {@link ArgumentCaptor} instances. Then user has to provide a call to a single specific method
 * in the form of a {@link BiConsumer} where first argument is an instance of the type {@code <Target>}
 * and the second argument is an {@code ArgumentCaptor[]} array of the specified size. In this consumer user
 * should perform a method call and specify which arguments exactly he wants to capture. All provides captors are
 * of the same basic unknown type {@code <?>}, so user have to perform manual cast for each captor.  Also user has to provide
 * a function that takes an {@code Object[]} array as argument and returns an instance of the specified type {@code <Event>}.
 * This function will be called whenever {@link #getValue()} is called, and will receive array of the same size {@code N}
 * specified at the construction; this array will contain all the objects returned by the ArgumentCaptor instances.
 * 
 * <p>See method {@link #forCaptors(int, BiConsumer, Function)} for described basic functionality. But also take a look
 * at the {@link #forValues(int, BiConsumer, Function)} method, allowing you to perform almost the same operation,
 * but automatically initializing captors.
 * 
 * <p>See method {@link #forCaptor(BiConsumer)} allowing to perform a single selective capture.
 * 
 * <p>Also note all the {@link #create(BiConsumer)} methods with a single argument. Those provide the easiest way
 * to create a combiner for multiple arguments.
 * 
 * @see #forValues(int, BiConsumer, Function)
 * @see #forCaptors(int, BiConsumer, Function)
 * @see #forCaptor(BiConsumer)
 * @see #create(BiConsumer)
 * @see #create(Consumer3)
 * @see #create(Consumer4)
 * @see #create(Consumer5)
 * @see EventCollector
 */
public class EventCombiner<Target, Event> implements Consumer<Target> {

	private final ArgumentCaptor<?>[] captors;
	private final BiConsumer<Target, ArgumentCaptor<?>[]> methodCall;
	private final Function<Object[], Event> combiner;
	
	private EventCombiner(int numberOfEvents,
			BiConsumer<Target, ArgumentCaptor<?>[]> methodCall,
			Function<Object[], Event> combiner) {
		Preconditions.checkArgument(numberOfEvents > 0, "Expected positive number of events!");
		this.methodCall = Objects.requireNonNull(methodCall, "Method call cannot be null!");
		this.combiner = Objects.requireNonNull(combiner, "Arguments combiner cannot be null!");
		this.captors = IntStream.range(0, numberOfEvents)
				.mapToObj(i -> ArgumentCaptor.forClass(Object.class))
				.toArray(ArgumentCaptor[]::new);
	}
	
	/**
	 * <p>Basic factory method for an {@link EventCombiner} (see class documentation for basic understanding).
	 *
	 * @param numberOfEvents - number of argument captors to be generated by the combiner
	 * @param methodCall - a consumer that will take an instance of the target class and array of captors
	 * and perform a call to a method to be stubbed.
	 * @param combiner - the combinator function that will take an array of captured argument and convert them
	 * into a single instance of the specified "event" type {@code <E>}
	 * 
	 * @see #forCaptor(BiConsumer)
	 * @see #forValues(int, BiConsumer, Function)
	 * @see #create(BiConsumer)
	 */
	public static <T,E> EventCombiner<T,E> forCaptors(int numberOfEvents,
			BiConsumer<T, ArgumentCaptor<?>[]> methodCall, Function<Object[], E> combiner) {
		return new EventCombiner<>(numberOfEvents, methodCall, combiner);
	}
	
	/**
	 * <p>Pure form of the event collector (backed by the {@link EventCombiner#create(BiConsumer)} method) allows
	 * to stub only methods with one argument. But using a {@link BiPredicate} to indicate stubbed method allows you
	 * to call <i>any</i> method where matcher (passed into a predicate) might be passed as one
	 * of the multiple arguments. Example:
	 * <pre>
	 * 	interface Listener {
	 * 		void event(Integer i, String s);
	 * 	}
	 * 
	 * 	EventCombiner&lt;Listener, String&gt; collector =
	 * 		EventCombiner.create((l,s) -> l.event(Matchers.any(), s));
	 * </pre>
	 * 
	 * <p>The problem with this example is that it won't work as expected. Specifics of the {@link Mockito} functionality
	 * require matchers to be created <b>in the same order</b> as matched arguments. And because the matcher specified
	 * in the predicate by an event collector was created <i>before</i> the one created manually - it still will
	 * try to match <b>the first</b> argument of the called method.
	 * 
	 * <p>To perform such a capture method {@link #forCaptors(int, BiConsumer, Function)} might be used,
	 * but the problem arise is that user will have to specify the number of required captors: 1, and the to specify
	 * a BiPredicate that takes an array of captors, and then a combinator function, that casts an object to the
	 * required type. All of this might be avoided, since we definitely use only a single captor.
	 * 
	 * <p>So {@link BiPredicate} accepted by this method takes an instance of the {@link ArgumentCaptor}
	 * as a second argument. User will have to call {@link ArgumentCaptor#capture()} on it to specify the argument
	 * to be collected. Example:
	 * <pre>
	 * 	interface Listener {
	 * 		void event(Integer i, String s);
	 * 	}
	 * 
	 * 	EventCombiner&lt;Listener, String&gt; collector =
	 * 		EventCombiner.forCaptor((l,c) -> l.event(Matchers.any(), c.capture()));
	 * </pre>
	 * 
	 * <p>In this example combiner's matcher is initiated after the matcher created by the {@link Matchers#any()} call.
	 * As a result it provides you an interesting functionality (relatively easy to implement) that allows you
	 * to capture one of the arguments, completely ignoring others (<code>Mockito's</code> {@link Matchers} got
	 * to be used manually to create matchers for other arguments).
	 * 
	 * @see #forCaptors(int, BiConsumer, Function)
	 */
	@SuppressWarnings("unchecked")
	public static <T,E> EventCombiner<T,E> forCaptor(BiConsumer<T, ArgumentCaptor<E>> methodCall) {
		return forCaptors(1,
				(t, captors) -> methodCall.accept(t, (ArgumentCaptor<E>) captors[0]),
				arr -> (E)arr[0]);
	}
	
	/**
	 * <p>Basically equal to the {@link #forCaptors(int, BiConsumer, Function)} with only difference that
	 * specified {@link BiPredicate} method call receives an array of matchers ({@code Object[]}) instead of
	 * captors. So user don't have to manually call {@link ArgumentCaptor#capture()} method on all of them.
	 * 
	 * <p><b>Note:</b> since all captors get initiated before the call to the consumer - they <b>will definitely
	 * match only {@code N} first arguments</b> of the called method. Where {@code N} is the number of generated
	 * captors. This means that this method will not suffice in case you want to captor <i>non-sequential</i>
	 * arguments!
	 * 
	 * @see #forCaptors(int, BiConsumer, Function)
	 * @see #forCaptor(BiConsumer)
	 */
	public static <T,E> EventCombiner<T,E> forValues(int numberOfEvents,
			BiConsumer<T, Object[]> methodCall,
			Function<Object[], E> combiner) {
		Objects.requireNonNull(methodCall, "Method call cannot be null!");
		return forCaptors(numberOfEvents,
				(t, captors) -> {
					Object[] arr = new Object[captors.length];
					for (int i = 0; i < arr.length; i++)
						arr[i] = captors[i].capture();
					methodCall.accept(t, arr);
				},
				combiner);
	}
	
	/**
	 * <p>The easiest way to create a combiner that generates a single captor and captures a single argument.
	 * Specified {@link BiConsumer} should perform a single method call on the first argument with a second
	 * argument as method parameter.
	 * 
	 * <p>Usability method creating a combiner without any actual combining. Single captor is created, single argument
	 * is captured and then just gets cast to the required type.
	 * 
	 * @see #create(Consumer3)
	 * @see #create(Consumer4)
	 * @see #create(Consumer5)
	 */
	@SuppressWarnings("unchecked")
	public static <T, E> EventCombiner<T, E> create(BiConsumer<T, E> methodCall) {
		Objects.requireNonNull(methodCall, "Method call cannot be null!");
		return forValues(1,
				(t, arr) -> methodCall.accept(t, (E)arr[0]),
				arr -> (E)arr[0]);
	}

	/**
	 * <p>The easiest way to create a combiner that generates two captors and captures two sequential arguments.
	 * Specified {@link Consumer3} should perform a single method call on the first argument with a second and third
	 * arguments as method parameters.
	 * 
	 * <p>Captured arguments are combined into a single {@link Tuple2} instance.
	 * 
	 * @see #create(BiConsumer)
	 * @see #create(Consumer4)
	 * @see #create(Consumer5)
	 */
	@SuppressWarnings("unchecked")
	public static <T, A, B> EventCombiner<T, Tuple2<A, B>> create(Consumer3<T, A, B> methodCall) {
		Objects.requireNonNull(methodCall, "Method call cannot be null!");
		return forValues(2,
				(t, arr) -> methodCall.accept(t, (A)arr[0], (B)arr[1]),
				arr -> tuple2((A)arr[0], (B)arr[1]));
	}
	
	/**
	 * <p>The easiest way to create a combiner that generates three captors and captures three sequential arguments.
	 * Specified {@link Consumer4} should perform a single method call on the first argument with a second, third, and
	 * fourth arguments as method parameters.
	 * 
	 * <p>Captured arguments are combined into a single {@link Tuple3} instance.
	 * 
	 * @see #create(BiConsumer)
	 * @see #create(Consumer3)
	 * @see #create(Consumer5)
	 */
	@SuppressWarnings("unchecked")
	public static <T, A, B, C> EventCombiner<T, Tuple3<A, B, C>> create(Consumer4<T, A, B, C> methodCall) {
		Objects.requireNonNull(methodCall, "Method call cannot be null!");
		return forValues(3,
				(t, arr) -> methodCall.accept(t, (A)arr[0], (B)arr[1], (C)arr[2]),
				arr -> tuple3((A)arr[0], (B)arr[1], (C)arr[2]));
	}
	
	/**
	 * <p>The easiest way to create a combiner that generates four captors and captures four sequential arguments.
	 * Specified {@link Consumer5} should perform a single method call on the first argument with a second, third,
	 * fourth, and fifth arguments as method parameters.
	 * 
	 * <p>Captured arguments are combined into a single {@link Tuple4} instance.
	 * 
	 * @see #create(BiConsumer)
	 * @see #create(Consumer3)
	 * @see #create(Consumer4)
	 */
	@SuppressWarnings("unchecked")
	public static <T, A, B, C, D> EventCombiner<T, Tuple4<A, B, C, D>> create(Consumer5<T, A, B, C, D> methodCall) {
		Objects.requireNonNull(methodCall, "Method call cannot be null!");
		return forValues(4,
				(t, arr) -> methodCall.accept(t, (A)arr[0], (B)arr[1], (C)arr[2], (D)arr[3]),
				arr -> tuple4((A)arr[0], (B)arr[1], (C)arr[2], (D)arr[3]));
	}
	
	/**
	 * <p>Copy of array of all generated {@link ArgumentCaptor} instances.
	 */
	public ArgumentCaptor<?>[] getCaptors() {
		return captors.clone();
	}
	
	/**
	 * {@link BiConsumer} representing a call to the linked method; used to delegate target call.
	 */
	public BiConsumer<Target, ArgumentCaptor<?>[]> getMethodCall() {
		return methodCall;
	}
	
	/**
	 * A function that will be used to combine captured argument into a single instance of the {@code <Event>} type.
	 */
	public Function<Object[], Event> getCombiner() {
		return combiner;
	}
	
	/**
	 * This method delegates specified target to the {@link #getMethodCall()} consumer along with captors
	 * received from {@link #getCaptors()}.
	 */
	@Override
	public void accept(Target t) {
		getMethodCall().accept(t, getCaptors());
	}

	/**
	 * This method requests last captured value from all the captors received from {@link #getCaptors()}
	 * and then applies {@link #getCombiner()} function to them. Result is returned.
	 */
	public Event getValue() {
		Object[] values = stream(getCaptors()).map(c -> c.getValue()).toArray();
		return getCombiner().apply(values);
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("captors", getCaptors().length)
				.addValue(System.identityHashCode(this))
				.toString();
	}
}