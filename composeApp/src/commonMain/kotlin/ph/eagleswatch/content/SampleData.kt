package ph.eagleswatch.content

/*
 * DEVELOPMENT FIXTURE.
 *
 * Ten fictional projects used to design and test the app before live sources are connected. The places are
 * real municipalities; the projects, contracts, amounts, people, documents and dates are invented. Every
 * record carries `sample = true`, and the app says so on every surface that shows one.
 *
 * The field-level attributions (agency-reported, verified, community-reported, not available) show how a
 * real record will be labelled. They do not mean anyone checked these fictional projects.
 */

/** The date the fixture pretends to have been refreshed. */
val SAMPLE_AS_OF = Day(2026, 10, 10)

private const val EDITORS = "Eagle's Watch editors (sample)"

private val fixtureSource = DataSource("Sample fixture in the project repository", GITHUB_URL)

private fun agency(source: String, asOf: Day? = null) = Attribution(Verification.AgencyReported, source, asOf)
private fun verified(source: String, asOf: Day? = null) = Attribution(Verification.Verified, source, asOf)
private fun community(source: String, asOf: Day? = null) = Attribution(Verification.CommunityReported, source, asOf)
private fun unavailable(note: String) = Attribution(Verification.Unavailable, note)

val sampleProjects: List<Project> = listOf(
    Project(
        id = "san-isidro-bridge",
        title = "San Isidro Bridge rehabilitation",
        category = Category.Bridges,
        level = Level.National,
        status = Status.Ongoing,
        place = Place("San Isidro", "Guagua", "Pampanga", "Region III (Central Luzon)", Coordinates(14.967, 120.633)),
        description = "Replaces the deck slab and two of the four piers of a 60-metre bridge, adds bored-pile " +
            "foundations and raises the approaches by 1.2 metres above the 2024 flood level. One lane stays " +
            "open during the works.",
        descriptionAttribution = agency("DPWH Pampanga 1st DEO project posting", Day(2025, 3, 5)),
        contract = Contract(
            amount = 48_500_000,
            fundingSource = "General Appropriations Act 2025",
            reference = "25C00123 (sample)",
            attribution = agency("Notice of award, PhilGEPS posting", Day(2025, 2, 20)),
        ),
        schedule = Schedule(Day(2025, 3, 3), Day(2026, 12, 15), null, null, agency("Contract as posted", Day(2025, 2, 20))),
        progress = Progress(
            reported = PhysicalProgress(78, Day(2026, 10, 2), agency("DPWH progress update", Day(2026, 10, 2))),
            independent = null,
            financial = FinancialProgress(31_200_000, Day(2026, 9, 30), agency("DPWH disbursement report", Day(2026, 9, 30))),
        ),
        parties = listOf(
            Party(PartyRole.Implementing, "DPWH Pampanga 1st District Engineering Office", null, agency("Project posting", Day(2025, 3, 5))),
            Party(PartyRole.Contractor, "Tala Builders Inc.", "Named in the notice of award", agency("PhilGEPS posting", Day(2025, 2, 20))),
            Party(PartyRole.Official, "Engr. Maria Santos", "District Engineer, DPWH Pampanga 1st DEO", agency("Project posting", Day(2025, 3, 5))),
            Party(PartyRole.Official, "Engr. Paolo Ramos", "Project Engineer, DPWH Pampanga 1st DEO", agency("Project posting", Day(2025, 3, 5))),
            Party(PartyRole.Official, "Ramon Bautista", "Project Manager, Tala Builders Inc.", agency("Project posting", Day(2025, 3, 5))),
        ),
        updates = listOf(
            Update(Day(2025, 3, 10), "Site cleared and survey done", "Temporary one-lane traffic scheme set up. Survey markers placed for the new piers.", 8, agency("DPWH progress update", Day(2025, 3, 10))),
            Update(Day(2025, 9, 22), "New pier foundations complete", "Bored piles for piers 2 and 3 reached the design depth of 18 metres.", 40, agency("DPWH progress update", Day(2025, 9, 22))),
            Update(Day(2026, 4, 14), "Deck pour, first half", "The first 30 metres of the new deck slab were poured and are curing.", 62, agency("DPWH progress update", Day(2026, 4, 14))),
            Update(Day(2026, 10, 2), "Second half of the deck poured", "Railings and approach slabs are next.", 78, agency("DPWH progress update", Day(2026, 10, 2))),
        ),
        photos = emptyList(),
        documents = listOf(
            Document(DocumentKind.Budget, "GAA 2025 line item, DPWH Region III", Day(2024, 12, 30), null, agency("DBM, GAA 2025")),
            Document(DocumentKind.Award, "Notice of award to Tala Builders Inc.", Day(2025, 2, 20), null, agency("PhilGEPS")),
            Document(DocumentKind.ProgramOfWork, "Program of work and detailed estimates", Day(2025, 2, 10), null, agency("DPWH Pampanga 1st DEO")),
        ),
        sources = listOf(fixtureSource),
        revisions = listOf(
            Revision(Day(2025, 3, 5), "Record created from the agency's project posting", EDITORS),
            Revision(Day(2026, 4, 20), "Physical progress updated to 62% from the Apr 14 update", EDITORS),
            Revision(Day(2026, 10, 4), "Physical progress updated to 78%; disbursement as of Sep 30 added", EDITORS),
        ),
        lastChecked = Day(2026, 10, 4),
        sample = true,
    ),
    Project(
        id = "bagong-silang-school",
        title = "Bagong Silang Elementary School, two-storey eight-classroom building",
        category = Category.Schools,
        level = Level.National,
        status = Status.Delayed,
        place = Place("Bagong Silang", "Lipa City", "Batangas", "Region IV-A (Calabarzon)", Coordinates(13.941, 121.163)),
        description = "A two-storey building with eight classrooms, toilets on both floors and a ramp, so the " +
            "school can end its two-shift schedule for Grades 1 to 3.",
        descriptionAttribution = agency("DepEd Lipa City project posting", Day(2025, 7, 3)),
        contract = Contract(
            amount = 38_200_000,
            fundingSource = "General Appropriations Act 2025, Basic Educational Facilities Fund",
            reference = "25D00456 (sample)",
            attribution = agency("Notice of award, PhilGEPS posting", Day(2025, 6, 12)),
        ),
        schedule = Schedule(
            start = Day(2025, 7, 1),
            targetEnd = Day(2026, 6, 30),
            revisedEnd = Day(2026, 11, 30),
            actualEnd = null,
            attribution = agency("Time extension approved by DPWH Batangas 4th DEO", Day(2026, 4, 2)),
        ),
        progress = Progress(
            reported = PhysicalProgress(71, Day(2026, 9, 9), agency("DPWH progress update", Day(2026, 9, 9))),
            independent = null,
            financial = FinancialProgress(22_900_000, Day(2026, 8, 31), agency("DPWH disbursement report", Day(2026, 8, 31))),
        ),
        parties = listOf(
            Party(PartyRole.Proposing, "DepEd Schools Division of Lipa City", null, agency("Project posting", Day(2025, 7, 3))),
            Party(PartyRole.Implementing, "DPWH Batangas 4th District Engineering Office", null, agency("Project posting", Day(2025, 7, 3))),
            Party(PartyRole.Contractor, "Bituin Construction Corp.", "Named in the notice of award", agency("PhilGEPS posting", Day(2025, 6, 12))),
            Party(PartyRole.Official, "Dr. Teresa Villanueva", "Schools Division Superintendent, DepEd Lipa City", agency("Project posting", Day(2025, 7, 3))),
            Party(PartyRole.Official, "Engr. Carlo Garcia", "Project Engineer, DPWH Batangas 4th DEO", agency("Project posting", Day(2025, 7, 3))),
            Party(PartyRole.Official, "Liza Mendoza", "School Principal, Bagong Silang Elementary School", agency("Project posting", Day(2025, 7, 3))),
        ),
        updates = listOf(
            Update(Day(2025, 7, 15), "Groundbreaking", "Excavation for the foundation started beside the old Grade 6 building.", 6, agency("DPWH progress update", Day(2025, 7, 15))),
            Update(Day(2025, 12, 5), "Ground floor columns up", "Ground floor columns and beams are complete.", 35, agency("DPWH progress update", Day(2025, 12, 5))),
            Update(Day(2026, 3, 20), "Steel delivery delayed", "Second floor works paused for five weeks while waiting for rebar deliveries.", 50, agency("DPWH progress update", Day(2026, 3, 20))),
            Update(Day(2026, 4, 2), "Time extension approved", "Completion moved from Jun 30 to Nov 30, 2026, citing supplier delays.", null, agency("DPWH Batangas 4th DEO", Day(2026, 4, 2))),
            Update(Day(2026, 9, 9), "Roofing in progress", "Second floor slab done. Roof trusses and sheets being installed.", 71, agency("DPWH progress update", Day(2026, 9, 9))),
        ),
        photos = emptyList(),
        documents = listOf(
            Document(DocumentKind.Budget, "GAA 2025 line item, Basic Educational Facilities Fund", Day(2024, 12, 30), null, agency("DBM, GAA 2025")),
            Document(DocumentKind.Award, "Notice of award to Bituin Construction Corp.", Day(2025, 6, 12), null, agency("PhilGEPS")),
            Document(DocumentKind.Other, "Time extension order", Day(2026, 4, 2), null, agency("DPWH Batangas 4th DEO")),
        ),
        sources = listOf(fixtureSource),
        revisions = listOf(
            Revision(Day(2025, 7, 3), "Record created from the agency's project posting", EDITORS),
            Revision(Day(2026, 4, 6), "Status set to Delayed; revised completion date added from the time extension order", EDITORS),
            Revision(Day(2026, 9, 12), "Physical progress updated to 71% from the Sep 9 update", EDITORS),
        ),
        lastChecked = Day(2026, 9, 12),
        sample = true,
    ),
    Project(
        id = "carmen-rhu",
        title = "Carmen Rural Health Unit expansion",
        category = Category.Health,
        level = Level.Municipal,
        status = Status.Completed,
        place = Place("Poblacion Norte", "Carmen", "Bohol", "Region VII (Central Visayas)", Coordinates(9.822, 124.195)),
        description = "A new wing with a two-bed birthing room, a TB-DOTS treatment room with its own entrance and a " +
            "covered waiting area for 80 people, added to the town's only rural health unit.",
        descriptionAttribution = agency("Municipality of Carmen project posting", Day(2024, 1, 20)),
        contract = Contract(
            amount = 12_750_000,
            fundingSource = "Municipal 20% Development Fund and DOH Health Facilities Enhancement Program",
            reference = "24M00017 (sample)",
            attribution = agency("Notice of award, PhilGEPS posting", Day(2023, 12, 18)),
        ),
        schedule = Schedule(Day(2024, 1, 15), Day(2025, 3, 30), null, Day(2025, 3, 28), agency("Certificate of completion", Day(2025, 3, 28))),
        progress = Progress(
            reported = PhysicalProgress(100, Day(2025, 3, 28), agency("Certificate of completion", Day(2025, 3, 28))),
            independent = PhysicalProgress(100, Day(2025, 6, 3), verified("Site visit, sample volunteer check", Day(2025, 6, 3))),
            financial = FinancialProgress(12_750_000, Day(2025, 4, 30), agency("Municipal disbursement report", Day(2025, 4, 30))),
        ),
        parties = listOf(
            Party(PartyRole.Proposing, "Municipality of Carmen, Municipal Health Office", null, agency("Project posting", Day(2024, 1, 20))),
            Party(PartyRole.Implementing, "Carmen Municipal Engineering Office", null, agency("Project posting", Day(2024, 1, 20))),
            Party(PartyRole.Contractor, "Dagat Builders", "Named in the notice of award", agency("PhilGEPS posting", Day(2023, 12, 18))),
            Party(PartyRole.Official, "Dr. Ana Dela Cruz", "Municipal Health Officer, Carmen", agency("Project posting", Day(2024, 1, 20))),
            Party(PartyRole.Official, "Engr. Jose Reyes", "Municipal Engineer, Carmen", agency("Project posting", Day(2024, 1, 20))),
        ),
        updates = listOf(
            Update(Day(2024, 2, 1), "Construction starts", "Old storage room demolished to make way for the new wing.", 10, agency("Municipal progress update", Day(2024, 2, 1))),
            Update(Day(2024, 8, 19), "Structure complete", "Walls and roof of the new wing are done. Electrical works next.", 60, agency("Municipal progress update", Day(2024, 8, 19))),
            Update(Day(2025, 3, 28), "New wing opens", "The wing was turned over and the birthing room began operating.", 100, agency("Certificate of completion", Day(2025, 3, 28))),
            Update(Day(2025, 6, 3), "Site check", "The new wing was found in use. Birthing room, TB-DOTS room and waiting area match the program of work.", 100, verified("Site visit, sample volunteer check", Day(2025, 6, 3))),
        ),
        photos = emptyList(),
        documents = listOf(
            Document(DocumentKind.Budget, "2024 municipal budget, 20% Development Fund", Day(2023, 11, 10), null, agency("Full Disclosure Policy posting")),
            Document(DocumentKind.Award, "Notice of award to Dagat Builders", Day(2023, 12, 18), null, agency("PhilGEPS")),
            Document(DocumentKind.Completion, "Certificate of completion and acceptance", Day(2025, 3, 28), null, agency("Carmen Municipal Engineering Office")),
        ),
        sources = listOf(fixtureSource),
        revisions = listOf(
            Revision(Day(2024, 1, 20), "Record created from the municipality's project posting", EDITORS),
            Revision(Day(2025, 4, 2), "Status set to Completed from the certificate of completion", EDITORS),
            Revision(Day(2025, 6, 5), "Independent site check added", EDITORS),
        ),
        lastChecked = Day(2025, 6, 5),
        sample = true,
    ),
    Project(
        id = "mabini-water",
        title = "Barangay Mabini Level III water system",
        category = Category.Water,
        level = Level.Barangay,
        status = Status.Ongoing,
        place = Place("Mabini", "Santa Cruz", "Laguna", "Region IV-A (Calabarzon)", Coordinates(14.278, 121.416)),
        description = "A deep well, a 20,000-litre elevated tank and 2.8 kilometres of pipe with a tap at each of " +
            "310 households that now draw from three shared hand pumps.",
        descriptionAttribution = agency("Barangay Mabini project posting", Day(2026, 2, 5)),
        contract = Contract(
            amount = 3_480_000,
            fundingSource = "Barangay 20% Development Fund 2026",
            reference = null,
            attribution = agency("Barangay appropriation ordinance", Day(2025, 12, 15)),
        ),
        schedule = Schedule(Day(2026, 2, 2), Day(2026, 11, 27), null, null, agency("Program of work", Day(2026, 1, 20))),
        progress = Progress(
            reported = PhysicalProgress(64, Day(2026, 9, 25), agency("Barangay progress update", Day(2026, 9, 25))),
            independent = null,
            financial = FinancialProgress(2_100_000, Day(2026, 9, 30), agency("Barangay treasurer's report", Day(2026, 9, 30))),
        ),
        parties = listOf(
            Party(PartyRole.Proposing, "Sangguniang Barangay of Mabini", null, agency("Project posting", Day(2026, 2, 5))),
            Party(PartyRole.Implementing, "Barangay Mabini, with the Santa Cruz Municipal Engineering Office", null, agency("Project posting", Day(2026, 2, 5))),
            Party(PartyRole.Contractor, "By administration (no contractor)", "Works done by the barangay's own crew", agency("Project posting", Day(2026, 2, 5))),
            Party(PartyRole.Official, "Hon. Ricardo Navarro", "Punong Barangay, Barangay Mabini", agency("Project posting", Day(2026, 2, 5))),
            Party(PartyRole.Official, "Engr. Celia Castillo", "Municipal Engineer, Santa Cruz", agency("Project posting", Day(2026, 2, 5))),
            Party(PartyRole.Official, "Marites Torres", "Barangay Treasurer, Barangay Mabini", agency("Project posting", Day(2026, 2, 5))),
        ),
        updates = listOf(
            Update(Day(2026, 2, 16), "Deep well drilled", "Water found at 96 metres. Water quality test passed.", 15, agency("Barangay progress update", Day(2026, 2, 16))),
            Update(Day(2026, 6, 8), "Tank tower erected", "Steel tower for the 20,000-litre tank is up.", 45, agency("Barangay progress update", Day(2026, 6, 8))),
            Update(Day(2026, 9, 25), "Pipes laid in Purok 4 and 5", "1.7 of 2.8 kilometres of pipe laid. House connections start next month.", 64, agency("Barangay progress update", Day(2026, 9, 25))),
            Update(Day(2026, 9, 28), "Trench open along the Purok 5 road", "A resident reports the pipe trench dug along the main Purok 5 road, with pipes stacked at the roadside.", null, community("Citizen report through the app", Day(2026, 9, 28))),
        ),
        photos = emptyList(),
        documents = listOf(
            Document(DocumentKind.Budget, "Barangay appropriation ordinance 2026", Day(2025, 12, 15), null, agency("Full Disclosure Policy posting")),
            Document(DocumentKind.ProgramOfWork, "Program of work", Day(2026, 1, 20), null, agency("Barangay Mabini")),
        ),
        sources = listOf(fixtureSource),
        revisions = listOf(
            Revision(Day(2026, 2, 5), "Record created from the barangay's project posting", EDITORS),
            Revision(Day(2026, 9, 29), "Physical progress updated to 64%; citizen report added to the timeline", EDITORS),
        ),
        lastChecked = null,
        sample = true,
    ),
    Project(
        id = "naga-slope-protection",
        title = "Bicol River slope protection, Naga City segment",
        category = Category.FloodControl,
        level = Level.National,
        status = Status.Planned,
        place = Place("Sabang", "Naga City", "Camarines Sur", "Region V (Bicol)", Coordinates(13.621, 123.195)),
        description = "A stone masonry slope with a concrete toe wall and a 3-metre walkway along 850 metres of " +
            "eroding riverbank where 140 houses stand within 10 metres of the water.",
        descriptionAttribution = agency("DPWH Camarines Sur 3rd DEO budget proposal", Day(2026, 8, 1)),
        contract = Contract(
            amount = 96_000_000,
            fundingSource = "General Appropriations Act 2027 (proposed, not yet enacted)",
            reference = null,
            attribution = agency("DPWH budget proposal; no award yet", Day(2026, 8, 1)),
        ),
        schedule = Schedule(Day(2027, 1, 11), Day(2027, 12, 17), null, null, agency("Indicative schedule in the budget proposal", Day(2026, 8, 1))),
        progress = Progress(reported = null, independent = null, financial = null),
        parties = listOf(
            Party(PartyRole.Implementing, "DPWH Camarines Sur 3rd District Engineering Office", null, agency("Budget proposal", Day(2026, 8, 1))),
            Party(PartyRole.Official, "Engr. Victor Salazar", "District Engineer, DPWH Camarines Sur 3rd DEO", agency("Budget proposal", Day(2026, 8, 1))),
            Party(PartyRole.Official, "Engr. Rina Pascual", "Planning and Design Chief, DPWH Camarines Sur 3rd DEO", agency("Budget proposal", Day(2026, 8, 1))),
        ),
        updates = listOf(
            Update(Day(2026, 8, 20), "Public consultation held", "Residents of Purok 1 to 3 reviewed the design. Their comments are in the minutes.", null, agency("Minutes of the consultation", Day(2026, 8, 20))),
        ),
        photos = emptyList(),
        documents = listOf(
            Document(DocumentKind.Budget, "Proposed GAA 2027 line item", Day(2026, 8, 1), null, agency("DBM, National Expenditure Program 2027")),
            Document(DocumentKind.Minutes, "Minutes of the public consultation", Day(2026, 8, 20), null, agency("DPWH Camarines Sur 3rd DEO")),
        ),
        sources = listOf(fixtureSource),
        revisions = listOf(
            Revision(Day(2026, 8, 5), "Record created from the proposed budget line item", EDITORS),
            Revision(Day(2026, 8, 28), "Consultation minutes added", EDITORS),
        ),
        lastChecked = Day(2026, 8, 28),
        sample = true,
    ),
    Project(
        id = "malabog-farm-road",
        title = "Malabog–Lumbo farm-to-market road",
        category = Category.Roads,
        level = Level.Provincial,
        status = Status.Completed,
        place = Place(null, "Valencia City", "Bukidnon", "Region X (Northern Mindanao)", Coordinates(7.906, 125.094)),
        description = "Concreting of 4.1 kilometres of dirt road, 6 metres wide with drainage canals on both sides, " +
            "between Barangays Malabog and Lumbo.",
        descriptionAttribution = agency("Provincial Government of Bukidnon project posting", Day(2025, 4, 10)),
        contract = Contract(
            amount = 27_600_000,
            fundingSource = "Department of Agriculture farm-to-market road program and provincial counterpart",
            reference = "25P00088 (sample)",
            attribution = agency("Notice of award, PhilGEPS posting", Day(2025, 3, 14)),
        ),
        schedule = Schedule(Day(2025, 4, 7), Day(2026, 2, 26), null, Day(2026, 3, 6), agency("Certificate of completion", Day(2026, 3, 6))),
        progress = Progress(
            reported = PhysicalProgress(100, Day(2026, 3, 6), agency("Certificate of completion", Day(2026, 3, 6))),
            independent = PhysicalProgress(100, Day(2026, 4, 12), verified("Site visit, sample volunteer check", Day(2026, 4, 12))),
            financial = FinancialProgress(27_150_000, Day(2026, 4, 15), agency("Provincial disbursement report; ₱450,000 returned to the treasury", Day(2026, 4, 15))),
        ),
        parties = listOf(
            Party(PartyRole.Proposing, "Provincial Government of Bukidnon", null, agency("Project posting", Day(2025, 4, 10))),
            Party(PartyRole.Implementing, "Bukidnon Provincial Engineering Office", null, agency("Project posting", Day(2025, 4, 10))),
            Party(PartyRole.Contractor, "Kalabaw Road Works", "Named in the notice of award", agency("PhilGEPS posting", Day(2025, 3, 14))),
            Party(PartyRole.Official, "Engr. Arturo Galang", "Provincial Engineer, Bukidnon", agency("Project posting", Day(2025, 4, 10))),
            Party(PartyRole.Official, "Engr. Mae Abellera", "Project Engineer, Bukidnon PEO", agency("Project posting", Day(2025, 4, 10))),
        ),
        updates = listOf(
            Update(Day(2025, 4, 21), "Grading starts", "Road widening and grading from the Malabog end.", 10, agency("Provincial progress update", Day(2025, 4, 21))),
            Update(Day(2025, 10, 3), "2 kilometres concreted", "Half the length is done. Drainage canals follow the pour.", 50, agency("Provincial progress update", Day(2025, 10, 3))),
            Update(Day(2026, 3, 6), "Road opened", "All 4.1 kilometres are open. Unused funds returned to the treasury.", 100, agency("Certificate of completion", Day(2026, 3, 6))),
            Update(Day(2026, 4, 12), "Site check", "Full length concreted with canals on both sides. No road signs yet near the Lumbo school.", 100, verified("Site visit, sample volunteer check", Day(2026, 4, 12))),
        ),
        photos = emptyList(),
        documents = listOf(
            Document(DocumentKind.Award, "Notice of award to Kalabaw Road Works", Day(2025, 3, 14), null, agency("PhilGEPS")),
            Document(DocumentKind.ProgramOfWork, "Program of work and detailed estimates", Day(2025, 3, 1), null, agency("Bukidnon PEO")),
            Document(DocumentKind.Completion, "Certificate of completion", Day(2026, 3, 6), null, agency("Bukidnon PEO")),
        ),
        sources = listOf(fixtureSource),
        revisions = listOf(
            Revision(Day(2025, 4, 10), "Record created from the provincial project posting", EDITORS),
            Revision(Day(2026, 3, 9), "Status set to Completed; actual completion date added", EDITORS),
            Revision(Day(2026, 4, 18), "Independent site check and final disbursement added", EDITORS),
        ),
        lastChecked = Day(2026, 4, 18),
        sample = true,
    ),
    Project(
        id = "culasi-evacuation-center",
        title = "Culasi covered court and evacuation center",
        category = Category.Buildings,
        level = Level.Municipal,
        status = Status.Ongoing,
        place = Place("Culasi", "Roxas City", "Capiz", "Region VI (Western Visayas)", Coordinates(11.585, 122.751)),
        description = "A covered court with a typhoon-rated steel roof, raised flooring, separate toilets, a kitchen " +
            "and a 10,000-litre water tank, built to shelter 600 people during evacuations.",
        descriptionAttribution = agency("Roxas City project posting", Day(2026, 6, 5)),
        contract = Contract(
            amount = 18_900_000,
            fundingSource = "City 20% Development Fund and Local Disaster Risk Reduction and Management Fund",
            reference = "26C00201 (sample)",
            attribution = agency("Notice of award, PhilGEPS posting", Day(2026, 5, 15)),
        ),
        schedule = Schedule(Day(2026, 6, 2), Day(2027, 3, 31), null, null, agency("Contract as posted", Day(2026, 5, 15))),
        progress = Progress(
            reported = PhysicalProgress(34, Day(2026, 9, 30), agency("City progress update", Day(2026, 9, 30))),
            independent = null,
            financial = FinancialProgress(6_600_000, Day(2026, 9, 30), agency("City disbursement report", Day(2026, 9, 30))),
        ),
        parties = listOf(
            Party(PartyRole.Proposing, "City Government of Roxas, City Disaster Risk Reduction and Management Office", null, agency("Project posting", Day(2026, 6, 5))),
            Party(PartyRole.Implementing, "Roxas City Engineering Office", null, agency("Project posting", Day(2026, 6, 5))),
            Party(PartyRole.Contractor, "Banca Builders", "Named in the notice of award", agency("PhilGEPS posting", Day(2026, 5, 15))),
            Party(PartyRole.Official, "Engr. Gilbert Uy", "City Engineer, Roxas City", agency("Project posting", Day(2026, 6, 5))),
            Party(PartyRole.Official, "Ma. Cristina Lim", "CDRRM Officer, Roxas City", agency("Project posting", Day(2026, 6, 5))),
        ),
        updates = listOf(
            Update(Day(2026, 6, 15), "Foundation works", "Footings poured for the 12 main columns.", 12, agency("City progress update", Day(2026, 6, 15))),
            Update(Day(2026, 9, 30), "Columns and roof trusses", "Steel columns up. Roof trusses arriving in October.", 34, agency("City progress update", Day(2026, 9, 30))),
        ),
        photos = emptyList(),
        documents = listOf(
            Document(DocumentKind.Budget, "2026 annual investment program, Roxas City", Day(2025, 11, 20), null, agency("Full Disclosure Policy posting")),
            Document(DocumentKind.Award, "Notice of award to Banca Builders", Day(2026, 5, 15), null, agency("PhilGEPS")),
        ),
        sources = listOf(fixtureSource),
        revisions = listOf(
            Revision(Day(2026, 6, 5), "Record created from the city's project posting", EDITORS),
            Revision(Day(2026, 10, 2), "Physical progress updated to 34% from the Sep 30 update", EDITORS),
        ),
        lastChecked = null,
        sample = true,
    ),
    Project(
        id = "munoz-canal-lining",
        title = "Muñoz lateral canal concrete lining",
        category = Category.Irrigation,
        level = Level.National,
        status = Status.Suspended,
        place = Place(null, "Science City of Muñoz", "Nueva Ecija", "Region III (Central Luzon)", Coordinates(15.716, 120.903)),
        description = "Concrete lining of 7.5 kilometres of earth canal serving about 1,100 hectares of rice land, " +
            "done in sections between harvests so water keeps flowing during planting.",
        descriptionAttribution = agency("NIA Region III project posting", Day(2024, 11, 8)),
        contract = Contract(
            amount = 64_300_000,
            fundingSource = "General Appropriations Act 2024 and 2025, National Irrigation Administration",
            reference = "24N00312 (sample)",
            attribution = agency("Notice of award, PhilGEPS posting", Day(2024, 10, 21))),
        schedule = Schedule(Day(2024, 11, 4), Day(2027, 1, 29), null, null, agency("Contract as posted", Day(2024, 10, 21))),
        progress = Progress(
            reported = PhysicalProgress(70, Day(2026, 8, 26), agency("NIA progress update", Day(2026, 8, 26))),
            independent = null,
            financial = FinancialProgress(41_000_000, Day(2026, 8, 31), agency("NIA disbursement report", Day(2026, 8, 31))),
        ),
        parties = listOf(
            Party(PartyRole.Proposing, "National Irrigation Administration, Region III", null, agency("Project posting", Day(2024, 11, 8))),
            Party(PartyRole.Implementing, "NIA Nueva Ecija Irrigation Management Office", null, agency("Project posting", Day(2024, 11, 8))),
            Party(PartyRole.Contractor, "Palay Engineering Services", "Named in the notice of award", agency("PhilGEPS posting", Day(2024, 10, 21))),
            Party(PartyRole.Official, "Engr. Rolando Mercado", "Regional Manager, NIA Region III", agency("Project posting", Day(2024, 11, 8))),
            Party(PartyRole.Official, "Engr. Joan Sison", "Project Engineer, NIA Nueva Ecija IMO", agency("Project posting", Day(2024, 11, 8))),
        ),
        updates = listOf(
            Update(Day(2024, 11, 18), "First section starts", "Lining starts at the head gate after the wet season harvest.", 8, agency("NIA progress update", Day(2024, 11, 18))),
            Update(Day(2025, 6, 2), "3 kilometres lined", "Works pause for the wet season planting. Water flowing.", 40, agency("NIA progress update", Day(2025, 6, 2))),
            Update(Day(2026, 8, 26), "5.2 kilometres lined", "Tail-end farms report water arriving two days sooner.", 70, agency("NIA progress update", Day(2026, 8, 26))),
            Update(Day(2026, 9, 15), "Works suspended", "Suspension order issued for the wet-season cropping. Resumption planned for November 2026.", null, agency("NIA suspension order", Day(2026, 9, 15))),
        ),
        photos = emptyList(),
        documents = listOf(
            Document(DocumentKind.Award, "Notice of award to Palay Engineering Services", Day(2024, 10, 21), null, agency("PhilGEPS")),
            Document(DocumentKind.ProgramOfWork, "Program of work, Lateral C", Day(2024, 10, 1), null, agency("NIA Region III")),
            Document(DocumentKind.Other, "Suspension order", Day(2026, 9, 15), null, agency("NIA Nueva Ecija IMO")),
        ),
        sources = listOf(fixtureSource),
        revisions = listOf(
            Revision(Day(2024, 11, 8), "Record created from the agency's project posting", EDITORS),
            Revision(Day(2026, 8, 30), "Physical progress updated to 70% from the Aug 26 update", EDITORS),
            Revision(Day(2026, 9, 20), "Status set to Suspended from the suspension order", EDITORS),
        ),
        lastChecked = Day(2026, 9, 20),
        sample = true,
    ),
    Project(
        id = "tinago-road-concreting",
        title = "Purok 3 to Purok 6 road concreting",
        category = Category.Roads,
        level = Level.Barangay,
        status = Status.Ongoing,
        place = Place("Tinago", "Ormoc City", "Leyte", "Region VIII (Eastern Visayas)", coordinates = null),
        description = "Concreting of 480 metres of road used by 200 households and by pupils walking to Tinago " +
            "Elementary School, poured in 60-metre sections.",
        descriptionAttribution = agency("Barangay Tinago project posting", Day(2026, 8, 20)),
        contract = Contract(
            amount = 2_150_000,
            fundingSource = "Barangay 20% Development Fund 2026",
            reference = null,
            attribution = agency("Barangay appropriation ordinance", Day(2025, 12, 10)),
        ),
        schedule = Schedule(Day(2026, 8, 18), Day(2026, 12, 4), null, null, agency("Program of work", Day(2026, 8, 1))),
        progress = Progress(
            reported = PhysicalProgress(40, Day(2026, 10, 6), agency("Barangay progress update", Day(2026, 10, 6))),
            independent = null,
            financial = null,
        ),
        parties = listOf(
            Party(PartyRole.Proposing, "Sangguniang Barangay of Tinago", null, agency("Project posting", Day(2026, 8, 20))),
            Party(PartyRole.Implementing, "Barangay Tinago", null, agency("Project posting", Day(2026, 8, 20))),
            Party(PartyRole.Contractor, "Lakbay Concrete Works", "Named in the project posting; no award notice on file", agency("Project posting", Day(2026, 8, 20))),
            Party(PartyRole.Official, "Hon. Lourdes Abad", "Punong Barangay, Barangay Tinago", agency("Project posting", Day(2026, 8, 20))),
            Party(PartyRole.Official, "Hon. Rey Gallardo", "Chair, Committee on Infrastructure, Barangay Tinago", agency("Project posting", Day(2026, 8, 20))),
        ),
        updates = listOf(
            Update(Day(2026, 8, 25), "Base course laid", "Gravel base compacted along the whole 480 metres.", 15, agency("Barangay progress update", Day(2026, 8, 25))),
            Update(Day(2026, 10, 6), "Three sections poured", "180 of 480 metres poured. Next pour after the rains ease.", 40, agency("Barangay progress update", Day(2026, 10, 6))),
        ),
        photos = emptyList(),
        documents = listOf(
            Document(DocumentKind.Budget, "Barangay appropriation ordinance 2026", Day(2025, 12, 10), null, agency("Full Disclosure Policy posting")),
            Document(DocumentKind.ProgramOfWork, "Program of work", Day(2026, 8, 1), null, agency("Barangay Tinago")),
        ),
        sources = listOf(fixtureSource),
        revisions = listOf(
            Revision(Day(2026, 8, 20), "Record created from the barangay's project posting", EDITORS),
            Revision(Day(2026, 10, 8), "Physical progress updated to 40% from the Oct 6 update", EDITORS),
        ),
        lastChecked = null,
        sample = true,
    ),
    Project(
        id = "tagum-science-lab",
        title = "Tagum City National High School science laboratory",
        category = Category.Schools,
        level = Level.Municipal,
        status = Status.Completed,
        place = Place("Apokon", "Tagum City", "Davao del Norte", "Region XI (Davao)", Coordinates(7.448, 125.808)),
        description = "Two classrooms joined into a chemistry and physics laboratory with eight workstations, a fume " +
            "hood, an eyewash station and a stock room, for 2,600 senior high school students.",
        descriptionAttribution = agency("Tagum City project posting", Day(2026, 3, 5)),
        contract = Contract(
            amount = 6_800_000,
            fundingSource = "Special Education Fund 2026",
            reference = "26S00044 (sample)",
            attribution = agency("Notice of award, PhilGEPS posting", Day(2026, 2, 16)),
        ),
        schedule = Schedule(Day(2026, 3, 2), Day(2026, 9, 15), null, Day(2026, 9, 14), agency("Certificate of completion", Day(2026, 9, 14))),
        progress = Progress(
            reported = PhysicalProgress(100, Day(2026, 9, 14), agency("Certificate of completion", Day(2026, 9, 14))),
            independent = PhysicalProgress(100, Day(2026, 9, 25), community("Citizen report through the app, with photos described", Day(2026, 9, 25))),
            financial = FinancialProgress(6_720_000, Day(2026, 9, 30), agency("City disbursement report", Day(2026, 9, 30))),
        ),
        parties = listOf(
            Party(PartyRole.Proposing, "Tagum City Local School Board", null, agency("Project posting", Day(2026, 3, 5))),
            Party(PartyRole.Implementing, "Tagum City Engineering Office", null, agency("Project posting", Day(2026, 3, 5))),
            Party(PartyRole.Contractor, "Agila Builders", "Named in the notice of award", agency("PhilGEPS posting", Day(2026, 2, 16))),
            Party(PartyRole.Official, "Engr. Danilo Corpuz", "City Engineer, Tagum City", agency("Project posting", Day(2026, 3, 5))),
            Party(PartyRole.Official, "Dr. Helen Bacani", "School Principal, Tagum City National High School", agency("Project posting", Day(2026, 3, 5))),
        ),
        updates = listOf(
            Update(Day(2026, 3, 16), "Room renovation starts", "Two old classrooms joined to make the laboratory space.", 20, agency("City progress update", Day(2026, 3, 16))),
            Update(Day(2026, 9, 14), "Laboratory turned over", "Workstations installed and safety-inspected. First classes the following week.", 100, agency("Certificate of completion", Day(2026, 9, 14))),
            Update(Day(2026, 9, 25), "In use", "A student reports the laboratory in use for a titration class, with eight workstations and one fume hood.", 100, community("Citizen report through the app", Day(2026, 9, 25))),
        ),
        photos = emptyList(),
        documents = listOf(
            Document(DocumentKind.Budget, "Special Education Fund budget 2026", Day(2025, 12, 5), null, agency("Full Disclosure Policy posting")),
            Document(DocumentKind.Award, "Notice of award to Agila Builders", Day(2026, 2, 16), null, agency("PhilGEPS")),
            Document(DocumentKind.Completion, "Certificate of completion", Day(2026, 9, 14), null, agency("Tagum City Engineering Office")),
        ),
        sources = listOf(fixtureSource),
        revisions = listOf(
            Revision(Day(2026, 3, 5), "Record created from the city's project posting", EDITORS),
            Revision(Day(2026, 9, 16), "Status set to Completed from the certificate of completion", EDITORS),
            Revision(Day(2026, 9, 28), "Citizen report added to the timeline", EDITORS),
        ),
        lastChecked = Day(2026, 9, 28),
        sample = true,
    ),
)
