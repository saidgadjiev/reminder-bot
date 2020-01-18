package ru.gadjini.reminder.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.model.TgMessage;
import ru.gadjini.reminder.service.InviteService;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.command.CommandParser;
import ru.gadjini.reminder.service.keyboard.reply.ReplyKeyboardService;
import ru.gadjini.reminder.service.keyboard.reply.ReplyKeyboardServiceImpl;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;

@Component
public class InviteFilter extends BaseBotFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(InviteFilter.class);

    private CommandParser commandParser;

    private TgUserService userService;

    private MessageService messageService;

    private LocalisationService localisationService;

    private InviteService inviteService;

    private ReplyKeyboardService replyKeyboardService;

    private ObjectMapper objectMapper;

    @Autowired
    public InviteFilter(CommandParser commandParser, TgUserService userService, MessageService messageService,
                        LocalisationService localisationService, InviteService inviteService,
                        ReplyKeyboardServiceImpl replyKeyboardService, ObjectMapper objectMapper) {
        this.commandParser = commandParser;
        this.userService = userService;
        this.messageService = messageService;
        this.localisationService = localisationService;
        this.inviteService = inviteService;
        this.replyKeyboardService = replyKeyboardService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void doFilter(Update update) {
        TgMessage message = TgMessage.from(update);
        boolean exists = userService.isExists(message.getUser().getId());

        if (!exists) {
            if (isStartCommand(update)) {
                messageService.sendMessageAsync(
                        new SendMessageContext(PriorityJob.Priority.MEDIUM)
                                .chatId(message.getChatId())
                                .text(localisationService.getMessage(MessagesProperties.MESSAGE_BOT_CLOSE_TESTING))
                                .replyKeyboard(replyKeyboardService.removeKeyboard(message.getChatId()))
                );
            } else {
                String token = inviteService.delete(message.getText());

                if (token != null) {
                    super.doFilter(getStartCommandUpdate(update));
                } else {
                    messageService.sendMessageAsync(
                            new SendMessageContext(PriorityJob.Priority.MEDIUM)
                                    .chatId(message.getChatId())
                                    .text(localisationService.getMessage(MessagesProperties.MESSAGE_INVITE_TOKEN_NOT_FOUND))
                                    .replyKeyboard(replyKeyboardService.removeKeyboard(message.getChatId()))
                    );
                }
            }
        } else {
            super.doFilter(update);
        }
    }

    private boolean isStartCommand(Update update) {
        if (update.hasMessage()) {
            String commandName = commandParser.parseBotCommandName(update.getMessage());

            return commandName.equals(CommandNames.START_COMMAND_NAME);
        }

        return false;
    }

    private Update getStartCommandUpdate(Update update) {
        try {
            String json = objectMapper.writeValueAsString(update);
            ObjectNode objectNode = objectMapper.readValue(json, ObjectNode.class);

            ObjectNode message = (ObjectNode) objectNode.get("message");
            message.put("text", "/" + CommandNames.START_COMMAND_NAME);
            message.set("entities", getBotCommandEntities());

            return objectMapper.treeToValue(objectNode, Update.class);
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_ERROR));
        }
    }

    private ArrayNode getBotCommandEntities() {
        ArrayNode entities = objectMapper.createArrayNode();

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("type", "bot_command");
        objectNode.put("offset", 0);
        objectNode.put("length", 6);

        entities.add(objectNode);

        return entities;
    }
}
