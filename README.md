# com.github.javabdd

## Intro

JavaBDD is a Java library for manipulating BDDs (Binary Decision Diagrams).
Binary decision diagrams are widely used in model checking, formal
verification, optimizing circuit diagrams, etc.

The JavaBDD API is based on that of the popular BuDDy package, a BDD package
written in C by Jørn Lind-Nielsen. However, JavaBDD's API is designed to be
object-oriented. The ugly C function interface and reference counting schemes
have been hidden underneath a uniform, object-oriented interface.

JavaBDD includes multiple 100% Java implementations. JavaBDD provides a
uniform interface to all of these implementations, so you can easily switch
between them without having to make changes to your application.

JavaBDD is designed for high performance applications, so it also exposes
many of the lower level options of the BDD library, like cache sizes and
advanced variable reordering.

## History

This version of JavaBDD is a fork of the Sourceforge version, see:

 * http://javabdd.sourceforge.net/
 * https://sourceforge.net/projects/javabdd/

It is based on trunk revision r483 from 2011-11-24.

See [CHANGES.md](CHANGES.md) for all changes that have been made in this fork.

See [CONTRIBUTORS.md][CONTRIBUTORS.md] for all contributors.

## License

This project inherits the license from the Sourceforge project it is forked
from, the GNU Library General Public License v2 (LGPLv2) or later. See the
[LICENSE.txt](LICENSE.txt) file.

SPDX-License-Identifier: LGPL-2.0-or-later
