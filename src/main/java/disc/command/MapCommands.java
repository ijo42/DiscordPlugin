package disc.command;

import arc.Core;
import arc.Events;
import arc.files.Fi;
import arc.struct.Array;
import disc.CommandsConstants;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.io.SaveIO;
import mindustry.maps.Map;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.zip.InflaterInputStream;

public class MapCommands implements MessageCreateListener {
    final long minMapChangeTime = 30L; //30 seconds
    final String commandDisabled = "This command is disabled.";
    final String noPermission = "You don't have permissions to use this command!";

    private JSONObject data;
    private long lastMapChange = 0L;


    public MapCommands(JSONObject _data) {
        this.data = _data;
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessageContent().equalsIgnoreCase(CommandsConstants.MapsCommand)) {
            Vars.maps.reload();
            StringBuilder mapLijst = new StringBuilder();
            mapLijst.append("List of available maps:\n");
            for (Map m : Vars.maps.customMaps()) {
                mapLijst.append("* ").append(m.name()).append("/ ").append(m.width).append(" x ").append(m.height).append("\n");
            }
            mapLijst.append("Total number of maps: ").append(Vars.maps.customMaps().size);
            new MessageBuilder().appendCode("", mapLijst.toString()).send(event.getChannel());

        } else if (event.getMessageContent().startsWith(CommandsConstants.ChangeMapCommand)) {
            if (!data.has("changeMap_role_id")) {
                if (event.isPrivateMessage()) return;
                event.getChannel().sendMessage(commandDisabled);
                return;
            }
            Role r = getRole(event.getApi(), data.getString("changeMap_role_id"));
            if (!hasPermission(r, event)) return;

            if (System.currentTimeMillis() / 1000L - this.lastMapChange < this.minMapChangeTime) {
                if (event.isPrivateMessage()) return;
                event.getChannel().sendMessage(String.format("This commands has a %d s cooldown.", this.minMapChangeTime));
                return;
            }

            String[] splitted = event.getMessageContent().split(" ", 2);
            if (splitted.length == 1) {
                int index = 1;
                StringBuilder sb = new StringBuilder();
                for (Map m : Vars.maps.customMaps()) {
                    sb.append(index++).append(" : ").append(m.name()).append("\n");
                }
                sb.append("\nUse ").append(CommandsConstants.ChangeMapCommand).append(" <number/name>");
                new MessageBuilder().appendCode("", sb.toString()).send(event.getChannel());
            } else {
                Map found = null;
                try {
                    splitted[1] = splitted[1].trim();
                    found = Vars.maps.customMaps().get(Integer.parseInt(splitted[1]) - 1);
                } catch (Exception e) {
                    //check if map exits
                    for (Map m : Vars.maps.customMaps()) {
                        if (m.name().equals(splitted[1])) {
                            found = m;
                            break;
                        }
                    }
                }
                if (found == null) {
                    event.getChannel().sendMessage("Map not found...");
                    return;
                }

                Fi temp = Core.settings.getDataDirectory().child("maps").child("temp");
                temp.mkdirs();
                for (Map m1 : Vars.maps.customMaps()) {
                    if (m1.equals(Vars.world.getMap())) continue;
                    if (m1.equals(found)) continue;
                    m1.file.moveTo(temp);
                }

                Vars.maps.reload();
                Events.fire(new EventType.GameOverEvent(Team.crux));
                Vars.maps.reload();
                Fi mapsDir = Core.settings.getDataDirectory().child("maps");
                for (Fi fh : temp.list()) {
                    fh.moveTo(mapsDir);
                }
                temp.deleteDirectory();
                Vars.maps.reload();

                event.getChannel().sendMessage("Next map selected: " + found.name() + "\nThe current map will change in 10 seconds.");

                this.lastMapChange = System.currentTimeMillis() / 1000L;
            }

        } else if (event.getMessageContent().equalsIgnoreCase(CommandsConstants.UploadMapCommand)) {
            if (!data.has("mapConfig_role_id")) {
                if (event.isPrivateMessage()) return;
                event.getChannel().sendMessage(commandDisabled);
                return;
            }
            Role r = getRole(event.getApi(), data.getString("mapConfig_role_id"));
            if (!hasPermission(r, event)) return;

            Array<MessageAttachment> ml = new Array<>();
            for (MessageAttachment ma : event.getMessageAttachments()) {
                if (ma.getFileName().split("\\.", 2)[1].trim().equals("msav")) {
                    ml.add(ma);
                }
            }
            if (ml.size != 1) {
                if (event.isPrivateMessage()) return;
                event.getChannel().sendMessage("You need to add 1 valid .msav file!");
                return;
            } else if (Core.settings.getDataDirectory().child("maps").child(ml.get(0).getFileName()).exists()) {
                if (event.isPrivateMessage()) return;
                event.getChannel().sendMessage("There is already a map with this name on the server!");
                return;
            }
            //more custom filename checks possible

            CompletableFuture<byte[]> cf = ml.get(0).downloadAsByteArray();
            Fi fh = Core.settings.getDataDirectory().child("maps").child(ml.get(0).getFileName());

            try {
                byte[] data = cf.get();
                if (!SaveIO.isSaveValid(new DataInputStream(new InflaterInputStream(new ByteArrayInputStream(data))))) {
                    if (event.isPrivateMessage()) return;
                    event.getChannel().sendMessage("invalid .msav file!");
                    return;
                }
                fh.writeBytes(cf.get(), false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Vars.maps.reload();
            event.getChannel().sendMessage(ml.get(0).getFileName() + " added succesfully!");

        } else if (event.getMessageContent().startsWith(CommandsConstants.RemoveMapCommand)) {
            if (!data.has("mapConfig_role_id")) {
                if (event.isPrivateMessage()) return;
                event.getChannel().sendMessage(commandDisabled);
                return;
            }
            Role r = getRole(event.getApi(), data.getString("mapConfig_role_id"));
            if (!hasPermission(r, event)) return;

            String[] splitted = event.getMessageContent().split(" ", 2);
            if (splitted.length == 1) {
                int index = 1;
                StringBuilder sb = new StringBuilder();
                for (Map m : Vars.maps.customMaps()) {
                    sb.append(index++).append(" : ").append(m.name()).append("\n");
                }
                sb.append("\nUse ").append(CommandsConstants.RemoveMapCommand).append(" <number/name>");
                new MessageBuilder().appendCode("", sb.toString()).send(event.getChannel());
            } else {
                //try number
                Map found = null;
                try {
                    splitted[1] = splitted[1].trim();
                    found = Vars.maps.customMaps().get(Integer.parseInt(splitted[1]) - 1);
                } catch (Exception e) {
                    //check if map exits
                    for (Map m : Vars.maps.customMaps()) {
                        if (m.name().equals(splitted[1])) {
                            found = m;
                            break;
                        }
                    }
                }
                if (found == null) {
                    event.getChannel().sendMessage("Map not found...");
                    return;
                }
                Vars.maps.removeMap(found);
                Vars.maps.reload();
                event.getChannel().sendMessage("Deleted succesfully: " + found.name());

            }
        }
    }

    public Role getRole(DiscordApi api, String id){
        Optional<Role> r1 = api.getRoleById(id);
        if (!r1.isPresent()) {
            System.out.println("[ERR!] discordplugin: role not found!");
            return null;
        }
        return r1.get();
    }

    public Boolean hasPermission(Role r, MessageCreateEvent event){
        try {
            if (r == null) {
                if (event.isPrivateMessage()) return false;
                event.getChannel().sendMessage(commandDisabled);
                return false;
            } else if (hasRole(r, event)) {
                if (event.isPrivateMessage()) return false;
                event.getChannel().sendMessage(noPermission);
                return false;
            } else {
                return true;
            }
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean hasRole(Role r, MessageCreateEvent event) {
        if ((event.getMessageAuthor().asUser().isPresent() && event.getServer().isPresent())) {
            User user = event.getMessageAuthor().asUser().get();
            return !user.getRoles(event.getServer().get()).contains(r);
        } else
            return false;
    }
}
