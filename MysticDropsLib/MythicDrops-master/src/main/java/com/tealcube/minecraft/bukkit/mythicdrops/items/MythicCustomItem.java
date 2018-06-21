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
package com.tealcube.minecraft.bukkit.mythicdrops.items;

import com.google.common.base.Preconditions;
import com.tealcube.minecraft.bukkit.mythicdrops.api.items.CustomItem;
import com.tealcube.minecraft.bukkit.mythicdrops.api.items.MythicItemStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public final class MythicCustomItem implements CustomItem {

  private final String name;
  private double chanceToBeGivenToAMonster;
  private double chanceToDropOnDeath;
  private String displayName;
  private Map<Enchantment, Integer> enchantments;
  private List<String> lore;
  private Material material;
  private boolean broadcastOnFind;
  private short durability;

  MythicCustomItem(String name) {
    this.name = name;
    enchantments = new HashMap<>();
    lore = new ArrayList<>();
  }

  @Override
  public double getChanceToBeGivenToAMonster() {
    return chanceToBeGivenToAMonster;
  }

  void setChanceToBeGivenToAMonster(double chanceToBeGivenToAMonster) {
    this.chanceToBeGivenToAMonster = chanceToBeGivenToAMonster;
  }

  @Override
  public double getChanceToDropOnDeath() {
    return chanceToDropOnDeath;
  }

  void setChanceToDropOnDeath(double chanceToDropOnDeath) {
    this.chanceToDropOnDeath = chanceToDropOnDeath;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }

  void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  @Override
  public Map<Enchantment, Integer> getEnchantments() {
    return enchantments;
  }

  void setEnchantments(Map<Enchantment, Integer> enchantments) {
    this.enchantments = enchantments;
  }

  @Override
  public List<String> getLore() {
    return lore;
  }

  void setLore(List<String> lore) {
    this.lore = lore;
  }

  @Override
  @Deprecated
  public MaterialData getMaterialData() {
    return new MaterialData(material);
  }

  @Deprecated
  public void setMaterialData(MaterialData materialData) {
    // do nothing
  }

  public Material getMaterial() {
    return this.material;
  }

  public void setMaterial(Material material) {
    this.material = material;
  }

  /**
   * Converts the CustomItem to an {@link org.bukkit.inventory.ItemStack}.
   *
   * @return CustomItem as an ItemStack
   */
  @Override
  public ItemStack toItemStack() {
    Preconditions.checkNotNull(material, "material cannot be null");
    return new MythicItemStack(material, 1, durability, displayName, lore,
        enchantments);
  }

  @Override
  public boolean isBroadcastOnFind() {
    return broadcastOnFind;
  }

  public void setBroadcastOnFind(boolean broadcastOnFind) {
    this.broadcastOnFind = broadcastOnFind;
  }

  @Override
  public short getDurability() {
    return 0;
  }

  public void setDurability(short durability) {
    this.durability = durability;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    MythicCustomItem that = (MythicCustomItem) o;

    if (Double.compare(that.chanceToBeGivenToAMonster, chanceToBeGivenToAMonster) != 0) {
      return false;
    }
    if (Double.compare(that.chanceToDropOnDeath, chanceToDropOnDeath) != 0) {
      return false;
    }
    if (broadcastOnFind != that.broadcastOnFind) {
      return false;
    }
    if (durability != that.durability) {
      return false;
    }
    if (name != null ? !name.equals(that.name) : that.name != null) {
      return false;
    }
    if (displayName != null ? !displayName.equals(that.displayName) : that.displayName != null) {
      return false;
    }
    if (enchantments != null ? !enchantments.equals(that.enchantments) : that.enchantments != null) {
      return false;
    }
    if (lore != null ? !lore.equals(that.lore) : that.lore != null) {
      return false;
    }
    return material == that.material;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    result = name != null ? name.hashCode() : 0;
    temp = Double.doubleToLongBits(chanceToBeGivenToAMonster);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(chanceToDropOnDeath);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
    result = 31 * result + (enchantments != null ? enchantments.hashCode() : 0);
    result = 31 * result + (lore != null ? lore.hashCode() : 0);
    result = 31 * result + (material != null ? material.hashCode() : 0);
    result = 31 * result + (broadcastOnFind ? 1 : 0);
    result = 31 * result + (int) durability;
    return result;
  }

}
