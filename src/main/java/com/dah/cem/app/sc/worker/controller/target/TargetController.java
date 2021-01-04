package com.dah.cem.app.sc.worker.controller.target;

import com.dah.cem.app.sc.worker.controller.ExcelHelperAction;
import com.dah.cem.app.sc.worker.domain.target.BaseTarget;
import com.dah.cem.app.sc.worker.domain.target.BaseTargetMapper;
import com.dah.cem.app.sc.worker.domain.target.BaseTargetService;
import com.dah.cem.app.sc.worker.infrastructure.exception.ServiceException;
import com.dah.cem.app.sc.worker.infrastructure.mybatis.Pagination;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.ParseException;

@Controller
public class TargetController {

    @Autowired
    private BaseTargetMapper baseTargetMapper;

    @Autowired
    private BaseTargetService baseTargetService;

    @Autowired
    ExcelHelperAction excelHelperAction;

    @GetMapping(path = "/targets", produces = "text/html")
    public String toPage() {
        return "/view/target/targets";
    }

    @GetMapping(path = "/targets/list-page", produces = "application/json")
    @ResponseBody
    public Pagination<BaseTarget> getPage(@RequestParam("search") String search,
                                          @RequestParam("pageNum") int pageNum,
                                          @RequestParam("pageSize") int pageSize) {
        return new Pagination<>(baseTargetMapper.getPage(search, new RowBounds(pageNum, pageSize)));
    }

    @PostMapping(path = "/targets", produces = "application/json")
    @ResponseBody
    public void save(@RequestBody BaseTarget baseTarget) {
        baseTargetService.save(baseTarget);
    }

    @PutMapping(path = "/targets/{probeCode}", produces = "application/json")
    @ResponseBody
    public void update(@PathVariable("probeCode") String probeCode, @RequestBody BaseTarget baseTarget) {
        baseTargetService.update(probeCode, baseTarget);
    }

    @DeleteMapping(path = "/targets/{probeCode}", produces = "application/json")
    @ResponseBody
    public void remove(@PathVariable("probeCode") String probeCode) {
        baseTargetService.remove(probeCode);
    }

    @PostMapping(path = "/targets/import", produces = "application/json")
    @ResponseBody
    public void importTarget(@RequestParam(value = "file", required = false) MultipartFile file) throws ParseException {
        excelHelperAction.readExcelFile(file);
    }

    @GetMapping(path = "/targets/temp-file", produces = "application/json")
    @ResponseBody
    public void getTempFile(HttpServletResponse response) throws Exception {
        String path = "classpath:templates/view/target/指标数据模板.xlsx";
        File file = ResourceUtils.getFile(path);
        if (!file.exists()) {
            throw new ServiceException("文件不存在！");
        }
        response.setCharacterEncoding("utf-8");
        response.setContentType("multipart/form-data");
        response.setCharacterEncoding("UTF-8");
        response.addHeader("Pragma", "no-cache");
        response.addHeader("Cache-Control", "no-cache");
        response.addHeader("Expires", "0");
        try {
            response.addHeader("Content-Disposition", "attachment;filename=\"" + new String("指标数据模板.xlsx".getBytes("gb2312"), "ISO8859-1") + "\"");
        } catch (UnsupportedEncodingException e) {
            response.addHeader("Content-Disposition", "attachment;filename=\"指标数据模板.xlsx\"");
        }
        try {
            InputStream fis = new FileInputStream(file);
            byte[] bytes = FileCopyUtils.copyToByteArray(fis);
            response.setContentLength(bytes.length);
            FileCopyUtils.copy(bytes, response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
