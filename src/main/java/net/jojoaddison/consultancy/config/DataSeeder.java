package net.jojoaddison.consultancy.config;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.jojoaddison.consultancy.domain.Authority;
import net.jojoaddison.consultancy.domain.Client;
import net.jojoaddison.consultancy.domain.Course;
import net.jojoaddison.consultancy.domain.Milestone;
import net.jojoaddison.consultancy.domain.Project;
import net.jojoaddison.consultancy.domain.ServiceItem;
import net.jojoaddison.consultancy.domain.TeamMember;
import net.jojoaddison.consultancy.domain.Ticket;
import net.jojoaddison.consultancy.domain.User;
import net.jojoaddison.consultancy.domain.enumeration.CatalogueGroup;
import net.jojoaddison.consultancy.domain.enumeration.DeliveryMode;
import net.jojoaddison.consultancy.domain.enumeration.Market;
import net.jojoaddison.consultancy.domain.enumeration.MilestoneState;
import net.jojoaddison.consultancy.domain.enumeration.RateUnit;
import net.jojoaddison.consultancy.domain.enumeration.ServicePillar;
import net.jojoaddison.consultancy.domain.enumeration.Status;
import net.jojoaddison.consultancy.domain.enumeration.TicketState;
import net.jojoaddison.consultancy.repository.AuthorityRepository;
import net.jojoaddison.consultancy.repository.ClientRepository;
import net.jojoaddison.consultancy.repository.CourseRepository;
import net.jojoaddison.consultancy.repository.MilestoneRepository;
import net.jojoaddison.consultancy.repository.ProjectRepository;
import net.jojoaddison.consultancy.repository.ServiceItemRepository;
import net.jojoaddison.consultancy.repository.TeamMemberRepository;
import net.jojoaddison.consultancy.repository.TicketRepository;
import net.jojoaddison.consultancy.repository.UserRepository;
import net.jojoaddison.consultancy.security.AuthoritiesConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.config.JHipsterConstants;

/**
 * Loads the real Jojo Addison demo fixtures (from jojoaddison-consultancy-demo.html) in the dev profile,
 * replacing JHipster's faker sample data. Idempotent: skips if clients already exist.
 *
 * <p>Also provisions two portal logins that exercise role scoping:
 * <ul>
 *   <li>{@code kojo} / {@code demo1234} — consultant (ROLE_CONSULTANT), sees all clients' data;
 *   <li>{@code ama} / {@code demo1234} — client (ROLE_USER), scoped to Bedrock Insurance Ghana only.
 * </ul>
 */
@Component
@Profile(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT)
public class DataSeeder {

    private static final Logger LOG = LoggerFactory.getLogger(DataSeeder.class);
    private static final String DEMO_PASSWORD = "demo1234";

    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClientRepository clientRepository;
    private final ProjectRepository projectRepository;
    private final MilestoneRepository milestoneRepository;
    private final TicketRepository ticketRepository;
    private final ServiceItemRepository serviceItemRepository;
    private final CourseRepository courseRepository;
    private final TeamMemberRepository teamMemberRepository;

