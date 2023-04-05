package com.iflytek.gulimall.search.controller;


import com.iflytek.gulimall.common.exception.GulimallExceptinCodeEnum;
import com.iflytek.gulimall.common.utils.ResultBody;
import com.iflytek.gulimall.search.service.MallSearchService;
import com.iflytek.gulimall.search.vo.SearchParam;

import com.iflytek.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Controller
public class SearchController {

    @Autowired
    private MallSearchService mallSearchService;

    /**
     * catalog3Id=167&keyword=手机&sort=saleCount_desc&brandId=1 可以直接用对象接收
     *
     * @param paramEo
     * @return
     */
    @GetMapping({"/", "list.html"})
    public String list(SearchParam paramEo, Model model, HttpServletRequest request) throws IOException {
        paramEo.set_queryString(request.getQueryString());
        SearchResult searchResult = mallSearchService.search(paramEo);
        model.addAttribute("result",searchResult);
        return "list";
    }


}
