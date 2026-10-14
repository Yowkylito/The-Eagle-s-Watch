package ph.eagleswatch.content

/** What a project builds. Drives the Explore tabs and the illustration on every picture. */
enum class Category(val label: String) {
    Roads("Roads"),
    Bridges("Bridges"),
    Schools("Schools"),
    Health("Health"),
    Water("Water"),
    FloodControl("Flood control"),
    Farms("Farms"),
    Buildings("Buildings"),
}

/** Which level of government owns the project. */
enum class Level(val label: String) {
    Barangay("Barangay"),
    City("City / Municipal"),
    Province("Province"),
    National("National"),
}

enum class Status(val label: String) {
    Planned("Planned"),
    Ongoing("Ongoing"),
    Delayed("Delayed"),
    Completed("Completed"),
}

/** Someone accountable for the project, as the posting agency lists them. */
data class Person(val name: String, val role: String, val office: String) {
    val initials: String
        get() = name.split(" ").filter { it.first().isUpperCase() && !it.endsWith(".") }
            .let { listOfNotNull(it.firstOrNull(), it.lastOrNull()) }
            .joinToString("") { it.first().toString() }
}

/** A dated post from the agency. [stage] is how far along the work was, 0 to 1, for its picture. */
data class Update(val date: Day, val title: String, val body: String, val stage: Float)

data class Feedback(val name: String, val place: String, val date: Day, val text: String, val liked: Boolean)

data class Project(
    val id: String,
    val title: String,
    val category: Category,
    val level: Level,
    val status: Status,
    /** "Brgy. San Isidro, Guagua, Pampanga" */
    val place: String,
    /** Agency that proposed the project. */
    val proposedBy: String,
    /** Agency that carries it out, when different from the proposer. */
    val implementedBy: String,
    val contractor: String?,
    val budget: Long,
    /** Funds released so far. */
    val released: Long,
    /** Where the money comes from, e.g. the year's General Appropriations Act. */
    val fundedBy: String,
    val start: Day,
    val end: Day,
    /** Physical work done, 0 to 100, as last reported. */
    val progress: Int,
    val purpose: String,
    val article: List<String>,
    val people: List<Person>,
    val updates: List<Update>,
    val feedback: List<Feedback>,
    val likes: Int,
    val dislikes: Int,
    val records: List<String>,
    /** Picks the sky and light in this project's pictures so cards don't all look alike. */
    val palette: Int,
) {
    val lead: Person get() = people.first()
    val lastUpdate: Update? get() = updates.maxByOrNull { it.date }
    val durationDays: Long get() = end.epochDay - start.epochDay

    /** Share of the planned time already used, 0 to 1. */
    fun elapsed(today: Day): Float =
        ((today.epochDay - start.epochDay).toFloat() / durationDays.coerceAtLeast(1)).coerceIn(0f, 1f)
}

/** One picture of a project: the illustration at a given stage, and what it shows. */
data class Picture(val stage: Float, val caption: String)

/** Pictures newest first: one per update, or the design perspective for a project not yet started. */
val Project.pictures: List<Picture>
    get() {
        val shots = updates.sortedByDescending { it.date }.map { Picture(it.stage, "${it.date.format()} · ${it.title}") }
        return if (status == Status.Planned) listOf(Picture(1f, "Design perspective")) + shots else shots
    }

private val SampleRecords = listOf(
    "Budget line item",
    "Notice of award (PhilGEPS)",
    "Program of work and detailed estimates",
)

