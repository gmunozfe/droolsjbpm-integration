/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.router.identity;

import java.security.Principal;
import java.util.Collections;
import java.util.Set;

import io.undertow.security.idm.Account;


public class KieServerInstanceAccount implements Account {

    private static final long serialVersionUID = 1L;

    private KieServerInstancePrincipal principal;

    private String hash;

    public KieServerInstanceAccount(KieServerInstancePrincipal principal, String hash) {
        this.principal = principal;
        this.hash = hash;
    }

    public KieServerInstanceAccount(String user, String password) {
        this(new KieServerInstancePrincipal(user), password);
    }

    @Override
    public Principal getPrincipal() {
        return principal;
    }

    @Override
    public Set<String> getRoles() {
        return Collections.emptySet();
    }

    public String getHash() {
        return hash;
    }

}
