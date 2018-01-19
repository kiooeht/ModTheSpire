# ModTheSpire #
ModTheSpire is a tool to load external mods for Slay the Spire without modifying the base game files.

## Requirements ##
Java 8+

## Installation ##
1.	Extract the ModTheSpire archive into your Slay the Spire installation directory.
2.	Create a `mods` directory. Place mod JAR files into the `./mods/` directory.
3.	Run `ModTheSpire.jar`

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