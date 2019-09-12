## Changelog ##
#### dev ####

#### v3.13.1 ####
* Fix loading workshop beta mods when local beta mod should be loaded

#### v3.13.0 ####
* Save workshop item locations so they can be used later without Steam running
* Fix regression of ByRef named type parameter

#### v3.12.0 ####
* Make ByRef work on all Insert patch parameters, not just localvars
* Add capturing private fields as patch parameters
* Add ability to load different mod file on beta branch

#### v3.11.0 ####
* Add check to Prefix for ByRef not being array type
* Fix ByRef not working on array types
* Fix some weirdness of UI warnings
* Add toggle all mods button
* Fix SpireReturn memory leak
* Fix crash if update save contains an error
* Add mod profiles
* Fix StS version finding failing on v2 version numbers
* Fix displaying version incorrectly in-game for v2 version numbers

#### v3.10.1 ####
* Fix crash in incomplete last-update code

#### v3.10.0 ####
* Include all of javassist library for use
* Fix SpireReturn not working on constructors in Insert patches
* Allow SpireOverride to work on methods that already have SpireOverride
* Fix StaticSpireField not working

#### v3.9.2 ####
* Fix new version display to work on StS beta

#### v3.9.1 ####
* Fix duplicate SpireEnum crash

#### v3.9.0 ####
* Main menu no longer lists all mods, instead just the number of mods
* Separate MTS from in-game version number and show number of mods
* Make mods use StS version of Gson, not Gson packaged with MTS
* Patch fields onto enums for SpireEnum
  * Allows Gson to work with SpireEnum values instead of crashing

#### v3.8.3 ####
* Fix being unable to SpireOverride the same method on multiple subclasses

#### v3.8.2 ####
* Fix ClassPool changes not propagating

#### v3.8.1 ####
* Fix GameVersionFinder for full release

#### v3.8.0 ####
* Update libraries to use newer ASM
* Reduce MTS size by 30%
* Fix a typo (reina)

#### v3.7.5 ####
* Fix OBS capturing log window instead of the game sometimes

#### v3.7.4 ####
* Fix ModTheSpire hanging if there are too many Steam mods installed

#### v3.7.3 ####
* Restart MTS using jre1.8.0_51 if available
  * Resolves "OpenGL is not supported by the video driver" crash for people with older laptops

#### v3.7.2 ####
* Fix crash if mod is so old it only has ModTheSpire.config

#### v3.7.1 ####
* When opening mods folder, create it if it doesn't exist

#### v3.7.0 ####
* Steam Workshop support
* Fix SpireReturn not working on constructors

#### v3.6.3 ####
* Reroll to avoid duplicate SpireField names

#### v3.6.2 ####
* Fix silent crash in auto-updater if a mod's release has a bad version number

#### v3.6.1 ####
* Fix silent crash if a mod has a bad version number

#### v3.6.0 ####
* Fix ClassLoader to retrieve correct DesktopLauncher when invoked via ClassPool
* Time mod initializers

#### v3.5.0 ####
* Cache updater  to avoid hitting the rate limit

#### v3.4.0 ####
* Fix in-game mods menu not scrolling if you have a lot of mods
* Download and restart now uses same arguments as first launch

#### v3.3.0 ####
* Fix crash if a mod doesn't have an ID
* Copy annotations from SpireFields

#### v3.2.0 ####
* SpireOverride: Allow overriding private methods from superclasses
* Cleanup after the patching process

#### v3.1.0 ####
* Option dependencies field in ModInfo
  * Will be loaded before your mod, but aren't required
* Use SemVer library for version numbers

#### v3.0.0 ####
* More debug print info for SpireField
* Fix SpireField to work with generic types
* Fix SpireField to not use duplicate objects
* Fix NPE in isModLoaded
* Reworked UI
* Store configs in ~/Library/Preferences on Mac
* Make annotationDBMap public for mods to use
* Add some functionality to SpireConfig
* Add extra options for LineFinder
* Fix in-game mod list tooltip position on other resolutions
* Allow multiple Prefix, Postfix, and Insert patches to exist in a single patch class
  * Use the SpirePrefixPatch, SpirePostfixPatch, and SpireInsertPatch annotations to mark methods
  * If using a locator, Insert must specify locator with the `locator` parameter of SpireInsertPatch
