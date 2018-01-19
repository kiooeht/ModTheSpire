ModTheSpire
by kiooeht
===========
ModTheSpire is a method to load external mods for Slay The Spire without modifying the base game files.

Installing:
1.	Extract the ModTheSpire archive into your Slay the Spire installation directory.
2.	Place any mod .jars into the `mods/` directory.
3.	Run `ModTheSpire.jar`

For Modders:
ModTheSpire automatically sets the Settings.isModded flag to true, so there is no need to do that yourself.
When making your mod .jar, you do not have to include the base game .jar, even if it is a dependency.
If you include a file called `ModTheSpireVersion` at the root of your .jar, ModTheSpire will use its contents as your mod name in the version string. Leaving the file empty will remove ModTheSpire from the version string completely.

For collaborators:
compile with maven
`mvn clean package` will put a modthespire-dev.jar into `target/`
if you do not have maven install it or use the maven wrapper (./mvnw or mvnw.cmd)

Changelog
=========

v1:
+ Initial release
