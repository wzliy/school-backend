package com.zlwang.school.modules.portal.dto;

import com.zlwang.school.common.pagination.PageQuery;
import com.zlwang.school.modules.template.model.SiteType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "公开内容搜索参数")
public class PortalSearchQuery extends PageQuery {

    @NotBlank(message = "不能为空")
    @Size(max = 100, message = "长度不能超过 100 个字符")
    @Schema(description = "标题关键词，去除首尾空白后匹配", example = "校园新闻", maxLength = 100)
    private String keyword;

    @NotNull(message = "不能为空")
    @Schema(description = "目标站点", example = "MAIN_SITE")
    private SiteType siteType;

    @Positive(message = "栏目 ID 必须大于 0")
    @Schema(description = "可选栏目 ID，必须属于目标站点且已启用", example = "101")
    private Long columnId;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public SiteType getSiteType() {
        return siteType;
    }

    public void setSiteType(SiteType siteType) {
        this.siteType = siteType;
    }

    public Long getColumnId() {
        return columnId;
    }

    public void setColumnId(Long columnId) {
        this.columnId = columnId;
    }
}
