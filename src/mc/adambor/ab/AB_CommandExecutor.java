package mc.adambor.ab;

import mc.alk.arena.BattleArena;
import mc.alk.arena.executors.CustomCommandExecutor;
import mc.alk.arena.executors.MCCommand;
import org.bukkit.entity.Player;

public class AB_CommandExecutor extends CustomCommandExecutor{

	@MCCommand(cmds={"addCompressor"}, admin=true)
	public static boolean addCompressor(Player sender, ArenaBlutonium arena) {
		arena.addCompressor(sender.getLocation());
		BattleArena.saveArenas(Main.plugin);
		return sendMessage(sender,"&2Compressor added!");
	}
	@MCCommand(cmds={"addBlutonium"}, admin=true)
	public static boolean addBlutonium(Player sender, ArenaBlutonium arena, Integer index) {
		arena.addBlutonium(sender.getLocation(),index-1);
		BattleArena.saveArenas(Main.plugin);
		return sendMessage(sender,"&2Blutonium with index:&6 "+index+"added!");
	}
	@MCCommand(cmds={"clearCompressors"}, admin=true)
	public static boolean clearCompressors(Player sender, ArenaBlutonium arena) {
		arena.clearCompressors();
		BattleArena.saveArenas(Main.plugin);
		return sendMessage(sender,"&2All compressors removed!");
	}
	@MCCommand(cmds={"clearBlutonium"}, admin=true)
	public static boolean clearBlutonium(Player sender, ArenaBlutonium arena) {
		arena.clearBlutonium();
		BattleArena.saveArenas(Main.plugin);
		return sendMessage(sender,"&2All blutonium removed!");
	}
}
