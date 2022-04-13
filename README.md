# FishMode Plugin
This plugin was made to address a plugin request where the players should breathe in water 
and suffocate for lack of oxygen in air, like a fish.

## Commands

| Command                               | Description                               | Permission                     |
|---------------------------------------|-------------------------------------------|--------------------------------|
| `/setfish [true/false] [Player Name]` | Set a direct fish flag to a player        | `fishmode.cmd.setfish`         |
| `/unsetfish [Player Name]`            | Remove the direct fish flag from a player | `fishmode.cmd.unsetfish`       |
| `/reload-fishmode`                    | Reload the configs from the filesystem    | `fishmode.cmd.fishmode-reload` |
| `/isfish [Player Name]`               | Shows the fish status of a player         | `fishmode.cmd.isfish`          |

Note: The player names can have space, you don't have to put them between quotes, in these commands.

Note: All command permissions are automatically granted to `OP` players.

## Permissions
Aside from the command permissions there are two extra permissions that you can give
to users to override the default settings, create a group of fishes with your permission manager,
or make your staff never fish unless directly set as fish.

These permissions are not granted by default to anybody.

| Permission             | Description                                                                                                         |
|------------------------|---------------------------------------------------------------------------------------------------------------------|
| `fishmode.fish.always` | Makes the user always fish unless directly set to not be                                                            |
| `fishmode.fish.never`  | Makes the user never fish, most settings except the direct fish flag, this has priority over `fishmode.fish.always` |

## Configuration
After running the first time, edit the file `plugins/FishMode/config.yml` as you wish.

Default configuration:
```yaml
# 1 = refresh all players every tick, 2 = every 2 ticks, 3 = every 3 ticks and so on
tick-rate: 2
fish-player:
  # Should the players who are marked as fish be able to breath in non-fish worlds on only in fish worlds?
  only-in-fish-worlds: true
  # This makes all players who don't have a direct flag associated to them a fish when it is set to true
  players-are-fish-by-default: false
fish-worlds:
  # All players who are in a fish-world should be fish or only those who were marked as fish by command or plugin?
  all-players-are-fish: false
  # If you want all your worlds to become fish-world, set this to true
  all: false
  # If you want to disable this plugin in all nether dimensions, set this to true
  not-nether: true
  # This is a list of strings, you can add the name of the worlds here
  by-name:
    - world
  # This is a list of regular expressions, this is an advanced way just in case you want to be fancy
  regex: []
```

## Cloning and importing
1. Just do a normal `git clone https://github.com/PowerNukkit/ExamplePlugin.git` (or the URL of your own git repository)
2. Import the `pom.xml` file with your IDE, it should do the rest by itself

## Debugging
1. Create a zip file containing only your `plugin.yml` file
2. Rename the zip file to change the extension to jar
3. Create an empty folder anywhere, that will be your server folder.  
   <small>_Note: You don't need to place the PowerNukkit jar in the folder, your IDE will load it from the maven classpath._</small>
4. Create a folder named `plugins` inside your server folder  
   <small>_Note: It is needed to bootstrap your plugin, your IDE will load your plugin classes from the classpath automatically,
   so it needs to have only the `plugin.yml` file._</small>
5. Move the jar file that contains only the `plugin.yml` to the `plugins` folder
6. Create a new Application run configuration setting the working directory to the server folder and the main class to:  `cn.nukkit.Nukkit`  
![](https://i.imgur.com/NUrrZab.png)
7. Now you can run in debug mode. If you change the `plugin.yml` you will need to update the jar file that you've made.
