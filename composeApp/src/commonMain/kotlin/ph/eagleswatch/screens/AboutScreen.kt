package ph.eagleswatch.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import ph.eagleswatch.content.GITHUB_URL
import ph.eagleswatch.content.SUGGEST_SOURCE_URL
import ph.eagleswatch.content.Status
import ph.eagleswatch.content.Verification
import ph.eagleswatch.content.facts
import ph.eagleswatch.content.principles
import ph.eagleswatch.content.publicSources
import ph.eagleswatch.resources.Res
import ph.eagleswatch.resources.eagle_logo
import ph.eagleswatch.state.CitizenState
import ph.eagleswatch.ui.EText
import ph.eagleswatch.ui.EagleColors
import ph.eagleswatch.ui.EagleMetrics
import ph.eagleswatch.ui.EagleType
import ph.eagleswatch.ui.FlagMark
import ph.eagleswatch.ui.Glyph
import ph.eagleswatch.ui.Hairline
import ph.eagleswatch.ui.LinkRow
import ph.eagleswatch.ui.LocalLayout
import ph.eagleswatch.ui.Notice
import ph.eagleswatch.ui.NoticeTone
import ph.eagleswatch.ui.SectionLabel
import ph.eagleswatch.ui.StatusTag
import ph.eagleswatch.ui.VerificationTag

