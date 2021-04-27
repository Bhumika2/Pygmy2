/**
 * Copyright (C) the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * Copyright (C) 2013 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * Copyright (C) 2013 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Copyright (C) 2013 the original author or authors.
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

package controllers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import models.CatalogResponse;
import models.OrderRequest;
import models.OrderResponse;
import ninja.Result;
import ninja.Results;
import ninja.params.PathParam;
import ninja.utils.NinjaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.Catalog;
import service.Health;
import service.Order;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;

@Singleton
public class ApplicationController {
    private final int CACHE_SIZE = 3;
    LinkedHashMap<Integer, CatalogResponse> lookupCache;
    LinkedHashMap<String, List<CatalogResponse>> searchCache;
    boolean catalogSwitch;
    boolean orderSwitch;
    Logger logger = LoggerFactory.getLogger("Pygmy");
    /**
     * search is the microservice endpoint that invokes searchTopic method in Catalog class under
     * service package to handle the request. It is invoked from client and returns books corresponding to topic.
     */
    @Inject
    NinjaProperties ninjaProperties;
    public ApplicationController() {
        this.catalogSwitch = true;
        this.orderSwitch = true;
        this.lookupCache = new LinkedHashMap<>(CACHE_SIZE);
        this.searchCache = new LinkedHashMap<>(CACHE_SIZE);

        Runnable checkHealth =
                new Runnable() {
                    public void run() {
                        Health health = new Health();
                        health.checkSystemHealth();
                    }
                };
        Thread thread = new Thread(checkHealth);
        thread.start();
    }

    public Result search(@PathParam("topic") String topic) {
        logger.info("Search request received for topic: " + topic);
        long startTime = System.nanoTime();
        List<CatalogResponse> catalogResponse = null;
        String key = null;
        try {
            key = URLDecoder.decode(topic, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        if (searchCache.containsKey(key)) {
            catalogResponse = searchCache.get(key);
            searchCache.remove(key);
            searchCache.put(key, catalogResponse);
            logger.info("Returning result from search cache");
        } else {
            Catalog catalog = new Catalog(ninjaProperties);
            if (Health.getCatalog1State().equals("RUNNING") && (catalogSwitch || Health.getCatalog2State().equals("FAILED"))) {
                catalogResponse = catalog.searchTopic(topic, ninjaProperties.get("catalogHost"), ninjaProperties.get("catalogPort"));
                if (catalogResponse == null) {
                    catalogResponse = catalog.searchTopic(topic, ninjaProperties.get("catalogReplicaHost"), ninjaProperties.get("catalogReplicaPort"));
                }
            } else {
                catalogResponse = catalog.searchTopic(topic, ninjaProperties.get("catalogReplicaHost"), ninjaProperties.get("catalogReplicaPort"));
                if (catalogResponse == null) {
                    catalogResponse = catalog.searchTopic(topic, ninjaProperties.get("catalogHost"), ninjaProperties.get("catalogPort"));
                }
            }
            this.catalogSwitch = !this.catalogSwitch;

            if (catalogResponse != null) {
                if (searchCache.size() == CACHE_SIZE) {
                    String LRUKey = searchCache.entrySet().iterator().next().getKey();
                    searchCache.remove(LRUKey);
                }
                searchCache.put(key, catalogResponse);
            }
        }
        long timeElapsed = System.nanoTime() - startTime;
        logger.info("Search response time in milliseconds : " + timeElapsed / 1000000);
        return Results.json().render(catalogResponse);

    }

    /**
     * lookup is the microservice endpoint that invokes lookupBook method in Catalog class under
     * service package to handle the request. It is invoked from client and returns book corresponding to number.
     */
    public Result lookup(@PathParam("bookNumber") Integer bookNumber) {
        logger.info("Lookup request received for item: " + bookNumber);
        long startTime = System.nanoTime();
        CatalogResponse catalogResponse = null;
        if (lookupCache.containsKey(bookNumber)) {
            catalogResponse = lookupCache.get(bookNumber);
            lookupCache.remove(bookNumber);
            lookupCache.put(bookNumber, catalogResponse);
            logger.info("Returning result from lookup cache");
        } else {
            Catalog catalog = new Catalog(ninjaProperties);
            if (Health.getCatalog1State().equals("RUNNING") && (catalogSwitch || Health.getCatalog2State().equals("FAILED"))) {
                catalogResponse = catalog.lookupBook(bookNumber, ninjaProperties.get("catalogHost"), ninjaProperties.get("catalogPort"));
                if (catalogResponse == null) {
                    catalogResponse = catalog.lookupBook(bookNumber, ninjaProperties.get("catalogReplicaHost"), ninjaProperties.get("catalogReplicaPort"));
                }
            } else {
                catalogResponse = catalog.lookupBook(bookNumber, ninjaProperties.get("catalogReplicaHost"), ninjaProperties.get("catalogReplicaPort"));
                if (catalogResponse == null) {
                    catalogResponse = catalog.lookupBook(bookNumber, ninjaProperties.get("catalogHost"), ninjaProperties.get("catalogPort"));
                }
            }
            this.catalogSwitch = !this.catalogSwitch;

            if (catalogResponse != null) {
                if (lookupCache.size() == CACHE_SIZE) {
                    Integer LRUKey = lookupCache.entrySet().iterator().next().getKey();
                    lookupCache.remove(LRUKey);
                }
                lookupCache.put(bookNumber, catalogResponse);
            }
        }
        long timeElapsed = System.nanoTime() - startTime;
        logger.info("Lookup response time in milliseconds : " + timeElapsed / 1000000);
        return Results.json().render(catalogResponse);
    }

    public Result invalidate(@PathParam("bookNumber") Integer bookNumber) {
        logger.info("Invalidate Cache entry for item: " + bookNumber);
        OrderResponse res = invalidateCacheEntry(bookNumber);
        return Results.json().render(res);
    }

    /**
     * invalidateCacheEntry removes the cache entry corresponding to bookNumber in lookup and search cache.
     * It is invoked from application controller and invoked from order server on successful buy request
     */
    public OrderResponse invalidateCacheEntry(int bookNumber) {
        OrderResponse orderResponse = new OrderResponse();
        try {
            CatalogResponse lookupCacheEntry = lookupCache.get(bookNumber);
            if (lookupCacheEntry != null) {
                List<CatalogResponse> searchCacheEntry = searchCache.get(lookupCacheEntry.getTopic());
                lookupCache.remove(bookNumber);
                if (searchCacheEntry != null)
                    searchCache.remove(lookupCacheEntry.getTopic());
            }
            orderResponse.setBookNumber(bookNumber);
            orderResponse.setMessage("Cache Entry Invalidated");
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        return orderResponse;
    }

    /**
     * buy is the microservice endpoint that invokes buyBook method in Catalog class under
     * service package to handle the request. It is invoked from client and returns message about the purchase status.
     */
    public Result buy(@PathParam("bookNumber") Integer bookNumber) {
        logger.info("Buy request received for item:" + bookNumber);
        long startTime = System.nanoTime();
        Order order = new Order(ninjaProperties);
        OrderRequest orderRequest = new OrderRequest(bookNumber);
        OrderResponse orderResponse = null;
        if (Health.getOrder1State().equals("RUNNING") && Health.getCatalog1State().equals("RUNNING")
                && (orderSwitch || Health.getOrder2State().equals("FAILED") || Health.getCatalog2State().equals("FAILED"))) {
            orderResponse = order.buyBook(orderRequest, ninjaProperties.get("orderHost"), ninjaProperties.get("orderPort"));
            if (orderResponse == null) {
                orderResponse = order.buyBook(orderRequest, ninjaProperties.get("orderReplicaHost"), ninjaProperties.get("orderReplicaPort"));
            }
        } else {
            orderResponse = order.buyBook(orderRequest, ninjaProperties.get("orderReplicaHost"), ninjaProperties.get("orderReplicaPort"));
            if (orderResponse == null) {
                orderResponse = order.buyBook(orderRequest, ninjaProperties.get("orderHost"), ninjaProperties.get("orderPort"));
            }
        }
        this.orderSwitch = !this.orderSwitch;
        long timeElapsed = System.nanoTime() - startTime;
        logger.info("Order response time in milliseconds : " + timeElapsed / 1000000);
        return Results.json().render(orderResponse);
    }

    public Result heartbeat(@PathParam("serverName") String serverName) {
        Health health = new Health();
        health.updateHealth(serverName);
        return Results.json().render("success");
    }


}
