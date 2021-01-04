package com.dah.cem.app.sc.worker.config;

import com.github.pagehelper.PageInterceptor;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.aspectj.lang.annotation.Aspect;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.*;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Aspect
@Configuration
@MapperScan(basePackages = "com.dah.cem.app.sc.worker.domain", annotationClass = Mapper.class, sqlSessionTemplateRef = "sqlSessionTemplateTarget")
public class MybatisConfig {

    private static final int TX_METHOD_TIMEOUT = 60;
    private static final String AOP_POINTCUT_EXPRESSION = "@within(org.springframework.stereotype.Service) || @within(org.springframework.stereotype.Repository)";

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.dah-target")
    public DataSource datasourceTarget() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    public SqlSessionFactory sqlSessionFactoryTarget() throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(datasourceTarget());
        sqlSessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:com/dah/cem/app/sc/worker/domain/**/*Mapper.xml"));
        sqlSessionFactoryBean.setPlugins(pageInterceptorTarget());
        return sqlSessionFactoryBean.getObject();
    }

    @Bean
    public PageInterceptor pageInterceptorTarget() {
        PageInterceptor pageInterceptor = new PageInterceptor();
        Properties properties = new Properties();
        properties.setProperty("offsetAsPageNum", "true");
        properties.setProperty("rowBoundsWithCount", "true");
        properties.setProperty("reasonable", "true");
        pageInterceptor.setProperties(properties);
        return pageInterceptor;
    }

    @Bean
    public SqlSessionTemplate sqlSessionTemplateTarget() throws Exception {
        return new SqlSessionTemplate(sqlSessionFactoryTarget());
    }

    @Bean
    public PlatformTransactionManager transactionManagerTarget() {
        return new DataSourceTransactionManager(datasourceTarget());
    }

    @Bean
    public TransactionInterceptor txAdviceTarget() {
        NameMatchTransactionAttributeSource tas = new NameMatchTransactionAttributeSource();
        // readOnlyTx
        RuleBasedTransactionAttribute readOnlyTx = new RuleBasedTransactionAttribute();
        readOnlyTx.setReadOnly(true);
        readOnlyTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
        // requiredTx
        RuleBasedTransactionAttribute requiredTx = new RuleBasedTransactionAttribute();
        requiredTx.setRollbackRules(Collections.singletonList(new RollbackRuleAttribute(Exception.class)));
        requiredTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        requiredTx.setTimeout(TX_METHOD_TIMEOUT);
        Map<String, TransactionAttribute> txMap = new HashMap<>();
        txMap.put("get*", readOnlyTx);
        txMap.put("*", requiredTx);
        tas.setNameMap(txMap);
        return new TransactionInterceptor(transactionManagerTarget(), tas);
    }

    @Bean
    public Advisor txAdviceAdvisorTarget() {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression(AOP_POINTCUT_EXPRESSION);
        return new DefaultPointcutAdvisor(pointcut, txAdviceTarget());
    }
}
