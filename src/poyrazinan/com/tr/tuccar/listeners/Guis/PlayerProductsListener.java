package poyrazinan.com.tr.tuccar.listeners.Guis;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import poyrazinan.com.tr.tuccar.Tuccar;
import poyrazinan.com.tr.tuccar.Utils.BuyItem;
import poyrazinan.com.tr.tuccar.Utils.getLang;
import poyrazinan.com.tr.tuccar.Utils.Storage.ProductStorage;
import poyrazinan.com.tr.tuccar.Utils.api.EventReason;
import poyrazinan.com.tr.tuccar.api.events.ProductRemoveEvent;
import poyrazinan.com.tr.tuccar.database.DatabaseQueries;
import poyrazinan.com.tr.tuccar.gui.PlayerProducts;

public class PlayerProductsListener implements Listener {

	private final Tuccar plugin;
	public static HashMap<String, ProductStorage> rePrice = new HashMap<>();

	public PlayerProductsListener(Tuccar plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player)) {
			return;
		}

		if (!e.getView().getTitle().contains(getLang.getText("selfProducts"))) {
			return;
		}

		e.setCancelled(true);

		Player player = (Player) e.getWhoClicked();

		if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) {
			return;
		}

		if (e.getClickedInventory() == null || e.getClickedInventory().getType() != InventoryType.CHEST) {
			return;
		}

		String[] sayfa = e.getView().getTitle().split(" ");
		List<ProductStorage> items = DatabaseQueries.getPlayerProducts(player.getName());

		if (!ItemSelectionListener.isNumeric(sayfa[sayfa.length-1])) {
			return;
		}

		if (items.isEmpty()) {
			return;
		}

		handleInventoryClick(e, player, items, sayfa);
	}

	private void handleInventoryClick(InventoryClickEvent e, Player player, List<ProductStorage> items, String[] sayfa) {
		int tiklama = calculateClickIndex(e.getSlot());
		if (tiklama == -1) {
			handleNavigationClick(e, player, sayfa);
			return;
		}

		int tiklamaCalculator = ((Integer.valueOf(sayfa[sayfa.length-1])*28)-28)+tiklama;
		if (tiklamaCalculator >= items.size()) {
			return;
		}

		ProductStorage selectedItem = items.get(tiklamaCalculator);
		Material itemMaterial = Material.getMaterial(selectedItem.getItemMaterial().toUpperCase());

		if (e.getClick() == ClickType.MIDDLE && plugin.getConfig().getBoolean("Settings.middleClickRePrice")) {
			handleRePricing(player, selectedItem);
			return;
		}

		int withdrawAmount = calculateBuyAmount(e.getClick(), selectedItem, player, itemMaterial);
		
		// Direkt olarak ürünü çek (onay ekranı olmadan)
		withdrawProduct(player, selectedItem, withdrawAmount, itemMaterial, sayfa);
	}

	/**
	 * Ürünü direkt olarak oyuncuya verir ve stoktan düşer
	 */
	private void withdrawProduct(Player player, ProductStorage item, int amount, Material itemMaterial, String[] sayfa) {
		// Stok kontrolü
		if (!DatabaseQueries.checkStock(item.getID(), amount)) {
			player.sendMessage(Tuccar.color(getLang.getText("Messages.couldntFindStock")));
			return;
		}

		// Envanter boşluk kontrolü
		int emptySlots = ConfirmationGuiListener.getEmptySlotsAmount(player);
		int maxStackSize = itemMaterial.getMaxStackSize();
		int maxCanHold = emptySlots * maxStackSize;

		if (maxCanHold < amount) {
			player.sendTitle(getLang.getText("Titles.notEnoughSpace.title"), 
					getLang.getText("Titles.notEnoughSpace.subTitle"), 20, 40, 20);
			return;
		}

		// Ürünü veritabanından düş
		DatabaseQueries.removeProductCount(item.getID(), amount, item.getDataName(), item.getItemCategory(), item.getPrice());

		// Event tetikle
		Bukkit.getScheduler().runTask(Tuccar.instance, () -> {
			ProductRemoveEvent productRemove = new ProductRemoveEvent(EventReason.CANCEL, item.getDataName(), item.getItemCategory(), item.getPrice());
			Bukkit.getPluginManager().callEvent(productRemove);
		});

		// Ürünü oyuncuya ver
		addItemToPlayer(amount, BuyItem.buyItem(item, maxStackSize), player);

		// Başarı mesajı
		player.sendTitle(getLang.getText("Titles.productWithdrawn.title"), 
				getLang.getText("Titles.productWithdrawn.subTitle"), 20, 40, 20);

		// GUI'yi yenile (stok bittiyse veya güncellendiyse görmesi için)
		Bukkit.getScheduler().runTaskLater(Tuccar.instance, () -> {
			int currentPage = Integer.valueOf(sayfa[sayfa.length-1]);
			PlayerProducts.createGui(player, currentPage);
		}, 5L);
	}

	/**
	 * Ürünü oyuncunun envanterine ekler
	 */
	private void addItemToPlayer(int amount, ItemStack item, Player player) {
		Bukkit.getScheduler().runTask(Tuccar.instance, () -> {
			int remaining = amount;
			int maxAmount = item.getMaxStackSize();

			while (remaining > 0) {
				int toGive = Math.min(remaining, maxAmount);
				ItemStack toAdd = item.clone();
				toAdd.setAmount(toGive);
				player.getInventory().addItem(toAdd);
				remaining -= toGive;
			}
		});
	}

	private int calculateClickIndex(int slot) {
		if (slot >= 10 && slot <= 16) return slot - 10;
		if (slot >= 19 && slot <= 25) return slot - 12;
		if (slot >= 28 && slot <= 34) return slot - 14;
		if (slot >= 37 && slot <= 43) return slot - 16;
		return -1;
	}

	private void handleNavigationClick(InventoryClickEvent e, Player player, String[] sayfa) {
		int currentPage = Integer.valueOf(sayfa[sayfa.length-1]);
		if (e.getSlot() == 48 && e.getCurrentItem().getType() == Material.getMaterial(getLang.getText("Gui.previousPage.material"))) {
			PlayerProducts.createGui(player, currentPage - 1);
		} else if (e.getSlot() == 50 && e.getCurrentItem().getType() == Material.getMaterial(getLang.getText("Gui.nextPage.material"))) {
			PlayerProducts.createGui(player, currentPage + 1);
		}
	}

	private int calculateBuyAmount(ClickType clickType, ProductStorage item, Player player, Material itemStack) {
		switch (clickType) {
			case LEFT:
				return 1;
			case SHIFT_LEFT:
				int customAmount = plugin.getConfig().getInt("Settings.customBuyAmount", 32);
				return Math.min(customAmount, item.getStock());
			case RIGHT:
				return Math.min(item.getStock(), itemStack.getMaxStackSize());
			case SHIFT_RIGHT:
				int slots = ConfirmationGuiListener.getEmptySlotsAmount(player);
				int maxStackSize = itemStack.getMaxStackSize();
				int playerMaxSize = slots * maxStackSize;
				return Math.min(playerMaxSize, item.getStock());
			default:
				return 1;
		}
	}

	private void handleRePricing(Player player, ProductStorage selectedItem) {
		if (!rePrice.containsKey(player.getName())) {
			rePrice.put(player.getName(), selectedItem);
			player.closeInventory();
			player.sendMessage(getLang.getText("Messages.rePrice"));

			Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
				rePrice.remove(player.getName());
			}, 200L);
		}
	}
}