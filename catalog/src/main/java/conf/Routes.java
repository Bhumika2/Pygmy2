/**
 * Copyright (C) 2012-2019 the original author or authors.
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


package conf;


import controllers.ApplicationController;
import ninja.Router;
import ninja.application.ApplicationRoutes;

public class Routes implements ApplicationRoutes {

    @Override
    public void init(Router router) {
        router.GET().route("/queryBySubject/{topic}").with(ApplicationController::queryBySubject);
        router.GET().route("/queryByItem/{id}").with(ApplicationController::queryByItem);
        router.POST().route("/updateInventory/{id}/{type}").with(ApplicationController::update);
        router.POST().route("/updateCost/{id}/{cost}").with(ApplicationController::updateCost);
        router.POST().route("/syncDb/{id}/{key}").with(ApplicationController::syncDB);
        router.GET().route("/resyncDB").with(ApplicationController::resyncDB);
    }

}
