package mc.adambor.ab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import mc.alk.arena.BattleArena;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.messaging.MatchMessageHandler;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.serializers.Persist;
import mc.alk.scoreboardapi.api.SEntry;
import mc.alk.scoreboardapi.api.SObjective;
import mc.alk.scoreboardapi.api.SScoreboard;
import mc.alk.scoreboardapi.scoreboard.SAPIDisplaySlot;

public class ArenaBlutonium extends Arena {
	@Persist 
	List<Location> compressorsL = new ArrayList<Location>();
	@Persist 
	HashMap<Integer,Location> itemsL = new HashMap<Integer, Location>();
	
	Integer respawncounter = 0;
	Integer brespawning;
	final List<ArmorStand> compressors = new ArrayList<ArmorStand>();
	final HashMap<Integer,Entity> items = new HashMap<Integer, Entity>();
	HashMap<Location,Entity> bspawnpoints = new HashMap<Location, Entity>();
	HashMap<Integer,ArenaPlayer> collectors = new HashMap<Integer, ArenaPlayer>();
	ItemStack blutonium = new ItemStack(Material.DIAMOND);
	SScoreboard scoreb;
    SObjective objective;
    HashMap<ArenaTeam,SEntry> teams = new HashMap<ArenaTeam,SEntry>();
    HashMap<ArenaTeam,SEntry> collectorsent = new HashMap<ArenaTeam,SEntry>();
    HashMap<ArenaTeam,Integer> collectorsentint = new HashMap<ArenaTeam,Integer>();
    MatchMessageHandler mmh;
    Random rand = new Random();
	
