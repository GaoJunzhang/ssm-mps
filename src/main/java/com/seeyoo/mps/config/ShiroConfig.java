package com.seeyoo.mps.config;

import com.seeyoo.mps.integration.shiro.ShiroAuthRealm;
import com.seeyoo.mps.integration.shiro.ShiroExceptionHandler;
import com.seeyoo.mps.integration.shiro.ShiroRedisSessionDao;
import com.seeyoo.mps.integration.shiro.ShiroSessionManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
public class ShiroConfig {


    @Bean
    public ShiroFilterChainDefinition shiroFilterChainDefinition() {
        DefaultShiroFilterChainDefinition chain = new DefaultShiroFilterChainDefinition();
        chain.addPathDefinition("/auth/login", "anon");
        chain.addPathDefinition("/kaptcha/**", "anon");
        chain.addPathDefinition("/druid/**", "anon");
        chain.addPathDefinition("/swagger-resources", "anon");
        chain.addPathDefinition("/v2/api-docs", "anon");
        chain.addPathDefinition("/v2/api-docs-ext", "anon");
        chain.addPathDefinition("/doc.html", "anon");
        chain.addPathDefinition("/webjars/**", "anon");
        chain.addPathDefinition("/**", "authc");
        return chain;
    }

    @Bean
    public static DefaultAdvisorAutoProxyCreator getDefaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator creator = new DefaultAdvisorAutoProxyCreator();
        creator.setUsePrefix(true);
        return creator;
    }

    @Bean
    public Realm realm() {
        return new ShiroAuthRealm();
    }

    @Bean
    public SessionManager sessionManager(){
        ShiroSessionManager shiroSessionManager = new ShiroSessionManager();
        shiroSessionManager.setGlobalSessionTimeout(6000*1000);
        shiroSessionManager.setSessionDAO(redisSessionDao());
        return shiroSessionManager;
    }


    @Bean
    public ShiroRedisSessionDao redisSessionDao(){
        return new ShiroRedisSessionDao();
    }

    @Bean(name = "exceptionHandler")
    public HandlerExceptionResolver handlerExceptionResolver() {
        return new ShiroExceptionHandler();
    }
}

