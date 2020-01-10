package ru.gadjini.reminder.controller;

import org.glassfish.jersey.server.mvc.Template;
import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import ru.gadjini.reminder.domain.Plan;
import ru.gadjini.reminder.properties.SubscriptionProperties;
import ru.gadjini.reminder.service.subscription.PlanService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Map;

@Path("/")
@Controller
public class RootController {

    private PlanService planService;

    private SubscriptionProperties subscriptionProperties;

    @Autowired
    public RootController(PlanService planService, SubscriptionProperties subscriptionProperties) {
        this.planService = planService;
        this.subscriptionProperties = subscriptionProperties;
    }

    @Template(name = "/base.ftl")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Map<String, Object> get() {
        Plan activePlan = planService.getActivePlan();

        return Map.of("template", "index.ftl", "number", 171241421, "trialPeriod", subscriptionProperties.getTrialPeriod(), "price", activePlan.getPrice(), "period", getPeriod(activePlan.getPeriod()));
    }

    private String getPeriod(Period period) {
        return period.getMonths() + " месяц";
    }
}
