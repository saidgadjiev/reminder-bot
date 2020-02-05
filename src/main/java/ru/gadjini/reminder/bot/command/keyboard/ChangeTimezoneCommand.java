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
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.TimezoneService;
import ru.gadjini.reminder.service.command.CommandNavigator;
import ru.gadjini.reminder.service.keyboard.reply.CurrReplyKeyboard;
import ru.gadjini.reminder.service.keyboard.reply.ReplyKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.time.DateTimeFormats;
import ru.gadjini.reminder.util.TimeCreator;

import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Component
public class ChangeTimezoneCommand implements KeyboardBotCommand, NavigableBotCommand {

    private Set<String> names = new HashSet<>();

    private MessageService messageService;

    private TgUserService tgUserService;

    private TimezoneService timezoneService;

    private CommandNavigator commandNavigator;

    private ReplyKeyboardService replyKeyboardService;

    private LocalisationService localisationService;

    private TimeCreator timeCreator;

    @Autowired
    public ChangeTimezoneCommand(LocalisationService localisationService,
                                 MessageService messageService,
                                 TgUserService tgUserService,
                                 TimezoneService timezoneService,
                                 CurrReplyKeyboard replyKeyboardService,
                                 LocalisationService localisationService1, TimeCreator timeCreator) {
        this.messageService = messageService;
        this.tgUserService = tgUserService;
        this.timezoneService = timezoneService;
        this.replyKeyboardService = replyKeyboardService;
        this.localisationService = localisationService1;
        this.timeCreator = timeCreator;

        for (Locale locale : localisationService.getSupportedLocales()) {
            this.names.add(localisationService.getMessage(MessagesProperties.CHANGE_TIMEZONE_COMMAND_NAME, locale));
        }
    }

    @Override
    public boolean canHandle(long chatId, String command) {
        return names.contains(command);
    }

    @Autowired
    public void setCommandNavigator(CommandNavigator commandNavigator) {
        this.commandNavigator = commandNavigator;
    }

    @Override
    public boolean processMessage(Message message, String text) {
        ZoneId zoneId = tgUserService.getTimeZone(message.getFrom().getId());

        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(message.getChatId())
                        .text(localisationService.getCurrentLocaleMessage(MessagesProperties.CURRENT_TIMEZONE, new Object[]{
                                zoneId.getDisplayName(TextStyle.FULL, localisationService.getCurrentLocale()),
                                DateTimeFormats.TIMEZONE_LOCAL_TIME_FORMATTER.format(timeCreator.zonedDateTimeNow(zoneId))
                        })).replyKeyboard(replyKeyboardService.goBackCommand(message.getChatId()))
        );

        return true;
    }

    @Override
    public String getHistoryName() {
        return CommandNames.CHANGE_TIMEZONE_COMMAND_HISTORY_NAME;
    }

    @Override
    public boolean accept(Message message) {
        return message.hasLocation();
    }

    @Override
    public void processNonCommandUpdate(Message message, String text) {
        Location location = message.getLocation();
        timezoneService.getZoneId(location.getLatitude(), location.getLongitude(), zoneId -> {
            tgUserService.saveZoneId(message.getFrom().getId(), zoneId);
            ReplyKeyboardMarkup replyKeyboardMarkup = commandNavigator.silentPop(message.getChatId());

            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(message.getChatId())
                            .text(localisationService.getCurrentLocaleMessage(MessagesProperties.TIMEZONE_CHANGED, new Object[]{
                                    zoneId.getDisplayName(TextStyle.FULL, localisationService.getCurrentLocale()),
                                    DateTimeFormats.TIMEZONE_LOCAL_TIME_FORMATTER.format(timeCreator.zonedDateTimeNow(zoneId))
                            })).replyKeyboard(replyKeyboardMarkup)
            );
        });
    }
}
