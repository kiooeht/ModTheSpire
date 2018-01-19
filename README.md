# ModTheSpire #
ModTheSpire is a tool to load external mods for Slay the Spire without modifying the base game files.

## Requirements ##
Java 9

## Installation ##
1.	Extract the ModTheSpire archive into your Slay the Spire installation directory.
2.	Place any mod .jars into the `./mods/` directory.
3.	Run `ModTheSpire.jar`

## For Modders ##
ModTheSpire automatically sets the Settings.isModded flag to true, so there is no need to do that yourself.
When making your mod .jar, you do not have to include the base game .jar, even if it is a dependency.
If you include a file called `ModTheSpireVersion` at the root of your .jar, ModTheSpire will use its contents as your mod name in the version string. Leaving the file empty will remove ModTheSpire from the version string completely.

## Changelog ##
#### v1 ####
* Initial release

## Contributors ##
* kiooeht - Original author
* t-larson - Multi-loading, mod initialization, some UI work