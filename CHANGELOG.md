## Changelog ##
#### dev ####
* Change ModInfo to use JSON
* Update checker for ModTheSpire
* Warn if ModTheSpire is in the mod list and don't load it as a mod
* Add useful debug info to start of log
* Mod dependencies: Load dependencies first
* Search for desktop-1.0.jar in Steam installation directory
* Mod screen in game
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
