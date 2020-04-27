package disc.command;

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
        if (event.getMessageContent().startsWith("..chat ")){
            //discord -> server
            String[] msg = event.getMessageContent().split(" ", 2);
            Call.sendMessage("[sky]" +event.getMessageAuthor().getName()+ " @discord >[] " + msg[1].trim());
        }

        //playerlist
        else if (event.getMessageContent().equalsIgnoreCase("..players")){
            StringBuilder lijst = new StringBuilder();
            StringBuilder admins = new StringBuilder();
            lijst.append("players: ").append(Vars.playerGroup.size()).append("\n");
            admins.append("online admins: ");// + Vars.playerGroup.all().count(p->p.isAdmin)+"\n");
            for (Player p :Vars.playerGroup.all()){
                if (p.isAdmin){
                    admins.append("* ").append(p.name.trim()).append("\n");
                } else {
                    lijst.append("* ").append(p.name.trim()).append("\n");
                }
            }
            new MessageBuilder().appendCode("", lijst.toString() + admins.toString()).send(event.getChannel());
        }
        //info
        else if (event.getMessageContent().equalsIgnoreCase("..info")){
            try {
                //lijst.append("admins (online): " + Vars.playerGroup.all().count(p -> p.isAdmin));
                String lijst = "map: " + Vars.world.getMap().name() + "\n" +
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
        }
        //infores, werkt enkel als er minstens 1 speler online is!
        else if (event.getMessageContent().equalsIgnoreCase("..infores")){
            //event.getChannel().sendMessage("not implemented yet...");
            if (!Vars.state.rules.waves){
                event.getChannel().sendMessage("Only available when playing survivalmode!");
            } else if(Vars.playerGroup.isEmpty()) {
                event.getChannel().sendMessage("No players online!");
            } else {
                StringBuilder lijst = new StringBuilder();
                lijst.append("amount of items in the core\n\n");
                ItemModule core = Vars.playerGroup.all().get(0).getClosestCore().items;
                lijst.append("copper: ").append(core.get(Items.copper)).append("\n");
                lijst.append("lead: ").append(core.get(Items.lead)).append("\n");
                lijst.append("graphite: ").append(core.get(Items.graphite)).append("\n");
                lijst.append("metaglass: ").append(core.get(Items.metaglass)).append("\n");
                lijst.append("titanium: ").append(core.get(Items.titanium)).append("\n");
                lijst.append("thorium: ").append(core.get(Items.thorium)).append("\n");
                lijst.append("silicon: ").append(core.get(Items.silicon)).append("\n");
                lijst.append("plastanium: ").append(core.get(Items.plastanium)).append("\n");
                lijst.append("phase fabric: ").append(core.get(Items.phasefabric)).append("\n");
                lijst.append("surge alloy: ").append(core.get(Items.surgealloy)).append("\n");

                new MessageBuilder().appendCode("", lijst.toString()).send(event.getChannel());
            }


        }

    }
}
