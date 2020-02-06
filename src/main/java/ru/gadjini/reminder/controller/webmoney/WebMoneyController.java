package ru.gadjini.reminder.controller.webmoney;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.mvc.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.UriComponentsBuilder;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.common.WebMoneyConstants;
import ru.gadjini.reminder.domain.PaymentType;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.model.WebMoneyPayment;
import ru.gadjini.reminder.properties.BotProperties;
import ru.gadjini.reminder.properties.WebHookProperties;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.command.CommandNavigator;
import ru.gadjini.reminder.service.keyboard.reply.CurrReplyKeyboard;
import ru.gadjini.reminder.service.keyboard.reply.ReplyKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.payment.PaymentService;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;

import static ru.gadjini.reminder.common.TemplateConstants.*;

@Path(WebMoneyController.CONTROLLER_PATH)
@Controller
public class WebMoneyController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebMoneyController.class);

    static final String CONTROLLER_PATH = "/payment";

    private static final String PAYMENT_RESULT_PATH = "/result";

    private static final int PRE_REQUEST = 1;

    private ReplyKeyboardService replyKeyboardService;

    private PaymentService paymentService;

    private MessageService messageService;

    private LocalisationService localisationService;

    private BotProperties botProperties;

    private WebHookProperties webHookProperties;

    private CommandNavigator commandNavigator;

    private TgUserService userService;

    @Autowired
    public WebMoneyController(CurrReplyKeyboard replyKeyboardService, PaymentService paymentService,
                              MessageService messageService, LocalisationService localisationService,
                              BotProperties botProperties, WebHookProperties webHookProperties, CommandNavigator commandNavigator, TgUserService userService) {
        this.replyKeyboardService = replyKeyboardService;
        this.paymentService = paymentService;
        this.messageService = messageService;
        this.localisationService = localisationService;
        this.botProperties = botProperties;
        this.webHookProperties = webHookProperties;
        this.commandNavigator = commandNavigator;
        this.userService = userService;

        LOGGER.debug("WebMoneyController initialized");
    }

    @Template(name = "/base.ftl")
    @GET
    @Path(WebMoneyController.PAYMENT_RESULT_PATH)
    @Produces(MediaType.TEXT_HTML)
    public Map<String, Object> paymentResult(@QueryParam("fail") boolean fail) {
        return Map.of(
                FAIL, fail,
                TEMPLATE, "payment_result.ftl",
                PAYMENT_RESULT, localisationService.getMessage(fail ? MessagesProperties.MESSAGE_PAYMENT_FAIL : MessagesProperties.MESSAGE_PAYMENT_SUCCESS, Locale.getDefault()),
                REDIRECT, localisationService.getMessage(MessagesProperties.MESSAGE_TELEGRAM_REDIRECT, Locale.getDefault()),
                BOT_NAME, botProperties.getName()
        );
    }

    @POST
    @Path("/success")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response success() {
        return Response.seeOther(URI.create(redirectUrl(false))).build();
    }

    @POST
    @Path("/fail")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response fail(@FormParam("user_id") int userId) {
        messageService.sendMessageAsync(new SendMessageContext(PriorityJob.Priority.MEDIUM).chatId(userId).text(localisationService.getMessage(MessagesProperties.MESSAGE_PAYMENT_FAIL, Locale.getDefault())));

        return Response.seeOther(URI.create(redirectUrl(true))).build();
    }

    @Template(name = "/payment_request.ftl")
    @Path("/pay")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Map<String, Object> paymentRequest(@QueryParam("planId") int planId, @QueryParam("userId") int userId, @QueryParam("paymentType") int paymentType) {
        try {
            return paymentService.processPaymentRequest(planId, userId, PaymentType.fromType(paymentType), userService.getLocale(userId));
        } catch (UserException ex) {
            LOGGER.error(ex.getMessage());
            throw new WebApplicationException(ex.getMessage(), Response.status(Response.Status.BAD_REQUEST).build());
        }
    }

    @Path("/process")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response processPayment(@Context ContainerRequest request) {
        if (request.getMediaType() == null) {
            return processUrlCheck();
        }
        Form form = request.readEntity(Form.class);
        MultivaluedMap<String, String> formData = form.asMap();
        int preRequest = NumberUtils.toInt(formData.getFirst(WebMoneyConstants.LMI_PRE_REQUEST), -1);

        if (preRequest == PRE_REQUEST) {
            return preProcessPayment();
        }

        return processPayment(formData);
    }

    private Response processUrlCheck() {
        return Response.ok().build();
    }

    private Response preProcessPayment() {
        return Response.ok("YES").build();
    }

    private Response processPayment(MultivaluedMap<String, String> formData) {
        try {
            int userId = NumberUtils.toInt(formData.getFirst("user_id"));
            WebMoneyPayment webMoneyPayment = new WebMoneyPayment()
                    .payeePurse(formData.getFirst(WebMoneyConstants.LMI_PAYEE_PURSE))
                    .paymentAmount(NumberUtils.toDouble(formData.getFirst(WebMoneyConstants.LMI_PAYMENT_AMOUNT)))
                    .secretKey(formData.getFirst(WebMoneyConstants.LMI_SECRET_KEY))
                    .userId(userId)
                    .planId(NumberUtils.toInt(formData.getFirst("plan_id")))
                    .locale(userService.getLocale(userId));

            validate(webMoneyPayment);

            return processPayment(webMoneyPayment);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    private Response processPayment(WebMoneyPayment webMoneyPayment) {
        PaymentService.PaymentProcessResult paymentProcessResult = paymentService.processPayment(webMoneyPayment);

        if (paymentProcessResult.getPaymentMessageId() != null) {
            deletePaymentMessage(webMoneyPayment.userId(), paymentProcessResult.getPaymentMessageId());
        }
        sendSubscriptionRenewed(webMoneyPayment.userId(), paymentProcessResult.getSubscriptionEnd(), webMoneyPayment.locale());

        return Response.ok().build();
    }

    private void validate(WebMoneyPayment payment) {
        if (StringUtils.isBlank(payment.payeePurse())) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        if (StringUtils.isBlank(payment.payeePurse())) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }

    private void deletePaymentMessage(long chatId, int messageId) {
        try {
            messageService.deleteMessage(chatId, messageId);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    private void sendSubscriptionRenewed(int userId, LocalDate subscriptionEnd, Locale locale) {
        try {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(userId)
                            .text(localisationService.getMessage(MessagesProperties.MESSAGE_SUBSCRIPTION_RENEWED, new Object[]{subscriptionEnd}, locale))
                            .replyKeyboard(replyKeyboardService.getMainMenu(userId, locale))
            );
            commandNavigator.setCurrentCommand(userId, CommandNames.START_COMMAND_NAME);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    private String redirectUrl(boolean fail) {
        return UriComponentsBuilder.fromHttpUrl(webHookProperties.getExternalUrl())
                .path(CONTROLLER_PATH + PAYMENT_RESULT_PATH)
                .queryParam("fail", fail)
                .toUriString();
    }
}
