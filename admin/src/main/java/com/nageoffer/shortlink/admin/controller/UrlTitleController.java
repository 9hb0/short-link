package com.nageoffer.shortlink.admin.controller;

import com.nageoffer.shortlink.admin.remote.ShortLinkRemoteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UrlTitleController {
    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };
    @GetMapping("/api/short-link/admin/v1/title")
    public String getTitle(@RequestParam("url") String url) {
        return shortLinkRemoteService.getTitle(url);
    }
}
