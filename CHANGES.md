# Changelog for com.github.javabdd

All notable changes to this project will be documented in this file.

## [Unreleased]
* Added CHANGES.md to track changes between releases.
* Added the collection of performance statistics.
* Added bdd_used_nodes_count() that counts the number of used nodes.
* Replaced static CACHESTATS by non-static cachestats.enabled, which is adaptable by user.
* Removed shutdown hook from JFactory.
* Changed Cache statistics from int to long.
* Added opAccess statistic to cache statistics.

## [1.0.1] - 2020-03-17
* Updated SCM URL for proper Maven Central metadata.

## [1.0.0] - 2020-03-13
* First release of com.github.javabdd.