	@Override
	public void onStart(){
		mmh = match.getMessageHandler();
		scoreb = match.getScoreboard();
		objective = scoreb.registerNewObjective( "ab", "ArenaBlutonium", "&bBlutonium captured", SAPIDisplaySlot.SIDEBAR);
		objective.setDisplayTeams(false);
		objective.setDisplayPlayers(false);
		int i = -1;
		for(ArenaTeam team : match.getTeams()){
			teams.put(team, objective.addEntry(team.getDisplayName(), 0));
			objective.addEntry(team.getDisplayName()+" collector:", i);
			SEntry ent = objective.addEntry(team.getDisplayName()+"*",i-1);
			collectorsent.put(team, ent);
			collectorsentint.put(team, i-1);
			i=i-2;
		}
		setBlutonium();
		spawnCompressors();
		spawnBlutonium();
		for(ArenaTeam team:match.getTeams()){
			setCollector(team);
		}
		brespawning = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.plugin, new Runnable(){
				@Override
				public void run() {
					Entity itm = items.get(respawncounter);
					if(itm.isDead()){
                        spawnBlutoniumOne(itm.getLocation(), respawncounter);
                        itm.remove();
					}
					if(respawncounter<items.size()-1){
				    	respawncounter++;
					} else {
						respawncounter=0;
					}
				}
			}, 0L, 5*20L);
	}
	public void spawnBlutoniumOne(Location l, Integer i) {
		l.getChunk().load();
		Item Eitem = l.getWorld().dropItem(l,blutonium);
		Eitem.setPickupDelay(Integer.MAX_VALUE);
		items.put(i,(Entity)Eitem);
		bspawnpoints.put(l,(Entity)Eitem);
	}
	@Override
	public void onFinish(){
		despawnBlutonium();
		despawnCompressors();
		if (brespawning != null){
            Bukkit.getScheduler().cancelTask(brespawning);
            brespawning = null;
        }
	}
	@ArenaEventHandler
	public void onPlayerSneak(PlayerToggleSneakEvent event){
		if(event.isSneaking() && collectors.containsValue(BattleArena.toArenaPlayer(event.getPlayer()))){
			Player p = event.getPlayer();
			List<Entity> ent = p.getNearbyEntities(1.0,1.0,1.0);
			for(Entity entity:ent){
				if(items.containsValue(entity) && p.getInventory().contains(blutonium)==false){
					entity.remove();
					giveBlutonium(p);
				}
			}
		}
	}
	@ArenaEventHandler
	public void onPlayerDeath(PlayerDeathEvent evnt){
		final ArenaPlayer ap = BattleArena.toArenaPlayer(evnt.getEntity());
		if(collectors.containsValue(ap)){
                    setCollector(ap.getTeam());
		}
	}
	@ArenaEventHandler
	public void onPlayerInteract(PlayerInteractEvent evnt){
		if(evnt.getAction()==Action.RIGHT_CLICK_AIR && evnt.getItem().equals(blutonium)){
		    throwBlutonium(evnt.getPlayer());
			evnt.setCancelled(true);
		}
	}
	@ArenaEventHandler
	public void onPlayerMoveEvent(PlayerMoveEvent event){
		if (event.getFrom().getBlockX() != event.getTo().getBlockX()
			|| event.getFrom().getBlockY() != event.getTo().getBlockY()
			|| event.getFrom().getBlockZ() != event.getTo().getBlockZ()){
				Player p = event.getPlayer();
				for(Entity ent:p.getNearbyEntities(3.0,3.0,3.0)){
					if(compressors.contains(ent)){
						ArenaPlayer ap = BattleArena.toArenaPlayer(p);
						if(collectors.containsValue(ap) && p.getInventory().contains(blutonium)){
							p.getInventory().remove(blutonium);
							Location loc = ent.getLocation();
							loc.setY(ent.getLocation().getY()+1.5D);
							loc.getWorld().playEffect(loc, Effect.MOBSPAWNER_FLAMES, 1);
							objective.setPoints(teams.get(ap.getTeam()), objective.getPoints(teams.get(ap.getTeam()))+1);
						}
					}
				}
			}
	}
	@ArenaEventHandler
	public void onPlayerQ(PlayerDropItemEvent e){
		if(e.getItemDrop().getItemStack().getType().equals(Material.DIAMOND)){
			throwBlutonium(e.getPlayer());
			e.getItemDrop().remove();
		}
	}
	public void spawnCompressors(){
		for(Location l:compressorsL){
			l.getChunk().load();
			l.setPitch(0);
			l.setYaw(0);
			ArmorStand comp = (ArmorStand) l.getWorld().spawnEntity(l,EntityType.ARMOR_STAND);
			comp.setCustomName(ChatColor.GREEN+"Compressor");
			comp.setCustomNameVisible(true);
			comp.setHelmet(new ItemStack(Material.REDSTONE_BLOCK));
			comp.setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
			comp.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
			comp.setBoots(new ItemStack(Material.DIAMOND_BOOTS));
			compressors.add(comp);
		}
	}
	@ArenaEventHandler
	public void onEntityDamage(EntityDamageByEntityEvent e){
		if(e.getEntityType().equals(EntityType.ARMOR_STAND)){
			e.setCancelled(true);
		}
	}
	public void spawnBlutonium(){
		Location l;
		for(int i=0;i<itemsL.size();i++){
			l = itemsL.get(i);
			l.getChunk().load();
			Item Eitem = l.getWorld().dropItem(l,blutonium);
			Eitem.setPickupDelay(Integer.MAX_VALUE);
			items.put(i,(Entity)Eitem);
			bspawnpoints.put(l,(Entity)Eitem);
		}
	}
	public void giveBlutonium(Player player){
		Inventory inv = player.getInventory();
		inv.addItem(blutonium);
	}
	public void setCollector(final ArenaTeam team){
		collectors.put(team.getId(), null);
		scoreb.setEntryDisplayName(collectorsent.get(team), team.getDisplayName()+"*");
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable(){
			public void run(){
				Set<ArenaPlayer> tplayers = team.getPlayers();
				int i = 0;
				HashMap<Integer,ArenaPlayer> randplayers = new HashMap<Integer, ArenaPlayer>();
				for(ArenaPlayer ap:tplayers){
					randplayers.put(i,ap);
					i++;
				}
				if(randplayers.size() != 1){
			    	randplayers.remove(collectors.get(team.getId()));
				}
				collectors.put(team.getId(),randplayers.get(rand.nextInt(randplayers.size())));
				match.sendMessage(mmh.getMessage("ArenaBlutonium.new_collector").replace("%p", collectors.get(team.getId()).getName()).replace("%t", team.getDisplayName()));
				scoreb.setEntryDisplayName(collectorsent.get(team), collectors.get(team.getId()).getName());
				objective.setPoints(collectorsent.get(team), collectorsentint.get(team));
			}
		}, 100L);
	}
	public void setBlutonium(){
		ItemStack is = new ItemStack(Material.DIAMOND);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(ChatColor.BLUE+"Blutonium");
		is.setItemMeta(im);
		blutonium = is;
	}
	@ArenaEventHandler
	public void interactEvent(PlayerInteractAtEntityEvent e){
	if(e.getRightClicked().getType().equals(EntityType.ARMOR_STAND)){
		e.setCancelled(true);
	}
	}
	public void throwBlutonium(final Player p){
		p.getInventory().remove(blutonium);
		p.getLocation().getWorld().playSound(p.getLocation(), Sound.LAVA_POP, 1, 5);
		ItemStack throwedB = new ItemStack(Material.DIAMOND);
	    throwedB.getItemMeta().setDisplayName(""+rand.nextInt(Integer.MAX_VALUE));
		final Item item = p.getLocation().getWorld().dropItem(p.getEyeLocation(), blutonium);
		item.setPickupDelay(Integer.MAX_VALUE);
		item.setVelocity(p.getLocation().getDirection().multiply(0.5D));
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable(){
				@Override
				public void run() {
					List<Entity> nearby = item.getNearbyEntities(5,5,5);
					for(Entity ent:nearby){
						 if(ent instanceof Player){
							 ArenaPlayer apent = BattleArena.toArenaPlayer((Player) ent);
							 if(!BattleArena.toArenaPlayer(p).getTeam().getPlayers().contains(apent)){
								apent.getPlayer().damage(30.0D,p);
							 }
						 }
					}
				    item.getLocation().getWorld().playEffect(item.getLocation(),Effect.EXPLOSION_HUGE, 1);
				    doBExplosion(item.getLocation(), Material.DIAMOND_BLOCK);
				    item.remove();
				}
			}, 2*20L);
	}
	public void despawnBlutonium(){
		for(int e=0;e<items.size();e++){
			items.get(e).remove();
		}
	}
	public void despawnCompressors(){
		for(Entity comp:compressors){
			comp.remove();
		}
	}
	public void addCompressor(Location loc){
		compressorsL.add(loc);
	}
	public void addBlutonium(Location loc, Integer index){
		itemsL.put(index,loc);
	}
	public void clearCompressors(){
		compressorsL = new ArrayList<Location>();
	}
	public void clearBlutonium(){
		itemsL = new HashMap<Integer, Location>();
	}
	public void doBExplosion(Location l, Material m){
            for(int i=0;i<21;i++){
			ItemStack is = new ItemStack(m);
			ItemMeta meta = is.getItemMeta();
			meta.setDisplayName(""+rand.nextInt());
			is.setItemMeta(meta);
			final Item ent = l.getWorld().dropItem(l, is);
			ent.setPickupDelay(Integer.MAX_VALUE);
            Vector vec = new Vector(rand.nextDouble()/2-0.25,rand.nextDouble()/1.5,rand.nextDouble()/2-0.25);
			ent.setVelocity(vec);
			Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable(){
				public void run() {
                    ent.remove();
				}
			}, 20L);
		}
	}
}
