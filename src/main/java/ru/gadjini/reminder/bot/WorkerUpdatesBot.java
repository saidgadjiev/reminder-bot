package ru.gadjini.reminder.bot;

import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gadjini.reminder.security.SecurityContext;
import ru.gadjini.reminder.security.SecurityContextHolder;
import ru.gadjini.reminder.security.SecurityContextRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class WorkerUpdatesBot extends TelegramWebhookBot {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private SecurityContextRepository securityContextRepository = new SecurityContextRepository();

    @Override
    public BotApiMethod onWebhookUpdateReceived(Update update) {
        EXECUTOR_SERVICE.submit(() -> {
            try {
                SecurityContext securityContext = securityContextRepository.loadContext(update);
                SecurityContextHolder.setContext(securityContext);

                onUpdateReceived(update);
            } finally {
                SecurityContextHolder.clearContext();
            }
        });

        return null;
    }

    protected void onUpdateReceived(Update update) {
    }
}
