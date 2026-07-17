package com.zlwang.school.modules.seo.model;

public record SeoMetadata(
    String title,
    String keywords,
    String description,
    String canonicalPath
) {
}
