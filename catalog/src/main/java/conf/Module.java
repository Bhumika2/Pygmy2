package conf;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import service.HeartBeatService;

@Singleton
public class Module extends AbstractModule {
    protected void configure(){
        bind(HeartBeatService.class);
    }
}
