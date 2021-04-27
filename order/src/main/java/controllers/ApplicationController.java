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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import models.BuyRequest;
import models.BuyResponse;
import ninja.Result;
import ninja.Results;
import ninja.utils.NinjaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.BuyItem;
import service.HeartBeatService;


@Singleton
public class ApplicationController {
    Logger logger = LoggerFactory.getLogger("Pygmy");

    NinjaProperties ninjaProperties;

    @Inject
    public ApplicationController(NinjaProperties ninjaProperties) {
        this.ninjaProperties = ninjaProperties;
    }
    /**
     * buy serves the buy http requests from frontend server. It invokes the buy method from BuyItem class under service package
     * returns the message of purchase status
     */
    public Result buy(BuyRequest buyObj) {
        logger.info("Buy request received for book: " + buyObj.getBookNumber());
        long startTime = System.nanoTime();
        BuyItem buyItem = new BuyItem(ninjaProperties);
        BuyResponse buyResponse = buyItem.buy(buyObj);
        long timeElapsed = System.nanoTime() - startTime;
        logger.info("Buy response time in milliseconds : " + timeElapsed / 1000000);
        return Results.json().render(buyResponse);
    }

}
