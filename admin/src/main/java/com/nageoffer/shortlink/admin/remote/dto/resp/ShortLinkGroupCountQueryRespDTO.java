package com.nageoffer.shortlink.admin.remote.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短链接分组查询返回参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShortLinkGroupCountQueryRespDTO {
    private String gid;
    //短链接数目
    private Integer shortLinkCount;
}
