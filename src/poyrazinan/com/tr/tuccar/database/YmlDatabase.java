package poyrazinan.com.tr.tuccar.database;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import poyrazinan.com.tr.tuccar.Tuccar;
import poyrazinan.com.tr.tuccar.Utils.Storage.ItemExistCheck;
import poyrazinan.com.tr.tuccar.Utils.Storage.ProductStorage;
import poyrazinan.com.tr.tuccar.Utils.Storage.GuiUtils.ProductCounts;

/**
 * YML tabanlı basit database implementasyonu.
 * Test ortamları için tasarlanmıştır.
 */
public class YmlDatabase {
    
    private static File databaseFile;
    private static FileConfiguration database;
    private static AtomicInteger nextId = new AtomicInteger(1);
    
    public static void initialize() {
        databaseFile = new File(Tuccar.instance.getDataFolder(), "database.yml");
        if (!databaseFile.exists()) {
            try {
                databaseFile.createNewFile();
            } catch (IOException e) {
                Bukkit.getLogger().severe("[Tuccar] database.yml oluşturulamadı: " + e.getMessage());
            }
        }
        database = YamlConfiguration.loadConfiguration(databaseFile);
        
        // En yüksek ID'yi bul
        if (database.isConfigurationSection("products")) {
            Set<String> keys = database.getConfigurationSection("products").getKeys(false);
            int maxId = 0;
            for (String key : keys) {
                try {
                    int id = Integer.parseInt(key);
                    if (id > maxId) maxId = id;
                } catch (NumberFormatException ignored) {}
            }
            nextId.set(maxId + 1);
        }
        
        Bukkit.getLogger().info("[Tuccar] YML Database başlatıldı. Sonraki ID: " + nextId.get());
    }
    
    private static synchronized void save() {
        try {
            database.save(databaseFile);
        } catch (IOException e) {
            Bukkit.getLogger().severe("[Tuccar] database.yml kaydedilemedi: " + e.getMessage());
        }
    }
    
    public static synchronized void reload() {
        database = YamlConfiguration.loadConfiguration(databaseFile);
    }
    
    // ==================== CREATE ====================
    
    public static synchronized boolean registerProduct(String playerName, String category, String product, int stock, double price) {
        int id = nextId.getAndIncrement();
        String path = "products." + id;
        
        database.set(path + ".username", playerName);
        database.set(path + ".category", category);
        database.set(path + ".product", product);
        database.set(path + ".stock", stock);
        database.set(path + ".price", price);
        
        save();
        return true;
    }
    
    // ==================== READ ====================
    
    public static ProductCounts getProductInfos(String product, String category) {
        double minPrice = Double.MAX_VALUE;
        int sellerCount = 0;
        
        if (!database.isConfigurationSection("products")) {
            return new ProductCounts(0, 0);
        }
        
        for (String id : database.getConfigurationSection("products").getKeys(false)) {
            String path = "products." + id;
            if (database.getString(path + ".product", "").equals(product) 
                && database.getString(path + ".category", "").equals(category)) {
                sellerCount++;
                double price = database.getDouble(path + ".price", 0);
                if (price < minPrice) minPrice = price;
            }
        }
        
        if (minPrice == Double.MAX_VALUE) minPrice = 0;
        return new ProductCounts(minPrice, sellerCount);
    }
    
    public static double getMinimumPrice(String product, String category) {
        double minPrice = Double.MAX_VALUE;
        
        if (!database.isConfigurationSection("products")) {
            return 0;
        }
        
        for (String id : database.getConfigurationSection("products").getKeys(false)) {
            String path = "products." + id;
            if (database.getString(path + ".product", "").equals(product) 
                && database.getString(path + ".category", "").equals(category)) {
                double price = database.getDouble(path + ".price", 0);
                if (price < minPrice) minPrice = price;
            }
        }
        
        return minPrice == Double.MAX_VALUE ? 0 : minPrice;
    }
    
