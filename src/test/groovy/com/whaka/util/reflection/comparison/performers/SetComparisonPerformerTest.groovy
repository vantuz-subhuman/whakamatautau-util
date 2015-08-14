package com.whaka.util.reflection.comparison.performers

import spock.lang.Specification

import com.whaka.util.reflection.comparison.ComparisonPerformer
import com.whaka.util.reflection.comparison.ComparisonPerformers
import com.whaka.util.reflection.comparison.ComparisonResult
import com.whaka.util.reflection.comparison.ComplexComparisonResult
import com.whaka.util.reflection.properties.ClassPropertyKey

class SetComparisonPerformerTest extends Specification {

	def "construction"() {
		given:
			ComparisonPerformer<?> delegate = Mock()
		when:
			SetComparisonPerformer<?> performer = new SetComparisonPerformer(delegate)
		then:
			performer.getElementPerformer().is(delegate)
	}

	def "perform comparison - null check"() {
		given:
			ComparisonPerformer<?> delegate = Mock()
			SetComparisonPerformer<?> performer = new SetComparisonPerformer(delegate)
			Set<Object> set = [1, false, "qwe"]
			def result = null

		when: "performer is called with two nulls"
			result = performer.compare(null, null)
		then: "delegate is not used"
			0 * delegate._
		and: "result is successful"
			checkResult(result, null, null, performer, true)

		when: "one of the arguments is null"
			result = performer.compare(set, null)
		then: "delegate is not used"
			0 * delegate._
		and: "result is failed"
			checkResult(result, set, null, performer, false)

		when: result = performer.compare(null, set)
		then: 0 * delegate._
		and: checkResult(result, null, set, performer, false)

		when: "performer is called with two 'identical' collections"
			result = performer.compare(set, set)
		then: "delegate is not used"
			0 * delegate._
		and: "result is successful"
			checkResult(result, set, set, performer, true)
	}

	def "perform comparison - length check"() {
		given:
			ComparisonPerformer<?> delegate = Mock()
			SetComparisonPerformer<?> performer = new SetComparisonPerformer(delegate)
			Collection<Object> col1 = [1, false, "qwe"]
			List<Object> col2 = [1, false]
			def result = null

		when: "performer is called with collections of different size"
			result = performer.compare(col1, col2)
		then: "delegate is not used"
			0 * delegate._
		and: "result is failed"
			checkResult(result, col1, col2, performer, false)
		and: "result is complex, and contains single sub result"
			result instanceof ComplexComparisonResult
			result.getPropertyResults().size() == 1
		and: "sub result with key 'Collection#size' is failed, and contains corresponding length values"
			ComparisonResult lengthResult = result.getPropertyResults()[new ClassPropertyKey("size", Collection)]
			checkResult(lengthResult, 3, 2, ComparisonPerformers.DEEP_EQUALS, false)
	}

	def "perform comparison - expected not matched"() {
		given:
			ComparisonPerformer<?> delegate = Mock()
			SetComparisonPerformer<?> performer = new SetComparisonPerformer(delegate)
		and: "performer may process ANY collections, not only sets"
			Set<Object> actual = ["qaz", 1, false]
			List<Object> expected = [1, false, "qwe"]
		and:
			ComparisonResult falseResult = new ComparisonResult(null, null, null, false)
			ComparisonResult trueResult = new ComparisonResult(null, null, null, true)

		when: "performer is called with two collections of any size"
			def result = performer.compare(actual, expected)
		then: "delegate is called with first elements of both collections"
			1 * delegate.compare("qaz", 1) >> falseResult
		and: "if delegate returned NOT successfult result - comparison continue for the next EXPECTED element"
			1 * delegate.compare(1, 1) >> trueResult
		and: "if delegate returns SUCCESSFUL result - comparison continue for the next ACTUAL element"
			1 * delegate.compare("qaz", false) >> falseResult
		and: "previously matched EXPECTED elements are ignored"
			1 * delegate.compare(false, false) >> trueResult
		and: "if actual element was not matched with any expected element"
			1 * delegate.compare("qaz", "qwe") >> falseResult
		and: "final result is complex and contains single sub result"
			checkResult(result, actual, expected, performer, false)
			result instanceof ComplexComparisonResult
			def map = result.getPropertyResults()
			map.size() == 1
		and: "sub result is matched by the key 'Collection#contains'"
			ComparisonResult subResult = map[new ClassPropertyKey("contains", Collection)]
		and: "sub result contains not matched 'expected' value and null 'actual' value"
			checkResult(subResult, null, "qwe", delegate, false)
	}

	def "perform comparison - success"() {
		given:
			ComparisonPerformer<?> delegate = Mock()
			SetComparisonPerformer<?> performer = new SetComparisonPerformer(delegate)
		and: "expected has more elements than actual"
			Collection<Object> actual = [1, false]
			Collection<Object> expected = [1, false]
		and:
			ComparisonResult trueResult = new ComparisonResult(null, null, null, true)

		when:
			def result = performer.compare(actual, expected)
		then:
			1 * delegate.compare(1, 1) >> trueResult
			1 * delegate.compare(false, false) >> trueResult
		and:
			checkResult(result, actual, expected, performer, true)
	}

	void checkResult(ComparisonResult result, Object actual, Object expected, ComparisonPerformer performer, boolean success) {
		assert result.getActual() == actual
		assert result.getExpected() == expected
		assert result.getComparisonPerformer().is(performer)
		assert result.isSuccess() == success
	}
}
