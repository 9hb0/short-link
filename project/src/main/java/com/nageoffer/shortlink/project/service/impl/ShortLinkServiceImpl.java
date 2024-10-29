package com.nageoffer.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.project.common.convention.exception.ClientException;
import com.nageoffer.shortlink.project.common.convention.exception.ServiceException;
import com.nageoffer.shortlink.project.common.enums.ValidDateTypeEnum;
import com.nageoffer.shortlink.project.dao.entity.*;
import com.nageoffer.shortlink.project.dao.mapper.*;
import com.nageoffer.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.nageoffer.shortlink.project.service.ShortLinkService;
import com.nageoffer.shortlink.project.toolkit.HashUtil;
import com.nageoffer.shortlink.project.util.LinkUtil;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static cn.hutool.core.date.DateTime.now;
import static com.nageoffer.shortlink.project.common.constant.RedisKeyConstant.*;
import static com.nageoffer.shortlink.project.common.constant.ShortLinkConstant.AMAP_REMOTE_URL;
import static com.nageoffer.shortlink.project.util.LinkUtil.*;


@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;
    private final StringRedisTemplate stringRedisTemplate;
    private final ShortLinkGotoMapper shortLinkGotoMapper;
    private final RedissonClient redissonClient;
    private final TLinkAccessStatsMapper tLinkAccessStatsMapper;
    private final TLinkLocaleStatsMapper tLinkLocaleStatsMapper;
    private final TLinkOsStatsMapper tLinkOsStatsMapper;
    private final TLinkBrowserStatsMapper tLinkBrowserStatsMapper;
    private final TLinkAccessLogsMapper tLinkAccessLogsMapper;
    private final TLinkDeviceStatsMapper tLinkDeviceStatsMapper;
    private final TLinkNetworkStatsMapper tLinkNetworkStatsMapper;

    @Value("${short-link.stats.locale.amap-key}")
    private String amapKey;

    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        String shortLinkSuffix = generateSuffix(requestParam);
        String fullShortUrl = StrBuilder.create(requestParam.getDomain())
                .append("/")
                .append(shortLinkSuffix)
                .toString();
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .domain(requestParam.getDomain())
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .createdType(requestParam.getCreatedType())
                .validDateType(requestParam.getValidDateType())
                .validDate(requestParam.getValidDate())
                .describe(requestParam.getDescribe())
                .shortUri(shortLinkSuffix)
                .favicon(getFaviconUrl(requestParam.getOriginUrl()))
                .enableStatus(0)
                .fullShortUrl(fullShortUrl)
                .build();
        ShortLinkGotoDO shortLinkGotoDO = ShortLinkGotoDO.builder()
                .id(shortLinkDO.getId())
                .gid(shortLinkDO.getGid())
                .fullShortUrl(shortLinkDO.getFullShortUrl())
                .build();
        try{
            baseMapper.insert(shortLinkDO);
            shortLinkGotoMapper.insert(shortLinkGotoDO);

        } catch (DuplicateKeyException ex){
            //TODO:为什么还要生成一次
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                            .eq(ShortLinkDO::getFullShortUrl, fullShortUrl);
            ShortLinkDO hasSameShortLinkDO = baseMapper.selectOne(queryWrapper);
            if (hasSameShortLinkDO != null){
                log.warn("短链接重复入库" + fullShortUrl);
                throw new ServiceException("短链接生成重复");
            }
        }
        //key：fullshorturl ; value: originurl，long timeout: 值在自动删除之前应在缓存中保留的时间。
        stringRedisTemplate.opsForValue().set(
                String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                requestParam.getOriginUrl(),
                LinkUtil.getLinkCacheValidTime(requestParam.getValidDate())
        );

        shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);
        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl("http://" + shortLinkDO.getFullShortUrl())
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .build();
    }

    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam) {
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getEnableStatus, 0)
                .eq(ShortLinkDO::getDelFlag, 0);
        IPage<ShortLinkDO> resultPage = baseMapper.selectPage(requestParam, queryWrapper);
        return resultPage.convert(each -> {
            ShortLinkPageRespDTO result = BeanUtil.toBean(each, ShortLinkPageRespDTO.class);
            result.setDomain("http://" + result.getDomain());
            return result;
        });
    }

    @Override
    public List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkGroup(List<String> requestParam) {
        QueryWrapper<ShortLinkDO> queryWrapper = Wrappers.query(new ShortLinkDO())
                .select("gid as gid, count(*) as shortLinkCount")
                .in("gid", requestParam)
                .eq("enable_status", 0)
                .groupBy("gid");
        List<Map<String, Object>> shortLinkList = baseMapper.selectMaps(queryWrapper);

//        List<Object> objectList = baseMapper.selectObjs(queryWrapper);
        return BeanUtil.copyToList(shortLinkList, ShortLinkGroupCountQueryRespDTO.class);
    }

    //声明式事务，发生错误-》回滚
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Void updateShortLink(ShortLinkUpdateReqDTO requestParam) {
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
//        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(new ShortLinkDO())
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0);
        ShortLinkDO hasShortLinkDO = baseMapper.selectOne(queryWrapper);
        if (hasShortLinkDO == null){
            throw new ClientException("短链接不存在");
        }
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .gid(requestParam.getGid())
                .originUrl(requestParam.getOriginUrl())
                .describe(requestParam.getDescribe())
                .validDateType(requestParam.getValidDateType())
                .validDate(requestParam.getValidDate())
                .build();
        if (Objects.equals(requestParam.getGid(), hasShortLinkDO.getGid())){
            LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(ShortLinkDO::getGid, requestParam.getGid())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .set(Objects.equals(requestParam.getValidDateType(), ValidDateTypeEnum.PERMANET.getType()), ShortLinkDO::getValidDateType, null);
            baseMapper.update(shortLinkDO, updateWrapper);
        } else {
            LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(ShortLinkDO::getGid, requestParam.getGid())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .set(Objects.equals(requestParam.getValidDateType(), ValidDateTypeEnum.PERMANET.getType()), ShortLinkDO::getValidDateType, null);
            baseMapper.delete(queryWrapper);
            shortLinkDO.setGid(requestParam.getGid());
            baseMapper.insert(shortLinkDO);
        }
        return null;
    }

    @SneakyThrows
    @Override
    public void restoreUrl(String shortUri, ServletRequest request, ServletResponse response) {
        String serverName = request.getServerName();
        //Optional 类来处理可能为空的对象引用
//        String serverPort = Optional.of(request.getServerPort())
//                .filter(each -> !Objects.equals(each, 80))
//                .map(String::valueOf)
//                .map(each -> ":" + each)
//                .orElse("");
        String fullShortUrl = serverName + "/" +shortUri;
        //判断短链接是否在redis中
        String originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(originalLink)) {
            //在redis中
            insertOrUpdate(fullShortUrl,null,request,response);
            ((HttpServletResponse)response).sendRedirect(originalLink);
            return;
        }
        //不在redis中，再用bloomfilter判断fullshorturl是否存在
        boolean contains = shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl);
        if (!contains) {
            ((HttpServletResponse)response).sendRedirect("page/notfound");
            return;
        }
        //判断是否是空值
        String gotoIsNullShortLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(gotoIsNullShortLink)) {
            ((HttpServletResponse)response).sendRedirect("page/notfound");
            return;
        }
        //分布式锁
        RLock lock = redissonClient.getLock(String.format(LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl));
        lock.lock();
        try {
            //双重判定
            originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
            if (StrUtil.isNotBlank(originalLink)) {
                insertOrUpdate(fullShortUrl,null,request,response);
                ((HttpServletResponse)response).sendRedirect(originalLink);
                return;
            }
            //双重判定
            gotoIsNullShortLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl));
            if (StrUtil.isNotBlank(gotoIsNullShortLink)) {
                ((HttpServletResponse)response).sendRedirect("page/notfound");
                return;
            }

            LambdaQueryWrapper<ShortLinkGotoDO> linkGotoQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                    .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
            ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(linkGotoQueryWrapper);
            if (shortLinkGotoDO == null) {
                stringRedisTemplate.opsForValue().set(
                        String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl),
                        "-", 3, TimeUnit.MINUTES
                );

                ((HttpServletResponse)response).sendRedirect("page/notfound");
                //严禁来说此处需要进行风控
                return;
            }
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getGid, shortLinkGotoDO.getGid())
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0);
            ShortLinkDO shortLinkDO = baseMapper.selectOne(queryWrapper);
            if (shortLinkDO != null) {
                if (shortLinkDO.getValidDate() != null && shortLinkDO.getValidDate().before(now())) {
                    stringRedisTemplate.opsForValue().set(
                            String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl),
                            "-", 3, TimeUnit.MINUTES
                    );

                    ((HttpServletResponse)response).sendRedirect("page/notfound");
                }
                stringRedisTemplate.opsForValue().set(
                        String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                        shortLinkDO.getOriginUrl(),
                        LinkUtil.getLinkCacheValidTime(shortLinkDO.getValidDate()),
                        TimeUnit.MILLISECONDS
                );
                insertOrUpdate(fullShortUrl,shortLinkDO.getGid(),request,response);
                ((HttpServletResponse)response).sendRedirect(shortLinkDO.getOriginUrl());
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 插入或更新-->记录短链接访问状态
     * @param fullShortUrl
     * @param gid
     * @param request
     * @param response
     */
    private void insertOrUpdate(String fullShortUrl, String gid, ServletRequest request, ServletResponse response) {
        Cookie[] cookies = ((HttpServletRequest) request).getCookies();
        //多线程环境中提供原子操作
        AtomicBoolean uvFirstFlag = new AtomicBoolean();
        try {
            AtomicReference<String> uv = new AtomicReference<>();
            Runnable addResponseCookieTask = () -> {
            uv.set(UUID.randomUUID().toString());
            Cookie uvcookie = new Cookie("uv", uv.get());
            uvcookie.setMaxAge(60 * 60 * 24 * 30);
            uvcookie.setPath(StrUtil.sub(fullShortUrl, fullShortUrl.indexOf("/"), fullShortUrl.length()));
            ((HttpServletResponse)response).addCookie(uvcookie);
            uvFirstFlag.set(Boolean.TRUE);
            stringRedisTemplate.opsForSet().add("short-link:stats:uv:" + fullShortUrl, uv.get() );
            };
            //设置COOKIE，判断是否为同一用户访问
            if (ArrayUtil.isNotEmpty(cookies)) {
                Arrays.stream(cookies)
                        .filter(each -> Objects.equals(each.getName(), "uv"))
                        .findFirst()
                        .map(Cookie::getValue)
                        .ifPresentOrElse(each -> {
                            uv.set(each);
                            Long uvAdded = stringRedisTemplate.opsForSet().add("short-link:stats:uv:" + fullShortUrl, each);
                            uvFirstFlag.set(uvAdded != null && uvAdded > 0L);
                        }, addResponseCookieTask);
            } else {
                addResponseCookieTask.run();
            }
            //获取用户真实IP，设置uip
            String ipAddress = getIpAddress((HttpServletRequest) request);
            Long uipAdded = stringRedisTemplate.opsForSet().add("short-link:static:uip:", ipAddress);
            boolean uipFirstFlag = uipAdded != null && uipAdded >0L;
            //传入的gid不存在时，从linkgoto去查找
            if (StrUtil.isBlank(gid)) {
                LambdaQueryWrapper<ShortLinkGotoDO> linkGotoQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                        .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
                ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(linkGotoQueryWrapper);
                    gid = shortLinkGotoDO.getGid();
            }

            int hour = DateUtil.hour(new Date(), true);
            Week week = DateUtil.dayOfWeekEnum(new Date());
            int weekValue = week.getValue();
            TLinkAccessStatsDO linkAccessStats = TLinkAccessStatsDO
                    .builder()
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .uv(uvFirstFlag.get()? 1: 0)
                    .pv(1)
                    .uip(uipFirstFlag? 1 : 0)
                    .date(new Date())
                    .hour(hour)
                    .weekday(weekValue)
                    .build();
            tLinkAccessStatsMapper.insertOrUpdate(linkAccessStats);
            //根据ip获取地区信息
            HashMap<String, Object> paramMap = new HashMap<>();
            paramMap.put("key", amapKey);
            paramMap.put("ip", ipAddress);
            String localeResultStr = HttpUtil.get(AMAP_REMOTE_URL, paramMap);
            JSONObject localResultobj = JSON.parseObject(localeResultStr);
            String infocode = localResultobj.getString("infocode");
            String actualProvince = "未知";
            String actualCity = "未知";
            if (StrUtil.isNotBlank(infocode) && StrUtil.equals(infocode, "10000")) {
                String province = localResultobj.getString("province");
                Boolean unknownFlag = StrUtil.isBlank(province);
                TLinkLocaleStatsDO tLinkLocaleStatsDO = TLinkLocaleStatsDO
                        .builder()
                        .gid(gid)
                        .fullShortUrl(fullShortUrl)
                        .date(new Date())
                        .cnt(1)
                        .province(actualProvince = unknownFlag ?  "未知" : province)
                        .city(actualCity = unknownFlag ?  "未知" : localResultobj.getString("city"))
                        .adcode(unknownFlag ? localResultobj.getString("adcode") : "未知")
                        .country("中国")
                        .build();
                tLinkLocaleStatsMapper.insertOrUpdate(tLinkLocaleStatsDO);
            }
            //获取操作系统
            String stringOs = httpForOS((HttpServletRequest) request);
            TLinkOsStatsDO tLinkOsStatsDO = TLinkOsStatsDO
                    .builder()
                    .gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .date(new Date())
                    .cnt(1)
                    .os(stringOs)
                    .build();
            tLinkOsStatsMapper.insertOrUpdate(tLinkOsStatsDO);
            //获取浏览器
            String browser = httpForBrowser((HttpServletRequest) request);
            TLinkBrowserStatsDO tLinkBrowserStatsDO = TLinkBrowserStatsDO
                    .builder()
                    .gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .date(new Date())
                    .cnt(1)
                    .browser(browser)
                    .build();
            tLinkBrowserStatsMapper.insertOrUpdate(tLinkBrowserStatsDO);
            //获取高频访问的完整短链接
            TLinkAccessLogsDO tLinkAccessLogsDO = TLinkAccessLogsDO
                    .builder()
                    .gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .user(uv.get())
                    .ip(ipAddress)
                    .os(stringOs)
                    .browser(browser)
                    .build();
            tLinkAccessLogsMapper.insert(tLinkAccessLogsDO);

            //统计设备信息
            String device = LinkUtil.getDevice(((HttpServletRequest) request));
            TLinkDeviceStatsDO linkDeviceStatsDO = TLinkDeviceStatsDO.builder()
                    .device(device)
                    .cnt(1)
                    .gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .date(new Date())
                    .build();
            tLinkDeviceStatsMapper.shortLinkDeviceState(linkDeviceStatsDO);

            //统计访问网络
            String network = LinkUtil.getNetwork(((HttpServletRequest) request));
            TLinkNetworkStatsDO linkNetworkStatsDO = TLinkNetworkStatsDO.builder()
                    .network(network)
                    .cnt(1)
                    .gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .date(new Date())
                    .build();
            tLinkNetworkStatsMapper.shortLinkNetworkState(linkNetworkStatsDO);

            //记录短链接访问日志
            TLinkAccessLogsDO tLinkAccessLogsDO1 = TLinkAccessLogsDO.builder()
                    .user(uv.get())
                    .ip(ipAddress)
                    .browser(browser)
                    .os(stringOs)
                    .network(network)
                    .device(device)
                    .locale(StrUtil.join("-", "中国", actualProvince, actualCity))
                    .gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .build();
            tLinkAccessLogsMapper.insert(tLinkAccessLogsDO1);
        } catch (Exception e) {
            log.error("统计访问量失败", e);
        }
    }

    private String generateSuffix(ShortLinkCreateReqDTO requestParam) {
        int customGenerateCount = 0;
        String shortUri;
        while(true){
            if (customGenerateCount > 10){
                throw new ServiceException("生成短链接频繁，请稍后再试");
            }
            String originUrl = requestParam.getOriginUrl();
            shortUri = HashUtil.hashToBase62(originUrl);
//          用布隆过滤器替代的方法
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, requestParam.getDomain() + "/" + shortUri);
            ShortLinkDO shortLinkDO = baseMapper.selectOne(queryWrapper);
//            if (shortUriCreateCachePenetrationBloomFilter.contains(requestParam.getDomain() + "/" + shortUri)){
//                break;
//            }
            if (shortLinkDO == null) {
                break;
            }
            customGenerateCount++;
        }
        return shortUri;
    }

    private String getFaviconUrl(String websiteUrl) {
        try {
            // 发送HTTP HEAD请求以获取HTML头部信息（可以更快，但不一定包含favicon链接）
            // 或者发送GET请求以获取完整的HTML文档
            Document doc = Jsoup.connect(websiteUrl).get();

            // 使用Jsoup选择器查找favicon链接
            // 注意：favicon的链接可能有多种写法，这里只检查最常见的几种
            Elements links = doc.select("link[rel=icon], link[rel=shortcut icon], link[type=image/x-icon]");
            for (Element link : links) {
                String href = link.attr("href");
                if (!href.isEmpty() && !(href.startsWith("/") && !href.startsWith("//"))) {
                    // 如果href是相对路径且不是协议相对URL，则转换为绝对路径
                    if (href.startsWith("/")) {
                        href = websiteUrl + href;
                    }
                    return href;
                }
            }

            // 如果没有找到<link>标签，可以检查HTML的<head>部分是否包含内联的base64编码的favicon
            // 这里不实现此逻辑，因为它更复杂且不如直接链接常见

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}