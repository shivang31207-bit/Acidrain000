# Acid Ocean (Fabric 1.21.11)

Build:  ./gradlew build  (JDK 21; use the wrapper from the Fabric template if missing)
Jar:    build/libs/acidocean-1.0.0.jar
Install: put the jar + Fabric API in your mods folder (server and/or singleplayer).

Commands (OP level 2 / cheats on):
  /acidocean on
  /acidocean off
  /acidocean damage <half-hearts>
  /acidocean interval <ticks>
  /acidocean status

No Java/Gradle on your PC? Upload this project to a new GitHub repo.
GitHub Actions (.github/workflows/build.yml) builds the .jar for you:
Actions tab -> latest run -> Artifacts -> acidocean-jar.

v1.1: ocean water turns green on clients while acid is ON.
Install the mod on the server AND on every player's client for the green colour.
(Damage works even for players without the mod.)
