[![Stories in Ready](https://badge.waffle.io/Whaka-project/whakamatautau-util.png?label=ready&title=Ready)](https://waffle.io/Whaka-project/whakamatautau-util)
# whakamatautau-util
Set of Java utils useful for authomation testing and any everyday coding. Part of the "Whaka" project.

## What
Bunch of tools that we've found usable for extended automation-testing or other everyday coding:
* **Assert-builder** (allowing to build an exception with multiple messages before throwing it)
* **Interface-compare** (allowing to compare recursively instances of different classes by common interface, or just a list of common methods)
* **PairWise** (top-level implementation of an algorithm to shuffle data into orthogonal arrays, to minimize number of test launches. See [w:All-pairs testing](https://en.wikipedia.org/wiki/All-pairs_testing) and [w:Orthogonal array testing](https://en.wikipedia.org/wiki/Orthogonal_array_testing))
* **DoubleMath** (allowing to perform null-safe operations, like `#equals`, `#compare`, `#round` and `#roundTo`)
* **Try** (allowing to perform try-catch operations in functional manner)
* **UberStreams** (allowing to create wrappers for default streams, adding some useful method shortenings; and providing `MapStream` that implements stream of entries and provides lots of maps-specific methods)

## Why
We couldn't find existing implementations of tools like multiple-assert, interface-compare and (especially) pair-wise shuffle. So we decided to do the bike.

## Kanban
https://waffle.io/Whaka-project/whakamatautau-util

## Whakamātautau ki te mate!
<img src="http://i.imgur.com/CEAYRqW.jpg" width="400" alt="Whaka!">
