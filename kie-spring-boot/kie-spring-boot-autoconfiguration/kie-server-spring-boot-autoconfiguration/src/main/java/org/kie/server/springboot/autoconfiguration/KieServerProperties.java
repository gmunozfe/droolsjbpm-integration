/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.springboot.autoconfiguration;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.kie.server.springboot.EmbeddedKieJar;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kieserver")
public class KieServerProperties implements InitializingBean {
    
    private static final String PROPERTY_PREFIX = "org.kie.server.";

    private String location = "";
    private String controllers = "";
    private String serverId = "SpringBoot";
    private String serverName = "KieServer-SpringBoot";
    private boolean classPathContainer;

    private Swagger swagger = new Swagger();
    
    private List<EmbeddedKieJar> deployments;

    private Map<String, String> addons = new HashMap<>(); 

    
    public List<EmbeddedKieJar> getDeployments() {
        if(deployments == null) {
            return Collections.emptyList();
        }
        return deployments;
    }

    public void setDeployments(List<EmbeddedKieJar> deployments) {
        this.deployments = deployments;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getControllers() {
        return controllers;
    }

    public void setControllers(String controllers) {
        this.controllers = controllers;
    }

    public Swagger getSwagger() {
        return swagger;
    }

    public void setSwagger(Swagger swagger) {
        this.swagger = swagger;
    }

    public Map<String, String> getAddons() {
        return addons;
    }
    
    public void setAddons(Map<String, String> addons) {
        this.addons = addons;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        if (!this.addons.isEmpty()) {
            for (Entry<String, String> entry : this.addons.entrySet()) {
                System.setProperty(PROPERTY_PREFIX + entry.getKey(), entry.getValue());
            }
        }
    }


    public boolean isClassPathContainer() {
        return classPathContainer;
    }

    public void setClassPathContainer(boolean classPathContainer) {
        this.classPathContainer = classPathContainer;
    }


    public static class Swagger {

        private boolean enabled = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

}
