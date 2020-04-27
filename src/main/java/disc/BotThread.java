package disc;

import disc.command.ComCommands;
import disc.command.MapCommands;
import disc.command.ServerCommands;
import org.javacord.api.DiscordApi;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.permission.Role;
import org.json.JSONObject;

import java.lang.Thread;

public class BotThread extends Thread{
    public DiscordApi api;
    private Thread mt;
    private JSONObject data;

    public BotThread(DiscordApi _api, Thread _mt, JSONObject _data) {
        api = _api; //new DiscordApiBuilder().setToken(data.get(0)).login().join();
        mt = _mt;
        data = _data;

        //communication commands
        api.addMessageCreateListener(new ComCommands());
        //server manangement commands
        api.addMessageCreateListener(new ServerCommands(data));
        api.addMessageCreateListener(new MapCommands(data));
    }

    public void run(){
        while (this.mt.isAlive()){
            try{
                Thread.sleep(1000);
            } catch (Exception e) {

            }
        }
        if (data.has("serverdown_role_id")){
            Role r = new UtilMethods().getRole(api, data.getString("serverdown_role_id"));
            TextChannel tc = new UtilMethods().getTextChannel(api, data.getString("serverdown_channel_id"));
            if (r == null || tc ==  null) {
                try {
                    Thread.sleep(1000);
                } catch (Exception _) {}
            } else {
                if (data.has("serverdown_name")){
                    String serverNaam = data.getString("serverdown_name");
                    new MessageBuilder()
                            .append(String.format("%s\nServer %s is down",r.getMentionTag(),((serverNaam != "") ? ("**"+serverNaam+"**") : "")))
                            .send(tc);
                } else {
                    new MessageBuilder()
                            .append(String.format("%s\nServer is down.", r.getMentionTag()))
                            .send(tc);
                }
            }
        }
        api.disconnect();
    }
}
