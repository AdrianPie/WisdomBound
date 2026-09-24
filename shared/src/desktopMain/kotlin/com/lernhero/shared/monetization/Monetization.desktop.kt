package com.lernhero.shared.monetization

actual fun platformSubscriptionService(): SubscriptionService = SubscriptionService.None

actual fun platformRewardedAds(): RewardedAds = RewardedAds.None

actual fun platformOneTimePurchases(): OneTimePurchases = OneTimePurchases.None
