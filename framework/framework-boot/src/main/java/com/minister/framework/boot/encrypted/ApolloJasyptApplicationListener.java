package com.minister.framework.boot.encrypted;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * 解决apollo和Jasypt冲突导致无法热更新问题
 * 通过 @Value 拿到的值是已解密的
 * 通过 @ApolloConfigChangeListener 拿到的值是未解密的
 *
 * @author QIUCHANGQING620
 * @date 2020-11-26 10:43
 */
@Component
@Slf4j
@SuppressWarnings("rawtypes")
public class ApolloJasyptApplicationListener implements ApplicationListener<ApplicationEvent> {

    public static final Set<String> APOLLO_PROPERTY_SOURCE_NAMES = new HashSet<String>() {{
        add("ApolloBootstrapPropertySources");
        add("ApolloPropertySources");
    }};

    public static final String ENCRYPTABLE_DELEGATE_FIELD_NAME = "encryptableDelegate";

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationStartedEvent) {
            ConfigurableEnvironment environment = ((ApplicationStartedEvent) event).getApplicationContext().getEnvironment();
            final MutablePropertySources mps = environment.getPropertySources();
            mps.stream().forEach(ps -> {
                if (APOLLO_PROPERTY_SOURCE_NAMES.contains(ps.getName())) {
                    // EncryptablePropertySourceConverter.instantiatePropertySource
                    Field field = ReflectionUtils.findField(ps.getClass(), ENCRYPTABLE_DELEGATE_FIELD_NAME);
                    if (field != null) {
                        field.setAccessible(true);
                        EncryptablePropertySource<?> encryptableDelegate = (EncryptablePropertySource) ReflectionUtils.getField(field, ps);
                        if (!(encryptableDelegate instanceof ApolloEncryptablePropertySourceWrapper)) {
                            ReflectionUtils.setField(field, ps, new ApolloEncryptablePropertySourceWrapper<>(encryptableDelegate));
                        }
                    }
                }
            });
        }
    }

    private static class ApolloEncryptablePropertySourceWrapper<T> implements EncryptablePropertySource<T> {

        private final EncryptablePropertySource<T> delegate;

        public ApolloEncryptablePropertySourceWrapper(EncryptablePropertySource<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public PropertySource<T> getDelegate() {
            return delegate.getDelegate();
        }

        @Override
        public Object getProperty(String name) {
            // 每次取值前都清空缓存, 因为apollo是支持@Value都这种自动更新,但是jasypt有缓存, 只有第一次会取值,
            // 取完就缓存了, 所以要把 jasypt缓存清空
            // https://github.com/ctripcorp/apollo/issues/2170
            delegate.refresh();
            return delegate.getProperty(name);
        }

        @Override
        public void refresh() {
            delegate.refresh();
        }

    }

}
