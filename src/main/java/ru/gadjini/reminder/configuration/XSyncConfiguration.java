package ru.gadjini.reminder.configuration;

import com.antkorwin.xsync.XSync;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XSyncConfiguration {

    @Bean
    public XSync<String> xSync() {
        return new XSync<>();
    }
}
