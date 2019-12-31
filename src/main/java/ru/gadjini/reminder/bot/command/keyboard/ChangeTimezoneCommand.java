package ru.gadjini.reminder.bot.command.keyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.TimezoneService;
import ru.gadjini.reminder.service.command.CommandNavigator;
import ru.gadjini.reminder.service.keyboard.ReplyKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.time.DateTimeFormats;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.Locale;

@Component
public class ChangeTimezoneCommand implements KeyboardBotCommand, NavigableBotCommand {

    private String name;

    private MessageService messageService;

    private TgUserService tgUserService;

    private TimezoneService timezoneService;

    private CommandNavigator commandNavigator;

    private ReplyKeyboardService replyKeyboardService;

    @Autowired
    public ChangeTimezoneCommand(LocalisationService localisationService,
                                 MessageService messageService,
                                 TgUserService tgUserService,
                                 TimezoneService timezoneService,
                                 CommandNavigator commandNavigator,
                                 ReplyKeyboardService replyKeyboardService) {
        name = localisationService.getMessage(MessagesProperties.CHANGE_TIMEZONE_COMMAND_NAME);
        this.messageService = messageService;
        this.tgUserService = tgUserService;
        this.timezoneService = timezoneService;
        this.commandNavigator = commandNavigator;
        this.replyKeyboardService = replyKeyboardService;
    }

    @Override
    public boolean canHandle(String command) {
        return name.equals(command);
    }

    @Override
    public boolean processMessage(Message message, String text) {
        ZoneId zoneId = tgUserService.getTimeZone(message.getFrom().getId());

        messageService.sendMessageByCode(message.getChatId(), MessagesProperties.CURRENT_TIMEZONE, new Object[]{
                        zoneId.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                        DateTimeFormats.TIMEZONE_LOCAL_TIME_FORMATTER.format(ZonedDateTime.now(zoneId))
                },
                replyKeyboardService.goBackCommand());

        return true;
    }

    @Override
    public String getHistoryName() {
        return CommandNames.CHANGE_TIMEZONE_COMMAND_HISTORY_NAME;
    }

    @Override
    public String getParentHistoryName() {
        return CommandNames.USER_SETTINGS_COMMAND_HISTORY_NAME;
    }

    @Override
    public boolean accept(Message message) {
        return message.hasLocation();
    }

    @Override
    public void processNonCommandUpdate(Message message, String text) {
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
