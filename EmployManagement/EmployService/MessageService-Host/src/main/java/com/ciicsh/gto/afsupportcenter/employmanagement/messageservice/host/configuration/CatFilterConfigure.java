package com.ciicsh.gto.afsupportcenter.employmanagement.messageservice.host.configuration;


import com.ciicsh.gto.afsupportcenter.employmanagement.messageservice.host.interceptor.CatInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author yuyong
 * @date 2018/1/8
 */
@Configuration
public class CatFilterConfigure extends WebMvcConfigurerAdapter {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CatInterceptor()).addPathPatterns("/**");
        super.addInterceptors(registry);
    }

}
