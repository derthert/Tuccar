<h1 align="center">🏪 Tüccar</h1>

<p align="center">
  <strong>Minecraft Oyuncu Marketi Eklentisi</strong>
</p>

<p align="center">
  <a href="https://bstats.org/plugin/bukkit/Tuccar/10085">
    <img src="https://img.shields.io/badge/bStats-Live%20Usage-blue?style=for-the-badge" alt="bStats"/>
  </a>
  <img src="https://img.shields.io/badge/Version-1.3.0-green?style=for-the-badge" alt="Version"/>
  <img src="https://img.shields.io/badge/API-1.20+-orange?style=for-the-badge" alt="API Version"/>
  <img src="https://img.shields.io/badge/Java-21-red?style=for-the-badge" alt="Java"/>
  <img src="https://img.shields.io/badge/License-Apache%202.0-purple?style=for-the-badge" alt="License"/>
</p>

<p align="center">
  <a href="#-özellikler">Özellikler</a> •
  <a href="#-kurulum">Kurulum</a> •
  <a href="#-komutlar">Komutlar</a> •
  <a href="#%EF%B8%8F-yapılandırma">Yapılandırma</a> •
  <a href="#-api">API</a> •
  <a href="#-bağımlılıklar">Bağımlılıklar</a>
</p>

---

> 💡 **İpucu:** 1.13 ve üzeri sürümlerde kullanıyorsanız, `.jar` dosyasını winrar ile açıp `1.16-config.yml` dosyasını mevcut `config.yml` dosyasıyla değiştirin!

---

## 📖 Hakkında

**Tüccar**, Minecraft sunucuları için geliştirilmiş profesyonel bir oyuncu marketi eklentisidir. Oyuncuların kendi eşyalarını diğer oyunculara satmasına olanak tanıyan bu sistem, NPC tabanlı bir altyapı ile çalışır.

### Nasıl Çalışır?

1. 🎭 **NPC Entegrasyonu** - Görevli bir NPC aracılığıyla market sistemi çalışır
2. 📦 **Otomatik Kategorilendirme** - Eşyalar otomatik olarak kategorize edilir
3. 💰 **Akıllı Fiyatlandırma** - Aynı kategorideki ürünler fiyata göre listelenir
4. 📥 **Envanter Yönetimi** - Oyuncular offline olsa bile satışlar envanterde saklanır
5. 🔄 **Geri Çekme** - İstediğiniz zaman ürünlerinizi marketten geri çekebilirsiniz

---

## ✨ Özellikler

| Özellik | Açıklama |
|---------|----------|
| 🏪 **Oyuncu Marketi** | Oyuncular arası ticaret sistemi |
| 📂 **Kategori Sistemi** | Özelleştirilebilir ürün kategorileri |
| 💵 **Vergi Sistemi** | Yapılandırılabilir vergi oranları |
| 🌍 **Dünya Kısıtlaması** | Belirli dünyalarda çalışma desteği |
| 🗄️ **Veritabanı Desteği** | MySQL ve SQLite desteği |
| ⚡ **Redis Cache** | Yüksek performans için Redis önbellekleme |
| 🎨 **CustomItems Desteği** | Özel eşya eklentisi entegrasyonu |
| 📊 **bStats Entegrasyonu** | Kullanım istatistikleri |
| 🔧 **Tamamen Özelleştirilebilir** | Config ve dil dosyaları |

---

## 📥 Kurulum

### Gereksinimler

- ☕ **Java 21** veya üzeri
- 🎮 **Paper/Spigot 1.20.4+**
- 💰 **Vault** (Ekonomi sistemi için)
- 🎭 **Citizens** (NPC desteği için - opsiyonel)

### Kurulum Adımları

