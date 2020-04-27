package disc;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.permission.Role;

import java.util.Optional;

public class UtilMethods {
    public TextChannel getTextChannel(DiscordApi api, String id){
        Optional<Channel> dc = api.getChannelById(id);
        if (!dc.isPresent()) {
            System.out.println("[ERR!] DiscordPlugin: Текстовый Канал не найден!");
            return null;
        }
        Optional<TextChannel> dtc = dc.get().asTextChannel();
        if (!dtc.isPresent()) {
            System.out.println("[ERR!] DiscordPlugin: Текстовый Канал не найден!");
            return null;
        }
        dtc.get().getMessageCache().setCapacity(10);
        return dtc.get();
    }

    public Role getRole(DiscordApi api, String id){
        Optional<Role> r1 = api.getRoleById(id);
        if (!r1.isPresent()) {
            System.out.println("[ERR!] DiscordPlugin: Админ Роль не найдена!");
            return null;
        }
        return r1.get();
    }
}
