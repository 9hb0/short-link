package com.nageoffer.shortlink.project.controller;

import com.nageoffer.shortlink.project.service.UrlTitleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UrlTitleController {
    private final UrlTitleService urlTitleService;

    @GetMapping("/api/short-link/v1/title")
    public String getTitle(@RequestParam("url") String url) {
        return urlTitleService.getTitle(url);
    }
}
