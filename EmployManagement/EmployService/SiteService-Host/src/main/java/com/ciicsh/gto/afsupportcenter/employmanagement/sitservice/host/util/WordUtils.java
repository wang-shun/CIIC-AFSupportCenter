package com.ciicsh.gto.afsupportcenter.employmanagement.sitservice.host.util;

import com.ciicsh.gto.afsupportcenter.util.StringUtil;
import com.ciicsh.gto.afsupportcenter.util.logService.LogApiUtil;
import com.ciicsh.gto.afsupportcenter.util.logService.LogMessage;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;



public class WordUtils {

    @Autowired
    private static LogApiUtil logApiUtil;

    private static Configuration configuration = null;
    static {
        try {
            logApiUtil.error(LogMessage.create().setTitle("WordUtils.static").setContent("Configuration load start"));
            configuration = new Configuration();
            configuration.setDefaultEncoding("utf-8");

            // 以上方式不行 只能在本地拿到模板  发布到linux上去 打成jar包 要以这种方式读取模板
            ClassPathResource classPathResource = new ClassPathResource("template");

            File file = classPathResource.getFile();

            configuration.setDirectoryForTemplateLoading(file);

        } catch (Exception e) {
            e.printStackTrace();
            logApiUtil.error(LogMessage.create().setTitle("WordUtils.static").setContent(e.getMessage()));
        }
    }

    private WordUtils() {
        throw new AssertionError();
    }

    public static void exportMillCertificateWord(HttpServletRequest request, HttpServletResponse response, Map map,String title,String ftlFile) throws IOException {

        try {
            Template freemarkerTemplate = configuration.getTemplate(ftlFile);

            response.setCharacterEncoding("utf-8");
            response.setContentType("application/msword");
            // 设置浏览器以下载的方式处理该文件名
            String fileName = title+ StringUtil.getDateString(new Date()) + ".doc";
            response.setHeader("Content-Disposition", "attachment;filename="
                .concat(String.valueOf(URLEncoder.encode(fileName, "UTF-8"))));
            Writer w = new OutputStreamWriter(response.getOutputStream(), "utf-8");
            freemarkerTemplate.process(map,w);
            w.close();
        }catch (Exception ex) {
            logApiUtil.error(LogMessage.create().setTitle("WordUtils.exportMillCertificateWord").setContent(ex.getMessage()));
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

}
