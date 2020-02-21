/*
 * Copyright (c) 2020, Wild Adventure
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 * 4. Redistribution of this software in source or binary forms shall be free
 *    of all charges or fees to the recipient of this software.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gmail.filoghost.treeassist;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

public class TreeAssist extends JavaPlugin implements Listener {

	private static final String METADATA_KEY = "PlayerDrop";
	
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, this);
	}

	@SuppressWarnings("deprecation")
	@EventHandler (ignoreCancelled = true)
	public void onSaplingDrop(ItemSpawnEvent event) {
		if (event.getEntity().getItemStack().getType() == Material.SAPLING) {
			if (event.getEntity().hasMetadata(METADATA_KEY)) {
				// Non viene considerato se l'ha gettato un giocatore
				return;
			}
			
			int y = event.getLocation().getBlockY();
			if (y < 1 && y > 256) return;
			
			Block block = event.getLocation().getBlock();
			
			if (!canSaplingDropPassThrough(block.getRelative(BlockFace.DOWN).getType())) {
				return;
			}

			while (canSaplingDropPassThrough(block.getType()) && block.getY() > 1) {
				block = block.getRelative(BlockFace.DOWN);
			}
			
			if (canSaplingBePlantedOn(block.getType())) {

				block = block.getRelative(BlockFace.UP);
				
				if (block.getType() != Material.AIR && block.getType() != Material.LONG_GRASS) {
					// Solo aria e erba possono essere sostituite
					return;
				}
				
				if (!hasTreePartNear(block)) {
					block.setType(Material.SAPLING);
					block.setData((byte) event.getEntity().getItemStack().getDurability());
				}
			}
		}
	}
	
	private boolean hasTreePartNear(Block block) {
		int x = block.getX();
		int y = block.getY();
		int z = block.getZ();
		World world = block.getWorld();
		for (int i = x - 2 ; i < x + 2; i++) {
			for (int j = y - 2 ; j < y + 2; j++) {
				for (int k = z - 2 ; k < z + 2; k++) {
					if (isTreePart(world.getBlockAt(i, j, k).getType())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@EventHandler (ignoreCancelled = true)
	public void onSaplingDrop(PlayerDropItemEvent event) {
		if (event.getItemDrop().getItemStack().getType() == Material.SAPLING) {
			// Memorizza se l'ha gettato un giocatore
			event.getItemDrop().setMetadata(METADATA_KEY, new FixedMetadataValue(this, true));
		}
	}
	
	private boolean canSaplingDropPassThrough(Material mat) {
		return mat == Material.AIR || mat == Material.LEAVES || mat == Material.LEAVES_2 || mat == Material.LONG_GRASS;
	}
	
	private boolean canSaplingBePlantedOn(Material mat) {
		return mat == Material.GRASS || mat == Material.DIRT || mat == Material.MYCEL;
	}
	
	private boolean isTreePart(Material mat) {
		return mat == Material.SAPLING || mat == Material.LOG || mat == Material.LOG_2;
	}
	
}