    public DataSeeder(
        UserRepository userRepository,
        AuthorityRepository authorityRepository,
        PasswordEncoder passwordEncoder,
        ClientRepository clientRepository,
        ProjectRepository projectRepository,
        MilestoneRepository milestoneRepository,
        TicketRepository ticketRepository,
        ServiceItemRepository serviceItemRepository,
        CourseRepository courseRepository,
        TeamMemberRepository teamMemberRepository
    ) {
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
        this.passwordEncoder = passwordEncoder;
        this.clientRepository = clientRepository;
        this.projectRepository = projectRepository;
        this.milestoneRepository = milestoneRepository;
        this.ticketRepository = ticketRepository;
        this.serviceItemRepository = serviceItemRepository;
        this.courseRepository = courseRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        if (clientRepository.count() > 0) {
            LOG.debug("Demo data already present; skipping DataSeeder");
            return;
        }
        LOG.info("Seeding Jojo Addison demo fixtures (dev profile)");

        Authority userAuthority = authorityRepository
            .findById(AuthoritiesConstants.USER)
            .orElseGet(() -> saveAuthority(AuthoritiesConstants.USER));
        Authority consultantAuthority = authorityRepository
            .findById(AuthoritiesConstants.CONSULTANT)
            .orElseGet(() -> saveAuthority(AuthoritiesConstants.CONSULTANT));

        User consultantUser = ensureUser(
            "kojo",
            "Kojo",
            "Ampia-Addison",
            "kojo@jojoaddison.net",
            Set.of(userAuthority, consultantAuthority)
        );
        User clientUser = ensureUser("ama", "Ama", "Kusi", "ama.kusi@bedrockinsurancegh.com", Set.of(userAuthority));

        // ---- team ----
        TeamMember kojo = team("Kojo Ampia-Addison", "KA", "Managing Consultant", "Software & Systems Engineer (Bsc.)").user(
            consultantUser
        );
        TeamMember kwesi = team("Kwesi Arku-Addison", "KwA", "Partner Consultant", "Electrical & Electronic Technician");
        TeamMember kwabena = team("Kwabena Addai Frimpong", "KF", "Associate Consultant", "Software Engineer (Bsc.)");
        TeamMember nii = team("Nii Adjei Osae", "NO", "Associate Consultant", "Software Engineer (Bsc.)");
        teamMemberRepository.saveAll(List.of(kojo, kwesi, kwabena, nii));

        // ---- clients ---- (Bedrock is owned by the client login)
        Client bedrock = client("Bedrock Insurance Ghana", Market.BANKING_FINANCE_INSURANCE, 2020, Status.GOOD, 412000).user(clientUser);
        Client ports = client("Ghana Ports Authority", Market.HAULAGE_TRANSPORTATION, 2023, Status.WARN, 96000);
        Client izzi = client("Izzi Technologies", Market.BANKING_FINANCE_INSURANCE, 2022, Status.GOOD, 118000);
        Client melcom = client("Melcom Retail Group", Market.RETAIL_DISTRIBUTION, 2024, Status.SERIOUS, 212000);
        Client nsd = client("National Security Directorate", Market.DEFENCE_NATIONAL_SECURITY, 2025, Status.CRIT, 340000);
        Client afri = client("AFRI-EUROTEXT e.V.", Market.INSTITUTIONS_COMMISSIONS_AUTHORITIES, 2021, Status.GOOD, 74000);
        Client aaa = client("AAA African Afforestation", Market.AGRICULTURE, 2023, Status.GOOD, 61000);
        Client gahana = client("Gahana Mobile Money", Market.COMMUNICATION, 2024, Status.WARN, 128000);
        clientRepository.saveAll(List.of(bedrock, ports, izzi, melcom, nsd, afri, aaa, gahana));

        // ---- service catalogue ----
        serviceItemRepository.saveAll(
            List.of(
                svc("c1", "Business analysis & conception", 4800, RateUnit.PER_PHASE, CatalogueGroup.CONSULTANCY),
                svc("c2", "Business process digitization", 9600, RateUnit.PER_PROCESS, CatalogueGroup.CONSULTANCY),
                svc("c3", "Information systems audit prep", 12000, RateUnit.PER_SCOPE, CatalogueGroup.CONSULTANCY),
                svc("c4", "Web-based software engineering", 15500, RateUnit.PER_MODULE, CatalogueGroup.SOLUTIONS),
                svc("c5", "Enterprise integration", 11200, RateUnit.PER_INTEGRATION, CatalogueGroup.SOLUTIONS),
                svc("c6", "Cyber security protection", 13400, RateUnit.PER_ENVIRONMENT, CatalogueGroup.SOLUTIONS),
                svc("c7", "Operations monitoring & support", 3200, RateUnit.PER_MONTH, CatalogueGroup.SERVICES),
                svc("c8", "Data processing & visualization", 8700, RateUnit.PER_DATASET, CatalogueGroup.SERVICES),
                svc("c9", "In-house / virtual training", 2600, RateUnit.PER_COHORT, CatalogueGroup.TRAINING)
            )
        );

        // ---- courses ----
        courseRepository.saveAll(
            List.of(
                course("Software architecture fundamentals", 12, DeliveryMode.VIRTUAL, false, 14, 72),
                course("Angular for enterprise front ends", 9, DeliveryMode.IN_HOUSE, true, 11, 45),
                course("Spring Boot microservices", 14, DeliveryMode.VIRTUAL, true, 9, 61),
                course("Software testing & quality gates", 8, DeliveryMode.VIRTUAL, false, 17, 88),
                course("Cyber security awareness (all staff)", 5, DeliveryMode.VIRTUAL, false, 126, 93),
                course("Data visualization & reporting", 7, DeliveryMode.IN_HOUSE, false, 6, 20)
            )
        );

        // ---- projects + milestones ----
        seedProject(
            project(
                "JA-2401",
                "Insurance platform — claims module",
                ServicePillar.BESPOKE_SOLUTIONS,
                Status.GOOD,
                78,
                "12 Sep 2026",
                false,
                184000,
                141000,
                "Spring Boot, Angular, Kafka, MongoDB"
            )
                .client(bedrock)
                .lead(kojo),
            List.of(
                "Business case & friction map:done",
                "Mockup & review cycles:done",
                "Claims service implementation:done",
                "Integration testing:now",
                "Documentation & handover:next",
                "Training window:next"
            )
        );
        seedProject(
            project(
                "JA-2402",
                "Paperless transition — records digitisation",
                ServicePillar.DIGITAL_TRANSFORMATION,
                Status.WARN,
                46,
                "30 Oct 2026",
                false,
                96000,
                52000,
                "Elasticsearch, OCR pipeline, Angular"
            )
                .client(ports)
                .lead(kwabena),
            List.of(
                "Business case & friction map:done",
                "Records inventory:done",
                "Ingestion pipeline:now",
                "Search & retrieval UI:next",
                "Audit trail & ISO prep:next",
                "Training window:next"
            )
        );
        seedProject(
            project(
                "JA-2403",
                "ISO 27001 audit preparation",
                ServicePillar.CONSULTANCY,
                Status.GOOD,
                61,
                "05 Nov 2026",
                false,
                48000,
                27500,
                "Policy framework, Risk register, GAP analysis"
            )
                .client(izzi)
                .lead(kojo),
            List.of(
                "Scope & asset register:done",
                "Gap analysis:done",
                "Control implementation:now",
                "Internal audit:next",
                "Management review:next",
                "Certification support:next"
            )
        );
        seedProject(
            project(
                "JA-2404",
                "Inventory system rollout — 14 branches",
                ServicePillar.ENTERPRISE_INTEGRATION,
                Status.SERIOUS,
                34,
                "21 Dec 2026",
                false,
                212000,
                88000,
                "Spring Boot, Angular, Kafka, Docker"
            )
                .client(melcom)
                .lead(nii),
            List.of(
                "Branch survey:done",
                "Core catalogue migration:done",
                "Supplier integration:now",
                "Branch pilot (2 sites):next",
                "Full rollout:next",
                "Support handover:next"
            )
        );
        seedProject(
            project(
                "JA-2405",
                "Cyber threat intelligence pilot",
                ServicePillar.BESPOKE_SOLUTIONS,
                Status.CRIT,
                22,
                "28 Feb 2027",
                false,
                340000,
                71000,
                "Elasticsearch, Logstash, Kafka, D3, Kibana"
            )
                .client(nsd)
                .lead(kojo),
            List.of(
                "Source inventory:done",
                "Correlation rules v1:now",
                "Enrichment & parsers:next",
                "Visualisation layer:next",
                "Situation room UI:next",
                "Operator training:next"
            )
        );
        seedProject(
            project(
                "JA-2406",
                "Corporate web presence & CMS",
                ServicePillar.DIGITAL_TRANSFORMATION,
                Status.DONE,
                100,
                "14 Mar 2026",
                true,
                26000,
                24800,
                "CMS, HTML5, Responsive"
            )
                .client(afri)
                .lead(kwabena),
            List.of(
                "Business case:done",
                "Design & mockups:done",
                "Build:done",
                "Testing:done",
                "Documentation:done",
                "Training delivered:done"
            )
        );
        seedProject(
            project(
                "JA-2407",
                "Field data collection — afforestation",
                ServicePillar.CAPACITY_BUILDING,
                Status.GOOD,
                88,
                "19 Aug 2026",
                false,
                34000,
                29900,
                "GIS, Mobile capture, MongoDB"
            )
                .client(aaa)
                .lead(nii),
            List.of(
                "Field workflow analysis:done",
                "GIS schema:done",
                "Mobile capture app:done",
                "Volunteer training:now",
                "Reporting dashboards:next",
                "Handover:next"
            )
        );
        seedProject(
            project(
                "JA-2408",
                "Payments integration & reconciliation",
                ServicePillar.ENTERPRISE_INTEGRATION,
                Status.WARN,
                55,
                "07 Oct 2026",
                false,
                128000,
                79000,
                "Spring Boot, Kafka, Prometheus, Grafana"
            )
                .client(gahana)
                .lead(kwesi),
            List.of(
                "Integration audit:done",
                "Sandbox connectivity:done",
                "Reconciliation engine:now",
                "Failure replay:next",
                "Load testing:next",
                "Go-live support:next"
            )
        );

        // ---- support desk tickets ----
        ticketRepository.saveAll(
            List.of(
                ticket("SD-1188", "Claims PDF export renders blank on Safari", Status.CRIT, 4, TicketState.OPEN, 2, bedrock, kojo),
                ticket("SD-1187", "Kafka consumer lag on reconciliation topic", Status.SERIOUS, 8, TicketState.OPEN, 6, gahana, kwesi),
                ticket("SD-1186", "Add supplier bulk-import template", Status.WARN, 72, TicketState.OPEN, 24, melcom, nii),
                ticket("SD-1185", "Request: extra admin seat for finance team", Status.GOOD, 120, TicketState.OPEN, 24, izzi, kwabena),
                ticket("SD-1184", "GIS layer not refreshing after sync", Status.WARN, 72, TicketState.OPEN, 48, aaa, nii),
                ticket("SD-1183", "Grafana alert threshold tuning", Status.GOOD, 120, TicketState.CLOSED, 72, bedrock, kwesi),
                ticket("SD-1182", "Password policy update after ISO gap analysis", Status.WARN, 72, TicketState.CLOSED, 96, izzi, kojo)
            )
        );

        LOG.info(
            "Demo data seeded: {} clients, {} projects, {} tickets",
            clientRepository.count(),
            projectRepository.count(),
            ticketRepository.count()
        );
    }

