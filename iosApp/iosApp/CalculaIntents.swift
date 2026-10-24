import AppIntents
import WidgetKit

/// "Hey Siri, calculate with Calcula" — or a step in a shortcut. Uses the app's own engine and
/// settings, answers out loud, and keeps the calculation in history like anything typed.
struct CalculateIntent: AppIntent {
    static var title: LocalizedStringResource = "Calculate"
    static var description = IntentDescription("Works out a calculation, like 12 times 3 plus 4 or 15% of 80.")

    @Parameter(title: "Calculation", requestValueDialog: IntentDialog("What would you like to calculate?"))
    var expression: String

    func perform() async throws -> some IntentResult & ReturnsValue<String> & ProvidesDialog {
        guard let answer = CalculaBridge.calculate(expression) else {
            throw $expression.needsValueError("I couldn't work that out. Try something like 12 times 3.")
        }
        WidgetCenter.shared.reloadAllTimelines()
        return .result(value: answer.result, dialog: "\(answer.spoken)")
    }
}

/// The newest answer in history, said back and handed on to the next step of a shortcut.
struct LastResultIntent: AppIntent {
    static var title: LocalizedStringResource = "Last Result"
    static var description = IntentDescription("Says the newest answer in Calcula's history.")

    func perform() async throws -> some IntentResult & ReturnsValue<String> & ProvidesDialog {
        guard let answer = CalculaBridge.lastResult() else {
            return .result(value: "", dialog: "There's nothing in Calcula's history yet.")
        }
        return .result(value: answer.result, dialog: "\(answer.spoken)")
    }
}

/// Ready-made in Shortcuts and Spotlight, and spoken to Siri without any setup.
struct CalculaShortcuts: AppShortcutsProvider {
    static var appShortcuts: [AppShortcut] {
        AppShortcut(
            intent: CalculateIntent(),
            phrases: [
                "Calculate with \(.applicationName)",
                "Do math with \(.applicationName)",
                "\(.applicationName) calculate",
            ],
            shortTitle: "Calculate",
            systemImageName: "plus.forwardslash.minus"
        )
        AppShortcut(
            intent: LastResultIntent(),
            phrases: [
                "Last result in \(.applicationName)",
                "What was my last \(.applicationName) answer",
            ],
            shortTitle: "Last Result",
            systemImageName: "clock.arrow.circlepath"
        )
    }
}
