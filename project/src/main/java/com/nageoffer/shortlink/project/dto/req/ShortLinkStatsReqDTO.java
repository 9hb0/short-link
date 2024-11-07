package com.nageoffer.shortlink.project.dto.req;

import lombok.Data;

/**
 * 短链接监控请求参数
 */
@Data
public class ShortLinkStatsReqDTO {

    private String fullShortUrl;
    private String gid;
    private String startDate;
    private String endDate;
}
