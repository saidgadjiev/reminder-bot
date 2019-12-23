package ru.gadjini.reminder.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiValidationException;
import ru.gadjini.reminder.bot.ReminderBot;
import ru.gadjini.reminder.configuration.BotConfiguration;

@Profile(BotConfiguration.PROFILE_TEST)
@RestController
@RequestMapping("/callback")
public class BotController {

    private ReminderBot reminderBot;

    @Autowired
    public BotController(ReminderBot reminderBot) {
        this.reminderBot = reminderBot;
    }

    @PostMapping("/{botPath}")
    public ResponseEntity<?> updateReceived(@PathVariable("botPath") String botPath, @RequestBody Update update) {
        try {
            BotApiMethod response = reminderBot.onWebhookUpdateReceived(update);
            if (response != null) {
                response.validate();
            }

            return ResponseEntity.ok().build();
        } catch (TelegramApiValidationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{botPath}")
    public String testReceived(@PathVariable("botPath") String botPath) {
            return "Hi there " + botPath + "!";
    }
}
