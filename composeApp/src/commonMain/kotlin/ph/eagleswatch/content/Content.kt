package ph.eagleswatch.content

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
