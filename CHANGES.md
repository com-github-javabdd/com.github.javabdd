# Changelog for com.github.javabdd

All notable changes to this project will be documented in this file.

## [2.0.1] - unreleased

* Improved license headers, and related changes.

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
