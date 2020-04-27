package disc;

import arc.Core;
import arc.Events;
import arc.util.CommandHandler;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.entities.type.Player;
import mindustry.game.EventType;
import mindustry.gen.Call;
import mindustry.plugin.Plugin;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.awt.*;
import java.util.HashMap;
import java.util.Optional;

public class DiscordPlugin extends Plugin {
    private final Long CDT = 300L;
    private JSONObject data;
    private DiscordApi api = null;
    private HashMap<Long, String> cooldowns = new HashMap<>(); //uuid

    public DiscordPlugin() {
        JSONObject cfg;
        try {
            String pureJson = Core.settings.getDataDirectory().child("mods/settings.json").readString();
            cfg = new JSONObject(new JSONTokener(pureJson));
            if (!cfg.has("in-game")) {
                System.out.println("[ERR!] DiscordPlugin: settings.json имеет неизвестный формат!\n");
                return;
            } else {
                data = cfg.getJSONObject("in-game");
            }
        } catch (Exception e) {
            String fileNotFoundErrorMessage = "File not found: config\\mods\\settings.json";
            if (e.getMessage().contains(fileNotFoundErrorMessage)) {
                System.out.println("[ERR!] DiscordPlugin: settings.json файл не найден.\nБот не может стартовать.");
                //this.makeSettingsFile("settings.json");
                return;
            } else {
                System.out.println("[ERR!] DiscordPlugin: Init Error");
                e.printStackTrace();
                return;
            }
        }
        try {
            api = new DiscordApiBuilder().setToken(cfg.getString("token")).login().join();
        }catch (Exception e){
            if (e.getMessage().contains("READY packet")){
                System.out.println("\n[ERR!] DiscordPlugin: Токен не валидный.\n");
            } else {
                e.printStackTrace();
            }
        }
        BotThread bt = new BotThread(api, Thread.currentThread(), cfg.getJSONObject("discord"));
        bt.setDaemon(false);
        bt.start();

        //live chat
        if (data.has("live_chat_channel_id")) {
            TextChannel tc = this.getTextChannel(data.getString("live_chat_channel_id"));
            if (tc != null) {
                Events.on(EventType.PlayerChatEvent.class, event -> tc.sendMessage("**" + event.player.name.replace('*', '+') + "**: " + event.message));
            }
        }
    }

    //register commands that run on the server
    @Override
    public void registerServerCommands(CommandHandler handler){

    }

