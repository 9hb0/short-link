package com.nageoffer.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.admin.remote.ShortLinkRemoteService;
import com.nageoffer.shortlink.admin.remote.dto.req.RecycleBinRecoverReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.req.RecycleBinRemoveReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.nageoffer.shortlink.admin.remote.dto.req.RecycleBinSaveReqDTO;
import com.nageoffer.shortlink.admin.service.RecycleBinService;
import com.nageoffer.shortlink.project.common.convention.result.Result;
import com.nageoffer.shortlink.project.common.convention.result.Results;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RecycleBinController {
    //后续重构为spring clound
    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };
    private final RecycleBinService recycleBinService;

    /**
     * 回收站保存功能
     * @param requestParam
     * @return
     */
    @PostMapping("/api/short-link/admin/v1/recycle-bin/save")
    public Void save(@RequestBody RecycleBinSaveReqDTO requestParam)  {
        return shortLinkRemoteService.saveRecycleBin(requestParam);
    }

    /**
     * 分页查询短链接
     */
    @GetMapping("/api/short-link/admin/v1/recycle-bin/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkRecycleBinPageReqDTO requestParam){
        return Results.success(recycleBinService.pageShortLink(requestParam));
    }

    /**
     * 短链接恢复功能
     * @param requestParam
     * @return
     */
    @PostMapping("/api/short-link/admin/v1/recycle-bin/recover")
    public Void recover(@RequestBody RecycleBinRecoverReqDTO requestParam)  {
        return shortLinkRemoteService.recoverRecycleBin(requestParam);
    }

    /**
     * 短链接移除功能
     */
    @PostMapping("/api/short-link/admin/ v1/recycle-bin/remove")
    public Void removeRecycleBin(@RequestBody RecycleBinRemoveReqDTO requestParam)  {
        return shortLinkRemoteService.removeRecycleBin(requestParam);
    }
}
