What is this?
=============

An *aborted* attempt to provide Sublime-Text-like multi cursor support for text editors in the Eclipse IDE (3.7 and later).

[![Build Status](https://travis-ci.org/caspark/eclipse-multicursor.svg?branch=master)](https://travis-ci.org/caspark/eclipse-multicursor)

What works?
-----------

I intended to implement full multi cursor behaviour in this plugin, but stopped using Eclipse before I got more than bits and pieces of that working. So at the moment this plugin is implemented under the covers by using Eclipse linked mode editing (similar to existing "rename in file" functionality), so **you can't move the cursors outside the initial selection areas, and all multiple-cursor-edited-text must be the same string.**

Usage
-----

* `Alt+J` (OSX: `Ctrl+G`): "Select Next Occurrence" of the selected text & start editing it. Repeat to select remaining occurrences.
* `Alt+F3` (OSX: `Ctrl+Cmd+G`): "Select All Occurrences" of the selected text & start editing all of them at once.

(If you haven't got anything selected, it'll expand your selection to the word under the cursor, or to the whole line if the cursor is in whitespace.)

What's next?
------------

Nothing from me. I'm not using Eclipse any more and don't have much interest in continuing this project as a result.

Ideally, I'd like to hand this project over to someone else, as I still believe it's very doable to create a real implementation (i.e. not using Eclipse linked mode). Open an issue if you're interested.

Getting and installing it
-------------------------

Get the latest release from the [releases page] and follow the install instructions below.

You have 2 options for installing (pick one):

*Option 1:* [Download](https://github.com/caspark/eclipse-multicursor/releases) the P2 update site (i.e. `com.asparck.eclipse.multicursor.p2updatesite-X.Y.Z.zip`), then in Eclipse choose Help >> Install New Software >> Add >> Archive >> choose the downloaded P2 update site >> tick the box to select Eclipse Multicursor >> go through the rest of the wizard >> ignore any warnings about unsigned content (just click OK) >> restart Eclipse when prompted. You can uninstall it later by going to Help >> About Eclipse >> Installation Details >> select "Eclipse Multicursor" >> Uninstall >> restart Eclipse when prompted.

*Option 2:* [Download](https://github.com/caspark/eclipse-multicursor/releases) `com.asparck.eclipse.multicursor.plugin_X.Y.Z.jar` and put it in `eclipse/plugins/dropins`, then restart Eclipse. You can uninstall it by shutting down Eclipse and deleting the jar from the dropins directory.

The former is the official recommended way of installing plugins & the latter is the quick and dirty way.

After installing, look at the *what works?* section above to use it.

Building from source
--------------------

Uses [Tycho](https://eclipse.org/tycho/) for building via Maven 3:

    mvn verify

This:

* looks at `c.a.e.m.target/c.a.e.m.target.target` to find the repo to get Eclipse OSGi dependency bundles from
* builds the plugin, feature, and update site in the target directories of `c.a.e.m.plugin`, `c.a.e.m.feature`, and `c.a.e.m.p2updatesite`
* runs the tests in `c.a.e.m.tests`

Installing after building
-------------------------

After building, point your Eclipse at the update site in `c.a.e.m.p2updatesite\target` and install the feature contained therein.

Developing
----------

Using Eclipse, with the Plugin Development Environment (PDE) plugins installed:

1. Import the projects from the repo as Existing Eclipse projects
2. Open `c.a.e.m.target/c.a.e.m.target.target` and click the "activate this target platform" link on the top right of the editor that opens
3. Right click on `c.a.e.m.plugin` and choose `Debug As` >> `Eclipse Application`
4. You can also right click on tests in `c.a.e.m.tests` to run them as either plugin tests or normal JUnit tests.

Note that the target platform you activate this way is more restricted than the target platform that Tycho will build with when you `mvn verify`. Tycho only uses the `.target` file to find which P2 repo it should search, and it ignores any restrictions in the enabled features of the target platform.
