# Changelog for com.github.javabdd

All notable changes to this project will be documented in this file.

## [9.0.0] - 2024-09-26

* `BDD.pathCount` and `BDD.satCount` now return a `BigInteger` rather than a `double`, giving better precision for larger BDDs, and preventing wrong results for very large BDDs, at the cost of being a bit slower and requiring more memory for the cache in which the results for these operations are stored.
* `BDDDomain.getVarIndices` methods no longer produce invalid results in case of too many satisfying assignments to fit in a Java array, but instead throw an exception.
* `BDDFactory` now has `setSaturationCallback` and `unsetSaturationCallback` methods, that allow configuring a callback for the various saturation reachability computation operations.
  The callback is invoked after each application of a transition during saturation reachability computations, allowing to print debug information, check for termination requests, and so on.

## [8.0.0] - 2024-08-01

* Several new operations for BDDs were added, namely `relnextUnion`, `relprevUnion`, `saturationForward`, `boundedSaturationForward`, `saturationBackward` and `boundedSaturationBackward`.
* Fixed an integer overflow bug, which may trigger while checking for the need to resize the node array, causing large node arrays to grow unnecessary.

## [7.0.0] - 2024-06-11

* Several new operations for BDDs were added, namely `relnext`, `relnextIntersection`, `relprev` and `relprevIntersection`.
* A proper `OutOfMemoryError` is now thrown when the BDD nodes array is to be resized larger than the Java array limit, rather than crashing with a `NegativeArraySizeException`.
* The BDD node array can now be resized to the largest size supported by Java, before getting an `OutOfMemoryError` on the next resize, thus allowing slightly larger problems to be solved with JavaBDD.
* Extended the `README` file with information about where to find the plugin on Maven Central.
* Improved the copyright headers in source files.

## [6.0.0] - 2023-06-14

* Inverted the meaning of the operation cache ratio option, and improved its precision and documentation.

## [5.0.0] - 2023-04-25

* Added an OSGi-compatible manifest.
* Improved Maven metadata.

## [4.0.0] - 2023-02-16

* Added maximum memory statistics, which are a best-effort approximation only.
* Small fix in README file.

## [3.0.0] - 2022-04-27

* Improved license headers, and related changes.
* Completely reworked the statistics callbacks API.
* No statistics callbacks are registered by default any more, but they are still available to be registered manually.
* Added statistics callbacks for operation cache statistics, maximum used BDD node statistics and continuous performance statistics.
* Removed non-callback use of garbage collection statistics, variable reordering statistics and continuous performance statistics.
* The maximum used BDD nodes statistics are now properly copied when a `JFactory` is cloned.

## [2.0.0] - 2021-10-28
* Added CHANGES.md to track changes between releases.
* Added the collection of platform-independent performance statistics.
* Added `bdd_used_nodes_count()` that counts the number of used nodes.
* Replaced static `CACHESTATS` by non-static `cachestats.enabled`, which is adaptable by user and configurable per BDD factory instance.
* Removed global JVM shutdown hook, that prints cache statistics to `stdout`, from `JFactory`.
* Cache statistics are now measured using `longs` rather than `ints` to reduce integer overflow issues.
* Added `opAccess` statistic to cache statistics.
* Added functionality to reset statistics.
* Removed `MicroFactory`, `UberMicroFactory`, `TypedBDDFactory` and `TestBDDFactory`.
* Upgrade to Java 11. Java 11 is now required to use this library.
* Cleaned up source formatting, warnings, etc.
* Enabled Checkstyle static code checks.
* Fixed operation cache hashing.
* Various other small code improvements.
* Most stdout printing now disabled by default.
* Removed `BDDFactory.getVersion`.

## [1.0.1] - 2020-03-17
* Updated SCM URL for proper Maven Central metadata.

## [1.0.0] - 2020-03-13
* This project is a fork of the JavaBDD project on Sourceforge (see https://sourceforge.net/projects/javabdd/ and http://javabdd.sourceforge.net/), based on trunk revision r483 from 2011-11-24.
* Kept only the pure Java implementations, removing the interfaces to the JDD, BuDDy, CUDD and CAL libraries.
* Cleaned up the project setup.
* Adapted project to allow deployment to Maven Central.
* First release of com.github.javabdd.
