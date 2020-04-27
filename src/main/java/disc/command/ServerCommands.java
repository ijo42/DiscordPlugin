package disc.command;

import arc.Core;
import arc.Events;
import disc.CommandsConstants;
import mindustry.Vars;
import mindustry.core.GameState;
import mindustry.game.EventType;
import mindustry.game.Team;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.json.JSONObject;

import java.util.Optional;


public class ServerCommands implements MessageCreateListener {
    final String commandDisabled = "Эта команда отключена.";
    final String noPermission = "У Вас не хватает прав для этого!";

    private JSONObject data;

    public ServerCommands(JSONObject _data) {
        this.data = _data;
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessageContent().equalsIgnoreCase(CommandsConstants.GameOverCommand)) {
            if (!data.has("gameOver_role_id")) {
                if (event.isPrivateMessage()) return;
                event.getChannel().sendMessage(commandDisabled);
                return;
            }
            Role r = getRole(event.getApi(), data.getString("gameOver_role_id"));

            if (!hasPermission(r, event)) return;
            if (Vars.state.is(GameState.State.menu)) {
                return;
            }
            Events.fire(new EventType.GameOverEvent(Team.crux));

        } else if (event.getMessageContent().startsWith(CommandsConstants.CloseServerCommand)) {
            if (!data.has("closeServer_role_id")) {
                if (event.isPrivateMessage()) return;
                event.getChannel().sendMessage(commandDisabled);
                return;
            }
            Role r = getRole(event.getApi(), data.getString("closeServer_role_id"));
            if (!hasPermission(r, event)) return;

            Vars.net.dispose(); //todo: check
            Core.app.exit();
        }
    }

    public Role getRole(DiscordApi api, String id){
        Optional<Role> r1 = api.getRoleById(id);
        if (!r1.isPresent()) {
            System.out.println("[ERR!] DiscordPlugin: Роль не найдена!");
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