    public static List<ProductStorage> getAllListsOnProduct(String product) {
        List<ProductStorage> storage = new ArrayList<>();
        
        if (!database.isConfigurationSection("products")) {
            return storage;
        }
        
        for (String idStr : database.getConfigurationSection("products").getKeys(false)) {
            String path = "products." + idStr;
            if (!database.getString(path + ".product", "").equals(product)) continue;
            
            int id = Integer.parseInt(idStr);
            String seller = database.getString(path + ".username");
            String category = database.getString(path + ".category");
            int stock = database.getInt(path + ".stock");
            double price = database.getDouble(path + ".price");
            
            // Config'den ürün bilgilerini al
            String displayName = Tuccar.instance.getConfig().getString("Tuccar." + category + ".items." + product + ".displayName");
            String material = Tuccar.instance.getConfig().getString("Tuccar." + category + ".items." + product + ".material");
            String itemName = null;
            if (Tuccar.instance.getConfig().isSet("Tuccar." + category + ".items." + product + ".itemName")) 
                itemName = Tuccar.instance.getConfig().getString("Tuccar." + category + ".items." + product + ".itemName");
            List<String> displayLore = Tuccar.instance.getConfig().getStringList("Tuccar." + category + ".items." + product + ".displayLore");
            List<String> lore = Tuccar.instance.getConfig().getStringList("Tuccar." + category + ".items." + product + ".itemLore");
            int damage = 0;
            if (Tuccar.instance.getConfig().isSet("Tuccar." + category + ".items." + product + ".damage")) 
                damage = Tuccar.instance.getConfig().getInt("Tuccar." + category + ".items." + product + ".damage");
            List<String> enchantment = null;
            if (Tuccar.instance.getConfig().isSet("Tuccar." + category + ".items." + product + ".enchantment")) 
                enchantment = Tuccar.instance.getConfig().getStringList("Tuccar." + category + ".items." + product + ".enchantment");
            
            storage.add(new ProductStorage(product, material != null ? material.toUpperCase() : "STONE", category, itemName, displayName, lore, displayLore, damage, stock, price, seller, id, enchantment));
        }
        
        // Fiyata göre sırala
        storage.sort((a, b) -> Double.compare(a.getPrice(), b.getPrice()));
        return storage;
    }
    
    public static List<ProductStorage> getCategoryItemList(String product, String category) {
        List<ProductStorage> storage = new ArrayList<>();
        
        if (!database.isConfigurationSection("products")) {
            return storage;
        }
        
        for (String idStr : database.getConfigurationSection("products").getKeys(false)) {
            String path = "products." + idStr;
            if (!database.getString(path + ".product", "").equals(product) 
                || !database.getString(path + ".category", "").equals(category)) continue;
            
            int id = Integer.parseInt(idStr);
            String seller = database.getString(path + ".username");
            int stock = database.getInt(path + ".stock");
            double price = database.getDouble(path + ".price");
            
            String displayName = Tuccar.instance.getConfig().getString("Tuccar." + category + ".items." + product + ".displayName");
            String material = Tuccar.instance.getConfig().getString("Tuccar." + category + ".items." + product + ".material");
            String itemName = null;
            if (Tuccar.instance.getConfig().isSet("Tuccar." + category + ".items." + product + ".itemName")) 
                itemName = Tuccar.instance.getConfig().getString("Tuccar." + category + ".items." + product + ".itemName");
            List<String> displayLore = Tuccar.instance.getConfig().getStringList("Tuccar." + category + ".items." + product + ".displayLore");
            List<String> lore = Tuccar.instance.getConfig().getStringList("Tuccar." + category + ".items." + product + ".itemLore");
            int damage = 0;
            if (Tuccar.instance.getConfig().isSet("Tuccar." + category + ".items." + product + ".damage")) 
                damage = Tuccar.instance.getConfig().getInt("Tuccar." + category + ".items." + product + ".damage");
            List<String> enchantment = null;
            if (Tuccar.instance.getConfig().isSet("Tuccar." + category + ".items." + product + ".enchantment")) 
                enchantment = Tuccar.instance.getConfig().getStringList("Tuccar." + category + ".items." + product + ".enchantment");
            
            storage.add(new ProductStorage(product, material != null ? material.toUpperCase() : "STONE", category, itemName, displayName, lore, displayLore, damage, stock, price, seller, id, enchantment));
        }
        
        storage.sort((a, b) -> Double.compare(a.getPrice(), b.getPrice()));
        return storage;
    }
    
    public static boolean isProductHasSeller(String product, String category) {
        if (!database.isConfigurationSection("products")) {
            return false;
        }
        
        for (String id : database.getConfigurationSection("products").getKeys(false)) {
            String path = "products." + id;
            if (database.getString(path + ".product", "").equals(product) 
                && database.getString(path + ".category", "").equals(category)) {
                return true;
            }
        }
        return false;
    }
    
