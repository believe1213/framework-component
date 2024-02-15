package com.minister.framework.boot.config;

import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.extra.spring.EnableSpringUtil;
import com.minister.component.trace.utils.ThreadPoolUtil;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import static com.minister.component.utils.constants.Constants.ASTERISK;

/**
 * BeanConfigurer
 *
 * @author QIUCHANGQING620
 * @date 2020-03-07 13:04
 */
@Configuration
@EnableSpringUtil
@EnableScheduling
public class BeanConfigurer {

    private static final String TASK_SCHEDULER_PREFIX = "scheduler-";

    @Value("${framework-boot.task-scheduler.pool-size:20}")
    private Integer taskSchedulerPoolSize;

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler executor = new ThreadPoolUtil.ThreadPoolTaskSchedulerMDCWrapper();
        executor.setThreadNamePrefix(TASK_SCHEDULER_PREFIX);
        executor.setPoolSize(taskSchedulerPoolSize);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }

    private static final String TASK_EXECUTOR_PREFIX = "task-";

    @Value("${framework-boot.task-executor.keep-alive-seconds:300}")
    private Integer taskExecutorKeepAliveSeconds;

    @Bean
    @Primary
    public TaskExecutor taskExecutor() {
        int core = RuntimeUtil.getProcessorCount();

        ThreadPoolTaskExecutor executor = new ThreadPoolUtil.ThreadPoolTaskExecutorMDCWrapper();
        executor.setThreadNamePrefix(TASK_EXECUTOR_PREFIX);
        // 核心线程数(线程池维护线程的最少数量，即使没有任务需要执行，也会一直存活)
        executor.setCorePoolSize(core);
        // 最大线程数
        executor.setMaxPoolSize(core * 4);
        // 队列最大长度
        executor.setQueueCapacity(core * 128);
        // 线程池维护线程所允许空闲时间，当线程空闲时间达到时，线程会退出，直到线程数量=corePoolSize
        executor.setKeepAliveSeconds(taskExecutorKeepAliveSeconds);
        // AbortPolicy 默认策略。使用该策略时，如果线程池队列满了丢掉这个任务并且抛出 RejectedExecutionException 异常
        // DiscardPolicy 如果线程池队列满了，会直接丢掉这个任务并且不会有任何异常
        // DiscardOldestPolicy 如果队列满了，会将最早进入队列的任务删掉腾出空间，再尝试加入队列。因为队列是队尾进，队头出，所以队头元素是最老的，因此每次都是移除对头元素后再尝试入队
        // CallerRunsPolicy 如果添加到线程池失败，那么主线程会自己去执行该任务，不会等待线程池中的线程去执行。
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }

    @Value("${framework-boot.cors-filter.allowed-origin-list:*}")
    private Set<String> allowedOriginList;

    /**
     * 处理跨域问题
     */
    @Bean
    @ConditionalOnProperty(name = "framework-boot.cors-filter.enable", havingValue = "true", matchIfMissing = true)
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // 值为当前页面所在的域，用于告诉服务器当前请求的域
        allowedOriginList.forEach(corsConfiguration::addAllowedOrigin);
        corsConfiguration.addAllowedHeader(ASTERISK);
        corsConfiguration.addAllowedMethod(ASTERISK);
        // 服务器是否允许使用 cookies
        corsConfiguration.setAllowCredentials(false);
        corsConfiguration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsFilter(source);
    }

    @Value("${framework-boot.customized-encryptor.password:123456}")
    private String password;

    @Value("${framework-boot.customized-encryptor.algorithm:PBEWITHHMACSHA512ANDAES_256}")
    private String algorithm;

    @Value("${framework-boot.customized-encryptor.customized-encryptor.key-obtention-iterations:1000}")
    private String keyObtentionIterations;

    @Value("${framework-boot.customized-encryptor.pool-size:1}")
    private String poolSize;

    @Value("${framework-boot.customized-encryptor.provider-name:SunJCE}")
    private String providerName;

    @Value("${framework-boot.customized-encryptor.salt-generator-classname:org.jasypt.salt.RandomSaltGenerator}")
    private String saltGeneratorClassName;

    @Value("${framework-boot.customized-encryptor.iv-generator-classname:org.jasypt.iv.RandomIvGenerator}")
    private String ivGeneratorClassName;

    @Value("${framework-boot.customized-encryptor.string-output-type:base64}")
    private String stringOutputType;

    /**
     * 配置项加解密配置
     */
    @Bean(name = "customizedStringEncryptor")
    public StringEncryptor stringEncryptor() {

        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(password);
        // 算法
        config.setAlgorithm(algorithm);
        config.setKeyObtentionIterations(keyObtentionIterations);
        config.setPoolSize(poolSize);
        config.setProviderName(providerName);
        config.setSaltGeneratorClassName(saltGeneratorClassName);
        config.setIvGeneratorClassName(ivGeneratorClassName);
        config.setStringOutputType(stringOutputType);
        encryptor.setConfig(config);
        return encryptor;
    }

}
