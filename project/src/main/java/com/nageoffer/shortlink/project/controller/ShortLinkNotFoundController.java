package com.nageoffer.shortlink.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ShortLinkNotFoundController {
    @RequestMapping("/page/notfound")
    public String notFound(){
        //返回视图名称
        return "notfound";
    }

}
