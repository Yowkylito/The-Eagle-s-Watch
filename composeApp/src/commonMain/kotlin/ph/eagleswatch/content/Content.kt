package ph.eagleswatch.content

data class Principle(val title: String, val body: String)

val principles: List<Principle> = listOf(
    Principle(
        "Official records only",
        "Figures come from public documents: budgets, procurement awards, audit reports and local disclosure postings.",
    ),
    Principle(
        "Every figure is labelled",
        "Each value says whether it was independently verified, reported by the agency, reported by a citizen, or is not available.",
    ),
    Principle(
        "Status, work and money are separate",
        "A project's status, its physical progress and the funds released are shown as three facts, never as one number.",
    ),
    Principle(
        "Corrections are welcome",
        "Every record has a way to report wrong or outdated information. Changes are listed in its revision history.",
    ),
)

/** Public sources the project plans to connect, with their public portals. */
val publicSources: List<DataSource> = listOf(
    DataSource("General Appropriations Act and DBM budget documents", "https://www.dbm.gov.ph"),
    DataSource("PhilGEPS procurement notices and awards", "https://www.philgeps.gov.ph"),
    DataSource("Commission on Audit annual audit reports", "https://www.coa.gov.ph"),
    DataSource("DILG Full Disclosure Policy Portal (LGU postings)", "https://fdpp.dilg.gov.ph"),
    DataSource("DPWH project postings", "https://www.dpwh.gov.ph"),
    DataSource("Open Data Philippines", "https://data.gov.ph"),
)

/** A rule of Philippine public finance, with the law that sets it. These are real, not sample data. */
data class Fact(val text: String, val source: String)

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
        "Government purchases must be posted on PhilGEPS, the public procurement portal.",
        "New Government Procurement Act (RA 12009)",
    ),
)

const val GITHUB_URL = "https://github.com/yowkylito/the-eagle-s-watch"
const val SUGGEST_SOURCE_URL = "$GITHUB_URL/issues/new"