* Allow Class types to be used in SpirePatch
  * No longer have to type the fully qualified class name
* Allow Class types in locator Matchers
  * No longer have to type the fully qualified class name
* Always print patch debug info on patching error
* More understandable errors for some patching errors
* Force defining paramtypes on overloaded methods
* Stricter error when method to patch isn't found

#### v2.9.1 ####
* Patch to always enable Custom mode

#### v2.9.0 ####
* Allow Prefix patches to skip the original method
* Allow Insert patches to skip the remainder of the original method
* Method for mods to check if another mod is loaded
* Fix finding Steam install on Mac and Linux

#### v2.8.0 ####
* SpireField: For adding new fields to existing classes

#### v2.7.0 ####
* Fix for week 29
* Option for modders to dump patched JAR for inspection (test447)
* Format logs nicer (test447)
* Mod update checker (test447/kiooeht)
* Make constants for patching constructors and static initializers
* Make Play button default for keyboard use
* Warning banner if using beta branch of StS

#### v2.6.0 ####
* Change ModInfo to use JSON
* Update checker for ModTheSpire
* Warn if ModTheSpire is in the mod list and don't load it as a mod
* Add useful debug info to start of log
* Mod dependencies: Load dependencies first
* Search for desktop-1.0.jar in Steam installation directory
* Mod screen in game
* Locator for Insert patches (test447)
* Fix: Disable checkboxes for mods that need newer MTS version

#### v2.5.0 ####
* **Merge ModTheSpire and ModTheSpireLib. They are now one project**
* Maintain launcher window size and position between uses
* When not using debug mode, close log window when game closes
* Retain debug mode between uses
* Mods can specify an exact StS version they support
* Warn in launcher if mod specifies a specific StS version that doesn't match the current
* SpireConfig: Save/load mod config options from user directory
* Fix: Launcher UI for long lists of mod authors or long descriptions
* Fix: UTF-8 support in ModInfo (pk27602017)

#### v2.4.0 ####
* Allow multiple @SpirePatches on single class
* Warn if not running with Java 8
* Fix: NullPointerException when no/empty mods folder
* Fix?: Unable to find `desktop-1.0.jar` on Mac
* Fix: Sometimes crashing when patching a superclass and subclass

#### v2.3.0 ####
* Allow patching static initializers (`"<staticinit>"`)
* Replace patches, completely replace a method
* Raw patches, gives complete access to Javassist API
* Patch loading order now: Insert, Instrument, Replace, Prefix, Postfix, Raw
* Include mod author and description in launcher (test447)
* Debug mode: Displays some additional info for modders
  * Enable with `--debug` flag or checkbox in GUI
* ByRef can auto-determine parameter type for Prefix patches
* Fix: ModTheSpire can now be run through SlayTheSpire.exe

#### v2.2.1 ####
* Fix: ByRef can now specify the real type name when using `Object` as parameter type

#### v2.2.0 ####
* Inject patches in mod load order (kiooeht)
* Include dependency licenses (kiooeht)
* Mod list when hovering over version string in-game (kiooeht)
* Debug log window in launcher (kiooeht)
* Relative line numbers for insert patches (kiooeht)
* Allow @ByRef for prefixes (kiooeht)
* Instrument (ExprEditor) patches (kiooeht)
* SpireEnum to add new enum values (kiooeht)
* Mods can specify minimum ModTheSpire version needed (kiooeht)
* Mods can tag a class @SpireInitializer, and the class's `initialize()` method will be called (kiooeht)
* Fix: Stop code patches from stopping mod patches (kiooeht)
* Fix: Can now prefix constructors (kiooeht)
* Fix NullPointerException if mod doesn't contain `ModTheSpire.config` (kiooeht)

#### v2.1.0 ####
* Display mods on main menu (kiooeht)
* Insert patches (kiooeht)
* Warn if unable to find `desktop-1.0.jar` (kiooeht)
* Popup error messages (kiooeht)
* Allow running ModTheSpire as `desktop-1.0.jar` (kiooeht)

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
