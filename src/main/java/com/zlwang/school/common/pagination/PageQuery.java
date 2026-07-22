package com.zlwang.school.common.pagination;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class PageQuery {

    @Min(value = 1, message = "页码不能小于 1")
    @Schema(description = "页码，从 1 开始", example = "1", defaultValue = "1", minimum = "1")
    private long pageNo = 1;

    @Min(value = 1, message = "每页条数不能小于 1")
    @Max(value = 100, message = "每页条数不能大于 100")
    @Schema(description = "每页条数", example = "10", defaultValue = "10", minimum = "1", maximum = "100")
    private long pageSize = 10;

    public long getPageNo() {
        return pageNo;
    }

    public void setPageNo(long pageNo) {
        this.pageNo = pageNo;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }
}
