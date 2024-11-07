package com.nageoffer.shortlink.project.service.impl;

import com.nageoffer.shortlink.project.dao.mapper.TLinkAccessStatsMapper;
import com.nageoffer.shortlink.project.service.TLinkAccessStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TLinkAccessStatsServiceImpl implements TLinkAccessStatsService {
    private final TLinkAccessStatsMapper baseMapper;

//    @Override
//    public void insertOrUpdate(String fullShortUrl, String gid, Servlet request, Servlet response) {
//        int hour = DateUtil.hour(new Date(), true);
//        Week week = DateUtil.dayOfWeekEnum(new Date());
//        int weekValue = week.getValue();
//        TLinkAccessStatsDO linkAccessStatsDO = TLinkAccessStatsDO
//                .builder()
//                .fullShortUrl(fullShortUrl)
//                .gid(gid)
//                .date(new Date())
//                .build();
//
//        baseMapper.insertOrUpdate();
//    }
}
