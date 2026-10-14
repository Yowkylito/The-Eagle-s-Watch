package ph.eagleswatch.content

/** One stop on the trail from the taxpayer up to the highest office. */
data class Tier(
    val index: String,
    val name: String,
    val short: String,
    val tagline: String,
    val reveals: List<String>,
    val sources: String,
    /** Placeholder allocation of a sample ₱100 at this level. Null for the taxpayer, who is the source. */
    val sample: List<Allocation>? = null,
)

data class Allocation(val label: String, val pesos: Int)

val trail: List<Tier> = listOf(
    Tier(
        index = "00",
        name = "Citizens",
        short = "CITIZENS",
        tagline = "Where every peso begins. Income tax, VAT, and local business and property taxes all start with you.",
        reveals = listOf(
            "What kinds of taxes fund each level of government",
            "How collected taxes are shared between national and local government",
        ),
        sources = "BIR, BOC and local treasurers' public reports",
    ),
    Tier(
        index = "01",
        name = "Barangay",
        short = "BARANGAY",
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
        short = "CITY / MUN.",
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
        short = "PROVINCE",
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
        short = "AGENCIES",
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
        short = "PRESIDENT",
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

data class Principle(val index: String, val verb: String, val title: String, val body: String)

val principles: List<Principle> = listOf(
    Principle(
        "01", "SOURCE", "Official records only",
        "We connect public documents: budgets, audit reports, procurement awards and local disclosure postings.",
    ),
    Principle(
        "02", "TRACE", "One trail, every level",
        "Funds are linked from the national budget down to the barangay, so a single peso can be followed across offices.",
    ),
    Principle(
        "03", "VERIFY", "Receipts for every figure",
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

const val GITHUB_URL = "https://github.com/yowkylito/the-eagle-s-watch"
