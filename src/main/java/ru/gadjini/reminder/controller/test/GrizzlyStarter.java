package ru.gadjini.reminder.controller.test;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.strategies.WorkerThreadIOStrategy;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.configuration.BotConfiguration;
import ru.gadjini.reminder.properties.WebHookProperties;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;

@Profile(BotConfiguration.PROFILE_TEST)
@Service
public class GrizzlyStarter {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrizzlyStarter.class);

    private BotController botController;

    private WebHookProperties webHookProperties;

    @Autowired
    public GrizzlyStarter(BotController botController, WebHookProperties webHookProperties) {
        this.botController = botController;
        this.webHookProperties = webHookProperties;
    }

    @PostConstruct
    public void start() {
        ResourceConfig rc = new ResourceConfig();
        rc.register(botController);
        rc.register(JacksonFeature.class);

        HttpServer httpServer = GrizzlyHttpServerFactory.createHttpServer(URI.create(webHookProperties.getInternalUrl()), rc, false);

        TCPNIOTransport transport = httpServer.getListener("grizzly").getTransport();
        transport.setIOStrategy(WorkerThreadIOStrategy.getInstance());
        transport.getWorkerThreadPoolConfig().getInitialMonitoringConfig().addProbes(new ThreadLoggingProbe());

        try {
            httpServer.start();
            LOGGER.debug("Grizzly server started at: " + webHookProperties.getInternalUrl());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            httpServer.shutdown();
        }
    }
}
