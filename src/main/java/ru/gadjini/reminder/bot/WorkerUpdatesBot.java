package ru.gadjini.reminder.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gadjini.reminder.security.SecurityContext;
import ru.gadjini.reminder.security.SecurityContextHolder;
import ru.gadjini.reminder.security.SecurityContextRepository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public abstract class WorkerUpdatesBot extends TelegramLongPollingBot {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private SecurityContextRepository securityContextRepository = new SecurityContextRepository();

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        Map<Long, List<Update>> updatesByChat = updates.stream()
                .collect(Collectors.groupingBy(update -> {
                    if (update.hasMessage()) {
                        return update.getMessage().getChatId();
                    } else if (update.hasCallbackQuery()) {
                        return update.getCallbackQuery().getMessage().getChatId();
                    }

                    throw new IllegalArgumentException();
                }));

        for (Map.Entry<Long, List<Update>> updatesByChatId : updatesByChat.entrySet()) {
            EXECUTOR_SERVICE.submit(() -> {
                try {
                    SecurityContext securityContext = securityContextRepository.loadContext(updatesByChatId.getValue().get(0));
                    SecurityContextHolder.setContext(securityContext);

                    updatesByChatId.getValue().forEach(this::onUpdateReceived);
                } finally {
                    SecurityContextHolder.clearContext();
                }
            });
        }
    }

}
