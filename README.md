### Setup
Make a bot and request a token. Make sure to give the bot some permissions (Admin for now, this will change 
because this plugin is in early dev). 

Before you start your server you will need to create a `settings.json` file in the `config/mods` directory.
You need to follow a specific template. This is an [example](https://github.com/J-VdS/DiscordPlugin/blob/master/settings.json) of how this file should look.

I recommend to enable `Developer Mode` on discord. This is the easiest way to get the `id` values.

### Disable commands
You are able to disable some in-game commands by leaving some fields in the `settings.json` file blank (`""`).
 * *Disable /d* : `"dchannel_id": ""` 
 * *Disable /gr* : `"role_id": ""`  and `"channel_id":""`

### Basic Usage
You can use some basic commands. I will add more in the future.

#### Discord
* `..chat <text...>` Send a message to the in-game chat
* `..players` a list of the online players
* `..info` gives some info (amount of enemies, number of waves, name of the map)
* `..infores` amount of resources collected
* `..gameover` ends a game (adminonly)
* `..maps` maps on the server<br>
(TODO)
* `..help` shows a help msg
* `..changemap`

#### In-game
* `/d <text...>` Send a message to discord.
* `/gr [player] [reason...]` Alert admins on discord if someone is griefing (5 minute cooldown)

### Building the Jar

`gradlew jar` / `./gradlew jar`

Output jar should be in `build/libs`.


### Installing

Simply place the output jar from the step above in your server's `config/mods` directory, add a `settings.json` file and restart the server.
List your currently installed plugins by running the `mods` command.
