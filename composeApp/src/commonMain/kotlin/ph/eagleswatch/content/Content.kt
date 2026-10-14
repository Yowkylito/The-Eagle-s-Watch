package ph.eagleswatch.content

/** One stop on the trail from the taxpayer up to the highest office. */
data class Tier(
    val index: String,
    val name: String,
    /** One word for the rail, where space is tight. */
    val short: String,
    val tagline: String,
    val reveals: List<String>,
    val sources: String,
    /** Placeholder allocation of a sample ₱100 at this level. Null for the taxpayer, who is the source. */
    val sample: List<Allocation>? = null,
    /** Who does the spending, to finish "How ... would split your pesos". */
    val spender: String = name,
)

data class Allocation(val label: String, val pesos: Int)

val trail: List<Tier> = listOf(
    Tier(
        index = "00",
        name = "Citizens",
        short = "Citizens",
        tagline = "Where every peso begins. Set roughly what you paid in taxes this year, then follow it up the trail.",
        reveals = emptyList(),
        sources = "BIR, BOC and local treasurers' public reports",
    ),
    Tier(
        index = "01",
        name = "Barangay",
        spender = "your barangay",
        short = "Barangay",
        tagline = "The first level of government, and the closest to your daily life.",
        reveals = listOf(
            "Funds received and how the barangay budget is planned",
            "Projects implemented and their status",
            "Spending against the approved budget",
        ),
        sources = "LGU Full Disclosure Policy postings",
        sample = listOf(
            Allocation("Health & nutrition", 22),
            Allocation("Peace & order", 18),
            Allocation("Infrastructure", 26),
            Allocation("Youth & sports", 16),
            Allocation("Administration", 18),
        ),
    ),
    Tier(
        index = "02",
        name = "City / Municipality",
        spender = "your city or town",
        short = "City",
        tagline = "Local taxes plus the national tax allotment, spent on the services you use.",
        reveals = listOf(
            "Annual budget and the income it relies on",
            "Procurement and infrastructure projects",
            "Audit observations on local accounts",
        ),
        sources = "LGU postings, COA annual audit reports",
        sample = listOf(
            Allocation("Social services", 24),
            Allocation("Infrastructure", 28),
            Allocation("Health", 17),
            Allocation("Education support", 11),
            Allocation("Administration", 20),
        ),
    ),
    Tier(
        index = "03",
        name = "Province",
        spender = "your province",
        short = "Province",
        tagline = "Roads, hospitals and programs that span many towns.",
        reveals = listOf(
            "Provincial budget and capital projects",
            "Funds passed to component cities and municipalities",
            "Procurement notices and awards",
        ),
        sources = "LGU postings, PhilGEPS, COA",
        sample = listOf(
            Allocation("Roads & bridges", 32),
            Allocation("Hospitals", 25),
            Allocation("Agriculture", 14),
            Allocation("Social welfare", 12),
            Allocation("Administration", 17),
        ),
    ),
    Tier(
        index = "04",
        name = "National Agencies",
        spender = "national agencies",
        short = "National",
        tagline = "Departments and bureaus carrying out the national budget.",
        reveals = listOf(
            "General Appropriations Act line items by agency",
            "Project status and spending to date",
            "Procurement notices and awards",
        ),
        sources = "DBM, GAA, PhilGEPS, COA",
        sample = listOf(
            Allocation("Education", 22),
            Allocation("Public works", 21),
            Allocation("Health", 11),
            Allocation("Social protection", 15),
            Allocation("Debt & others", 31),
        ),
    ),
    Tier(
        index = "05",
        name = "Office of the President",
        spender = "the Office of the President",
        short = "President",
        tagline = "The top of the trail: the office and the agencies attached to it.",
        reveals = listOf(
            "Office budget and attached agencies",
            "Special and lump-sum funds, as published",
            "Audit findings from the Commission on Audit",
        ),
        sources = "DBM, GAA, COA",
        sample = listOf(
            Allocation("Office operations", 28),
            Allocation("Attached agencies", 34),
            Allocation("Special funds", 12),
            Allocation("Programs", 16),
            Allocation("Other", 10),
        ),
    ),
)

