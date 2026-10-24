import SwiftUI
import WidgetKit

@main
struct iOSApp: App {
    @Environment(\.scenePhase) private var scenePhase

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
        // The widget shows the newest answer: leaving the app is when it may have changed.
        .onChange(of: scenePhase) { _, phase in
            if phase == .background { WidgetCenter.shared.reloadAllTimelines() }
        }
    }
}
