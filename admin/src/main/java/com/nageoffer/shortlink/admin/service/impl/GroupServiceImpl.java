package com.nageoffer.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.admin.dao.entity.GroupDO;
import com.nageoffer.shortlink.admin.dao.mapper.GruopMapper;
import com.nageoffer.shortlink.admin.service.GroupService;
import com.nageoffer.shortlink.admin.toolkit.RandomGenerate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 短链接分组接口实现层
 */
@Slf4j
@Service
public class GroupServiceImpl extends ServiceImpl<GruopMapper, GroupDO> implements GroupService  {

    @Override
    public void saveGroup(String groupName) {
//        GroupDO groupDO = new GroupDO();
        GroupDO groupDO = GroupDO.builder()
                .gid(RandomGenerate.generateRandomString())
                .username(groupName)
                .build();
//        groupDO.setName(groupName);
        baseMapper.insert(groupDO);
    }
}
