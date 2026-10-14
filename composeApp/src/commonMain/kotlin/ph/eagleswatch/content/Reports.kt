package ph.eagleswatch.content

import kotlin.random.Random

/** What a citizen is reporting. The first four appear on a project's page; the rest anywhere. */
enum class Subject(val label: String, val hint: String) {
    ThisProject("This project", "Its works, quality, schedule or money"),
    ProjectPerson("Someone on this project", "An official, engineer or contractor named here"),
    ProjectAgency("The agency in charge", "The office that proposed or carries out the project"),
    Project("A government project", "Roads, buildings, flood control and other works"),
    Lgu("An LGU", "A barangay, city, municipality or province"),
    PublicServant("A public servant", "An official or government employee"),
    Other("Something else", "Wrong information, a missing project or anything else"),
    ;

    val aboutProject: Boolean get() = this == ThisProject || this == Project
    val aboutPerson: Boolean get() = this == ProjectPerson || this == PublicServant
    val aboutOffice: Boolean get() = this == ProjectAgency || this == Lgu

    companion object {
        val onProject = listOf(ThisProject, ProjectPerson, ProjectAgency, Other)
        val general = listOf(Project, Lgu, PublicServant, Other)
    }
}

/** Ready-made answers for "What happened?", so most reports need no typing. */
fun Subject.issues(): List<String> = when {
    aboutProject -> listOf(
        "Work stopped or abandoned",
        "Behind schedule",
        "Poor quality or unsafe work",
        "Not built as described",
        "Project does not exist (ghost project)",
        "Cost looks too high",
        "Damage to homes, roads or farms",
        "Safety hazard at the site",
    )
    aboutPerson -> listOf(
        "Asked for money or a bribe",
        "Absent during office hours",
        "Rude or unhelpful service",
        "Delayed or refused service",
        "Abuse of authority",
        "Fixer activity",
        "Conflict of interest",
        "Using government property for personal use",
    )
    aboutOffice -> listOf(
        "Budget or projects not posted publicly",
        "Irregular bidding or procurement",
        "Funds used for other purposes",
        "No action on requests or complaints",
        "Using government property for personal use",
        "Services not delivered",
    )
    else -> listOf(
        "Wrong information in the app",
        "A project is missing",
        "Suggestion for The Eagle's Watch",
    )
}

/** Roles to pick for a public servant, instead of typing a title. */
val publicServantRoles = listOf(
    "Barangay official",
    "Barangay tanod",
    "City or municipal employee",
    "Provincial employee",
    "National agency employee",
    "Police officer",
    "Traffic enforcer",
    "Public school teacher",
    "Health worker",
)

val lguLevels = listOf("Barangay", "City", "Municipality", "Province")

val reportPlaces = listOf(
    "Barangay hall",
    "City or municipal hall",
    "Provincial capitol",
    "Agency office",
    "Public school",
    "Health center or hospital",
    "Road or public place",
    "Online or by phone",
)

val reportTimes = listOf(
    "Today",
    "Yesterday",
    "This week",
    "This month",
    "Earlier this year",
    "Last year or earlier",
    "It's still happening",
)

/** Facts a reporter can tick instead of describing them. */
val reportFacts = listOf(
    "I have photos or videos",
    "I have receipts or documents",
    "Others saw it too",
    "I reported this before",
    "It happened more than once",
)

/** Answer for any question the reporter could not or would not answer. */
const val NOT_SURE = "Not sure"

/** A report as the citizen filed it. */
data class Report(
    val reference: String,
    val filed: Day,
    val subject: Subject,
    /** Which project, by id, when the report is about one in the app. */
    val projectId: String?,
    /** The project, office or person in plain words, e.g. "Engr. Paolo Ramos, Project Engineer". */
    val target: String,
    /** Context for the target: who is responsible for a project, or the project a person works on. */
    val involved: String,
    val issues: List<String>,
    val place: String,
    val time: String,
    val facts: List<String>,
    val details: String,
    /** Null when the citizen chose to stay anonymous. */
    val contact: String?,
)

/** "EW-261014-4821": the date it was filed and four random digits, easy to read out on a hotline. */
fun newReference(day: Day): String {
    val date = "${day.year % 100}".padStart(2, '0') + "${day.month}".padStart(2, '0') + "${day.day}".padStart(2, '0')
    return "EW-$date-${Random.nextInt(1000, 10000)}"
}
