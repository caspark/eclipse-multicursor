What is this?
=============

A work-in-progress attempt to provide Sublime-Text-like multi cursor support for text editors in the Eclipse IDE.

[![Build Status](https://travis-ci.org/caspark/eclipse-multicursor.svg?branch=master)](https://travis-ci.org/caspark/eclipse-multicursor)

What works?
-----------

* `Ctrl+Shift+D`: "Select Next Occurrence" of the selected text & start editing it. Repeat to select remaining occurrences.
* `Ctrl+Shift+I`: "Select All Occurrences" of the selected text & start editing all of them at once.

(If you haven't got anything selected, it'll expand your selection to the word under the cursor, or to the whole line if the cursor is in whitespace.)

This is currently implemented using Eclipse linked mode editing (similar to existing "rename in file" functionality), so you can't go outside the initial edit area yet.

*Still to come*: Check the [open issues](https://github.com/caspark/eclipse-multicursor/issues?state=open).

Building
--------

Uses [Tycho](https://eclipse.org/tycho/) for building via Maven 3:

    mvn verify

This:

* looks at `c.a.e.m.target/c.a.e.m.target.target` to find the repo to get Eclipse OSGi dependency bundles from
* builds the plugin, feature, and update site in the target directories of `c.a.e.m.plugin`, `c.a.e.m.feature`, and `c.a.e.m.p2updatesite`
* runs the tests in `c.a.e.m.tests`

Installing
----------

If I were you I wouldn't bother yet (star the repo and come back later!).

If you insist: after building, point your Eclipse at the update site in `c.a.e.m.p2updatesite\target`

Developing
----------

Using Eclipse, with the Plugin Development Environment (PDE) plugins installed:

1. Import the projects from the repo as Existing Eclipse projects
2. Open `c.a.e.m.target/c.a.e.m.target.target` and click the "active this target platform" link on the top right of the editor that opens
3. Right click on `c.a.e.m.plugin` and choose `Debug As` >> `Eclipse Application`
4. You can also right click on tests in `c.a.e.m.tests` to run them as either plugin tests or normal JUnit tests.

Note that the target platform you activate this way is more restricted than the target platform that Tycho will build with when you `mvn verify`. Tycho only uses the `.target` file to find which P2 repo it should search, and it ignores any restrictions in the enabled features of the target platform.
