/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Spliterator.NONNULL;
import static java.util.stream.Collectors.collectingAndThen;

public class KieServerConfigProviderLoader {

    private static final List<KieServerConfigProvider> PROVIDERS;
    private static final List<KieServerConfigItem> ITEMS;

    private KieServerConfigProviderLoader() {
    }

    static {
        final ServiceLoader<KieServerConfigProvider> serviceLoader = ServiceLoader.load(KieServerConfigProvider.class);
        PROVIDERS = StreamSupport.stream(Spliterators.spliteratorUnknownSize(serviceLoader.iterator(), NONNULL), false)
                .collect(collectingAndThen(Collectors.toList(), Collections::unmodifiableList));

        ITEMS = PROVIDERS.stream()
                .map(KieServerConfigProvider::getItems)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    public static List<KieServerConfigProvider> getConfigProviders() {
        return PROVIDERS;
    }

    public static List<KieServerConfigItem> getConfigItems() {
        return ITEMS;
    }
}
