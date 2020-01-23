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
import ru.gadjini.reminder.util.TimeUtils;

import java.time.ZoneId;
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

    private LocalisationService localisationService;

    @Autowired
    public ChangeTimezoneCommand(LocalisationService localisationService,
                                 MessageService messageService,
                                 TgUserService tgUserService,
                                 TimezoneService timezoneService,
                                 CurrReplyKeyboard replyKeyboardService,
                                 LocalisationService localisationService1) {
        name = localisationService.getMessage(MessagesProperties.CHANGE_TIMEZONE_COMMAND_NAME);
        this.messageService = messageService;
        this.tgUserService = tgUserService;
        this.timezoneService = timezoneService;
        this.replyKeyboardService = replyKeyboardService;
        this.localisationService = localisationService1;
    }

    @Override
    public boolean canHandle(long chatId, String command) {
        return name.equals(command);
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
                        .text(localisationService.getMessage(MessagesProperties.CURRENT_TIMEZONE, new Object[]{
                                zoneId.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                                DateTimeFormats.TIMEZONE_LOCAL_TIME_FORMATTER.format(TimeUtils.zonedDateTimeNow(zoneId))
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
                            .text(localisationService.getMessage(MessagesProperties.TIMEZONE_CHANGED, new Object[]{
                                    zoneId.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                                    DateTimeFormats.TIMEZONE_LOCAL_TIME_FORMATTER.format(TimeUtils.zonedDateTimeNow(zoneId))
                            })).replyKeyboard(replyKeyboardMarkup)
            );
        });
    }
}