1. En son sürümü [Releases](https://github.com/Geik/Tuccar/releases) sayfasından indirin
2. `Tuccar.jar` dosyasını sunucunuzun `plugins` klasörüne koyun
3. Sunucuyu başlatın veya yeniden başlatın
4. `plugins/Tuccar/config.yml` dosyasını düzenleyin
5. `/tüccar reload` komutu ile yapılandırmayı yükleyin

---

## 💻 Komutlar

| Komut | Açıklama | Yetki |
|-------|----------|-------|
| `/tüccar` | Ana menüyü açar | - |
| `/tüccar ekle [fiyat]` | Eldeki ürünü satışa ekler | - |
| `/tüccar stokekle` | Mevcut ürüne stok ekler | - |
| `/tüccar ürünlerim` | Kendi ürünlerinizi görüntüler | - |
| `/tüccar setnpc` | Tüccar NPC'sini ayarlar | `tuccar.admin` |
| `/tüccar reload` | Yapılandırmayı yeniden yükler | `tuccar.admin` |

### Kısayollar

- **SHIFT + Sol Tık** - Toplu alım (varsayılan: 32 adet)
- **Orta Tık** - Fiyat güncelleme (aktifse)

---

## ⚙️ Yapılandırma

### config.yml

```yaml
# Veritabanı Ayarları
Database:
  type: MYSQL          # MYSQL veya SQLITE
  host: localhost
  port: 3306
  database: tuccar
  username: root
  password: ''

# Redis Cache (Opsiyonel)
Redis:
  enabled: false
  host: localhost
  port: 6379
  password: ''
  serverId: server-1

# Genel Ayarlar
Settings:
  customBuyAmount: 32        # SHIFT + Tık ile alınacak miktar
  minimumPrice: 1            # Minimum fiyat limiti
  openTuccarViaCmd: false    # Komut ile açma
  categorySize: 36           # Kategori menü boyutu
  middleClickRePrice: true   # Orta tık ile fiyat güncelleme

# Vergi Sistemi
Tax:
  taxRate: 0                 # Vergi oranı (% olarak)
  depositAccount: false      # Vergiyi bir hesaba yatır
  account: Admin             # Vergi hesabı
```

### Kategori Yapılandırması

```yaml
Tuccar:
  Blok:                      # Kategori ID
    slot: 12                 # Menüdeki slot
    displayName: '&6Bloklar' # Görünen ad
    material: GRASS          # Kategori ikonu
    displayLore:             # Açıklama
      - ''
      - ' &8▪ &7Blokları incele'
      - ''
    items:                   # Kategori ürünleri
      1:
        material: ICE
        displayName: '&eBuz'
      2:
        material: PACKED_ICE
        displayName: '&ePaketlenmiş Buz'
```

### Dil Dosyası (lang.yml)

Tüm mesajlar, başlıklar ve GUI metinleri `lang.yml` dosyasından özelleştirilebilir.

---

## 🔌 API

Tüccar, diğer eklentiler için kapsamlı bir API sunar.

### Eventler

```java
// Ürün satıldığında
@EventHandler
public void onProductSold(ProductSoldEvent event) {
    Player buyer = event.getBuyer();
    ItemStack item = event.getItem();
    double price = event.getPrice();
}

// Ürün eklendiğinde
@EventHandler
public void onProductRegister(ProductRegisterEvent event) {
    Player seller = event.getSeller();
    ItemStack item = event.getItem();
}

// Ürün kaldırıldığında
@EventHandler
public void onProductRemove(ProductRemoveEvent event) {
    Player owner = event.getOwner();
}
```

### Maven Dependency

```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>

<dependency>
    <groupId>com.github.Geik</groupId>
    <artifactId>Tuccar</artifactId>
    <version>1.3.0</version>
    <scope>provided</scope>
</dependency>
```

---

## 📦 Bağımlılıklar

### Zorunlu
| Bağımlılık | Açıklama |
|------------|----------|
| [Vault](https://github.com/MilkBowl/Vault) | Ekonomi sistemi entegrasyonu |

### Opsiyonel
| Bağımlılık | Açıklama |
|------------|----------|
| [Citizens](https://github.com/CitizensDev/Citizens2) | NPC desteği |
| [CustomItems](https://www.spigotmc.org/resources/customitems.63848/) | Özel eşya desteği |

### Dahili Kütüphaneler
- **HikariCP** - Veritabanı bağlantı havuzu
- **Jedis** - Redis istemcisi
- **org.json** - JSON işleme

---

## 🛠️ Derleme

Projeyi kaynak koddan derlemek için:

```bash
# Repoyu klonlayın
git clone https://github.com/Geik/Tuccar.git
cd Tuccar

# Maven ile derleyin
mvn clean package
```

Derlenen JAR dosyası `target/` klasöründe oluşturulacaktır.

---

## 📊 İstatistikler

[![bStats](https://bstats.org/signatures/bukkit/Tuccar.svg)](https://bstats.org/plugin/bukkit/Tuccar/10085)

---

## 🤝 Katkıda Bulunma

1. Bu repoyu fork edin
2. Feature branch oluşturun (`git checkout -b feature/YeniOzellik`)
3. Değişikliklerinizi commit edin (`git commit -m 'Yeni özellik eklendi'`)
4. Branch'inizi push edin (`git push origin feature/YeniOzellik`)
5. Pull Request açın

---

## 📄 Lisans

Bu proje **Apache License 2.0** altında lisanslanmıştır. Detaylar için [LICENSE](LICENSE) dosyasına bakın.

---

## 👨‍💻 Geliştirici

<table>
  <tr>
    <td align="center">
      <strong>Geik</strong><br/>
      <em>Ana Geliştirici</em>
    </td>
  </tr>
</table>

---

<p align="center">
  <strong>⭐ Bu projeyi beğendiyseniz yıldız vermeyi unutmayın!</strong>
</p>

<p align="center">
  Made with ❤️ for the Minecraft community
</p>
