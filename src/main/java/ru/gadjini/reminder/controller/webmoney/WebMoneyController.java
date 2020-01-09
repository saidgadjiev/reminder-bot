package ru.gadjini.reminder.controller.webmoney;

import org.glassfish.jersey.server.mvc.Template;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;

@Path("/payment")
@Controller
public class WebMoneyController {

    @Path("/pay")
    @Template(name = "/payment_request.ftl")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Map<String, Object> paymentRequest() {
        return Map.of("name", "Said");
    }
}
