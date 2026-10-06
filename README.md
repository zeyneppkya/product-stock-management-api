# Ürün ve Stok Yönetim Sistemi

Ürün, kategori, tedarikçi ve stok hareketi işlemlerini yöneten bir stok takip sistemidir.
Proje Spring Boot ile REST API olarak geliştirilmiş, verileri PostgreSQL veritabanında saklamaktadır.

## Özellikler

* Ürün yönetimi
* Kategori yönetimi
* Tedarikçi yönetimi
* Stok giriş ve çıkış hareketleri
* Stok hareketlerinde otomatik stok güncelleme
* Yetersiz stokta çıkış engelleme
* Az stoklu ürünleri listeleme
* Gelişmiş ürün arama ve filtreleme
* Sayfalama ve sıralama desteği
* Excel rapor alma
* Swagger / OpenAPI desteği
* Log4j2 ile konsol ve dosya loglama
* Merkezi hata yönetimi
* JUnit & Mockito ile servis katmanı testleri

## Kullanılan Teknolojiler

* Java 21
* Spring Boot
* Spring Web MVC
* Spring Data JPA
* Hibernate
* PostgreSQL
* Bean Validation
* Lombok
* Log4j2
* Swagger / OpenAPI
* Apache POI
* JUnit
* Mockito
* Maven

## Katmanlı Mimari

Projede katmanlı mimari kullanılmıştır.

* **Controller:** HTTP isteklerini karşılar. İş mantığı içermez.
* **Service:** İş kuralları ve transaction yönetimi bu katmanda yapılır.
* **Repository:** Veritabanı işlemleri Spring Data JPA ile yapılır.
* **DTO:** Request ve response verileri için DTO yapısı kullanılır.
* **Mapper:** Entity ve DTO dönüşümleri mapper sınıfları ile yapılır.
* **Exception Handler:** Hatalar `GlobalExceptionHandler` ile merkezi olarak yönetilir.

Bu yapı sayesinde kodlar daha okunabilir, test edilebilir ve geliştirilebilir hale getirilmiştir.

## Modüller

### Ürün Yönetimi

Ürün ekleme, güncelleme, silme ve listeleme işlemleri yapılabilir.
Her ürün bir kategoriye ve bir tedarikçiye bağlıdır.

Ürün kayıtlarında şu bilgiler tutulur:

* Ürün adı
* Açıklama
* Kategori
* Tedarikçi
* Fiyat
* Stok adedi
* Oluşturulma ve güncellenme tarihi

Stoğu belirlenen eşiğin altına düşen ürünler ayrıca listelenebilir.
Eşik değeri `application.properties` üzerinden değiştirilebilir.

### Kategori ve Tedarikçi Yönetimi

Kategori ve tedarikçi kayıtları eklenebilir, güncellenebilir, silinebilir ve listelenebilir.
Aynı isimde ikinci bir kategori oluşturulamaz.
Kendisine bağlı ürünü olan kategori veya tedarikçi silinemez.

### Stok Hareketi

Ürünlere yapılan her stok girişi ve çıkışı kayıt altına alınır.

* Giriş hareketinde ürünün stoğu artar.
* Çıkış hareketinde ürünün stoğu azalır.
* Stok yetersizse çıkış işlemi yapılmaz ve hata döner.
* Hareket kaydı ile stok güncellemesi aynı transaction içinde gerçekleşir.
* Her hareket, işlem sonrası stok değerini de saklar.

Bu sayede ürünün stok geçmişi takip edilebilir.

### Raporlama

Ürün listesi ve stok hareketleri Excel dosyası olarak dışa aktarılabilir.
Excel işlemleri backend tarafında Apache POI ile gerçekleştirilir.

## API Uç Noktaları

**Ürünler**

```
GET    /api/products                      Ürünleri listeler
GET    /api/products/search               Gelişmiş arama
GET    /api/products/low-stock            Az stoklu ürünler
GET    /api/products/{id}                 Ürün detayı
GET    /api/products/{id}/stock-movements Ürünün stok geçmişi
POST   /api/products                      Ürün oluşturur
PUT    /api/products/{id}                 Ürün günceller
DELETE /api/products/{id}                 Ürün siler
```

**Kategoriler / Tedarikçiler**

```
GET    /api/categories        GET    /api/suppliers
GET    /api/categories/{id}   GET    /api/suppliers/{id}
POST   /api/categories        POST   /api/suppliers
PUT    /api/categories/{id}   PUT    /api/suppliers/{id}
DELETE /api/categories/{id}   DELETE /api/suppliers/{id}
```

**Stok Hareketleri ve Raporlar**

```
POST   /api/stock-movements                  Stok hareketi oluşturur
GET    /api/stock-movements                  Hareketleri listeler
GET    /api/reports/products/excel           Ürün raporu (.xlsx)
GET    /api/reports/stock-movements/excel    Hareket raporu (.xlsx)
```

Listeleme uç noktaları `page`, `size` ve `sort` parametrelerini kabul eder.

## Örnek Kullanım

Stok çıkışı:

```json
POST /api/stock-movements

{
  "productId": 1,
  "type": "OUT",
  "quantity": 5,
  "note": "Musteri siparisi"
}
```

Stok yetersizse dönen hata:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Yetersiz stok. Urun: Mekanik Klavye, mevcut stok: 3, istenen miktar: 10",
  "path": "/api/stock-movements"
}
```

## Kurulum

Gereksinimler: JDK 21 ve PostgreSQL. Maven kurmaya gerek yoktur, proje Maven Wrapper ile gelir.

1. Veritabanını oluşturun:

```sql
CREATE DATABASE product_db;
```

2. `src/main/resources/application.properties` dosyasındaki veritabanı kullanıcı adı ve şifresini kendi kurulumunuza göre düzenleyin.

3. Uygulamayı çalıştırın:

```bash
mvnw.cmd spring-boot:run     # Windows
./mvnw spring-boot:run       # Linux / macOS
```

Uygulama `http://localhost:8080` adresinde çalışır.
Tablolar ilk açılışta otomatik oluşturulur ve örnek veriler yüklenir.

## Swagger

API uç noktaları Swagger üzerinden incelenebilir ve test edilebilir.

```
http://localhost:8080/swagger-ui.html
```

## Testleri Çalıştırma

Servis katmanı için JUnit ve Mockito testleri hazırlanmıştır.

```bash
mvnw.cmd test     # Windows
./mvnw test       # Linux / macOS
```

Testlerde özellikle service katmanındaki iş kuralları kontrol edilmiştir.

Test kapsamına örnekler:

* Ürün işlemleri
* Kategori ve tedarikçi işlemleri
* Stok giriş ve çıkış hesaplamaları
* Yetersiz stok kontrolü

## Proje Durumu

Bu proje temel ürün yönetimi özelliklerinin yanında kategori ve tedarikçi yönetimi,
stok hareketi takibi, gelişmiş arama, sayfalama, Excel raporlama, Swagger dokümantasyonu
ve birim testleriyle geliştirilmiştir.

Katmanlı mimari prensiplerine uygun olarak geliştirildiği için yeni modüller eklemeye
ve mevcut modülleri geliştirmeye uygundur.