/** What the project is, how to read its records, where the data will come from, and how to take part. */
@Composable
fun AboutScreen(citizen: CitizenState, onReport: () -> Unit, modifier: Modifier = Modifier) {
    val layout = LocalLayout.current
    val uriHandler = LocalUriHandler.current
    Column(modifier.fillMaxSize().background(EagleColors.Page).verticalScroll(rememberScrollState())) {
        TabTitle("About")
        ContentColumn(max = EagleMetrics.ReadingMax) {
            Column(Modifier.padding(horizontal = layout.gutter)) {
                EText("Follow every peso.", display = true, size = EagleType.Display, lineHeight = 30.sp, color = EagleColors.Blue)
                EText("Sundan ang bawat piso.", size = EagleType.Secondary, weight = FontWeight.SemiBold, color = EagleColors.Muted)
                Spacer(Modifier.height(10.dp))
                EText(
                    text = "The Eagle's Watch lists public infrastructure projects in the Philippines with their contracts, " +
                        "schedules, reported progress and the documents behind them, so citizens can check what is being " +
                        "built with public money and by whom.",
                    size = EagleType.Body,
                    lineHeight = 22.sp,
                    color = EagleColors.Ink,
                )
                Spacer(Modifier.height(16.dp))
                Notice(
                    title = "Where the project stands",
                    text = "This preview runs on a sample dataset of ten fictional projects, labelled on every screen. " +
                        "No live government source is connected yet. When one is, each figure will link to its official record.",
                    tone = NoticeTone.Sample,
                )

                AboutSection("How to read a record") {
                    EText(
                        text = "Every figure says how it was established.",
                        size = EagleType.Secondary,
                        lineHeight = 20.sp,
                        color = EagleColors.Body,
                    )
                    Spacer(Modifier.height(10.dp))
                    Legend(Verification.Verified, "Checked against an independent source: a site visit, an audit report or a second record.")
                    Legend(Verification.AgencyReported, "Taken from the implementing or proposing agency's own posting, unchecked.")
                    Legend(Verification.CommunityReported, "Submitted by a citizen through the app, unchecked.")
                    Legend(Verification.Unavailable, "Nobody has supplied this information yet. The record says so instead of guessing.")
                    Spacer(Modifier.height(12.dp))
                    EText(
                        text = "Status, physical progress and funds disbursed are three separate facts. A project can be ongoing " +
                            "at 40% physical progress with 80% of its funds released, and the record shows all three.",
                        size = EagleType.Secondary,
                        lineHeight = 20.sp,
                        color = EagleColors.Body,
                    )
                    Spacer(Modifier.height(10.dp))
                    Status.entries.forEach { status ->
                        Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            StatusTag(status)
                            Spacer(Modifier.width(10.dp))
                            EText(
                                text = when (status) {
                                    Status.Planned -> "Proposed or awarded, works not started."
                                    Status.Ongoing -> "Works under way, as reported."
                                    Status.Delayed -> "Behind its contract schedule, or on an extended one."
                                    Status.Suspended -> "Works stopped under a suspension order."
                                    Status.Completed -> "Turned over, with a completion record."
                                },
                                size = EagleType.Secondary,
                                lineHeight = 19.sp,
                                color = EagleColors.Body,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                AboutSection("What we promise") {
                    principles.forEachIndexed { index, principle ->
                        if (index > 0) Spacer(Modifier.height(10.dp))
                        EText(principle.title, size = EagleType.Body, weight = FontWeight.SemiBold, lineHeight = 20.sp)
                        EText(principle.body, size = EagleType.Secondary, lineHeight = 20.sp, color = EagleColors.Body)
                    }
                }

                AboutSection("Public sources we plan to connect") {
                    publicSources.forEachIndexed { index, source ->
                        val url = source.url
                        LinkRow(
                            title = source.name,
                            glyph = Glyph.Document,
                            onClick = if (url != null) ({ uriHandler.openUri(url) }) else null,
                            external = true,
                            unavailable = "No link on file",
                        )
                        if (index < publicSources.lastIndex) Hairline()
                    }
                }

                AboutSection("Rules of public money") {
                    EText(
                        text = "Real rules of Philippine public finance, each with the law it comes from. These are not sample data.",
                        size = EagleType.Caption,
                        lineHeight = 16.sp,
                        color = EagleColors.Muted,
                    )
                    Spacer(Modifier.height(8.dp))
                    facts.forEachIndexed { index, fact ->
                        if (index > 0) Spacer(Modifier.height(10.dp))
                        EText(fact.text, size = EagleType.Secondary, lineHeight = 20.sp, color = EagleColors.Ink)
                        EText(fact.source, size = EagleType.Caption, lineHeight = 16.sp, color = EagleColors.Muted)
                    }
                }

                if (citizen.reports.isNotEmpty()) {
                    AboutSection("My reports") {
                        EText(
                            text = "Kept on this device. Keep the reference number if you follow up by phone.",
                            size = EagleType.Caption,
                            lineHeight = 16.sp,
                            color = EagleColors.Muted,
                        )
                        Spacer(Modifier.height(8.dp))
                        citizen.reports.forEachIndexed { index, report ->
                            if (index > 0) Spacer(Modifier.height(8.dp))
                            ReportCard(report)
                        }
                    }
                }

                AboutSection("Take part") {
                    LinkRow(
                        title = "Report a problem",
                        detail = "About a project, an office or a public servant",
                        glyph = Glyph.Flag,
                        onClick = onReport,
                    )
                    Hairline()
                    LinkRow(
                        title = "View the code on GitHub",
                        detail = "The Eagle's Watch is open source",
                        glyph = Glyph.External,
                        onClick = { uriHandler.openUri(GITHUB_URL) },
                        external = true,
                    )
                    Hairline()
                    LinkRow(
                        title = "Suggest a data source",
                        detail = "Point us to a public record we should connect",
                        glyph = Glyph.Plus,
                        onClick = { uriHandler.openUri(SUGGEST_SOURCE_URL) },
                        external = true,
                    )
                }

                Spacer(Modifier.height(28.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(Res.drawable.eagle_logo),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(9.dp)),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        EText("Built by citizens, for citizens.", size = EagleType.Secondary, weight = FontWeight.SemiBold)
                        EText("Non-partisan and open source.", size = EagleType.Label, color = EagleColors.Muted)
                    }
                }
                Spacer(Modifier.height(12.dp))
                FlagMark()
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun AboutSection(title: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(top = 20.dp)) {
        Hairline()
        Spacer(Modifier.height(12.dp))
        SectionLabel(title)
        Spacer(Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun Legend(verification: Verification, text: String) {
    Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
        VerificationTag(verification, Modifier.width(150.dp))
        Spacer(Modifier.width(10.dp))
        EText(text, size = EagleType.Secondary, lineHeight = 19.sp, color = EagleColors.Body, modifier = Modifier.weight(1f))
    }
}
