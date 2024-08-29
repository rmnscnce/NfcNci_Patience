# NfcNci Patience

Add a delay (1000ms by default) to the NFC presence check to accommodate longer NFC smart card operations in the AOSP NfcNci implementation on certain devices (see [LineageOS issue #7268](https://gitlab.com/LineageOS/issues/android/-/issues/7268))

This bug — which may sound obscure at first, plagues several apps e.g. the German eID app and most of Indonesian electronic pass (Mandiri e-money, BCA Flazz, etc) recharge apps (failure upon balance update) due to those apps not having sufficient [`EXTRA_READER_PRESENCE_CHECK_DELAY`](https://developer.android.com/reference/android/nfc/NfcAdapter#EXTRA_READER_PRESENCE_CHECK_DELAY) value, which results in the NFC service's assumption that the smart card has been lost.

## Releases

Releases can be obtained from the [release page](https://github.com/Xposed-Modules-Repo/id.my.pjm.toys.nfcnci_patience/releases) in the Xposed Modules Repository page for this project.