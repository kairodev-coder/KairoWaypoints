# KairoWaypoints

KairoWaypoints is a client-side Fabric waypoint and navigation mod for Minecraft 1.21.1. It works in singleplayer and multiplayer without a server-side installation.

Maintained by [kairodev-coder](https://github.com/kairodev-coder).

## Features

- Per-world and per-dimension waypoints
- Searchable waypoint manager and editor
- Directional HUD markers, compass, and tracked waypoint display
- Automatic deathpoints
- Waypoint groups and visibility controls
- Ordered directional routes
- Temporary and session-only waypoints
- Share codes and JSON exports
- Backups and recovery for stored waypoint data
- Built-in settings with optional Mod Menu and Cloth Config support

## Requirements

- Minecraft `1.21.1`
- Java `21`
- Fabric Loader `0.16.14`
- Fabric API `0.116.12+1.21.1`

Mod Menu `11.0.4` and Cloth Config `15.0.140` are optional.

## Installation

1. Install Fabric Loader for Minecraft 1.21.1.
2. Add Fabric API to the client `mods` folder.
3. Add `KairoWaypoints-1.0.0.jar` to the same folder.
4. Start Minecraft and press `U` or run `/waypoint gui`.

## Commands

`/waypoint`, `/waypoints`, and `/wp` open the main command tree. Common commands include:

| Command | Purpose |
| --- | --- |
| `/waypoint add <name> [x y z] [dimension]` | Create a waypoint |
| `/waypoint remove <name>` | Delete a waypoint |
| `/waypoint edit <name>` | Open the waypoint editor |
| `/waypoint list` | List saved waypoints |
| `/waypoint track <name>` | Track a waypoint |
| `/waypoint share <name>` | Copy a share code |
| `/waypoint import` | Import a share code from the clipboard |
| `/waypoint gui` | Open the waypoint manager |
| `/waypoint settings` | Open settings |

Use `/deathpoint` to manage deathpoints and `/route` to manage routes. Quoted names such as `"Main Base"` are supported.

## Controls

- `U`: open the waypoint manager
- `B`: create a waypoint

Other actions are available as configurable keybinds and are unbound by default.

## Data

KairoWaypoints stores client data under `config/kairowaypoints/`. Waypoints are separated by world or server and dimension. The mod creates backups before sensitive storage operations and moves damaged data to a recovery directory.

## Limitations

- Navigation is directional and does not calculate paths around blocks.
- Markers are HUD projections and do not use terrain occlusion.
- The mod must be installed on the client, not the server.

## Building

```powershell
.\gradlew.bat clean build
```

The release JAR is written to `build/libs/KairoWaypoints-1.0.0.jar`.

## License

KairoWaypoints is licensed under the [MIT License](LICENSE).
