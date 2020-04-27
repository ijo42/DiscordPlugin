package disc.command;

import disc.CommandsConstants;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.entities.type.Player;
import mindustry.gen.Call;
import mindustry.world.modules.ItemModule;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;


public class ComCommands implements MessageCreateListener {
    @Override
    public void onMessageCreate(MessageCreateEvent event){
        if (event.getMessageContent().startsWith(CommandsConstants.ChatCommand)) {
            //discord -> server
            String[] msg = event.getMessageContent().split(" ", 2);
            if (msg.length < 2)
                Call.sendMessage("\nИспользуйте " + CommandsConstants.ChatCommand + " [текст]");
            else
                Call.sendMessage("[sky]" + event.getMessageAuthor().getName() + " @discord >[] " + msg[ 1 ].trim());
        } else if (event.getMessageContent().equalsIgnoreCase(CommandsConstants.PlayersCommand)) {
            StringBuilder lijst = new StringBuilder();
            StringBuilder admins = new StringBuilder();
            lijst.append("Игроки: ").append(Vars.playerGroup.size()).append("\n");
            admins.append("Админов онлайн: ").append(Vars.playerGroup.all().count(p -> p.isAdmin)).append("\n");
            for (Player p : Vars.playerGroup.all()) {
                if (p.isAdmin) {
                    admins.append("* ").append(p.name.trim()).append("\n");
                } else {
                    lijst.append("* ").append(p.name.trim()).append("\n");
                }
            }
            new MessageBuilder().appendCode("", lijst.toString() + admins.toString()).send(event.getChannel());
        } else if (event.getMessageContent().equalsIgnoreCase(CommandsConstants.InfoCommand)) {
            try {
                String lijst =
                        "Карта: " + Vars.world.getMap().name() + "\n" +
                                "Автор: " + Vars.world.getMap().author() + "\n" +
                                "Волна: " + Vars.state.wave + "\n" +
                                "Врагов: " + Vars.state.enemies + "\n" +
                                "Игроков: " + Vars.playerGroup.size() + '\n' +
                                "Админов онлайн: " + Vars.playerGroup.all().count(p -> p.isAdmin);
                new MessageBuilder().appendCode("", lijst).send(event.getChannel());
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        } else if (event.getMessageContent().equalsIgnoreCase(CommandsConstants.ResourceInfoCommand)) {
            if (!Vars.state.rules.waves) {
                event.getChannel().sendMessage("Доступно только для режима Survival!");
            } else if (Vars.playerGroup.isEmpty()) {
                event.getChannel().sendMessage("Нет игроков онлайн :(");
            } else {
                ItemModule core = Vars.playerGroup.all().get(0).getClosestCore().items;
                String lijst =
                        "Предметы в ядре\n\n" +
                                "Медь: " + core.get(Items.copper) + "\n" +
                                "Свинец: " + core.get(Items.lead) + "\n" +
                                "Графит: " + core.get(Items.graphite) + "\n" +
                                "Метастекло: " + core.get(Items.metaglass) + "\n" +
                                "Титаниум: " + core.get(Items.titanium) + "\n" +
                                "Ториум: " + core.get(Items.thorium) + "\n" +
                                "Кремний: " + core.get(Items.silicon) + "\n" +
                                "Пластан: " + core.get(Items.plastanium) + "\n" +
                                "Фазовая Ткань: " + core.get(Items.phasefabric) + "\n" +
                                "Кинетический Сплав: " + core.get(Items.surgealloy) + "\n";
                new MessageBuilder().appendCode("", lijst).send(event.getChannel());
            }
        }

    }
}
