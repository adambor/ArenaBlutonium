![alt tag](http://dev.bukkit.org/media/images/82/382/ArenaBlutonium_2.png)

An extraction minigame mode from the popular game LoadOut.

## How to play?

  On the game start one player from each team is choosen to be collector.
  Collector must earn blutonium by sneaking at dropped blutonium and drop it to one of the compressors by moving to distance 3 blocks from compressor.
  If collector death in 5 seconds is choosen new another collector.

## Features

  - Multiple arenas
  - Simple drop-off mechanics
  - Throwing blutonium
  - Original minigame
  - If you want another feauture use tickets/comments or PM me ;)

## Installing

  - Put the ArenaBlutonium.jar in your plugins folder, along with BattleArena.jar. 

## Creating an arena

  - 1. /ab create <arena name> : Example /ab create myFirstArena
  - 2. /ab alter <arena name> spawn 2 <- setup a second spawn, you can keep adding them if you want more than 2, like /ab alter <arena name> spawn <team number>. Example : /ab alter myFirstArena spawn 3 
  - 3. Go to the place where you want a deployment station (In this plugin called compressor) and type /ab addCompressor <arena name> (You can create as more compressors as you want simple type /ab addCompressor <arena name> in chat several times)
  - 4. Go to the place where you want to spawn blutonium and type /ab addBlutonium <arena name> <index>

## Joining

  - arena.join.arenablutonium : Permission node to join.
  - arena.leave : Permission to leave the game.
  - /ab join : Command to join. Or you can click on a join sign
  - /ab leave : Command to leave. Or you can click on a leave sign 

## Help

If you found any bug or want new feature to be added leave a comment or use tickets.

## Requirements

This plugin is made to hook into [BattleArena](http://dev.bukkit.org/bukkit-plugins/battlearena2), so you also need to download it as well 

## Official bukkit page

http://dev.bukkit.org/bukkit-plugins/ArenaBlutonium
