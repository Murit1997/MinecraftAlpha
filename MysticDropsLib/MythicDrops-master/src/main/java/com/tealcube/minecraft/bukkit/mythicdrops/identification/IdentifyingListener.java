/*
 * This file is part of MythicDrops, licensed under the MIT License.
 *
 * Copyright (C) 2013 Richard Harrah
 *
 * Permission is hereby granted, free of charge,
 * to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.tealcube.minecraft.bukkit.mythicdrops.identification;

import com.tealcube.minecraft.bukkit.mythicdrops.MythicDropsPlugin;
import com.tealcube.minecraft.bukkit.mythicdrops.api.items.ItemGenerationReason;
import com.tealcube.minecraft.bukkit.mythicdrops.api.tiers.Tier;
import com.tealcube.minecraft.bukkit.mythicdrops.logging.MythicLoggerFactory;
import com.tealcube.minecraft.bukkit.mythicdrops.utils.ItemUtil;
import com.tealcube.minecraft.bukkit.mythicdrops.utils.TierUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class IdentifyingListener implements Listener {

  private static final Logger LOGGER = MythicLoggerFactory.getLogger(IdentifyingListener.class);

  private Map<String, ItemStack> heldIdentify;
  private MythicDropsPlugin plugin;

  public IdentifyingListener(MythicDropsPlugin plugin) {
    this.plugin = plugin;
    heldIdentify = new HashMap<>();
  }

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
    Player player = event.getEntity();
    if (heldIdentify.containsKey(player.getName())) {
      heldIdentify.remove(player.getName());
    }
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onRightClick(PlayerInteractEvent event) {
    if (event.getAction() != Action.RIGHT_CLICK_AIR
        && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
      LOGGER.fine("event.getAction() != RIGHT_CLICK_AIR && event.getAction() != RIGHT_CLICK_BLOCK");
      return;
    }
    Player player = event.getPlayer();
    ItemStack itemInMainHand = player.getEquipment().getItemInMainHand();
    if (itemInMainHand == null || itemInMainHand.getType() == null) {
      LOGGER.fine("itemInMainHand == null || itemInMainHand.getType() == null");
      return;
    }
    if (!player.hasPermission("mythicdrops.identify")) {
      LOGGER.fine("!player.hasPermission(\"mythicdrops.identify\")");
      return;
    }
    String itemInMainHandType = ItemUtil.getItemTypeFromMaterial(itemInMainHand.getType());

    if (heldIdentify.containsKey(player.getName())) {
      LOGGER.fine("heldIdentify.containsKey(player.getName())");
      identifyItem(event, player, itemInMainHand, itemInMainHandType);
    } else {
      LOGGER.fine("!heldIdentify.containsKey(player.getName())");
      addHeldIdentify(event, player, itemInMainHand);
    }
  }

  private void addHeldIdentify(PlayerInteractEvent event, final Player player, ItemStack itemInHand) {
    if (!itemInHand.hasItemMeta()) {
      LOGGER.fine("!itemInHand.hasItemMeta()");
      return;
    }
    ItemMeta im = itemInHand.getItemMeta();
    ItemStack identityTome = new IdentityTome();
    if (!im.hasDisplayName() ||
        !identityTome.getItemMeta().hasDisplayName() ||
        !im.getDisplayName().equals(identityTome.getItemMeta().getDisplayName())) {
      LOGGER.fine("!im.hasDisplayName() || !identityTome.getItemMeta().hasDisplayName() || !im.getDisplayName().equals(identityTome.getItemMeta().getDisplayName())");
      return;
    }
    player.sendMessage(
        plugin.getConfigSettings().getFormattedLanguageString("command.identifying-instructions"));
    heldIdentify.put(player.getName(), itemInHand);
    Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> heldIdentify.remove(player.getName()), 20L * 30);
    cancelResults(event);
  }

  private void identifyItem(PlayerInteractEvent event, Player player, ItemStack itemInHand, String itemType) {
    LOGGER.fine("identifyItem() - ENTRY");
    if (ItemUtil.isArmor(itemType) || ItemUtil.isTool(itemType)) {
      LOGGER.fine("identifyItem() - is tool or armor");
      if (!itemInHand.hasItemMeta() || !itemInHand.getItemMeta().hasDisplayName()) {
        cannotUse(event, player);
        return;
      }
      if (!player.getInventory().contains(heldIdentify.get(player.getName()))) {
        player.sendMessage(plugin.getConfigSettings().getFormattedLanguageString("command.identifying-do-not-have"));
        heldIdentify.remove(player.getName());
        return;
      }
      UnidentifiedItem uid = new UnidentifiedItem(itemInHand.getData().getItemType());
      boolean b = itemInHand.getItemMeta().getDisplayName().equals(uid.getItemMeta().getDisplayName());
      if (!b) {
        cannotUse(event, player);
        return;
      }
      String potentialTierString = "";
      if (itemInHand.getItemMeta().hasLore() && itemInHand.getItemMeta().getLore().size() > 0) {
        potentialTierString = ChatColor.stripColor(itemInHand.getItemMeta().getLore().get(itemInHand
            .getItemMeta().getLore().size() - 1));
      }
      Tier potentialTier = TierUtil.getTier(potentialTierString);
      List<Tier> iihTiers = new ArrayList<>(ItemUtil.getTiersFromMaterial(itemInHand.getType()));
      Tier iihTier = potentialTier != null ? potentialTier : TierUtil.randomTierWithIdentifyChance(iihTiers);
      if (iihTier == null) {
        cannotUse(event, player);
        return;
      }

      ItemStack iih = MythicDropsPlugin.getNewDropBuilder().withItemGenerationReason
          (ItemGenerationReason.EXTERNAL).withMaterial(itemInHand.getType()).withTier(iihTier)
          .useDurability(false).build();
      iih.setDurability(itemInHand.getDurability());

      ItemMeta itemMeta = iih.getItemMeta();
      List<String> lore = new ArrayList<>();
      if (itemMeta.hasLore()) {
        lore = itemMeta.getLore();
      }

      itemMeta.setLore(lore);
      iih.setItemMeta(itemMeta);

      IdentificationEvent identificationEvent = new IdentificationEvent(iih, player);
      Bukkit.getPluginManager().callEvent(identificationEvent);

      if (identificationEvent.isCancelled()) {
        cannotUse(event, player);
        return;
      }

      int indexOfItem = player.getInventory().first(heldIdentify.get(player.getName()));
      ItemStack inInventory = player.getInventory().getItem(indexOfItem);
      inInventory.setAmount(inInventory.getAmount() - 1);
      player.getInventory().setItem(indexOfItem, inInventory);
      player.getEquipment().setItemInMainHand(identificationEvent.getResult());
      player.sendMessage(
          plugin.getConfigSettings().getFormattedLanguageString("command.identifying-success"));
      cancelResults(event);
      heldIdentify.remove(player.getName());
    } else {
      LOGGER.fine("identifyItem() - not tool or armor");
      cannotUse(event, player);
    }
  }

  private void cannotUse(PlayerInteractEvent event, Player player) {
    player.sendMessage(
        plugin.getConfigSettings().getFormattedLanguageString("command.identifying-cannot-use"));
    cancelResults(event);
    heldIdentify.remove(player.getName());
  }

  private void cancelResults(PlayerInteractEvent event) {
    LOGGER.fine("cancelResults - cancelling results");
    event.setCancelled(true);
    event.setUseInteractedBlock(Event.Result.DENY);
    event.setUseItemInHand(Event.Result.DENY);
    event.getPlayer().updateInventory();
  }

}
