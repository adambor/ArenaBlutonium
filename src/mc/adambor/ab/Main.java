package mc.adambor.ab;

import mc.alk.arena.BattleArena;
import mc.alk.arena.objects.victoryconditions.VictoryType;
import mc.alk.arena.util.Log;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin{

	static Main plugin;
	
	@Override
	public void onEnable(){
		plugin = this;
		VictoryType.register(Victory.class, this);
		BattleArena.registerCompetition(this, "ArenaBlutonium", "ab", ArenaBlutonium.class, new AB_CommandExecutor());
		loadConfig();
		Log.info("[" + getName()+ "] v" + getDescription().getVersion()+ " enabled!");
	}

	@Override
	public void onDisable(){
		Log.info("[" + getName()+ "] v" + getDescription().getVersion()+ " stopping!");
	}
	public void loadConfig(){
		saveDefaultConfig();
        FileConfiguration conf = plugin.getConfig();
		ArenaBlutonium.pointsToWin = conf.getInt("scoreToWin", 30);
	}
	@Override
	public void reloadConfig(){
		super.reloadConfig();
	    loadConfig();
	}

}
