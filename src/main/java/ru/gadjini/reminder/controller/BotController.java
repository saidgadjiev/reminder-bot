package ru.gadjini.reminder.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gadjini.reminder.service.ReminderBotService;
import ru.gadjini.reminder.configuration.BotConfiguration;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

//TODO:выкинуть и запустить grizzly server. Надо тестить его.
@Profile(BotConfiguration.PROFILE_TEST)
@Path("/callback")
@Controller
public class BotController {

    private ReminderBotService reminderBotService;

    @Autowired
    public BotController(ReminderBotService reminderBotService) {
        this.reminderBotService = reminderBotService;
    }

    @POST
    @Path("/{botPath}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateReceived(@PathParam("botPath") String botPath, Update update) {
        reminderBotService.onWebhookUpdateReceived(update);

        return Response.ok(null).build();
    }

    @GET
    @Path("/{botPath}")
    @Produces(MediaType.APPLICATION_JSON)
    public String testReceived(@PathParam("botPath") String botPath) {
        return "Hi there " + botPath + "!";
    }
}
