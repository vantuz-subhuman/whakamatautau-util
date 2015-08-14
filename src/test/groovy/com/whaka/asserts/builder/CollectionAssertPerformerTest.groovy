package com.whaka.asserts.builderimport java.util.function.BiPredicateimport java.util.function.Consumerimport spock.lang.Specificationimport com.whaka.asserts.AssertResultimport com.whaka.util.reflection.comparison.ComparisonPerformerimport com.whaka.util.reflection.comparison.ComparisonPerformersimport com.whaka.util.reflection.comparison.ComparisonResult
class CollectionAssertPerformerTest extends Specification {
	def "construction"() {		given:			Consumer<AssertResult> consumer = Mock(Consumer)			Collection<?> collection = Arrays.asList(1,2,3)		when:			CollectionAssertPerformer performer = new CollectionAssertPerformer(collection, consumer)		then:			0 * consumer.accept(_)			performer.getActual().is(collection)	}	def "is-not-empty success"() {		given:			Consumer<AssertResult> consumer = Mock(Consumer)			CollectionAssertPerformer performer = new CollectionAssertPerformer(collection, consumer)		when:			AssertResultConstructor messageConstructor = performer.isNotEmpty()		then:			0 * consumer.accept(_)			messageConstructor.getAssertResult() == null		where:			collection << [Arrays.asList(1,2), [2,3], new HashSet<String>(["qwe"])]	}	def "is-not-empty fail"() {		given:			AssertResult capturedResult = null			Consumer<AssertResult> consumer = Mock(Consumer)			CollectionAssertPerformer performer = new CollectionAssertPerformer(collection, consumer)		when:			AssertResultConstructor messageConstructor = performer.isNotEmpty()		then:			1 * consumer.accept(_) >> {args -> capturedResult = args[0]}			messageConstructor.getAssertResult().is(capturedResult)		and:			capturedResult.getActual().is(collection)			capturedResult.getExpected() == "Not []"			capturedResult.getMessage() == CollectionAssertPerformer.MESSAGE_NOT_EMPTY_COLLECTION_EXPECTED			capturedResult.getCause() == null		where:			collection << [null, Collections.emptyList(), Collections.emptySet(), Arrays.asList(), []]	}	def "is-empty success"() {		given:			Consumer<AssertResult> consumer = Mock(Consumer)			CollectionAssertPerformer performer = new CollectionAssertPerformer(collection, consumer)		when:			AssertResultConstructor messageConstructor = performer.isEmpty()		then:			0 * consumer.accept(_)			messageConstructor.getAssertResult() == null		where:			collection << [Collections.emptyList(), Collections.emptySet(), Arrays.asList(), []]	}	def "is-empty fail"() {		given:			AssertResult capturedResult = null			Consumer<AssertResult> consumer = Mock(Consumer)			CollectionAssertPerformer performer = new CollectionAssertPerformer(collection, consumer)		when:			AssertResultConstructor messageConstructor = performer.isEmpty()		then:			1 * consumer.accept(_) >> {args -> capturedResult = args[0]}			messageConstructor.getAssertResult().is(capturedResult)		and:			capturedResult.getActual().is(collection)			capturedResult.getExpected() == []			capturedResult.getMessage() == CollectionAssertPerformer.MESSAGE_EMPTY_COLLECTION_EXPECTED			capturedResult.getCause() == null		where:			collection << [null, Arrays.asList(1,2), [2,3], new HashSet<String>(["qwe"])]	}	def "is-empty-or-null"() {		given:			AssertResult capturedResult = null			Consumer<AssertResult> consumer = Mock(Consumer)			CollectionAssertPerformer performerEmpty = new CollectionAssertPerformer([], consumer)			CollectionAssertPerformer performerNull = new CollectionAssertPerformer(null, consumer)		when: "is-empty is called agains empty collection"			performerEmpty.isEmpty()		then: "assert is passed"			0 * consumer.accept(_)		when: "is-null is called against null value"			performerNull.isEmpty()		then: "assert is failed"			1 * consumer.accept(_)		when: "is-null-or-empty is called against either empty collection or null value"			performerEmpty.isEmptyOrNull()			performerNull.isEmptyOrNull()		then: "assert is passed"			0 * consumer.accept(_)		when: "is-null-or-empty is called against not-empty collection"			new CollectionAssertPerformer([42], consumer).isEmptyOrNull()		then: "assert result is received with specific message, and empty collection as expected value"			1 * consumer.accept(_) >> {AssertResult result -> capturedResult = result}			capturedResult.getActual() == [42]			capturedResult.getExpected() == []			capturedResult.getMessage() == CollectionAssertPerformer.MESSAGE_EMPTY_OR_NULL_COLLECTION_EXPECTED			capturedResult.getCause() == null	}	def "is-size success"() {		given:			Consumer<AssertResult> consumer = Mock(Consumer)			CollectionAssertPerformer performer = new CollectionAssertPerformer(collection, consumer)		when:			AssertResultConstructor messageConstructor = performer.isSize(collectionSize)		then:			0 * consumer.accept(_)			messageConstructor.getAssertResult() == null		where:			collection			|		collectionSize			[]					|		0			[1]					|		1			[1,2,3]				|		3			[null, null]		|		2			[[22],[12,42]]		|		2	}	def "is-size fail"() {		given:			AssertResult capturedResult = null			Consumer<AssertResult> consumer = Mock(Consumer)			CollectionAssertPerformer performer = new CollectionAssertPerformer(collection, consumer)		when:			AssertResultConstructor messageConstructor = performer.isSize(collectionSize)		then:			1 * consumer.accept(_) >> {args -> capturedResult = args[0]}			messageConstructor.getAssertResult().is(capturedResult)		and:			capturedResult.getActual().is(collection)			capturedResult.getExpected() == "Size ${collectionSize}"			capturedResult.getMessage() == CollectionAssertPerformer.MESSAGE_ILLEGAL_COLLECTION_SIZE			capturedResult.getCause() == null		where:			collection			|		collectionSize			[]					|		-10			[]					|		-1			[]					|		12			[1]					|		10			[1,2,3]				|		0			[null, null]		|		0			[null]				|		0			[[22],[12,42]]		|		3	}	def "contains-any success"() {		given:			Consumer<AssertResult> consumer = Mock(Consumer)			CollectionAssertPerformer performer = new CollectionAssertPerformer(collection, consumer)		when:			AssertResultConstructor messageConstructor = performer.containsAny(anyOf)		then:			0 * consumer.accept(_)			messageConstructor.getAssertResult() == null		where:			collection			|		anyOf			[]					|		[]			[1,2,3]				|		[]			[1,2,3]				|		[9,8,2]			[1,2,3]				|		[2]			[null,null]			|		[null]			[null]				|		[null]			[arr(1,2),arr(3,4)]	|		[arr(3,4),arr(5,6)]		// < default deep predicate is used	}	def "contains-any fail"() {		given:			AssertResult capturedResult = null			Consumer<AssertResult> consumer = Mock(Consumer)			CollectionAssertPerformer performer = new CollectionAssertPerformer(collection, consumer)		when:			AssertResultConstructor messageConstructor = performer.containsAny(anyOf)		then:			1 * consumer.accept(_) >> {args -> capturedResult = args[0]}			messageConstructor.getAssertResult().is(capturedResult)		and:			capturedResult.getActual().is(collection)			capturedResult.getExpected() == "Any of ${anyOf}"			capturedResult.getMessage() == CollectionAssertPerformer.MESSAGE_COLLECTION_NOT_CONTAINS_EXPECTED_VALUES			capturedResult.getCause() == null		where:			collection			|		anyOf			null				|		[]			null				|		[12]			[]					|		[12]			[]					|		[null]			[1,2,3]				|		[4]			[1,2,3]				|		[9,8,7]			[1,2,3]				|		[0,null,"qwe"]			[null,null]			|		[42]			[arr(1,2),arr(3,4)]	|		[arr(7,8),arr(5,6)]	}	def "contains-all success"() {		given:			Consumer<AssertResult> consumer = Mock(Consumer)			CollectionAssertPerformer performer = new CollectionAssertPerformer(collection, consumer)		when:			AssertResultConstructor messageConstructor = performer.containsAll(allOf)		then:			0 * consumer.accept(_)			messageConstructor.getAssertResult() == null		where:			collection			|		allOf			[]					|		[]			[null]				|		[]			[42]				|		[]			[1,2,3]				|		[1,2]			[1,2,3]				|		[3,2]			[1,2,3]				|		[3,1]			[1,2,3]				|		[3]			[1,2,3,4,5]			|		[1,4,5]			[null,null]			|		[null]			[arr(1,2),arr(3,4)]	|		[arr(3,4)]		// < default deep predicate is used	}	def "contains-all fail"() {		given:			AssertResult capturedResult = null			Consumer<AssertResult> consumer = Mock(Consumer)			CollectionAssertPerformer performer = new CollectionAssertPerformer(collection, consumer)		when:			AssertResultConstructor messageConstructor = performer.containsAll(allOf)		then:			1 * consumer.accept(_) >> {args -> capturedResult = args[0]}			messageConstructor.getAssertResult().is(capturedResult)		and:			capturedResult.getActual().is(collection)			capturedResult.getExpected() == "All of ${allOf}"			capturedResult.getMessage() == CollectionAssertPerformer.MESSAGE_COLLECTION_NOT_CONTAINS_EXPECTED_VALUES			capturedResult.getCause() == null		where:			collection			|		allOf			null				|		[]			null				|		[12]			[]					|		[12]			[]					|		[null]			[1,2,3]				|		[4]			[1,2,3]				|		[9,8,7]			[1,2,3]				|		[9,8,7,1]			[1,2,3]				|		[9,8,7,1,2,3]			[1,2,3]				|		[0,null,"qwe"]			[null,null]			|		[42]			[null,null]			|		[null,null,null]			[arr(1,2),arr(3,4)]	|		[arr(1,2),arr(5,6)]	}	def "contains success"() {		given:			Consumer<AssertResult> consumer = Mock(Consumer)			CollectionAssertPerformer performer = new CollectionAssertPerformer(collection, consumer)		when:			AssertResultConstructor messageConstructor = performer.contains(expected)		then:			0 * consumer.accept(_)			messageConstructor.getAssertResult() == null		where:			collection			|		expected			[null]				|		null			[42]				|		42			[1,2,3]				|		1			[1,2,3]				|		2			[1,2,3]				|		3			[null,null]			|		null			[arr(1,2),arr(3,4)]	|		arr(3,4)		// < default deep predicate is used	}	def "contains fail"() {		given:			AssertResult capturedResult = null			Consumer<AssertResult> consumer = Mock(Consumer)			CollectionAssertPerformer performer = new CollectionAssertPerformer(collection, consumer)		when:			AssertResultConstructor messageConstructor = performer.contains(expected)		then:			1 * consumer.accept(_) >> {args -> capturedResult = args[0]}			messageConstructor.getAssertResult().is(capturedResult)		and:			capturedResult.getActual().is(collection)			capturedResult.getExpected() == "All of ${[expected]}"			capturedResult.getMessage() == CollectionAssertPerformer.MESSAGE_COLLECTION_NOT_CONTAINS_EXPECTED_VALUES			capturedResult.getCause() == null		where:			collection			|		expected			null				|		null			null				|		42			[]					|		12			[]					|		null			[1,2,3]				|		4			[1,2,3]				|		9			[1,2,3]				|		"qwe"			[null,null]			|		42			[null,null]			|		""			[arr(1,2),arr(3,4)]	|		arr(5,6)	}	def "contains-equal-elements success"() {		given:			Consumer<AssertResult> consumer = Mock(Consumer)			CollectionAssertPerformer performer = new CollectionAssertPerformer(collection, consumer)		when:			AssertResultConstructor messageConstructor = performer.containsSameElements(equalCollection)		then:			0 * consumer.accept(_)			messageConstructor.getAssertResult() == null		where:			collection			|		equalCollection			[]					|		[]			[null]				|		[null]			[null, null]		|		[null, null]			[42]				|		[42]			[1,2,3]				|		[1,2,3]			[1,2,3]				|		[3,2,1]			[1,2,3]				|		[3,1,2]			[null,null,1]		|		[null,1,null]			[arr(1,2),arr(3,4)]	|		[arr(3,4),arr(1,2)]		// < default deep predicate is used	}	def "contains-equal-elements fail"() {		given:			AssertResult capturedResult = null			Consumer<AssertResult> consumer = Mock(Consumer)			CollectionAssertPerformer performer = new CollectionAssertPerformer(collection, consumer)		when:			AssertResultConstructor messageConstructor = performer.containsSameElements(notEqualCollection)		then:			1 * consumer.accept(_) >> {args -> capturedResult = args[0]}			messageConstructor.getAssertResult().is(capturedResult)		and:			capturedResult.getActual().is(collection)			capturedResult.getExpected() == notEqualCollection			capturedResult.getMessage() == CollectionAssertPerformer.MESSAGE_COLLECTION_NOT_CONTAINS_EXPECTED_VALUES			capturedResult.getCause() == null		where:			collection			|		notEqualCollection			null				|		[]			null				|		[12]			[]					|		[12]			[]					|		[null]			[1,2,3]				|		[4]			[1,2,3]				|		[9,8,7]			[1,2,3]				|		[9,8,7,1]			[1,2,3]				|		[9,8,7,1,2,3]			[1,2,3]				|		[0,null,"qwe"]			[null,null]			|		[42]			[null,null]			|		[null,null,null]			[arr(1,2),arr(3,4)]	|		[arr(1,2),arr(5,6)]			[arr(1,2),arr(3,4)]	|		[arr(1,2),arr(3,4),false]	}	def "contains-any/all with predicate"() {		given:			AssertResult capturedResult = null			Consumer<AssertResult> consumer = Mock(Consumer)		and:			BiPredicate<?, Object> falsePredicate = Mock(BiPredicate)			BiPredicate<?, Object> truePredicate = Mock(BiPredicate)			BiPredicate<?, Object> shallowPredicate = Objects.&equals			falsePredicate.test(_, _) >> false			truePredicate.test(_, _) >> true		and:			CollectionAssertPerformer<?> performer123 = new CollectionAssertPerformer([1,2,3], consumer)			CollectionAssertPerformer<?> performerArr = new CollectionAssertPerformer([arr(1,2)], consumer)		when:			performer123.contains(42, truePredicate)			performer123.containsAny([5,6,7,8], truePredicate)			performer123.containsAll([5,6], truePredicate)		then:			0 * consumer.accept(_)		when:			performer123.contains(2, falsePredicate)			performer123.containsAny([1,2,3,4], falsePredicate)			performer123.containsAll([1,2], falsePredicate)		then:			3 * consumer.accept(_)		when:			performerArr.contains(arr(1,2), shallowPredicate)			performerArr.containsAny([arr(1,2)], shallowPredicate)			performerArr.containsAll([arr(1,2)], shallowPredicate)		then:			3 * consumer.accept(_)	}	def "contains-equal with predicate"() {		given:			AssertResult capturedResult = null			Consumer<AssertResult> consumer = Mock(Consumer)		and:			ComparisonPerformer<?> falsePerformer = Mock()			ComparisonPerformer<?> truePerformer = Mock()			ComparisonPerformer<?> shallowPerformer = ComparisonPerformers.fromPredicate(Objects.&equals)			falsePerformer.compare(_, _) >> new ComparisonResult(null, null, null, false)			truePerformer.compare(_, _) >> new ComparisonResult(null, null, null, true)		and:			CollectionAssertPerformer<?> performer123 = new CollectionAssertPerformer([1,2,3], consumer)			CollectionAssertPerformer<?> performerArr = new CollectionAssertPerformer([arr(1,2)], consumer)		when:			performer123.containsSameElements([5,6,7], truePerformer)		then:			0 * consumer.accept(_)		when:			performer123.containsSameElements([1,2,3], falsePerformer)		then:			1 * consumer.accept(_)		when:			performerArr.containsSameElements([arr(1,2)], shallowPerformer)		then:			1 * consumer.accept(_)	}	def "each assert place it's message as message-constructor base"() {		given:			Consumer<AssertResult> consumer = Mock(Consumer)		expect:			new CollectionAssertPerformer(null, consumer).isNotEmpty()				.getAssertResult().getMessage() == CollectionAssertPerformer.MESSAGE_NOT_EMPTY_COLLECTION_EXPECTED			new CollectionAssertPerformer(null, consumer).isEmpty()				.getAssertResult().getMessage() == CollectionAssertPerformer.MESSAGE_EMPTY_COLLECTION_EXPECTED			new CollectionAssertPerformer([42], consumer).isEmptyOrNull()				.getAssertResult().getMessage() == CollectionAssertPerformer.MESSAGE_EMPTY_OR_NULL_COLLECTION_EXPECTED			new CollectionAssertPerformer([42], consumer).isSize(10)				.getAssertResult().getMessage() == CollectionAssertPerformer.MESSAGE_ILLEGAL_COLLECTION_SIZE			new CollectionAssertPerformer([10], consumer).contains(42)				.getAssertResult().getMessage() == CollectionAssertPerformer.MESSAGE_COLLECTION_NOT_CONTAINS_EXPECTED_VALUES			new CollectionAssertPerformer([10], consumer).containsAny([42])				.getAssertResult().getMessage() == CollectionAssertPerformer.MESSAGE_COLLECTION_NOT_CONTAINS_EXPECTED_VALUES			new CollectionAssertPerformer([10], consumer).containsAll([42])				.getAssertResult().getMessage() == CollectionAssertPerformer.MESSAGE_COLLECTION_NOT_CONTAINS_EXPECTED_VALUES			new CollectionAssertPerformer([10], consumer).containsSameElements([42])				.getAssertResult().getMessage() == CollectionAssertPerformer.MESSAGE_COLLECTION_NOT_CONTAINS_EXPECTED_VALUES	}	private int[] arr(int[] ints) {		return ints	}}