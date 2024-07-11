package com.nageoffer.shortlink.admin.remote;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.remote.dto.req.*;
import com.nageoffer.shortlink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.admin.remote.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.nageoffer.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 短链接中台远程调用服务
 */
public interface ShortLinkRemoteService {

    /**
     * 创建短链接
     * @param requestParam 请求参数
     * @return 短链接创建响应
     */
    default Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        String resultBodyStr = HttpUtil.post("127.0.0.1:8003/api/short-link/v1/create", JSON.toJSONString(requestParam));
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
        });
    }

    /**
     * 分页查询短链接
     * @param requestParam
     * @return
     */
    default Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        HashMap<String, Object> requestMap = new HashMap<>();
        requestMap.put("gid", requestParam.getGid());
        requestMap.put("current", requestParam.getCurrent());
        requestMap.put("size", requestParam.getSize());
        String resultPageStr = HttpUtil.get("127.0.0.1:8003/api/short-link/v1/page", requestMap);
        return JSON.parseObject(resultPageStr, new TypeReference<>() {
        });
    }

    /**
     * 修改短链接
     *
     * @param requestParam
     * @return
     */
    default void updateShortLink(ShortLinkUpdateReqDTO requestParam) {
        String resultBodyStr = HttpUtil.post("127.0.0.1:8003/api/short-link/v1/update", JSON.toJSONString(requestParam));

    }

    /**
     * 查询短链接分组数量
     * @param requestParam
     * @return
     */
    default Result<List<ShortLinkGroupCountQueryRespDTO>> listGroupShortLinkCount(List<String> requestParam){
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("requestParam", requestParam);
//        List<String> gid = (List<String>) requestMap.put("gid", requestParam);
        String resultPageStr = HttpUtil.get("127.0.0.1:8003/api/short-link/v1/count", requestMap);
        return JSON.parseObject(resultPageStr, new TypeReference<>() {
        });

    }

    /**
     * 根据url获取网站标题
     * @param url
     * @return
     */
    default String getTitle(String url) {
        Map<String, Object> titleMap = new HashMap<>();
        titleMap.put("url", url);
        String title = HttpUtil.get("127.0.0.1:8003/api/short-link/v1/title", titleMap);
        return title;
    }


    default Void saveRecycleBin(RecycleBinSaveReqDTO requestParam) {
        HttpUtil.post("127.0.0.1:8003/api/short-link/v1/recycle-bin/save", JSON.toJSONString(requestParam));
        return null;
    }

    default IPage<ShortLinkPageRespDTO> pageRecycleBinShortLink(ShortLinkRecycleBinPageReqDTO requestParam) {
        HashMap<String, Object> requestMap = new HashMap<>();
        requestMap.put("gidList", requestParam.getGidList());
        requestMap.put("current", requestParam.getCurrent());
        requestMap.put("size", requestParam.getSize());
        String resultPageStr = HttpUtil.get("127.0.0.1:8003/api/short-link/v1/recycle-bin/page", requestMap);
        return JSON.parseObject(resultPageStr, new TypeReference<>() {
        });
    }

    default Void recoverRecycleBin(RecycleBinRecoverReqDTO requestParam) {
        HttpUtil.post("127.0.0.1:8003/api/short-link/v1/recycle-bin/recover", JSON.toJSONString(requestParam));
        return null;
    }

    default Void removeRecycleBin(RecycleBinRemoveReqDTO requestParam) {
        HttpUtil.post("127.0.0.1:8003/api/short-link/v1/recycle-bin/remove", JSON.toJSONString(requestParam));
        return null;
    }
}