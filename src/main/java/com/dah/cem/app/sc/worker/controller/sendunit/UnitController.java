package com.dah.cem.app.sc.worker.controller.sendunit;

import com.dah.cem.app.sc.worker.domain.sendunit.Unit;
import com.dah.cem.app.sc.worker.domain.sendunit.UnitMapper;
import com.dah.cem.app.sc.worker.infrastructure.mybatis.Pagination;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class UnitController {

    @Autowired
    private UnitMapper unitMapper;

    @GetMapping(path = "/units", produces = "text/html")
    public String toPage() {
        return "/view/unit/units";
    }

    @GetMapping(path = "/units/list-page", produces = "application/json")
    @ResponseBody
    public Pagination<Unit> getPage(@RequestParam("search") String search,
                                    @RequestParam("pageNum") int pageNum,
                                    @RequestParam("pageSize") int pageSize) {
        return new Pagination<>(unitMapper.getPage(search, new RowBounds(pageNum, pageSize)));
    }
}
