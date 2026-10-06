package com.zeynep.productapi.config;

import com.zeynep.productapi.model.Category;
import com.zeynep.productapi.model.MovementType;
import com.zeynep.productapi.model.Product;
import com.zeynep.productapi.model.StockMovement;
import com.zeynep.productapi.model.Supplier;
import com.zeynep.productapi.repository.CategoryRepository;
import com.zeynep.productapi.repository.ProductRepository;
import com.zeynep.productapi.repository.StockMovementRepository;
import com.zeynep.productapi.repository.SupplierRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Uygulama acilirken ornek veriyi yukler: kategori -> tedarikci -> urun -> stok hareketi.
 * Her adim yalnizca kendi tablosu bossa calisir; boylece yeniden baslatmalarda kayitlar cogalmaz.
 */
@Log4j2
@Component
public class DataLoader implements CommandLineRunner {

    private static final int MOVEMENT_COUNT = 40;

    private static final String[] IN_NOTES = {
            "Tedarikciden sevkiyat", "Stok yenileme", "Iade girisi", "Sayim duzeltmesi (fazla)"
    };

    private static final String[] OUT_NOTES = {
            "Musteri siparisi", "Toptan satis", "Magaza transferi", "Hasarli urun cikisi"
    };

    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    public DataLoader(CategoryRepository categoryRepository,
                      SupplierRepository supplierRepository,
                      ProductRepository productRepository,
                      StockMovementRepository stockMovementRepository) {
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @Override
    public void run(String... args) {
        loadCategories();
        loadSuppliers();
        loadProducts();
        loadStockMovements();
    }

    private void loadCategories() {
        long mevcutKayitSayisi = categoryRepository.count();

        if (mevcutKayitSayisi > 0) {
            log.info("Veritabaninda zaten {} kategori var. Kategori verisi yuklenmedi.", mevcutKayitSayisi);
            return;
        }

        List<Category> kategoriler = List.of(
                kategori("Elektronik", "Bilgisayar cevre birimleri ve elektronik cihazlar"),
                kategori("Kirtasiye", "Ofis ve okul icin kirtasiye malzemeleri"),
                kategori("Mobilya", "Ofis mobilyalari ve depolama urunleri"),
                kategori("Aksesuar", "Calisma alanini tamamlayan yardimci urunler"),
                kategori("Bilgisayar Bilesenleri", "Islemci, anakart, bellek gibi donanim parcalari"),
                kategori("Ag ve Kablolama", "Ag cihazlari, kablolar ve baglanti ekipmanlari")
        );

        categoryRepository.saveAll(kategoriler);
        log.info("Kategori verisi yuklendi. Toplam {} kategori eklendi.", kategoriler.size());
    }

    private void loadSuppliers() {
        long mevcutKayitSayisi = supplierRepository.count();

        if (mevcutKayitSayisi > 0) {
            log.info("Veritabaninda zaten {} tedarikci var. Tedarikci verisi yuklenmedi.", mevcutKayitSayisi);
            return;
        }

        List<Supplier> tedarikciler = List.of(
                tedarikci("Anadolu Teknoloji Dagitim A.S.", "Ahmet Yilmaz",
                        "satis@anadoluteknoloji.example", "0212 555 01 01",
                        "Maslak Mah. Buyukdere Cad. No:12 Sariyer/Istanbul"),
                tedarikci("Marmara Kirtasiye Ltd. Sti.", "Elif Demir",
                        "siparis@marmarakirtasiye.example", "0216 555 02 02",
                        "Kosuyolu Mah. Katip Salih Sok. No:8 Kadikoy/Istanbul"),
                tedarikci("Ege Ofis Mobilyalari San. Tic. A.S.", "Mehmet Kaya",
                        "info@egeofismobilya.example", "0232 555 03 03",
                        "Ataturk Organize Sanayi Bolgesi 10003 Sok. No:5 Cigli/Izmir"),
                tedarikci("Bogazici Bilisim Urunleri A.S.", "Zeynep Arslan",
                        "kurumsal@bogazicibilisim.example", "0212 555 04 04",
                        "Esentepe Mah. Kore Sehitleri Cad. No:20 Sisli/Istanbul"),
                tedarikci("Baskent Elektronik Pazarlama Ltd. Sti.", "Can Ozturk",
                        "satis@baskentelektronik.example", "0312 555 05 05",
                        "Ostim OSB 1151 Sok. No:14 Yenimahalle/Ankara"),
                tedarikci("Toros Ag Sistemleri Ltd. Sti.", "Burak Sahin",
                        "destek@torosag.example", "0322 555 06 06",
                        "Resatbey Mah. Ataturk Cad. No:33 Seyhan/Adana"),
                tedarikci("Karadeniz Aksesuar Dis Tic. Ltd. Sti.", "Ayse Celik",
                        "iletisim@karadenizaksesuar.example", "0462 555 07 07",
                        "Cumhuriyet Mah. Maras Cad. No:41 Ortahisar/Trabzon"),
                tedarikci("Trakya Bilgisayar Parcalari A.S.", "Emre Aydin",
                        "siparis@trakyabilgisayar.example", "0282 555 08 08",
                        "Hurriyet Mah. Sinan Dede Cad. No:7 Suleymanpasa/Tekirdag")
        );

        supplierRepository.saveAll(tedarikciler);
        log.info("Tedarikci verisi yuklendi. Toplam {} tedarikci eklendi.", tedarikciler.size());
    }

    private void loadProducts() {
        long mevcutKayitSayisi = productRepository.count();

        if (mevcutKayitSayisi > 0) {
            log.info("Veritabaninda zaten {} urun var. Urun verisi yuklenmedi.", mevcutKayitSayisi);
            return;
        }

        List<Supplier> tedarikciler = supplierRepository.findAll(Sort.by("id"));

        if (categoryRepository.count() == 0 || tedarikciler.isEmpty()) {
            log.warn("Kategori ya da tedarikci yok. Urun verisi yuklenemedi.");
            return;
        }

        Category elektronik = kategoriBul("Elektronik");
        Category kirtasiye = kategoriBul("Kirtasiye");
        Category mobilya = kategoriBul("Mobilya");
        Category aksesuar = kategoriBul("Aksesuar");
        Category bilesen = kategoriBul("Bilgisayar Bilesenleri");
        Category ag = kategoriBul("Ag ve Kablolama");

        // Tablo onceden doluysa tedarikci sayisi farkli olabilir; indeks tasmasin diye mod alinir
        Supplier anadolu = tedarikciler.get(0);
        Supplier marmara = tedarikciler.get(1 % tedarikciler.size());
        Supplier ege = tedarikciler.get(2 % tedarikciler.size());
        Supplier bogazici = tedarikciler.get(3 % tedarikciler.size());
        Supplier baskent = tedarikciler.get(4 % tedarikciler.size());
        Supplier toros = tedarikciler.get(5 % tedarikciler.size());
        Supplier karadeniz = tedarikciler.get(6 % tedarikciler.size());
        Supplier trakya = tedarikciler.get(7 % tedarikciler.size());

        List<Product> urunler = List.of(

                // ELEKTRONIK
                urun("Mekanik Klavye", "RGB isikli, mavi switch", elektronik, anadolu, "1450.00", 25),
                urun("Kablosuz Mouse", "2.4 GHz, sessiz tiklama", elektronik, anadolu, "380.50", 8),
                urun("27 inc Monitor", "2K cozunurluk, 144 Hz", elektronik, baskent, "7250.00", 4),
                urun("USB-C Hub", "7 portlu, HDMI ve kart okuyucu dahil", elektronik, anadolu, "890.00", 15),
                urun("Webcam 1080p", "Otomatik odaklama, dahili mikrofon", elektronik, baskent, "1120.00", 6),
                urun("Kablosuz Kulaklik", "Aktif gurultu engelleme, 30 saat pil", elektronik, baskent, "2450.00", 11),
                urun("Harici SSD 1TB", "Tasinabilir, USB 3.2", elektronik, anadolu, "2890.00", 9),
                urun("Bluetooth Hoparlor", "Suya dayanikli, 12 saat pil", elektronik, baskent, "1350.00", 20),
                urun("Tablet 10 inc", "128 GB, Wi-Fi", elektronik, anadolu, "8900.00", 3),
                urun("Powerbank 20000 mAh", "Hizli sarj destekli, cift cikis", elektronik, baskent, "780.00", 35),

                // KIRTASIYE
                urun("Defter A4", "80 yaprak, cizgili", kirtasiye, marmara, "45.90", 150),
                urun("Tukenmez Kalem Seti", "10'lu, mavi", kirtasiye, marmara, "89.00", 60),
                urun("Post-it Blok", "5 renk, 400 yaprak", kirtasiye, marmara, "32.75", 3),
                urun("Dosya Klasoru", "Genis omurgali, A4", kirtasiye, marmara, "58.00", 45),
                urun("Fosforlu Kalem Seti", "6 renk, kesik uclu", kirtasiye, marmara, "74.50", 22),
                urun("Zimba Makinesi", "25 sayfa kapasiteli, metal govde", kirtasiye, marmara, "165.00", 7),
                urun("A4 Fotokopi Kagidi", "80 gr, 500 yaprak", kirtasiye, marmara, "185.00", 200),
                urun("Makas", "21 cm, paslanmaz celik", kirtasiye, marmara, "42.00", 5),
                urun("Beyaz Tahta Kalemi", "4 renk, silinebilir", kirtasiye, marmara, "96.00", 40),
                urun("Hesap Makinesi", "12 haneli, gunes enerjili", kirtasiye, marmara, "320.00", 12),

                // MOBILYA
                urun("Ofis Sandalyesi", "Ergonomik, bel destekli", mobilya, ege, "4200.00", 12),
                urun("Yukseklik Ayarli Masa", "Elektrikli, 120x60 cm", mobilya, ege, "9800.00", 2),
                urun("Kitaplik", "5 rafli, ahsap", mobilya, ege, "2750.00", 5),
                urun("Cekmeceli Dolap", "3 cekmeceli, kilitli", mobilya, ege, "1980.00", 14),
                urun("Toplanti Masasi", "8 kisilik, 240x110 cm", mobilya, ege, "12500.00", 1),
                urun("Misafir Koltugu", "Kumas doseme, metal ayak", mobilya, ege, "3100.00", 8),
                urun("Dosya Dolabi", "4 cekmeceli, metal", mobilya, ege, "3650.00", 10),
                urun("Calisma Masasi", "140x70 cm, kablo kanalli", mobilya, ege, "5400.00", 16),
                urun("Ayak Destegi", "Egimi ayarlanabilir", mobilya, ege, "480.00", 28),
                urun("Portmanto", "Ayakli, 8 askili", mobilya, ege, "950.00", 6),

                // AKSESUAR
                urun("Laptop Standi", "Aluminyum, yuksekligi ayarlanabilir", aksesuar, karadeniz, "640.00", 18),
                urun("Masa Lambasi", "LED, 3 kademeli isik ayari", aksesuar, karadeniz, "520.00", 9),
                urun("Klavye Bilek Destegi", "Jel dolgulu, kaymaz taban", aksesuar, karadeniz, "245.00", 30),
                urun("Mouse Pad XL", "90x40 cm, dikisli kenar", aksesuar, karadeniz, "190.00", 55),
                urun("Monitor Kolu", "Gazli amortisorlu, tek ekran", aksesuar, anadolu, "1150.00", 7),
                urun("Laptop Cantasi", "15.6 inc, su itici kumas", aksesuar, karadeniz, "870.00", 24),
                urun("Kablo Duzenleyici", "10'lu klips seti", aksesuar, karadeniz, "85.00", 70),
                urun("Ekran Temizleme Seti", "Sprey ve mikrofiber bez", aksesuar, karadeniz, "120.00", 4),
                urun("Telefon Standi", "Katlanabilir, metal", aksesuar, karadeniz, "150.00", 32),
                urun("USB Masa Vantilatoru", "Sessiz, 2 kademeli", aksesuar, anadolu, "230.00", 13),

                // BILGISAYAR BILESENLERI
                urun("Islemci 8 Cekirdek", "16 izlek, 4.7 GHz", bilesen, trakya, "9800.00", 6),
                urun("Anakart ATX", "DDR5, Wi-Fi 6 dahili", bilesen, trakya, "6400.00", 9),
                urun("RAM 16GB DDR5", "5600 MHz, tek modul", bilesen, trakya, "2300.00", 40),
                urun("Ekran Karti 12GB", "Cift fanli, 3 ekran cikisi", bilesen, bogazici, "24500.00", 2),
                urun("NVMe SSD 1TB", "M.2, 7000 MB/s okuma", bilesen, trakya, "3100.00", 26),
                urun("Guc Kaynagi 750W", "80+ Gold, tam moduler", bilesen, trakya, "3450.00", 11),
                urun("Bilgisayar Kasasi", "Mid tower, temperli cam", bilesen, bogazici, "2800.00", 8),
                urun("Islemci Sogutucu", "Kule tipi, 120 mm fan", bilesen, trakya, "1250.00", 17),
                urun("Termal Macun", "4 gr, yuksek iletkenlik", bilesen, trakya, "140.00", 65),
                urun("Kasa Fani 120mm", "PWM, sessiz", bilesen, bogazici, "210.00", 48),

                // AG VE KABLOLAMA
                urun("Cat6 Ethernet Kablosu 5m", "UTP, fabrikasyon uclu", ag, toros, "95.00", 120),
                urun("Wi-Fi 6 Router", "Cift bant, 4 anten", ag, toros, "3200.00", 7),
                urun("8 Port Switch", "Gigabit, yonetilemez", ag, toros, "1100.00", 14),
                urun("24 Port Switch", "Gigabit, yonetilebilir", ag, bogazici, "5600.00", 3),
                urun("Patch Panel 24 Port", "Cat6, 1U", ag, toros, "1450.00", 5),
                urun("RJ45 Konnektor", "100'lu paket, Cat6", ag, toros, "180.00", 80),
                urun("HDMI Kablo 2m", "4K destekli, altin uclu", ag, toros, "160.00", 90),
                urun("Fiber Optik Kablo 10m", "LC-LC, single mode", ag, bogazici, "420.00", 19),
                urun("Access Point", "Tavan tipi, PoE destekli", ag, toros, "2750.00", 4),
                urun("Kablo Test Cihazi", "RJ45 ve RJ11 destekli", ag, toros, "680.00", 10)
        );

        productRepository.saveAll(urunler);
        log.info("Urun verisi yuklendi. Toplam {} urun eklendi.", urunler.size());
    }

    // Hareketler dogrudan repository ile kaydedilir; urunlerin stogu DEGISMEZ.
    // Tutarlilik icin gecmis, urunun BUGUNKU stogundan geriye dogru kurulur:
    // en yeni hareketin "hareket sonrasi stok" degeri urunun mevcut stoguna esittir
    // ve geriye gidildikce stok hicbir zaman eksiye dusmez.
    private void loadStockMovements() {
        long mevcutKayitSayisi = stockMovementRepository.count();

        if (mevcutKayitSayisi > 0) {
            log.info("Veritabaninda zaten {} stok hareketi var. Hareket verisi yuklenmedi.", mevcutKayitSayisi);
            return;
        }

        List<Product> urunler = productRepository.findAll(Sort.by("id"));

        if (urunler.isEmpty()) {
            log.warn("Hic urun yok. Stok hareketi verisi yuklenemedi.");
            return;
        }

        // Her urun icin "o andaki stok". Baslangicta urunun bugunku stogu.
        Map<Long, Integer> bakiye = new HashMap<>();
        LocalDateTime simdi = LocalDateTime.now();

        List<StockMovement> hareketler = new ArrayList<>();
        List<LocalDateTime> tarihler = new ArrayList<>();

        // i = 0 en yeni hareket; i buyudukce gecmise gidilir
        for (int i = 0; i < MOVEMENT_COUNT; i++) {
            Product urun = urunler.get((i * 3) % urunler.size());
            int hareketSonrasiStok = bakiye.getOrDefault(urun.getId(), urun.getStock());

            MovementType tip = (i % 5 < 3) ? MovementType.OUT : MovementType.IN;
            int miktar = 1 + (i * 3) % 12;

            // Giris hareketi, kendisinden sonraki stoktan buyuk olamaz
            // (yoksa hareketten onceki stok eksi cikardi). Bu durumda cikisa cevir.
            if (tip == MovementType.IN && miktar > hareketSonrasiStok) {
                tip = MovementType.OUT;
            }

            String not = (tip == MovementType.IN)
                    ? IN_NOTES[i % IN_NOTES.length]
                    : OUT_NOTES[i % OUT_NOTES.length];

            hareketler.add(StockMovement.builder()
                    .product(urun)
                    .type(tip)
                    .quantity(miktar)
                    .note(not)
                    .stockAfter(hareketSonrasiStok)
                    .build());

            // Yaklasik 3 aya yayilmis, saatleri farkli tarihler
            tarihler.add(simdi.minusDays(1 + i * 2L).minusHours(i % 7).minusMinutes((i * 13) % 60));

            // Bir adim geriye git: hareketten ONCEKI stogu hesapla
            int hareketOncesiStok = (tip == MovementType.IN)
                    ? hareketSonrasiStok - miktar
                    : hareketSonrasiStok + miktar;
            bakiye.put(urun.getId(), hareketOncesiStok);
        }

        // movementDate @CreationTimestamp oldugu icin kayit aninda "su an" yazilir.
        // Gecmis tarihleri verebilmek icin once kaydedip sonra tarihi sorguyla guncelliyoruz.
        List<StockMovement> kaydedilenler = stockMovementRepository.saveAll(hareketler);
        for (int i = 0; i < kaydedilenler.size(); i++) {
            stockMovementRepository.updateMovementDate(kaydedilenler.get(i).getId(), tarihler.get(i));
        }

        log.info("Stok hareketi verisi yuklendi. Toplam {} hareket eklendi.", kaydedilenler.size());
    }

    private Category kategori(String name, String description) {
        return Category.builder().name(name).description(description).build();
    }

    private Supplier tedarikci(String name, String contactName, String email, String phone, String address) {
        return Supplier.builder()
                .name(name)
                .contactName(contactName)
                .email(email)
                .phone(phone)
                .address(address)
                .build();
    }

    private Product urun(String name, String description, Category category, Supplier supplier,
                         String price, int stock) {
        return Product.builder()
                .name(name)
                .description(description)
                .category(category)
                .supplier(supplier)
                .price(new BigDecimal(price))
                .stock(stock)
                .build();
    }

    /**
     * Kategoriyi adina gore bulur.
     * Tablo onceden doluysa ve bu isim yoksa ilk kategoriye duser.
     */
    private Category kategoriBul(String name) {
        return categoryRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> categoryRepository.findAll(Sort.by("id")).get(0));
    }
}
