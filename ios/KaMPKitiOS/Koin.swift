//
//  KoinApplication.swift
//  KaMPStarteriOS
//
//  Created by Russell Wolf on 6/18/20.
//  Copyright © 2020 Touchlab. All rights reserved.
//

import Foundation
import shared

func startKoin() {
    // You could just as easily define all these dependencies in Kotlin,
    // but this helps demonstrate how you might pass platform-specific
    // dependencies in a larger scale project where declaring them in
    // Kotlin is more difficult, or where they're also used in
    // iOS-specific code.

    let userDefaults = UserDefaults(suiteName: "KAMPSTARTER_SETTINGS")!
    let iosAppInfo = IosAppInfo()
    let doOnStartup = { NSLog("Hello from iOS/Swift!") }

    let koinApplication = KoinIOSKt.doInitKoinIos(
        userDefaults: userDefaults,
        appInfo: iosAppInfo,
        doOnStartup: doOnStartup
    )
    _koin = koinApplication.koin
}

private var _koin: Koin_coreKoin?

// @AssertAccess | Error: Property wrappers are not yet supported in top-level code
var koin: Koin_coreKoin {
    assert(_koin != nil, "")
    return _koin!
}

class IosAppInfo: AppInfo {
    let appId: String = Bundle.main.bundleIdentifier!
}

@propertyWrapper
struct AssertAccess<T: AnyObject> {
    private var _wrappedValue: T?
    var wrappedValue: T {
        get {
            assert(_wrappedValue != nil, "Make sure you have an instance before trying to access it")
            return _wrappedValue!
        }
        set {
            _wrappedValue = newValue
        }
    }
}
