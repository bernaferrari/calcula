import Shared

/// The one place Swift talks to the shared Kotlin engine for Siri and Shortcuts. Kept apart from
/// the intents because the framework's names (Parameter, Summary…) would shadow App Intents' own.
enum CalculaBridge {
    struct Answer {
        let spoken: String
        let result: String
    }

    static func calculate(_ input: String) -> Answer? {
        ShortcutsKt.calculateForShortcut(input: input).map { Answer(spoken: $0.spoken, result: $0.result) }
    }

    static func lastResult() -> Answer? {
        ShortcutsKt.lastResultForShortcut().map { Answer(spoken: $0.spoken, result: $0.result) }
    }
}