val projects: List<Project> = listOf(
    Project(
        id = "san-isidro-bridge",
        title = "San Isidro Bridge rehabilitation",
        category = Category.Bridges,
        level = Level.National,
        status = Status.Ongoing,
        place = "Brgy. San Isidro, Guagua, Pampanga",
        proposedBy = "DPWH Pampanga 1st District Engineering Office",
        implementedBy = "DPWH Pampanga 1st District Engineering Office",
        contractor = "Tala Builders Inc.",
        budget = 48_500_000,
        released = 31_200_000,
        fundedBy = "General Appropriations Act 2025",
        start = Day(2025, 3, 3),
        end = Day(2026, 12, 15),
        progress = 78,
        purpose = "Replaces the cracked deck and weak piers of a 60-metre bridge that farm trucks, school buses and " +
            "tricycles cross every day, and raises it above recent flood levels.",
        article = listOf(
            "The San Isidro Bridge links three barangays to the Guagua town proper. A 2024 inspection found cracks " +
                "in the deck slab and scouring around two of its four piers, and heavy trucks have been limited to " +
                "one lane since then.",
            "The project rebuilds the deck in reinforced concrete, adds two new piers on deeper foundations and " +
                "raises the approaches by 1.2 metres, above the water level reached during the 2024 typhoon season.",
            "One lane stays open during the works, with flaggers on both ends from 6 AM to 7 PM. Full closures, if " +
                "needed for the deck pour, will be announced here at least a week ahead.",
        ),
        people = listOf(
            Person("Engr. Maria Santos", "District Engineer", "DPWH Pampanga 1st DEO"),
            Person("Engr. Paolo Ramos", "Project Engineer", "DPWH Pampanga 1st DEO"),
            Person("Ramon Bautista", "Project Manager", "Tala Builders Inc."),
        ),
        updates = listOf(
            Update(Day(2025, 3, 10), "Site cleared and survey done", "Temporary one-lane traffic scheme set up. Survey markers placed for the new piers.", 0.08f),
            Update(Day(2025, 9, 22), "New pier foundations complete", "Bored piles for piers 2 and 3 reached the design depth of 18 metres.", 0.4f),
            Update(Day(2026, 4, 14), "Deck pour, first half", "The first 30 metres of the new deck slab were poured and are curing.", 0.62f),
            Update(Day(2026, 10, 2), "Second half of the deck poured", "Railings and approach slabs are next. Work is at 78%.", 0.78f),
        ),
        feedback = listOf(
            Feedback("Joy M.", "Brgy. San Isidro", Day(2026, 10, 4), "The one-lane setup is well managed. Flaggers are always there even when it rains.", true),
            Feedback("Arnel P.", "Guagua", Day(2026, 9, 18), "Please post the full closure dates early. We bring our harvest across every Saturday.", true),
            Feedback("Lorna D.", "Brgy. Santa Ursula", Day(2026, 8, 30), "It's been almost two years. The sign at the site still says it ends in October.", false),
        ),
        likes = 1_184,
        dislikes = 162,
        records = SampleRecords,
        palette = 0,
    ),
    Project(
        id = "bagong-silang-school",
        title = "Bagong Silang Elementary School 2-storey, 8-classroom building",
        category = Category.Schools,
        level = Level.National,
        status = Status.Delayed,
        place = "Brgy. Bagong Silang, Lipa City, Batangas",
        proposedBy = "DepEd Schools Division of Lipa City",
        implementedBy = "DPWH Batangas 4th District Engineering Office",
        contractor = "Bituin Construction Corp.",
        budget = 38_200_000,
        released = 22_900_000,
        fundedBy = "General Appropriations Act 2025, Basic Educational Facilities Fund",
        start = Day(2025, 7, 1),
        end = Day(2026, 6, 30),
        progress = 71,
        purpose = "Adds eight classrooms so the school can end its two-shift schedule, where Grade 1 to 3 pupils " +
            "take turns using the same rooms in the morning and afternoon.",
        article = listOf(
            "Bagong Silang Elementary School has 1,240 pupils and 18 classrooms. Since 2023 the lower grades have " +
                "run in two shifts, so some pupils start at 6 AM and others finish at 6 PM.",
            "The new building has eight classrooms, toilets on both floors and a ramp for pupils with disabilities. " +
                "It was due to be turned over in June 2026.",
            "The contractor reported delays in steel deliveries in the first quarter of 2026. A revised schedule " +
                "targets turnover before the second semester. The division office will post the new date here.",
        ),
        people = listOf(
            Person("Dr. Teresa Villanueva", "Schools Division Superintendent", "DepEd Lipa City"),
            Person("Engr. Carlo Garcia", "Project Engineer", "DPWH Batangas 4th DEO"),
            Person("Liza Mendoza", "School Principal", "Bagong Silang Elementary School"),
        ),
        updates = listOf(
            Update(Day(2025, 7, 15), "Groundbreaking", "Excavation for the foundation started beside the old Grade 6 building.", 0.06f),
            Update(Day(2025, 12, 5), "Ground floor columns up", "Ground floor columns and beams are complete.", 0.35f),
            Update(Day(2026, 3, 20), "Steel delivery delayed", "Second floor works paused for five weeks while waiting for rebar deliveries.", 0.5f),
            Update(Day(2026, 9, 9), "Roofing in progress", "Second floor slab done. Roof trusses and sheets being installed. Work is at 71%.", 0.71f),
        ),
        feedback = listOf(
            Feedback("Mylene R.", "Brgy. Bagong Silang", Day(2026, 9, 12), "My son is in the 6 AM shift. We are counting the days until this opens.", true),
            Feedback("Edwin C.", "Lipa City", Day(2026, 8, 2), "Three months late already. Will the contractor be penalized for the delay?", false),
            Feedback("Grace T.", "Brgy. Marawoy", Day(2026, 7, 21), "Thank you for posting the reason for the delay instead of keeping quiet.", true),
        ),
        likes = 2_031,
        dislikes = 418,
        records = SampleRecords,
        palette = 1,
    ),
    Project(
        id = "carmen-rhu",
        title = "Carmen Rural Health Unit expansion",
        category = Category.Health,
        level = Level.City,
        status = Status.Completed,
        place = "Poblacion Norte, Carmen, Bohol",
        proposedBy = "Municipality of Carmen, Municipal Health Office",
        implementedBy = "Municipal Engineering Office, Carmen",
        contractor = "Dagat Builders",
        budget = 12_750_000,
        released = 12_750_000,
        fundedBy = "Municipal 20% Development Fund and DOH Health Facilities Enhancement Program",
        start = Day(2024, 1, 15),
        end = Day(2025, 3, 30),
        progress = 100,
        purpose = "Adds a birthing room, a TB-DOTS room and a larger waiting area to the town's only rural health " +
            "unit, which serves more than 45,000 residents.",
        article = listOf(
            "Before the expansion, mothers in labour were referred to the provincial hospital 40 minutes away, and " +
                "the waiting area held 20 people for a daily average of 120 patients.",
            "The new wing has a birthing room with two beds, a TB-DOTS treatment room with its own entrance, and a " +
                "covered waiting area for 80 people. It opened in March 2025.",
        ),
        people = listOf(
            Person("Dr. Ana Dela Cruz", "Municipal Health Officer", "Carmen Municipal Health Office"),
            Person("Engr. Jose Reyes", "Municipal Engineer", "Carmen Municipal Engineering Office"),
            Person("Nilda Flores", "Public Health Nurse", "Carmen Rural Health Unit"),
        ),
        updates = listOf(
            Update(Day(2024, 2, 1), "Construction starts", "Old storage room demolished to make way for the new wing.", 0.1f),
            Update(Day(2024, 8, 19), "Structure complete", "Walls and roof of the new wing are done. Electrical works next.", 0.6f),
            Update(Day(2025, 3, 28), "New wing opens", "The birthing room delivered its first baby on opening day.", 1f),
        ),
        feedback = listOf(
            Feedback("Rhea S.", "Brgy. Katipunan", Day(2025, 6, 3), "I gave birth here last month. Clean and the midwives were kind.", true),
            Feedback("Benjie O.", "Poblacion Sur", Day(2025, 4, 22), "Waiting area is much better now. No more standing outside in the sun.", true),
        ),
        likes = 964,
        dislikes = 21,
        records = SampleRecords,
        palette = 2,
    ),
    Project(
        id = "mabini-water",
        title = "Barangay Mabini Level III water system",
        category = Category.Water,
        level = Level.Barangay,
        status = Status.Ongoing,
        place = "Brgy. Mabini, Santa Cruz, Laguna",
        proposedBy = "Sangguniang Barangay of Mabini",
        implementedBy = "Barangay Mabini with the Santa Cruz Municipal Engineering Office",
        contractor = null,
        budget = 3_480_000,
        released = 2_100_000,
        fundedBy = "Barangay 20% Development Fund 2026",
        start = Day(2026, 2, 2),
        end = Day(2026, 11, 27),
        progress = 64,
        purpose = "Brings piped water into 310 households that now fetch water from three shared hand pumps.",
        article = listOf(
            "Most households in Purok 4 to 7 of Mabini rely on three hand pumps, and the water fails coliform tests " +
                "every dry season.",
            "The project builds a 20,000-litre elevated tank fed by a new deep well, and lays 2.8 kilometres of pipe " +
                "with a tap at each house. Households will pay a monthly water fee set by the barangay assembly.",
        ),
        people = listOf(
            Person("Hon. Ricardo Navarro", "Punong Barangay", "Barangay Mabini"),
            Person("Engr. Celia Castillo", "Municipal Engineer", "Santa Cruz Municipal Engineering Office"),
            Person("Marites Torres", "Barangay Treasurer", "Barangay Mabini"),
        ),
        updates = listOf(
            Update(Day(2026, 2, 16), "Deep well drilled", "Water found at 96 metres. Water quality test passed.", 0.15f),
            Update(Day(2026, 6, 8), "Tank tower erected", "Steel tower for the 20,000-litre tank is up.", 0.45f),
            Update(Day(2026, 9, 25), "Pipes laid in Purok 4 and 5", "1.7 of 2.8 kilometres of pipe laid. House connections start next month.", 0.64f),
        ),
        feedback = listOf(
            Feedback("Aling Bebeng", "Purok 5", Day(2026, 9, 28), "Salamat! Our street is finally dug up for the pipes. Excited.", true),
            Feedback("Dennis V.", "Purok 7", Day(2026, 9, 1), "How much will the monthly fee be? Please post it before connections start.", true),
        ),
        likes = 412,
        dislikes = 18,
        records = SampleRecords,
        palette = 0,
    ),
    Project(
        id = "naga-slope-protection",
        title = "Bicol River slope protection, Naga City segment",
        category = Category.FloodControl,
        level = Level.National,
        status = Status.Planned,
        place = "Brgy. Sabang, Naga City, Camarines Sur",
        proposedBy = "DPWH Camarines Sur 3rd District Engineering Office",
        implementedBy = "DPWH Camarines Sur 3rd District Engineering Office",
        contractor = null,
        budget = 96_000_000,
        released = 0,
        fundedBy = "General Appropriations Act 2027 (proposed)",
        start = Day(2027, 1, 11),
        end = Day(2027, 12, 17),
        progress = 0,
        purpose = "Protects 850 metres of eroding riverbank in Sabang, where 140 houses stand within 10 metres of " +
            "the water's edge.",
        article = listOf(
            "The Sabang riverbank has lost up to four metres of land since 2020. During the 2024 floods, water " +
                "reached the second floor of houses nearest the river.",
            "The design calls for a stone masonry slope with a concrete toe wall and a 3-metre walkway on top. " +
                "Bidding is set for November 2026. The bid documents will be linked here once posted.",
        ),
        people = listOf(
            Person("Engr. Victor Salazar", "District Engineer", "DPWH Camarines Sur 3rd DEO"),
            Person("Engr. Rina Pascual", "Planning and Design Chief", "DPWH Camarines Sur 3rd DEO"),
            Person("Hon. Dante Domingo", "Punong Barangay", "Barangay Sabang"),
        ),
        updates = listOf(
            Update(Day(2026, 8, 20), "Public consultation held", "Residents of Purok 1 to 3 reviewed the design. Their comments are in the minutes.", 0f),
        ),
        feedback = listOf(
            Feedback("Mario L.", "Brgy. Sabang", Day(2026, 8, 22), "Please include a drainage outlet for Purok 2. Water pools there for days.", true),
            Feedback("Carmen B.", "Naga City", Day(2026, 8, 21), "Will families near the river be relocated? Nobody has told us yet.", false),
        ),
        likes = 688,
        dislikes = 97,
        records = SampleRecords.take(1) + "Minutes of the public consultation",
        palette = 3,
    ),
    Project(
        id = "malabog-farm-road",
        title = "Malabog–Lumbo farm-to-market road",
        category = Category.Roads,
        level = Level.Province,
        status = Status.Completed,
        place = "Valencia City, Bukidnon",
        proposedBy = "Provincial Government of Bukidnon",
        implementedBy = "Bukidnon Provincial Engineering Office",
        contractor = "Kalabaw Road Works",
        budget = 27_600_000,
        released = 27_150_000,
        fundedBy = "Department of Agriculture farm-to-market road program and provincial counterpart",
        start = Day(2025, 4, 7),
        end = Day(2026, 2, 26),
        progress = 100,
        purpose = "Concretes 4.1 kilometres of dirt road so corn and vegetable farmers can reach the Valencia " +
            "trading post in any weather.",
        article = listOf(
            "Before the road was concreted, a trip from Malabog to the trading post took two hours in the rainy " +
                "season, and trucks often got stuck.",
            "The finished road is 6 metres wide with drainage canals on both sides. The trip now takes 25 minutes. " +
                "₱450,000 of the budget was not used and has been returned to the provincial treasury.",
        ),
        people = listOf(
            Person("Engr. Arturo Galang", "Provincial Engineer", "Bukidnon Provincial Engineering Office"),
            Person("Engr. Mae Abellera", "Project Engineer", "Bukidnon Provincial Engineering Office"),
            Person("Rogelio Tan", "Project Manager", "Kalabaw Road Works"),
        ),
        updates = listOf(
            Update(Day(2025, 4, 21), "Grading starts", "Road widening and grading from the Malabog end.", 0.1f),
            Update(Day(2025, 10, 3), "2 kilometres concreted", "Half the length is done. Drainage canals follow the pour.", 0.5f),
            Update(Day(2026, 3, 6), "Road opened", "All 4.1 kilometres are open. Unused funds returned to the treasury.", 1f),
        ),
        feedback = listOf(
            Feedback("Nestor A.", "Brgy. Malabog", Day(2026, 3, 15), "We can now deliver corn even during heavy rain. Laking tulong.", true),
            Feedback("Jun-jun S.", "Brgy. Lumbo", Day(2026, 4, 2), "Good road but there are no road signs near the school yet.", true),
        ),
        likes = 1_502,
        dislikes = 44,
        records = SampleRecords + "Certificate of completion",
        palette = 1,
    ),
    Project(
        id = "culasi-evacuation-center",
        title = "Culasi covered court and evacuation center",
        category = Category.Buildings,
        level = Level.City,
        status = Status.Ongoing,
        place = "Brgy. Culasi, Roxas City, Capiz",
        proposedBy = "City Government of Roxas, City Disaster Risk Reduction and Management Office",
        implementedBy = "Roxas City Engineering Office",
        contractor = "Banca Builders",
        budget = 18_900_000,
        released = 6_600_000,
        fundedBy = "City 20% Development Fund and Local Disaster Risk Reduction and Management Fund",
        start = Day(2026, 6, 2),
        end = Day(2027, 3, 31),
        progress = 34,
        purpose = "Builds a covered court that doubles as an evacuation center for 600 people, with toilets, a " +
            "kitchen and a water tank.",
        article = listOf(
            "Culasi is a coastal barangay that evacuates every typhoon season. Families now shelter in the " +
                "barangay hall and a chapel that fit fewer than 200 people.",
            "The new structure has a steel roof rated for typhoon winds, raised flooring, separate toilets for men " +
                "and women, a kitchen and a 10,000-litre water tank. Outside typhoon season it is a basketball court.",
        ),
        people = listOf(
            Person("Engr. Gilbert Uy", "City Engineer", "Roxas City Engineering Office"),
            Person("Ma. Cristina Lim", "CDRRM Officer", "Roxas City DRRM Office"),
            Person("Hon. Efren Belleza", "Punong Barangay", "Barangay Culasi"),
        ),
        updates = listOf(
            Update(Day(2026, 6, 15), "Foundation works", "Footings poured for the 12 main columns.", 0.12f),
            Update(Day(2026, 9, 30), "Columns and roof trusses", "Steel columns up. Roof trusses arriving in October.", 0.34f),
        ),
        feedback = listOf(
            Feedback("Weng A.", "Brgy. Culasi", Day(2026, 10, 1), "Please finish before December. That's when the strong typhoons come.", true),
            Feedback("Toto G.", "Brgy. Baybay", Day(2026, 7, 9), "Will nearby barangays be allowed to evacuate here too?", true),
        ),
        likes = 733,
        dislikes = 39,
        records = SampleRecords,
        palette = 2,
    ),
    Project(
        id = "munoz-canal-lining",
        title = "Muñoz irrigation canal lining",
        category = Category.Farms,
        level = Level.National,
        status = Status.Ongoing,
        place = "Science City of Muñoz, Nueva Ecija",
        proposedBy = "National Irrigation Administration, Region III",
        implementedBy = "NIA Nueva Ecija Irrigation Management Office",
        contractor = "Palay Engineering Services",
        budget = 64_300_000,
        released = 41_000_000,
        fundedBy = "General Appropriations Act 2024 and 2025 (NIA)",
        start = Day(2024, 11, 4),
        end = Day(2027, 1, 29),
        progress = 70,
        purpose = "Lines 7.5 kilometres of earth canal with concrete so water reaches the tail-end rice farms that " +
            "now get only half the water they need.",
        article = listOf(
            "Unlined canals lose up to 40% of their water to seepage before it reaches farms at the end of the line. " +
                "About 1,100 hectares of rice land depend on this lateral canal.",
            "Lining is done in sections between harvests, so water can still flow during planting season. Farmers' " +
                "irrigators associations helped set the schedule.",
        ),
        people = listOf(
            Person("Engr. Rolando Mercado", "Regional Manager", "NIA Region III"),
            Person("Engr. Joan Sison", "Project Engineer", "NIA Nueva Ecija IMO"),
            Person("Felipe Santiago", "President", "Lateral C Irrigators Association"),
        ),
        updates = listOf(
            Update(Day(2024, 11, 18), "First section starts", "Lining starts at the head gate after the wet season harvest.", 0.08f),
            Update(Day(2025, 6, 2), "3 kilometres lined", "Works pause for the wet season planting. Water flowing.", 0.4f),
            Update(Day(2026, 8, 26), "5.2 kilometres lined", "Tail-end farms report water arriving two days sooner.", 0.7f),
        ),
        feedback = listOf(
            Feedback("Mang Andoy", "Brgy. Bantug", Day(2026, 9, 3), "For the first time in years, water reached my farm on schedule.", true),
            Feedback("Rosalie F.", "Brgy. Maligaya", Day(2026, 8, 30), "The access road beside the canal was damaged by the trucks. Who will fix it?", false),
        ),
        likes = 1_247,
        dislikes = 133,
        records = SampleRecords,
        palette = 3,
    ),
    Project(
        id = "tinago-road-concreting",
        title = "Purok 3 to Purok 6 road concreting",
        category = Category.Roads,
        level = Level.Barangay,
        status = Status.Ongoing,
        place = "Brgy. Tinago, Ormoc City, Leyte",
        proposedBy = "Sangguniang Barangay of Tinago",
        implementedBy = "Barangay Tinago",
        contractor = "Lakbay Concrete Works",
        budget = 2_150_000,
        released = 1_075_000,
        fundedBy = "Barangay 20% Development Fund 2026",
        start = Day(2026, 8, 18),
        end = Day(2026, 12, 4),
        progress = 40,
        purpose = "Concretes 480 metres of muddy road used by 200 households and pupils walking to Tinago " +
            "Elementary School.",
        article = listOf(
            "The road from Purok 3 to Purok 6 turns to mud every time it rains, and pupils carry their shoes to " +
                "school. It was the top request in the 2025 barangay assembly.",
            "The road is being poured in 60-metre sections so residents can still get through. Each section cures " +
                "for 14 days before vehicles may use it.",
        ),
        people = listOf(
            Person("Hon. Lourdes Abad", "Punong Barangay", "Barangay Tinago"),
            Person("Hon. Rey Gallardo", "Chair, Committee on Infrastructure", "Sangguniang Barangay of Tinago"),
            Person("Manny Lagman", "Foreman", "Lakbay Concrete Works"),
        ),
        updates = listOf(
            Update(Day(2026, 8, 25), "Base course laid", "Gravel base compacted along the whole 480 metres.", 0.15f),
            Update(Day(2026, 10, 6), "Three sections poured", "180 of 480 metres poured. Next pour after the rains ease.", 0.4f),
        ),
        feedback = listOf(
            Feedback("Inday R.", "Purok 4", Day(2026, 10, 8), "No more muddy shoes for the kids. Thank you, Kapitana!", true),
        ),
        likes = 286,
        dislikes = 9,
        records = SampleRecords.take(2),
        palette = 0,
    ),
    Project(
        id = "tagum-science-lab",
        title = "Tagum City National High School science laboratory",
        category = Category.Schools,
        level = Level.City,
        status = Status.Completed,
        place = "Brgy. Apokon, Tagum City, Davao del Norte",
        proposedBy = "Tagum City Local School Board",
        implementedBy = "Tagum City Engineering Office",
        contractor = "Agila Builders",
        budget = 6_800_000,
        released = 6_720_000,
        fundedBy = "Special Education Fund 2026",
        start = Day(2026, 3, 2),
        end = Day(2026, 9, 15),
        progress = 100,
        purpose = "Gives 2,600 senior high school students a working chemistry and physics laboratory, in place " +
            "of demonstrations done on a classroom table.",
        article = listOf(
            "The school had no laboratory, so experiments in the STEM strand were shown by the teacher at the front " +
                "of the class.",
            "The new laboratory has eight student workstations with sinks and gas outlets, a fume hood, an " +
                "eyewash station and a stock room. Classes started using it in September 2026.",
        ),
        people = listOf(
            Person("Engr. Danilo Corpuz", "City Engineer", "Tagum City Engineering Office"),
            Person("Dr. Helen Bacani", "School Principal", "Tagum City National High School"),
            Person("Arlene Yap", "Science Department Head", "Tagum City National High School"),
        ),
        updates = listOf(
            Update(Day(2026, 3, 16), "Room renovation starts", "Two old classrooms joined to make the laboratory space.", 0.2f),
            Update(Day(2026, 9, 14), "Laboratory turned over", "Workstations installed and safety-inspected. First classes next week.", 1f),
        ),
        feedback = listOf(
            Feedback("Kyle B.", "Grade 12 STEM", Day(2026, 9, 25), "We did our first titration today. Way better than watching a video.", true),
            Feedback("Ma'am Joy", "Parent", Day(2026, 9, 20), "Good use of the school fund. Please add a second fume hood.", true),
        ),
        likes = 857,
        dislikes = 12,
        records = SampleRecords + "Certificate of completion",
        palette = 1,
    ),
)

fun projectById(id: String): Project? = projects.firstOrNull { it.id == id }
