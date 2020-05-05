package ru.gadjini.reminder.controller;

import org.glassfish.jersey.server.mvc.Template;
import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import ru.gadjini.reminder.common.TemplateConstants;
import ru.gadjini.reminder.domain.Plan;
import ru.gadjini.reminder.property.BotProperties;
import ru.gadjini.reminder.property.SubscriptionProperties;
import ru.gadjini.reminder.service.subscription.PlanService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;

@Path("/")
@Controller
public class RootController {

    private PlanService planService;

    private SubscriptionProperties subscriptionProperties;

    private BotProperties botProperties;

    @Autowired
    public RootController(PlanService planService, SubscriptionProperties subscriptionProperties, BotProperties botProperties) {
        this.planService = planService;
        this.subscriptionProperties = subscriptionProperties;
        this.botProperties = botProperties;
    }

    @Template(name = "/base.ftl")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Map<String, Object> get() {
        Plan activePlan = planService.getActivePlan();

        return Map.of(
                TemplateConstants.BOT_NAME, botProperties.getName(),
                TemplateConstants.TEMPLATE, "index.ftl",
                TemplateConstants.TRIAL_PERIOD, subscriptionProperties.getTrialPeriod(),
                TemplateConstants.PRICE, activePlan.getPrice(),
                TemplateConstants.PERIOD, getPeriod(activePlan.getPeriod())
        );
    }

    private String getPeriod(Period period) {
        return period.getMonths() + " месяц";
    }
}
