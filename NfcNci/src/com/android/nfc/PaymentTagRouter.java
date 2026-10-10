/*
 * Copyright (C) 2025 The AviumUI Project
 * Copyright (C) 2026 The ArkUI Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.android.nfc;

import android.content.ComponentName;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/** Known payment-tag formats used by AviumUI's external NFC dispatcher. */
final class PaymentTagRouter {
    private PaymentTagRouter() {}

    static ComponentName match(NdefMessage message) {
        if (message == null) return null;
        for (NdefRecord record : message.getRecords()) {
            if (record.getTnf() == NdefRecord.TNF_EXTERNAL_TYPE
                    && Arrays.equals(record.getType(), NdefRecord.RTD_ANDROID_APP)
                    && Arrays.equals(record.getPayload(),
                            "com.eg.android.AlipayGphone".getBytes(StandardCharsets.US_ASCII))) {
                return new ComponentName("com.eg.android.AlipayGphone",
                        "com.alipay.mobile.quinox.SchemeLauncherActivity");
            }
            if (record.getTnf() == NdefRecord.TNF_MIME_MEDIA
                    && "application/com.tag.tagpay".equals(record.toMimeType())) {
                byte[] payload = record.getPayload();
                if (payload.length >= 6 && payload.length <= 8192) {
                    String prefix = new String(payload, 0, 6, StandardCharsets.US_ASCII);
                    if ("000201".equals(prefix) || "000202".equals(prefix)) {
                        return new ComponentName("com.unionpay",
                                "com.unionpay.activity.mine.UPActivityReadNFCTag");
                    }
                }
            }
        }
        return null;
    }
}
