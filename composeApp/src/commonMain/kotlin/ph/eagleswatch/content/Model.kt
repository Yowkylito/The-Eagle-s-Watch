package ph.eagleswatch.content

/** What a project builds. Drives the type filter and the category glyph. */
enum class Category(val label: String) {
    Roads("Roads"),
    Bridges("Bridges"),
    Schools("Schools"),
    Health("Health"),
    Water("Water"),
    FloodControl("Flood control"),
    Irrigation("Irrigation"),
    Buildings("Buildings"),
}

/** Which level of government owns the project. */
enum class Level(val label: String) {
    Barangay("Barangay"),
    Municipal("City / municipal"),
    Provincial("Provincial"),
    National("National"),
}

/**
 * Reported status of the works. Status, physical progress and financial progress are three separate
 * facts: a project can be "Ongoing" at 40% physical with 80% of its funds released.
 */
enum class Status(val label: String) {
    Planned("Planned"),
    Ongoing("Ongoing"),
    Delayed("Delayed"),
    Suspended("Suspended"),
    Completed("Completed"),
}

/** How a piece of information was established. Every figure in the app carries one of these. */
enum class Verification(val label: String) {
    /** Checked against an independent source: a site visit, an audit report, or a second record. */
    Verified("Verified"),

    /** Taken from the implementing or proposing agency's own posting, unchecked. */
    AgencyReported("Agency-reported"),

    /** Submitted by a citizen through the app, unchecked. */
    CommunityReported("Community-reported"),

    /** Nobody has supplied this information. */
    Unavailable("Not available"),
}

/** Where a figure came from and the date it was true. */
data class Attribution(val verification: Verification, val source: String, val asOf: Day? = null)

data class Coordinates(val lat: Double, val lon: Double)

enum class Precision(val label: String) {
    Site("site location"),
    Municipality("municipality centre, approximate"),
}

data class Place(
    /** Barangay name without the "Brgy." prefix, or null when the project spans several. */
    val barangay: String?,
    val municipality: String,
    val province: String,
    val region: String,
    val coordinates: Coordinates?,
    val precision: Precision = Precision.Municipality,
) {
    /** "Guagua, Pampanga" */
    val short: String get() = "$municipality, $province"

    /** "Brgy. San Isidro, Guagua, Pampanga" */
    val full: String get() = listOfNotNull(barangay?.let { "Brgy. $it" }, municipality, province).joinToString(", ")
}

data class Contract(
    /** Approved contract amount in pesos, or null when no award is on file. */
    val amount: Long?,
    val fundingSource: String?,
    /** Contract or procurement reference, as published. */
    val reference: String?,
    val attribution: Attribution,
)

data class Schedule(
    val start: Day?,
    /** The completion date in the contract. */
    val targetEnd: Day?,
    /** A later date set by a time extension or revised schedule, if any. */
    val revisedEnd: Day?,
    /** When the works were actually completed, for finished projects. */
    val actualEnd: Day?,
    val attribution: Attribution,
) {
    /** The date the project is currently expected to finish. */
    val currentEnd: Day? get() = revisedEnd ?: targetEnd
}

/** Physical accomplishment as reported on a date, with who reported it. */
data class PhysicalProgress(val percent: Int, val reportedOn: Day, val attribution: Attribution)

/** Funds disbursed against the contract as of a date. */
data class FinancialProgress(val disbursed: Long, val asOf: Day, val attribution: Attribution)

data class Progress(
    /** What the agency says has been built. */
    val reported: PhysicalProgress?,
    /** What an independent check found, if one has happened. */
    val independent: PhysicalProgress?,
    val financial: FinancialProgress?,
)

enum class PartyRole(val label: String) {
    Implementing("Implementing agency"),
    Proposing("Proposing office"),
    Contractor("Contractor"),
    Official("Official"),
}

/** An organisation or person named on the project, as the source lists them. */
data class Party(val role: PartyRole, val name: String, val detail: String? = null, val attribution: Attribution)

/** A dated entry on the project's timeline. [percent] is the physical progress reported with it, if any. */
data class Update(val date: Day, val title: String, val body: String, val percent: Int?, val attribution: Attribution)

/** A site photograph on file. Rendered inline once an image loader is wired in; until then it opens in the browser. */
data class Photo(val url: String, val takenOn: Day?, val caption: String, val attribution: Attribution)

enum class DocumentKind(val label: String) {
    Budget("Budget line item"),
    Procurement("Procurement notice"),
    Award("Notice of award"),
    Contract("Contract"),
    ProgramOfWork("Program of work"),
    Inspection("Inspection report"),
    Audit("Audit observation"),
    Completion("Certificate of completion"),
    Minutes("Minutes"),
    Other("Document"),
}

/** An official document the record relies on. [url] is null when the link is not yet on file. */
data class Document(val kind: DocumentKind, val title: String, val date: Day?, val url: String?, val attribution: Attribution)

/** One change to this record, newest last. */
data class Revision(val date: Day, val summary: String, val by: String)

/** A public source the record draws on. */
data class DataSource(val name: String, val url: String?)

data class Project(
    val id: String,
    val title: String,
    val category: Category,
    val level: Level,
    val status: Status,
    val place: Place,
    /** Scope of work in plain words, as the source describes it. */
    val description: String,
    val descriptionAttribution: Attribution,
    val contract: Contract,
    val schedule: Schedule,
    val progress: Progress,
    val parties: List<Party>,
    val updates: List<Update>,
    val photos: List<Photo>,
    val documents: List<Document>,
    val sources: List<DataSource>,
    val revisions: List<Revision>,
    /** When the record was last checked against its sources. Null means never. */
    val lastChecked: Day?,
    /** True for development fixtures. The UI labels these on every surface. */
    val sample: Boolean,
) {
    val implementingAgency: Party? get() = parties.firstOrNull { it.role == PartyRole.Implementing }
    val proposingOffice: Party? get() = parties.firstOrNull { it.role == PartyRole.Proposing }
    val contractor: Party? get() = parties.firstOrNull { it.role == PartyRole.Contractor }
    val officials: List<Party> get() = parties.filter { it.role == PartyRole.Official }

    val latestUpdate: Update? get() = updates.maxByOrNull { it.date }

    /** When anything about this record last changed. */
    val recordUpdated: Day? get() = revisions.maxOfOrNull { it.date } ?: latestUpdate?.date

    /** Share of the contract period that has passed by [today], or null when the dates are missing. */
    fun timeElapsed(today: Day): Float? {
        val start = schedule.start ?: return null
        val end = schedule.currentEnd ?: return null
        val total = (end.epochDay - start.epochDay).coerceAtLeast(1)
        return ((today.epochDay - start.epochDay).toFloat() / total).coerceIn(0f, 1f)
    }

    /** Case-insensitive match on the fields a citizen is likely to type. */
    fun matches(query: String): Boolean {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return true
        val haystack = listOf(
            title, id, place.full, place.region, category.label, level.label, status.label,
        ) + parties.map { it.name } + listOfNotNull(contract.reference, contract.fundingSource)
        return haystack.any { it.lowercase().contains(q) }
    }
}
