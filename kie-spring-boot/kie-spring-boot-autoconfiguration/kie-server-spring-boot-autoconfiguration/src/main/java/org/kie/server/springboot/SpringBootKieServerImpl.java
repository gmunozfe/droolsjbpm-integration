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

package org.kie.server.springboot;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.kie.internal.identity.IdentityProvider;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.impl.storage.KieServerStateRepository;


public class SpringBootKieServerImpl extends KieServerImpl {

    private IdentityProvider identityProvider;
    private List<KieServerExtension> extensions;
    

    public SpringBootKieServerImpl(List<KieServerExtension> extensions, IdentityProvider identityProvider, KieServerStateRepository stateRepository) {
        super(stateRepository);
        this.extensions = extensions;
        this.identityProvider = identityProvider;
    }

    public SpringBootKieServerImpl(List<KieServerExtension> extensions, IdentityProvider identityProvider) {
        this.extensions = extensions;
        this.identityProvider = identityProvider;
    }
    
    @Override
    protected List<KieServerExtension> sortKnownExtensions() {
        getServerRegistry().registerIdentityProvider(identityProvider);
        Collections.sort(extensions, new Comparator<KieServerExtension>() {
            @Override
            public int compare(KieServerExtension e1, KieServerExtension e2) {
                return e1.getStartOrder().compareTo(e2.getStartOrder());
            }
        });
        return extensions;
    }

    @Override
    public void init() {
        super.init();
    }


}
