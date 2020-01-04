package ru.gadjini.reminder.bot.api;

import com.google.inject.Inject;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.grizzly.strategies.WorkerThreadIOStrategy;
import org.glassfish.grizzly.threadpool.ThreadPoolProbe;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.generics.Webhook;
import org.telegram.telegrambots.meta.generics.WebhookBot;
import org.telegram.telegrambots.updatesreceivers.RestApi;
import ru.gadjini.reminder.controller.ThreadLoggingProbe;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class DefaultWebhook implements Webhook {

    private String keystoreServerFile;
    private String keystoreServerPwd;
    private String internalUrl;

    private final RestApi restApi;

    @Inject
    public DefaultWebhook() {
        this.restApi = new RestApi();
    }

    @Override
    public void setInternalUrl(String internalUrl) {
        this.internalUrl = internalUrl;
    }

    @Override
    public void setKeyStore(String keyStore, String keyStorePassword) throws TelegramApiRequestException {
        this.keystoreServerFile = keyStore;
        this.keystoreServerPwd = keyStorePassword;
        validateServerKeystoreFile(keyStore);
    }

    @Override
    public void registerWebhook(WebhookBot callback) {
        restApi.registerCallback(callback);
    }

    @Override
    public void startServer() throws TelegramApiRequestException {
        ResourceConfig rc = new ResourceConfig();
        rc.register(restApi);
        rc.register(JacksonFeature.class);

        final HttpServer grizzlyServer;
        if (keystoreServerFile != null && keystoreServerPwd != null) {
            SSLContextConfigurator sslContext = new SSLContextConfigurator();

            // set up security context
            sslContext.setKeyStoreFile(keystoreServerFile); // contains server keypair
            sslContext.setKeyStorePass(keystoreServerPwd);

            grizzlyServer = GrizzlyHttpServerFactory.createHttpServer(getBaseURI(), rc, true,
                    new SSLEngineConfigurator(sslContext).setClientMode(false).setNeedClientAuth(false), false);
        } else {
            grizzlyServer = GrizzlyHttpServerFactory.createHttpServer(getBaseURI(), rc, false);
        }

        setIOStrategy(grizzlyServer);
        addProbe(grizzlyServer, new ThreadLoggingProbe());
        try {
            grizzlyServer.start();
        } catch (IOException e) {
            grizzlyServer.shutdown();
            throw new TelegramApiRequestException("Error starting webhook server", e);
        }
    }

    private URI getBaseURI() {
        return URI.create(internalUrl);
    }

    private static void validateServerKeystoreFile(String keyStore) throws TelegramApiRequestException {
        File file = new File(keyStore);
        if (!file.exists() || !file.canRead()) {
            throw new TelegramApiRequestException("Can't find or access server keystore file.");
        }
    }

    private void setIOStrategy(HttpServer httpServer) {
        TCPNIOTransport transport = httpServer.getListener("grizzly").getTransport();
        transport.setIOStrategy(WorkerThreadIOStrategy.getInstance());
    }

    private void addProbe(HttpServer httpServer, ThreadPoolProbe probe) {
        TCPNIOTransport transport = httpServer.getListener("grizzly").getTransport();
        transport.getWorkerThreadPoolConfig().getInitialMonitoringConfig().addProbes(probe);
    }
}
