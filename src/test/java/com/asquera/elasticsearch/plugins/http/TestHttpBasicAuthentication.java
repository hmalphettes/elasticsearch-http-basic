package com.asquera.elasticsearch.plugins.http;

import static org.hamcrest.Matchers.equalTo;

import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.rest.helper.HttpClient;
import org.elasticsearch.rest.helper.HttpClientResponse;
import org.elasticsearch.test.ElasticsearchIntegrationTest;
import org.elasticsearch.test.ElasticsearchIntegrationTest.ClusterScope;
import org.elasticsearch.test.ElasticsearchIntegrationTest.Scope;
import org.junit.Test;

/**
 * Copied from elasticsearch tests: org.elasticsearch.plugin.ResponseHeaderPluginTest
 * then edited for this
 */
@ClusterScope(scope= Scope.SUITE, numDataNodes =1)
public class TestHttpBasicAuthentication extends ElasticsearchIntegrationTest {

    @Override
    protected Settings nodeSettings(int nodeOrdinal) {
        return ImmutableSettings.settingsBuilder()
        		.put("plugin.types", HttpAuthenticationPlugin.class.getName())
                .put("force.http.enabled", true)
                .put("http.basic.user", "Aladdin")
                .put("http.basic.password", "open sesame")
                .put("http.basic.ipwhitelist", new String[] { "192.169.12.12" })
                .put(super.nodeSettings(nodeOrdinal))
                .build();
    }

    @Test
    public void testHttpBasicAuth() throws Exception {
        ensureGreen();

        HttpClientResponse response = httpClient().request("/_status");
        assertThat(response.errorCode(), equalTo(RestStatus.UNAUTHORIZED.getStatus()));
        assertThat(response.getHeader("WWW-Authenticate"), equalTo("Basic realm=\"Restricted\""));

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ=="); // As seen on Wikipedia
        HttpClientResponse authResponse = httpClient().request("GET", "/_status", headers);
        assertThat(authResponse.errorCode(), equalTo(RestStatus.OK.getStatus()));
    }

    private HttpClient httpClient() {
    	return new HttpClient(cluster().httpAddresses()[0]);
    }

}
