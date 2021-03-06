package com.asquera.elasticsearch.plugins.http;

import static org.elasticsearch.rest.RestStatus.UNAUTHORIZED;
import net.jalg.hawkj.Algorithm;
import net.jalg.hawkj.AuthHeaderParsingException;
import net.jalg.hawkj.AuthorizationHeader;
import net.jalg.hawkj.HawkContext;
import net.jalg.hawkj.HawkWwwAuthenticateContext;
import net.jalg.hawkj.HawkWwwAuthenticateContext.HawkWwwAuthenticateContextBuilder;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;

public class HttpHawkAuthRestFilter extends AbstractAuthRestFilter {

    protected Algorithm algorithm;

    protected HttpHawkAuthRestFilter(Settings settings) {
        super(settings);
        String algorithmStr = settings.get("http.hawk.algorithm", "sha256");
        this.algorithm = Algorithm.fromString(algorithmStr);
        if (this.algorithm == null) {
            this.algorithm = Algorithm.SHA_256;
        }
    }

    @Override
    protected void sendAuthenticationChallenge(RestRequest request, RestChannel channel) {
        String addr = getAddress(request);
        logger.error("UNAUTHORIZED type:{}, address:{}, path:{}, request:{}, content:{}",
                request.method(), addr, request.path(), request.params(), request.content().toUtf8());

/*        long curTime = System.currentTimeMillis() / 1000L;
        StringBuilder sb = new StringBuilder(64);
        sb.append("Hawk ts=\"");
        sb.append(String.valueOf(curTime));
        sb.append("\" tsm=\"");
        sb.append(Hawk.calculateTSMac(curTime));
        sb.append('"');
        HawkWwwAuthenticateContext wwwAuth = HawkWwwAuthenticateContext.ts()
        			.credentials(this.user, this.password, this.algorithm).build();
        wwwAuth.createWwwAuthenticateHeader();
        BytesRestResponse response = new BytesRestResponse(UNAUTHORIZED, sb.toString());
*/

        BytesRestResponse response = new BytesRestResponse(UNAUTHORIZED, "Authentication Required");
        response.addHeader("WWW-Authenticate", "Basic realm=\"Restricted\"");
        channel.sendResponse(response);
    }

    @Override
    protected boolean isAuthenticated(RestRequest request) {
        String authHeaderStr = request.header("Authorization");
        if (authHeaderStr == null) {
            return false;
        }
        try {
            AuthorizationHeader authHeader = AuthorizationHeader.authorization(authHeaderStr);
            String[] hostAndPort = super.getHostAndPort(request).split(":");
            int port;
            if (hostAndPort.length == 2) {
                port = Integer.parseInt(hostAndPort[1]);
            } else {
                port = 80; // TODO if transport is secure then use 443
            }

            HawkContext hawk = HawkContext.request(
                    request.method().toString(),
                    request.uri(),
                    hostAndPort[0],
                    port)
                .credentials(this.user, this.password, this.algorithm)
                .tsAndNonce(authHeader.getTs(), authHeader.getNonce())
                .hash(authHeader.getHash())
                .build();
            return hawk.isValidMac(authHeader.getMac());
        } catch (AuthHeaderParsingException ex) {
            return false;
        }
    }

}
