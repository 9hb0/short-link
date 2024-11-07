package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkDO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkPageReqDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 短链接持久层
 */
public interface ShortLinkMapper extends BaseMapper<ShortLinkDO> {


    List<Map<String, Object>> selectMaps(QueryWrapper<Class<ShortLinkDO>> queryWrapper);

    @Update("update t_link set totalPv = totalPv + #{totalPv} and totalUv = totalUv + #{totalUv} and totalUip = totalUip + #{totalUip} where gid = #{gid} and fullShortUrl = #{fullShortUrl}")
    void incrementStats(
            @Param("gid") String gid,
            @Param("fullShortUrl") String fullShortUrl,
            @Param("totalPv") Integer totalPv,
            @Param("totalUv") Integer totalUv,
            @Param("totalUip") Integer totalUip);

    /**
     * 分页统计短链接
     * @param shortLinkPageReqDTO
     * @return
     */
    IPage<ShortLinkDO> pageLink(ShortLinkPageReqDTO shortLinkPageReqDTO);
}
