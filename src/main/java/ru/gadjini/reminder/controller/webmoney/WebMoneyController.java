package ru.gadjini.reminder.controller.webmoney;

import org.springframework.stereotype.Controller;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/webmoney")
@Controller
public class WebMoneyController {

    @Path("/pay")
    @POST
    @Produces(MediaType.TEXT_HTML)
    public Response test(@FormParam("age") int age, @FormParam("name") String name) {
        return Response.ok().build();
    }
}
