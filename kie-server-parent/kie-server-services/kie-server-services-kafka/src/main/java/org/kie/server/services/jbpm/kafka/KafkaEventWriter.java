/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.kie.server.services.jbpm.kafka;

import java.io.IOException;

import org.kie.api.runtime.process.ProcessInstance;

/** Implements the logic to convert an object into the byte[] being published into a Kafka Record 
 */
public interface KafkaEventWriter {

    /**
     * Converts from object to byte[] 
     * @param processInstance Contains process instance information that might be needed when building the event
     * @param value object to be converted into byte[]
     * @return byte[] generated from object
     * @throws IOException if any input output exception occurs
     */
    byte[] writeEvent(ProcessInstance processInstance, Object value) throws IOException;
}
