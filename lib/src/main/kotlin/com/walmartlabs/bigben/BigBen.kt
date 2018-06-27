/*-
 * #%L
 * BigBen:lib
 * =======================================
 * Copyright (C) 2016 - 2018 Walmart Inc.
 * =======================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.walmartlabs.bigben

import com.walmartlabs.bigben.api.EventReceiver
import com.walmartlabs.bigben.api.EventService
import com.walmartlabs.bigben.core.ScheduleScanner
import com.walmartlabs.bigben.entities.EntityProvider
import com.walmartlabs.bigben.entities.EventLoader
import com.walmartlabs.bigben.processors.MessageProcessor
import com.walmartlabs.bigben.processors.MessageProducerFactory
import com.walmartlabs.bigben.processors.ProcessorRegistry
import com.walmartlabs.bigben.utils.hz.ClusterSingleton
import com.walmartlabs.bigben.utils.hz.Hz
import com.walmartlabs.bigben.utils.logger
import com.walmartlabs.bigben.utils.commons.Props.exists
import com.walmartlabs.bigben.utils.commons.Props.string

/**
 * Created by smalik3 on 6/24/18
 */
object BigBen {
    private val l = logger<BigBen>()

    val eventService: EventService
    val eventReceiver: EventReceiver
    val entityProvider: EntityProvider<Any>
    val eventLoader: EventLoader
    val processorRegistry: ProcessorRegistry
    val hz: Hz
    val messageProducerFactory: MessageProducerFactory
    private val messageProcessor: MessageProcessor?

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> entityProvider() = entityProvider as EntityProvider<T>

    init {
        if (System.getProperty("props") == null) {
            l.warn("missing 'props' system property, using the default one: bigben.yaml")
            System.setProperty("props", "file:///bigben.yaml")
        }
        l.info("initializing entity provider")
        @Suppress("UNCHECKED_CAST")
        entityProvider = Class.forName(string("domain.entity.provider.class")).newInstance() as EntityProvider<Any>
        l.info("initializing event loader")
        eventLoader = if (entityProvider is EventLoader) entityProvider else Class.forName(string("domain.event.loader.class")).newInstance() as EventLoader
        l.info("loading processors")
        processorRegistry = ProcessorRegistry()
        l.info("initializing hazelcast")
        hz = Hz()
        l.info("initializing schedule scanner")
        val service = ScheduleScanner(hz)
        l.info("initializing cluster master")
        ClusterSingleton(service, hz)
        l.info("initializing event receiver")
        eventReceiver = EventReceiver(hz)
        l.info("initializing event service")
        eventService = EventService(hz, service, eventReceiver)
        l.info("initializing message producer factory")
        messageProducerFactory = Class.forName(string("messaging.producer.factory.class")).newInstance() as MessageProducerFactory
        messageProcessor = if (exists("messaging.processor.class")) {
            Class.forName(string("messaging.processor.class")).newInstance() as MessageProcessor
        } else null
        l.info("BigBen initialized successfully")
    }
}
