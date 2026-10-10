/* Copyright (C) 2026 The ArkUI Project; SPDX-License-Identifier: Apache-2.0 */
package com.android.nfc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class PaymentTagRouterTest {
    @Test public void alipayApplicationRecordSelectsPaymentActivity() {
        assertEquals("com.eg.android.AlipayGphone/com.alipay.mobile.quinox.SchemeLauncherActivity",
                PaymentTagRouter.match(new NdefMessage(
                        NdefRecord.createApplicationRecord("com.eg.android.AlipayGphone")))
                        .flattenToString());
    }

    @Test public void unionPayRecognizesBothPayloadVersions() {
        for (String payload : new String[] {"000201merchant", "000202merchant"}) {
            assertEquals("com.unionpay/com.unionpay.activity.mine.UPActivityReadNFCTag",
                    PaymentTagRouter.match(new NdefMessage(NdefRecord.createMime(
                            "application/com.tag.tagpay", payload.getBytes(StandardCharsets.US_ASCII))))
                            .flattenToString());
        }
    }

    @Test public void ordinaryAndUnrelatedPaymentRecordsKeepNormalDispatch() {
        assertNull(PaymentTagRouter.match(null));
        assertNull(PaymentTagRouter.match(new NdefMessage(NdefRecord.createUri("https://example.org"))));
        assertNull(PaymentTagRouter.match(new NdefMessage(
                NdefRecord.createApplicationRecord("other.wallet"))));
        assertNull(PaymentTagRouter.match(new NdefMessage(NdefRecord.createMime(
                "application/com.tag.tagpay", "000203".getBytes(StandardCharsets.US_ASCII)))));
    }

    @Test public void similarPayloadWithWrongTypeIsNotAPaymentTag() {
        assertNull(PaymentTagRouter.match(new NdefMessage(NdefRecord.createMime(
                "text/plain", "000201merchant".getBytes(StandardCharsets.US_ASCII)))));
        assertNull(PaymentTagRouter.match(new NdefMessage(NdefRecord.createMime(
                "text/plain", "com.eg.android.AlipayGphone".getBytes(StandardCharsets.US_ASCII)))));
    }

    @Test public void truncatedAndOversizedPayloadsFallBack() {
        assertNull(PaymentTagRouter.match(new NdefMessage(NdefRecord.createMime(
                "application/com.tag.tagpay", new byte[] {'0', '0', '0'}))));
        byte[] oversized = new byte[8193];
        System.arraycopy("000201".getBytes(StandardCharsets.US_ASCII), 0, oversized, 0, 6);
        assertNull(PaymentTagRouter.match(new NdefMessage(NdefRecord.createMime(
                "application/com.tag.tagpay", oversized))));
    }
}