    public static List<ProductStorage> getPlayerProducts(String player) {
        List<ProductStorage> storage = new ArrayList<>();
        
        if (!database.isConfigurationSection("products")) {
            return storage;
        }
        
        for (String idStr : database.getConfigurationSection("products").getKeys(false)) {
            String path = "products." + idStr;
            if (!database.getString(path + ".username", "").equals(player)) continue;
            
            int id = Integer.parseInt(idStr);
            String category = database.getString(path + ".category");
            String product = database.getString(path + ".product");
            int stock = database.getInt(path + ".stock");
            double price = database.getDouble(path + ".price");
            
            if (!Tuccar.instance.getConfig().isSet("Tuccar." + category + ".items." + product)) continue;
            
            String displayName = Tuccar.instance.getConfig().getString("Tuccar." + category + ".items." + product + ".displayName");
            String material = Tuccar.instance.getConfig().getString("Tuccar." + category + ".items." + product + ".material");
            String itemName = null;
            if (Tuccar.instance.getConfig().isSet("Tuccar." + category + ".items." + product + ".itemName")) 
                itemName = Tuccar.instance.getConfig().getString("Tuccar." + category + ".items." + product + ".itemName");
            List<String> displayLore = Tuccar.instance.getConfig().getStringList("Tuccar." + category + ".items." + product + ".displayLore");
            List<String> lore = Tuccar.instance.getConfig().getStringList("Tuccar." + category + ".items." + product + ".itemLore");
            int damage = 0;
            if (Tuccar.instance.getConfig().isSet("Tuccar." + category + ".items." + product + ".damage")) 
                damage = Tuccar.instance.getConfig().getInt("Tuccar." + category + ".items." + product + ".damage");
            List<String> enchantment = null;
            if (Tuccar.instance.getConfig().isSet("Tuccar." + category + ".items." + product + ".enchantment")) 
                enchantment = Tuccar.instance.getConfig().getStringList("Tuccar." + category + ".items." + product + ".enchantment");
            
            storage.add(new ProductStorage(product, material != null ? material.toUpperCase() : "STONE", category, itemName, displayName, lore, displayLore, damage, stock, price, player, id, enchantment));
        }
        
        storage.sort((a, b) -> Double.compare(a.getPrice(), b.getPrice()));
        return storage;
    }
    
    public static boolean checkStock(int id, int amount) {
        String path = "products." + id;
        if (!database.isSet(path)) return false;
        return database.getInt(path + ".stock", 0) >= amount;
    }
    
    public static int getProductCount(int id) {
        return database.getInt("products." + id + ".stock", 0);
    }
    
    public static ItemExistCheck getPlayerItem(String product, String category, String player) {
        if (!database.isConfigurationSection("products")) {
            return null;
        }
        
        for (String idStr : database.getConfigurationSection("products").getKeys(false)) {
            String path = "products." + idStr;
            if (database.getString(path + ".product", "").equals(product) 
                && database.getString(path + ".category", "").equals(category)
                && database.getString(path + ".username", "").equals(player)) {
                
                int id = Integer.parseInt(idStr);
                int stock = database.getInt(path + ".stock");
                double price = database.getDouble(path + ".price");
                return new ItemExistCheck(id, player, stock, price);
            }
        }
        return null;
    }
    
    // ==================== UPDATE ====================
    
    public static synchronized void addProductCount(int id, int count) {
        String path = "products." + id;
        if (!database.isSet(path)) return;
        database.set(path + ".stock", count);
        save();
    }
    
    public static synchronized void setProductPrice(int id, double price) {
        String path = "products." + id;
        if (!database.isSet(path)) return;
        database.set(path + ".price", price);
        save();
    }
    
    // ==================== DELETE ====================
    
    public static synchronized void removeProductCount(int id, int count, String dataName, String category, double price) {
        String path = "products." + id;
        if (!database.isSet(path)) return;
        
        int currentStock = database.getInt(path + ".stock", 0);
        int newStock = currentStock - count;
        
        if (newStock < 1) {
            // Ürünü tamamen sil
            database.set(path, null);
            save();
            
            // Event ve cache güncelleme
            Bukkit.getScheduler().runTask(Tuccar.instance, () -> {
                poyrazinan.com.tr.tuccar.api.events.ProductRemoveEvent productRemove = 
                    new poyrazinan.com.tr.tuccar.api.events.ProductRemoveEvent(
                        poyrazinan.com.tr.tuccar.Utils.api.EventReason.SELL, dataName, category, price);
                Bukkit.getPluginManager().callEvent(productRemove);
            });
            
            poyrazinan.com.tr.tuccar.commands.AddProduct.RegisterManager.removeValues(dataName, category, price);
        } else {
            database.set(path + ".stock", newStock);
            save();
        }
    }
}
