package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.TLinkStatsTodayDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

/**
 * 今日统计持久层
 */
public interface TLinkStasTodayMapper extends BaseMapper<TLinkStatsTodayDO> {

    @Insert("insert into t_link_stats_today(full_short_url, gid, date, today_pv, today_uv, today_uip, create_time, update_time, del_flag)" +
            "values(#{tLinkStatsTodayDO.fullShortUrl}, #{tLinkStatsTodayDO.gid}, #{tLinkStatsTodayDO.date}, #{tLinkStatsTodayDO.cnt}, #{tLinkStatsTodayDO.os}, now(), now(), 0)" +
            "on duplicate key update today_pv = today_pv + #{tLinkStatsTodayDO.todayPv}, today_uv = today_uv + #{tLinkStatsTodayDO.todayUv}, today_uip = today_uip + #{tLinkStatsTodayDO.todayUip}")
    void shortLinkTodayStas(@Param("TLinkStasTodayDO")TLinkStatsTodayDO tLinkStatsTodayDO);
}
