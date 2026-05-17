import SwiftUI
import ComposeApp
import FirebaseCore
import FirebaseRemoteConfig

@main
struct iOSApp: App {
	init() {
		FirebaseApp.configure()
		fetchRemoteConfig()
	}

	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}

	private func fetchRemoteConfig() {
		let remoteConfig = RemoteConfig.remoteConfig()
		let settings = RemoteConfigSettings()
		settings.minimumFetchInterval = 60
		remoteConfig.configSettings = settings

		remoteConfig.fetchAndActivate { _, error in
			let homeTabList = error == nil ? remoteConfig["home_tab_list"].stringValue : nil
			DispatchQueue.main.async {
				FirebaseRemoteConfigBridgeKt.updateFirebaseRemoteConfigHomeTabListFromSwift(value: homeTabList)
			}
		}
	}
}
