package ru.gadjini.reminder.controller.webmoney;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.mvc.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.common.WebMoneyConstants;
import ru.gadjini.reminder.domain.PaymentType;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.model.WebMoneyPayment;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.keyboard.ReplyKeyboardService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.payment.PaymentService;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.time.LocalDate;
import java.util.Map;

@Path("/payment")
@Controller
public class WebMoneyController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebMoneyController.class);

    private static final int PRE_REQUEST = 1;

    private ReplyKeyboardService replyKeyboardService;

    private PaymentService paymentService;

    private MessageService messageService;

    private TgUserService userService;

    @Autowired
    public WebMoneyController(ReplyKeyboardService replyKeyboardService, PaymentService paymentService,
                              MessageService messageService, TgUserService userService) {
        this.replyKeyboardService = replyKeyboardService;
        this.paymentService = paymentService;
        this.messageService = messageService;
        this.userService = userService;
        LOGGER.debug("WebMoneyController initialized");
    }

    @Path("/pay")
    @Template(name = "/payment_request.ftl")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Map<String, Object> paymentRequest(@QueryParam("planId") int planId, @QueryParam("userId") int userId, @QueryParam("paymentType") int paymentType) {
        try {
            return paymentService.processPaymentRequest(planId, userId, PaymentType.fromType(paymentType));
        } catch (UserException ex) {
            throw new WebApplicationException(ex.getMessage(), Response.status(Response.Status.BAD_REQUEST).build());
        }
    }

    @Path("/process")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response processPayment(@Context ContainerRequest request) {
        if (request.getMediaType() == null) {
            return handleUrlCheck();
        }
        Form form = request.readEntity(Form.class);
        MultivaluedMap<String, String> formData = form.asMap();
        int preRequest = NumberUtils.toInt(formData.getFirst(WebMoneyConstants.LMI_PREREQUEST), -1);

        if (preRequest == PRE_REQUEST) {
            return preProcessPayment();
        }

        return processPayment(
                formData.getFirst(WebMoneyConstants.LMI_PAYEE_PURSE),
                NumberUtils.toDouble(formData.getFirst(WebMoneyConstants.LMI_PAYMENT_AMOUNT)),
                formData.getFirst(WebMoneyConstants.LMI_SECRET_KEY),
                NumberUtils.toInt(formData.getFirst("user_id")),
                NumberUtils.toInt(formData.getFirst("plan_id"))
        );
    }

    private Response handleUrlCheck() {
        return Response.ok().build();
    }

    private Response preProcessPayment() {
        return Response.ok().build();
    }

    private Response processPayment(String payeePurse, double paymentAmount, String secretKey, int userId, int planId) {
        if (StringUtils.isBlank(payeePurse)) {
            return Response.ok().build();
        }
        try {
            LocalDate localDate = paymentService.processPayment(new WebMoneyPayment()
                    .payeePurse(payeePurse)
                    .paymentAmount(paymentAmount)
                    .secretKey(secretKey)
                    .userId(userId)
                    .planId(planId));
            try {
                messageService.sendMessageByCode(
                        userService.getChatId(userId),
                        MessagesProperties.MESSAGE_SUBSCRIPTION_RENEWED,
                        new Object[]{localDate},
                        replyKeyboardService.getMainMenu()
                );
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }

            return Response.ok().build();
        } catch (Exception ex) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
}
