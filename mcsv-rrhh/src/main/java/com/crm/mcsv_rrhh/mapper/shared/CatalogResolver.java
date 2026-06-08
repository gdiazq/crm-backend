package com.crm.mcsv_rrhh.mapper.shared;

import com.crm.mcsv_rrhh.dto.CatalogItem;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Resuelve catálogos por id de forma genérica (vía {@code getName()} por reflexión), para que los
 * mappers no repitan esta lógica. Sirve para cualquier repositorio cuya entidad exponga getName().
 */
public final class CatalogResolver {

    private CatalogResolver() {}

    /** Nombre del registro con ese id, o {@code null} si el id es null o no existe. */
    public static <T> String name(Long id, JpaRepository<T, Long> repo) {
        if (id == null) {
            return null;
        }
        return repo.findById(id).map(CatalogResolver::nameOf).orElse(null);
    }

    /** {@link CatalogItem} (id + nombre) del registro, o {@code null} si el id es null o no existe. */
    public static <T> CatalogItem item(Long id, JpaRepository<T, Long> repo) {
        if (id == null) {
            return null;
        }
        return repo.findById(id)
                .map(e -> new CatalogItem(id, nameOf(e)))
                .orElse(null);
    }

    private static String nameOf(Object entity) {
        try {
            return (String) entity.getClass().getMethod("getName").invoke(entity);
        } catch (Exception e) {
            return null;
        }
    }
}
