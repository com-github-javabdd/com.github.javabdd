# Changelog for com.github.javabdd

All notable changes to this project will be documented in this file.

## [Unreleased]
* Added CHANGES.md to track changes between releases.
* Added the collection of platform-independent performance statistics.
* Added `bdd_used_nodes_count()` that counts the number of used nodes.
* Replaced static `CACHESTATS` by non-static `cachestats.enabled`, which is adaptable by user and configurable per BDD factory instance.
* Removed global JVM shutdown hook, that prints cache statistics to `stdout`, from `JFactory`.
* Cache statistics are now measured using `longs` rather than `ints` to reduce integer overflow issues.
* Added `opAccess` statistic to cache statistics.
* Added functionality to reset statistics.

## [1.0.1] - 2020-03-17
* Updated SCM URL for proper Maven Central metadata.

## [1.0.0] - 2020-03-13
* First release of com.github.javabdd.
