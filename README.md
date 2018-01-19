# ModTheSpire #
ModTheSpire is a tool to load external mods for Slay the Spire without modifying the base game files.

## Requirements ##
Java 8+

## Building ##
1. Run `_compile.bat`.
2. Run `_package.bat`.

## Installation ##
1. Copy `ModTheSpire.jar` to your Slay the Spire install directory.
2. Create a `mods` directory. Place mod JAR files into the `mods` directory.

## Usage ##
1. Run `ModTheSpire.jar` or run `_run.bat` to get logger output.
2. Select the mod(s) you want to use. Ctrl+Click and Shift+Click can be used to select multiple.

## For Modders ##
* ModTheSpire automatically sets the Settings.isModded flag to true, so there is no need to do that yourself.
* Initialization: in `modname.ModName` implement `public static void initialize()`.

## Changelog ##
#### v1 ####
* Initial release

#### v1.1 ####
* Change buttons to multi-select list
* Add support for loading multiple mods at the same time
* Add support for mod initialization
* General code cleanup

## Contributors ##
* kiooeht - Original author
* t-larson - Multi-loading, mod initialization, some UI work