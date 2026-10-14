package com.hs.domain.dto;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(description = "分页查询参数")
public class PageQueryDTO {

    @Schema(description = "页码", example = "1")
    private Integer pageNo = 1;

    @Schema(description = "每页数量", example = "20")
    private Integer pageSize = 20;

    @Schema(description = "排序字段", example = "evaluated_at")
    private String sortBy;

    @Schema(description = "排序方向：asc/desc", example = "desc")
    private String sortOrder = "desc";

    public <T> Page<T> toMpPage(String defaultSortBy, boolean defaultAsc) {
        int pageNoValue = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int pageSizeValue = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 1000);
        Page<T> page = new Page<>(pageNoValue, pageSizeValue);

        String orderBy = (sortBy == null || sortBy.isBlank()) ? defaultSortBy : sortBy;
        boolean asc = (sortOrder == null || sortOrder.isBlank()) ? defaultAsc : "asc".equalsIgnoreCase(sortOrder);
        if (orderBy != null && !orderBy.isBlank()) {
            page.addOrder(asc ? OrderItem.asc(orderBy) : OrderItem.desc(orderBy));
        }
        return page;
    }
}
