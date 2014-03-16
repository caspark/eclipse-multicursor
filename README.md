What is this?
=============

A work-in-progress attempt to provide Sublime-Text-like multi cursor support for text editors in the Eclipse IDE.


What works?
-----------

Not much - don't get too excited ;)

* `Ctrl+Shift+i`: Rename all instances of the word under the cursor (or the selected text if text is selected, or identical lines if the cursor is in a block of whitespace). Effectively incremental & global find and replace.

Implemented using Eclipse linked mode editing (similar to existing "rename in file" functionality), so you can't go outside the initial edit area yet.


Still to come
-------------

* "select next" and associated editing using Eclipse linked mode, to only edit some instances of text

* editing of non-identical text / editing without using linked mode

* split selection to lines

Building
--------

Uses [Tycho](https://eclipse.org/tycho/) for building via Maven 3:

    cd com.asparck.eclipse.multicursor.parent
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
