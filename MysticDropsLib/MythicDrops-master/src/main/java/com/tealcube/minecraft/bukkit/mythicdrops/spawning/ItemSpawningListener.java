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
package com.tealcube.minecraft.bukkit.mythicdrops.spawning;

import com.tealcube.minecraft.bukkit.mythicdrops.MythicDropsPlugin;
import com.tealcube.minecraft.bukkit.mythicdrops.api.MythicDrops;
import com.tealcube.minecraft.bukkit.mythicdrops.api.items.CustomItem;
import com.tealcube.minecraft.bukkit.mythicdrops.api.items.ItemGenerationReason;
import com.tealcube.minecraft.bukkit.mythicdrops.api.names.NameType;
import com.tealcube.minecraft.bukkit.mythicdrops.api.tiers.Tier;
import com.tealcube.minecraft.bukkit.mythicdrops.events.EntityNameEvent;
import com.tealcube.minecraft.bukkit.mythicdrops.events.EntitySpawningEvent;
import com.tealcube.minecraft.bukkit.mythicdrops.identification.IdentityTome;
import com.tealcube.minecraft.bukkit.mythicdrops.identification.UnidentifiedItem;
import com.tealcube.minecraft.bukkit.mythicdrops.items.CustomItemMap;
import com.tealcube.minecraft.bukkit.mythicdrops.logging.MythicLoggerFactory;
import com.tealcube.minecraft.bukkit.mythicdrops.names.NameMap;
import com.tealcube.minecraft.bukkit.mythicdrops.socketting.SocketGem;
import com.tealcube.minecraft.bukkit.mythicdrops.socketting.SocketItem;
import com.tealcube.minecraft.bukkit.mythicdrops.utils.CustomItemUtil;
import com.tealcube.minecraft.bukkit.mythicdrops.utils.EntityUtil;
import com.tealcube.minecraft.bukkit.mythicdrops.utils.ItemStackUtil;
import com.tealcube.minecraft.bukkit.mythicdrops.utils.SocketGemUtil;
import com.tealcube.minecraft.bukkit.mythicdrops.utils.TierUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import mkremins.fanciful.FancyMessage;
import org.apache.commons.lang3.RandomUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public final class ItemSpawningListener implements Listener {

  private static final Logger LOGGER = MythicLoggerFactory.getLogger(ItemSpawningListener.class);
  private MythicDropsPlugin mythicDrops;

  public ItemSpawningListener(MythicDropsPlugin mythicDrops) {
    LOGGER.info("Created instance of ItemSpawningListener");
    this.mythicDrops = mythicDrops;
  }

  public MythicDrops getMythicDrops() {
    return mythicDrops;
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onCreatureSpawnEventLowest(CreatureSpawnEvent event) {
    if (!(event.getEntity() instanceof Monster) || event.isCancelled()) {
      return;
    }
    if (!mythicDrops.getConfigSettings().getEnabledWorlds().contains(event.getEntity().getWorld()
        .getName())) {
      LOGGER.info("cancelling item spawn because of multiworld support");
      return;
    }
    if (mythicDrops.getConfigSettings().isGiveAllMobsNames()) {
      nameMobs(event.getEntity());
    }
    if (mythicDrops.getConfigSettings().isBlankMobSpawnEnabled()) {
      event.getEntity().getEquipment().clear();
      if (event.getEntity() instanceof Skeleton && !mythicDrops.getConfigSettings()
          .isSkeletonsSpawnWithoutBows()) {
        event.getEntity().getEquipment().setItemInMainHand(new ItemStack(Material.BOW, 1));
      }
    }
    event.getEntity()
        .setCanPickupItems(mythicDrops.getConfigSettings().isMobsPickupEquipment());
  }

  private void nameMobs(LivingEntity livingEntity) {
    if (mythicDrops.getConfigSettings().isGiveMobsNames()) {
      String generalName = NameMap.getInstance().getRandom(NameType.GENERAL_MOB_NAME, "");
      String specificName = NameMap.getInstance().getRandom(NameType.SPECIFIC_MOB_NAME,
          "." + livingEntity.getType().name().toLowerCase());
      String name;
      if (specificName != null && !specificName.isEmpty()) {
        name = specificName;
      } else {
        name = generalName;
      }

      EntityNameEvent event = new EntityNameEvent(livingEntity, name);
      Bukkit.getPluginManager().callEvent(event);
      if (event.isCancelled()) {
        return;
      }

      livingEntity.setCustomName(event.getName());
      livingEntity.setCustomNameVisible(true);
    }
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onCreatureSpawnEvent(CreatureSpawnEvent event) {
    if (!(event.getEntity() instanceof Monster) || event.isCancelled()) {
      return;
    }
    if (!mythicDrops.getConfigSettings().getEnabledWorlds().contains(event.getEntity().getWorld().getName())) {
      LOGGER.info("cancelling item spawn because of multiworld support");
      return;
    }
    if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.REINFORCEMENTS
        && mythicDrops.getCreatureSpawningSettings().isPreventReinforcements()) {
      return;
    }
    if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER
        && mythicDrops.getCreatureSpawningSettings().isPreventSpawner()) {
      return;
    }
    if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG
        && mythicDrops.getCreatureSpawningSettings().isPreventSpawnEgg()) {
      return;
    }
    if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM &&
        mythicDrops.getCreatureSpawningSettings().isPreventSpawner()) {
      return;
    }
    if (mythicDrops.getCreatureSpawningSettings()
        .getSpawnHeightLimit(event.getEntity().getWorld().getName()) <= event
        .getEntity().getLocation().getY()) {
      return;
    }
    if (!mythicDrops.getConfigSettings().isDisplayMobEquipment()) {
      LOGGER.info("display mob equipment is off");
      return;
    }

    // Start off with the random item chance. If the mob doesn't pass that, it gets no items.
    double chanceToGetDrop = mythicDrops.getConfigSettings().getItemChance() * mythicDrops
        .getCreatureSpawningSettings().getEntityTypeChanceToSpawn(event.getEntity().getType());
    if (RandomUtils.nextDouble(0D, 1D) >= chanceToGetDrop) {
      LOGGER.info("double is <= chanceToGetDrop: " + chanceToGetDrop);
      return;
    }

    // Choose a tier for the item that the mob is given. If the tier is null, it gets no items.
    Tier tier = getTierForEntity(event.getEntity());
    if (tier == null) {
      LOGGER.info("tier is null for type: " + event.getEntity().getType());
      return;
    }

    // Create the item for the mob.
    ItemStack itemStack = MythicDropsPlugin.getNewDropBuilder().withItemGenerationReason(
        ItemGenerationReason.MONSTER_SPAWN).useDurability(false).withTier(tier).build();

    if (itemStack == null) {
      return;
    }

    // Begin to check for socket gem, identity tome, and unidentified.
    double customItemChance = mythicDrops.getConfigSettings().getCustomItemChance();
    double socketGemChance = mythicDrops.getConfigSettings().getSocketGemChance();
    double unidentifiedItemChance = mythicDrops.getConfigSettings().getUnidentifiedItemChance();
    double identityTomeChance = mythicDrops.getConfigSettings().getIdentityTomeChance();
    boolean sockettingEnabled = mythicDrops.getConfigSettings().isSockettingEnabled();
    boolean identifyingEnabled = mythicDrops.getConfigSettings().isIdentifyingEnabled();

    if (RandomUtils.nextDouble(0D, 1D) <= customItemChance) {
      CustomItem customItem = CustomItemMap.getInstance().getRandomWithChance();
      if (customItem != null) {
        itemStack = customItem.toItemStack();
      }
    } else if (sockettingEnabled && RandomUtils.nextDouble(0D, 1D) <= socketGemChance) {
      SocketGem socketGem = SocketGemUtil.getRandomSocketGemWithChance();
      Material material = SocketGemUtil.getRandomSocketGemMaterial();
      if (socketGem != null && material != null) {
        itemStack = new SocketItem(material, socketGem);
      }
    } else if (identifyingEnabled && RandomUtils.nextDouble(0D, 1D) <= unidentifiedItemChance) {
      Material material = itemStack.getType();
      itemStack = new UnidentifiedItem(material);
    } else if (identifyingEnabled && RandomUtils.nextDouble(0D, 1D) <= identityTomeChance) {
      itemStack = new IdentityTome();
    }

    EntitySpawningEvent ese = new EntitySpawningEvent(event.getEntity());
    Bukkit.getPluginManager().callEvent(ese);

    EntityUtil.equipEntity(event.getEntity(), itemStack);

    while (RandomUtils.nextDouble(0D, 1D) <= mythicDrops.getConfigSettings().getChainItemChance()) {
      itemStack = MythicDropsPlugin.getNewDropBuilder().withItemGenerationReason(ItemGenerationReason.MONSTER_SPAWN)
          .useDurability(false).withTier(tier).build();
      EntityUtil.equipEntity(event.getEntity(), itemStack);
    }

    nameMobs(event.getEntity());
  }

  private Tier getTierForEntity(Entity entity) {
    Collection<Tier> allowableTiers = mythicDrops.getCreatureSpawningSettings()
        .getEntityTypeTiers(entity.getType());
    Map<Tier, Double> chanceMap = new HashMap<>();
    int distFromSpawn = (int) entity.getLocation().distanceSquared(entity.getWorld().getSpawnLocation());
    for (Tier t : allowableTiers) {
      if (t.getMaximumDistance() == -1 || t.getOptimalDistance() == -1) {
        chanceMap.put(t, t.getSpawnChance());
        continue;
      }
      double weightMultiplier;
      int squareMaxDist = (int) Math.pow(t.getMaximumDistance(), 2);
      int squareOptDist = (int) Math.pow(t.getOptimalDistance(), 2);
      int difference = distFromSpawn - squareOptDist;
      if (difference < squareMaxDist) {
        weightMultiplier = 1D - ((difference * 1D) / squareMaxDist);
      } else {
        weightMultiplier = 0D;
      }
      double weight = t.getSpawnChance() * weightMultiplier;
      chanceMap.put(t, weight);
    }
    return TierUtil.randomTierWithChance(chanceMap);
  }

  @EventHandler
  public void onEntityDeath(EntityDeathEvent event) {
    if (event.getEntity() instanceof Player || event.getEntity().getLastDamageCause() == null
        || event.getEntity().getLastDamageCause().isCancelled()) {
      return;
    }
    if (!mythicDrops.getConfigSettings().getEnabledWorlds().contains(event.getEntity().getWorld()
        .getName())) {
      return;
    }

    if (event.getEntity().getKiller() == null) {
      return;
    }

    if (mythicDrops.getConfigSettings().isDisplayMobEquipment()) {
      handleEntityDyingWithGive(event);
    } else {
      handleEntityDyingWithoutGive(event);
    }
  }

  private void handleEntityDyingWithoutGive(EntityDeathEvent event) {
    // Start off with the random item chance. If the mob doesn't pass that, it gets no items.
    double chanceToGetDrop = mythicDrops.getConfigSettings().getItemChance() * mythicDrops
        .getCreatureSpawningSettings().getEntityTypeChanceToSpawn(event.getEntity().getType());
    if (RandomUtils.nextDouble(0D, 1D) > chanceToGetDrop) {
      return;
    }

    // Choose a tier for the item that the mob is given. If the tier is null, it gets no items.
    Tier tier = getTierForEntity(event.getEntity());
    if (tier == null) {
      return;
    }

    // Create the item for the mob.
    ItemStack itemStack = MythicDropsPlugin.getNewDropBuilder().withItemGenerationReason(
        ItemGenerationReason.MONSTER_SPAWN).useDurability(true).withTier(tier).build();

    // Begin to check for socket gem, identity tome, and unidentified.
    double customItemChance = mythicDrops.getConfigSettings().getCustomItemChance();
    double socketGemChance = mythicDrops.getConfigSettings().getSocketGemChance();
    double unidentifiedItemChance = mythicDrops.getConfigSettings().getUnidentifiedItemChance();
    double identityTomeChance = mythicDrops.getConfigSettings().getIdentityTomeChance();
    boolean sockettingEnabled = mythicDrops.getConfigSettings().isSockettingEnabled();
    boolean identifyingEnabled = mythicDrops.getConfigSettings().isIdentifyingEnabled();

    if (RandomUtils.nextDouble(0D, 1D) <= customItemChance) {
      CustomItem ci = CustomItemMap.getInstance().getRandomWithChance();
      if (ci != null) {
        itemStack = ci.toItemStack();
        if (ci.isBroadcastOnFind()) {
          broadcastMessage(event.getEntity().getKiller(), itemStack);
        }
      }
    } else if (sockettingEnabled && RandomUtils.nextDouble(0D, 1D) <= socketGemChance) {
      SocketGem socketGem = SocketGemUtil.getRandomSocketGemWithChance();
      Material material = SocketGemUtil.getRandomSocketGemMaterial();
      if (socketGem != null && material != null) {
        itemStack = new SocketItem(material, socketGem);
      }
    } else if (identifyingEnabled && RandomUtils.nextDouble(0D, 1D) <= unidentifiedItemChance) {
      Material material = itemStack.getType();
      itemStack = new UnidentifiedItem(material);
    } else if (identifyingEnabled && RandomUtils.nextDouble(0D, 1D) <= identityTomeChance) {
      itemStack = new IdentityTome();
    } else if (tier.isBroadcastOnFind()) {
      broadcastMessage(event.getEntity().getKiller(), itemStack);
    }

    event.getEntity().getEquipment().setBootsDropChance(0.0F);
    event.getEntity().getEquipment().setLeggingsDropChance(0.0F);
    event.getEntity().getEquipment().setChestplateDropChance(0.0F);
    event.getEntity().getEquipment().setHelmetDropChance(0.0F);
    event.getEntity().getEquipment().setItemInMainHandDropChance(0.0F);

    World w = event.getEntity().getWorld();
    Location l = event.getEntity().getLocation();
    w.dropItemNaturally(l, itemStack);
  }

  private void handleEntityDyingWithGive(EntityDeathEvent event) {
    List<ItemStack> newDrops = new ArrayList<>();

    ItemStack[] array = new ItemStack[6];
    System.arraycopy(event.getEntity().getEquipment().getArmorContents(), 0, array, 0, 4);
    array[4] = event.getEntity().getEquipment().getItemInMainHand();
    array[5] = event.getEntity().getEquipment().getItemInOffHand();

    event.getEntity().getEquipment().setBootsDropChance(0.0F);
    event.getEntity().getEquipment().setLeggingsDropChance(0.0F);
    event.getEntity().getEquipment().setChestplateDropChance(0.0F);
    event.getEntity().getEquipment().setHelmetDropChance(0.0F);
    event.getEntity().getEquipment().setItemInMainHandDropChance(0.0F);
    event.getEntity().getEquipment().setItemInOffHandDropChance(0.0F);

    for (ItemStack is : array) {
      if (is == null || is.getType() == Material.AIR || !is.hasItemMeta()) {
        continue;
      }
      CustomItem ci = CustomItemUtil.getCustomItemFromItemStack(is);
      if (ci != null) {
        newDrops.add(ci.toItemStack());
        if (ci.isBroadcastOnFind() && event.getEntity().getKiller() != null) {
          broadcastMessage(event.getEntity().getKiller(), ci.toItemStack());
        }
        continue;
      }
      SocketGem socketGem = SocketGemUtil.getSocketGemFromItemStack(is);
      if (socketGem != null) {
        newDrops.add(new SocketItem(is.getType(), socketGem));
        continue;
      }
      IdentityTome identityTome = new IdentityTome();
      if (is.isSimilar(identityTome)) {
        newDrops.add(identityTome);
        continue;
      }
      UnidentifiedItem unidentifiedItem = new UnidentifiedItem(is.getType());
      if (is.isSimilar(unidentifiedItem)) {
        newDrops.add(unidentifiedItem);
        continue;
      }
      Tier t = TierUtil.getTierFromItemStack(is);
      if (t != null && RandomUtils.nextDouble(0D, 1D) < t.getDropChance()) {
        ItemStack nis = is.getData().toItemStack(1);
        nis.setItemMeta(is.getItemMeta());
        nis.setDurability(ItemStackUtil.getDurabilityForMaterial(is.getType(),
            t.getMinimumDurabilityPercentage(),
            t.getMaximumDurabilityPercentage()));
        if (t.isBroadcastOnFind()) {
          if (event.getEntity().getKiller() != null) {
            broadcastMessage(event.getEntity().getKiller(), nis);
          }
        }
        newDrops.add(nis);
      }
    }

    for (ItemStack itemStack : newDrops) {
      if (itemStack.getType() == Material.AIR) {
        continue;
      }
      World w = event.getEntity().getWorld();
      Location l = event.getEntity().getLocation();
      w.dropItemNaturally(l, itemStack);
    }
  }

  private void broadcastMessage(Player player, ItemStack itemStack) {
    String locale = mythicDrops.getConfigSettings().getFormattedLanguageString("command.found-item-broadcast",
        new String[][]{{"%receiver%",
            player.getName()}});
    String[] messages = locale.split("%item%");
    FancyMessage fancyMessage = new FancyMessage("");
    for (int i1 = 0; i1 < messages.length; i1++) {
      String key = messages[i1];
      if (i1 < messages.length - 1) {
        fancyMessage.then(key).then(itemStack.getItemMeta().getDisplayName()).itemTooltip(itemStack);
      } else {
        fancyMessage.then(key);
      }
    }
    for (Player p : player.getWorld().getPlayers()) {
      fancyMessage.send(p);
    }
  }

}
