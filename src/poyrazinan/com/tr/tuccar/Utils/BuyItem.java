package poyrazinan.com.tr.tuccar.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import com.jojodmo.customitems.api.CustomItemsAPI;

import poyrazinan.com.tr.tuccar.Tuccar;
import poyrazinan.com.tr.tuccar.Utils.Storage.ProductStorage;

@SuppressWarnings("deprecation")
public class BuyItem {
	

	public static ItemStack buyItem(ProductStorage product, int amount) {
		
		boolean isCustom = false;
		
		isCustom = Tuccar.instance.getConfig().isSet("Tuccar." + product.getItemCategory() + ".items." + product.getDataName() + ".customitem");
		
		if (isCustom)
		{
			
			String custom_data = Tuccar.instance.getConfig().getString("Tuccar." + product.getItemCategory() + ".items." + product.getDataName() + ".customitem");
			
			ItemStack customItem = CustomItemsAPI.getCustomItem(custom_data);
			if (customItem != null) {
				customItem.setAmount(amount);
				return customItem;
			}
			
		}
		
		// Material kontrolü
		Material material = Material.getMaterial(product.getItemMaterial().toUpperCase());
		if (material == null) {
			Bukkit.getConsoleSender().sendMessage(Tuccar.color("&4[Tuccar] &cGeçersiz material: " + product.getItemMaterial()));
			return new ItemStack(Material.STONE, amount); // Fallback
		}
		
		List<String> newList = new ArrayList<String>();
		if (product.getLore() != null) {
			for (String string : product.getLore()) {
	    		newList.add(string.replace("&", "§"));}
		}
    	ItemStack item = new ItemStack(material, amount);
    	String nms = Tuccar.getNMSVersion();
    	
    	// 1.13+ sürüm kontrolü (1.20, 1.21 dahil)
		if (nms.contains("1_21") || nms.contains("1_20") || nms.contains("1_19") || nms.contains("1_18") || 
			nms.contains("1_17") || nms.contains("1_16") || nms.contains("1_15") || nms.contains("1_14") || nms.contains("1_13")) {
			if ((material.equals(Material.SPLASH_POTION) || material.equals(Material.POTION) || material.equals(Material.LINGERING_POTION))
					&& (Tuccar.instance.getConfig().isSet("Tuccar." + product.getItemCategory() + ".items." + product.getDataName() + ".potionType")) ) {
				PotionType type = null;
				try {type = PotionType.valueOf(Tuccar.instance.getConfig().getString("Tuccar." + product.getItemCategory() + ".items." + product.getDataName() + ".potionType"));
				} catch (NullPointerException | IllegalArgumentException ignored) {}
				if (type != null) {
					Potion potion = null;
					if (product.getItemDamage() == 1 || product.getItemDamage() == 2)potion = new Potion(type, product.getItemDamage());
					else {
						potion = new Potion(type, 1);
						potion.extend();
					}
					if (material.equals(Material.SPLASH_POTION)) potion.setSplash(true);
					item = potion.toItemStack(amount);
					if (material.equals(Material.LINGERING_POTION)) item.setType(Material.LINGERING_POTION);
				}
			}
		}
    	
    	
		ItemMeta meta = item.getItemMeta();
    	if (product.getItemName() != null) meta.setDisplayName(product.getItemName().replace("&", "§"));
    	if (product.getLore() != null) meta.setLore(newList);
    	if (product.getItemDamage() != 0) item.setDurability((short) product.getItemDamage());
    	if (product.hasEnchant()) {
    		HashMap<Enchantment, Integer> enchs = product.getEnchants();
    		for (Enchantment ench : enchs.keySet()) {
    			try {
    				if (item.getType().equals(Material.ENCHANTED_BOOK)) {
    		    		EnchantmentStorageMeta AMeta = (EnchantmentStorageMeta) item.getItemMeta();
    					AMeta.addStoredEnchant(ench, enchs.get(ench), true);
    					if (product.getItemName() != null) AMeta.setDisplayName(Tuccar.color(product.getItemName()));
    					if (product.getLore() != null) meta.setLore(newList);
    					item.setItemMeta(AMeta);
    		    	} else meta.addEnchant(ench, enchs.get(ench), true);
    			} catch (NullPointerException ignored) {}
    		}
    	}
    	if (!item.getType().equals(Material.ENCHANTED_BOOK)) item.setItemMeta(meta);
    	
    	return item;
	}

}