data class Principle(val title: String, val body: String)

val principles: List<Principle> = listOf(
    Principle(
        "Official records only",
        "We connect public documents: budgets, audit reports, procurement awards and local disclosure postings.",
    ),
    Principle(
        "One trail, every level",
        "Funds are linked from the national budget down to the barangay, so a single peso can be followed across offices.",
    ),
    Principle(
        "Receipts for every figure",
        "Each number links back to the document it came from. Where a source is missing, we say so.",
    ),
)

val plannedSources: List<String> = listOf(
    "General Appropriations Act",
    "COA audit reports",
    "PhilGEPS",
    "LGU Full Disclosure",
    "DBM budget documents",
    "Open data portals",
)

/** Where the citizen's pesos come from, shown on the first stop of the trail. */
data class TaxKind(val name: String, val body: String)

val taxKinds: List<TaxKind> = listOf(
    TaxKind("Income tax", "Withheld from your pay, or filed once a year if you run a business."),
    TaxKind("Value-added tax", "12% of the price of most goods and services you buy."),
    TaxKind("Local taxes", "Business permits, real property tax and fees paid to your city or town."),
)

/** Starting amount for "your taxes this year", before the citizen sets their own. */
const val DEFAULT_TAX_PESOS = 10_000L
const val MIN_TAX_PESOS = 1_000L
const val MAX_TAX_PESOS = 1_000_000L
val taxPresets: List<Long> = listOf(5_000L, 10_000L, 50_000L, 250_000L)

/** A rule of Philippine public finance, with the law that sets it. These are real, not sample data. */
data class Fact(val text: String, val source: String)

/** One is shown each day under "Alam mo ba?" so there is something new on every visit. */
val facts: List<Fact> = listOf(
    Fact(
        "Barangays get 20% of the national tax share set aside for local governments. Provinces and cities get 23% each, municipalities 34%.",
        "Local Government Code, Sec. 285",
    ),
    Fact(
        "Local governments receive 40% of national taxes, based on what was collected three years earlier.",
        "Local Government Code, Sec. 284; Mandanas-Garcia ruling",
    ),
    Fact(
        "At least 20% of every local government's tax allotment must go to development projects.",
        "Local Government Code, Sec. 287",
    ),
    Fact(
        "Every barangay sets aside 10% of its general fund for the Sangguniang Kabataan.",
        "SK Reform Act of 2015 (RA 10742), Sec. 20",
    ),
    Fact(
        "Local governments reserve at least 5% of their regular income for disaster risk reduction and response.",
        "RA 10121, Sec. 21",
    ),
    Fact(
        "Every agency and local government must spend at least 5% of its budget on gender and development.",
        "Magna Carta of Women (RA 9710), Sec. 36",
    ),
    Fact(
        "Local governments must post their budgets, spending and procurement where the public can see them.",
        "DILG Full Disclosure Policy",
    ),
    Fact(
        "The Commission on Audit examines the accounts of every government office, and its annual audit reports are public.",
        "1987 Constitution, Art. IX-D",
    ),
    Fact(
        "The national budget becomes law each year as the General Appropriations Act.",
        "1987 Constitution, Art. VI, Sec. 25",
    ),
    Fact(
        "The first ₱250,000 of a person's taxable income each year is not taxed.",
        "TRAIN Law (RA 10963)",
    ),
    Fact(
        "VAT is 12% of the price of most goods and services sold in the country.",
        "National Internal Revenue Code, Sec. 106 and 108",
    ),
    Fact(
        "Government purchases must be posted on PhilGEPS, the public procurement portal.",
        "New Government Procurement Act (RA 12009)",
    ),
)

const val GITHUB_URL = "https://github.com/yowkylito/the-eagle-s-watch"
const val SUGGEST_SOURCE_URL = "$GITHUB_URL/issues/new"
