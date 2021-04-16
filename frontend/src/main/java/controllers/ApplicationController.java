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

import com.google.inject.Singleton;
import models.CatalogResponse;
import models.OrderRequest;
import models.OrderResponse;
import ninja.Result;
import ninja.Results;
import ninja.params.PathParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.Catalog;
import service.Order;
import com.google.inject.Inject;
import ninja.utils.NinjaProperties;

import java.util.List;

@Singleton
public class ApplicationController {

    Logger logger = LoggerFactory.getLogger("Pygmy");
    /**
     * search is the microservice endpoint that invokes searchTopic method in Catalog class under
     * service package to handle the request. It is invoked from client and returns books corresponding to topic.
     */
    @Inject
    NinjaProperties ninjaProperties;

    public Result search(@PathParam("topic") String topic) {
        logger.info("Search request received for topic: " + topic);
        long startTime = System.nanoTime();
        Catalog catalog = new Catalog(ninjaProperties);
        List<CatalogResponse> catalogResponse = catalog.searchTopic(topic);
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
        Catalog catalog = new Catalog(ninjaProperties);
        CatalogResponse catalogResponse = catalog.lookupBook(bookNumber);
        long timeElapsed = System.nanoTime() - startTime;
        logger.info("Lookup response time in milliseconds : " + timeElapsed / 1000000);
        return Results.json().render(catalogResponse);
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
        OrderResponse orderResponse = order.buyBook(orderRequest);
        long timeElapsed = System.nanoTime() - startTime;
        logger.info("Order response time in milliseconds : " + timeElapsed / 1000000);
        return Results.json().render(orderResponse);
    }
}
