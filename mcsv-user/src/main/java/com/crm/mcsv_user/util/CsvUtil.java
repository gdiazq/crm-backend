package com.crm.mcsv_user.util;

import com.crm.mcsv_user.dto.BulkImportResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class CsvUtil {

    private static final Logger log = LoggerFactory.getLogger(CsvUtil.class);

    private CsvUtil() {}

    public static <T> byte[] build(String header, List<T> rows, Function<T, String> rowMapper) {
        if (rows == null) return header.concat("\n").getBytes(StandardCharsets.UTF_8);
        StringBuilder csv = new StringBuilder();
        csv.append(header).append("\n");
        rows.forEach(row -> csv.append(rowMapper.apply(row)).append("\n"));
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static String[] parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString());
        return fields.toArray(new String[0]);
    }

    public static Map<String, Integer> headerIndex(String[] headers) {
        Map<String, Integer> idx = new HashMap<>();
        for (int i = 0; i < headers.length; i++)
            idx.put(headers[i].trim().toLowerCase(), i);
        return idx;
    }

    public static String col(String[] cols, int index) {
        if (index < 0) return "";
        if (index >= cols.length)
            throw new IllegalArgumentException("Columna " + index + " fuera de rango (fila con " + cols.length + " columnas)");
        return cols[index].trim();
    }

    @FunctionalInterface
    public interface NameDescProcessor {
        void process(String name, String description) throws Exception;
    }

    public static BulkImportResult importNameDesc(MultipartFile file, NameDescProcessor processor) {
        List<BulkImportResult.RowError> errors = new ArrayList<>();
        int total = 0, success = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null)
                return BulkImportResult.builder().total(0).success(0).failed(0).errors(errors).build();

            Map<String, Integer> idx = headerIndex(parseLine(headerLine));
            int iName = idx.getOrDefault("nombre", -1);
            int iDesc = idx.getOrDefault("descripción", idx.getOrDefault("descripcion", -1));

            if (iName < 0) {
                errors.add(new BulkImportResult.RowError(1, "No se encontró la columna 'nombre' en el header"));
                return BulkImportResult.builder().total(0).success(0).failed(1).errors(errors).build();
            }

            String line;
            int row = 1;
            while ((line = reader.readLine()) != null) {
                row++;
                if (line.isBlank()) continue;
                total++;
                try {
                    String[] cols = parseLine(line);
                    String name = col(cols, iName);
                    if (name.isEmpty()) throw new IllegalArgumentException("El nombre es obligatorio");
                    String description = col(cols, iDesc);
                    processor.process(name, description.isEmpty() ? null : description);
                    success++;
                } catch (Exception e) {
                    log.debug("Error en fila {}: {}", row, e.getMessage(), e);
                    errors.add(new BulkImportResult.RowError(row, e.getMessage()));
                }
            }
        } catch (Exception e) {
            log.error("Error leyendo archivo CSV", e);
            errors.add(new BulkImportResult.RowError(0, "Error leyendo el archivo: " + e.getMessage()));
        }

        return BulkImportResult.builder().total(total).success(success).failed(errors.size()).errors(errors).build();
    }
}
