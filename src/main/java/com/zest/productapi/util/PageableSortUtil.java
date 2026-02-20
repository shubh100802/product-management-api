package com.zest.productapi.util;

// ==========file-context==========

import java.util.Set;

public final class PageableSortUtil {

    private static final Set<String> ALLOWED_PRODUCT_SORTS = Set.of(
            "id",
            "productName",
            "createdOn",
            "modifiedOn",
            "createdBy",
            "modifiedBy"
    );

    private PageableSortUtil() {
    }

    public static String validateProductSortBy(String sortBy) {
        if (!ALLOWED_PRODUCT_SORTS.contains(sortBy)) {
            throw new IllegalArgumentException("Invalid sortBy: " + sortBy);
        }
        return sortBy;
    }
}

