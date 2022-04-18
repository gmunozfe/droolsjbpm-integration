/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.router.proxy;

import org.jboss.logging.Logger;


import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient;

public class CaptureHostLoadBalancingProxyClient extends LoadBalancingProxyClient {
	
	protected static final Logger log = Logger.getLogger(CaptureHostLoadBalancingProxyClient.class);
    private String uri = null;
    @Override
    protected Host selectHost(HttpServerExchange exchange) {
    	
    	log.info("@@@>>> select host input "+exchange);
        Host host = super.selectHost(exchange);

        if (host != null) {
            uri = host.getUri().toString();
        }

        log.info("@@@>>> selectHost uri "+uri);
        log.info("@@@>>> selectHost return "+host);
        return host;
    }

    public String getUri() {
        return uri;
    }

    public void clear() {
       uri = null;
    }
}
