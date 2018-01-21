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
If you include a file called `ModTheSpire.config` at the root of your mod's .jar, ModTheSpire will use it to determine the mod's name and author.
Example:
```
name=Example Mod Name
author=kiooeht
```

For collaborators:
compile with maven
`mvn clean package` will put a modthespire-dev.jar into `target/`
if you do not have maven install it or use the maven wrapper (./mvnw or mvnw.cmd)

Changelog
=========

v2:
+ Credits injection

v1:
+ Initial release
