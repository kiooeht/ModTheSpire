# ModTheSpire #
ModTheSpire is a tool to load external mods for Slay the Spire without modifying the base game files.

## Requirements ##
#### General Use ####
* Java 8

#### Development ####
* Java 8
* Maven

## Building ##
1. Run `mvn package`

## Installation ##
1. Copy `target/ModTheSpire.jar` to your Slay the Spire install directory.
2. Create a `mods` directory. Place mod JAR files into the `mods` directory.

## Usage ##
1. Run `ModTheSpire.jar`.
2. Select the mod(s) you want to use.
3. Press 'Play'.

## For Modders ##
* ModTheSpire automatically sets the Settings.isModded flag to true, so there is no need to do that yourself.
* [Wiki](https://github.com/kiooeht/ModTheSpire/wiki/SpirePatch)

## Changelog ##
See [CHANGELOG](CHANGELOG.md)

## Contributors ##
* kiooeht - Original author
* t-larson - Multi-loading, mod initialization, some UI work
* test447 - Some launcher UI work
* reckter - Maven setup
* FlipskiZ - Mod initialization
* pk27602017 - UTF-8 support in ModInfo
