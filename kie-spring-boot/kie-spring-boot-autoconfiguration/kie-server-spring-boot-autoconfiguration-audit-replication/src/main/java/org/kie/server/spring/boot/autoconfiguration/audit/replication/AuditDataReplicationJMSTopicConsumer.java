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

package org.kie.server.spring.boot.autoconfiguration.audit.replication;

import javax.persistence.EntityManagerFactory;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.transaction.annotation.Transactional;

public class AuditDataReplicationJMSTopicConsumer extends AbstractAuditDataReplicationJMSConsumer {

    public AuditDataReplicationJMSTopicConsumer(EntityManagerFactory emf) {
        super(emf);
    }

    @JmsListener(destination = "${kieserver.audit-replication.topic}", subscription = "${kieserver.audit-replication.topic.subscriber}")
    @Transactional(rollbackFor = Exception.class)
    public void receiveMessage(Object message) {
        processMessage(message);
    }

}
