# ModTheSpire #
ModTheSpire is a tool to load external mods for Slay the Spire without modifying the base game files.

## Requirements ##
#### General Use ####
Java 8+

#### Development ####
Java 8+
Maven

## Building ##
Run `mvn package`

## Installation ##
1. Copy `target/modthespire-dev.jar` to your Slay the Spire install directory.
2. Create a `mods` directory. Place mod JAR files into the `mods` directory.

## Usage ##
1. Run `modthespire-dev.jar` or run `_run.bat` to get logger output.
2. Select the mod(s) you want to use. Ctrl+Click and Shift+Click can be used to select multiple.

## For Modders ##
* ModTheSpire automatically sets the Settings.isModded flag to true, so there is no need to do that yourself.
* Initialization: in `modname.ModName` implement `public static void initialize()`.

## Changelog ##
#### v1.0.0 ####
* Initial release (kiooeht)

#### v1.1.0 ####
* Change buttons to multi-select list (t-larson)
* Add support for loading multiple mods at the same time (t-larson)
* Add support for mod initialization (t-larson)
* General code cleanup (t-larson)

#### v1.1.1 ####
* Fix support for mods that do not contain `modname.ModName` (FlipskiZ)
* Switch build to Maven (reckter)

## Contributors ##
* kiooeht - Original author
* t-larson - Multi-loading, mod initialization, some UI work
* reckter - Maven setup
* FlipskiZ - Mod initialization
