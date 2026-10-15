package ph.eagleswatch.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import ph.eagleswatch.ui.Card
import ph.eagleswatch.ui.CardHeader
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
import ph.eagleswatch.ui.StatusTag
import ph.eagleswatch.ui.VerificationTag
import ph.eagleswatch.ui.meaning

/** What the project is, how to read its records, where the data will come from, and how to take part. */
@Composable
fun AboutScreen(citizen: CitizenState, onReport: () -> Unit, modifier: Modifier = Modifier) {
    val layout = LocalLayout.current
    val uriHandler = LocalUriHandler.current
    Column(modifier.fillMaxSize().background(EagleColors.Canvas).verticalScroll(rememberScrollState())) {
        TabTitle("About")
        ContentColumn(max = EagleMetrics.ReadingMax) {
            Column(Modifier.padding(horizontal = layout.gutter), verticalArrangement = Arrangement.spacedBy(EagleMetrics.CardGap)) {
                Card {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(painterResource(Res.drawable.eagle_logo), contentDescription = null, modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            EText("Follow every peso.", display = true, size = 22.sp, lineHeight = 26.sp)
                            EText("Sundan ang bawat piso.", size = EagleType.Label, color = EagleColors.Muted)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    EText(
                        "The Eagle's Watch lists public infrastructure projects in the Philippines with their contracts, schedules, " +
                            "reported progress and the documents behind them, so citizens can check what is being built with public money, and by whom.",
                        size = EagleType.Body,
                        lineHeight = 22.sp,
                        color = EagleColors.Body,
                    )
                    Spacer(Modifier.height(12.dp))
                    Notice(
                        title = "Where the project stands",
                        text = "This preview runs on ten fictional projects, labelled on every screen. No live government source is connected yet.",
                        tone = NoticeTone.Sample,
                    )
                }

                Card {
                    CardHeader("How to read a record", subtitle = "Every figure carries one of four labels. Tap a label in the app to see its source.")
                    Spacer(Modifier.height(10.dp))
                    Verification.entries.forEachIndexed { index, v ->
                        if (index > 0) Hairline()
                        Column(Modifier.padding(vertical = 10.dp)) {
                            VerificationTag(v)
                            Spacer(Modifier.height(4.dp))
                            EText(v.meaning, size = EagleType.Secondary, lineHeight = 20.sp, color = EagleColors.Body)
                        }
                    }
                }

                Card {
                    CardHeader("Status, work and money", subtitle = "Three separate facts. A project can be ongoing at 40% work with 80% of its funds released.")
                    Spacer(Modifier.height(10.dp))
                    Status.entries.forEach { status ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            StatusTag(status, Modifier.width(118.dp))
                            Spacer(Modifier.width(10.dp))
                            EText(
                                when (status) {
                                    Status.Planned -> "Proposed or awarded, works not started."
                                    Status.Ongoing -> "Works under way, as reported."
                                    Status.Delayed -> "Behind schedule, or on an extended one."
                                    Status.Suspended -> "Works stopped under a suspension order."
                                    Status.Completed -> "Turned over, with a completion record."
                                },
                                modifier = Modifier.weight(1f),
                                size = EagleType.Secondary,
                                lineHeight = 19.sp,
                                color = EagleColors.Body,
                            )
                        }
                    }
                }

                Card {
                    CardHeader("What we promise")
                    principles.forEach { principle ->
                        Spacer(Modifier.height(12.dp))
                        EText(principle.title, size = EagleType.Body, weight = FontWeight.SemiBold, lineHeight = 21.sp)
                        EText(principle.body, size = EagleType.Secondary, lineHeight = 20.sp, color = EagleColors.Body)
                    }
                }

                Card {
                    CardHeader("Public sources we plan to connect")
                    Spacer(Modifier.height(6.dp))
                    publicSources.forEach { source ->
                        val url = source.url
                        LinkRow(title = source.name, glyph = Glyph.Document, onClick = if (url != null) ({ uriHandler.openUri(url) }) else null, external = true, unavailable = "No link on file")
                    }
                }

                Card {
                    CardHeader("Rules of public money", subtitle = "Real rules of Philippine public finance, each with its law. Not sample data.")
                    facts.forEach { fact ->
                        Spacer(Modifier.height(12.dp))
                        EText(fact.text, size = EagleType.Secondary, lineHeight = 20.sp, color = EagleColors.Ink)
                        EText(fact.source, size = EagleType.Caption, lineHeight = 16.sp, color = EagleColors.Muted)
                    }
                }

                if (citizen.reports.isNotEmpty()) {
                    Card {
                        CardHeader("My reports", subtitle = "Kept on this device. Keep the reference number if you follow up by phone.")
                        citizen.reports.forEach { report ->
                            Spacer(Modifier.height(10.dp))
                            ReportCard(report)
                        }
                    }
                }

                Card {
                    CardHeader("Take part")
                    Spacer(Modifier.height(6.dp))
                    LinkRow(title = "Report a problem", detail = "About a project, an office or a public servant", glyph = Glyph.Flag, onClick = onReport)
                    LinkRow(title = "View the code on GitHub", detail = "The Eagle's Watch is open source", glyph = Glyph.External, onClick = { uriHandler.openUri(GITHUB_URL) }, external = true)
                    LinkRow(title = "Suggest a data source", detail = "Point us to a public record we should connect", glyph = Glyph.Plus, onClick = { uriHandler.openUri(SUGGEST_SOURCE_URL) }, external = true)
                }

                Column(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    FlagMark()
                    Spacer(Modifier.height(8.dp))
                    EText("Built by citizens, for citizens. Non-partisan and open source.", size = EagleType.Caption, color = EagleColors.Muted)
                }
            }
        }
    }
}
