package org.whaka.util

import spock.lang.Specification

class AbstractInitializableAndDestroyableTest extends Specification {

	def "test"() {
		given:
			AbstractInitializableAndDestroyable init = Spy(AbstractInitializableAndDestroyable)

		// pre-initialized

		expect:
			!init.isInitialized()
			!init.isDestroyed()
			init.getDestructionStackTrace().length == 0

		when: init.assertInitialized()
		then: thrown(IllegalStateException)

		when: init.assertNotDestroyed()
		then: notThrown(IllegalStateException)

		when:
			init.initialize()
		then:
			1 * init.initialize()
			1 * init.doInitialize()
			0 * init._

		// working

		when:
			init.initialize()
		then:
			1 * init.initialize()
			0 * init._

		expect:
			init.isInitialized()
			!init.isDestroyed()
			init.getDestructionStackTrace().length == 0

		when: init.assertInitialized()
		then: notThrown(IllegalStateException)

		when: init.assertNotDestroyed()
		then: notThrown(IllegalStateException)

		when:
			init.destroy()
		then:
			1 * init.destroy()
			1 * init.doDestroy()
			0 * init._

		// destroyed

		when:
			init.initialize()
			init.destroy()
		then:
			1 * init.initialize()
			1 * init.destroy()
			0 * init._

		expect:
			init.isInitialized()
			init.isDestroyed()
			init.getDestructionStackTrace().length > 0

		when: init.assertInitialized()
		then: notThrown(IllegalStateException)

		when: init.assertNotDestroyed()
		then: thrown(IllegalStateException)
	}
}
