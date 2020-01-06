package ru.gadjini.reminder.controller.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gadjini.reminder.bot.ReminderWebhookBot;
import ru.gadjini.reminder.configuration.BotConfiguration;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Profile(BotConfiguration.PROFILE_TEST)
@Path("/callback")
@Controller
public class BotController {

    private ReminderWebhookBot reminderWebhookBot;

    @Autowired
    public BotController(ReminderWebhookBot reminderWebhookBot) {
        this.reminderWebhookBot = reminderWebhookBot;
    }

    @POST
    @Path("/{botPath}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateReceived(@PathParam("botPath") String botPath, Update update) {
        reminderWebhookBot.onWebhookUpdateReceived(update);

        return Response.ok(null).build();
    }

    @GET
    @Path("/{botPath}")
    @Produces(MediaType.APPLICATION_JSON)
    public String testReceived(@PathParam("botPath") String botPath) {
        return "Hi there " + botPath + "!";
    }
}
