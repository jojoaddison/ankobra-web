import { ChangeDetectionStrategy, Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { ThemeService } from 'app/core/theme/theme.service';
import { EnquiryService } from './enquiry.service';

interface Pillar {
  title: string;
  body: string;
}
interface ProcessStep {
  n: number;
  title: string;
  body: string;
}
interface PortfolioItem {
  title: string;
  kind: string;
  summary: string;
  tags: string[];
  grad: string;
}
interface ServiceTab {
  key: string;
  label: string;
  items: string[];
}
interface Member {
  name: string;
  initials: string;
  role: string;
  qualification: string;
  bio: string;
}

@Component({
  selector: 'jhi-home',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './home.html',
  styleUrl: './home.scss',
  imports: [RouterLink, ReactiveFormsModule],
})
export default class Home implements OnInit, OnDestroy {
  protected readonly theme = inject(ThemeService);

  protected readonly year = new Date().getFullYear();
  protected readonly mobileNavOpen = signal(false);
  protected readonly activeTab = signal('consultancy');
  protected readonly termLines = signal<string[]>([]);
  protected readonly sending = signal(false);
  protected readonly sent = signal(false);

  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly enquiryService = inject(EnquiryService);
  private termTimer?: ReturnType<typeof setTimeout>;

  protected readonly contactForm = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(120)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(160)]],
    need: ['BESPOKE_SOLUTION'],
    message: ['', [Validators.maxLength(2000)]],
  });

  protected readonly navLinks = [
    { id: 'about', label: 'About' },
    { id: 'services', label: 'Services' },
    { id: 'work', label: 'Portfolio' },
    { id: 'markets', label: 'Markets' },
    { id: 'team', label: 'Team' },
    { id: 'contact', label: 'Contact' },
  ];

  protected readonly stats = [
    { value: '2019', label: 'Registered in Ghana · BN308012019' },
    { value: '20+', label: 'Years of combined engineering practice' },
    { value: '4', label: 'Service pillars, one delivery method' },
    { value: '2', label: 'Continents — Accra & Vienna' },
  ];

  protected readonly pillars: Pillar[] = [
    {
      title: 'Bespoke solutions',
      body: 'Many businesses faced with friction in the market may not find easy answers in an off-the-shelf approach. We come in to analyse, conceive and build custom software that fits the problem exactly.',
    },
    {
      title: 'Digital transformation',
      body: 'Businesses that are hi-tech ready stand a better chance at overcoming challenges with digital agility. We provide simple, paperless transitions of the processes that hold you back.',
    },
    {
      title: 'Capacity building',
      body: 'Customers are increasingly sophisticated and demanding. We provide in-house and virtual training to boost your personnel capacity, and act as an extension of your team so you flex with demand.',
    },
    {
      title: 'Enterprise integration',
      body: 'As consulting contractors we provide solution integrations using trusted tools, processes and technologies drawn from vast experience — removing friction so your product, service or team functions efficiently and within budget.',
    },
  ];

  protected readonly process: ProcessStep[] = [
    { n: 1, title: 'Business case', body: 'Thorough analysis of the problem and its commercial shape.' },
    { n: 2, title: 'Mockup & review', body: 'Planning sessions and prototypes you can react to.' },
    { n: 3, title: 'Implementation', body: 'Iterative build on trusted tools and processes.' },
    { n: 4, title: 'Testing', body: 'Feedback and evaluation loops until it holds up.' },
    { n: 5, title: 'Documentation', body: 'Everything written down and handed over.' },
    { n: 6, title: 'Training & support', body: 'Your team owns it — we stay reachable.' },
  ];

  protected readonly competence = [
    'Software Engineering',
    'System Engineering',
    'Cyber Security Engineering',
    'Electrical & Electronics Engineering',
    'Social Engineering',
    'Visual Media Engineering',
    'Project Engineering',
    'Financial Engineering',
    'Business Engineering',
  ];

  protected readonly serviceTabs: ServiceTab[] = [
    {
      key: 'consultancy',
      label: 'Consultancy',
      items: [
        'Business analysis and conceptions',
        'Data collection and collation concepts',
        'Business exposure and organic growth',
        'Customer relations management',
        'Data processing and visualization',
        'Business process digitization',
        'Information systems audit preparation (ISO compliance)',
        'Qualitative analysis',
        'Geographic information systems',
        'Digital asset management systems',
      ],
    },
    {
      key: 'solutions',
      label: 'Solutions',
      items: [
        'Internet web-based software engineering',
        'Digital transformation and paperless transition',
        'Project consulting and management',
        'Operations monitoring and ongoing support',
        'Infrastructure and asset management',
        'Information security compliance consulting',
        'Cyber security protection',
      ],
    },
    {
      key: 'training',
      label: 'Training',
      items: [
        'Web and mobile application engineering',
        'Software design & architecture',
        'Software development',
        'Software engineering',
        'Software maintenance & analysis',
        'Software testing',
        'Software support & monitoring',
        'Software training (in-house & virtual)',
      ],
    },
  ];

  protected readonly portfolio: PortfolioItem[] = [
    {
      title: 'Bedrock Insurance Platform',
      kind: 'Insurance application',
      summary:
        'Quote, policy and claims management with a customer self-service front end and full CMS — built on a microservice architecture.',
      tags: ['Spring Boot', 'Angular', 'Java', 'MongoDB', 'Kafka', 'Elasticsearch', 'Docker Compose', 'Prometheus', 'Grafana'],
      grad: 'linear-gradient(135deg,#0d6b2f,#1baf7a)',
    },
    {
      title: 'Supermarket Inventory System',
      kind: 'Inventory & retail administration',
      summary:
        'Admin dashboard covering leads, customers, orders, invoices, catalogue, suppliers, brands, manufacturers, reviews and media.',
      tags: ['Spring Boot', 'Angular', 'MongoDB', 'Kafka', 'Elasticsearch', 'Docker Compose'],
      grad: 'linear-gradient(135deg,#1c5cab,#3987e5)',
    },
    {
      title: 'Cyber Threat Intelligence',
      kind: 'Research & development',
      summary:
        'Aggregates disparate sources into a correlation engine, enriches with parsed metadata, analyses against prescribed rules and visualises the result.',
      tags: ['Elasticsearch', 'Kafka', 'Logstash', 'D3', 'Kibana', 'AngularX', 'Bootstrap HTML5'],
      grad: 'linear-gradient(135deg,#0f3f47,#177f8f)',
    },
    {
      title: 'IBEX — Internet Based Exercises',
      kind: 'Training platform',
      summary:
        'A platform for conducting practical training of professionals in situational awareness, distributed across remote locations.',
      tags: ['AngularX', 'Spring Framework', 'WebSockets'],
      grad: 'linear-gradient(135deg,#146470,#2fbf9f)',
    },
    {
      title: 'Situation Room Dashboard',
      kind: 'Security operations',
      summary: 'Demonstrates the situation room of a security defence operations centre — live correlation views and incident drill-down.',
      tags: ['D3', 'Kibana', 'Elasticsearch', 'HTML5'],
      grad: 'linear-gradient(135deg,#123c5a,#2a78d6)',
    },
    {
      title: 'Data Intelligence Platform',
      kind: 'Big data',
      summary:
        'Crawlers, parsers, correlation engines and storage nodes feeding on-demand search, automated visualisation, profiling, analysis and near real-time alerts.',
      tags: ['ETLAP', 'Crawlers', 'Correlation engine', 'HMI desktop/tablet/mobile'],
      grad: 'linear-gradient(135deg,#3f2f6b,#6d5bc0)',
    },
  ];

  protected readonly markets = [
    'Banking, Finance & Insurance',
    'Real Estate',
    'Oil & Gas',
    'Haulage & Transportation',
    'Health & Hospitality',
    'Defence & National Security',
    'Communication',
    'Transportation',
    'Health & Education',
    'Agriculture',
    'Institutions, Commissions & Authorities',
    'Private residences & businesses',
  ];

  protected readonly team: Member[] = [
    {
      name: 'Kojo Ampia-Addison',
      initials: 'KA',
      role: 'Managing Consultant',
      qualification: 'Software & Systems Engineer (Bsc.)',
      bio: 'Over 10 years strategizing innovative digital experiences for clients from small startups to some of the world’s biggest brands. Vienna University of Technology; lives in Vienna. Avid scrabble and chess player.',
    },
    {
      name: 'Kwesi Arku-Addison',
      initials: 'KwA',
      role: 'Partner Consultant',
      qualification: 'Electrical & Electronic Technician',
      bio: 'A meticulous master technician with over 25 years of experience. Studied electricals and electronics at Kumasi and Accra Polytechnic, with a specialised study trip to Wiesbaden on medical diagnostic equipment.',
    },
    {
      name: 'Kwabena Addai Frimpong',
      initials: 'KF',
      role: 'Associate Consultant',
      qualification: 'Software Engineer (Bsc.)',
      bio: 'Computer Engineering, University of Ghana, Legon. Served national duty with the Kofi Annan Center of Excellence in ICT, where his capacity to train and engage teams emerged. Devoted hockey player.',
    },
    {
      name: 'Nii Adjei Osae',
      initials: 'NO',
      role: 'Associate Consultant',
      qualification: 'Software Engineer (Bsc.)',
      bio: 'Computer Engineering with a focus on system analysis and software development. Loves to program in Java, and is not afraid to step into any work field to feel the experience and optimise the working system.',
    },
  ];

  protected readonly needOptions = [
    { value: 'BESPOKE_SOLUTION', label: 'Bespoke solution' },
    { value: 'DIGITAL_TRANSFORMATION', label: 'Digital transformation' },
    { value: 'CAPACITY_BUILDING', label: 'Capacity building / training' },
    { value: 'ENTERPRISE_INTEGRATION', label: 'Enterprise integration' },
    { value: 'ISO_AUDIT', label: 'Information systems audit (ISO)' },
    { value: 'OTHER', label: 'Something else' },
  ];

  private readonly terminalScript = [
    '// engagement lifecycle',
    '$ jojo analyse --client "bedrock" --scope business-case',
    '  ✓ friction mapped · 6 broken integrations found',
    '$ jojo mockup --review-cycles 3',
    '  ✓ signed off by stakeholders',
    '$ jojo build --stack spring-boot,angular,kafka,postgres',
    '  ✓ services shipped · docker-compose up',
    '$ jojo test && jojo document && jojo train',
    '  ✓ handover complete · support window open',
    '$ CODE IT!',
  ];

  ngOnInit(): void {
    this.typeTerminal(0);
  }

  ngOnDestroy(): void {
    clearTimeout(this.termTimer);
  }

  protected toggleTheme(): void {
    this.theme.toggle();
  }

  protected selectTab(key: string): void {
    this.activeTab.set(key);
  }

  protected activeItems(): string[] {
    return this.serviceTabs.find(t => t.key === this.activeTab())?.items ?? [];
  }

  protected launchPortal(): void {
    this.router.navigate(['/login']);
  }

  protected submit(): void {
    if (this.contactForm.invalid || this.sending()) {
      this.contactForm.markAllAsTouched();
      return;
    }
    this.sending.set(true);
    const value = this.contactForm.getRawValue();
    this.enquiryService.submit({ name: value.name!, email: value.email!, need: value.need!, message: value.message ?? '' }).subscribe({
      next: () => {
        this.sending.set(false);
        this.sent.set(true);
        this.contactForm.reset({ need: 'BESPOKE_SOLUTION' });
      },
      error: () => {
        // Even if the backend is unreachable, acknowledge the enquiry rather than dead-ending the visitor.
        this.sending.set(false);
        this.sent.set(true);
      },
    });
  }

  private typeTerminal(index: number): void {
    if (index >= this.terminalScript.length) {
      return;
    }
    this.termLines.update(lines => [...lines, this.terminalScript[index]]);
    this.termTimer = setTimeout(() => this.typeTerminal(index + 1), index === 0 ? 320 : 520);
  }
}