    //register commands that player can invoke in-game
    @Override
    public void registerClientCommands(CommandHandler handler){
        if (api != null) {
            handler.<Player>register("d", "<текст>", "Отправить сообщение в Discord", (args, player) -> {

                if (!data.has("dchannel_id")) {
                    player.sendMessage("[scarlet]Эта команда отключена.");
                } else {
                    TextChannel tc = this.getTextChannel(data.getString("dchannel_id"));
                    if (tc == null) {
                        player.sendMessage("[scarlet]Эта команда отключена.");
                        return;

                    }
                    tc.sendMessage(player.name + " *@mindustry* : " + args[ 0 ]);
                    Call.sendMessage(player.name + "[sky] в @discord[]: " + args[ 0 ]);
                }

            });

            handler.<Player>register("gr", "[id] [причина]", "Создать репорт на грифера по id (используйте '/gr' что бы получить список id)", (args, player) -> {
                if (!(data.has("channel_id") && data.has("role_id"))) {
                    player.sendMessage("[scarlet]Эта команда отключена");
                    return;
                }

                for (Long key : cooldowns.keySet()) {
                    if (key + CDT < System.currentTimeMillis() / 1000L) {
                        cooldowns.remove(key);
                    } else if (player.uuid.equals(cooldowns.get(key))) {
                        player.sendMessage("[scarlet]Эта команда имеет 5 минутный кулдаун!");
                        return;
                    }
                }

                if (args.length == 0) {
                    StringBuilder builder = new StringBuilder();
                    builder.append("[orange]Доступные для репорта: \n");
                    for (Player p : Vars.playerGroup.all()) {
                        if (p.isAdmin || p.con == null) continue;

                        builder.append("[lightgray] ").append(p.name).append("[accent] (#").append(p.id).append(")\n");
                    }
                    player.sendMessage(builder.toString());
                } else {
                    Player found = null;
                    if (args[0].length() > 1 && args[0].startsWith("#") && Strings.canParseInt(args[0].substring(1))) {
                        int id = Strings.parseInt(args[0].substring(1));
                        for (Player p: Vars.playerGroup.all()){
                            if (p.id == id){
                                found = p;
                                break;
                            }
                        }
                    } else {
                        for (Player p: Vars.playerGroup.all()){
                            if (p.name.equalsIgnoreCase(args[0])){
                                found = p;
                                break;
                            }
                        }
                    }
                    if (found != null) {
                        TextChannel tc = this.getTextChannel(data.getString("channel_id"));
                        Role r = this.getRole(data.getString("role_id"));
                        if (tc == null || r == null) {
                            player.sendMessage("[scarlet]Эта команда отключена");
                            return;
                        }
                        //send message
                        if (args.length > 1) {
                            new MessageBuilder()
                                    .setEmbed(new EmbedBuilder()
                                            .setTitle("Потенциальный грифер онлайн")
                                            .setDescription(r.getMentionTag())
                                            .addField("Имя ", found.name)
                                            .addField("Причина ", args[ 1 ])
                                            .setColor(Color.ORANGE)
                                            .setFooter("Отправлено " + player.name))
                                    .send(tc);
                        } else {
                            new MessageBuilder()
                                    .setEmbed(new EmbedBuilder()
                                            .setTitle("Потенциальный грифер онлайн")
                                            .setDescription(r.getMentionTag())
                                            .addField("Имя ", found.name)
                                            .setColor(Color.ORANGE)
                                            .setFooter("Отправлено " + player.name))
                                    .send(tc);
                        }
                        Call.sendMessage(found.name + "[sky] заявка отправлена.");
                        cooldowns.put(System.currentTimeMillis() / 1000L, player.uuid);
                    } else {
                        player.sendMessage("[scarlet]Игрок[orange] '" + args[ 0 ] + "'[scarlet] не найден.");
                    }
                }
            });
        }
    }

    public TextChannel getTextChannel(String id){
        Optional<Channel> dc = this.api.getChannelById(id);
        if (!dc.isPresent()) {
            System.out.println("[ERR!] DiscordPlugin: Текстовый Канал не найден!");
            return null;
        }
        Optional<TextChannel> dtc = dc.get().asTextChannel();
        if (!dtc.isPresent()){
            System.out.println("[ERR!] DiscordPlugin: Текстовый Канал не найден!");
            return null;
        }
        return dtc.get();
    }

    public Role getRole(String id){
        Optional<Role> r1 = this.api.getRoleById(id);
        if (!r1.isPresent()) {
            System.out.println("[ERR!] DiscordPlugin: Admin Роль Не найдена!");
            return null;
        }
        return r1.get();
    }
    /*
    private void makeSettingsFile(String _name){
        JSONObject obj = new JSONObject();
        obj.put("token", "put your token here");

        JSONObject inGame = new JSONObject();
        inGame.put("dchannel_id", "");
        inGame.put("channel_id", "");
        inGame.put("role_id", "");

        obj.put("in-game", inGame);

        JSONObject discord = new JSONObject();
        String[] discordFields = {
                "closeServer_role_id",
                "gameOver_role_id",
                "changeMap_role_id",
                "serverdown_role_id",
                "serverdown_name"
        };
        for (String fname : discordFields){
            discord.put(fname, "");
        }
        obj.put("discord", discord);

        //make file
        Path path = Paths.get(String.valueOf(Core.settings.getDataDirectory().child("mods/"+_name)));
        try {
            PrintWriter writer = new PrintWriter(path.toString(), "UTF-8");
            writer.println(obj.toString());
        } catch (Exception e){
            e.printStackTrace();
        }
    }*/
}