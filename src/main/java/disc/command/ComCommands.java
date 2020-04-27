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
                Call.sendMessage("\nUse " + CommandsConstants.ChatCommand + " [text]");
            else
                Call.sendMessage("[sky]" + event.getMessageAuthor().getName() + " @discord >[] " + msg[ 1 ].trim());
        } else if (event.getMessageContent().equalsIgnoreCase(CommandsConstants.PlayersCommand)) {
            StringBuilder lijst = new StringBuilder();
            StringBuilder admins = new StringBuilder();
            lijst.append("players: ").append(Vars.playerGroup.size()).append("\n");
            admins.append("online admins: ").append(Vars.playerGroup.all().count(p -> p.isAdmin)).append("\n");
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
                        "map: " + Vars.world.getMap().name() + "\n" +
                                "author: " + Vars.world.getMap().author() + "\n" +
                                "wave: " + Vars.state.wave + "\n" +
                                "enemies: " + Vars.state.enemies + "\n" +
                                "players: " + Vars.playerGroup.size() + '\n' +
                                "admins online: " + Vars.playerGroup.all().count(p -> p.isAdmin);
                new MessageBuilder().appendCode("", lijst).send(event.getChannel());
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        } else if (event.getMessageContent().equalsIgnoreCase(CommandsConstants.ResourceInfoCommand)) {
            if (!Vars.state.rules.waves) {
                event.getChannel().sendMessage("Only available when playing survivalmode!");
            } else if (Vars.playerGroup.isEmpty()) {
                event.getChannel().sendMessage("No players online!");
            } else {
                ItemModule core = Vars.playerGroup.all().get(0).getClosestCore().items;
                String lijst =
                        "amount of items in the core\n\n" +
                                "copper: " + core.get(Items.copper) + "\n" +
                                "lead: " + core.get(Items.lead) + "\n" +
                                "graphite: " + core.get(Items.graphite) + "\n" +
                                "metaglass: " + core.get(Items.metaglass) + "\n" +
                                "titanium: " + core.get(Items.titanium) + "\n" +
                                "thorium: " + core.get(Items.thorium) + "\n" +
                                "silicon: " + core.get(Items.silicon) + "\n" +
                                "plastanium: " + core.get(Items.plastanium) + "\n" +
                                "phase fabric: " + core.get(Items.phasefabric) + "\n" +
                                "surge alloy: " + core.get(Items.surgealloy) + "\n";
                new MessageBuilder().appendCode("", lijst).send(event.getChannel());
            }
        }

    }
}
