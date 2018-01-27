# ModTheSpire #
ModTheSpire is a tool to load external mods for Slay the Spire without modifying the base game files.

## Requirements ##
#### General Use ####
* Java 8+

#### Development ####
* Java 8+
* Maven
* [ModTheSpireLib](https://github.com/kiooeht/ModTheSpireLib)

## Building ##
1. Run `mvn package`

## Installation ##
1. Copy `target/ModTheSpire.jar` to your Slay the Spire install directory.
2. Create a `mods` directory. Place mod JAR files into the `mods` directory.

## Usage ##
1. Run `ModTheSpire.jar` or run `_run.bat` to get logger output.
2. Select the mod(s) you want to use.
3. Press 'Play'

## For Modders ##
* ModTheSpire automatically sets the Settings.isModded flag to true, so there is no need to do that yourself.
* Initialization: in `modname.ModName` implement `public static void initialize()`.
* If you include a file called `ModTheSpire.config` at the root of your mod's .jar, ModTheSpire will use it to determine the mod's name and author.
Example:
```
name=Example Mod Name
author=kiooeht
```
* For mod code injected, see [ModTheSpireLib](https://github.com/kiooeht/ModTheSpireLib).

## Changelog ##
#### v2.0.0 ####
* Credits injection (kiooeht)
* Mod code injection (kiooeht)
  * Prefix
  * Postfix
* Merge t-larson's changes
* Add checkboxes to mod select list (kiooeht)

#### v1.1.2 ####
* Fix exception that occured when mods folder is either not found or empty (t-larson)

#### v1.1.1 ####
* Fix support for mods that do not contain `modname.ModName` (FlipskiZ)
* Switch build to Maven (reckter)

#### v1.1.0 ####
* Change buttons to multi-select list (t-larson)
* Add support for loading multiple mods at the same time (t-larson)
* Add support for mod initialization (t-larson)
* General code cleanup (t-larson)

#### v1.0.0 ####
* Initial release (kiooeht)

## Contributors ##
* kiooeht - Original author
* t-larson - Multi-loading, mod initialization, some UI work
* reckter - Maven setup
* FlipskiZ - Mod initialization