    private void seedProject(Project project, List<String> milestones) {
        projectRepository.save(project);
        int position = 0;
        for (String spec : milestones) {
            int sep = spec.lastIndexOf(':');
            String title = spec.substring(0, sep);
            MilestoneState state = switch (spec.substring(sep + 1)) {
                case "done" -> MilestoneState.DONE;
                case "now" -> MilestoneState.NOW;
                default -> MilestoneState.NEXT;
            };
            milestoneRepository.save(new Milestone().title(title).state(state).position(position++).project(project));
        }
    }

    private Authority saveAuthority(String name) {
        Authority authority = new Authority();
        authority.setName(name);
        return authorityRepository.save(authority);
    }

    private User ensureUser(String login, String firstName, String lastName, String email, Set<Authority> authorities) {
        return userRepository.findOneByLogin(login).orElseGet(() -> {
            User user = new User();
            user.setLogin(login);
            user.setPassword(passwordEncoder.encode(DEMO_PASSWORD));
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);
            user.setActivated(true);
            user.setLangKey("en");
            user.setCreatedBy("system");
            user.setAuthorities(authorities);
            return userRepository.save(user);
        });
    }

    private TeamMember team(String name, String initials, String role, String qualification) {
        return new TeamMember().name(name).initials(initials).role(role).qualification(qualification).bio("");
    }

    private Client client(String name, Market sector, int since, Status health, long spend) {
        return new Client().name(name).sector(sector).clientSince(since).health(health).totalSpend(BigDecimal.valueOf(spend));
    }

    private ServiceItem svc(String code, String name, long rate, RateUnit unit, CatalogueGroup group) {
        return new ServiceItem().code(code).name(name).description(name).rate(BigDecimal.valueOf(rate)).unit(unit).serviceGroup(group);
    }

    private Course course(String name, int modules, DeliveryMode mode, boolean labBased, int enrolled, int progress) {
        return new Course()
            .name(name)
            .description(name)
            .moduleCount(modules)
            .mode(mode)
            .labBased(labBased)
            .enrolledCount(enrolled)
            .progress(progress);
    }

    private Project project(
        String reference,
        String name,
        ServicePillar pillar,
        Status status,
        int progress,
        String due,
        boolean delivered,
        long budget,
        long spent,
        String stack
    ) {
        return new Project()
            .reference(reference)
            .name(name)
            .pillar(pillar)
            .status(status)
            .progress(progress)
            .dueDate(parseDue(due))
            .delivered(delivered)
            .budget(BigDecimal.valueOf(budget))
            .spent(BigDecimal.valueOf(spent))
            .techStack(stack);
    }

    private Ticket ticket(
        String reference,
        String subject,
        Status priority,
        int slaHours,
        TicketState state,
        int ageHours,
        Client client,
        TeamMember owner
    ) {
        return new Ticket()
            .reference(reference)
            .subject(subject)
            .priority(priority)
            .slaHours(slaHours)
            .state(state)
            .openedAt(Instant.now().minus(ageHours, ChronoUnit.HOURS))
            .client(client)
            .owner(owner);
    }

    private static final Map<String, Integer> MONTHS = Map.ofEntries(
        Map.entry("Jan", 1),
        Map.entry("Feb", 2),
        Map.entry("Mar", 3),
        Map.entry("Apr", 4),
        Map.entry("May", 5),
        Map.entry("Jun", 6),
        Map.entry("Jul", 7),
        Map.entry("Aug", 8),
        Map.entry("Sep", 9),
        Map.entry("Oct", 10),
        Map.entry("Nov", 11),
        Map.entry("Dec", 12)
    );

    private LocalDate parseDue(String due) {
        String[] parts = due.split(" ");
        return LocalDate.of(Integer.parseInt(parts[2]), MONTHS.get(parts[1]), Integer.parseInt(parts[0]));
    }
}
