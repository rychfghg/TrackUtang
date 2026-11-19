package com.trackutang.app;

/** Calculates a fixed monthly amortized payment. Interest is a monthly percentage. */
public final class InstallmentCalculator {
    private InstallmentCalculator() { }
    public static double monthlyPayment(double principal, int months, double monthlyRatePercent) {
        if (principal <= 0 || months <= 0) return 0;
        double rate = monthlyRatePercent / 100.0;
        if (rate == 0) return principal / months;
        double factor = Math.pow(1 + rate, months);
        return principal * (rate * factor) / (factor - 1);
    }
}
