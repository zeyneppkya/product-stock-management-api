package com.zeynep.productapi.service;

import com.zeynep.productapi.model.Product;
import com.zeynep.productapi.model.StockMovement;
import com.zeynep.productapi.repository.ProductRepository;
import com.zeynep.productapi.repository.StockMovementRepository;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Excel (.xlsx) raporlarini uretir.
 * Raporlar bellekte olusturulur ve byte dizisi olarak dondurulur;
 * dosya adi ve HTTP basliklari Controller'in isidir.
 */
@Log4j2
@Service
public class ExcelReportService {

    private static final String[] PRODUCT_HEADERS = {
            "ID", "Urun Adi", "Aciklama", "Kategori", "Tedarikci",
            "Fiyat (TL)", "Stok", "Az Stok", "Olusturulma", "Guncellenme"
    };

    private static final String[] MOVEMENT_HEADERS = {
            "ID", "Tarih", "Urun ID", "Urun Adi", "Hareket Tipi",
            "Miktar", "Hareket Sonrasi Stok", "Not"
    };

    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final int lowStockThreshold;

    public ExcelReportService(ProductRepository productRepository,
                              StockMovementRepository stockMovementRepository,
                              @Value("${app.low-stock-threshold}") int lowStockThreshold) {
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.lowStockThreshold = lowStockThreshold;
    }

    @Transactional(readOnly = true)
    public byte[] generateProductReport() {
        log.info("Urun Excel raporu olusturuluyor...");

        List<Product> products = productRepository.findAll(Sort.by("id"));

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Urunler");
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle priceStyle = createPriceStyle(workbook);

            writeHeaderRow(workbook, sheet, PRODUCT_HEADERS);

            int rowIndex = 1;
            for (Product product : products) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(product.getId());
                row.createCell(1).setCellValue(product.getName());
                row.createCell(2).setCellValue(textOrEmpty(product.getDescription()));
                row.createCell(3).setCellValue(product.getCategory().getName());
                row.createCell(4).setCellValue(product.getSupplier().getName());

                Cell priceCell = row.createCell(5);
                priceCell.setCellValue(product.getPrice().doubleValue());
                priceCell.setCellStyle(priceStyle);

                row.createCell(6).setCellValue(product.getStock());
                row.createCell(7).setCellValue(product.getStock() < lowStockThreshold ? "Evet" : "Hayir");
                writeDateCell(row, 8, product.getCreatedAt(), dateStyle);
                writeDateCell(row, 9, product.getUpdatedAt(), dateStyle);
            }

            autoSizeColumns(sheet, PRODUCT_HEADERS.length);

            byte[] content = toBytes(workbook);
            log.info("Urun Excel raporu olusturuldu. {} satir, {} byte.", products.size(), content.length);
            return content;

        } catch (IOException e) {
            log.error("Urun Excel raporu olusturulamadi.", e);
            throw new UncheckedIOException("Urun Excel raporu olusturulamadi.", e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] generateStockMovementReport() {
        log.info("Stok hareketleri Excel raporu olusturuluyor...");

        List<StockMovement> movements = stockMovementRepository.findAll(
                Sort.by(Sort.Order.desc("movementDate"), Sort.Order.desc("id")));

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Stok Hareketleri");
            CellStyle dateStyle = createDateStyle(workbook);

            writeHeaderRow(workbook, sheet, MOVEMENT_HEADERS);

            int rowIndex = 1;
            for (StockMovement movement : movements) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(movement.getId());
                writeDateCell(row, 1, movement.getMovementDate(), dateStyle);
                row.createCell(2).setCellValue(movement.getProduct().getId());
                row.createCell(3).setCellValue(movement.getProduct().getName());
                row.createCell(4).setCellValue(movement.getType().name());
                row.createCell(5).setCellValue(movement.getQuantity());
                row.createCell(6).setCellValue(movement.getStockAfter());
                row.createCell(7).setCellValue(textOrEmpty(movement.getNote()));
            }

            autoSizeColumns(sheet, MOVEMENT_HEADERS.length);

            byte[] content = toBytes(workbook);
            log.info("Stok hareketleri Excel raporu olusturuldu. {} satir, {} byte.",
                    movements.size(), content.length);
            return content;

        } catch (IOException e) {
            log.error("Stok hareketleri Excel raporu olusturulamadi.", e);
            throw new UncheckedIOException("Stok hareketleri Excel raporu olusturulamadi.", e);
        }
    }

    private void writeHeaderRow(Workbook workbook, Sheet sheet, String[] headers) {
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(boldFont);

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Asagi kaydirinca baslik satiri gorunur kalsin
        sheet.createFreezePane(0, 1);
    }

    private void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("dd.mm.yyyy hh:mm"));
        return style;
    }

    private CellStyle createPriceStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        return style;
    }

    /** Tarih null ise hucreyi bos birakir. */
    private void writeDateCell(Row row, int columnIndex, LocalDateTime value, CellStyle dateStyle) {
        Cell cell = row.createCell(columnIndex);
        if (value != null) {
            cell.setCellValue(value);
            cell.setCellStyle(dateStyle);
        }
    }

    private String textOrEmpty(String value) {
        return value != null ? value : "";
    }

    private byte[] toBytes(Workbook workbook) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        return out.toByteArray();
    }
}
