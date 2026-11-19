package com.trackutang.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DebtDatabase extends SQLiteOpenHelper {
    private static final String DB = "trackutang.db";
    public DebtDatabase(Context context) { super(context, DB, null, 9); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE debts (id INTEGER PRIMARY KEY AUTOINCREMENT, person TEXT NOT NULL, contact TEXT, category TEXT, tag TEXT, custom_status TEXT, note TEXT, amount REAL NOT NULL, principal REAL NOT NULL DEFAULT 0, paid REAL NOT NULL DEFAULT 0, type TEXT NOT NULL, due TEXT, monthly_amount REAL NOT NULL DEFAULT 0, installments_remaining INTEGER NOT NULL DEFAULT 0, term_months INTEGER NOT NULL DEFAULT 0, monthly_interest REAL NOT NULL DEFAULT 0, archived INTEGER NOT NULL DEFAULT 0, created INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE payments (id INTEGER PRIMARY KEY AUTOINCREMENT, debt_id INTEGER NOT NULL, amount REAL NOT NULL, paid_at INTEGER NOT NULL, note TEXT)");
        db.execSQL("CREATE TABLE activity_log (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, detail TEXT, created INTEGER NOT NULL)");
    }
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { if(oldVersion < 2) { db.execSQL("ALTER TABLE debts ADD COLUMN contact TEXT"); db.execSQL("ALTER TABLE debts ADD COLUMN category TEXT"); } if(oldVersion < 3) db.execSQL("CREATE TABLE activity_log (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, detail TEXT, created INTEGER NOT NULL)"); if(oldVersion < 4) { db.execSQL("ALTER TABLE debts ADD COLUMN monthly_amount REAL NOT NULL DEFAULT 0"); db.execSQL("ALTER TABLE debts ADD COLUMN installments_remaining INTEGER NOT NULL DEFAULT 0"); } if(oldVersion < 5) db.execSQL("ALTER TABLE debts ADD COLUMN tag TEXT"); if(oldVersion < 6) { db.execSQL("ALTER TABLE debts ADD COLUMN term_months INTEGER NOT NULL DEFAULT 0"); db.execSQL("ALTER TABLE debts ADD COLUMN monthly_interest REAL NOT NULL DEFAULT 0"); } if(oldVersion < 7) { db.execSQL("ALTER TABLE debts ADD COLUMN archived INTEGER NOT NULL DEFAULT 0"); db.execSQL("UPDATE debts SET archived=1 WHERE paid >= amount"); } if(oldVersion < 8) { db.execSQL("ALTER TABLE debts ADD COLUMN principal REAL NOT NULL DEFAULT 0"); db.execSQL("UPDATE debts SET principal=amount WHERE principal=0"); db.execSQL("UPDATE debts SET amount=ROUND(monthly_amount * term_months, 2) WHERE monthly_amount>0 AND term_months>0"); db.execSQL("UPDATE debts SET installments_remaining=CAST((MAX(0, amount-paid)+monthly_amount-0.000001)/monthly_amount AS INTEGER) WHERE monthly_amount>0"); db.execSQL("UPDATE debts SET archived=CASE WHEN paid>=amount THEN 1 ELSE 0 END"); } if(oldVersion < 9) db.execSQL("ALTER TABLE debts ADD COLUMN custom_status TEXT"); }

    public long addDebt(String person, String contact, String category, String note, double amount, String type, String due) {
        ContentValues v = new ContentValues(); v.put("person", person); v.put("contact", contact); v.put("category", category); v.put("note", note); v.put("amount", amount); v.put("principal", amount); v.put("type", type); v.put("due", due); v.put("created", System.currentTimeMillis());
        return getWritableDatabase().insert("debts", null, v);
    }
    public void addPayment(long debtId, double amount, String note) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues p = new ContentValues(); p.put("debt_id", debtId); p.put("amount", amount); p.put("note", note); p.put("paid_at", System.currentTimeMillis()); db.insert("payments", null, p);
        db.execSQL("UPDATE debts SET paid = MIN(amount, paid + ?), archived = CASE WHEN paid + ? >= amount THEN 1 ELSE archived END WHERE id = ?", new Object[]{amount, amount, debtId});
        db.execSQL("UPDATE debts SET installments_remaining = CASE WHEN monthly_amount > 0 THEN CAST((MAX(0, amount - paid) + monthly_amount - 0.000001) / monthly_amount AS INTEGER) ELSE 0 END WHERE id = ?", new Object[]{debtId});
        log("Payment recorded", "Payment of " + amount + " was added", System.currentTimeMillis());
    }
    public void updateDebt(long id, String person, String contact, String category, String note, double amount, String due) {
        ContentValues values = new ContentValues();
        values.put("person", person); values.put("contact", contact); values.put("category", category);
        values.put("note", note); values.put("amount", amount); values.put("due", due);
        getWritableDatabase().update("debts", values, "id=?", new String[]{String.valueOf(id)});
    }
    public void setInstallment(long id, double monthlyAmount, int remaining) { ContentValues values=new ContentValues(); values.put("monthly_amount", monthlyAmount); values.put("installments_remaining", remaining); getWritableDatabase().update("debts",values,"id=?",new String[]{String.valueOf(id)}); }
    public void setInstallmentPlan(long id, int months, double monthlyInterest) { ContentValues values=new ContentValues(); values.put("term_months",months); values.put("monthly_interest",monthlyInterest); getWritableDatabase().update("debts",values,"id=?",new String[]{String.valueOf(id)}); }
    public void applyInstallmentPlan(long id, double principal, double totalPayable, double monthlyAmount, int months, double monthlyInterest) { ContentValues values=new ContentValues(); values.put("principal",principal); values.put("amount",totalPayable); values.put("monthly_amount",monthlyAmount); values.put("installments_remaining",months); values.put("term_months",months); values.put("monthly_interest",monthlyInterest); getWritableDatabase().update("debts",values,"id=?",new String[]{String.valueOf(id)}); }
    public void setTag(long id, String tag) { ContentValues values=new ContentValues(); values.put("tag",tag); getWritableDatabase().update("debts",values,"id=?",new String[]{String.valueOf(id)}); }
    public void setCustomStatus(long id, String status) { ContentValues values=new ContentValues(); values.put("custom_status",status); getWritableDatabase().update("debts",values,"id=?",new String[]{String.valueOf(id)}); }
    public List<Debt> activeDebts(String query, String type) { return debtList(query, type, false); }
    public List<Debt> archivedDebts(String type) { return debtList("", type, true); }
    /** Moves a completed record back to the active list without altering its payment history. */
    public void restoreDebt(long id) {
        Debt debt = debt(id);
        ContentValues values = new ContentValues();
        values.put("archived", 0);
        getWritableDatabase().update("debts", values, "id=?", new String[]{String.valueOf(id)});
        if (debt != null) log("Completed record restored", debt.person + " was moved back to active records", System.currentTimeMillis());
    }
    public void archiveDebt(long id) {
        Debt debt = debt(id);
        ContentValues values = new ContentValues();
        values.put("archived", 1);
        getWritableDatabase().update("debts", values, "id=?", new String[]{String.valueOf(id)});
        if (debt != null) log("Record archived", debt.person + " was moved to archived records", System.currentTimeMillis());
    }
    public Debt debt(long id) { Cursor c=getReadableDatabase().query("debts",null,"id=?",new String[]{String.valueOf(id)},null,null,null); Debt value=null; if(c.moveToFirst()) value=fromCursor(c); c.close(); return value; }
    public void deleteDebt(long id) {
        SQLiteDatabase db=getWritableDatabase(); String[] args={String.valueOf(id)}; Cursor c=db.query("debts",new String[]{"person","amount"},"id=?",args,null,null,null); if(c.moveToFirst()) log("Record deleted", c.getString(0) + " · " + c.getDouble(1), System.currentTimeMillis()); c.close();
        db.delete("payments", "debt_id=?", args); db.delete("debts", "id=?", args);
    }
    public void deleteAll() { SQLiteDatabase db=getWritableDatabase(); db.delete("payments",null,null); db.delete("debts",null,null); }
    public void logEvent(String title, String detail) { log(title, detail, System.currentTimeMillis()); }
    private void log(String title, String detail, long time) { ContentValues values=new ContentValues(); values.put("title",title); values.put("detail",detail); values.put("created",time); getWritableDatabase().insert("activity_log",null,values); }
    public List<Activity> activity() { List<Activity> list=new ArrayList<>(); Cursor c=getReadableDatabase().query("activity_log",null,null,null,null,null,"created DESC"); while(c.moveToNext()) list.add(new Activity(c.getString(c.getColumnIndexOrThrow("title")),c.getString(c.getColumnIndexOrThrow("detail")),c.getLong(c.getColumnIndexOrThrow("created")))); c.close(); return list; }
    public List<Payment> payments(long debtId) {
        List<Payment> list = new ArrayList<>();
        Cursor c=getReadableDatabase().query("payments",null,"debt_id=?",new String[]{String.valueOf(debtId)},null,null,"paid_at DESC");
        while(c.moveToNext()) list.add(new Payment(c.getDouble(c.getColumnIndexOrThrow("amount")),c.getLong(c.getColumnIndexOrThrow("paid_at")),c.getString(c.getColumnIndexOrThrow("note"))));
        c.close(); return list;
    }
    public List<Debt> debts(String query, String filter) {
        return debtList(query, filter, null);
    }
    private List<Debt> debtList(String query, String filter, Boolean archived) {
        List<Debt> list = new ArrayList<>();
        String where = "(person LIKE ? OR contact LIKE ? OR category LIKE ? OR tag LIKE ? OR note LIKE ? OR CAST(amount AS TEXT) LIKE ? OR due LIKE ?)"; String like = "%" + query + "%";
        if (!filter.equals("All")) where = where + " AND type=?";
        if (archived != null) where = "(" + where + ") AND archived=" + (archived ? "1" : "0");
        String[] args = filter.equals("All") ? new String[]{like, like, like, like, like, like, like} : new String[]{like, like, like, like, like, like, like, filter};
        Cursor c = getReadableDatabase().query("debts", null, where, args, null, null, "created DESC");
        while (c.moveToNext()) list.add(fromCursor(c));
        c.close(); return list;
    }
    private Debt fromCursor(Cursor c) { return new Debt(c.getLong(c.getColumnIndexOrThrow("id")), c.getString(c.getColumnIndexOrThrow("person")), c.getString(c.getColumnIndexOrThrow("contact")), c.getString(c.getColumnIndexOrThrow("category")), c.getString(c.getColumnIndexOrThrow("tag")), c.getString(c.getColumnIndexOrThrow("custom_status")), c.getString(c.getColumnIndexOrThrow("note")), c.getDouble(c.getColumnIndexOrThrow("amount")), c.getDouble(c.getColumnIndexOrThrow("principal")), c.getDouble(c.getColumnIndexOrThrow("paid")), c.getString(c.getColumnIndexOrThrow("type")), c.getString(c.getColumnIndexOrThrow("due")), c.getDouble(c.getColumnIndexOrThrow("monthly_amount")), c.getInt(c.getColumnIndexOrThrow("installments_remaining")), c.getInt(c.getColumnIndexOrThrow("term_months")), c.getDouble(c.getColumnIndexOrThrow("monthly_interest")), c.getInt(c.getColumnIndexOrThrow("archived")) == 1, c.getLong(c.getColumnIndexOrThrow("created"))); }
    public static class Debt {
        public final long id, created; public final String person, contact, category, tag, customStatus, note, type, due; public final double amount, principal, paid, monthlyAmount, monthlyInterest; public final int installmentsRemaining, termMonths; public final boolean archived;
        Debt(long id, String person, String contact, String category, String tag, String customStatus, String note, double amount, double principal, double paid, String type, String due, double monthlyAmount, int installmentsRemaining, int termMonths, double monthlyInterest, boolean archived, long created) { this.id=id; this.person=person; this.contact=contact; this.category=category; this.tag=tag; this.customStatus=customStatus; this.note=note; this.amount=amount; this.principal=principal; this.paid=paid; this.type=type; this.due=due; this.monthlyAmount=monthlyAmount; this.installmentsRemaining=installmentsRemaining; this.termMonths=termMonths; this.monthlyInterest=monthlyInterest; this.archived=archived; this.created=created; }
        public double remaining() { return Math.max(0, amount - paid); }
        public double interestTotal() { return Math.max(0, amount - principal); }
        public double monthlyBalance() { return monthlyBalanceForPaid(paid); }
        public double monthlyBalanceAfter(double additionalPayment) { return monthlyBalanceForPaid(Math.min(amount, paid + Math.max(0, additionalPayment))); }
        private double monthlyBalanceForPaid(double paidTotal) {
            double balance = Math.max(0, amount - paidTotal);
            if (monthlyAmount <= 0 || balance <= 0) return balance;
            long monthlyCentavos = Math.max(1, Math.round(monthlyAmount * 100));
            long paidCentavos = Math.round(paidTotal * 100);
            long paidInCurrentCycle = paidCentavos % monthlyCentavos;
            long dueCentavos = paidInCurrentCycle == 0 ? monthlyCentavos : monthlyCentavos - paidInCurrentCycle;
            return Math.min(balance, dueCentavos / 100.0);
        }
    }
    public static class Payment {
        public final double amount; public final long paidAt; public final String note;
        Payment(double amount, long paidAt, String note) { this.amount=amount; this.paidAt=paidAt; this.note=note; }
    }
    public static class Activity { public final String title, detail; public final long created; Activity(String title,String detail,long created){this.title=title;this.detail=detail;this.created=created;} }
}
