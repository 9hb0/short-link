package com.nageoffer.shortlink.project.common.config;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 布隆过滤器配置
 */
@Configuration
public class RBloomFilterConfiguration {
    /**
     * 防止短链接创建查询数据库
     */
    @Bean
    public RBloomFilter<String> ShortUriCreateCachePenetrationBloomFilter(RedissonClient redissonClient){
        RBloomFilter<String> cachePenetrationBloomFilter = redissonClient.getBloomFilter("userRegisterCachePenetrationBloomFilter");
        //第一个参数是布隆过滤器有多少参数，第二个是误判率
        cachePenetrationBloomFilter.tryInit(1000000,0.001);
        return cachePenetrationBloomFilter;
    }
}
