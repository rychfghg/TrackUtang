package com.trackutang.app;

/** Curated Philippine borrowing providers. Users may always choose Other for a provider not listed. */
public final class ProviderCatalog {
    private ProviderCatalog() { }

    public static String[] borrowedProviders() {
        return new String[]{
                "GCash – GLoan", "GCash – GGives", "GCash – GCredit",
                "Maya – Easy Credit", "Maya – Personal Loan",
                "SeaBank – Credit", "CIMB Bank – Personal Loan", "Tonik Bank – Quick Loan", "Tonik Bank – Big Loan", "UNO Digital Bank – Ready Cash", "GoTyme Bank – Personal Loan", "MariBank – Credit", "UnionDigital Bank – Personal Loan", "OwnBank – Loan Offer",
                "BillEase", "Atome Cash", "Atome Card", "TendoPay", "Home Credit", "Salmon Credit", "Skyro",
                "Tala", "JuanHand", "Cashalo", "Digido", "Finbro", "MoneyCat", "Cash Express", "Kviku", "Finizi", "Mabilis Cash", "Pera247", "CashMe", "CashXpress", "Robocash", "Cashwagon", "OLP Lending",
                "BDO Personal Loan", "BPI Personal Loan", "Metrobank Personal Loan", "RCBC Personal Loan", "Security Bank Personal Loan", "EastWest Personal Loan", "Chinabank Personal Loan", "PSBank Personal Loan", "LandBank Multi-Purpose Loan", "PNB Personal Loan", "UnionBank Personal Loan",
                "Friend or family", "Other"
        };
    }
}
