package com.minister.component.elasticsearch.config;

import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import com.minister.component.elasticsearch.properties.ElasticProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * ES 客户端连接配置
 *
 * @author QIUCHANGQING620
 * @date 2024-02-13 17:38
 */
@Slf4j
@Configuration
public class ElasticsearchConfiguration {

    @Bean(name = "restHighLevelClient")
    public RestHighLevelClient createElasticClient(@Autowired ElasticProperties elasticProperties) {
        final CredentialsProvider credentialsProvider;
        if (StringUtils.isEmpty(elasticProperties.getUsername()) || StringUtils.isEmpty(elasticProperties.getPassword())) {
            credentialsProvider = null;
        } else {

            credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(elasticProperties.getUsername(), elasticProperties.getPassword()));
        }

        RestClientBuilder restClientBuilder = RestClient.builder(getClusterNodes(elasticProperties.getClusterNodes()))
                .setHttpClientConfigCallback(httpClientBuilder -> {
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    // httpclient保活策略
                    httpClientBuilder.setKeepAliveStrategy(((response, context) -> Duration.ofMinutes(elasticProperties.getKeepAliveStrategy()).toMillis()));
                    return httpClientBuilder;
                });
        // 设置超时时间
        log.info("int Elasticsearch clusterNodes {}", elasticProperties.getClusterNodes());
        return new RestHighLevelClient(restClientBuilder);
    }


    private HttpHost[] getClusterNodes(String clusterNodes) {
        Assert.hasText(clusterNodes, "Cluster nodes source must not be null or empty!");
        String[] nodes = StrUtil.splitToArray(clusterNodes, StrPool.COMMA);
        List<HttpHost> httpHosts = new ArrayList<>();
        for (String node : nodes) {
            String[] segments = StrUtil.splitToArray(node, StrPool.COLON);
            Assert.isTrue(segments.length == 2,
                    String.format("Invalid cluster node %s in %s! Must be in the format host:port!", node, clusterNodes));
            String host = segments[0].trim();
            String port = segments[1].trim();
            Assert.hasText(host, String.format("No host name given cluster node %s!", node));
            Assert.hasText(port, String.format("No port given in cluster node %s!", node));
            httpHosts.add(new HttpHost(toInetAddress(host), Integer.parseInt(port)));
        }
        HttpHost[] hosts = new HttpHost[httpHosts.size()];
        httpHosts.toArray(hosts);
        return hosts;

    }

    private static InetAddress toInetAddress(String host) {
        try {
            return InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
