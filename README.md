# ModTheSpire #
ModTheSpire is a tool to load external mods for Slay the Spire without modifying the base game files.

## Usage ##
### Installation ###
1. Subscribe to [ModTheSpire](https://steamcommunity.com/sharedfiles/filedetails/?id=1605060445&searchtext=ModTheSpire) on the Steam workshop

### Running Mods ###
1. Launch SlayTheSpire from Steam.
2. Select: Play with Mods
2. Select the mod(s) you want to use.
3. Press 'Play'.

---

## For Modders ##
### Requirements ###
* JDK 8
* Maven

### General ###
* ModTheSpire automatically sets the Settings.isModded flag to true, so there is no need to do that yourself.
* [Wiki](https://github.com/kiooeht/ModTheSpire/wiki/SpirePatch)

### Building ###
1. Run `mvnw package`

---

## Changelog ##
See [CHANGELOG](CHANGELOG.md)

## Contributors ##
* kiooeht - Original author
* t-larson - Multi-loading, mod initialization, some UI work
* test447 - Some launcher UI work, Locator
* reckter - Maven setup
* FlipskiZ - Mod initialization
* pk27602017 - UTF-8 support in ModInfo
