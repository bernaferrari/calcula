import SwiftUI
import WidgetKit

/// History as the app keeps it (JSON in the shared app group), newest first.
struct HistoryEntry: Decodable, Hashable {
    let expression: String
    let result: String
    let timestamp: Double
    let note: String?
}

enum History {
    static let group = "group.com.bernaferrari.calcula"

    static func load() -> [HistoryEntry] {
        guard let json = UserDefaults(suiteName: group)?.string(forKey: "history"),
              let data = json.data(using: .utf8),
              let entries = try? JSONDecoder().decode([HistoryEntry].self, from: data) else { return [] }
        return entries.sorted { $0.timestamp > $1.timestamp }
    }

    /// The app's language made readable: its point as the region's, E notation as ×10ⁿ.
    static func pretty(_ text: String) -> String {
        let decimal = Locale.current.decimalSeparator ?? "."
        var out = text.replacingOccurrences(of: ".", with: decimal)
        if let range = out.range(of: "E") {
            let exponent = out[range.upperBound...]
            let superscripts: [Character: Character] = ["0": "⁰", "1": "¹", "2": "²", "3": "³", "4": "⁴", "5": "⁵", "6": "⁶", "7": "⁷", "8": "⁸", "9": "⁹", "−": "⁻", "-": "⁻"]
            out = String(out[..<range.lowerBound]) + "×10" + String(exponent.map { superscripts[$0] ?? $0 })
        }
        return out
    }
}

struct CalculaEntry: TimelineEntry {
    let date: Date
    let history: [HistoryEntry]
}

struct Provider: TimelineProvider {
    func placeholder(in context: Context) -> CalculaEntry {
        CalculaEntry(date: .now, history: [HistoryEntry(expression: "12+7×3", result: "33", timestamp: 0, note: nil)])
    }

    func getSnapshot(in context: Context, completion: @escaping (CalculaEntry) -> Void) {
        let history = History.load()
        completion(history.isEmpty ? placeholder(in: context) : CalculaEntry(date: .now, history: history))
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<CalculaEntry>) -> Void) {
        // Nothing changes on its own: the app and Siri ask for a reload when there's a new answer.
        completion(Timeline(entries: [CalculaEntry(date: .now, history: History.load())], policy: .never))
    }
}

private let accent = Color(red: 0.31, green: 0.27, blue: 0.90)

struct CalculaWidgetView: View {
    @Environment(\.widgetFamily) private var family
    let entry: CalculaEntry

    var body: some View {
        switch family {
        case .accessoryInline:
            Text(entry.history.first.map { "= " + History.pretty($0.result) } ?? "Calcula")
        case .accessoryRectangular:
            VStack(alignment: .leading, spacing: 0) {
                if let latest = entry.history.first {
                    Text(History.pretty(latest.expression)).font(.caption).foregroundStyle(.secondary).lineLimit(1)
                    Text("= " + History.pretty(latest.result)).font(.title3.weight(.semibold)).lineLimit(1).minimumScaleFactor(0.5)
                } else {
                    Text("Calcula").font(.headline)
                }
            }
        case .systemMedium:
            medium
        default:
            small
        }
    }

    /// The newest answer, big, with its question above.
    private var small: some View {
        VStack(alignment: .trailing, spacing: 4) {
            Text("Calcula").font(.caption.weight(.semibold)).foregroundStyle(accent).frame(maxWidth: .infinity, alignment: .leading)
            Spacer(minLength: 0)
            if let latest = entry.history.first {
                if let note = latest.note { Text(note).font(.caption2).foregroundStyle(accent).lineLimit(1) }
                Text(History.pretty(latest.expression)).font(.subheadline).foregroundStyle(.secondary).lineLimit(2).minimumScaleFactor(0.7)
                Text(History.pretty(latest.result)).font(.system(size: 40, weight: .regular, design: .rounded)).lineLimit(1).minimumScaleFactor(0.4)
            } else {
                Text("No calculations yet").font(.subheadline).foregroundStyle(.secondary)
            }
        }
    }

    /// The last few, like the tape above the keys.
    private var medium: some View {
        VStack(alignment: .trailing, spacing: 6) {
            Text("Calcula").font(.caption.weight(.semibold)).foregroundStyle(accent).frame(maxWidth: .infinity, alignment: .leading)
            Spacer(minLength: 0)
            if entry.history.isEmpty {
                Text("No calculations yet").font(.subheadline).foregroundStyle(.secondary)
            }
            ForEach(Array(entry.history.prefix(3).reversed().enumerated()), id: \.offset) { index, item in
                let newest = index == min(entry.history.count, 3) - 1
                HStack(spacing: 6) {
                    Spacer(minLength: 0)
                    Text(History.pretty(item.expression)).foregroundStyle(.secondary).lineLimit(1)
                    Text("=").foregroundStyle(.tertiary)
                    Text(History.pretty(item.result)).fontWeight(newest ? .semibold : .medium).lineLimit(1)
                }
                .font(.system(size: newest ? 22 : 16, design: .rounded))
                .minimumScaleFactor(0.6)
            }
        }
    }
}

struct CalculaWidget: Widget {
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: "CalculaLatest", provider: Provider()) { entry in
            CalculaWidgetView(entry: entry)
                .containerBackground(.background, for: .widget)
        }
        .configurationDisplayName("Latest result")
        .description("Your newest answer, one glance away. Tap to keep calculating.")
        .supportedFamilies([.systemSmall, .systemMedium, .accessoryRectangular, .accessoryInline])
    }
}

@main
struct CalculaWidgets: WidgetBundle {
    var body: some Widget {
        CalculaWidget()
    }
}
