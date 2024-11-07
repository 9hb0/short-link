package com.nageoffer.shortlink.admin.dto.req;

import cn.hutool.db.Page;
import lombok.Data;

@Data
public class ShortLinkPageReqDTO extends Page {
    private String gid;
    /**
     * 排序标识
     */
    private String orderTag;
}
