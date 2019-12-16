package ru.gadjini.reminder.bot.command.keyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.service.*;
import ru.gadjini.reminder.service.command.CommandNavigator;
import ru.gadjini.reminder.service.keyboard.KeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.time.DateTimeFormats;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class ChangeTimezoneCommand implements KeyboardBotCommand, NavigableCallbackBotCommand {

    private String name;

    private MessageService messageService;

    private TgUserService tgUserService;

    private TimezoneService timezoneService;

    private CommandNavigator commandNavigator;

    private KeyboardService keyboardService;

    @Autowired
    public ChangeTimezoneCommand(LocalisationService localisationService,
                                 MessageService messageService,
                                 TgUserService tgUserService,
                                 TimezoneService timezoneService,
                                 CommandNavigator commandNavigator,
                                 KeyboardService keyboardService) {
        name = localisationService.getMessage(MessagesProperties.CHANGE_TIMEZONE_COMMAND_NAME);
        this.messageService = messageService;
        this.tgUserService = tgUserService;
        this.timezoneService = timezoneService;
        this.commandNavigator = commandNavigator;
        this.keyboardService = keyboardService;
    }

    @Override
    public boolean canHandle(String command) {
        return name.equals(command);
    }

    @Override
    public void processMessage(Message message) {
        ZoneId zoneId = tgUserService.getTimeZone(message.getFrom().getId());

        messageService.sendMessageByCode(message.getChatId(), MessagesProperties.CURRENT_TIMEZONE, new Object[]{
                        zoneId.toString(),
                        DateTimeFormats.TIMEZONE_LOCAL_TIME_FORMATTER.format(ZonedDateTime.now(zoneId))
                },
                keyboardService.goBackCommand());
    }

    @Override
    public String getHistoryName() {
        return MessagesProperties.CHANGE_TIMEZONE_COMMAND_HISTORY_NAME;
    }

    @Override
    public void processNonCommandUpdate(Message message) {
        if (!message.hasLocation()) {
            return;
        }
        Location location = message.getLocation();
        ZoneId zoneId = timezoneService.getZoneId(location.getLatitude(), location.getLongitude());

        tgUserService.saveZoneId(message.getFrom().getId(), zoneId);
        ReplyKeyboardMarkup replyKeyboardMarkup = commandNavigator.silentPop(message.getChatId());

        messageService.sendMessageByCode(message.getChatId(), MessagesProperties.TIMEZONE_CHANGED, new Object[]{
                zoneId.toString(),
                DateTimeFormats.TIMEZONE_LOCAL_TIME_FORMATTER.format(ZonedDateTime.now(zoneId))
        }, replyKeyboardMarkup);
    }
}
