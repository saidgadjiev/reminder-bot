package ru.gadjini.reminder.controller.webmoney;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.generics.Webhook;

import javax.annotation.PostConstruct;

@Component
public class ControllerInitializer {

    private AutowireCapableBeanFactory beanFactory;

    @Autowired
    public ControllerInitializer(AutowireCapableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @PostConstruct
    public void init() {
        Webhook instance = ApiContext.getInstance(Webhook.class);
        beanFactory.autowireBean(instance);
    }
}
