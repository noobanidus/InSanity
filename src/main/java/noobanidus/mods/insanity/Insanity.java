package noobanidus.mods.insanity;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.tiffit.sanity.SanityCapability;
import org.apache.logging.log4j.Logger;

import java.util.List;

@Mod.EventBusSubscriber
@Mod(modid = Insanity.MODID, name = Insanity.MODNAME, version = Insanity.VERSION, dependencies = "required-after:sanity")
@SuppressWarnings("WeakerAccess")
public class Insanity {
  public static final String MODID = "insanity";
  public static final String MODNAME = "Insanity";
  public static final String VERSION = "GRADLE:VERSION";

  public static Logger LOG;

  @SuppressWarnings("unused")
  @Mod.Instance(Insanity.MODID)
  public static Insanity instance;

  @Mod.EventHandler
  public void preInit(FMLPreInitializationEvent event) {
    LOG = event.getModLog();
  }

  @Mod.EventHandler
  public void onServerStarting(FMLServerStartingEvent event) {
    event.registerServerCommand(new CommandAdjustSanity());
  }

  @Config(modid = Insanity.MODID)
  public static class InsanityConfig {
    @Config.Comment("The permission level required to execute the increment/decrement sanity commands")
    @Config.RangeInt(min = 0, max = 10)
    public static int permissionLevel = 4;
  }

  public static class CommandAdjustSanity extends CommandBase {
    @Override
    public String getName() {
      return "adjustsanity";
    }

    @Override
    public String getUsage(ICommandSender sender) {
      return "/adjustsanity [amount] ([player name, optional]) | use negative amounts to reduce, positive to increase; accepts floating point numbers";
    }

    @Override
    public int getRequiredPermissionLevel() {
      return InsanityConfig.permissionLevel;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      EntityPlayer target = null;
      float amount = 0;
      if (args.length >= 1 && sender instanceof EntityPlayer) {
        target = (EntityPlayer) sender;
        try {
          amount = Float.valueOf(args[0]);
        } catch (NumberFormatException e) {
          throw new WrongUsageException(getUsage(sender));
        }
      }
      if (args.length == 2) {
        List<EntityPlayerMP> players = server.getPlayerList().getPlayers();
        for (EntityPlayerMP potential : players) {
          if (potential.getName().equals(args[1].trim())) {
            target = potential;
            break;
          }
        }
      }
      if (target == null || amount == 0) {
        throw new WrongUsageException(getUsage(sender));
      }
      SanityCapability cap = target.getCapability(SanityCapability.INSTANCE, null);
      if (cap != null) {
        float actual = cap.getSanityExact();
        cap.setSanity(actual + amount);
        actual = cap.getSanityExact();
        if (sender != target) {
          sender.sendMessage(new TextComponentString("Adjust sanity of player " + args[1] + " by " + args[0] + ", sanity is now " + actual));
        }
      }
    }
  }
}
